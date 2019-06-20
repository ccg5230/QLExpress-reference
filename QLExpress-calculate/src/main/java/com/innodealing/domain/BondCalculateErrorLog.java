/**
 * BondCalculateErrorLog.java
 * com.innodealing.domain
 *
 * Function： 财报指标计算异常日志类 
 *
 *   ver     date      		author
 * ──────────────────────────────────
 *   		 2017年5月27日 		chungaochen
 *
 * Copyright (c) 2017, DealingMatrix All Rights Reserved.
*/

package com.innodealing.domain;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

import io.swagger.annotations.ApiModelProperty;

/**
 * ClassName:BondCalculateErrorLog
 * Function: 主体财报指标计算错误日志domain
 * Reason:	 
 *
 * @author   chungaochen
 * @version  
 * @since    Ver 1.1
 * @Date	 2017年5月27日		上午11:19:47
 *
 * @see 	 
 */
public class BondCalculateErrorLog implements Serializable  {
    
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键id")
    private Long id;
    
    @ApiModelProperty(value = "主体|发行人id（安硕）")
    private Long amaComId;
    
    @ApiModelProperty(value = "公司统一编码")
    private Long comUniCode;
    
    @ApiModelProperty(value = "财报表名")
    private String tableName;
    
    @ApiModelProperty(value = "报表日期")
    private Date finDate;
    
    @ApiModelProperty(value = "指标名称")
    private String ratio;
    
    @ApiModelProperty(value = "指标计算公式")
    private String expressFormat;
    
    @ApiModelProperty(value = "错误类型：指标计算错误/年化错误")
    private String errorType;

    @ApiModelProperty(value = "错误备注")
    private String errorRemark;
    
    @ApiModelProperty(value = "创建时间")
    private Timestamp createTime;
    
    @ApiModelProperty(value = "最后更新时间")
    private Timestamp lastUpdateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAmaComId() {
        return amaComId;
    }

    public void setAmaComId(Long amaComId) {
        this.amaComId = amaComId;
    }

    public Long getComUniCode() {
        return comUniCode;
    }

    public void setComUniCode(Long comUniCode) {
        this.comUniCode = comUniCode;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Date getFinDate() {
        return finDate;
    }

    public void setFinDate(Date finDate) {
        this.finDate = finDate;
    }

    public String getRatio() {
        return ratio;
    }

    public void setRatio(String ratio) {
        this.ratio = ratio;
    }

    public String getExpressFormat() {
        return expressFormat;
    }

    public void setExpressFormat(String expressFormat) {
        this.expressFormat = expressFormat;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getErrorRemark() {
        return errorRemark;
    }

    public void setErrorRemark(String errorRemark) {
        this.errorRemark = errorRemark;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public String toString() {
        return "BondCalculateErrorLog [id=" + id + ", amaComId=" + amaComId + ", comUniCode=" + comUniCode + ", tableName=" + tableName + ", finDate=" + finDate
                + ", ratio=" + ratio + ", expressFormat=" + expressFormat + ", errorType=" + errorType + ", errorRemark=" + errorRemark + ", createTime="
                + createTime + ", lastUpdateTime=" + lastUpdateTime + "]";
    }


}

