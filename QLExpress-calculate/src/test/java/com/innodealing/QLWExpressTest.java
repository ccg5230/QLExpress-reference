package com.innodealing;

import org.junit.Test;

import com.innodealing.util.express.IndicatorConvertOperator;
import com.innodealing.util.express.MaxFreeCf;
import com.innodealing.util.express.SqrtOperator;
import com.innodealing.util.express.StdOperator;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.IExpressContext;

public class QLWExpressTest {

	@Test
	public void mathTest() {
		System.out.println(Math.sqrt(4));
		System.out.println(Math.sqrt(2));
	}

	@Test
	public void stdTest() {
		ExpressRunner runner = new ExpressRunner();
		runner.addFunction("std", new StdOperator("std"));

		IExpressContext<String, Object> context = new DefaultContext<>();
		context.put("a", 1);
		context.put("b", 2);
		context.put("c", 3);
		String t = "/((0.4689238307+0.4356935824+0.4396449065)/3)";
		String expressString = "std(0.4689238307,0.4396449065,0.4356935824)";
		try {
			Object result = runner.execute(expressString, context, null, true, true);
			System.out.println(result.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void sqrtTest() {
		ExpressRunner runner = new ExpressRunner();
		runner.addFunction("sqrt", new SqrtOperator("sqrt"));

		IExpressContext<String, Object> context = new DefaultContext<>();
		context.put("a", 1);
		context.put("b", 2);
		context.put("c", 3);
		String expressString = "sqrt(a+b+c)";
		try {
			Object result = runner.execute(expressString, context, null, true, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void indicatorConvertTest() {
		ExpressRunner runner = new ExpressRunner();
		runner.addFunction("indicatorConvert", new IndicatorConvertOperator("indicatorConvert"));

		System.out.println(12);
//		IExpressContext<String, Object> context = new DefaultContext<>();
//		context.put("a", "人数");
//		context.put("b", "语文成绩");
//		context.put("c", "数学成绩");
//		context.put("pp", 1);
//		String expressString = "if(pp == 1){return a+b;}else{return c}";
//		try {
//			Object result = runner.execute(expressString, context, null, true, true);
//			System.out.println("IndicatorConvertTest->" + result);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	
	@Test
	public void MaxFreeCfTest() {
		ExpressRunner runner = new ExpressRunner();
		runner.addFunction("MaxFreeCf", new MaxFreeCf("MaxFreeCf"));

		IExpressContext<String, Object> context = new DefaultContext<>();
		context.put("a", 1);
		context.put("b", 2);
		context.put("c", 3);
		String expressString = "MaxFreeCf(0,(a+b+c)/0)";
		try {
			Object result = runner.execute(expressString, context, null, true, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
