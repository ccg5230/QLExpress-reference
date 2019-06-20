package com.innodealing.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class NumberUtils {
    
    /**
     * 讲结果保留两位有效小数
     * @param field
     * @return
     */
    public static BigDecimal KeepTwoDecimal(BigDecimal field , int percent){
        if(field == null){
            return null;
        }else{
            BigDecimal c = field;
            //是百分比的需要乘以100
            if(percent == 1){
                c = c.multiply(new BigDecimal(100));
            }
            //保留两位有效小数为0的话则取原值
            if(c.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() != 0){
                c = c.setScale(2, BigDecimal.ROUND_HALF_UP);
            }
            return c;
        }
    }
    
    /**
	 * 判断字符串是否为数字
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str){  
		if(str == null || "".equals(str.replaceAll(" ", ""))){
			return false;
		}
	    Pattern pattern = Pattern.compile("(-)?[0-9]*(.)?[0-9]*");  
	    return pattern.matcher(str).matches();     
	}  
	
	   /**
     * 
     * @名称 comparison
     * @描述 两个数字进行比较
     * @作者 XieZhenGuo
     * @时间 Apr 1, 2011 10:20:10 AM
     * @param v1
     * @param v2
     * v1>v2 -> 1
     * @return 0：相等；1：大于；-1：小于
     */
    public static int comparison(String v1, String v2) {
        int rflag = 0;
        if ("".equals(v1) || null == v1) {
            v1 = "0";
        }
        if ("".equals(v2) || null == v2) {
            v2 = "0";
        }
        BigDecimal a = new BigDecimal(v1.replaceAll(",", ""));
        BigDecimal b = new BigDecimal(v2.replaceAll(",", ""));
        rflag = a.compareTo(b);
        return rflag;
    }
}
