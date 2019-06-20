package com.innodealing.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.innodealing.domain.FinanceSheetFieldMap;

/**
 * ClassName:FinanceSheetFieldMapDao
 * Function: 中诚信安硕字段映射表
 * Reason:	 
 *
 * @author   yig
 * @version  
 * @since    Ver 1.1
 * @Date	 2017年6月21日		下午2:02:43
 *
 * @see 	 
 */
@Component
public class FinanceSheetFieldMapDao {
    public static final Logger log = LoggerFactory.getLogger(FinanceSheetFieldMapDao.class);
    
    @Autowired private JdbcTemplate jdbcTemplate;
  
    public Map<String, String> findFieldMap(String tableName) {
    	Map<String, String> fieldMap = new HashMap<String, String>();
    	
    	final String querySqlFormat = "select T.ccxe_field_name, T.field_name from "
    			+ "dmdb.t_bond_finance_ccxe_field_map T where T.table_name = '%1$s' and T.ccxe_field_name is not null";
    	String querySql = String.format(querySqlFormat, tableName);
    	List<FinanceSheetFieldMap> result = jdbcTemplate.query(querySql, new BeanPropertyRowMapper<>(FinanceSheetFieldMap.class));
    	if (result != null) { 
    		for (FinanceSheetFieldMap p : result) {
    			fieldMap.put(p.getCcxeFieldName(), p.getFieldName());
    		}
    	}
		return fieldMap;
    }
  
}
