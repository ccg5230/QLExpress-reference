/**
 * AppConfig.java
 * com.innodealing.config
 *
 * Function： TODO 
 *
 *   ver     date      		author
 * ──────────────────────────────────
 *   		 2017年5月27日 		chungaochen
 *
 * Copyright (c) 2017, DealingMatrix All Rights Reserved.
*/

package com.innodealing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ClassName:AppConfig
 * Function: 配置文件属性获取类
 * Reason:	 
 *
 * @author   chungaochen
 * @version  
 * @since    Ver 1.1
 * @Date	 2017年5月27日		下午3:46:08
 *
 * @see 	 
 */
@ConfigurationProperties(prefix = "amaresun_")
public class AppConfig {
    
    /** 接口调用方用户id */
    @Value("${amaresun_credit_user_id}")
    private String amaresunUserId;
    /** 接口调用方用户名称 */
    @Value("${amaresun_credit_user_name}")
    private String amaresunUserName;
    /** 接口调用方用户密码*/
    @Value("${amaresun_credit_password}")
    private String amaresunPassword;
    /** 令牌接口url*/
    @Value("${amaresun_auth_url}")
    private String amaresunAuthUrl;
    /** 评级计算接口url*/
    @Value("${amaresun_rating_url}")
    private String amaresunRatingUrl;
    /** 财务质量分析计算接口url*/
    @Value("${amaresun_quality_url}")
    private String amaresunQualityUrl;
    
    public String getAmaresunUserId() {
        return amaresunUserId;
    }
    public void setAmaresunUserId(String amaresunUserId) {
        this.amaresunUserId = amaresunUserId;
    }
    public String getAmaresunUserName() {
        return amaresunUserName;
    }
    public void setAmaresunUserName(String amaresunUserName) {
        this.amaresunUserName = amaresunUserName;
    }
    public String getAmaresunPassword() {
        return amaresunPassword;
    }
    public void setAmaresunPassword(String amaresunPassword) {
        this.amaresunPassword = amaresunPassword;
    }
    public String getAmaresunAuthUrl() {
        return amaresunAuthUrl;
    }
    public void setAmaresunAuthUrl(String amaresunAuthUrl) {
        this.amaresunAuthUrl = amaresunAuthUrl;
    }
    public String getAmaresunRatingUrl() {
        return amaresunRatingUrl;
    }
    public void setAmaresunRatingUrl(String amaresunRatingUrl) {
        this.amaresunRatingUrl = amaresunRatingUrl;
    }
    public String getAmaresunQualityUrl() {
        return amaresunQualityUrl;
    }
    public void setAmaresunQualityUrl(String amaresunQualityUrl) {
        this.amaresunQualityUrl = amaresunQualityUrl;
    }
    @Override
    public String toString() {
        return "AppConfig [amaresunUserId=" + amaresunUserId + ", amaresunUserName=" + amaresunUserName + ", amaresunPassword=" + amaresunPassword
                + ", amaresunAuthUrl=" + amaresunAuthUrl + ", amaresunRatingUrl=" + amaresunRatingUrl + ", amaresunQualityUrl=" + amaresunQualityUrl + "]";
    }
    
    
    
}

