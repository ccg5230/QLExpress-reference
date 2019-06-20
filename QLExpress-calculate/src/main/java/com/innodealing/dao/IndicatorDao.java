package com.innodealing.dao;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.innodealing.config.DatabaseNameConfig;
import com.innodealing.constant.CommonConstant;
import com.innodealing.constant.TableNameConstant;
import com.innodealing.exception.BusinessException;

/**
 * <p>财务指标dao
 * @author 赵正来
 *
 */

@Component
public class IndicatorDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired private DatabaseNameConfig databaseNameConfig;
	
	
	public static final Logger log = LoggerFactory.getLogger(IndicatorDao.class);

	/**
	 * 保存
	 * @param tableName
	 * @param data
	 */
	public void save(String tableName, List<Map<String, Object>> data) {
	    if(null==data || data.size()==0) {
	        return;
	    }
		setSystemInfomation(data);
		
		log.debug("save table:" + tableName + " begin");
		String sql = "insert into " + databaseNameConfig.getDmdb() +  "." +tableName + "(";
		StringBuffer column = new StringBuffer();
		StringBuffer set = new StringBuffer(" values (");

		// sql语句
		Set<String> columns = data.get(0).keySet();
		columns.forEach(item -> {
			column.append(item).append(",");
			set.append("?").append(",");
		});
		sql = sql + column.toString().substring(0, column.length() - 1) + ")"
				+ set.toString().substring(0, set.length() - 1) + ")";

		// 参数
		List<Object[]> batchArgs = new ArrayList<>();

		data.forEach(item -> {
			Object[] args = new Object[item.size()];
			int index = 0;
			for (Entry<String, Object> entry : item.entrySet()) {
				args[index] = entry.getValue();
				index++;
			}
			batchArgs.add(args);
		});
		data.clear();
		jdbcTemplate.batchUpdate(sql, batchArgs);
		log.debug("save table:" + tableName + "done, sql:" + sql);
	}
	
	private void setSystemInfomation(List<Map<String, Object>> data) {
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		for(Map<String, Object> fields : data) {
			fields.put("last_update_time", sdfDate.format(new Date()));
			fields.put("insert_user", 7);
		}
	}

	/**
	 * 按发行人的id和财报日期查找财报记录
	 * @param issuerId
	 * @param finDate
	 * @return
	 */
	public Map<String,Object> findIndicatorByIssuerIdAndFinDate(Long issuerId, Date finDate){
		//主体的类型
		String tableName =findFinaTable(issuerId);
		if(tableName == null){
			return null;
		}
		String table = databaseNameConfig.getDmdb() + "." +  tableName;
		String sql = "select * from " + table  + " where comp_id = (select ama_com_id from " + databaseNameConfig.getDmdb() + ".t_bond_com_ext where com_uni_code = ?) and fin_date = ?";
		try {
			return jdbcTemplate.queryForMap(sql, issuerId, finDate);
		} catch (DataAccessException e) {
			log.warn(sql +" error![" + issuerId +  "]" + e.getMessage());
			return null;
		}
	}

	/**
	 * 查询主体类型
	 * @param issuerId
	 * @return
	 */
	public String findIssuerType(Long issuerId){
		try {
			String sql  = "SELECT c.model_id FROM " + databaseNameConfig.getDmdb() + ".t_bond_com_ext e INNER JOIN " + databaseNameConfig.getDmdb() + ".t_bond_industry_classification c ON e.indu_uni_code_l4 = c.industry_code where e.com_uni_code = ?";
			String model = jdbcTemplate.queryForObject(sql, String.class, issuerId);
			if(model == null){
				log.error("no result for issuer[" + issuerId + "]找不到对应的模型" + sql);
			}
			switch (model) {
			case CommonConstant.ISSUER_TYPE_BANK:
				return CommonConstant.ISSUER_TYPE_BANK;
			case CommonConstant.ISSUER_TYPE_INSU:
				return CommonConstant.ISSUER_TYPE_INSU;
			case CommonConstant.ISSUER_TYPE_SECU:
				return CommonConstant.ISSUER_TYPE_SECU;
			default:
				return CommonConstant.ISSUER_TYPE_INDU;
			}
		} catch (EmptyResultDataAccessException e) {
			log.error("no result for issuer[" + issuerId + "]!" + e.getMessage());
			return null;
		}
	}
	
	/**
	 * 查询主体类型财务表名
	 * @param issuerId
	 * @return
	 */
	public String findFinaTable(Long issuerId){
		String model = findIssuerType(issuerId);
		switch (model) {
		case CommonConstant.ISSUER_TYPE_BANK:
			return TableNameConstant.FIN_SHEET_BANK;
		case CommonConstant.ISSUER_TYPE_INSU:
			return TableNameConstant.FIN_SHEET_INSU;
		case CommonConstant.ISSUER_TYPE_SECU:
			return TableNameConstant.FIN_SHEET_SECU;
		default:
			return TableNameConstant.FIN_SHEET_INDU;
		}
	}
	
	/**
	 * 按发行人的id查找最新财报记录
	 * @param issuerId
	 * @return
	 */
	public Map<String,Object> findNewestIndicatorByIssuerId(Long issuerId){
		//主体的类型
		String tableName = findFinaTable(issuerId);
		if(tableName == null){
			return null;
		}
		String table = databaseNameConfig.getDmdb() + "." + tableName;
		String sql = "select * from " + table  + " where comp_id = (select ama_com_id from " + databaseNameConfig.getDmdb() + ".t_bond_com_ext where com_uni_code = ?)  order by fin_date desc limit 1";
		try {
			return jdbcTemplate.queryForMap(sql, issuerId);
		} catch (EmptyResultDataAccessException e) {
			log.warn(sql +" error![" + issuerId +  "]" + e.getMessage());
			return null;
		}
	}
	
	/**
	 * 删除所有
	 * @param tableName
	 * @return
	 */
	public int deleteAll(String tableName){
		String setIncrement =  "alter table " + tableName + " AUTO_INCREMENT=1";
		//删除
		int count =jdbcTemplate.update("delete from " + databaseNameConfig.getDmdb() + "." + tableName);
		
		int count2 =jdbcTemplate.update("delete from " + databaseNameConfig.getDmdb() + "." + tableName + "_annual");
		//设置AUTO_INCREMENT起始值
		jdbcTemplate.update(setIncrement);
		return count + count2;
	}
	
	/**
	 * 删除单个主体信息
	 * @param compId
	 * @param tableName
	 * @param finDate
	 * @return
	 */
	public int deleteByCompId(Long compId, String tableName, String finDate){
		//删除
		String sql = "delete from " + databaseNameConfig.getDmdb() + "." + tableName + " where comp_id = " + compId;
		if(finDate != null){
			sql = sql + " and fin_date = '" + finDate + "'";
		}
		int count =jdbcTemplate.update(sql);
		return count;
	}
	
	
	/**
	 * indu所有财报主体
	 * @return
	 */
	public List<Long> findInduIssuerId(){
		//所有财报主体
		String issuerSql = "SELECT e.ama_com_id FROM dmdb.t_bond_com_ext e INNER JOIN  " + databaseNameConfig.getDmdb() + ".t_bond_industry_classification c on e.indu_uni_code_l4 = c.industry_code WHERE model_id NOT IN('bank','insu','secu') ";
		return jdbcTemplate.queryForList(issuerSql, Long.class);
	}
	
	/**
	 * insu所有财报主体
	 * @return
	 */
	public List<Long> findInsuIssuerId(){
		//所有财报主体
		String issuerSql = "SELECT e.ama_com_id FROM dmdb.t_bond_com_ext e INNER JOIN  " + databaseNameConfig.getDmdb() + ".t_bond_industry_classification c on e.indu_uni_code_l4 = c.industry_code WHERE model_id = 'insu'";
		
		return jdbcTemplate.queryForList(issuerSql, Long.class);
	}
	
	/**
	 * indu所有财报主体
	 * @return
	 */
	public List<Long> findBankIssuerId(){
		//所有财报主体
		String issuerSql = "SELECT e.ama_com_id FROM dmdb.t_bond_com_ext e INNER JOIN  " + databaseNameConfig.getDmdb() + ".t_bond_industry_classification c on e.indu_uni_code_l4 = c.industry_code WHERE model_id = 'bank'";
		return jdbcTemplate.queryForList(issuerSql, Long.class);
	}
	
	/**
	 * secu所有财报主体
	 * @return
	 */
	public List<Long> findSecuIssuerId(){
		//所有财报主体
		String issuerSql = "SELECT e.ama_com_id FROM dmdb.t_bond_com_ext e INNER JOIN  " + databaseNameConfig.getDmdb() + ".t_bond_industry_classification c on e.indu_uni_code_l4 = c.industry_code WHERE model_id = 'secu'";
		return jdbcTemplate.queryForList(issuerSql, Long.class);
	}
	
	
	/**
	 * 查询主体的行业模型
	 * @param induId
	 * @return indu,bank,insu,secu
	 */
	public String findIssuerModelFromMysql(Long induId) throws BusinessException{
		String sql = "SELECT model_id FROM  dmdb.t_bond_industry_classification WHERE industry_code = " + induId;
		String model = null;
		
		try {
			model = jdbcTemplate.queryForObject(sql, String.class);
		} catch (EmptyResultDataAccessException e) {
			e.printStackTrace();
		}
		if(model == null){
			log.error(sql);
			throw new BusinessException("行业 [dmdb.t_bond_industry_classification ，industry_code=" + induId + "] 找不到对应的模型。");
		}
		if(!CommonConstant.ISSUER_TYPE_BANK.equals(model) && !CommonConstant.ISSUER_TYPE_INSU.equals(model) && !CommonConstant.ISSUER_TYPE_SECU.equals(model)){
			model = CommonConstant.ISSUER_TYPE_INDU;
		}
		return model;
	}
	
	/**
	 * 查找主体分类
	 * @param compId
	 * @return
	 */
	public Long findComInduId(Long compId){
		String sql = "SELECT indu_uni_code FROM dmdb.t_bond_com_ext WHERE ama_com_id = " + compId;
		try {
			return jdbcTemplate.queryForObject(sql, Long.class);
		} catch (EmptyResultDataAccessException e) {
			log.error("主体compId["+ compId +"]找不到对应的行业模型，sql-》" + sql );
			return null;
		}
	}
	
	public static void main(String[] args) throws Exception {
		// List<Map<String,Object>> list = new ArrayList<>();
		// for (int i = 0; i < 10; i++) {
		// Map<String,Object> map = new HashMap<>();
		// map.put("comp_id", i + "");
		// map.put("mame", "name" + i);
		// map.put("date", "data" + i);
		// list.add(map);
		// }
		//
		// new IndicatorDao().save("bank", list);

//		ExpressRunner runner = new ExpressRunner();
//		DefaultContext<String, Object> context = new DefaultContext<String, Object>();
//		context.put("a", 1);
//		context.put("b", 2);
//		context.put("c", 3);
//		String express = "a+b*c";
//		Object r = runner.execute(express, context, null, true, false);
//		System.out.println(r);
//		
		
		new Runnable() {
			public void run() {
				System.out.println(233);
			}
		};
	}

}
