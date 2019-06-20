/**
 * BondFinanceSheetDao.java
 * com.innodealing.dao
 *
 * Function： TODO 
 *
 *   ver     date      		author
 * ──────────────────────────────────
 *   		 2017年6月23日 		chungaochen
 *
 * Copyright (c) 2017, DealingMatrix All Rights Reserved.
*/

package com.innodealing.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * ClassName:BondFinanceSheetDao
 * Function: 财报Dao
 * Reason:	 
 *
 * @author   chungaochen
 * @version  
 * @since    Ver 1.1
 * @Date	 2017年6月23日		下午3:36:04
 *
 * @see 	 
 */
@Component
public class BondFinanceSheetDao {
    
    @Autowired private JdbcTemplate jdbcTemplate;
    
    public static final Logger log = LoggerFactory.getLogger(BondFinanceSheetDao.class);
    
    /**
     * 
     * queryCount:(查询统计)
     * @param  @param tableName
     * @param  @return    设定文件
     * @return int    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public int queryCount(String tableName) {
        StringBuilder sql = new StringBuilder(64);
        sql.append("SELECT COUNT(1) FROM " ).append(tableName);
        int count = jdbcTemplate.queryForObject(sql.toString(), Integer.class);
        return count;
    }
    
    /**
     * 
     * queryListByLimit:(分页查询财报数据)
     * @param  @param tableName
     * @param  @param offset
     * @param  @param rows
     * @param  @return    设定文件
     * @return List<Map<String,Object>>    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public List<Map<String,Object>> queryFinaSheetByLimit(String tableName, int offset, int rows) {
        StringBuilder sql = new StringBuilder(64);
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            sql.append("SELECT COMP_ID,FIN_DATE,FIN_PERIOD FROM " ).append(tableName).
                append(" ORDER BY COMP_ID,FIN_DATE,FIN_PERIOD LIMIT ").append(offset).append(",").append(rows);
            list = jdbcTemplate.queryForList(sql.toString());
        } catch(DataAccessException e) {
            log.info("queryFinaSheetByLimit error: no result for query[" + "sql=" + sql +  "]");
        }
        return list;
    }
    
    /**
     * 
     * queryFinaSheetByComId:(查询财报主体所有财报数据)
     * @param  @param tableName
     * @param  @param amaComId
     * @param  @return    设定文件
     * @return List<Map<String,Object>>    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public List<Map<String,Object>> queryFinaSheetByComId(String tableName, Long amaComId) {
        StringBuilder sql = new StringBuilder(64);
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            sql.append("SELECT * FROM " ).append(tableName).
                append(" WHERE COMP_ID=?  AND VISIBLE='1' ");
            list = jdbcTemplate.queryForList(sql.toString(),amaComId);
        } catch(DataAccessException e) {
            log.info("queryFinaSheetByLimit error: no result for query[" + "sql=" + sql +  "]");
        }
        return list;
    }
    
    /**
     * 
     * findSheetByIssuerIdAndFinDate:(根据日期查询对应公司报表数据)
     * @param  @param table
     * @param  @param amaComId 公司id(安硕）
     * @param  @param finDate
     * @param  @return    设定文件
     * @return Map<String,Object>    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public Map<String, Object> findSheetByIssuerIdAndFinDate(String table, Long amaComId, Date finDate){
        StringBuilder sql  = new StringBuilder(64);
        sql.append("select * from " + table);
        sql.append(" where comp_id =? and fin_date=? AND VISIBLE='1' LIMIT 1 ");
        try {
            return jdbcTemplate.queryForMap(sql.toString(),amaComId,finDate);
        } catch (DataAccessException e) {
            log.warn(sql +" error![" + amaComId +  "]" + e.getMessage());
            return null;
        }
    }
    
    /**
     * 
     * findSheetByIssuerIdAndFinDateAndfinPeriod:(根据日期查询同周期对应公司报表数据)
     * @param  @param table:带数据库的表名
     * @param  @param amaComId 公司id(安硕）
     * @param  @param finDate  财报日期
     * @param  @param finPeriod 财报涵盖日期区间
     * @param  @return    设定文件
     * @return Map<String,Object>    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public Map<String, Object> findSheetByIssuerIdAndFinDateAndfinPeriod(String table, Long amaComId, Date finDate,String finPeriod){
        StringBuilder sql  = new StringBuilder(64);
        sql.append("select * from " + table);
        sql.append(" where comp_id =? and fin_date=? and FIN_PERIOD =? AND VISIBLE='1' LIMIT 1 ");
        try {
            return jdbcTemplate.queryForMap(sql.toString(),amaComId,finDate,finPeriod);
        } catch (DataAccessException e) {
            log.warn(sql +" error![" + amaComId +  "]" + e.getMessage());
            return null;
        }
    }
    
    /**
     * 
     * findSheetByIssuerId:(单个amaComId主体同期财报数据)
     * @param  @param table
     * @param  @param amaComId
     * @param  @param finPeriod
     * @param  @return    设定文件
     * @return Map<String,Object>    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public List<Map<String, Object>> findSheetByIssuerIdAndfinPeriod(String table, Long amaComId,String finPeriod){
        StringBuilder sql  = new StringBuilder(64);
        sql.append("select * from " + table);
        sql.append(" where comp_id =? and FIN_PERIOD =? AND VISIBLE='1' ");
        try {
            return jdbcTemplate.queryForList(sql.toString(),amaComId,finPeriod);
        } catch (DataAccessException e) {
            log.warn(sql +" error![" + amaComId +  "]" + e.getMessage());
            return null;
        }
    }
    
    /**
     * 
     * queryComFinaSheetGroups:(查询财报主体财报期限分组)
     * @param  @param table
     * @param  @param amaComId
     * @param  @return    设定文件
     * @return Map<String,List<Map<String,Object>>>    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public Map<String, List<Map<String, Object>>> queryComFinaSheetGroups(String table, Long amaComId){
        Map<String,List<Map<String, Object>>> comFinaSheetGroups = null;
        try {
            List<Map<String, Object>> list = queryFinaSheetByComId(table,amaComId);
            if(null != list && list.size()>0){
                comFinaSheetGroups = new HashMap<>();
                List<Map<String, Object>> threeList = new ArrayList<>();
                List<Map<String, Object>> sexList = new ArrayList<>();
                List<Map<String, Object>> nineList = new ArrayList<>();
                List<Map<String, Object>> twelveList = new ArrayList<>();
                for(Map<String, Object> map : list){
                    String finPeriod = map.get("FIN_PERIOD").toString();
                    if("3".equals(finPeriod)) {
                        threeList.add(map);
                    } else if("6".equals(finPeriod)) {
                        sexList.add(map);
                    } else if("9".equals(finPeriod)) {
                        nineList.add(map);
                    } else if("12".equals(finPeriod)) {
                        twelveList.add(map);
                    }
                }
                comFinaSheetGroups.put(amaComId+"_"+"3", threeList);
                comFinaSheetGroups.put(amaComId+"_"+"6", sexList);
                comFinaSheetGroups.put(amaComId+"_"+"9", nineList);
                comFinaSheetGroups.put(amaComId+"_"+"12", twelveList);
            }
        } catch (Exception e) {
            log.warn( "queryComFinaSheetGroups error![" + amaComId +  "]" + e.getMessage());
            comFinaSheetGroups = null;
        }
        return comFinaSheetGroups;
    }

}

