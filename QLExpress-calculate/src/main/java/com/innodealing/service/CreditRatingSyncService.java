package com.innodealing.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.innodealing.config.RestUrlConfig;
import com.innodealing.constant.CommonConstant;
import com.innodealing.constant.ResponseData;
import com.innodealing.dao.BondComExtDao;
import com.innodealing.dao.BondCreditRatingDao;
import com.innodealing.dao.DmBondDao;
import com.innodealing.dao.DmFinQualityAnalysisDao;
import com.innodealing.dao.RatingRatioScoreDao;
import com.innodealing.domain.BondCreditRating;
import com.innodealing.domain.RatingRatioScore;
import com.innodealing.util.DateUtils;
import com.innodealing.vo.BondComExtVo;
import com.innodealing.vo.JsonResult;
import com.innodealing.vo.rating.RatingModelVo;

/**
 * 同步财务质量、信誉评级、指标得分service
 * @author 赵正来
 *
 */
@Service
public class CreditRatingSyncService {
	
	
	private @Autowired BondCreditRatingDao bondCreditRatingDao;
	
	private @Autowired RatingRatioScoreDao ratingRatioScoreDao;
	
	private @Autowired DmBondDao bondDao;
	
	private @Autowired DmFinQualityAnalysisDao dmFinQualityAnalysisDao;
	
	private @Autowired BondComExtDao bondComExtDao;
	

	private @Autowired CreditRatingCalculateService creditRatingCalculateService;
	
	private @Autowired BondFinaSheetSyncService finaSheetSyncService;
	
	private @Autowired RestTemplate restTemplate;
	
	private @Autowired RestUrlConfig restUrlConfig;
	
	
	private final static Logger log = LoggerFactory.getLogger(CreditRatingSyncService.class);
	
	/**
	 * dm自定义的model与安硕的model信息map
	 */
	public static Map<String,RatingModelVo> MODEL_MAP_INFO;
	
	static{
		//放到map
		MODEL_MAP_INFO = new HashMap<>();
		MODEL_MAP_INFO.put("bank", new RatingModelVo(464, "银行评级模型"));
		MODEL_MAP_INFO.put("busin",new RatingModelVo(477, "商业服务业评级模型"));
		MODEL_MAP_INFO.put("estate", new RatingModelVo(474, "房地产企业评级模型"));
		MODEL_MAP_INFO.put("indu", new RatingModelVo(479, "工业评级模型"));
		MODEL_MAP_INFO.put("insu", new RatingModelVo(444, "保险评级模型"));
		MODEL_MAP_INFO.put("secu", new RatingModelVo(444, "证券评级模型"));
		MODEL_MAP_INFO.put("nfin", new RatingModelVo(0, "非金融"));
	}
	
	public boolean syncAll(){
		long start = System.currentTimeMillis();
		log.info("total 评级、财务质量、指标得分开始");
		List<BondComExtVo> list = bondComExtDao.findAll();
		list.stream().forEach(comExt -> {
			try {
				sync(comExt.getAmaComId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		long end = System.currentTimeMillis();
		log.info("total  评级、财务质量、指标得分结束，总耗时：" + (end-start) + "ms");
		return true;
	}
	
	/**
	 * 
	 * @param compId 安硕主体id
	 * @return
	 * @throws Exception 
	 */
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public boolean sync(Long compId) throws Exception{
		long start = System.currentTimeMillis();
		//安硕主体id
		BondComExtVo bondComExtVo = bondComExtDao.findByComId(compId);
		if(bondComExtVo == null){
			log.info("主体[compId=" + compId + "]不存在！");
		}
		List<BondCreditRating> list = bondCreditRatingDao.findByCompId(bondComExtVo.getComUniCode());
		if(list == null || list.size() == 0){
			log.info(compId + "list<BondCreditRating> 数据为空");
			return false;
			//throw new BusinessException("list<BondCreditRating> 数据不能为空");
		}
		//分别获得质量数据和指标得分、评级数据
		List<BondCreditRating> qualityList = new ArrayList<>();
		List<BondCreditRating> ratingList = new ArrayList<>();
		list.forEach(creditRating -> {
			if(CommonConstant.ISSUER_TYPE_NFIN.equals(creditRating.getModelId())){
				qualityList.add(creditRating);
			}else{
				ratingList.add(creditRating);
			}
		});
		
		//同步指标得分
		syncRatingRatioScore(ratingList, bondComExtVo);
		//同步信誉评级
		syncDmBondRating(ratingList, bondComExtVo);
		//同步财务质量(只同步非金融数据)
		syncFinanceQualityScore(qualityList, bondComExtVo);
		long end = System.currentTimeMillis();
		log.info("评级、财务质量、指标得分结束[ " + compId + "]"  + (end-start) +"ms");
		return true;
	}

	/**
	 * 同步信誉评级
	 * @param 财务质量评分list
	 * @return
	 * @throws Exception
	 */
	public boolean syncDmBondRating(List<BondCreditRating> list,BondComExtVo bondComExtVo) throws Exception{
		if(list == null || list.size() == 0){
			return false;
		}
		//批量更新参数
		List<Object[]> batchArgs = new ArrayList<>();
		list.forEach(creditRating -> {
			String compName = creditRating.getComChiName();
			String year  = Objects.toString(DateUtils.getYear(creditRating.getFinDate()));
			Byte month  = Byte.valueOf(DateUtils.getMonth(creditRating.getFinDate()) + "" );
			String rating  = creditRating.getRating();
			Object[] arg = {bondComExtVo.getAmaComId(), compName, bondComExtVo.getInduUniCode().toString(), bondComExtVo.getInduUniNameL4(), year, month, rating};
			batchArgs.add(arg);
			//bondDao.insertorUpdate();
		});
		
		bondDao.batchInsertorUpdate(batchArgs);
		return true; 
	}
	
	/**
	 * 同步财务质量
	 * @param 财务质量评分list
	 * @return
	 * @throws Exception
	 */
	public boolean syncFinanceQualityScore(List<BondCreditRating> list, BondComExtVo bondComExtVo) throws Exception{
		if(list == null || list.size()==0){
			return false;
		}
		String compId =  bondComExtVo.getAmaComId().toString();
		//批量更新参数
		List<Object[]> args = new ArrayList<>();
		//保存
		list.forEach(creditRating -> {
			if(creditRating.getFinDate() != null && DateUtils.convert2String(creditRating.getFinDate(), DateUtils.YYYY_MM_DD).contains("12-31")){
				String year =  Objects.toString(DateUtils.getYear(creditRating.getFinDate()));
				BigDecimal scoreB = creditRating.getFinQualityScore();
				Float score = scoreB == null ? null : Float.valueOf(scoreB.toString());
				Object[] arg = {compId, year, score};
				args.add(arg);
			}
		});
		dmFinQualityAnalysisDao.batchInsertorUpdate(args);
		return true; 
	}
	
	/**
	 * 同步指标得分
	 * @param list 财务质量评分list
	 * @return
	 * @throws Exception
	 */
	public boolean syncRatingRatioScore(List<BondCreditRating> list, BondComExtVo bondComExtVo) throws Exception{
		if(list == null || list.size()==0){
			return false;
		}
		List<RatingRatioScore> listToSave = new ArrayList<>();
		for (BondCreditRating bondCreditRating : list) {
			RatingRatioScore ratioScore = new RatingRatioScore();
			//同步
			String modelId = bondCreditRating.getModelId();
			ratioScore.setCompId(bondComExtVo.getAmaComId());
			ratioScore.setYear(DateUtils.getYearMonth(bondCreditRating.getFinDate()));
			ratioScore.setModelId(MODEL_MAP_INFO.get(modelId).getModelId());
			ratioScore.setModelName(MODEL_MAP_INFO.get(modelId).getModelName());
			if(CommonConstant.ISSUER_TYPE_BANK.equals(modelId)) {//银行模型Ratio1Score与Ratio2Score与数据库rating_ratio_score表的ratio1、ratio2正好相反，需要互换
			    ratioScore.setRatio2(bondCreditRating.getRatio1Score()== null ? null : (bondCreditRating.getRatio1Score().setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue()));
	            ratioScore.setRatio1(bondCreditRating.getRatio2Score()== null ? null : (bondCreditRating.getRatio2Score().setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue()));
			} else {
			    ratioScore.setRatio1(bondCreditRating.getRatio1Score()== null ? null : (bondCreditRating.getRatio1Score().setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue()));
	            ratioScore.setRatio2(bondCreditRating.getRatio2Score()== null ? null : (bondCreditRating.getRatio2Score().setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue()));
			}
			ratioScore.setRatio3(bondCreditRating.getRatio3Score()== null ? null : (bondCreditRating.getRatio3Score().setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue()));
			ratioScore.setRatio4(bondCreditRating.getRatio4Score()== null ? null : (bondCreditRating.getRatio4Score().setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue()));
			ratioScore.setRatio5(bondCreditRating.getRatio5Score()== null ? null : (bondCreditRating.getRatio5Score().setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue()));
			ratioScore.setRatio6(bondCreditRating.getRatio6Score()== null ? null : (bondCreditRating.getRatio6Score().setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue()));
			ratioScore.setRatio7(bondCreditRating.getRatio7Score()== null ? null : (bondCreditRating.getRatio7Score().setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue()));
			ratioScore.setRatio8(bondCreditRating.getRatio8Score()== null ? null : (bondCreditRating.getRatio8Score().setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue()));
			ratioScore.setRatio9(bondCreditRating.getRatio9Score()== null ? null : (bondCreditRating.getRatio9Score().setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue()));
			ratioScore.setRatio10(bondCreditRating.getRatio10Score()== null ? null : (bondCreditRating.getRatio10Score().setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue()));
			//ratingRatioScoreDao.insert(ratioScore);
			listToSave.add(ratioScore);
		}
		return ratingRatioScoreDao.batchInsert(listToSave);
	}
	
	
	/**
	 * 同步主体财务指标、专项指标、质量评级信息
	 * @param comUniCode
	 * @param isAddComp
	 * @param sheetSource
	 * @param dataInDb
	 * @return
	 * @throws InterruptedException
	 */
	public boolean syncIndicatorAndRating(Long comUniCode, boolean isAddComp, String sheetSource,Date finDate, Map<String,Object> dataInDb) throws InterruptedException{
		BondComExtVo comExt = bondComExtDao.findByUniCode(comUniCode);
		//中诚信的数据到dmdb
		if(isAddComp){
			String syncFinaSheetResult = finaSheetSyncService.processIssuerFinaSheet(comUniCode, finDate, false);
			log.info("将中诚信数["+ comUniCode +"]据同步到dmdb中，执行结果：" + syncFinaSheetResult);
		}
 		if(comExt != null){
			//计算指标
//			boolean finCalcuResut = financeIndicatorService.save(comExt.getAmaComId(), null, DateUtils.convert2String(finDate, DateUtils.YYYY_MM_DD));
//			log.info("计算指标["+ comUniCode +"]，执行结果：" + finCalcuResut);
			//计算评级
			ResponseData rd = creditRatingCalculateService.addCalculate(comExt.getAmaComId(), finDate, sheetSource);
			log.info("计算评级和财务质量["+ comUniCode +"]，执行结果：" + rd.toString());
		}
		//将更新后的财务指标和专项指标同步到mongo中
		if(comExt != null){
			new Runnable() {
				@Override
				public void run() {
					String url = restUrlConfig.bondIntegrationSyncComIndicator + "?compId=" + comExt.getAmaComId();
					JsonResult result =	restTemplate.postForObject( url, null, JsonResult.class); 
					log.info("将数据同步到mongo["+ comUniCode +"]，执行结果：" + result.toString());
				}
			}.run();
		}
		//将指标的信息和行业分位保存到备忘录中
//		new Runnable() {
//			public void run() {
//				JsonResult resultMementoAdd = restTemplate.postForObject(String.format(restUrlConfig.bondWebindicatorsMementoAdd, comUniCode, DateUtils.convert2String(finDate, DateUtils.YYYY_MM_DD)), 
//						null, JsonResult.class);
//				log.info("将指标的信息和行业分位保存到备忘录中["+ comUniCode +"]，执行结果：" + resultMementoAdd.toString());
//			}
//		}.run();
		return true;
	}

	/**
	 * 获取指标的分位值
	 * @param comUniCode
	 * @param finDate
	 * @param urlQuartiles
	 * @param finCalcuResut
	 * @return
	 */
	private Map<String,Object> getIndicatorQuartile(Long comUniCode, Date finDate, String urlQuartiles, boolean finCalcuResut) {
		Map<String,Object> newestDataRankInsert = new HashMap<>();
		//指标计算前的分位值
		if(finCalcuResut){
			//最新的专项指标分位数据
			JsonResult quartiles =  restTemplate.getForObject(String.format(urlQuartiles, comUniCode, DateUtils.convert2String(finDate, DateUtils.YYYY_MM_DD), CommonConstant.SPECIAL,null ),
					JsonResult.class);
			if(quartiles.getData() != null){
				newestDataRankInsert.putAll((Map<String, Object>)quartiles.getData());
			}
			//最新的财务数据分位数据
			JsonResult quartilesFinance =  restTemplate.getForObject(String.format(urlQuartiles, comUniCode, DateUtils.convert2String(finDate, DateUtils.YYYY_MM_DD),CommonConstant.FINANCE,null ),
					JsonResult.class);
			if(quartilesFinance.getData() != null){
				newestDataRankInsert.putAll((Map<String, Object>)quartilesFinance.getData());
			}
		}
		return newestDataRankInsert;
	}
	
	
	
	
}
