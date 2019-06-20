package com.innodealing.domain;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@Document(collection="bond_ccxe_fina_sheet_sync")
public class BondCcxeFinaSheetSyncBookMark {
	
	@Id
	private String ccxeFinaSheetTableName;
	private Long comUniCode;
	private Date endDate;
	private Date ccxeId;
	
	public String getCcxeFinaSheetTableName() {
		return ccxeFinaSheetTableName;
	}
	public void setCcxeFinaSheetTableName(String ccxeFinaSheetTableName) {
		this.ccxeFinaSheetTableName = ccxeFinaSheetTableName;
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
	public Date getCcxeId() {
		return ccxeId;
	}
	public void setCcxeId(Date ccxeId) {
		this.ccxeId = ccxeId;
	}
	@Override
	public String toString() {
		return "BondCcxeFinaSheetSyncBookMark [ccxeFinaSheetTableName=" + ccxeFinaSheetTableName + ", comUniCode="
				+ comUniCode + ", endDate=" + endDate + ", ccxeId=" + ccxeId + "]";
	}
	
	
	
}
