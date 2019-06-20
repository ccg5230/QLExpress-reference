package com.innodealing.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 解析std函数
 * @author 赵正来
 *
 */
public class StdParser implements Parser{

	@Override
	public Result parse(String target) {

		//空值处理
		if(target == null){
			return null;
		}
		
		//std函数处理
		String targetLower = target.toLowerCase();
		if(targetLower.contains("std(") || targetLower.contains("std2(") || targetLower.contains("std3(")){
			Stack<Character> stack = new Stack<>();
			List<SEItem> items = new ArrayList<>();
			int start=0,end=0;
			
			char[] ch = target.toCharArray();
			for (int i = 0; i < ch.length; i++) {
				//第一次处理max(
				if('(' == ch[i] && i > 2){
					String max = new Character(ch[i-3]).toString() + new Character(ch[i-2]).toString() + new Character(ch[i-1]).toString();
					if("std".equals(max.toLowerCase())){
						stack.push(ch[i]);
						start = i - 3;
						continue;
					}
					if(max.toLowerCase().matches("td[1-9]")){
						stack.push(ch[i]);
						start = i - 4;
						continue;
					}
				}
				if(stack.size() > 0){
					if('(' == ch[i]){
						stack.push(ch[i]);
						continue;
					}
					if(')'== ch[i]){
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
		System.out.println("td3".matches("[t][d][1-9]"));
		System.out.println(new StdParser().parse("std3(Tot_Asst_Turnvr)/avg3(Tot_Asst_Turnvr)"));
	}
}
