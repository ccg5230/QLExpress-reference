package com.innodealing.domain;

import java.util.Date;


public class BondFinaSheetId {

	private Long comUniCode;
	private Date endDate;
	
	public BondFinaSheetId(Long comUniCode, Date endDate) {
		super();
		this.comUniCode = comUniCode;
		this.endDate = endDate;
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((comUniCode == null) ? 0 : comUniCode.hashCode());
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BondFinaSheetId other = (BondFinaSheetId) obj;
		if (comUniCode == null) {
			if (other.comUniCode != null)
				return false;
		} else if (!comUniCode.equals(other.comUniCode))
			return false;
		if (endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!endDate.equals(other.endDate))
			return false;
		return true;
	}
	
}
