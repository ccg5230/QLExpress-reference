package com.innodealing.vo;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
/**
 * <p>单个指标VO,用于返回计算指标的值
 * @author 赵正来
 *
 */
@ApiModel(description = "单个指标VO")
@JsonInclude(value = Include.NON_NULL)
public class IndicatorVo {
	
	@ApiModelProperty(value = "指标代码")
	private String indicatorCode;
	
	@ApiModelProperty(value = "指标名称")
	private String indicatorName;
	
	@ApiModelProperty(value = "指标值")
	private BigDecimal indicatorValue;

	public String getIndicatorCode() {
		return indicatorCode;
	}

	public void setIndicatorCode(String indicatorCode) {
		this.indicatorCode = indicatorCode;
	}

	public String getIndicatorName() {
		return indicatorName;
	}

	public void setIndicatorName(String indicatorName) {
		this.indicatorName = indicatorName;
	}

	public BigDecimal getIndicatorValue() {
		return indicatorValue;
	}

	public void setIndicatorValue(BigDecimal indicatorValue) {
		this.indicatorValue = indicatorValue;
	}
	
	
}
