package com.innodealing.domain;

import java.io.Serializable;
import java.sql.Date;

/**
 * 财务质量等级
 * @author 赵正来
 *
 */
public class DmFinQualityAnalysis implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer id;
	 
	private String custid;
	
	private String year;
	
	private String tape;
	
	private Float quanScore;
	
	private Byte visible;
	
	private Date createTime;
	
	private Integer insertUser;
	
	private Date lastUPdateTime;
	
	private Integer lastUpdateUser;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCustid() {
		return custid;
	}

	public void setCustid(String custid) {
		this.custid = custid;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getTape() {
		return tape;
	}

	public void setTape(String tape) {
		this.tape = tape;
	}

	public Float getQuanScore() {
		return quanScore;
	}

	public void setQuanScore(Float quanScore) {
		this.quanScore = quanScore;
	}

	public Byte getVisible() {
		return visible;
	}

	public void setVisible(Byte visible) {
		this.visible = visible;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getInsertUser() {
		return insertUser;
	}

	public void setInsertUser(Integer insertUser) {
		this.insertUser = insertUser;
	}

	public Date getLastUPdateTime() {
		return lastUPdateTime;
	}

	public void setLastUPdateTime(Date lastUPdateTime) {
		this.lastUPdateTime = lastUPdateTime;
	}

	public Integer getLastUpdateUser() {
		return lastUpdateUser;
	}

	public void setLastUpdateUser(Integer lastUpdateUser) {
		this.lastUpdateUser = lastUpdateUser;
	}

	@Override
	public String toString() {
		return "DmFinQualityAnalysis [id=" + id + ", custid=" + custid + ", year=" + year + ", tape=" + tape
				+ ", quanScore=" + quanScore + ", visible=" + visible + ", createTime=" + createTime + ", insertUser="
				+ insertUser + ", lastUPdateTime=" + lastUPdateTime + ", lastUpdateUser=" + lastUpdateUser + "]";
	}
	
	
	
	
}
