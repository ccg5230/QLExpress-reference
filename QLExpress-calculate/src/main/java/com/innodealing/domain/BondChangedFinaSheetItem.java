package com.innodealing.domain;

import java.math.BigDecimal;
import java.util.Date;


public class BondChangedFinaSheetItem {

	private Long comUniCode;
	private Long amaComId;
	private Date endDate;
	private String finaSheetTableName;
	private String ccxeFinaSheetTableName;
	private Date ccxeId;
	
	public BondChangedFinaSheetItem()
	{
	}
	
	public BondChangedFinaSheetItem(Long comUniCode, Long amaComId, Date endDate, String finaSheetTableName) {
		super();
		this.comUniCode = comUniCode;
		this.amaComId = amaComId;
		this.endDate = endDate;
		this.finaSheetTableName = finaSheetTableName;
	}
	public Long getComUniCode() {
		return comUniCode;
	}
	public void setComUniCode(Long comUniCode) {
		this.comUniCode = comUniCode;
	}
	public Long getAmaComId() {
		return amaComId;
	}
	public void setAmaComId(Long amaComId) {
		this.amaComId = amaComId;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public String getFinaSheetTableName() {
		return finaSheetTableName;
	}
	public void setFinaSheetTableName(String finaSheetTableName) {
		this.finaSheetTableName = finaSheetTableName;
	}
	public String getCcxeFinaSheetTableName() {
		return ccxeFinaSheetTableName;
	}
	public void setCcxeFinaSheetTableName(String ccxeFinaSheetTableName) {
		this.ccxeFinaSheetTableName = ccxeFinaSheetTableName;
	}
	public Date getCcxeId() {
		return ccxeId;
	}
	public void setCcxeId(Date ccxeId) {
		this.ccxeId = ccxeId;
	}

	@Override
	public String toString() {
		return "BondFinaSheetKey [comUniCode=" + comUniCode + ", amaComId=" + amaComId + ", endDate=" + endDate
				+ ", finaSheetTableName=" + finaSheetTableName + ", ccxeFinaSheetTableName=" + ccxeFinaSheetTableName
				+ ", ccxeId=" + ccxeId + "]";
	}

}
