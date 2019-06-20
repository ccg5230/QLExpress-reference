package com.innodealing.json;

import java.io.Serializable;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

/** 
* @author 赵正来
* @date 2017年5月22日 上午10:34:56 
* @describe  mq 指标变动提醒mq body
*/
public class FinSpclindicatorJson implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@ApiModelProperty(name="发行人ID")
    private Long comUniCode;
	
	@ApiModelProperty(name="报表日期")
	private String finQuarter;
	
	@ApiModelProperty(name="同比")
	private Map<String,Object> YOY;
	
	@ApiModelProperty(name="行业排名")
	private Map<String,Object> RANK;
	
	@ApiModelProperty(name="指标自身")
	private Map<String,Object> SELF;

	public Long getComUniCode() {
		return comUniCode;
	}

	public void setComUniCode(Long comUniCode) {
		this.comUniCode = comUniCode;
	}

	public String getFinQuarter() {
		return finQuarter;
	}

	public void setFinQuarter(String finQuarter) {
		this.finQuarter = finQuarter;
	}

	public Map<String, Object> getYOY() {
		return YOY;
	}

	public void setYOY(Map<String, Object> yOY) {
		YOY = yOY;
	}

	public Map<String, Object> getRANK() {
		return RANK;
	}

	public void setRANK(Map<String, Object> rANK) {
		RANK = rANK;
	}

	public Map<String, Object> getSELF() {
		return SELF;
	}

	public void setSELF(Map<String, Object> sELF) {
		SELF = sELF;
	}


	
	
	
}
