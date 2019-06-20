package com.innodealing.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import com.innodealing.engine.OriginalData;
import com.innodealing.vo.ExpressionVo;

/**
 * 数据源
 * @author 赵正来
 *
 */
public abstract class DataSource {

	/**
	 * 表达式
	 */
	//private Map<String,String> expressions;
	
	/**
	 * 数据
	 */
	//private List<OriginalData> data;
	
	
	
	
	/**
	 * 获取表达式
	 * @return
	 */
	public abstract  Map<String,String> getExpressions();
	
	/**
	 * 获取表达式
	 */
	public abstract List<ExpressionVo> getExpressionsObj();
	
	/**
	 * 获取数据
	 * @return
	 */
	public abstract  List<OriginalData> getData(Long compId, String finDate);
	
	/**
	 * 加载指标表达式
	 * @param sqlQueryEcpression
	 * @return
	 */
	protected Map<String, String> loadExpressions(String sql, JdbcTemplate jdbcTemplate) {
		List<ExpressionVo> listExpression = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ExpressionVo.class));
		
		Map<String,String> expressions = new HashMap<>();
		listExpression.forEach(item -> {
			expressions.put(item.getField(), item.getExpressionFormat());
		});
		System.out.println(expressions.get("tngbl_tot_asst"));
		return expressions;
	}
	
	/**
	 * 加载数据
	 * @param sqlQuery
	 * @return
	 */
	protected List<OriginalData> loadData(String sqlQuery, JdbcTemplate jdbcTemplate) {
		List<Map<String,Object>> list = jdbcTemplate.queryForList(sqlQuery);
		//OriginalData od  = new OriginalData(list);
		
		//按发行人id分组
		Map<Object, List<Map<String, Object>>> issData = list.stream().collect(
			Collectors.groupingBy(m -> m.get("comp_id"),Collectors.toList())
		);
		
		//构建返回数据
		List<OriginalData> data = new ArrayList<>();
		issData.forEach((k,v) -> {
			data.add(new OriginalData(v));
		});
		return data;
	}

	
}
