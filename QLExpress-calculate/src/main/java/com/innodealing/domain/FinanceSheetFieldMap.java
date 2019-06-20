package com.innodealing.domain;

import java.math.BigDecimal;
import java.util.Date;


public class FinanceSheetFieldMap {

	String tableName;
	String fieldName;
	String fieldChinName;
	String ccxeTableName;
	String ccxeFieldName;
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getFieldChinName() {
		return fieldChinName;
	}
	public void setFieldChinName(String fieldChinName) {
		this.fieldChinName = fieldChinName;
	}
	public String getCcxeTableName() {
		return ccxeTableName;
	}
	public void setCcxeTableName(String ccxeTableName) {
		this.ccxeTableName = ccxeTableName;
	}
	public String getCcxeFieldName() {
		return ccxeFieldName;
	}
	public void setCcxeFieldName(String ccxeFieldName) {
		this.ccxeFieldName = ccxeFieldName;
	}
	@Override
	public String toString() {
		return "FinanceSheetFieldMap [tableName=" + tableName + ", fieldName=" + fieldName + ", fieldChinName="
				+ fieldChinName + ", ccxeTableName=" + ccxeTableName + ", ccxeFieldName=" + ccxeFieldName + "]";
	}
	
	

}
