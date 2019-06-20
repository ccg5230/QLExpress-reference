/**
 * ResponseData.java
 * com.innodealing.constant
 *
 * Function： TODO 
 *
 *   ver     date      		author
 * ──────────────────────────────────
 *   		 2017年6月6日 		chungaochen
 *
 * Copyright (c) 2017, DealingMatrix All Rights Reserved.
*/

package com.innodealing.constant;

import java.io.Serializable;

/**
 * ClassName:ResponseData
 * Function: 返回结果封装类
 * Reason:	 TODO ADD REASON
 *
 * @author   chungaochen
 * @version  
 * @since    Ver 1.1
 * @Date	 2017年6月6日		上午11:43:43
 *
 * @see 	 
 */
public class ResponseData implements Serializable {

    /**
     * serialVersionUID:TODO（用一句话描述这个变量表示什么）
     *
     * @since Ver 1.1
     */
    
    private static final long serialVersionUID = 1L;

    private String responseCode;
    
    private String responseMessage;
    
    private Object  responseData;

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public Object getResponseData() {
        return responseData;
    }

    public void setResponseData(Object responseData) {
        this.responseData = responseData;
    }

    @Override
    public String toString() {
        return "ResponseData [responseCode=" + responseCode + ", responseMessage=" + responseMessage + ", responseData=" + responseData + "]";
    }
    
    
}

