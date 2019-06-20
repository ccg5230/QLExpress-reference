package com.innodealing.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 解析max函数
 * @author 赵正来
 *
 */
public class MaxParser implements Parser{

	@Override
	public Result parse(String target) {

		//空值处理
		if(target == null){
			return null;
		}
		
		//max函数处理
		if(target.toLowerCase().contains("max(")){
			Stack<Character> stack = new Stack<>();
			List<SEItem> items = new ArrayList<>();
			int start=0,end=0;
			
			char[] ch = target.toCharArray();
			for (int i = 0; i < ch.length; i++) {
				//第一次处理max(
				if('(' == ch[i] && i > 2){
					String max = new Character(ch[i-3]).toString() + new Character(ch[i-2]).toString() + new Character(ch[i-1]).toString();
					if("max".equals(max.toLowerCase())){
						stack.push(ch[i]);
						start = i - 3;
						continue;
					}
				}
				if(stack.size() > 0){
					if("(".equals(new Character(ch[i]).toString())){
						stack.push(ch[i]);
						continue;
					}
					if(")".equals(new Character(ch[i]).toString())){
						stack.pop();
						if(stack.size() == 0){
							end = i + 1;
							stack.clear();
							items.add(new SEItem(start, end));
						}
						continue;
					}
				}
			}
			return doItem(target, items);
		}else{
			return new ZeroParser().parse(target);
		}
	}

	
	public static void main(String[] args) {
		System.out.println(new MaxParser().parse("if pp2=1,(avg(pp2(cf001),pp(cf001),cf001)-STDEV(pp2(cf001),pp(cf001),cf001))/MAX(BS002,1),else,missing value"));
	}
}
