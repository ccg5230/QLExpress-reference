package com.innodealing.engine;

import java.math.BigDecimal;
import java.util.Map;

public abstract class Calculate {

	/**
	 * 解析
	 * @param expression
	 * @return
	 */
	public abstract String parseExpression(String expression);
	
	/**
	 * 计算
	 * @param OriginalData
	 * @param expression
	 * @return
	 */
	public abstract BigDecimal calculate(Map<String, Object> OriginalData,String expression ) throws Exception;
	
}
