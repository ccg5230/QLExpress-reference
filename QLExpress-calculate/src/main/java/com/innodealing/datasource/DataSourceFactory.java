package com.innodealing.datasource;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.innodealing.constant.CommonConstant;

/**
 * 数据工厂
 * @author 赵正来
 *
 */
@Component
public class  DataSourceFactory {
	
	public enum DataSourceType{
		INDU,INSU,BANK,SECU
	}
	
    @Autowired private ListableBeanFactory listableBeanFactory;
	
	private static DataSource dataSource = null;
	
	/**
	 * 
	 * @param type  {@link com.innodealing.datasource.DataSourceFactory.Type Type}
	 * @return
	 */
	public DataSource createData(String type){
		if(dataSource != null){
			return dataSource;
		}
		
		switch (type) {
		case CommonConstant.ISSUER_TYPE_INDU:
			dataSource = listableBeanFactory.getBean(DataSourceIndu.class);
			break;
		case CommonConstant.ISSUER_TYPE_INSU:
			dataSource = listableBeanFactory.getBean(DataSourceInsu.class);
			break;
		case CommonConstant.ISSUER_TYPE_BANK:
			dataSource = listableBeanFactory.getBean(DataSourceBank.class);
			break;
		case CommonConstant.ISSUER_TYPE_SECU:
			dataSource = listableBeanFactory.getBean(DataSourceSecu.class);
			break;
		default:
			break;
		}
		return dataSource;
	}
	
	
	
}
