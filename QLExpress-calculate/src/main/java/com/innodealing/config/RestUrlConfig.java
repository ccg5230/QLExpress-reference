package com.innodealing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
/**
 * 项目中所有用到rest请求的配置内容
 * @author 赵正来
 *
 */

@ConfigurationProperties(prefix="rest")
@Component
public class RestUrlConfig {

	/**
	 * 同步主体指标到mongo
	 */
	
	@Value("${rest.integration.sync.com.indicator}")
	public String bondIntegrationSyncComIndicator;
	
	/**
	 * 获取指标分位值
	 */
	@Value("${rest.bond.web.indicators.quartile}")
	public String bondWebindicatorsQuartile;
	
	/**
	 * 添加指标备忘录
	 */
	@Value("${rest.bond.web.indicators.memento.add}")
	public String bondWebindicatorsMementoAdd;
	
	
	
	
	
	
}
