package com.innodealing.engine.innodealing;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.innodealing.engine.Calculate;
import com.innodealing.engine.OriginalData;
import com.innodealing.util.NumberUtils;
import com.innodealing.util.express.AvgOperator;
import com.innodealing.util.express.DMMaxOperator;
import com.innodealing.util.express.SqrtOperator;
import com.innodealing.util.express.StdOperator;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.IExpressContext;

/**
 * 
 * @author 赵正来
 *
 */
public class CalculateAlibaba extends Calculate{
	/**
	 * 当前主体的所有指标表达式集合
	 */
	private Map<String,String> expressions;
	
	
	private static final Logger logger  = LoggerFactory.getLogger(CalculateAlibaba.class);
	
	private static ExpressRunner runner = new ExpressRunner(true,true);
	
	/**
	 * 当前主体的所有财务指标
	 */
	private OriginalData od;
	
	public CalculateAlibaba() {
	}
	
	public CalculateAlibaba(OriginalData od, Map<String, String> expressions) {
		super();
		this.expressions = expressions;
		this.od = od;
	}

	@Override
	public String parseExpression(String expression) {
		if(expression == null){
			return null;
		}
		return null;
	}

	@Override
	public BigDecimal calculate(Map<String, Object> originalData, String expression) throws Exception{
		//空值处理
		if(originalData == null || expression == null) {
		    return null;
		}
	    Object result = execute(originalData, expression);
		return result == null ? null : new BigDecimal(result.toString());
	}

	
	/**
	 * 执行计算
	 * @param originalData
	 * @param expression
	 * @return
	 */
	public Object execute(Map<String, Object> originalData, String expression) throws Exception{
		IExpressContext<String,Object> expressContext = new DefaultContext<String,Object>();
		Object result = null;
		String initPostfix ="";
	    for (Entry<String, Object> entry : originalData.entrySet()) {
	        String k = entry.getKey();
	        Object v = entry.getValue();
            if(null == v || StringUtils.isEmpty(v.toString())) {
                v = new BigDecimal(0);  
            } else {
                if(v instanceof Number) {
                    String value = String.valueOf(v).trim();
                    if(value.startsWith("+")) {
                        value = value.replace("+", "");
                    }
                    if(value.startsWith("-")) {
                        value = value.replaceAll(" ", "");
                    }
                    v = new BigDecimal(value); 
                } else {
                    continue;//非数值类型不参与计算，防止转换出错
                }
            }
	        expressContext.put(k, v == null ? 0 : v);
	     }

		//初始化好的表达式
		initPostfix = initPostfix(od, originalData, expression, expressions);
		if(initPostfix.contains("std") &&  runner.getFunciton("std") == null){
			runner.addFunction("std", new StdOperator());
		}
		if(initPostfix.contains("sqrt") &&  runner.getFunciton("sqrt") == null){
			runner.addFunction("sqrt", new SqrtOperator());
		}
		if(initPostfix.contains("avg") &&  runner.getFunciton("avg") == null){
            runner.addFunction("avg", new AvgOperator());
        }
		if(initPostfix.contains("dmmax") &&  runner.getFunciton("dmmax") == null){
            runner.addFunction("dmmax", new DMMaxOperator());
        }
	
		initPostfix = initPostfix.replaceAll("\\+\\-", "-")
				.replaceAll("\\-\\-", "+")
				.replaceAll("\\(\\-\\-", "(")
				.replaceAll("\\(\\+\\-", "(-")
				.replaceAll("\\(\\-\\+", "(-")
				.replaceAll("\\(\\+", "(");
		result = runner.execute(initPostfix, expressContext, null, false,false);
		return result == null ? null : new BigDecimal(result.toString()).setScale(6, BigDecimal.ROUND_HALF_UP);
	}

    /**
	 * 是否有空值
	 * @param originalData
	 * @param expression
	 * @return
	 */
	private boolean isContainNull(Map<String, Object> originalData, String expression) {
		StringTokenizer  tokenizer   = new StringTokenizer(expression, ",+-%/*(){}><!= ");
		while (tokenizer.hasMoreElements()) {
			String  next = tokenizer.nextElement().toString().replaceAll(" ", "");
			Object val = originalData.get(next);
			if(val == null && !NumberUtils.isNumeric(next)){
				return true;
			}
		}
		return false;
	}
	
	

	/**
	 * 将计算表达式具体化
	 * @param od 原始财报数据
	 * @param originalData 当前季度原始财报数据
	 * @param expression 当前表达式
	 * @param expressions 单个主体的所有量化指标 表达式
	 * @param finDate 当前季度时间
	 * @return
	 */
	private String initPostfix(OriginalData od, Map<String,Object> originalData,String expression, Map<String,String> expressions)
	        throws Exception{
		if(originalData == null || expression == null){
			return null;
		}
		//将原始数据替换字符变量
		expression = replaceVariate(od, originalData, expression, expressions);
		if("null".equals(expression)){
			return null;
		}
		if(expression.replaceAll("\\.", "").matches("[0]+")){
			expression = "0.0";
		}
		return expression;
	}

	
	/**
	 * 将原始表达式变量替换具体数值具体化
	 * @param od
	 * @param originalData
	 * @param expression
	 * @param expressions
	 * @return
	 */
	private String replaceVariate(OriginalData od,Map<String, Object> originalData, String expression,
			Map<String, String> expressions) throws Exception {
		StringTokenizer  tokenizer   = new StringTokenizer(expression, ",+-%/*(){}><!= ");
		//财报日期
		Object finDate = originalData.get("fin_date");
		while (tokenizer.hasMoreElements()) {
			String  next = tokenizer.nextElement().toString().replaceAll(" ", "");
			Object val = originalData.get(next);
			//是否该变量为量化后的指标
			if(val == null && expressions !=null && expressions.get(next) != null){
				String subEx = expressions.get(next);
				val = DateConvertUtil.formatterExpressionByTaobao(subEx, od, expressions, finDate.toString());
				//originalData.put(next, val== null ? "0" : val.toString());
				expression = expression.replaceAll(next, val== null ? "0" : val.toString());
			}
			//数据库中没有的字段初始化值为0
			if(expressions.get(next) == null && val == null){
				originalData.put(next, 0);
			}
		}
		return expression;
	}
	
	public static void main(String[] args) throws IOException {
		Map<String,Object> map  = new HashMap<>();
		map.put("index", 1);
		mapTest(map);
		System.out.println(map);
		//String express = "max(-(-3459665.44+-3459665.44+cf002),1)";
		String express = "3459665.44--3459665.44+cf002";
		IExpressContext<String,Object> expressContext = new DefaultContext<String,Object>();
		expressContext.put("pp", 1);
		expressContext.put("pp2", 1);
		expressContext.put("salegrwth", 1000);
		expressContext.put("cf002", 1000);
		ExpressRunner runner = new ExpressRunner(true,true);
		try {
			express = express.replaceAll("\\-\\-", "\\+");
			System.out.println(express);
			System.out.println(runner.execute(express, expressContext, null, false, true));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    
	public static void mapTest(Map<String,Object> map){
		map.put("name", "李达康");
	}
	
}
