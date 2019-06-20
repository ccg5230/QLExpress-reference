package com.innodealing.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StringUtils {
	/**
	 * 是否为空
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isBlank(String str){
		if(!StringUtils.isNull(str) && !"".equals(str.trim())){
			return false;
		}
		return true;
	}

	/**
	 * 是否为null
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNull(String str){
		if(str == null){
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * printStackTraceToString:(获取异常堆栈消息字符串)
	 * @param  @param t
	 * @param  @return    设定文件
	 * @return String    DOM对象
	 * @throws 
	 * @since  CodingExample　Ver 1.1
	 */
	public static String printStackTraceToString(Throwable t) {
	    StringWriter sw = new StringWriter();
	    t.printStackTrace(new PrintWriter(sw, true));
	    return sw.getBuffer().toString();
	}
}
