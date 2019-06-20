package com.innodealing.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.innodealing.config.DatabaseNameConfig;
import com.innodealing.exception.BusinessException;

/**
 * DmBond Dao
 * @author 赵正来
 *
 */

@Component
public class DmBondDao {

	private @Autowired JdbcTemplate jdbcTemplate;
	
	private @Autowired DatabaseNameConfig databaseNameConfig;
	
	public  boolean insertorUpdate(Long compId, String compName, String induId, String nduName4, String year, Byte month, String rating ) throws Exception{
		
		String quarySql = "select count(1) from " + databaseNameConfig.getDmdb() + ".dm_bond where comp_id = ? and year = ? and quan_month = ?";
		Object[] args = {compId, year, month};
		int count = jdbcTemplate.queryForObject(quarySql, args,  Integer.class);
		//没有数据则插入有的话更新
		int result;
		if(count == 0){
			String insertSql = "insert into " + databaseNameConfig.getDmdb() + ".dm_bond (comp_id, comp_name, industry_id, induname4, year, quan_month, rating,last_update_timestamp) values(?,?,?,?,?,?,?,now())";
			result = jdbcTemplate.update(insertSql,compId, compName, induId, nduName4, year, month,rating);
		}else{
			String insertSql = "update " + databaseNameConfig.getDmdb() + ".dm_bond set comp_id = ?, comp_name = ?, industry_id = ?, induname4 = ?, year = ?, quan_month = ?, rating = ?  where comp_id = ? and year = ? and quan_month = ?";
			result = jdbcTemplate.update(insertSql,compId, compName, induId, nduName4, year, month,rating,compId, year, month);
		}
		return result != 0;
	}
	
	
	public  boolean batchInsertorUpdate(List<Object[]> batchArgs) throws Exception{
		if(batchArgs == null || batchArgs.size() == 0){
			throw new BusinessException("args 不能为空！");
		}
		//先删除老数据
		Long compId = Long.valueOf(batchArgs.get(0)[0].toString());
		deleteByCompId(compId);
		String insertSql = "insert into " + databaseNameConfig.getDmdb() + ".dm_bond (comp_id, comp_name, industry_id, induname4, year, quan_month, rating,last_update_timestamp) values(?,?,?,?,?,?,?,now())";
		int[] result = jdbcTemplate.batchUpdate(insertSql,batchArgs);
		return result.length != 0;
	}
	
	public boolean deleteByCompId(Long compId){
		String delSql = "delete from  " + databaseNameConfig.getDmdb() + ".dm_bond where  comp_id = ?"; 
		jdbcTemplate.update(delSql, compId);
		return true;
	}

}
