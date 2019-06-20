package com.innodealing.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.innodealing.constant.CommonConstant;
import com.innodealing.constant.ResponseData;
import com.innodealing.dao.BondIndustryClassificationDao;
import com.innodealing.dao.FinanceSheetFieldMapDao;
import com.innodealing.domain.BondCcxeFinaSheetSyncBookMark;
import com.innodealing.domain.BondChangedFinaSheetItem;
import com.innodealing.domain.BondFinaSheetId;
import com.innodealing.domain.BondFinaSheetSyncParam;
import com.innodealing.domain.BondFinaSheetTimestamp;
import com.innodealing.exception.BusinessException;
import com.innodealing.repository.BondCcxeFinaSheetSyncRepo;
import com.innodealing.repository.BondCcxeFinaSheetTimeStampRepo;
import com.innodealing.util.SafeUtils;
import com.innodealing.util.StringUtils;

/**
 * 财报数据同步服务
 * 
 * 将中诚信的财报数据来初始化/增量构建dm的财报数据库
 * 接口分两类，全量构建以t_bond_com_ext中的全部发行主体为基础，构建2007年开始到接口执行日期之间所有财报数据
 * 增量构建根据中诚信六个财报数据表最新的ccxeid，对比mongo中记录的最后一次构建的ccxeid, 构建其中发生变化的财报数据。
 * 
 * 系统上线后，有两种选择: (1)是先执行一次全量构建，再定时执行增量构建
 * (2)是先执行一次书签同步，再定时执行增量构建，这时上线时点前的财报数据不再重新同步和计算评级
 * 
 * Note: bond_ccxe下的是中诚信的财报数据表，dmdb下的t_bond_xxx_fina_sheet是评级用到的dm内部财报数据表，
 * 这些表的设计 完全克隆了原来asbrs下安硕定义的几张财报表 (xxx_fina_sheet)
 * 
 * @author yig
 */
@Service
public class BondFinaSheetSyncService {

	static final Logger LOG = LoggerFactory.getLogger(BondFinaSheetSyncService.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private BondCcxeFinaSheetSyncRepo finaSheetSyncRepo;

	@Autowired
	private BondCcxeFinaSheetTimeStampRepo finaSheetTimestampRepo;

	@Autowired
	private CreditRatingCalculateService creditRatingService;

	@Autowired
	private FinanceSheetFieldMapDao finaSheetFieldMapDao;

	@Autowired
	private BondIndustryClassificationDao industryClassificationDao;

	class DependencyCheck {
		public void reportError(BondFinaSheetSyncParam param, String error, String modelId, String field,
				String detail) {
			String errorReportSql = "insert into dmdb.t_bond_credit_rating_depency_problem "
					+ " (com_uni_code, fin_date, error, model_id, field, detail  ) values (?, ?, ?, ?, ?, ? )";
			jdbcTemplate.update(errorReportSql,
					new Object[] { param.getComUniCode(), param.getEndDate(), error, modelId, field, detail });
		}
	}

	private DependencyCheck dependencyCheck = new DependencyCheck();

	public enum ProcFinSheetRet {
		E_RATING_OK(1), E_RATING_ERR(2), E_SYN_OK(3), E_NO_CCXE_FIN_SHEET_EXIST(4), E_NO_COMP_EXIST(5), E_OTHER(6);
		private Integer value;

		ProcFinSheetRet(Integer value) {
			this.value = value;
		}

		public Integer code() {
			return this.value;
		}

		public String toString() {
			return this.name();
		}
	}

	class SyncStatistics {
		@Autowired
		Environment environment;

		public void addProcessResult(ProcFinSheetRet ret, BondFinaSheetSyncParam param) {
			try {
				if (!statisticsMap.containsKey(ret)) {
					statisticsMap.put(ret, new AtomicInteger(0));
				}
				statisticsMap.get(ret).incrementAndGet();
				String hostInfo = SafeUtils.getHostName();
				String errorReportSql = "insert into dmdb.t_bond_finance_ccxe_sync_log "
						+ " (ama_com_id, com_uni_code, fin_date, error_info, host_info, fina_sheet_table ) values (?, ?, ?, ?, ?, ? )";
				jdbcTemplate.update(errorReportSql, new Object[] { param.getAmaComId(), param.getComUniCode(),
						param.getEndDate(), ret.name(), hostInfo, param.getFinaSheetTableName() });
			} catch (Exception e) {
				LOG.error("failed to addProcessResult", e);
			}
		}

		public AtomicInteger getStatisticsInfo(ProcFinSheetRet ret) {
			return statisticsMap.get(ret);
		}

		public void printLog() {
			LOG.info("------------SyncStatistics start:-------------------");
			for (Map.Entry<ProcFinSheetRet, AtomicInteger> entry : statisticsMap.entrySet()) {
				ProcFinSheetRet key = entry.getKey();
				LOG.info(key.name() + " count:" + entry.getValue());
			}
			LOG.info("------------SyncStatistics end-------------------");
		}

		private Map<ProcFinSheetRet, AtomicInteger> statisticsMap = new HashMap<ProcFinSheetRet, AtomicInteger>();
	}

	final private String CREDIT_RATING_DATA_SROUCE_CCXE = "0";
	final private Integer BATCH_PROCESS_THREAD_COUNT = 20;

	// 源数据表: CCXE的财务表
	final static private String CCXE_FIN_BALA_SHEET = "bond_ccxe.d_bond_fin_fal_bala_tafbb";
	final static private String CCXE_FIN_CASH_SHEET = "bond_ccxe.d_bond_fin_fal_cash_tafcb";
	final static private String CCXE_FIN_PROF_SHEET = "bond_ccxe.d_bond_fin_fal_prof_tafpb";
	final static private String CCXE_GEN_BALA_SHEET = "bond_ccxe.d_bond_fin_gen_bala_tacbb";
	final static private String CCXE_GEN_CASH_SHEET = "bond_ccxe.d_bond_fin_gen_cash_taccb";
	final static private String CCXE_GEN_PROF_SHEET = "bond_ccxe.d_bond_fin_gen_prof_tacpb";

	final static private List<String> ccxeFinaSheetTables = new ArrayList<String>();
	{
		ccxeFinaSheetTables.add(CCXE_FIN_BALA_SHEET);
		ccxeFinaSheetTables.add(CCXE_FIN_CASH_SHEET);
		ccxeFinaSheetTables.add(CCXE_FIN_PROF_SHEET);
		ccxeFinaSheetTables.add(CCXE_GEN_BALA_SHEET);
		ccxeFinaSheetTables.add(CCXE_GEN_CASH_SHEET);
		ccxeFinaSheetTables.add(CCXE_GEN_PROF_SHEET);
	}

	// 目标数据表: 安硕dm的财务表
	final static private String DMDB_INDU_FINA_SHEET = "dmdb.t_bond_manu_fina_sheet";
	final static private String DMDB_INSU_FINA_SHEET = "dmdb.t_bond_insu_fina_sheet";
	final static private String DMDB_SECU_FINA_SHEET = "dmdb.t_bond_secu_fina_sheet";
	final static private String DMDB_BANK_FINA_SHEET = "dmdb.t_bond_bank_fina_sheet";
	final static private String DMDB_FINA_SHEET_PREFIX = "dmdb.t_bond_";

	static private Map<String, Map<String, String>> dmdbFinaSheetFieldsMap = new HashMap<String, Map<String, String>>();
	static private Map<String, String> dmdbFinaSheetSqlFieldsStringMap = new HashMap<String, String>();

	@PostConstruct
	public void initialize() {
		initFinaSheetFieldMap(DMDB_INDU_FINA_SHEET);
		initFinaSheetFieldMap(DMDB_INSU_FINA_SHEET);
		initFinaSheetFieldMap(DMDB_BANK_FINA_SHEET);
		initFinaSheetFieldMap(DMDB_SECU_FINA_SHEET);
	}

	private void initFinaSheetFieldMap(String dmFinaSheetName) {
		dmdbFinaSheetFieldsMap.put(dmFinaSheetName,
				finaSheetFieldMapDao.findFieldMap(getOrgFinaSheetName(dmFinaSheetName)));
	}

	private String getOrgFinaSheetName(String dmFinaSheetName) {
		return dmFinaSheetName.substring(DMDB_FINA_SHEET_PREFIX.length(), dmFinaSheetName.length());
	}

	public String checkRatingDependencyAll() {
		Set<BondFinaSheetSyncParam> finaSheets = findAllFinaSheets(false);
		for (final BondFinaSheetSyncParam finaSheet : finaSheets) {
			try {
				checkRatingDependency(finaSheet);
			} catch (Exception ex) {
				LOG.error("finaSheet problem, finaSheet:" + finaSheet.toString(), ex);
				dependencyCheck.reportError(finaSheet, "finaSheet problem", "", "", ex.toString());
			}
		}
		return "";
	}

	public void checkRatingDependency(BondFinaSheetSyncParam finaSheet) {

		// LOG.info("sync finance sheet:" + finaSheet.toString());
		String finaSheetTableName = finaSheet.getFinaSheetTableName();
		if (StringUtils.isBlank(finaSheetTableName)) {
			LOG.error("issuer not exist in t_bond_com_ext table, comUniCode:" + finaSheet.getComUniCode());
			return;
		}

		String sqlQueryFormat = "select ext.com_uni_code, ext.com_chi_name, c.model_id, D.dependency from dmdb.t_bond_com_ext ext left join dmdb.t_bond_industry_classification c ON c.industry_code = ext.indu_uni_code_l4\r\n"
				+ "left join dmdb.t_bond_rating_dependency D on D.model_id = c.model_id\r\n"
				+ "WHERE ext.com_uni_code = %1$s limit 1";

		List<Map<String, Object>> dependencys = jdbcTemplate
				.queryForList(String.format(sqlQueryFormat, finaSheet.getComUniCode()));
		if (dependencys == null || dependencys.isEmpty()) {
			dependencyCheck.reportError(finaSheet, "dependency rel not exist", "", "", "");
			LOG.error("dependency rel not exist, comUniCode:" + finaSheet.getComUniCode());
			return;
		}

		Map<String, Object> dependencyFields = dependencys.get(0);
		String nativeQueryFields = (String) dependencyFields.get("dependency");
		String[] queryFieldArray = nativeQueryFields.split("\\,");
		String queryFields = "";
		for (String f : queryFieldArray) {
			if (f.startsWith("pp") || f.startsWith("INDU_SCORE") || f.startsWith("COM_ATTR_PAR") || f.startsWith("ITN")
					|| f.startsWith("BTN")) {
				continue;
			}
			if (!queryFields.isEmpty())
				queryFields += ",";
			queryFields += f;
		}
		String modelId = (String) dependencyFields.get("model_id");

		// select
		// BS004,BS301,BS302,BS311,BS313,BS401,BS402,BS403,BS001,PL400,CF001,PL220,PL301,PL301_1,BS002,PL300
		// from dmdb.t_bond_manu_fina_sheet S where S.COMP_ID = 283 AND
		// S.FIN_DATE = '2016-09-30'
		String depQueryFormat = "select %1$s from %2$s where COMP_ID = %3$d AND FIN_DATE = '%4$s'";
		String depQuery = String.format(depQueryFormat, queryFields, finaSheet.getFinaSheetTableName(),
				finaSheet.getAmaComId(), SafeUtils.convertDateToString(finaSheet.getEndDate(), SafeUtils.DATE_FORMAT));

		List<Map<String, Object>> list;
		// 查询对应的财报字段
		try {
			list = jdbcTemplate.queryForList(depQuery);
		} catch (Exception ex) {
			dependencyCheck.reportError(finaSheet, "finaSheet problem", modelId, "", "");
			LOG.error("finaSheet problem, finaSheet:" + finaSheet.toString(), ex);
			return;
		}

		if (null == list || list.isEmpty()) {
			// LOG.warn("# finaSheet does not exist:" + finaSheet.toString() +
			// ", sql:" + sql );
			// saveCcxeTimeStamp(finaSheet);
			dependencyCheck.reportError(finaSheet, "finaSheet not exist", modelId, "", "");
			LOG.error("finaSheet not exist, finaSheet:" + finaSheet.toString());
		}

		for (Map<String, Object> fieldMap : list) {
			for (Map.Entry<String, Object> entry : fieldMap.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (value == null) {
					dependencyCheck.reportError(finaSheet, "field miss", modelId, key, "");
					LOG.error("field miss, finaSheet:" + finaSheet.toString() + ", field:" + key);
				}
			}
		}
	}

	/**
	 * 
	 * processChangedFinaSheets:(【增量】将财报数据从中诚信库同步到dmdb)
	 * @param isManuFinaSheetOnly 
	 * 
	 * @param @param
	 *            requireCreditRating 是否需要再评级
	 * @param @return
	 * @return String
	 * @throws @since
	 *             CodingExample Ver 1.1
	 */
	public String processChangedFinaSheets(Boolean requireCreditRating, Boolean isManuFinaSheetOnly) throws InterruptedException {

		// 出于性能考虑，如果发现mongo中没有上一次构建过程留下的书签数据，那么不建议直接开始增量构建
		long ccxeBookMarkCount = finaSheetSyncRepo.count();
		if (ccxeBookMarkCount < ccxeFinaSheetTables.size()) {
			return "failed, mongo bond_ccxe_fina_sheet_sync's count is invalid:" + ccxeBookMarkCount;
		}

		// 构建中间变量 (必须是concurrent数据结构，因为会被batchProcessFinaSheet多线程访问)，
		// 用于在currentProcessChangedFinaSheetsByTables 和 batchProcessFinaSheet之间传递
		// bookmarkMap为书签位置，changedFinaSheets为变化过的财报
		Map<String, BondCcxeFinaSheetSyncBookMark> bookmarkMap = 
				new ConcurrentHashMap<String, BondCcxeFinaSheetSyncBookMark>();
		Map<BondFinaSheetId, BondFinaSheetSyncParam> changedFinaSheetMap = 
				new ConcurrentHashMap<BondFinaSheetId, BondFinaSheetSyncParam>();

		// 根据上次记录书签，找到中诚信的财报更新记录
		currentProcessChangedFinaSheetsByTables(changedFinaSheetMap, bookmarkMap, isManuFinaSheetOnly);

		// 将更新过的记录更新到财报数据表
		Set<BondFinaSheetSyncParam> changedFinaSheetSet = Collections
				.newSetFromMap(new ConcurrentHashMap<BondFinaSheetSyncParam, Boolean>());
		changedFinaSheetSet.addAll(changedFinaSheetMap.values());
		batchProcessFinaSheet(changedFinaSheetSet, requireCreditRating);

		// 保存最新的书签状态
		for (Map.Entry<String, BondCcxeFinaSheetSyncBookMark> entry : bookmarkMap.entrySet()) {
			saveCcxeBookmark(entry.getKey(), entry.getValue().getCcxeId(), entry.getValue().getComUniCode(),
					entry.getValue().getEndDate());
		}

		return "success";
	}

	/**
	 * 
	 * processAllFinaSheet:(【全量】将财报数据从中诚信库同步到dmdb/慢～ 慎用:全量数据同步完，才能计算指标评级)
	 * @param isManuFinaSheetOnly 
	 * 
	 * @param @return
	 * @param @throws
	 *            InterruptedException
	 * @return String
	 * @throws @since
	 *             CodingExample Ver 1.1
	 */
	public String processAllFinaSheet(Boolean isManuFinaSheetOnly) throws InterruptedException {
		batchProcessFinaSheet(findAllFinaSheets(isManuFinaSheetOnly), false);
		updateCcxeBookMark();
		return "success";
	}

	public String processFinaSheetByQuarter(Date finDate, Boolean requireCreditRating, Boolean isManuFinaSheetOnly) 
	{
		batchProcessFinaSheet(findFinaSheets(null, finDate, isManuFinaSheetOnly), requireCreditRating);
		updateCcxeBookMark();
		return "success";
	}

	/**
	 * 
	 * processIssuerFinaSheet:(【指定主体和财报时间】将财报数据从中诚信库同步到dmdb:如果要计算一个主体多期财报，则需要同步完再单独计算)
	 * 
	 * @param @param
	 *            comUniCode 是否需要再评级
	 * @param @param
	 *            endDate
	 * @param @param
	 *            requireCreditRating
	 * @param @return
	 * @param @throws
	 *            InterruptedException 设定文件
	 * @return String DOM对象
	 * @throws @since
	 *             CodingExample Ver 1.1
	 */
	public String processIssuerFinaSheet(Long comUniCode, Date endDate, Boolean requireCreditRating)
			throws InterruptedException {
	    batchProcessFinaSheet(findFinaSheets(comUniCode, endDate, false), requireCreditRating);
		// note：对单个主体/财报的刷新不更新书签
		return "success";
	}

	// 将中诚信最新ccxeid设置成下次增量的起始位置, 该接口只同步ccxeid, 不会重新同步财报数据
	public String syncCcxeId() {
		updateCcxeBookMark();
		return "success";
	}

	private Set<BondFinaSheetSyncParam> findAllFinaSheets(Boolean isManuFinaSheetOnly) {
		return findFinaSheets(null, null, isManuFinaSheetOnly);
	}

	/**
	 * 根据指定的主体/财报参数找到对应的财报信息（比较重要的比如保存在哪个表，安硕的主体主键）
	 * @param isManuFinaSheetOnly 
	 * 
	 * @param @param
	 *            comUniCode 中诚信主体主键
	 * @param @param
	 *            endDate 财报时间
	 * @return void
	 */
	private Set<BondFinaSheetSyncParam> findFinaSheets(Long comUniCode, Date endDate, Boolean isManuFinaSheetOnly) {

		final String QUERY_FORMAT = "SELECT a.com_uni_code, a.ama_com_id, case when LOCATE('bank', b.model_id)>0 then 'dmdb.t_bond_bank_fina_sheet' \r\n"
				+ "					when LOCATE('secu', b.model_id)>0 then 'dmdb.t_bond_secu_fina_sheet' \r\n"
				+ "					when LOCATE('insu', b.model_id)>0 then  'dmdb.t_bond_insu_fina_sheet' \r\n"
				+ "					else 'dmdb.t_bond_manu_fina_sheet' end as finaSheetTableName \r\n"
				+ "					FROM dmdb.t_bond_com_ext a left join dmdb.t_bond_industry_classification b \r\n"
				+ "					on a.indu_uni_code_l4=b.industry_code ";

		List<BondFinaSheetSyncParam> keys; 
		if (comUniCode == null) {
			String sql = isManuFinaSheetOnly? 
					(QUERY_FORMAT + " where b.model_id not in ('bank', 'secu', 'insu')"):QUERY_FORMAT;
			keys = jdbcTemplate.query(QUERY_FORMAT, new BeanPropertyRowMapper(BondFinaSheetSyncParam.class));
			if (keys == null || keys.size() <= 0) {
				throw new BusinessException("t_bond_com_ext is null");
			}
		} else {
			String sql = QUERY_FORMAT + " where a.com_uni_code=" + comUniCode + " GROUP BY a.ama_com_id ";// GROUP																						// 防止同一财报主体添加多次
			keys = jdbcTemplate.query(sql, new BeanPropertyRowMapper(BondFinaSheetSyncParam.class));
			if (keys == null || keys.size() <= 0) {
				throw new BusinessException("t_bond_com_ext is null");
			}
		}

		Set<BondFinaSheetSyncParam> finaSheets = new HashSet<BondFinaSheetSyncParam>();
		for (BondFinaSheetSyncParam key : keys) {
			if (endDate == null) {
				final Integer yearStart = 2007;
				Integer yearEnd = Calendar.getInstance().get(Calendar.YEAR);
				String quarters[] = { "03-31", "06-30", "09-30", "12-31" };
				for (Integer year = yearStart; year <= yearEnd; ++year) {
					for (String quarter : quarters) {
						String finDate = String.format("%1$d-%2$s", year, quarter);
						finaSheets.add(new BondFinaSheetSyncParam(key.getComUniCode(), key.getAmaComId(),
								SafeUtils.parseDate(finDate, SafeUtils.DATE_FORMAT), key.getFinaSheetTableName()));
					}
				}
			} else {
				finaSheets.add(new BondFinaSheetSyncParam(key.getComUniCode(), key.getAmaComId(), endDate,
						key.getFinaSheetTableName()));
			}
		}
		return finaSheets;
	}

	// 根据上次记录书签，找到中诚信的财报更新记录
	private void currentProcessChangedFinaSheetsByTables(Map<BondFinaSheetId, BondFinaSheetSyncParam> changedFinaSheets,
			Map<String, BondCcxeFinaSheetSyncBookMark> bookmarkMap, Boolean isManuFinaSheetOnly) {
		ExecutorService pool = Executors.newFixedThreadPool(ccxeFinaSheetTables.size());
		for (String tableName : ccxeFinaSheetTables) {
			pool.execute(new Runnable() {
				@Override
				public void run() {
					processChangedFinaSheetsByTable(tableName, changedFinaSheets, bookmarkMap, isManuFinaSheetOnly);
				}
			});
		}
		pool.shutdown();
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			LOG.error("等待任务完成中发生异常 ", e);
			e.printStackTrace();
		}
	}

	// 同步有改动或新增的中诚信财报数据到dmdb
	private void processChangedFinaSheetsByTable(String tableName,
			Map<BondFinaSheetId, BondFinaSheetSyncParam> changedFinaSheets,
			Map<String, BondCcxeFinaSheetSyncBookMark> bookmarkMap, Boolean isManuFinaSheetOnly) {
		BondCcxeFinaSheetSyncBookMark bookMark = finaSheetSyncRepo.findOne(tableName);
		if (bookMark != null) {
			String sqlFormat = "select T1.CCXEID, T1.COM_UNI_CODE, T1.END_DATE, T2.ama_com_id, T2.finaSheetTableName , '%2$s' as ccxeFinaSheetTableName from \r\n"
					+ "(\r\n" + "	select  T.CCXEID, T.COM_UNI_CODE, T.END_DATE from %2$s T \r\n"
					+ "					where %1$s T.ISVALID = 1 and SHEET_MARK_PAR = 1 \r\n"
					+ "					group by T.COM_UNI_CODE, T.END_DATE \r\n"
					+ "					order by T.COM_UNI_CODE, T.END_DATE \r\n" + ") T1\r\n" + "inner join\r\n"
					+ "(\r\n"
					+ "	SELECT a.com_uni_code, a.ama_com_id, case when LOCATE('bank', b.model_id)>0 then 'dmdb.t_bond_bank_fina_sheet' \r\n"
					+ "				when LOCATE('secu', b.model_id)>0 then 'dmdb.t_bond_secu_fina_sheet' \r\n"
					+ "				when LOCATE('insu', b.model_id)>0 then 'dmdb.t_bond_insu_fina_sheet' \r\n"
					+ "				else 'dmdb.t_bond_manu_fina_sheet' end as finaSheetTableName \r\n"
					+ "				FROM dmdb.t_bond_com_ext a left join dmdb.t_bond_industry_classification b \r\n"
					+ "				on a.indu_uni_code_l4=b.industry_code "
					+ ((isManuFinaSheetOnly)? " where b.model_id not in ('bank', 'secu', 'insu')" : "")
					+ "             GROUP BY a.ama_com_id \r\n" + ") T2 \r\n"
					+ "on T1.com_uni_code = T2.com_uni_code\r\n" + "order by T1.CCXEID desc, T1.COM_UNI_CODE desc\r\n";

			String lastCcxeId = SafeUtils.convertDateToString(bookMark.getCcxeId(), SafeUtils.DATE_TIME_FORMAT1);
			String ccxeidCond = String.format("T.CCXEID >= '%1$s' and", lastCcxeId);
			String sql = String.format(sqlFormat, ccxeidCond, tableName);
			LOG.debug("finding changed data, sql:" + sql);
			LOG.info("finding changed data, tableName:" + tableName + ", CCXEID>=" + lastCcxeId);
			List<BondChangedFinaSheetItem> matchedKeys = jdbcTemplate.query(sql,
					new BeanPropertyRowMapper(BondChangedFinaSheetItem.class));
			if (null == matchedKeys) {
				// LOG.warn("数据异常，检查数据, SQL:" + sql);
				return;
			} else if (matchedKeys.isEmpty()) {
				LOG.warn("数据状态异常, tableName:" + tableName);
				return;
			} else {
				for (BondChangedFinaSheetItem key : matchedKeys) {
					// LOG.info("update >> " + key.toString());
					String endDate = SafeUtils.convertDateToString(key.getEndDate(), SafeUtils.DATE_FORMAT);
					int lastUnderScore1 = tableName.lastIndexOf("_");
					int lastUnderScore2 = tableName.lastIndexOf("_", tableName.lastIndexOf("_") - 1) + 1;
					String type = tableName.substring(lastUnderScore2, lastUnderScore1);
					String finaSheetTimestampRepoKey = key.getComUniCode() + "#" + endDate + "#" + type;

					BondFinaSheetTimestamp timestamp = finaSheetTimestampRepo.findOne(finaSheetTimestampRepoKey);
					if (timestamp != null) {
						if (key.getCcxeId().compareTo(timestamp.getTimeStamp()) > 0) {
							addChangedFinaSheet(key, type, changedFinaSheets);
							LOG.info("found update, tableName:" + tableName + ", finaSheetTimestampRepoKey:"
									+ finaSheetTimestampRepoKey + ", new ccxeid:" + key.getCcxeId() + ", last ccxeid:"
									+ timestamp.getTimeStamp());
						}
					} else {
						addChangedFinaSheet(key, type, changedFinaSheets);
						LOG.info("found update, tableName:" + tableName + ", key:" + key);
					}
				}
				// 更新财报表同步书签
				BondChangedFinaSheetItem lastRow = matchedKeys.get(0);
				bookMark.setCcxeId(lastRow.getCcxeId());
				bookMark.setComUniCode(lastRow.getComUniCode());
				bookMark.setEndDate(lastRow.getEndDate());
				bookmarkMap.put(tableName, bookMark);
			}
		} else {
			LOG.error("数据异常，mongo bookmark invalid, key:" + tableName);
		}
	}

	private void addChangedFinaSheet(BondChangedFinaSheetItem key, String type,
			Map<BondFinaSheetId, BondFinaSheetSyncParam> changedFinaSheets) {
		BondFinaSheetId sheetId = new BondFinaSheetId(key.getComUniCode(), key.getEndDate());
		BondFinaSheetSyncParam syncParam = changedFinaSheets.get(sheetId);
		if (syncParam == null) {
			syncParam = new BondFinaSheetSyncParam();
			BeanUtils.copyProperties(key, syncParam);
			changedFinaSheets.put(sheetId, syncParam);
		}
		syncParam.getCcxeidMap().put(type, key.getCcxeId());
	}

	
	/**
	 * 
	 * batchProcessFinaSheet:(采用线程池批量同步中诚信财报到dmdb：注意先同步再调用评级接口)
	 * @param  @param finaSheets
	 * @param  @param requireCreditRating    设定文件
	 * @return void    DOM对象
	 * @throws 
	 * @since  CodingExample　Ver 1.1
	 */
	private void batchProcessFinaSheet(Set<BondFinaSheetSyncParam> finaSheets, Boolean requireCreditRating) {
	    if(null == finaSheets || finaSheets.size()==0) {
	        return;
	    }
	    SyncStatistics syncStatistics = new SyncStatistics();
		ExecutorService pool = Executors.newFixedThreadPool(BATCH_PROCESS_THREAD_COUNT);
		for (final BondFinaSheetSyncParam key : finaSheets) {
			pool.execute(new Runnable() {
				@Override
				public void run() {
					StopWatch watch = new StopWatch();
					watch.start();
					syncStatistics.addProcessResult(processFinaSheet(key), key);
					watch.stop();
					LOG.info("processFinaSheet done, " + key.toString() + ", elapsed millis:"
							+ watch.getTotalTimeMillis());
				}
			});
		}
		pool.shutdown();
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);//请求关闭、发生超时或者当前线程中断，无论哪一个首先发生之后，都将导致阻塞，直到所有任务完成执行
			//同步结束后计算财报评级和质量
			if(requireCreditRating) {
			    addCalculateSheet(finaSheets);
            }
		} catch (InterruptedException e) {
			LOG.error("等待任务完成中发生异常 ", e);
			e.printStackTrace();
		}
		syncStatistics.printLog();
	}

	/**
	 * 从中诚信同步指定的一期财报数据，并重新评级
	 * 
	 * @param @param
	 *            finaSheet 财报主键
	 * @param @param
	 *            requireCreditRating 是否需要重新评级（出于性能要求某些情况不需要计算评级）
	 * @return void
	 */
	public ProcFinSheetRet processFinaSheet(BondFinaSheetSyncParam finaSheet) {
		// LOG.info("sync finance sheet:" + finaSheet.toString());

		// 找出需要同步的中诚信财报数据
		String finaSheetTableName = finaSheet.getFinaSheetTableName();
		if (StringUtils.isBlank(finaSheetTableName)) {
			LOG.warn("issuer not exist in t_bond_com_ext table, comUniCode:" + finaSheet.getComUniCode());
			return ProcFinSheetRet.E_NO_COMP_EXIST;
		}

		// 不同财报行业分类的表结构不同，这里根据分类生成不同的查询字段
		// 中诚信和安硕的字段映射通过数据库字段别名实现，即sql查询返回集已经使用了安硕的字段定义，比如 ccxeFieldA as
		// dmFieldA
		String queryFields = getFinaSheetQueryFields(finaSheet.getFinaSheetTableName());
		String sqlFormat = "select T1.CCXEID as ccxeid_bala, T2.CCXEID AS ccxeid_cash, T3.CCXEID AS ccxeid_prof, %1$s from %4$s T1 \r\n"
				+ "	INNER JOIN  %5$s T2 ON T1.COM_UNI_CODE = T2.COM_UNI_CODE AND T1.END_DATE = T2.END_DATE AND T1.SHEET_MARK_PAR = T2.SHEET_MARK_PAR  AND T1.ISVALID = T2.ISVALID   \r\n"
				+ "	INNER JOIN  %6$s T3 ON T2.COM_UNI_CODE = T3.COM_UNI_CODE AND T2.END_DATE = T3.END_DATE AND T2.SHEET_MARK_PAR = T3.SHEET_MARK_PAR AND T2.ISVALID = T3.ISVALID	\r\n"
				+ "	where T1.COM_UNI_CODE = %2$d and T1.END_DATE = '%3$s' AND T1.SHEET_MARK_PAR = 1 and T1.ISVALID = 1  \r\n"
				+ "	LIMIT 1";
		String balanceSheet = finaSheetTableName.equals(DMDB_INDU_FINA_SHEET) ? CCXE_GEN_BALA_SHEET
				: CCXE_FIN_BALA_SHEET;
		String cashSheet = finaSheetTableName.equals(DMDB_INDU_FINA_SHEET) ? CCXE_GEN_CASH_SHEET : CCXE_FIN_CASH_SHEET;
		String profSheet = finaSheetTableName.equals(DMDB_INDU_FINA_SHEET) ? CCXE_GEN_PROF_SHEET : CCXE_FIN_PROF_SHEET;
		String endDate = SafeUtils.convertDateToString(finaSheet.getEndDate(), SafeUtils.DATE_TIME_FORMAT1);
		String sql = String.format(sqlFormat, queryFields, finaSheet.getComUniCode(), endDate, balanceSheet, cashSheet,
				profSheet);

		// 查询对应的财报字段
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
		if (null == list || list.isEmpty()) {
			// LOG.warn("# finaSheet does not exist:" + finaSheet.toString() +
			// ", sql:" + sql );
			// saveCcxeTimeStamp(finaSheet);
			LOG.warn("@ finaSheet does not exist:" + finaSheet.toString() + ", sql:" + sql );
			return ProcFinSheetRet.E_NO_CCXE_FIN_SHEET_EXIST;
		}

		LOG.debug("@ finaSheet exist:" + finaSheet.toString() + ", sql:" + sql);
		// 根据查询结果生成update子句, 用于更新dmdb
		Map<String, Object> fieldMap = list.get(0);
		String fieldValueAssign = new String();
		for (Map.Entry<String, Object> entry : fieldMap.entrySet()) {
			String key = entry.getKey();
			if (!key.startsWith("ccxeid")) {
				BigDecimal value = (BigDecimal) entry.getValue();
				if (value != null) {
					if (!fieldValueAssign.isEmpty())
						fieldValueAssign += ",";
					fieldValueAssign += String.format("%1$s=%2$f", key, value);
				}
			}
		}

		// 执行dmdb数据更新, 如果存在即更新否则插入
		String finDate = SafeUtils.convertDateToString(finaSheet.getEndDate(), SafeUtils.DATE_FORMAT);
		if (isFinaSheetExist(finaSheet.getAmaComId(), finDate, finaSheetTableName)) {
			String updateSqlFormat = "update %1$s SET %2$s, last_update_timestamp=now() where COMP_ID=%3$d AND FIN_DATE = '%4$s' and VISIBLE=1";
			String updateSql = String.format(updateSqlFormat, finaSheetTableName, fieldValueAssign,
					finaSheet.getAmaComId(), finDate);
			LOG.debug("SQL>>" + updateSql);
			jdbcTemplate.update(updateSql);
		} else {
			String insertSqlFormat = "insert into %1$s SET %2$s, COMP_ID=%3$d, FIN_DATE='%4$s', FIN_ENTITY='1', FIN_STATE_TYPE='HR', FIN_PERIOD=%5$d, last_update_timestamp=now(), VISIBLE=1 ";
			String insertSql = String.format(insertSqlFormat, finaSheetTableName, fieldValueAssign,
					finaSheet.getAmaComId(), finDate, finaSheet.getEndDate().getMonth() + 1);
			LOG.debug("SQL>>" + insertSql);
			jdbcTemplate.update(insertSql);
		}

		saveCcxeTimeStamp(finaSheet, "bala", (Date) fieldMap.get("ccxeid_bala"));
		saveCcxeTimeStamp(finaSheet, "cash", (Date) fieldMap.get("ccxeid_cash"));
		saveCcxeTimeStamp(finaSheet, "prof", (Date) fieldMap.get("ccxeid_prof"));

		return ProcFinSheetRet.E_SYN_OK;
	}

	private void saveCcxeTimeStamp(BondFinaSheetSyncParam finaSheet, String type, Date ccxeId) {

		String timeStampKey = finaSheet.getComUniCode() + "#"
				+ SafeUtils.convertDateToString(finaSheet.getEndDate(), SafeUtils.DATE_FORMAT) + "#" + type;
		Date lastCcxeId = finaSheet.getCcxeidMap().containsKey(type) ? finaSheet.getCcxeidMap().get(type) : ccxeId;

		BondFinaSheetTimestamp timestamp = new BondFinaSheetTimestamp();
		timestamp.setComUniCodeData(timeStampKey);
		timestamp.setTimeStamp(lastCcxeId);
		finaSheetTimestampRepo.save(timestamp);

		LOG.debug("issuer timestamp saved, key:" + timeStampKey + ", lastCcxeId:" + lastCcxeId);
	}

	private String MD5(String md5) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}

	private boolean isFinaSheetExist(Long compId, String finDate, String finaSheetTableName) {
		String sqlQuery = String.format(
				"select count(1) from %1$s where COMP_ID=%2$d AND FIN_DATE ='%3$s' AND VISIBLE=1", finaSheetTableName,
				compId, finDate);
		Integer cnt = jdbcTemplate.queryForObject(sqlQuery, Integer.class);
		return cnt != null && cnt > 0;
	}

	public String getFinaSheetTableName(Long comUniCode) {
		String sqlFormat = "SELECT a.ama_com_id, case when LOCATE('bank', b.model_id)>0 then '%1$s' \r\n"
				+ "				when LOCATE('secu', b.model_id)>0 then '%2$s' \r\n"
				+ "				when LOCATE('insu', b.model_id)>0 then '%3$s' \r\n"
				+ "				else '%4$s' end as finaSheetTableName \r\n"
				+ "				FROM t_bond_com_ext a left join t_bond_industry_classification b \r\n"
				+ "				on a.indu_uni_code_l4=b.industry_code where a.com_uni_code = %5$d LIMIT 1;\r\n"
				+ "				";
		List<String> list = jdbcTemplate.query(String.format(sqlFormat, DMDB_BANK_FINA_SHEET, DMDB_SECU_FINA_SHEET,
				DMDB_INSU_FINA_SHEET, DMDB_INDU_FINA_SHEET, comUniCode), new BeanPropertyRowMapper(String.class));
		if (null == list || list.isEmpty()) {
			throw new BusinessException("unknow comUniCode:" + comUniCode);
		}
		return list.get(0);
	}

	public String getFinaSheetQueryFields(String finaSheetTableName) {
		if (!dmdbFinaSheetSqlFieldsStringMap.containsKey(finaSheetTableName)) {
			String queryFields = new String();
			Map<String, String> fieldMap = dmdbFinaSheetFieldsMap.get(finaSheetTableName);
			for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (!queryFields.isEmpty())
					queryFields += ",";
				queryFields += String.format("%1$s as %2$s", key, value);
			}
			dmdbFinaSheetSqlFieldsStringMap.put(finaSheetTableName, queryFields);
		}
		return dmdbFinaSheetSqlFieldsStringMap.get(finaSheetTableName);
	}

	private void saveCcxeBookmark(String ccxeFinaSheetTableName, Date ccxeId, Long comUniCode, Date endDate) {
		BondCcxeFinaSheetSyncBookMark entity = new BondCcxeFinaSheetSyncBookMark();
		entity.setCcxeFinaSheetTableName(ccxeFinaSheetTableName);
		entity.setCcxeId(ccxeId);
		entity.setComUniCode(comUniCode);
		entity.setEndDate(endDate);
		finaSheetSyncRepo.save(entity);
		LOG.info("bookmark saved, tableName:" + ccxeFinaSheetTableName + ", ccxeId:" + ccxeId);
	}

	private void updateCcxeBookMark() {
		for (String tableName : ccxeFinaSheetTables) {
			String sqlFormat = "select ccxeid, com_uni_code, end_date from %1$s order by ccxeid desc, com_uni_code, end_date desc limit 1";
			String sql = String.format(sqlFormat, tableName);
			List<BondCcxeFinaSheetSyncBookMark> bookMarks = jdbcTemplate.query(sql,
					new BeanPropertyRowMapper(BondCcxeFinaSheetSyncBookMark.class));
			if (null == bookMarks || bookMarks.isEmpty()) {
				LOG.error("数据异常，检查数据, SQL:" + sql);
			}
			BondCcxeFinaSheetSyncBookMark e = bookMarks.get(0);
			LOG.info("find max ccxeId:" + e.getCcxeId() + ", comUniCode:" + e.getComUniCode() + ", tableName:"
					+ tableName);
			saveCcxeBookmark(tableName, e.getCcxeId(), e.getComUniCode(), e.getEndDate());
		}
	}
	
	/**
	 * 
	 * addCalculate:(计算财报评级质量)
	 * @param  @param finaSheets    设定文件
	 * @return void    DOM对象
	 * @throws InterruptedException 
	 * @throws 
	 * @since  CodingExample　Ver 1.1
	 */
	private void addCalculateSheet(final Set<BondFinaSheetSyncParam> finaSheets) throws InterruptedException {
	    ThreadPoolExecutor threadpool = new ThreadPoolExecutor(1, 15, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(finaSheets.size())); 
        List<Future<ResponseData>> calculationResultList = new ArrayList<Future<ResponseData>>(finaSheets.size());
        for(BondFinaSheetSyncParam finaSheet : finaSheets) {
            calculationResultList.add(threadpool.submit(new Callable<ResponseData>() {
                @Override
                public ResponseData call() {
                    return creditRatingService.addCalculate(finaSheet.getAmaComId(), finaSheet.getEndDate(),
                            CREDIT_RATING_DATA_SROUCE_CCXE);
                }
            }));
        }
        // 关闭线程池:会等待所有线程执行完
        threadpool.shutdown();
        threadpool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        for (Future<ResponseData> res : calculationResultList) {
            try {
                if (!CommonConstant.DMCallbackCode.NOMAL_RETURN.test(res.get().getResponseCode())) {
                    LOG.error("calculate credit rating failed: "+res.get().getResponseMessage());
                    break;
                } 
            } catch (Exception e) {
                LOG.error("calculate credit rating failed: "+e.getMessage());
                
            }
        }
	}

}
