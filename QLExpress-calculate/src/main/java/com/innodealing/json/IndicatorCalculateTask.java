package com.innodealing.json;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;


public class IndicatorCalculateTask  implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(name="发行人ID/安硕")
    private List<Long> compIds;
    
    @ApiModelProperty(name="主体类型(bank、indu、insu、secu)")
    private String issuerType;

    public IndicatorCalculateTask(List<Long> compIds, String issuerType) {
        super();
        this.compIds = compIds;
        this.issuerType = issuerType;
    }
    
    public List<Long> getCompId() {
        return compIds;
    }

    public void setCompId(List<Long> compId) {
        this.compIds = compId;
    }

    public String getIssuerType() {
        return issuerType;
    }

    public void setIssuerType(String issuerType) {
        this.issuerType = issuerType;
    }

    @Override
    public String toString() {
        return "IndicatorCalculateTask [compIds=" + compIds + ", issuerType=" + issuerType + "]";
    }

}
