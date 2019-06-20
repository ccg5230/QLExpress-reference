package com.innodealing.engine.innodealing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.innodealing.engine.Calculate;
import com.innodealing.engine.OriginalData;
import com.innodealing.parser.AvgParser;
import com.innodealing.parser.MaxParser;
import com.innodealing.parser.PPParser;
import com.innodealing.parser.Parser;
import com.innodealing.parser.Result;
import com.innodealing.parser.StdParser;
import com.innodealing.util.DateUtils;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.IExpressContext;

/**
 * 数据转换工具
 * @author zzl
 *
 */
public class DateConvertUtil {
	
	public static final Logger logger = LoggerFactory.getLogger(DateConvertUtil.class);

	public static String formatter(OriginalData originalData, String ex, String finDate) {
		if(ex.contains("pp")){
			return null;
		}
		//if()
		return ex;
	}
	
	
	
	public static BigDecimal formatterExpression(String expression, OriginalData od, Map<String,String> expressions, String finDate) throws Exception{
		
		List<Parser> parsers = new ArrayList<>();
		parsers.add(new PPParser());
		parsers.add(new MaxParser());
		parsers.add(new AvgParser());
		parsers.add(new StdParser());
		
		//计算器
		Calculate calculate = getCalculate(od, expressions);
		//do if
		if(expression != null && expression.contains("if")){
			expression = doIf(od, expressions, finDate, expression, calculate);
		}
		//解析表达式
		for (Parser parser : parsers) {
			expression = doParser(expression, od, expressions, finDate, parser,calculate);
		}
		expression = doIf(od, expressions, finDate, expression, calculate);
		
		//最终处理的表达式
		return calculate(expression, od.getIndicatorItems().get(finDate), calculate);
	}



	/**
	 * 获取计算器
	 * @param od
	 * @param expressions
	 * @return
	 */
	private static Calculate getCalculate(OriginalData od, Map<String, String> expressions) {
		//return  new CalculateDm(od, expressions);
		return  new CalculateAlibaba(od, expressions);
	}
	
	
	public static BigDecimal formatterExpressionByTaobao(String expression, OriginalData od, Map<String,String> expressions, String finDate)
	        throws Exception{
		
		Calculate calculate = getCalculate(od, expressions);
		//解析表达式
		expression = doParser(expression, od, expressions, finDate, new PPParser(),calculate);
		//最终处理的表达式
		Map<String,Object> data = od.getIndicatorItems().get(finDate);
		return calculate(expression, data, calculate);
	}
	


	public Object calculate(String expression,OriginalData od, Map<String,String> expressions, String finDate){
		IExpressContext<String,Object> expressContext = new DefaultContext<String,Object>();
		expressContext.put("a", 2);
		expressContext.put("b", 3);
		expressContext.put("pp",2);
		ExpressRunner runner = new ExpressRunner(true,true);
		Object result = null;
		try {
			result = runner.execute(expression,expressContext, null, false,true);
		} catch (Exception e) {
		    logger.error("calculate error "+e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 解析
	 * @param expression
	 * @param od
	 * @param expressions
	 * @param finDate
	 * @param parser
	 * @return
	 */
	private static String doParser(String expression, OriginalData od, Map<String, String> expressions, String finDate,
			Parser parser, Calculate calculate) throws Exception{
		Result result = parser.parse(expression);
		if(result == null){
			return null;
		}
		String recognized = result.getRecognized(); 
		String remaining = result.getRemaining();
		if(recognized == null || "".equals(recognized.replaceAll(" ", ""))){
			expression = remaining;
		}else{
			//匹配的
			StringTokenizer remainingTokenizer = new StringTokenizer(remaining, "?");
			//没有匹配的
			StringTokenizer recognizedTokenizer = new StringTokenizer(recognized, "?");
			StringBuffer newExpression = new StringBuffer("");
			List<BigDecimal> values = new ArrayList<>();
			while (recognizedTokenizer.hasMoreElements()) {
				BigDecimal value = new BigDecimal(0);
				String subEx = recognizedTokenizer.nextToken();
				if(parser instanceof PPParser){
					value = doPP(od, subEx, calculate, finDate);
				}
				if(parser instanceof MaxParser){
					value = doMax(od, subEx, calculate, finDate);
				}
				values.add(value);
			}
			int index = 0;
			while (remainingTokenizer.hasMoreElements() ) {
				newExpression.append(remainingTokenizer.nextToken());
				if(values.size() > index){
					newExpression.append(values.get(index));
				}
				index ++;
			}
			expression = newExpression.toString();
		}
		return expression;
	}
	
	/**
	 * 计算
	 * @param expression
	 * @param data
	 * @param calculate
	 * @return
	 */
	private static BigDecimal  calculate(String expression,Map<String,Object> data, Calculate calculate) throws Exception {
		if(expression == null || "".equals(expression)){
			return null;
		}
		BigDecimal result = calculate.calculate(data, expression);
		return result;
	}


	/**
	 * do pp
	 * @param od
	 * @param expression
	 * @param calculate
	 * @param finDate
	 * @return
	 */
	private static BigDecimal doPP(OriginalData od, String expression, Calculate calculate, String finDate) 
	        throws Exception{
		//当前季度数据
		Map<String,Object> currentDatas = od.getIndicatorItems().get(finDate);
		//上年同期财报数据
		String ppFinDate = DateUtils.prevYear(finDate, 1);
		Map<String,Object> ppDatas = od.getIndicatorItems().get(ppFinDate);
		//上2年同期财报数据
		String pp2FinDate = DateUtils.prevYear(finDate, 2);
		Map<String,Object> pp2Datas = od.getIndicatorItems().get(pp2FinDate);
		//上3年同期财报数据
		String pp3FinDate = DateUtils.prevYear(finDate, 3);
		Map<String,Object> pp3Datas = od.getIndicatorItems().get(pp3FinDate);
		BigDecimal result = null;
		//PP函数处理结果null要替换为0
		if(expression.contains("pp3(")){
			expression = expression.replaceAll("pp3", "");
			result = calculate.calculate(pp3Datas, expression);
			result = result==null? new BigDecimal(0) : result;
		}else if(expression.contains("pp2(")){
			expression = expression.replaceAll("pp2", "");
			result = calculate.calculate(pp2Datas, expression);
			result = result==null? new BigDecimal(0) : result;
		}else if(expression.contains("pp(")){
			expression = expression.replaceAll("pp", "");
			result = calculate.calculate(ppDatas, expression);
			result = result==null? new BigDecimal(0) : result;
		}else{
			result = calculate.calculate(currentDatas, expression);
		}
		//if(pp >0) 等处理,不能删除，安硕有根据pp来选择计算表达式的
		if(ppDatas != null){
			currentDatas.put("pp", 1);
		}else{
			currentDatas.put("pp", 0);
		}
		if(ppDatas != null && pp2Datas != null){
			currentDatas.put("pp2", 1);
		}else{
			currentDatas.put("pp2", 0);
		}
		
		if(ppDatas != null && pp2Datas != null && pp3Datas!= null){
			currentDatas.put("pp3", 1);
		}else{
			currentDatas.put("pp3", 0);
		}
		return result;
	}

	/**
	 *  max 处理
	 * @param od
	 * @param expression
	 * @param calculate
	 * @param finDate
	 * @return
	 */
	private static BigDecimal doMax(OriginalData od, String expression, Calculate calculate, String finDate) throws Exception{
		Map<String,Object> currentDatas = od.getIndicatorItems().get(finDate);
		if(expression != null && expression.contains("max(")){
			
			//Math.max(a, b);
			String[] recArr =  expression.split(",");
			String front = recArr[0].substring(4);
			String back = null;
			try {
				back = recArr[1].substring(0, recArr[1].length() -1);
			} catch (Exception e) {
				e.printStackTrace();
			}
			BigDecimal frontV =  front.contains("null") ? null : calculate.calculate(currentDatas,front);
			
			BigDecimal backV =  back.contains("null") ? null : calculate.calculate(currentDatas,back);
			
			if(frontV == null){
				return backV == null ? null : backV;
			}else{
				if(backV == null){
					return frontV;
				}else{
					Double  val = Math.max(frontV.doubleValue(), backV.doubleValue());
					return  new BigDecimal(val);
				}
			}
		}else{
			BigDecimal val = calculate.calculate(currentDatas, expression);
			return val;
		}
	}


	/**
	 * if 语句处理
	 * @param od
	 * @param expressions
	 * @param finDate
	 * @param expression
	 * @return
	 */
	private static String doIf(OriginalData od, Map<String, String> expressions, String finDate,String expression, Calculate calculate) 
	        throws Exception{
		//当前季度数据
		Map<String,Object> currentDatas = od.getIndicatorItems().get(finDate);
		//上年同期财报数据
		String ppFinDate = DateUtils.prevYear(finDate, 1);
		Map<String,Object> ppDatas = od.getIndicatorItems().get(ppFinDate);
		//上2年同期财报数据
		String pp2FinDate = DateUtils.prevYear(finDate, 2);
		Map<String,Object> pp2Datas = od.getIndicatorItems().get(pp2FinDate);
		//上3年同期财报数据
		/*String pp3FinDate = DateUtils.prevYear(finDate, 3);
		Map<String,Object> pp3Datas = od.getIndicatorItems().get(pp3FinDate);*/
		if(expression == null) return null;
		if(expression.contains("if")){
			//处理复杂变量
			for (Entry<String, String> entry : expressions.entrySet()) {
				String k = entry.getKey();
				if(expression.replaceAll(" ", "").contains(k+">")){
					String e = expressions.get(k);
					if(e != null){
						expression.replaceAll(k, "(" + e + ")");
					}
				}
			}
			//处理if条件
			String[] sentences = expression.split(",|;");
			if(sentences.length != 4){
				logger.error("表达式有误[" + expression + "]");
				return null;
			}
			//1.为等于条件(pp=1 || pp2=1 || (pp =1 and pp(x) > 0))
			String sentences0 = sentences[0].toLowerCase().replaceAll(" ", "");
			if(sentences0.contains("pp=")&& !sentences0.contains("and")){
				expression = ppDatas != null ? sentences[1] : null;
			}
			if(sentences0.contains("pp=")&& sentences0.contains("and") && ppDatas != null){
				String ifSent = sentences[0].split("and")[1];
				StringTokenizer stk  = new StringTokenizer(ifSent, "><=");
				List<String> stkArr = new ArrayList<>();
				while (stk.hasMoreElements()) {
					stkArr.add((String) stk.nextElement());
				}
				if(stkArr.size() != 2){
					logger.error("表达式有误[" + expression + "]");
					return null;
				}
				String exp = stkArr.get(0);
				exp = expressions.get(exp.replaceAll(" ", "")) == null ? exp : expressions.get(exp);
				BigDecimal val =calculate(exp, currentDatas, calculate);
				//这里只考虑大于的时候
				try {
					if(val != null && val.doubleValue() > Double.parseDouble(stkArr.get(1))){
						expression = sentences[1];
					}else{
						expression = null;
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(sentences0.contains("pp2=")){
				expression = pp2Datas != null ? expression = sentences[1] : null;
			}
			
			//2.只有大于条件
			if(sentences0.contains(">")&& !sentences0.contains("and")){
				
				String[] sentArr = sentences[0].split(">");
				if(sentArr.length != 2){
					logger.error("表达式有误[" + expressions + "]");
					return null;
				}
				String exp = sentArr[0].replaceAll("if", "").replaceAll("IF", "");
				String subEx = expressions.get(exp.replaceAll(" ", ""));
				exp = subEx == null ? exp : subEx;
				BigDecimal val = calculate(exp, currentDatas, calculate);
				if(val != null && val.doubleValue() > Double.parseDouble(sentArr[1])){
					expression = sentences[1];
				}else{
					expression = null;
				}
			}
			//3.只有小于条件
			if(sentences0.contains("<")&& !sentences0.contains("and")){
				String[] sentArr = sentences[0].split("<");
				if(sentArr.length != 2){
					logger.error("表达式有误[" + expressions + "]");
					return null;
				}
				String exp = sentArr[1].replaceAll("if", "").replaceAll("IF", "");
				String subEx = expressions.get(exp);
				exp = subEx == null ? exp : subEx;
				BigDecimal val = calculate(exp, currentDatas, calculate);
				if(val != null && val.doubleValue() < Double.parseDouble(sentArr[1])){
					expression = sentences[1];
				}else{
					expression = null;
				}
			}
		}
		return expression;
	}
	
	
	
	public static String filterExpression(OriginalData od, Map<String, String> expressions, String finDate,String expression){
		//if处理
		List<Parser> parsers = new ArrayList<>();
		parsers.add(new PPParser());
		parsers.add(new MaxParser());
		
		return expression;
	}
	
	
	
	
	public static void main(String[] args) {
		String ex = "(BTN147+BBS101+BBS102+BBS104+BBS105+BBS106+BBS107+BBS110+BBS111+BBS112+BBS118)/PP(BTN147+BBS101+BBS102+BBS104+BBS105+BBS106+BBS107+BBS110+BBS111+BBS112+BBS118)-1";
	
		String ex2 = "B+pp(BPL101_1-BPL101_2)-1" + "+pp2(BPL-BP)";
		Result result = new PPParser().parse(ex2);
		System.out.println(result);
		String recognized = result.getRecognized(); 
		String[] items = recognized.split("\\?");
		for (String item : items) {
			char[] c = item.toCharArray();
			
		}
		
		System.out.println("p0(".matches("p[1-9]\\("));
		
		String c = "2015-03-31";
		
		String y = c.substring(0, 4);
		
		Integer year = Integer.valueOf(y);
		
		System.out.println(year -1);
		
		//System.out.println(45%*3);
	}
	
}





