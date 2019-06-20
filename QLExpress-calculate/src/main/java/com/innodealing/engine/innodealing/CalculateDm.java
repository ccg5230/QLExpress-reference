package com.innodealing.engine.innodealing;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.innodealing.engine.Calculate;
import com.innodealing.engine.OriginalData;
/**
 * DM数据计算引擎
 * @author 赵正来
 *
 */
public class CalculateDm extends Calculate {
    
    private static final Logger log = LoggerFactory.getLogger(CalculateDm.class);

	/**
	 * 计算执行器
	 */
	private Execute execute = new Execute();
	
	/**
	 * 当前主体的所有指标表达式集合
	 */
	private Map<String,String> expressions;
	
	
	/**
	 * 当前主体的所有财务指标
	 */
	private OriginalData od;
	
	public CalculateDm() {
	}
	
	public CalculateDm(OriginalData od, Map<String, String> expressions) {
		super();
		this.expressions = expressions;
		this.od = od;
	}

	@Override
	public String parseExpression(String expression) {
		if(expression == null){
			return null;
		}
		return execute.infixToSuffix(expression);
	}

	@Override
	public BigDecimal calculate(Map<String, Object> originalData, String expression) {
		//初始化好的表达式
		String initPostfix = initPostfix(od, originalData, expression, expressions);
		try {
			return execute.suffixToArithmetic(execute.infixToSuffix(initPostfix),expression);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
//		BigDecimal result = null;
//		if(initPostfix != null){
//			try {
//				AviatorEvaluator.setOptimize(4);
//				result = (BigDecimal) AviatorEvaluator.execute(initPostfix);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		try {
//			return result == null ? null : new BigDecimal(result.toString()).setScale(4, BigDecimal.ROUND_HALF_UP);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		}
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
	private String initPostfix(OriginalData od, Map<String,Object> originalData,String expression, Map<String,String> expressions){
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
			Map<String, String> expressions) {
		StringTokenizer  tokenizer   = new StringTokenizer(expression, "+-%/*() ");
		
		Object finDate = originalData.get("fin_date");
		while (tokenizer.hasMoreElements()) {
			String next = null;
			try {
				next = tokenizer.nextElement().toString().replaceAll(" ", "");
				Object val = originalData.get(next);
	            //是否该变量为量化后的指标
	            if(val == null && expressions !=null && expressions.get(next) != null){
	                String subEx = expressions.get(next);
	                val = DateConvertUtil.formatterExpression(subEx, od, expressions, finDate.toString());
	                //val = calculate(originalData, val2);
	                //expression = replaceVariate(od, originalData, expression, expressions);
	            }
	            //是变量还是常数
	            if(!next.matches("[0-9]+[\\.]*[0-9]*")){
	                val = val == null ? "0" : val;
	                    expression = expression.replaceAll(next, val.toString());
	            }
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			
		}
		return expression;
	}
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * 计算执行器
	 * @author zzl
	 *
	 */
	public class Execute {
		/**
		 * 中缀表达式转后缀表达式 只处理了+,-,*,/和括号，没有处理负号及其它运算符，也没对前缀表达式验证。
		 * 如要处理负号，可对表达式进行预转义处理，当下面条件成立时，将负号换成单目运算符"!" infix.charAt[i]=='-'&&(
		 * i==0||infix.charAt[i-1]=='(') 3*6/4+3 3+6-4 3 6 + 4 - 3+(6-4/2)*5 3 6
		 * 4 2 / - 5 * +
		 */
		// 方法：中缀表达式转成后缀表达式
		public String infixToSuffix(String infix) {
			if(infix == null){
				return null;
			}
			infix =  infix.replaceAll(" " , "");
			if(infix.contains("+-") || infix.contains("-+") || infix.contains("--")){
				infix =	infix.replaceAll("\\+\\-", "\\-").replaceAll("\\-\\+", "\\-").replaceAll("\\-\\-", "\\+");
			}
			Stack<Character> stack = new Stack<Character>();
			String suffix = "";
			int length = infix.length();
			for (int i = 0; i < length; i++) {
				Character temp;
				char c = infix.charAt(i);
				switch (c) {
				// 忽略空格
				case ' ':
					break;
				// 碰到'('，push到栈
				case '(':
					stack.push(c);
					break;
				// 碰到'+''-'，将栈中所有运算符弹出，送到输出队列中
				case '+':
				case '-':
					while (stack.size() != 0) {
						temp = stack.pop();
						if (temp == '(') {
							stack.push('(');
							break;
						}
						suffix += " " + temp;
					}
					//负数处理
					if(i ==0 || infix.charAt(i - 1) == '(' || infix.charAt(i - 1) == '/' || infix.charAt(i - 1) == '*'){
						
						suffix += c;
						break;
					}
					stack.push(c);
					suffix += " ";
					break;
				// 碰到'*''/'，将栈中所有乘除运算符弹出，送到输出队列中
				case '*':
				case '/':
					while (stack.size() != 0) {
						temp = stack.pop();
						if (temp == '(' || temp == '+' || temp == '-') {
							stack.push(temp);
							break;
						} else {
							suffix += " " + temp;
						}
					}
					stack.push(c);
					suffix += " ";
					break;
				// 碰到右括号，将靠近栈顶的第一个左括号上面的运算符全部依次弹出，送至输出队列后，再丢弃左括号
				case ')':
					while (stack.size() != 0) {
						temp = stack.pop();
						if (temp == '(')
							break;
						else
							suffix += " " + temp;
					}
					// suffix += " ";
					break;
				// 如果是数字，直接送至输出序列
				default:
					suffix += c;
				}
			}

			// 如果栈不为空，把剩余的运算符依次弹出，送至输出序列。
			while (stack.size() != 0) {
				suffix += " " + stack.pop();
			}
			return suffix;
		}

		/**
		 * postfix
		 *
		 * @return double
		 */
		// 方法：通过后缀表达式求出算术结果
		public BigDecimal suffixToArithmetic(String postfix, String ex) {
			
			if(postfix == null || "".equals(postfix.replaceAll(" ", ""))){
				return null;
			}
			
			Pattern pattern = Pattern.compile("[-]*\\d+||(\\d+\\.\\d+)"); // 使用正则表达式
																		// 匹配数字
			String strings[] = postfix.split(" "); // 将字符串转化为字符串数组
			for (int i = 0; i < strings.length; i++)
				strings[i].trim(); // 去掉字符串首尾的空格
			Stack<BigDecimal> stack = new Stack<BigDecimal>();

			//String regex = "[\\(]\\-*[0-9]+[\\.][0-9]+[\\)]";
			for (int i = 0; i < strings.length; i++) {

				if (strings[i].equals(""))
					continue;

				// 如果是数字，则进栈
				if ((pattern.matcher(strings[i])).matches()) {
					stack.push(new BigDecimal(strings[i]));
				} else {
					// 如果是运算符，弹出运算数，计算结果。
					BigDecimal y = null;
					BigDecimal x = null;
					try {
						y = stack.pop();
						x = stack.pop();
						
					} catch (Exception e) {
						return new BigDecimal(0);
					}
					String opt = null;
					try {
						 opt = strings[i];
						stack.push(caculate(x, y, opt)); // 将运算结果重新压入栈。
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			BigDecimal value = null;
			try {
				value = stack.pop();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return  value;

		}

		private  BigDecimal caculate(BigDecimal x, BigDecimal y, String simble) throws Exception{
			if(x == null){
				x = new BigDecimal(0);
			}
			
			if (simble.trim().equals("+"))
				return x.add(y);
			if (simble.trim().equals("-"))
				return x.subtract(y);
			if (simble.trim().equals("*"))
				return x .multiply(y);
			if (simble.trim().equals("/"))
				try {
					return y.doubleValue() == 0 ? null : x.divide(y, 4, BigDecimal.ROUND_HALF_UP);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			return new BigDecimal(0);
		}
	}
	
	public static void main(String[] args) throws IOException {
		Execute execute = new CalculateDm().new Execute();
		String fix = execute.infixToSuffix("-5 + (4+(-5))*8/(-1)");
		System.out.println(execute.suffixToArithmetic(fix,""));
//		 SimpleEvaluationContext context = new SimpleEvaluationContext();
//		 String line = "78/89";
//		 Expr e = ExprParser.parse(line);
//		 Exprs.toUpperCase(e);
//		System.out.println(Exprs.parseValue(line).);
	}
	
}
