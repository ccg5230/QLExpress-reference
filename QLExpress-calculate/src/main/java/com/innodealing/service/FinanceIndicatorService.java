package com.innodealing.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.innodealing.amqp.BondAmqpSender;
import com.innodealing.constant.CommonConstant;
import com.innodealing.dao.BondComExtDao;
import com.innodealing.dao.IndicatorDao;
import com.innodealing.datasource.DataSource;
import com.innodealing.datasource.DataSourceBank;
import com.innodealing.datasource.DataSourceIndu;
import com.innodealing.datasource.DataSourceInsu;
import com.innodealing.datasource.DataSourceSecu;
import com.innodealing.engine.OriginalData;
import com.innodealing.engine.innodealing.DateConvertUtil;
import com.innodealing.exception.BusinessException;
import com.innodealing.json.IndicatorCalculateTask;
import com.innodealing.vo.BondComExtVo;
import com.innodealing.vo.ExpressionVo;


/**
 * 主体财务指标接口
 * @author 赵正来
 *
 */
@Service
public class FinanceIndicatorService {
	
	@Autowired private DataSourceBank dataSourceBank;
	
	@Autowired private DataSourceIndu dataSourceIndu;
	
	@Autowired private DataSourceInsu dataSourceInsu;
	
	@Autowired private DataSourceSecu dataSourceSecu;
	
	@Autowired private IndicatorDao indicatorDao;
	
	@Autowired private BondComExtDao bondComExtDao;
	
	@Autowired private BondAmqpSender amqpSender;
	
	@Value("${calculate.indu.use-dist-work}")
	private Boolean useMqDistWork;
	
	public static final Logger log = LoggerFactory.getLogger(FinanceIndicatorService.class);
	
	public static Map<String, String> mapConstant = new HashMap<>();
	
	static {
		mapConstant.put("comp_id", "Comp_ID");
		mapConstant.put("comp_name", "Comp_Name");
		mapConstant.put("fin_date", "FIN_DATE");
		mapConstant.put("fin_entity", "FIN_ENTITY");
		mapConstant.put("fin_state_type", "FIN_STATE_TYPE");
		mapConstant.put("fin_period", "FIN_PERIOD");
	}
	
	
	//表中没有的列
	public static String[] NO_COLUMNS_SECU = {"fin_period", "fin_state_type", "fin_entity"};
	public static String[] NO_COLUMNS_BANK = {"fin_period", "fin_state_type", "fin_entity", "bank_ratio81", "bank_ratio146", "bank_ratio50", "bank_ratio56"};
	public static String[] NO_COLUMNS_INDU =  {"fin_period", "fin_state_type", "fin_entity"};
	public static String[] NO_COLUMNS_INSU =  {"fin_period", "fin_state_type", "fin_entity"};;
	
	Logger logger = LoggerFactory.getLogger(FinanceIndicatorService.class);
	
	/**
	 * 银行
	 */
	@Transactional
	public void saveBank() throws BusinessException{
		//indicatorDao.deleteAll(CommonConstant.SPECIAL_TABLE_BANK);
		List<Long> induBankIds = indicatorDao.findBankIssuerId();
		induBankIds.forEach(compId -> {
			save(compId, CommonConstant.ISSUER_TYPE_BANK, null);
		});
	}
	
	/**
	 * 非金融
	 */
	public String saveIndu() throws BusinessException{
		//indicatorDao.deleteAll(CommonConstant.SPECIAL_TABLE_INDU);
		List<Long> induIssuerIds = indicatorDao.findInduIssuerId();
		
		List<List<Long>> batchs = new ArrayList<>();
		if (!useMqDistWork) {
			ExecutorService service = Executors.newFixedThreadPool(10);
			List<List<Long>> subCompIds = subList(induIssuerIds, 10);
			subCompIds.forEach(item -> {
				SaveTask task = new SaveTask(item, CommonConstant.ISSUER_TYPE_INDU);
				service.submit(task);
			});
		}
		else {
			int i = 0; 
			List<Long> compIds = new ArrayList();
			for(Long compId: induIssuerIds) {
				compIds.add(compId);
				if (i++ % 100 == 0) {
//					amqpSender.distIndicatorCalculateWork(
//							new IndicatorCalculateTask(compIds, CommonConstant.ISSUER_TYPE_INDU));
					batchs.add(compIds);
					compIds = new ArrayList();
				}
			}
			batchs.add(compIds);
//			amqpSender.distIndicatorCalculateWork(
//					new IndicatorCalculateTask(compIds, CommonConstant.ISSUER_TYPE_INDU));
		}
		for (List<Long> list : batchs) {
			amqpSender.distIndicatorCalculateWork(
					new IndicatorCalculateTask(list, CommonConstant.ISSUER_TYPE_INDU));
		}
		
		return "共发送了" + ( batchs.size() ) + " 批次！";
	}
	
	/**
	 * 保险
	 */
	@Transactional
	public void saveInsu() throws BusinessException{
		//indicatorDao.deleteAll(CommonConstant.SPECIAL_TABLE_INSU);
		List<Long> insuSecuIds = indicatorDao.findInsuIssuerId();
		insuSecuIds.forEach(compId -> {
			save(compId, CommonConstant.ISSUER_TYPE_INSU, null);
		});
		
	}
	
	
	/**
	 * 证券
	 */
	@Transactional
	public void saveSecu() throws BusinessException{
		//indicatorDao.deleteAll(CommonConstant.SPECIAL_TABLE_SECU);
		List<Long> secuSecuIds = indicatorDao.findSecuIssuerId();
//		secuSecuIds.forEach(compId -> {
//		});
		for (Long compId : secuSecuIds) {
			save(compId, CommonConstant.ISSUER_TYPE_SECU,null);
			
		}
	}
	
	
	/**
	 * 构建单个主体专项指标数据
	 * @param compId
	 * @param issuerType
	 * @return
	 * @throws Exception 
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean save(Long compId, String issuerType, String finDate) throws BusinessException{
		log.info("###### 开始计算主体财务指标, compId:" + compId + ", issuerType:" + issuerType);
		if(StringUtils.isEmpty(issuerType)){
			Long induId = indicatorDao.findComInduId(compId);
			if(induId == null){
				return true;
			}
		    issuerType = indicatorDao.findIssuerModelFromMysql(induId);
		}
		boolean result = true;
		//当前主体的专项指标临时存储
		List<Map<String, Object>> listToSave = null; 
		//bulidDataToSave(dataSourceSecu, noColumns, null);
		switch (issuerType) {
    		case CommonConstant.ISSUER_TYPE_BANK:
    			listToSave = bulidDataToSave(dataSourceBank, NO_COLUMNS_BANK, compId, finDate);
    			indicatorDao.deleteByCompId(compId, CommonConstant.SPECIAL_TABLE_BANK, finDate);
    			indicatorDao.save(CommonConstant.SPECIAL_TABLE_BANK, listToSave);
    			break;
    		case CommonConstant.ISSUER_TYPE_INSU:
    			listToSave = bulidDataToSave(dataSourceInsu, NO_COLUMNS_INSU, compId, finDate);
    			indicatorDao.deleteByCompId(compId, CommonConstant.SPECIAL_TABLE_INSU, finDate);
    			indicatorDao.save(CommonConstant.SPECIAL_TABLE_INSU, listToSave);
    			break;
    		case CommonConstant.ISSUER_TYPE_INDU:
    			listToSave = bulidDataToSave(dataSourceIndu, NO_COLUMNS_INDU, compId ,finDate);
    			indicatorDao.deleteByCompId(compId, CommonConstant.SPECIAL_TABLE_INDU, finDate);
    			indicatorDao.save(CommonConstant.SPECIAL_TABLE_INDU, listToSave);
    			break;
    		case CommonConstant.ISSUER_TYPE_SECU:
    			listToSave = bulidDataToSave(dataSourceSecu, NO_COLUMNS_SECU, compId, finDate);
    			indicatorDao.deleteByCompId(compId, CommonConstant.SPECIAL_TABLE_SECU, finDate);
    			indicatorDao.save(CommonConstant.SPECIAL_TABLE_SECU, listToSave);
    			break;
    		default:
    			result = false;
    			break;
		}
		log.info("###### 结束计算主体财务指标, compId:" + compId + ", issuerType:" + issuerType);
		return result;
	}

	/**
	 * 获取财务指标
	 * @param issuerId 主体id
	 * @param finDate 财报时间
	 * @param fields 指标code
	 * @return
	 */
	
	public Map<String, Object> findFinanceIndicators (Long issuerId, Date finDate, String[] fields) throws BusinessException{
		Map<String, Object> financeIndicator = indicatorDao.findIndicatorByIssuerIdAndFinDate(issuerId, finDate);
		if(financeIndicator == null){
			return null;
		}else{
			Map<String, Object> result = new HashMap<>();
			for (String field : fields) {
				result.put(field, financeIndicator.get(field));
			}
			return result;
		}
	}
	
	
	/**
	 * 获取财务指标
	 * @param issuerId 主体id
	 * @param finDate 财报时间
	 * @return
	 */
	
	public Map<String, Object> findFinanceIndicators (Long issuerId, Date finDate) throws BusinessException{
		return indicatorDao.findIndicatorByIssuerIdAndFinDate(issuerId, finDate);
	}
	
	
	
	
	/**
	 * 初始化发行人类型（indu、insu、secu、bank）
	 * @return
	 * @throws SQLException 
	 */
	/*@Transactional
	public boolean initIssuerType() throws BusinessException, SQLException{
		
		log.info("initIssuerType start");
		return indicatorDao.initIssuerType();
	}*/
	
	
	
	
	/**
	 * 构建持久化数据
	 * @param noColumns
	 * @return
	 */
	private List<Map<String, Object>> bulidDataToSave(DataSource dataSource, String[] noColumns, Long compId, String finDate) {
		//原始data
		List<OriginalData> listData = dataSource.getData(compId, finDate);
		//指标对应的表达式
		Map<String, String> expressions = dataSource.getExpressions();
		//原始指标
		List<ExpressionVo> listExpression = dataSource.getExpressionsObj();
		List<Map<String,Object>> listToSave = new ArrayList<>();
		//多线程处理
		for (OriginalData od : listData) {
			for (Entry<String, Map<String, Object>> item : od.getIndicatorItems().entrySet()) {
				String date = item.getKey();
				Map<String,Object> map = buildDataToSave(noColumns, expressions, listExpression, od, date);
				if(map != null && map.size() >0 ){
					listToSave.add(map);
				}
				log.info("##计算财报 compId:"+ compId + ", finDate:" + date+ " 完成");
			}
		}
		return listToSave;
	}

	/**
	 * 构建保存数据
	 * @param noColumns
	 * @param expressions
	 * @param listExpression
	 * @param od
	 * @param finDate
	 * @return
	 */
	private Map<String, Object> buildDataToSave(String[] noColumns, Map<String, String> expressions,
			List<ExpressionVo> listExpression, OriginalData od, String finDate) {
		Map<String, Object> map = new HashMap<>();
		listExpression.forEach(item -> {
			
			String expression = item.getExpressionFormat();
			String field = item.getField();
			Map<String, Object> data = od.getIndicatorItems().get(finDate);
			//处理一些常量的属性
			if (mapConstant.containsKey(field.toLowerCase())) {
				map.put(field, data.get(field));
			//处理变量属性
			} else {
			    BigDecimal ex = null;
			    try {
			        ex = DateConvertUtil.formatterExpressionByTaobao(expression, od, expressions, finDate);
			    } catch(Exception e) {
			       // log.error(e.getMessage());
			    }
				map.put(field, ex == null ? null :ex.setScale(4, BigDecimal.ROUND_HALF_UP));
			}
		});
		//去除没有的列
		for(String column : noColumns){
			map.remove(column);
		}
		//获取主体名称
		//Map<Long, BondComExtVo> mapIssuer =  bondComExtDao.findAllAmaCache();
		
		Long compId = (Long) map.get("comp_id");
		BondComExtVo bondComExtVo = bondComExtDao.findByComId(compId);
		if(bondComExtVo == null){
			log.warn("安硕主体 comp_id[" + compId + "] 在dmdb.t_bond_com_ext 中不存在,主体名称会为空值！");
			map.put("comp_name", null);
		}else{
			map.put("comp_name", bondComExtVo.getAmaComName());
		}
		return map;
	}
	
	/**
	 * 数量太大考虑多线程
	 * @author 赵正来
	 *
	 */
	public class SaveTask implements Callable<Boolean> {

		private List<Long> compIds;
		
		private String issuerType;
		
		@Override
		public Boolean call() throws Exception {
			compIds.forEach(item -> {
				save(item, issuerType, null);
			});
			return true;
		}

		public SaveTask( List<Long> compIds, String issuerType) {
			super();
			this.compIds = compIds;
			this.issuerType = issuerType;
		}
		
	}
	
	public static void main(String[] args) {
		
		List<Long> ids = new ArrayList<>();
		for (long i = 1; i < 509; i++) {
			ids.add(i);
		}
		List<Long> sub = new ArrayList<>();
		int i = 0;
		for (Long id : ids) {
			sub.add(id);
			if(i++ % 100 == 0){
				sub = new ArrayList<>();
			}
		}
		
		
		System.out.println(4501/10);
	}
	
	/**
	 * 将一个大的集合拆分出多个小集合
	 * @param list 要拆分的集合
	 * @param count 拆分集合的数量
	 * @return
	 */
	private  <T> List<List<T>>  subList(List<T> list, int count){
		int subSize = list.size()/count;
		List<List<T>> listtemp = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			if(i == (count-1)){
				listtemp.add(list.subList(i*subSize, list.size()));
			}else{
				listtemp.add(list.subList(i*(subSize), i*subSize + subSize));
			}
		}
		return listtemp;
	}

	
}
