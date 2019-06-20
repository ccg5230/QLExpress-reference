package com.innodealing.vo;

import java.io.Serializable;
/**
 * 表达式vo
 * @author 赵正来
 *
 */
public class ExpressionVo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3243146181065929295L;

	/**
	 * table名称
	 */
	String tableName;
	
	/**
	 * 指标code
	 */
	String field;
	
	/**
	 * 指标名称
	 */
	String fieldName;
	
	/**
	 * 原始表达式
	 */
	String expression;
	
	/**
	 * 格式化后的表达式
	 */
	String expressionFormat;
	
	/**
	 * 备注说明
	 */
	String remark;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getField() {
		return  field.toLowerCase();
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getExpression() {
		return expression == null ? null : 
			formate(expression);
	}

	private String formate(String expression) {
		return expression.toLowerCase()
			.replaceAll("（", "(")
			.replaceAll("）", ")")
			.replaceAll("\\[", "(")
			.replaceAll("\\]", ")")
			.replaceAll("＋", "+")
			.replaceAll("－", "-")
			.replaceAll("，", ",")
			.replaceAll("；", ",");
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getExpressionFormat() {
		return expressionFormat == null ? null : 
			formate(expressionFormat);
	}

	public void setExpressionFormat(String expressionFormat) {
		this.expressionFormat = expressionFormat;
	}
	
	
	
}
