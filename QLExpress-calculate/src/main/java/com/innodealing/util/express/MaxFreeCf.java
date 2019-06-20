package com.innodealing.util.express;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ql.util.express.Operator;

/**
 * express 标准差自定义函数
 * 
 * @author 赵正来
 *
 */
public class MaxFreeCf extends Operator {
	
	Logger log = LoggerFactory.getLogger(MaxFreeCf.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	
	public MaxFreeCf() {
		this.name = "MaxFreeCf";
	}

	public MaxFreeCf(String name){
		this.name = name;
	}

	@Override
	public Object executeInner(Object[] list) throws Exception {
		
		for (Object object : list) {
			
		}
		
		Double val = Double.parseDouble(list[0].toString());
		if(val.intValue() == 0){
			return 0;
		}else{
			return null;
		}
	}

	public static void main(String[] args) {

		
		System.out.println(Double.parseDouble("--9"));
	}
}
