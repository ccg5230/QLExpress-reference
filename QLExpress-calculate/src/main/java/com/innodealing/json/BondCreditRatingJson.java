/**
 * BondCreditRatingJson.java
 * com.innodealing.json
 *
 * Function： TODO 
 *
 *   ver     date      		author
 * ──────────────────────────────────
 *   		 2017年5月31日 		chungaochen
 *
 * Copyright (c) 2017, DealingMatrix All Rights Reserved.
*/

package com.innodealing.json;

import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;

/**
 * ClassName:BondCreditRatingJson
 * Function: TODO ADD FUNCTION
 * Reason:	 TODO ADD REASON
 *
 * @author   chungaochen
 * @version  
 * @since    Ver 1.1
 * @Date	 2017年5月31日		下午3:02:12
 *
 * @see 	 
 */
public class BondCreditRatingJson implements Serializable {

    /**
     * serialVersionUID:TODO（用一句话描述这个变量表示什么）
     *
     * @since Ver 1.1
     */
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "所需模型名称")
    private String modelId;
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
    
    public String getModelId() {
        return modelId;
    }
    public void setModelId(String modelId) {
        this.modelId = modelId;
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
    @Override
    public String toString() {
        return "BondCreditRatingJson [requiredModel=" + modelId + ", ratio1=" + ratio1 + ", ratio2=" + ratio2 + ", ratio3=" + ratio3 + ", ratio4="
                + ratio4 + ", ratio5=" + ratio5 + ", ratio6=" + ratio6 + ", ratio7=" + ratio7 + ", ratio8=" + ratio8 + ", ratio9=" + ratio9 + ", ratio10="
                + ratio10 + "]";
    }
    

}

