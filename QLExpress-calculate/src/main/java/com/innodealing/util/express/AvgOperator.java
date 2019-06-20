package com.innodealing.util.express;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ql.util.express.Operator;

/**
 * express 平均自定义函数(与一般的不同，特定处理安硕表达式)
 * 
 * @author 赵正来
 *
 */
public class AvgOperator extends Operator {
	
	Logger log = LoggerFactory.getLogger(AvgOperator.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	
	public AvgOperator() {
		this.name = "avg";
	}

	public AvgOperator(String name){
		this.name = name;
	}

	@Override
	public Object executeInner(Object[] list) throws Exception {
		List<Double> listData = new ArrayList<>();
		for (int i = 0; i < list.length; i++) {
		    if(null!= list[i]) {
    			Double val = Double.parseDouble(list[i].toString());
    			if(val.doubleValue() != 0){
    				listData.add(val);
    			}
		    }
		}
		Object r = 0;
		if(listData.size() > 0) {
    		BigDecimal divisor  = new BigDecimal(0);
    		for (int i = 0; i < listData.size(); i++) {
    			divisor = divisor.add(new BigDecimal(listData.get(i)));
    		}
    		r = divisor.divide(new  BigDecimal(listData.size()),16, BigDecimal.ROUND_HALF_UP);//防止除不尽
		} else {
		    throw new Exception("avg无法计算：参与计算财报数据全部为0");
		}
		return r;
	}

	public static void main(String[] args) {

		
		System.out.println(Character.isDigit('1'));
	}
}
