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
@Document(collection="bond_ccxe_fina_sheet_timestamp")
public class BondFinaSheetTimestamp {
	
	@Id
	private String comUniCodeData;
	private Date timeStamp;
	
	public String getComUniCodeData() {
		return comUniCodeData;
	}
	public void setComUniCodeData(String comUniCodeData) {
		this.comUniCodeData = comUniCodeData;
	}
	public Date getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	
}
