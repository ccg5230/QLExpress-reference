/**
 * BondCreditRating.java
 * com.innodealing.domain
 *
 * Function： TODO 
 *
 *   ver     date      		author
 * ──────────────────────────────────
 *   		 2017年5月27日 		chungaochen
 *
 * Copyright (c) 2017, DealingMatrix All Rights Reserved.
*/

package com.innodealing.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Date;

import io.swagger.annotations.ApiModelProperty;

/**
 * ClassName:BondCreditRating
 * Function: 主体财报指标质量得分评级domain
 * Reason:	 
 *
 * @author   chungaochen
 * @version  
 * @since    Ver 1.1
 * @Date	 2017年5月27日		上午11:19:47
 *
 * @see 	 
 */
public class BondCreditRating implements Serializable  {
    
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键id")
    private Long id;
    
    @ApiModelProperty(value = "公司名称")
    private String comChiName;
    
    @ApiModelProperty(value = "公司统一编码")
    private Long comUniCode;
    
    @ApiModelProperty(value = "模型ID：bank-银行 indu-工业 insu-保险 secu-证券 busin-商业 estate-房地产 nfin非金融（包含indu、busin、estate）")
    private String modelId;
    
    @ApiModelProperty(value = "报表日期")
    private Date finDate;
    
    @ApiModelProperty(value = "创建时间")
    private Timestamp createTime;
    
    @ApiModelProperty(value = "最后更新时间")
    private Timestamp lastUpdateTime;
    @ApiModelProperty(value = "RATIO1指标值")
    private BigDecimal ratio1;
    @ApiModelProperty(value = "RATIO2指标值")
    private BigDecimal ratio2;
    @ApiModelProperty(value = "RATIO3指标值")
    private BigDecimal ratio3;
    @ApiModelProperty(value = "RATIO4指标值")
    private BigDecimal ratio4;
    @ApiModelProperty(value = "RATIO5指标值")
    private BigDecimal ratio5;
    @ApiModelProperty(value = "RATIO6指标值")
    private BigDecimal ratio6;
    @ApiModelProperty(value = "RATIO7指标值")
    private BigDecimal ratio7;
    @ApiModelProperty(value = "RATIO8指标值")
    private BigDecimal ratio8;
    @ApiModelProperty(value = "RATIO9指标值")
    private BigDecimal ratio9;
    @ApiModelProperty(value = "RATIO10指标值")
    private BigDecimal ratio10;
    @ApiModelProperty(value = "量化风险等级")
    private String rating;
    @ApiModelProperty(value = "RATIO1指标得分")
    private BigDecimal ratio1Score;
    @ApiModelProperty(value = "RATIO2指标得分")
    private BigDecimal ratio2Score;
    @ApiModelProperty(value = "RATIO3指标得分")
    private BigDecimal ratio3Score;
    @ApiModelProperty(value = "RATIO4指标得分")
    private BigDecimal ratio4Score;
    @ApiModelProperty(value = "RATIO5指标得分")
    private BigDecimal ratio5Score;
    @ApiModelProperty(value = "RATIO6指标得分")
    private BigDecimal ratio6Score;
    @ApiModelProperty(value = "RATIO7指标得分")
    private BigDecimal ratio7Score;
    @ApiModelProperty(value = "RATIO8指标得分")
    private BigDecimal ratio8Score;
    @ApiModelProperty(value = "RATIO9指标得分")
    private BigDecimal ratio9Score;
    @ApiModelProperty(value = "RATIO10指标得分")
    private BigDecimal ratio10Score;
    @ApiModelProperty(value = "财务质量分析评分")
    private BigDecimal finQualityScore;
    @ApiModelProperty(value = "来源（DM/中诚信）")
    private String source;
    @ApiModelProperty(value = "安硕计算结果备注")
    private String remark;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getComChiName() {
        return comChiName;
    }
    public void setComChiName(String comChiName) {
        this.comChiName = comChiName;
    }
    public Long getComUniCode() {
        return comUniCode;
    }
    public void setComUniCode(Long comUniCode) {
        this.comUniCode = comUniCode;
    }
    public String getModelId() {
        return modelId;
    }
    public void setModelId(String modelId) {
        this.modelId = modelId;
    }
    public Date getFinDate() {
        return finDate;
    }
    public void setFinDate(Date finDate) {
        this.finDate = finDate;
    }
    public Timestamp getCreateTtime() {
        return createTime;
    }
    public void setCreateTtime(Timestamp createTime) {
        this.createTime = createTime;
    }
    public Timestamp getLastUpdateTime() {
        return lastUpdateTime;
    }
    public void setLastUpdateTime(Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
    public BigDecimal getRatio1() {
        return ratio1;
    }
    public void setRatio1(BigDecimal ratio1) {
        this.ratio1 = ratio1;
    }
    public BigDecimal getRatio2() {
        return ratio2;
    }
    public void setRatio2(BigDecimal ratio2) {
        this.ratio2 = ratio2;
    }
    public BigDecimal getRatio3() {
        return ratio3;
    }
    public void setRatio3(BigDecimal ratio3) {
        this.ratio3 = ratio3;
    }
    public BigDecimal getRatio4() {
        return ratio4;
    }
    public void setRatio4(BigDecimal ratio4) {
        this.ratio4 = ratio4;
    }
    public BigDecimal getRatio5() {
        return ratio5;
    }
    public void setRatio5(BigDecimal ratio5) {
        this.ratio5 = ratio5;
    }
    public BigDecimal getRatio6() {
        return ratio6;
    }
    public void setRatio6(BigDecimal ratio6) {
        this.ratio6 = ratio6;
    }
    public BigDecimal getRatio7() {
        return ratio7;
    }
    public void setRatio7(BigDecimal ratio7) {
        this.ratio7 = ratio7;
    }
    public BigDecimal getRatio8() {
        return ratio8;
    }
    public void setRatio8(BigDecimal ratio8) {
        this.ratio8 = ratio8;
    }
    public BigDecimal getRatio9() {
        return ratio9;
    }
    public void setRatio9(BigDecimal ratio9) {
        this.ratio9 = ratio9;
    }
    public BigDecimal getRatio10() {
        return ratio10;
    }
    public void setRatio10(BigDecimal ratio10) {
        this.ratio10 = ratio10;
    }
    public String getRating() {
        return rating;
    }
    public void setRating(String rating) {
        this.rating = rating;
    }
    public BigDecimal getRatio1Score() {
        return ratio1Score;
    }
    public void setRatio1Score(BigDecimal ratio1Score) {
        this.ratio1Score = ratio1Score;
    }
    public BigDecimal getRatio2Score() {
        return ratio2Score;
    }
    public void setRatio2Score(BigDecimal ratio2Score) {
        this.ratio2Score = ratio2Score;
    }
    public BigDecimal getRatio3Score() {
        return ratio3Score;
    }
    public void setRatio3Score(BigDecimal ratio3Score) {
        this.ratio3Score = ratio3Score;
    }
    public BigDecimal getRatio4Score() {
        return ratio4Score;
    }
    public void setRatio4Score(BigDecimal ratio4Score) {
        this.ratio4Score = ratio4Score;
    }
    public BigDecimal getRatio5Score() {
        return ratio5Score;
    }
    public void setRatio5Score(BigDecimal ratio5Score) {
        this.ratio5Score = ratio5Score;
    }
    public BigDecimal getRatio6Score() {
        return ratio6Score;
    }
    public void setRatio6Score(BigDecimal ratio6Score) {
        this.ratio6Score = ratio6Score;
    }
    public BigDecimal getRatio7Score() {
        return ratio7Score;
    }
    public void setRatio7Score(BigDecimal ratio7Score) {
        this.ratio7Score = ratio7Score;
    }
    public BigDecimal getRatio8Score() {
        return ratio8Score;
    }
    public void setRatio8Score(BigDecimal ratio8Score) {
        this.ratio8Score = ratio8Score;
    }
    public BigDecimal getRatio9Score() {
        return ratio9Score;
    }
    public void setRatio9Score(BigDecimal ratio9Score) {
        this.ratio9Score = ratio9Score;
    }
    public BigDecimal getRatio10Score() {
        return ratio10Score;
    }
    public void setRatio10Score(BigDecimal ratio10Score) {
        this.ratio10Score = ratio10Score;
    }
    public BigDecimal getFinQualityScore() {
        return finQualityScore;
    }
    public void setFinQualityScore(BigDecimal finQualityScore) {
        this.finQualityScore = finQualityScore;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }
    @Override
    public String toString() {
        return "BondCreditRating [id=" + id + ", comChiName=" + comChiName + ", comUniCode=" + comUniCode + ", modelId=" + modelId + ", finDate=" + finDate
                + ", createTtime=" + createTime + ", lastUpdateTime=" + lastUpdateTime + ", ratio1=" + ratio1 + ", ratio2=" + ratio2 + ", ratio3=" + ratio3
                + ", ratio4=" + ratio4 + ", ratio5=" + ratio5 + ", ratio6=" + ratio6 + ", ratio7=" + ratio7 + ", ratio8=" + ratio8 + ", ratio9=" + ratio9
                + ", ratio10=" + ratio10 + ", rating=" + rating + ", ratio1Score=" + ratio1Score + ", ratio2Score=" + ratio2Score + ", ratio3Score="
                + ratio3Score + ", ratio4Score=" + ratio4Score + ", ratio5Score=" + ratio5Score + ", ratio6Score=" + ratio6Score + ", ratio7Score="
                + ratio7Score + ", ratio8Score=" + ratio8Score + ", ratio9Score=" + ratio9Score + ", ratio10Score=" + ratio10Score + ", finQualityScore="
                + finQualityScore + ", source=" + source + ", remark=" + remark + "]";
    }
    

}

