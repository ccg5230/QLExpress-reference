/**
 * BondCalculateErrorLogDao.java
 * com.innodealing.dao
 *
 * Function： TODO 
 *
 *   ver     date      		author
 * ──────────────────────────────────
 *   		 2017年6月21日 		chungaochen
 *
 * Copyright (c) 2017, DealingMatrix All Rights Reserved.
*/

package com.innodealing.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Component;

import com.innodealing.config.DatabaseNameConfig;
import com.innodealing.domain.BondCalculateErrorLog;

/**
 * ClassName:BondCalculateErrorLogDao
 * Function: 财务指标计算错误日志Dao
 * Reason:	 
 *
 * @author   chungaochen
 * @version  
 * @since    Ver 1.1
 * @Date	 2017年6月21日		下午2:02:43
 *
 * @see 	 
 */
@Component
public class BondCalculateErrorLogDao {
    public static final Logger log = LoggerFactory.getLogger(BondCalculateErrorLogDao.class);
    
    @Autowired private JdbcTemplate jdbcTemplate;
    
    @Autowired private DatabaseNameConfig databaseNameConfig;
    
    /**
     * 
     * InsertBondCalculateErrorLog:(插入指标计算错误日志)
     * @param  @param bean
     * @param  @throws Exception    设定文件
     * @return void    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public void InsertBondCalculateErrorLog(BondCalculateErrorLog bean) {
        StringBuilder sql = new StringBuilder(64);
        sql.append("insert into ").append(databaseNameConfig.getDmdb()).append(".t_bond_calculate_error_log ");
        sql.append("(");
        sql.append("ama_com_id,com_uni_code,table_name,fin_date,ratio,express_format,error_type,error_remark,create_time,last_update_time");
        sql.append(")");
        sql.append(" values(?,?,?,?,?,?,?,?,?,?)");//10columns
        try {
            jdbcTemplate.update(sql.toString(), new PreparedStatementSetter() { 
                @Override
                public void setValues(PreparedStatement ps) throws SQLException {
                    ps.setLong(1, bean.getAmaComId());    
                    ps.setLong(2, bean.getComUniCode());    
                    ps.setString(3, bean.getTableName());    
                    ps.setDate(4, bean.getFinDate());  
                    ps.setString(5, bean.getRatio()); 
                    ps.setString(6, bean.getExpressFormat());
                    ps.setString(7, bean.getErrorType());
                    ps.setString(8, bean.getErrorRemark());
                    ps.setTimestamp(9, bean.getCreateTime());                    
                    ps.setTimestamp(10, bean.getLastUpdateTime()); 
                }    
          });    
        } catch (Exception e) {
            log.error("update into  t_bond_calculate_error_log error!" + e.getMessage());
            throw e;
        }
        
    }
    
    /**
     * 
     * batchInsertBondCalculateErrorLog:(批量插入错误日志)
     * @param  @param beanList
     * @param  @throws Exception    设定文件
     * @return void    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public void batchInsertBondCalculateErrorLog(List<BondCalculateErrorLog> beanList) {
        StringBuilder sql = new StringBuilder(64);
        sql.append("insert into ").append(databaseNameConfig.getDmdb()).append(".t_bond_calculate_error_log ");
        sql.append("(");
        sql.append("ama_com_id,com_uni_code,table_name,fin_date,ratio,express_format,error_type,error_remark,create_time,last_update_time");
        sql.append(")");
        sql.append(" values(?,?,?,?,?,?,?,?,?,?)");//10columns
        try {
            jdbcTemplate.batchUpdate(sql.toString(), new BatchPreparedStatementSetter() {   
                
                @Override  
                public int getBatchSize() {   
                     return beanList.size();    
                }   
                @Override  
                public void setValues(PreparedStatement ps, int i)   
                        throws SQLException {   
                      ps.setLong(1, beanList.get(i).getAmaComId());    
                      ps.setLong(2, beanList.get(i).getComUniCode());    
                      ps.setString(3, beanList.get(i).getTableName());    
                      ps.setDate(4, beanList.get(i).getFinDate());  
                      ps.setString(5, beanList.get(i).getRatio()); 
                      ps.setString(6, beanList.get(i).getExpressFormat());
                      ps.setString(7, beanList.get(i).getErrorType());
                      ps.setString(8, beanList.get(i).getErrorRemark());
                      ps.setTimestamp(9, beanList.get(i).getCreateTime());                    
                      ps.setTimestamp(10, beanList.get(i).getLastUpdateTime());   
                }    
          });    
        } catch (Exception e) {
            log.error("batchUpdate into  t_bond_calculate_error_log error!" + e.getMessage());
            throw e;
        }
    }
}
