package com.innodealing.engine;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * 单个主体原始数据
 * @author zzl
 *
 */
public class OriginalData {
	
	/**
	 * 原始单个指标数据集合
	 */
	private List<Map<String,Object>> indicatorItems;

	/**
	 * 按财报日期分组后的数据
	 */
	private volatile Map<String,Map<String, Object>> indicatorItemMap;
	
	public OriginalData() {
		super();
	}

	public OriginalData(List<Map<String, Object>> indicatorItems) {
		super();
		this.indicatorItems = indicatorItems;
	}

	/**
	 * 
	 * getIndicatorItems:(按日期重组财报数据，并将key值转换为小写)
	 * @param  @return    设定文件
	 * @return Map<String,Map<String,Object>>    DOM对象
	 * @throws 
	 * @since  CodingExample　Ver 1.1
	 */
	public synchronized Map<String,Map<String, Object>> getIndicatorItems() {
		String dateField = "FIN_DATE";
		if(indicatorItems == null){
			return null;
		}
		if(indicatorItemMap != null){
			return indicatorItemMap;
		}
		//季度化
		Map<String, Map<String, Object>> map = new LinkedHashMap<String,Map<String,Object>>();
		indicatorItems.forEach(item -> {
			Map<String, Object> mapNew = new HashMap<String, Object>();
			item.forEach((k,v) -> {
				//try {
					if(v != null && v instanceof BigDecimal){
						
						mapNew.put(k.toLowerCase(), new BigDecimal(v.toString()));
					}else{
						mapNew.put(k.toLowerCase(), v);
						if(null == v ) {//无法判断类型
						    mapNew.put(k.toLowerCase(), new BigDecimal(0));
						}
					}
				//} catch (NumberFormatException e) {
				//	mapNew.put(k.toLowerCase(), v);
				//}
				
			});
			map.put(item.get(dateField).toString().toLowerCase(), mapNew);
		});
		indicatorItemMap = map;
		return map;
	}

	public void setIndicatorItems(List<Map<String, Object>> indicatorItems) {
		this.indicatorItems = indicatorItems;
	}
	
	
	
	
	@Override
	public String toString() {
		return "OriginalData [indicatorItems=" + indicatorItems + ", indicatorItemMap=" + indicatorItemMap + "]";
	}

	public static void main(String[] args) {
		String regex = "[\\(]\\-*[0-9]+[\\.][0-9]+[\\)]";
		System.out.println("43(-343.45)".matches(regex));
	}
}
