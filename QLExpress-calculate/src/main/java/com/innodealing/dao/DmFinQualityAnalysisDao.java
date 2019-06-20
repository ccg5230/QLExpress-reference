package com.innodealing.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.innodealing.config.DatabaseNameConfig;

/**
 * 财务质量dao
 * @author 赵正来
 *
 */

@Component
public class DmFinQualityAnalysisDao {
	
	Logger log = LoggerFactory.getLogger(DmFinQualityAnalysisDao.class);
	
	private @Autowired JdbcTemplate jdbcTemplate;
	
	private @Autowired DatabaseNameConfig databaseNameConfig;

	public  boolean insertorUpdate(String compId, String year, Float score) throws Exception{
		
		String quarySql = "select count(1) from " + databaseNameConfig.getDmdb() + ".dm_fin_quality_analysis where custid = ? and year = ?";
		Object[] args = {compId, year};
		int count = jdbcTemplate.queryForObject(quarySql, args,  Integer.class);
		//没有数据则插入有的话更新
		int result;
		if(count == 0){
			String insertSql = "insert into " + databaseNameConfig.getDmdb() + ".dm_fin_quality_analysis (custid, year, quan_score,last_update_time) values(?, ?, ?,now())";
			result = jdbcTemplate.update(insertSql, compId, year, score);
		}else{
			String insertSql = "update " + databaseNameConfig.getDmdb() + ".dm_fin_quality_analysis set custid = ?, year = ?, quan_score = ?";
			result = jdbcTemplate.update(insertSql, compId, year, score);
		}
		return result != 0;
	}
	
	public  boolean batchInsertorUpdate(List<Object[]> args)  throws Exception{
		if(args == null || args.size() == 0){
			log.info("args 为空！ 跳过 batchInsertorUpdate");
			return true;
		}
		String insertSql ="insert into " + databaseNameConfig.getDmdb() + ".dm_fin_quality_analysis (custid, year, quan_score,last_update_time) values(?, ?, ?, now())";
		deleteByCompId(args.get(0)[0].toString());
		int[] result = jdbcTemplate.batchUpdate(insertSql, args);
		return result.length != 0;
	}
	
	public boolean deleteByCompId(String compId)  throws Exception{
		String sql = "delete from "+ databaseNameConfig.getDmdb() + ".dm_fin_quality_analysis where custid = ?";
		jdbcTemplate.update(sql, compId);
		return true;
	}
	
}
