package com.innodealing.util.express;

import com.ql.util.express.Operator;

/**
 * express 指标代码转换指标名称
 * @author 赵正来
 *
 */
public class IndicatorConvertOperator extends Operator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public IndicatorConvertOperator() {
		this.name = "indicatorConvert";
	}

	public IndicatorConvertOperator(String name){
		this.name = name;
	}
	
	@Override
	public Object executeInner(Object[] arg0) throws Exception {
		String[] data = super.getOperDataDesc();
		for (String string : data) {
			System.out.println(string);
		}
		return null;
		//return new Sqrt().value(Double.parseDouble(arg0[0].toString()));
	}

}
