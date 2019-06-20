/**
 * BondIndustryClassificationDao.java
 * com.innodealing.engine.jdbc.bond
 *
 * Function： TODO 
 *
 *   ver     date      		author
 * ──────────────────────────────────
 *   		 2017年5月25日 		chungaochen
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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.innodealing.config.DatabaseNameConfig;
import com.innodealing.domain.BondFinanceSheetIndicatorExpression;

/**
 * ClassName:BondIndustryClassificationDao
 * Function: GICS行业分类模型及得分Dao
 * Reason:	财报管理相关表数据插入更新操作
 *
 * @author   chungaochen
 * @version  
 * @since    Ver 1.1
 * @Date	 2017年5月25日		下午12:02:11
 *
 * @see 	 
 */
@Component
public class BondIndustryClassificationDao {
    @Autowired private JdbcTemplate jdbcTemplate;
    
    @Autowired private DatabaseNameConfig databaseNameConfig;
    
    public static final Logger log = LoggerFactory.getLogger(BondIndustryClassificationDao.class);
    
    
    /**
     * 删除所有
     * @author chungaochen
     * @param tableName
     * @return
     */
    public int truncateTable(String tableName){
        String sql =  "TRUNCATE TABLE " + tableName ;
        //TRUNCATE后AUTO_INCREMENT起始值
        return jdbcTemplate.update(sql);
    }
    
    /**
     * 
     * findAllAmaCompidClassification:(获取所有财报主体行业分类及主体名称)
     * @param  @return    设定文件
     * @return Map<String,Map<String,Object>>    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public Map<Long, Map<String, Object>> findAllAmaCompidClassification(){
        StringBuilder sql  = new StringBuilder(64);
        sql.append("SELECT ext.ama_com_id, c.industry_code, c.model_id,c.INDU_SCORE,ext.indu_uni_code,ext.com_uni_code,ext.com_chi_name "); 
        sql.append(" FROM (SELECT ama_com_id,indu_uni_code_l4 as indu_uni_code,com_chi_name,com_uni_code FROM dmdb.t_bond_com_ext GROUP BY ama_com_id) ext ");
        sql.append(" LEFT JOIN dmdb.t_bond_industry_classification c ON c.industry_code = ext.indu_uni_code AND c.VISIBLE='1' ");
        List<Map<String, Object>> list = null;
        Map<Long, Map<String, Object>> map = new HashMap<>();
        //按主体id和财报日期查找
        try {
            list = jdbcTemplate.queryForList(sql.toString());
            for(Map<String, Object> classMap : list) {
                if(null != classMap.get("ama_com_id")) {
                    Long amaComId= Long.parseLong(classMap.get("ama_com_id").toString());
                    map.put(amaComId, classMap);
                }
            }
        } catch (Exception e) {
            log.info("findAllAmaCompidClassification error for query[" + "sql=" + sql +  "] : " + e.getMessage());
        }
        return map;
    }
    
    /**
     * 
     * getFinSheetExpressionMap:(获取模型id所有指标公式Map)
     * @param  @param expressionModelId
     * @param  @return    设定文件
     * @return Map<String,String>    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public Map<String, String> getFinSheetExpressionMap(String expressionModelId) {
        List<BondFinanceSheetIndicatorExpression> expressionList =  findListExpressionByModelId(expressionModelId);
        Map<String,String> expressions = new HashMap<>();
        expressionList.forEach(item -> {
            expressions.put(item.getField(), item.getExpressFormat());
        });
        return expressions;
    }
    
    /**
     * 
     * findListExpressionByModelId:(查询财报模型所有指标计算式)
     * @param  @param modelId 模型ID
     * @param  @return    设定文件
     * @return List<BondFinanceSheetIndicatorExpression>    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public List<BondFinanceSheetIndicatorExpression> findListExpressionByModelId(String modelId) {
        StringBuilder sql  = new StringBuilder(64);
        sql.append("SELECT id,field,field_name fieldName,type,model_id modelId,expression,");
        sql.append(" express_description expressDescription,remark,express_format expressFormat ");
        sql.append(" FROM " +databaseNameConfig.getDmdb() +". t_bond_finance_sheet_indicator_expression");             
        sql.append(" WHERE model_id ='").append(modelId).append("' ");                      
        
        List<BondFinanceSheetIndicatorExpression> expressionList = new ArrayList<>();
        try {
            expressionList = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(BondFinanceSheetIndicatorExpression.class));
        } catch (EmptyResultDataAccessException e) {
            log.info("findListExpressionByModelId error: no result for query[" + "modelId=" + modelId +  "]");
        }
        return expressionList;
    }
    
    /**
     * 
     * getAllFinanceModelExpression:(获取所有财报模型指标计算公式)
     * @param  @return    设定文件
     * @return Map<String,List<BondFinanceSheetIndicatorExpression>>    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public Map<String,List<BondFinanceSheetIndicatorExpression>> getAllFinanceModelExpression() {
        StringBuilder sql  = new StringBuilder(64);
        Map<String,List<BondFinanceSheetIndicatorExpression>> map = new HashMap<>();
        sql.append("SELECT id,field,field_name fieldName,type,model_id modelId,expression,");
        sql.append(" express_description expressDescription,remark,express_format expressFormat ");
        sql.append(" FROM " +databaseNameConfig.getDmdb() +". t_bond_finance_sheet_indicator_expression");             
        
        List<BondFinanceSheetIndicatorExpression> expressionList = null;
        try {
            expressionList = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(BondFinanceSheetIndicatorExpression.class));
            if(null != expressionList && expressionList.size()>0){
                List<BondFinanceSheetIndicatorExpression> bankList = new ArrayList<>();
                List<BondFinanceSheetIndicatorExpression> induList = new ArrayList<>();
                List<BondFinanceSheetIndicatorExpression> insuList = new ArrayList<>();
                List<BondFinanceSheetIndicatorExpression> secuList = new ArrayList<>();
                List<BondFinanceSheetIndicatorExpression> businList = new ArrayList<>();
                List<BondFinanceSheetIndicatorExpression> estateList = new ArrayList<>();
                List<BondFinanceSheetIndicatorExpression> nfinList = new ArrayList<>();
                for(BondFinanceSheetIndicatorExpression exp : expressionList) {
                  //bank-银行 indu-工业 insu-保险 secu-证券 busin-商业 estate-房地产 nfin-非经融
                    if("bank".equalsIgnoreCase(exp.getModelId())) {
                        bankList.add(exp);
                    } else if("indu".equalsIgnoreCase(exp.getModelId())) {
                        induList.add(exp);
                    } else if("insu".equalsIgnoreCase(exp.getModelId())) {
                        insuList.add(exp);
                    } else if("secu".equalsIgnoreCase(exp.getModelId())) {
                        secuList.add(exp);
                    } else if("busin".equalsIgnoreCase(exp.getModelId())) {
                        businList.add(exp);
                    } else if("estate".equalsIgnoreCase(exp.getModelId())) {
                        estateList.add(exp);
                    } else if("nfin".equalsIgnoreCase(exp.getModelId())) {
                        nfinList.add(exp);
                    }      
                }
                map.put("bank", bankList);
                map.put("indu", induList);
                map.put("insu", insuList);
                map.put("secu", secuList);
                map.put("busin", businList);
                map.put("estate", estateList);
                map.put("nfin", nfinList);
            }
        } catch (Exception e) {
            log.info("findListExpressionByModelId error for query[" + "sql=" + sql +  "] : " + e.getMessage());
        }
        return map;
    }

    /**
     * 
     * getCompanyNature:(获取公司性质)
     * @param  @param issuerId
     * @param  @return    设定文件
     * @return Map<String,Object>    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public Map<String, Object> getCompanyNature(Long issuerId) {
        String sql ="select i.COM_ATTR_PAR ,p.PAR_NAME FROM "+ databaseNameConfig.getBondCcxe() +".d_pub_com_info_2 i"+
                " LEFT JOIN "+ databaseNameConfig.getBondCcxe() +".pub_par p ON p.PAR_SYS_CODE='1062' AND i.COM_ATTR_PAR = p.PAR_CODE"+
                " WHERE COM_UNI_CODE=? LIMIT 1 ";
        try {
            return jdbcTemplate.queryForMap(sql.toString(),issuerId);
        } catch (DataAccessException e) {
            log.warn(sql +" error![" + issuerId +  "]" + e.getMessage());
            return null;
        }
    }
    

}

