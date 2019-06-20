package com.innodealing.util.express;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ql.util.express.Operator;

/**
 * express 标准差自定义函数（样例）
 * 
 * @author 赵正来
 *
 */
public class StdOperator extends Operator {
	
	Logger log = LoggerFactory.getLogger(StdOperator.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	
	public StdOperator() {
		this.name = "std";
	}

	public StdOperator(String name){
		this.name = name;
	}

	@Override
	public Object executeInner(Object[] list) throws Exception {
		List<Double> listData = new ArrayList<>();
		for (int i = 0; i < list.length; i++) {
		    Object obj = list[i];
		    if(null != obj)  {
		        Double val = Double.parseDouble(list[i].toString());
	            if(val.doubleValue() != 0){
	                listData.add(val);
	            }
		    }
		}
		if(listData.size()==0) {
		    throw new Exception("std无法计算：参与计算财报数据全部为0"); 
		}
		double[] data = new double[listData.size()];
		for (int i = 0; i < data.length; i++) {
			data[i] = listData.get(i);
		}
		return  new StandardDeviation(true).evaluate(data);
	}

	public static void main(String[] args) throws Exception {

	    StdOperator s = new StdOperator();
	       String[] a ={"1","2"};    
		System.out.println(s.executeInner(a).toString());
	}
}
