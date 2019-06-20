package com.innodealing.parser;

import java.util.List;

/**
 * 表达式解析接口
 * 
 * @author 赵正来
 *
 */
public interface Parser {
	/**
	 * 目标表达式
	 * 
	 * @param target
	 * @return
	 */
	public Result parse(String target);

	/**
	 * 处理多个remaining and recognized
	 * 
	 * @param target
	 * @param items
	 * @return
	 */
    default Result doItem(String target, List<SEItem> items) {
		// 返回识别的字符串，多个用'?'隔开
		if (items.size() > 0) {
			StringBuffer sb = new StringBuffer();
			StringBuffer remaining = new StringBuffer("");
			int remainingStart = 0;
			for (int i = 0; i < items.size(); i++) {
				SEItem item = items.get(i);
				// 识别的部分
				sb.append(target.substring(item.getStart(), item.getEnd())).append("?");

				// 未识别的部分.?为占位符，表示已识别的部分
				remaining.append(target.substring(remainingStart, item.getStart())).append("?");
				remainingStart = item.getEnd();
			}
			remaining.append(target.substring(remainingStart, target.length()));
			return Result.succeed(sb.toString().substring(0, sb.length() - 1), remaining.toString());
		} else {
			return new ZeroParser().parse(target);
		}
	}
	
	public class SEItem{
		int start;
		int end;
		
		public SEItem(int start, int end) {
			super();
			this.start = start;
			this.end = end;
		}
		public int getStart() {
			return start;
		}
		public void setStart(int start) {
			this.start = start;
		}
		public int getEnd() {
			return end;
		}
		public void setEnd(int end) {
			this.end = end;
		}
		
	}
}
