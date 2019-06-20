package com.innodealing.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 解析avg函数
 * @author 赵正来
 *
 */
public class AvgParser implements Parser{

	@Override
	public Result parse(String target) {

		//空值处理
		if(target == null){
			return null;
		}
		
		//avg函数处理
		String targetLower = target.toLowerCase();
		if(targetLower.contains("avg(") || targetLower.contains("avg2(") || targetLower.contains("avg3(")){
			Stack<Character> stack = new Stack<>();
			List<SEItem> items = new ArrayList<>();
			int start=0,end=0;
			
			char[] ch = target.toCharArray();
			for (int i = 0; i < ch.length; i++) {
				//第一次处理max(
				if('(' == ch[i] && i > 2){
					String max = new Character(ch[i-3]).toString() + new Character(ch[i-2]).toString() + new Character(ch[i-1]).toString();
					if("avg".equals(max.toLowerCase())){
						stack.push(ch[i]);
						start = i - 3;
						continue;
					}
					if(max.toLowerCase().matches("vg[1-9]")){
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
		System.out.println(new AvgParser().parse("if pp2=1,(avg2(pp2(cf001),pp(cf001),cf001)-STDEV(pp2(cf001),pp(cf001),cf001))/MAX(BS002,1),else,missing value"));
	}
}
