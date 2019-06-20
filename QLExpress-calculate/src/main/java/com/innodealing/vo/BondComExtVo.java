package com.innodealing.vo;

import java.io.Serializable;

/**
 * 主体关系类
 * @author 赵正来
 *
 */
public class BondComExtVo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * dm 主体id
	 */
	private Long  comUniCode;
	
	/**
	 * dm 主体名称
	 */
	private String comChiName;
	
	/**
	 * 安硕 主体id
	 */
	private Long amaComId;
	
	/**
	 * 安硕 主体名称
	 */
	private String amaComName;

	/**
	 * 行业id
	 */
	private Long induUniCode;
	
	
	/**
	 * 行业四级分类名称
	 */
	private String induUniNameL4;
	
	public Long getComUniCode() {
		return comUniCode;
	}

	public void setComUniCode(Long comUniCode) {
		this.comUniCode = comUniCode;
	}

	public String getComChiName() {
		return comChiName;
	}

	public void setComChiName(String comChiName) {
		this.comChiName = comChiName;
	}

	public Long getAmaComId() {
		return amaComId;
	}

	public void setAmaComId(Long amaComId) {
		this.amaComId = amaComId;
	}

	public String getAmaComName() {
		return amaComName;
	}

	public void setAmaComName(String amaComName) {
		this.amaComName = amaComName;
	}

	public Long getInduUniCode() {
		return induUniCode;
	}

	public void setInduUniCode(Long induUniCode) {
		this.induUniCode = induUniCode;
	}

	public String getInduUniNameL4() {
		return induUniNameL4;
	}

	public void setInduUniNameL4(String induUniNameL4) {
		this.induUniNameL4 = induUniNameL4;
	}

	@Override
	public String toString() {
		return "BondComExtVo [comUniCode=" + comUniCode + ", comChiName=" + comChiName + ", amaComId=" + amaComId
				+ ", amaComName=" + amaComName + ", induUniCode=" + induUniCode + ", induUniNameL4=" + induUniNameL4
				+ "]";
	}

	
	
}