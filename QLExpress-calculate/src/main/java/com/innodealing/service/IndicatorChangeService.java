package com.innodealing.service;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Objects;
import com.innodealing.dao.BondComExtDao;
import com.innodealing.json.FinSpclindicatorJson;
import com.innodealing.util.DateUtils;
import com.innodealing.vo.BondComExtVo;
import com.innodealing.vo.IndicatorChangeVo;
/**
 * 指标变动计算
 * @author 赵正来
 *
 */
@Service
public class IndicatorChangeService {
	
	
	private @Autowired FinanceIndicatorService financeIndicatorService;
	
	private  @Autowired BondComExtDao bondComExtDao;
	
	/**
	 * 获取需要提醒的变动指标，不包含newestDataInDB，newestDataRankInsert，newestDataRankInDb
	 * @param comUniCode
	 * @param finDate
	 * @param fields
	 * @return
	 */
	public IndicatorChangeVo loadData(Long comUniCode, Date finDate, Collection<String> fields){
		IndicatorChangeVo data = new IndicatorChangeVo();
		//获取数据(同期)
		Map<String, Object> yoy = financeIndicatorService.findFinanceIndicators(comUniCode, DateUtils.prevYear(finDate, 1));
		data.setYoyData(yoy);
		//数据库最新的
		Map<String, Object> newestDataInserDb = financeIndicatorService.findFinanceIndicators(comUniCode, finDate);
		data.setNewestDataInserDb(newestDataInserDb);
		return data;
	}
	
	public FinSpclindicatorJson checkChange(IndicatorChangeVo indicatorChangeVo){
		//同比check
		Map<String,Object> yoy = checkYoYAndSelfAndRank(indicatorChangeVo.getNewestDataInserDb(), indicatorChangeVo.getYoyData());
		//自身比较
		Map<String,Object> self = checkYoYAndSelfAndRank(indicatorChangeVo.getNewestDataInserDb(), indicatorChangeVo.getNewestDataInDB());
		//行业排名
		Map<String,Object> rank = checkYoYAndSelfAndRank(indicatorChangeVo.getNewestDataRankInsert(), indicatorChangeVo.getNewestDataRankInDb());
		FinSpclindicatorJson json = new FinSpclindicatorJson();
		json.setRANK(rank);
		json.setSELF(self);
		json.setYOY(yoy);
		return json;
	}
	
	/**
	 * 检查出同期和自身变化的指标
	 * @param data1 基础数据
	 * @param data2 和data1比较的数据
	 * @return
	 */
	public Map<String, Object> checkYoYAndSelfAndRank(Map<String,Object> data1, Map<String,Object> data2){
		//空值判断
		if(data1 == null || data2 == null){
			return null;
		}
		Map<String,Object> result = new HashMap<>();
		//每个指标进行比较
		data1.forEach((field, value) -> {
			if(!Objects.equal(value, data2.get(field))){
				result.put(field, value);
			}
		});
		return result;
	}
	

	/**
	 * 检查出同期和自身变化的指标
	 * @param data1 基础数据
	 * @param data2 和data1比较的数据
	 * @return
	 */
	public Map<String, Object> checkRank(Map<String,Object> data1, Map<String,Object> data2){
		//空值判断
		if(data1 == null || data2 == null){
			return null;
		}
		Map<String,Object> result = new HashMap<>();
		//每个指标进行比较
		data1.forEach((field, value) -> {
			if(value != null && data2.get(field) != null){
				
			}
		});
		return result;
	}
}
