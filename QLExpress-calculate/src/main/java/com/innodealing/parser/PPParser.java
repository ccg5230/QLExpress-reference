package com.innodealing.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.innodealing.engine.OriginalData;

/**
 * 上一期解析类将pp(表达式)提取出来
 * @author 赵正来
 *
 */
public class PPParser implements Parser {
	
	/**
	 * 单个主体原始数据
	 */
	private OriginalData originalData;
	
	

	public OriginalData getOriginalData() {
		return originalData;
	}


	public PPParser() {
		super();
	}


	public PPParser(OriginalData originalData) {
		super();
		this.originalData = originalData;
	}



	@Override
	public Result parse(String target) {
		// if(target.contains("pp(") || target.contains("PP(")){

		if(target == null){
			return null;
		}
		
		char[] c = target.toCharArray();

		// 声明一个栈
		Stack<Character> stack = new Stack<>();
		List<SEItem> items = new ArrayList<>();
		int start = 0;
		int end = 0;
		for (int i = 0; i < c.length; i++) {
			// 判断pp的开始 or pp2
			Character ch = c[i];
			if ('p' == c[i]) {
				if(stack.size() == 0 && (i + 2) < c.length){
					Character c1 = c[i + 1];
					Character c2 = c[i + 2];
					String next = c1.toString() + c2.toString();
					if("p(".equals(next.toLowerCase())
							|| next.toLowerCase().matches("p[1-9]")){
						stack.push(c[i]);
						continue;
					}
					
				}
				//如果后面为p(
				if(stack.size() == 1 && (i + 2) < c.length){
					Character c0 = c[i];
					Character c1 = c[i + 1];
					Character c2 = c[i + 2];
					String next = c0.toString() + c1.toString() ;
					if("p(".equals(next.toLowerCase())
							||((next + c2.toString()).toLowerCase().matches("p[1-9]\\("))){
						stack.push(c[i]);
						continue;
					}
				}
			}
			
			//后面为[1-9](
			if(stack.size() == 2 && (i + 1) < c.length){
				Character c0 = c[i];
				Character c1 = c[i + 1];
				String next = c0.toString() + c1.toString();
				if(next.toLowerCase().matches("[1-9]\\(")){
					stack.push(c[i]);
					continue;
				}
			}
			
			// 判断pp后是否是(
			if (stack.size() == 2 && ('(' == c[i])) {
				start = i - 2;
				stack.push(c[i]);
				continue;
			}
			// 判断pp2后是否是(
			if (stack.size() == 3 && ('(' == c[i])) {
				start = i - 3;
				stack.push(c[i]);
				continue;
			}
			
			// 判断pp后是否是)
			if (stack.size() > 2 && ')' == c[i]) {
				stack.pop();
				if (stack.size() == 2 || stack.size() == 3) {
					end = i + 1;
					items.add(new SEItem(start, end));
					stack.clear();
					continue;
				}
			}
		}
		
		return doItem(target, items);
	}

	public static void main(String[] args) {
		String exprress =/* "pp2(pl220)";**/"if (pp2==1){((pp2(pl220)+pp(pl220)+pl220)/3)/max(pl301_1,1)}else{null}";
		Result result  = new PPParser().parse(exprress);
		System.out.println(result);
	}

}
