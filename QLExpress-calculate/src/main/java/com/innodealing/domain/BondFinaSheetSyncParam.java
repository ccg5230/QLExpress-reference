package com.innodealing.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.innodealing.util.SafeUtils;

public class BondFinaSheetSyncParam {

	private Long comUniCode;
	private Date endDate;
	private Long amaComId;
	private String finaSheetTableName;
	private Map<String, Date> ccxeidMap = new HashMap<String, Date>();

	public BondFinaSheetSyncParam(Long comUniCode, Long amaComId, Date endDate, String finaSheetTableName) {
		super();
		this.comUniCode = comUniCode;
		this.endDate = endDate;
		this.amaComId = amaComId;
		this.finaSheetTableName = finaSheetTableName;
	}

	public BondFinaSheetSyncParam() {
		// TODO Auto-generated constructor stub
	}

	public Long getComUniCode() {
		return comUniCode;
	}

	public void setComUniCode(Long comUniCode) {
		this.comUniCode = comUniCode;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Long getAmaComId() {
		return amaComId;
	}

	public void setAmaComId(Long amaComId) {
		this.amaComId = amaComId;
	}

	public String getFinaSheetTableName() {
		return finaSheetTableName;
	}

	public void setFinaSheetTableName(String finaSheetTableName) {
		this.finaSheetTableName = finaSheetTableName;
	}

	public Map<String, Date> getCcxeidMap() {
		return ccxeidMap;
	}

	public void setCcxeidMap(Map<String, Date> ccxeidMap) {
		this.ccxeidMap = ccxeidMap;
	}

	@Override
	public String toString() {
		return "BondFinaSheetSyncParam [" + (comUniCode != null ? "comUniCode=" + comUniCode + ", " : "")
				+ (endDate != null ? "endDate=" + SafeUtils.convertDateToString(endDate, SafeUtils.DATE_FORMAT) + ", " : "")
				+ (amaComId != null ? "amaComId=" + amaComId + ", " : "")
				+ (finaSheetTableName != null ? "finaSheetTableName=" + finaSheetTableName : "") + "]";
	}
	
}
