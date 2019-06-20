package com.innodealing.json;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * 
 * ClassName:CreditRatingCalculateJson
 * Function: 财报指标计算MQ消息队列数据json
 * Reason:	 
 *
 * @author   chungaochen
 * @version  
 * @since    Ver 1.1
 * @Date	 2017	2017年6月19日		上午10:34:31
 *
 * @see
 */
public class CreditRatingCalculateJson  implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(name="发行人ID/安硕")
    private List<Long> compIds;
    
    @ApiModelProperty(name="财报来源（0-中诚信/1-DM）")
    private String sheetSource;
    
    @ApiModelProperty(name="安硕接口令牌")
    private String token;

    public CreditRatingCalculateJson(List<Long> compIds, String sheetSource,String token) {
        super();
        this.compIds = compIds;
        this.sheetSource = sheetSource;
        this.token = token;
    }
    

    public List<Long> getCompIds() {
        return compIds;
    }

    public void setCompIds(List<Long> compIds) {
        this.compIds = compIds;
    }


    public String getSheetSource() {
        return sheetSource;
    }

    public void setSheetSource(String sheetSource) {
        this.sheetSource = sheetSource;
    }
    
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "CreditRatingCalculateJson [compIds=" + compIds + ", sheetSource=" + sheetSource + ", token=" + token + "]";
    }

}
