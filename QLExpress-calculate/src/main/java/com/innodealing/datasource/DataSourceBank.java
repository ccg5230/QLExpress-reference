package com.innodealing.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.innodealing.config.DatabaseNameConfig;
import com.innodealing.constant.TableNameConstant;
import com.innodealing.engine.OriginalData;
import com.innodealing.vo.ExpressionVo;

@Component
public class DataSourceBank extends DataSource {

	@Autowired DatabaseNameConfig  databaseNameConfig;
	
	@Autowired private JdbcTemplate jdbcTemplate;
	
	@Autowired private JdbcTemplate asbrsJdbcTemplate;
	
	@Override
	public Map<String, String> getExpressions() {
		//获取所有表达式
		String sqlQueryEcpression = "select table_name as tableName,field,field_name as fieldName,remark,expression,express_format as expressionFormat from " + databaseNameConfig.getDmdb() + ".t_bond_indicator_expression where table_name = 'FIN_RATIO_BANK'";
		return loadExpressions(sqlQueryEcpression, jdbcTemplate);
	}

	

	@Override
	public List<OriginalData> getData(Long compId, String finDate) {
		String sqlQuery = "select s.Comp_ID as comp_id,s.*  from " + databaseNameConfig.getDmdb()+ "." + TableNameConstant.FIN_SHEET_BANK + " s ";
		if(compId != null){
			sqlQuery += " where comp_id = " + compId;
		}
		if(finDate != null){
			sqlQuery += " and fin_date = '" + finDate + "'";
		}
		//获取原始数据
		return loadData(sqlQuery, asbrsJdbcTemplate);
	}

	@Override
	public List<ExpressionVo> getExpressionsObj() {
		//获取所有表达式
		String sqlQueryEcpression = "select table_name as tableName,field,field_name as fieldName,remark,expression,express_format as expressionFormat from " + databaseNameConfig.getDmdb() + ".t_bond_indicator_expression where table_name = 'FIN_RATIO_BANK'";

		return  jdbcTemplate.query(sqlQueryEcpression, new BeanPropertyRowMapper<>(ExpressionVo.class));
	}
	
	public static void main(String[] args) {
		List<Map<String,Object>> list = new  ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Map<String,Object> map = new HashMap<>();
			map.put("comp_id", i + "");
			map.put("mame", "name" + i);
			map.put("date", "data" + i);
			list.add(map);
		}
		Map<Object, List<Map<String, Object>>> issData = list.stream().collect(Collectors.groupingBy(m -> m.get("comp_id"),Collectors.toList()));
		System.out.println(issData.size());
	}



	
}
