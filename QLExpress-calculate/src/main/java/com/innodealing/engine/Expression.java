package com.innodealing.engine;

import java.util.List;
import java.util.Map;

/**
 * 主体指标表达式
 * @author 赵正来
 *
 */
public class Expression {
	/**
	 * 主体指标表达式
	 */
	List<Map<String,Object>> expressions;

	public List<Map<String, Object>> getExpressions() {
		return expressions;
	}

	public void setExpressions(List<Map<String, Object>> expressions) {
		this.expressions = expressions;
	}

	
	
}
