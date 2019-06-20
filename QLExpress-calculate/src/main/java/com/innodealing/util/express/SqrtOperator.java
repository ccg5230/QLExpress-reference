package com.innodealing.util.express;

import org.apache.commons.math3.analysis.function.Sqrt;

import com.ql.util.express.Operator;

/**
 * express 开平方根函数
 * @author 赵正来
 *
 */
public class SqrtOperator extends Operator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public SqrtOperator() {
		this.name = "sqrt";
	}

	public SqrtOperator(String name){
		this.name = name;
	}
	
	@Override
	public Object executeInner(Object[] arg0) throws Exception {
		if(null == arg0[0]) {
		    return 0;//安硕要求返回0
		} else {
		    Double val = Double.parseDouble(arg0[0].toString());
		    if(val.doubleValue() == 0){
		        return 0;
		    }else{
		        return new Sqrt().value(Double.parseDouble(arg0[0].toString()));
		    }
		}
	}

}
