package com.innodealing.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.innodealing.amqp.CreditRatingAmqpSender;
import com.innodealing.config.AppConfig;
import com.innodealing.config.DatabaseNameConfig;
import com.innodealing.config.RestUrlConfig;
import com.innodealing.constant.CommonConstant;
import com.innodealing.constant.ResponseData;
import com.innodealing.constant.TableNameConstant;
import com.innodealing.dao.BondCalculateErrorLogDao;
import com.innodealing.dao.BondCreditRatingDao;
import com.innodealing.dao.BondFinanceSheetDao;
import com.innodealing.dao.BondIndustryClassificationDao;
import com.innodealing.domain.BondCalculateErrorLog;
import com.innodealing.domain.BondCreditRating;
import com.innodealing.domain.BondFinanceSheetIndicatorExpression;
import com.innodealing.engine.OriginalData;
import com.innodealing.json.BondCreditRatingJson;
import com.innodealing.json.CreditRatingCalculateJson;
import com.innodealing.redis.RedisUtil;
import com.innodealing.util.DateUtils;
import com.innodealing.util.MD5Utils;
import com.innodealing.util.SafeUtils;
import com.innodealing.util.express.AvgOperator;
import com.innodealing.util.express.StdOperator;
import com.innodealing.vo.IndicatorVo;
import com.innodealing.vo.JsonResult;

/**
 * 主体信誉评级计算接口
 * 
 * @author zzl
 *
 */
@Service
public class CreditRatingCalculateService {
    
    public static final Logger log = LoggerFactory.getLogger(CreditRatingCalculateService.class);
            
    @Autowired private DatabaseNameConfig databaseNameConfig;
    
    @Autowired private BondIndustryClassificationDao bondIndustryClassificationDao;
    
    @Autowired private CalculateService calculateService;
    
    @Autowired private AppConfig appConfig;
    
    @Autowired private BondCreditRatingDao bondCreditRatingDao;
    
    @Autowired private RestTemplate restTemplate;
    
    @Autowired private RedisUtil redisUtil;
    
    @Autowired private CreditRatingAmqpSender creditRatingAmqpSender;
    
    @Autowired private BondCalculateErrorLogDao bondCalculateErrorLogDao;
    
    @Autowired private BondFinanceSheetDao bondFinanceSheetDao;
    
    @Autowired private CreditRatingSyncService creditRatingSyncService;
    
    @Autowired private FinanceIndicatorService financeIndicatorService;
   
    @Autowired private  RestUrlConfig restUrlConfig;
    
    /** 获取所有财报主体行业分类及主体名称 */
    private Map<Long, Map<String, Object>> classiFicationMap =  null; 
    /** 所有财报模型指标计算公式 */
    private Map<String,List<BondFinanceSheetIndicatorExpression>> sheetExpsMap = null;

    /**
     * 
     * initfinanceSheetRatio:(计算财报指标值、获取指标得分、评级、质量得分) (这里描述这个方法的执行流程 – 可选):
     * <p>
     * 1:获取财报数据 manu、insu、secu、bank 2:根据公司id获取行业分类代码 3：根据行业分类代码获取模型id
     * 4：根据模型id，获取模型指标计算公式 5：根据指标计算公式和财报数据，计算财报指标值，存入数据库
     * 6：调用安硕评级计算接口，获取指标得分、风险评级 7：调用安硕财报质量分析接口，获取质量分析评分，更新指标得分、风险评级、质量分析评分。
     * </p>
     * 
     * @author chungaochen
     * @param @return
     * @param @throws
     *            BusinessException
     * @param @throws
     *            SQLException
     * @return boolean
     * @throws @since
     *             CodingExample Ver 1.1
     */
    public boolean initfinanceSheetRatio(){
        boolean result = true;
        log.info("initfinanceSheetRatio start");
        int splitSize = 100;
        try {
            // 先删除所有
            String tableName = databaseNameConfig.getDmdb() + ".t_bond_credit_rating";
            bondIndustryClassificationDao.truncateTable(tableName);
            tableName = databaseNameConfig.getDmdb() + ".t_bond_calculate_error_log";
            bondIndustryClassificationDao.truncateTable(tableName);
            /**
             * 获取所有已映射主体的行业分类，没有映射公司的不处理
             */
            Map<Long, Map<String, Object>> classificationMap = getClassMap();
            if(null== classificationMap || classificationMap.keySet().size()==0) {
                return result;
            }
            
            List<Long> amaIdList = new ArrayList<>(classificationMap.keySet());
            List<List<Long>> amaIdSubList= splitList(amaIdList, splitSize);
            String token = getToken();//token放入mq
            /** 将财报主体id发送至MQ消息队列,采用分布式多机计算，提高速度 */
            for(List<Long> list : amaIdSubList) {
                creditRatingAmqpSender.send(new CreditRatingCalculateJson(list,CommonConstant.SHEET_SOURCE_CCXE,token));
            }
        } catch(Exception e) {
            log.error("initfinanceSheetRatio error :" + e.getMessage());
            result = false;
        }
        return result;
    }
    
    /**
     * 
     * threadCalculateOperate:(多线程处理财报指标计算)
     * @param  @param comIds
     * @param  @return    设定文件
     * @return boolean    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public boolean threadCalculateOperate(CreditRatingCalculateJson comIds) {
        int limitSize = 50;// 每次计算财报主体数
        boolean result = true;
        try {
            List<Long> amaIdList = comIds.getCompIds();
            if(null == amaIdList || amaIdList.size()==0) {
                return result;
            }
            List<List<Long>> amaIdSubList= splitList(amaIdList, limitSize);
            int totalSize = amaIdList.size()%limitSize==0 ? amaIdList.size()/limitSize : amaIdList.size()/limitSize+1;
            /**
             * ThreadPoolExecutor线程池： <p>corePoolSize - 核心线程池大小，包括空闲线程。 maximumPoolSize-池中允许的最大线程数(采用LinkedBlockingQueue时没有作用)。
             * keepAliveTime-线程池中超过corePoolSize数目的空闲线程等待新任务的最长时间，线程池维护线程所允许的空闲时间。可以allowCoreThreadTimeOut(true)使得核心线程有效时间
             * unit-keepAliveTime参数的时间单位，线程池维护线程所允许的空闲时间的单位:秒 。
             * workQueue-阻塞任务队列（缓冲队列）。此队列仅保持由execute方法提交的 Runnable 任务。
             * RejectedExecutionHandler-当提交任务数超过maxmumPoolSize+workQueue之和时，任务会交给RejectedExecutionHandler来处理。
             * 这个策略默认情况下是AbortPolicy，表示无法处理新任务时抛出异常。
             * CallerRunsPolicy：只用调用者所在线程来运行任务。
             * DiscardOldestPolicy：丢弃队列里最近的一个任务，并执行当前任务。 DiscardPolicy：不处理，丢弃掉。
             * 当然也可以根据应用场景需要来实现RejectedExecutionHandler接口自定义策略。如记录日志或持久化不能处理的任务。</p>
             **/
            //构造一个缓冲功能的线程池，new ArrayBlockingQueue<Runnable>(totalPageSize)==totalPageSize容量的阻塞队列  ArrayBlockingQueue，
            //rejectedExecutionHandler因提交任务数不会超过maxmumPoolSize+workQueue之和，所以无需实现策略
            ThreadPoolExecutor threadpool = new ThreadPoolExecutor(0, 9, 60,TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(totalSize)); 
            List<Future<String>> calculationResultList = new ArrayList<Future<String>>(totalSize);
            for(List<Long> list : amaIdSubList) {
                calculationResultList.add(threadpool.submit(new Callable<String>() {
                    @Override
                    public String call() {
                        return calculationRatio(list, comIds.getSheetSource(), comIds.getToken());
                    }
                }));
            }
            
            // 关闭线程池:会等待所有线程执行完
            threadpool.shutdown();
            threadpool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            for (Future<String> res : calculationResultList) {
                if (!"0000".equals(res.get().toString())) {
                    result = false;
                    break;
                }
            }
        } catch(Exception e) {
            log.error("threadCalculateOperate error :" + e.getMessage());
            result = false;
        }
        return result;
    }
    
    /**
     * 
     * getTableName:(获取财报表名称)
     * @param  @param sheetType
     * @param  @return    设定文件
     * @return String    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private String getTableName(String sheetType) {
        String tableName ="";
        if(CommonConstant.ISSUER_TYPE_NFIN.equals(sheetType)) {
            tableName = databaseNameConfig.getDmdb() + "." + TableNameConstant.FIN_SHEET_INDU;// 非金融
        } else if (CommonConstant.ISSUER_TYPE_BANK.equals(sheetType)) {
            tableName = databaseNameConfig.getDmdb() + "." +  TableNameConstant.FIN_SHEET_BANK;// 银行
        } else if (CommonConstant.ISSUER_TYPE_INSU.equals(sheetType)) {
            tableName = databaseNameConfig.getDmdb() + "." +  TableNameConstant.FIN_SHEET_INSU;// 保险
        } else if (CommonConstant.ISSUER_TYPE_SECU.equals(sheetType)) {
            tableName = databaseNameConfig.getDmdb() + "." +  TableNameConstant.FIN_SHEET_SECU;// 券商
        } 
        return tableName;
    }
    
    /**
     * 
     * getSheetType:(获取财报类别)
     * @param  @param modelId
     * @param  @return    设定文件
     * @return String    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private String getSheetType(String modelId) {
        String sheetType ="";
        if(CommonConstant.ISSUER_TYPE_INDU.equals(modelId) || CommonConstant.ISSUER_TYPE_BUSIN.equals(modelId) || 
                CommonConstant.ISSUER_TYPE_ESTATE.equals(modelId) || CommonConstant.ISSUER_TYPE_NFIN.equals(modelId)) {
            sheetType = CommonConstant.ISSUER_TYPE_NFIN;// 非金融
        } else if (CommonConstant.ISSUER_TYPE_BANK.equals(modelId)) {
            sheetType = CommonConstant.ISSUER_TYPE_BANK;// 银行
        } else if (CommonConstant.ISSUER_TYPE_INSU.equals(modelId)) {
            sheetType = CommonConstant.ISSUER_TYPE_INSU;// 保险
        } else if (CommonConstant.ISSUER_TYPE_SECU.equals(modelId)) {
            sheetType = CommonConstant.ISSUER_TYPE_SECU;// 券商
        }
        return sheetType;
    }

    /**
     * 
     * calculationRatio:(计算财报指标值、指标得分、指标质量、风险评级)
     * 
     * @author chungaochen
     * @param @param amaIdlist
     * @param @param sheetSource 财报来源：0-中诚信/1-DM
     * * @param @param token 安硕接口令牌
     * @param @return
     * @return String DOM对象
     * @throws @since
     *             CodingExample Ver 1.1
     */
    public String calculationRatio(List<Long> amaIdlist, String sheetSource, String token) {
        String result = "0000";// 0000-完成计算，其他异常
        try {
            List<BondCreditRating> batchList = new ArrayList<>();
            List<BondCalculateErrorLog> errorLogList = new ArrayList<>();
            String errorType = "指标计算错误";
            Date nowDate = new Date();
            for (Long amaId : amaIdlist) {
                Map<String,Object> classMap = getClassMap().get(amaId);
                if(null== classMap || classMap.keySet().size()==0) {//主体不在t_bond_com_ext跳过
                    log.error("主体|发行人id（安硕）"+amaId+"在t_bond_com_ext不存在，不计算财报指标");
                    BondCalculateErrorLog errLog = new BondCalculateErrorLog();
                    errLog.setAmaComId(amaId);
                    errLog.setCreateTime(DateUtils.convertSqlTime(nowDate));
                    errLog.setErrorRemark("主体|发行人id（安硕）"+amaId+"在t_bond_com_ext不存在，不计算财报指标");
                    errLog.setErrorType(errorType);
                    errLog.setLastUpdateTime(DateUtils.convertSqlTime(nowDate));
                    errorLogList.add(errLog);
                    continue;
                }
                String modelId = String.valueOf(classMap.get("model_id"));
                String sheetType = getSheetType(modelId);
                List<Map<String, Object>> list = bondFinanceSheetDao.queryFinaSheetByComId(getTableName(sheetType), amaId);
                if(null!= list && list.size()>0) {
                    Map<String, Object> classificationMap = getClassMap().get(amaId);
                    long com_uni_code = Long.parseLong(String.valueOf(classificationMap.get("com_uni_code")));// bond系统公司id
                    Map<String,List<Map<String, Object>>> comAllFinSheetMaps = getComFinaSheetGroups(list);
                    //年报数据(含本期）
                    List<Map<String, Object>> annualItems = comAllFinSheetMaps.get(amaId+"_"+"12");
                    OriginalData annualData = new OriginalData(annualItems);
                    //同期原始季报数据(含本期）, 季报需要转换年报
                    OriginalData same3AnnualData = convertAnnual(comAllFinSheetMaps.get(amaId+"_"+"3"),annualItems,getTableName(sheetType),
                            amaId,com_uni_code);
                    OriginalData same6AnnualData = convertAnnual(comAllFinSheetMaps.get(amaId+"_"+"6"),annualItems,getTableName(sheetType),
                            amaId,com_uni_code);
                    OriginalData same9AnnualData = convertAnnual(comAllFinSheetMaps.get(amaId+"_"+"9"),annualItems,getTableName(sheetType),
                            amaId,com_uni_code);
                    for (Map<String, Object> sheetMap : list) {
                        String finPeriod = String.valueOf(sheetMap.get("FIN_PERIOD"));// 报表涵盖期间
                        OriginalData sheetData = null;
                        if("3".equals(finPeriod)) {//年报数据
                            sheetData = same3AnnualData;
                        } else if("6".equals(finPeriod)){
                            sheetData = same6AnnualData;
                        } else if("9".equals(finPeriod)){
                            sheetData = same9AnnualData;
                        } else if("12".equals(finPeriod)){
                            sheetData = annualData;
                        }
                        calculdteOperator(sheetType,sheetSource,token,sheetMap,batchList,sheetData,classificationMap);
                    }
                } else {
                    log.error("主体|发行人id（安硕）"+amaId+"在"+getTableName(sheetType)+"无财报数据，不计算财报指标");
                    BondCalculateErrorLog errLog = new BondCalculateErrorLog();
                    errLog.setAmaComId(amaId);
                    errLog.setCreateTime(DateUtils.convertSqlTime(nowDate));
                    errLog.setErrorRemark("主体|发行人id（安硕）"+amaId+"在"+getTableName(sheetType)+"无财报数据，不计算财报指标");
                    errLog.setErrorType(errorType);
                    errLog.setLastUpdateTime(DateUtils.convertSqlTime(nowDate));
                    errorLogList.add(errLog);
                    continue;
                }
            }
            bondCreditRatingDao.batchInsertBondCreditRating(batchList);
            bondCalculateErrorLogDao.batchInsertBondCalculateErrorLog(errorLogList);
        } catch (Exception e) {
            log.error("calculationRatio error :" + e.getMessage());
            result = "1111";
            e.printStackTrace();
        } finally {

        }
        return result;
    }

    /**
     * 
     * addCalculate:(添加财报主体计算财报指标得分评级及质量)
     * 
     * @param @param
     *            amaId 主体id(安硕）
     * @param @param
     *            finDate
     * @param @param           
     *            sheetSource 财报来源：0-中诚信/1-DM
     * @param @return
     *            设定文件
     * @return boolean DOM对象
     * @throws @since
     *             CodingExample Ver 1.1
     */
    @Transactional
    public ResponseData addCalculate(Long amaId, Date finDate, String sheetSource) {
        ResponseData result = new ResponseData();
        try {
            Map<String, Object> classificationMap = getClassMap().get(amaId);
            if(null== classificationMap || classificationMap.keySet().size()==0) {//主体不在t_bond_com_ext跳过
                result.setResponseCode(CommonConstant.DMCallbackCode.SYSTEM_INTERNAL_ERROR.getCode());
                result.setResponseMessage("主体|发行人id（安硕）"+amaId+"在t_bond_com_ext不存在，请确认后重新操作");
                return result;
            }
            // 模型ID:bank-银行 indu-工业 insu-保险 secu-证券 busin-商业 estate-房地产
            String modelId = String.valueOf(classificationMap.get("model_id"));
            String sheetType = getSheetType(modelId);

            List<BondCreditRating> BondCreditRatingList = new ArrayList<>();
            List<Map<String, Object>> sheetMapList  = new ArrayList<>();
            if(null == finDate) {
                sheetMapList = bondFinanceSheetDao.queryFinaSheetByComId(getTableName(sheetType), amaId);
            } else {
                Map<String, Object> oneSheetMap = bondFinanceSheetDao.findSheetByIssuerIdAndFinDate(getTableName(sheetType), amaId, finDate);
                if(null == oneSheetMap || oneSheetMap.keySet().size()==0) {
                    result.setResponseCode(CommonConstant.DMCallbackCode.SYSTEM_INTERNAL_ERROR.getCode());
                    result.setResponseMessage("没有对应的财报数据，请确认后重新操作");
                    return result;
                } 
                sheetMapList.add(oneSheetMap);
            }
            
            long comUniCode = Long.parseLong(String.valueOf(classificationMap.get("com_uni_code")));// bond系统公司id
            Map<String, List<Map<String, Object>>> comAllFinSheetMaps = bondFinanceSheetDao.queryComFinaSheetGroups(getTableName(sheetType), amaId);
            for(Map<String, Object> sheetMap : sheetMapList) {
                finDate = (java.util.Date) sheetMap.get("FIN_DATE");// 报表日期
                // 删除老数据重新计算
                bondCreditRatingDao.delCreditRatingByIssuerIdAndFinDate(comUniCode, finDate);
                String finPeriod = String.valueOf(sheetMap.get("FIN_PERIOD"));// 报表涵盖期间
                //年报数据(含本期）
                List<Map<String, Object>> annualItems = comAllFinSheetMaps.get(amaId+"_"+"12");
                //同期原始数据(含本期）
                List<Map<String, Object>> sameTermTerms = comAllFinSheetMaps.get(amaId+"_"+finPeriod);
                OriginalData sheetData = null;
                if("12".equals(finPeriod)) {//年报数据
                    sheetData = new OriginalData(annualItems);
                } else{//季报需要转换年报
                    sheetData = convertAnnual(sameTermTerms, annualItems,getTableName(sheetType),amaId,comUniCode);
                }
                calculdteOperator(sheetType,sheetSource,getToken(),sheetMap,BondCreditRatingList,sheetData,classificationMap);
            }
            bondCreditRatingDao.batchInsertBondCreditRating(BondCreditRatingList);// 批量插入主体财报指标质量得分评级表
        
            result.setResponseCode(CommonConstant.DMCallbackCode.NOMAL_RETURN.getCode());
            result.setResponseMessage("新增成功");
            //将数据同步到t_bond、dm_fin_quality_analysis、rating_ratio_score
            creditRatingSyncService.sync(amaId);
            //计算专项指标
            financeIndicatorService.save(amaId, null,DateUtils.convert2String(finDate, DateUtils.YYYY_MM_DD));
            //将最新指标加到备忘录，并推送给用户
            String dateString = DateUtils.convert2String(finDate, DateUtils.YYYY_MM_DD);
//            new Runnable() {
//    			public void run() {
//    				JsonResult resultMementoAdd = restTemplate.postForObject(String.format(restUrlConfig.bondWebindicatorsMementoAdd, comUniCode, dateString), 
//    						null, JsonResult.class);
//    				log.info("将指标的信息和行业分位保存到备忘录中["+ comUniCode +"]，执行结果：" + resultMementoAdd.toString());
//    			}
//    		}.run();
        } catch (Exception e) {
            log.error("addCalculate error :" + e.getMessage());
            result.setResponseCode(CommonConstant.DMCallbackCode.SYSTEM_INTERNAL_ERROR.getCode());
            result.setResponseMessage(e.getMessage());
        }
        return result;

    }

    /**
     * 
     * calculdteOperator:(主体一期财报指标计算)
     * @param  @param sheetType
     * @param  @param sheetSource
     * * @param  @param token 安硕接口令牌
     * @param  @param sheetMap
     * @param  @param BondCreditRatingList
     * @param  @param sheetData    主体所有财报数据
     * @param  @param classificationMap    主体行业分类信息
     * @return void    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private void calculdteOperator(String sheetType, String sheetSource, String token, Map<String, Object> sheetMap, 
            final List<BondCreditRating> bondCreditRatingList, OriginalData  sheetData,  Map<String, Object> classificationMap) {
        java.sql.Date finDate = (java.sql.Date) sheetMap.get("FIN_DATE");// 报表日期
        /**
         * 获取行业分类、模型id 模型ID:bank-银行 indu-工业 insu-保险 secu-证券 busin-商业 estate-房地产
         */
        if (null == classificationMap || null==sheetData) {
            return;
        }
        String modelId = String.valueOf(classificationMap.get("model_id"));
        String expressionModelId = modelId;// 指标表达式模型Id
        int induScore = Integer.parseInt(String.valueOf(classificationMap.get("INDU_SCORE")));// 行业得分
        long ama_com_id = Long.parseLong(String.valueOf(classificationMap.get("ama_com_id")));//主体|发行人id（安硕）
        long com_uni_code = Long.parseLong(String.valueOf(classificationMap.get("com_uni_code")));// bond系统公司id
        String com_chi_name = String.valueOf(classificationMap.get("com_chi_name"));// 公司名称
        Date nowDate = new Date();
        
        log.info("calculdteOperator  com_uni_code=" + com_uni_code +" fin_date=" + 
                DateUtils.convert2String(finDate,DateUtils.YYYY_MM_DD)+" begin!");
        
        BondCreditRating bean = new BondCreditRating();
        bean.setComChiName(com_chi_name);
        bean.setComUniCode(com_uni_code);
        bean.setModelId(modelId);
        bean.setFinDate(finDate);
        bean.setCreateTtime(DateUtils.convertSqlTime(nowDate));
        bean.setLastUpdateTime(DateUtils.convertSqlTime(nowDate));
        bean.setSource(CommonConstant.SHEET_SOURCE_CCXE.equals(sheetSource) ? "中诚信" : "DM");
        boolean isModelMismatching = false;
        if(CommonConstant.ISSUER_TYPE_NFIN.equals(sheetType) &&  !(CommonConstant.ISSUER_TYPE_INDU.equals(modelId) || 
                CommonConstant.ISSUER_TYPE_BUSIN.equals(modelId) || CommonConstant.ISSUER_TYPE_ESTATE.equals(modelId))) {
            bean.setRemark("manu财报数据和行业分类模型"+modelId+"不匹配");
            isModelMismatching = true;
        } else if (CommonConstant.ISSUER_TYPE_BANK.equals(sheetType) && !CommonConstant.ISSUER_TYPE_BANK.equals(modelId)) {
            bean.setRemark("bank财报数据和行业分类模型"+modelId+"不匹配");
            isModelMismatching = true;
        } else if (CommonConstant.ISSUER_TYPE_INSU.equals(sheetType) && !CommonConstant.ISSUER_TYPE_INSU.equals(modelId)) {
            bean.setRemark("insu财报数据和行业分类模型"+modelId+"不匹配");
            isModelMismatching = true;
        } else if (CommonConstant.ISSUER_TYPE_SECU.equals(sheetType) && !CommonConstant.ISSUER_TYPE_SECU.equals(modelId)) {
            bean.setRemark("secu财报数据和行业分类模型"+modelId+"不匹配");
            isModelMismatching = true;
        }
        if(isModelMismatching) {
            bondCreditRatingList.add(bean);//模型不匹配，该财报数据不计算
        } else {
            List<BondFinanceSheetIndicatorExpression> modelExpression = getModelExpMap().get(modelId);
            List<BondCalculateErrorLog> errorLogList = new ArrayList<>();
            for (BondFinanceSheetIndicatorExpression expression : modelExpression) {
                String field = expression.getField();// 指标代码
                BigDecimal fieldcalculate = null;// 指标计算值
                String expressFormat = BondFinanceSheetIndicatorExpression.formate(expression.getExpressFormat());// 格式化指标计算表达式
                expression.setExpressFormat(expressFormat);
                if ("INDU_SCORE".equalsIgnoreCase(expressFormat)) {// 行业得分
                    fieldcalculate = new BigDecimal(induScore);
                    setFieldValue(bean, field, fieldcalculate.toString());// 设置指标值
                    continue;
                }
                if ("ratio9".equalsIgnoreCase(field) && CommonConstant.ISSUER_TYPE_ESTATE.equals(modelId)) {// 房地产企业性质需要单独查询
                    Map<String, Object> commNatureMap = bondIndustryClassificationDao.getCompanyNature(com_uni_code);
                    if (null != commNatureMap && null != commNatureMap.get("COM_ATTR_PAR") && ("1".equals(String.valueOf(commNatureMap.get("COM_ATTR_PAR")))
                            || "2".equals(String.valueOf(commNatureMap.get("COM_ATTR_PAR"))))) {// 中央国有企业、地方国有企业：值为2
                        fieldcalculate = new BigDecimal(2);
                    } else {
                        fieldcalculate = new BigDecimal(-0.5);
                    }
                    setFieldValue(bean, field, fieldcalculate.toString());// 设置指标值
                    continue;
                }
                try{
                    IndicatorVo vo = calculateService.calculateFinSheet(sheetData, expression, finDate, 
                            getModelExpressionMap(expressionModelId));
                    fieldcalculate = vo.getIndicatorValue();
                    if (null != fieldcalculate) {
                        setFieldValue(bean, field, fieldcalculate.toString());// 设置指标值
                    } 
                } catch(Exception e) {
                    //判断异常消息是否为空 ，RuntimeException异常getMessage()为空
                    log.error("calculdteOperator rating error：  com_uni_code=" + com_uni_code +" fin_date=" + 
                            DateUtils.convert2String(finDate,DateUtils.YYYY_MM_DD)+" ratio=" + field +
                            e.getMessage());
                    String errorRemark = e.getMessage();
                    BondCalculateErrorLog errLog = new BondCalculateErrorLog();
                    errLog.setAmaComId(ama_com_id);
                    errLog.setComUniCode(com_uni_code);
                    errLog.setCreateTime(DateUtils.convertSqlTime(nowDate));
                    errLog.setErrorRemark(errorRemark);
                    errLog.setErrorType("评级指标计算错误");
                    errLog.setFinDate(finDate);
                    errLog.setLastUpdateTime(DateUtils.convertSqlTime(nowDate));
                    errLog.setTableName(getTableName(sheetType));
                    errLog.setExpressFormat(expressFormat);
                    errLog.setRatio(field);
                    errorLogList.add(errLog);
                }
            }
            /**
             * 调用计算指标得分，风险评级接口
             */
            String callResult = "计算成功";
            try {
                ResponseData response = getRatingData(bean, token);// 可以判断调用结果
                callResult = response.getResponseMessage();
            } catch (Exception e) {
                callResult = e.getMessage();
            }
            bean.setRemark(callResult);
            bondCreditRatingList.add(bean);
            
            /** 非金融：接着计算nfin指标值  */
            if (CommonConstant.ISSUER_TYPE_INDU.equals(modelId) || CommonConstant.ISSUER_TYPE_BUSIN.equals(modelId) || CommonConstant.ISSUER_TYPE_ESTATE.equals(modelId)) {
                expressionModelId = CommonConstant.ISSUER_TYPE_NFIN;// 非金融
                BondCreditRating nFinBean = new BondCreditRating();
                nFinBean.setComChiName(com_chi_name);
                nFinBean.setComUniCode(com_uni_code);
                nFinBean.setModelId(expressionModelId);
                nFinBean.setFinDate(finDate);
                nFinBean.setCreateTtime(DateUtils.convertSqlTime(nowDate));
                nFinBean.setLastUpdateTime(DateUtils.convertSqlTime(nowDate));
                nFinBean.setSource(CommonConstant.SHEET_SOURCE_CCXE.equals(sheetSource) ? "中诚信" : "DM");
                BondCalculateErrorLog errLog = new BondCalculateErrorLog();
                errLog.setAmaComId(ama_com_id);
                errLog.setComUniCode(com_uni_code);
                errLog.setCreateTime(DateUtils.convertSqlTime(nowDate));
                errLog.setLastUpdateTime(DateUtils.convertSqlTime(nowDate));
                errLog.setTableName(getTableName(sheetType));
                //计算质量指标
                calQuanRatio(nFinBean,errLog,errorLogList,bondCreditRatingList,token,expressionModelId,induScore,sheetData,finDate);
            } 
            bondCalculateErrorLogDao.batchInsertBondCalculateErrorLog(errorLogList);//插入指标计算错误日志
            log.info("calculdteOperator  com_uni_code=" + com_uni_code +" fin_date=" + 
                    DateUtils.convert2String(finDate,DateUtils.YYYY_MM_DD)+" end!");
        }
    }
    
    /**
     * 
     * calQuanRatio:(计算质量指标:注意ql表达式要转换为小写)
     * @param  @param nFinBean
     * @param  @param errLog
     * @param  @param errorLogList
     * @param  @param bondCreditRatingList
     * @param  @param token
     * @param  @param expressionModelId
     * @param  @param induScore
     * @param  @param sheetData
     * @param  @param finDate    设定文件
     * @return void    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private void calQuanRatio(final BondCreditRating nFinBean,final BondCalculateErrorLog err,final List<BondCalculateErrorLog> errorLogList,
            final List<BondCreditRating> bondCreditRatingList, final String token, String expressionModelId,int induScore,OriginalData sheetData,java.sql.Date finDate  ) {
        List<BondFinanceSheetIndicatorExpression> nFinExpression = getModelExpMap().get(expressionModelId);
        for (BondFinanceSheetIndicatorExpression expression : nFinExpression) {
            String field = expression.getField();// 指标代码
            BigDecimal fieldcalculate = null;// 指标计算值
            String expressFormat = BondFinanceSheetIndicatorExpression.formate(expression.getExpressFormat());// 格式化指标计算表达式
            expression.setExpressFormat(expressFormat);
            if ("INDU_SCORE".equalsIgnoreCase(expressFormat)) {// 行业得分
                fieldcalculate = new BigDecimal(induScore);
                setFieldValue(nFinBean, field, fieldcalculate.toString());// 设置指标值
                continue;
            }
            try {
                /*
                 * 手动计算：
                 * std((if(pp2(PL400) < 0) {0.2} else{(pp2(PL501) + pp3(BS406) - pp2(BS406)) / pp2(PL400)}),(if(pp(PL400) < 0) {0.2} else{(pp(PL501) + pp2(BS406) - pp(BS406)) / pp(PL400)}),(if(PL400 < 0) {0.2} else{(PL501 + pp(BS406) - BS406) / PL400}))/
                 * avg((if(pp2(PL400) < 0) {0.2} else{(pp2(PL501) + pp3(BS406) - pp2(BS406)) / pp2(PL400)}),(if(pp(PL400) < 0) {0.2} else{(pp(PL501) + pp2(BS406) - pp(BS406)) / pp(PL400)}),(if(PL400 < 0) {0.2} else{(PL501 + pp(BS406) - BS406) / PL400}))
                 */
                if("ratio4".equalsIgnoreCase(field)) {
                    expression.setExpressFormat("if(pp2(PL400) < 0) {0.2} else{(pp2(PL501) + pp3(BS406) - pp2(BS406)) / pp2(PL400)}".toLowerCase());
                    IndicatorVo vo = calculateService.calculateFinSheet(sheetData, expression, finDate,
                            getModelExpressionMap(expressionModelId));
                    fieldcalculate = vo.getIndicatorValue();
                    BigDecimal b1 = fieldcalculate;
                    if(null == b1) {
                        continue;
                    }
                    expression.setExpressFormat("if(pp(PL400) < 0) {0.2} else{(pp(PL501) + pp2(BS406) - pp(BS406)) / pp(PL400)}".toLowerCase());
                    vo = calculateService.calculateFinSheet(sheetData, expression, finDate,
                            getModelExpressionMap(expressionModelId));
                    fieldcalculate = vo.getIndicatorValue();
                    BigDecimal b2 = fieldcalculate;
                    if(null == b2) {
                        continue;
                    }
                    expression.setExpressFormat("if(PL400 < 0) {0.2} else{(PL501 + pp(BS406) - BS406) / PL400}".toLowerCase());
                    vo = calculateService.calculateFinSheet(sheetData, expression, finDate,
                            getModelExpressionMap(expressionModelId));
                    fieldcalculate = vo.getIndicatorValue();
                    BigDecimal b3 = fieldcalculate;
                    if(null == b3) {
                        continue;
                    }
                    String[] bEle = {b1.toString(),b2.toString(),b3.toString()};
                    AvgOperator avgOp = new AvgOperator();
                    StdOperator stdOp = new StdOperator();
                    BigDecimal std = new BigDecimal(stdOp.executeInner(bEle).toString());
                    BigDecimal avg = new BigDecimal(avgOp.executeInner(bEle).toString());
                    fieldcalculate = std.divide(avg,4, BigDecimal.ROUND_HALF_UP);//防止除不尽
                    if (null != fieldcalculate) {
                        setFieldValue(nFinBean, field, fieldcalculate.toString());// 设置指标值
                    }
                }
                /**
                 * std((if(pp3(BS001) > 0) {2 * pp2(PL101) / (pp2(BS001) + pp3(BS001))} else{pp2(PL101) / pp2(BS001)}),(if(pp2(BS001) > 0) {2 * pp(PL101) / (pp(BS001) + pp2(BS001))} else{pp(PL101) / pp(BS001)}),(if(pp(BS001) > 0) {2 * PL101 / (BS001 + pp(BS001))} else{PL101 / BS001}))/
                 * avg((if(pp3(BS001) > 0) {2 * pp2(PL101) / (pp2(BS001) + pp3(BS001))} else{pp2(PL101) / pp2(BS001)}),(if(pp2(BS001) > 0) {2 * pp(PL101) / (pp(BS001) + pp2(BS001))} else{pp(PL101) / pp(BS001)}),(if(pp(BS001) > 0) {2 * PL101 / (BS001 + pp(BS001))} else{PL101 / BS001}))    
                 */
                else if("ratio5".equalsIgnoreCase(field)) {
                    expression.setExpressFormat("if(pp3(BS001) > 0) {2 * pp2(PL101) / (pp2(BS001) + pp3(BS001))} else{pp2(PL101) / pp2(BS001)}".toLowerCase());
                    IndicatorVo vo = calculateService.calculateFinSheet(sheetData, expression, finDate,
                            getModelExpressionMap(expressionModelId));
                    fieldcalculate = vo.getIndicatorValue();
                    BigDecimal b1 = fieldcalculate;
                    if(null == b1) {
                        continue;
                    }
                    expression.setExpressFormat("if(pp2(BS001) > 0) {2 * pp(PL101) / (pp(BS001) + pp2(BS001))} else{pp(PL101) / pp(BS001)}".toLowerCase());
                    vo = calculateService.calculateFinSheet(sheetData, expression, finDate,
                            getModelExpressionMap(expressionModelId));
                    fieldcalculate = vo.getIndicatorValue();
                    BigDecimal b2 = fieldcalculate;
                    if(null == b2) {
                        continue;
                    }
                    expression.setExpressFormat("if(pp(BS001) > 0) {2 * PL101 / (BS001 + pp(BS001))} else{PL101 / BS001}".toLowerCase());
                    vo = calculateService.calculateFinSheet(sheetData, expression, finDate,
                            getModelExpressionMap(expressionModelId));
                    fieldcalculate = vo.getIndicatorValue();
                    BigDecimal b3 = fieldcalculate;
                    if(null == b3) {
                        continue;
                    }
                    String[] bEle = {b1.toString(),b2.toString(),b3.toString()};
                    AvgOperator avgOp = new AvgOperator();
                    StdOperator stdOp = new StdOperator();
                    BigDecimal std = new BigDecimal(stdOp.executeInner(bEle).toString());
                    BigDecimal avg = new BigDecimal(avgOp.executeInner(bEle).toString());
                    fieldcalculate = std.divide(avg,4, BigDecimal.ROUND_HALF_UP);//防止除不尽
                    if (null != fieldcalculate) {
                        setFieldValue(nFinBean, field, fieldcalculate.toString());// 设置指标值
                    }
                } else {
                    IndicatorVo vo = calculateService.calculateFinSheet(sheetData, expression, finDate,
                            getModelExpressionMap(expressionModelId));
                    fieldcalculate = vo.getIndicatorValue();
                    if (null != fieldcalculate) {
                        setFieldValue(nFinBean, field, fieldcalculate.toString());// 设置指标值
                    }
                }
            } catch(Exception e) {
                log.error("calculdteOperator quality error：  com_uni_code=" + err.getComUniCode() +" fin_date=" + 
                        DateUtils.convert2String(finDate,DateUtils.YYYY_MM_DD)+" ratio=" + field +
                        e.getMessage());
              //判断异常消息是否为空 ，RuntimeException异常getMessage()为空
                String errorRemark = e.getMessage();
                BondCalculateErrorLog errLog = new BondCalculateErrorLog();
                errLog.setAmaComId(err.getAmaComId());
                errLog.setComUniCode(err.getComUniCode());
                errLog.setCreateTime(err.getCreateTime());
                errLog.setErrorRemark(errorRemark);
                errLog.setErrorType("质量指标计算错误");
                errLog.setFinDate(finDate);
                errLog.setLastUpdateTime(DateUtils.convertSqlTime(err.getLastUpdateTime()));
                errLog.setTableName(err.getTableName());
                errLog.setExpressFormat(expressFormat);
                errLog.setRatio(field);
                errorLogList.add(errLog);
            }
        }
        /**
         * 非金融：调用计算财务质量得分接口
         */
        String callResult = "计算成功";
        try {
            ResponseData response = getQuanData(nFinBean, token);// 可以判断调用结果
            callResult = response.getResponseMessage();
        } catch (Exception e) {
            callResult = e.getMessage();
        }
        nFinBean.setRemark(callResult);
        bondCreditRatingList.add(nFinBean);
    }

    /**
     * 
     * getRatingDataResult:(获取评分评级接口json结果)
     * 
     * @param @param
     *            jsonFilter
     * @param @return
     *            设定文件
     * @return String DOM对象
     * @since CodingExample Ver 1.1
     */
    public ResponseData getRatingDataResult(BondCreditRatingJson jsonFilter) {
        BondCreditRating bean = new BondCreditRating();
        bean.setModelId(jsonFilter.getModelId());
        if (null != jsonFilter.getRatio1()) {
            bean.setRatio1(jsonFilter.getRatio1());
        }
        if (null != jsonFilter.getRatio2()) {
            bean.setRatio2(jsonFilter.getRatio2());
        }
        if (null != jsonFilter.getRatio3()) {
            bean.setRatio3(jsonFilter.getRatio3());
        }
        if (null != jsonFilter.getRatio4()) {
            bean.setRatio4(jsonFilter.getRatio4());
        }
        if (null != jsonFilter.getRatio5()) {
            bean.setRatio5(jsonFilter.getRatio5());
        }
        if (null != jsonFilter.getRatio6()) {
            bean.setRatio6(jsonFilter.getRatio6());
        }
        if (null != jsonFilter.getRatio7()) {
            bean.setRatio7(jsonFilter.getRatio7());
        }
        if (null != jsonFilter.getRatio8()) {
            bean.setRatio8(jsonFilter.getRatio8());
        }
        if (null != jsonFilter.getRatio9()) {
            bean.setRatio9(jsonFilter.getRatio9());
        }
        if (null != jsonFilter.getRatio10()) {
            bean.setRatio10(jsonFilter.getRatio10());
        }
        ResponseData result = null;
        try {
            result = getRatingData(bean, getToken());
        } catch (Exception e) {
            log.error("getRatingDataResult error :" + e.getMessage());
            result.setResponseCode(CommonConstant.DMCallbackCode.SYSTEM_INTERNAL_ERROR.getCode());
            result.setResponseMessage(CommonConstant.DMCallbackCode.SYSTEM_INTERNAL_ERROR.getCodeMessage() + ": " + e.getMessage());
        }
        return result;
    }

    /**
     * 
     * getQuanDataResult:(获取质量评分json结果)
     * 
     * @param @param
     *            jsonFilter
     * @param @return
     *            设定文件
     * @return String DOM对象
     * @throws @since
     *             CodingExample Ver 1.1
     */
    public ResponseData getQuanDataResult(BondCreditRatingJson jsonFilter) {
        BondCreditRating bean = new BondCreditRating();
        bean.setModelId(jsonFilter.getModelId());
        if (null != jsonFilter.getRatio1()) {
            bean.setRatio1(jsonFilter.getRatio1());
        }
        if (null != jsonFilter.getRatio2()) {
            bean.setRatio2(jsonFilter.getRatio2());
        }
        if (null != jsonFilter.getRatio3()) {
            bean.setRatio3(jsonFilter.getRatio3());
        }
        if (null != jsonFilter.getRatio4()) {
            bean.setRatio4(jsonFilter.getRatio4());
        }
        if (null != jsonFilter.getRatio5()) {
            bean.setRatio5(jsonFilter.getRatio5());
        }
        if (null != jsonFilter.getRatio6()) {
            bean.setRatio6(jsonFilter.getRatio6());
        }
        if (null != jsonFilter.getRatio7()) {
            bean.setRatio7(jsonFilter.getRatio7());
        }
        if (null != jsonFilter.getRatio8()) {
            bean.setRatio8(jsonFilter.getRatio8());
        }
        if (null != jsonFilter.getRatio9()) {
            bean.setRatio9(jsonFilter.getRatio9());
        }
        if (null != jsonFilter.getRatio10()) {
            bean.setRatio10(jsonFilter.getRatio10());
        }
        ResponseData result = null;
        try {
            result = getQuanData(bean, getToken());
        } catch (Exception e) {
            log.error("getQuanDataResult error :" + e.getMessage());
            result.setResponseCode(CommonConstant.DMCallbackCode.SYSTEM_INTERNAL_ERROR.getCode());
            result.setResponseMessage(CommonConstant.DMCallbackCode.SYSTEM_INTERNAL_ERROR.getCodeMessage() + ": " + e.getMessage());
        }
        return result;
    }

    /**
     * 
     * getRatingData:(调用安硕接口，获取财报指标评分和风险评级)
     * 
     * @param @param
     *            bean
     * @param @param
     *            token
     * @param @return
     *            设定文件
     * @return String DOM对象
     * @throws @since
     *             CodingExample Ver 1.1
     */
    private ResponseData getRatingData(BondCreditRating bean, String token) throws Exception {
        ResponseData result = new ResponseData();
        String url = appConfig.getAmaresunRatingUrl();
        String userId = appConfig.getAmaresunUserId();
        String modelId = bean.getModelId();
        String requiredModel = "";// 模型名称
        String finalRating = "";// 字母评级
        // 封装请求参数：restTemplate要求为MultiValueMap
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("userId", userId);
        paramMap.add("token", token);
        JSONObject ratioJson = new JSONObject();
        boolean hasNullValue= false;
        StringBuilder nullValuePrompt = new StringBuilder();
        if (CommonConstant.ISSUER_TYPE_BANK.equals(modelId)) {// 银行模型7个指标：RATIO1-RATIO7
            if(bean.getRatio1() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio1为空，");
            }
            if(bean.getRatio2() == null) { 
                hasNullValue = true;
                nullValuePrompt.append("ratio2为空，");
            } 
            if(bean.getRatio3() == null) {
                hasNullValue = true; 
                nullValuePrompt.append("ratio3为空，");
            }
            if(bean.getRatio4() == null ) {
                hasNullValue = true;
                nullValuePrompt.append("ratio4为空，");
            }  
            if(bean.getRatio5() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio5为空，");
            }  
            if(bean.getRatio6() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio6为空，");
            } 
            if(bean.getRatio7() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio7为空，");
            }
            if(!hasNullValue) {
                requiredModel = CommonConstant.MODEL_NAME_BANK;
                ratioJson.put("requiredModel", requiredModel);
                ratioJson.put("ratio1", bean.getRatio1() == null ? "" : bean.getRatio1());// null属性值会被去掉，所以要处理
                ratioJson.put("ratio2", bean.getRatio2() == null ? "" : bean.getRatio2());
                ratioJson.put("ratio3", bean.getRatio3() == null ? "" : bean.getRatio3());
                ratioJson.put("ratio4", bean.getRatio4() == null ? "" : bean.getRatio4());
                ratioJson.put("ratio5", bean.getRatio5() == null ? "" : bean.getRatio5());
                ratioJson.put("ratio6", bean.getRatio6() == null ? "" : bean.getRatio6());
                ratioJson.put("ratio7", bean.getRatio7() == null ? "" : bean.getRatio7());
            }
        } else if (CommonConstant.ISSUER_TYPE_INDU.equals(modelId)) {// 工业模型10个指标：RATIO1-RATIO10
            if(bean.getRatio1() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio1为空，");
            }
            if(bean.getRatio2() == null) { 
                hasNullValue = true;
                nullValuePrompt.append("ratio2为空，");
            } 
            if(bean.getRatio3() == null) {
                hasNullValue = true; 
                nullValuePrompt.append("ratio3为空，");
            }
            if(bean.getRatio4() == null ) {
                hasNullValue = true;
                nullValuePrompt.append("ratio4为空，");
            }  
            if(bean.getRatio5() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio5为空，");
            }  
            if(bean.getRatio6() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio6为空，");
            } 
            if(bean.getRatio7() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio7为空，");
            }
            if(bean.getRatio8() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio8为空，");
            }  
            if(bean.getRatio9() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio9为空，");
            } 
            if(bean.getRatio10() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio10为空，");
            }
            if(!hasNullValue) {
                requiredModel = CommonConstant.MODEL_NAME_INDU;
                ratioJson.put("requiredModel", requiredModel);
                ratioJson.put("ratio1", bean.getRatio1() == null ? "" : bean.getRatio1());
                ratioJson.put("ratio2", bean.getRatio2() == null ? "" : bean.getRatio2());
                ratioJson.put("ratio3", bean.getRatio3() == null ? "" : bean.getRatio3());
                ratioJson.put("ratio4", bean.getRatio4() == null ? "" : bean.getRatio4());
                ratioJson.put("ratio5", bean.getRatio5() == null ? "" : bean.getRatio5());
                ratioJson.put("ratio6", bean.getRatio6() == null ? "" : bean.getRatio6());
                ratioJson.put("ratio7", bean.getRatio7() == null ? "" : bean.getRatio7());
                ratioJson.put("ratio8", bean.getRatio8() == null ? "" : bean.getRatio8());
                ratioJson.put("ratio9", bean.getRatio9() == null ? "" : bean.getRatio9());
                ratioJson.put("ratio10", bean.getRatio10() == null ? "" : bean.getRatio10());
            }
        } else if (CommonConstant.ISSUER_TYPE_BUSIN.equals(modelId)) {// 商业模型8个指标：RATIO1-RATIO8
            if(bean.getRatio1() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio1为空，");
            }
            if(bean.getRatio2() == null) { 
                hasNullValue = true;
                nullValuePrompt.append("ratio2为空，");
            } 
            if(bean.getRatio3() == null) {
                hasNullValue = true; 
                nullValuePrompt.append("ratio3为空，");
            }
            if(bean.getRatio4() == null ) {
                hasNullValue = true;
                nullValuePrompt.append("ratio4为空，");
            }  
            if(bean.getRatio5() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio5为空，");
            }  
            if(bean.getRatio6() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio6为空，");
            } 
            if(bean.getRatio7() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio7为空，");
            }
            if(bean.getRatio8() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio8为空，");
            }  
            if(!hasNullValue) {
                requiredModel = CommonConstant.MODEL_NAME_BUSIN;
                ratioJson.put("requiredModel", requiredModel);
                ratioJson.put("ratio1", bean.getRatio1() == null ? "" : bean.getRatio1());
                ratioJson.put("ratio2", bean.getRatio2() == null ? "" : bean.getRatio2());
                ratioJson.put("ratio3", bean.getRatio3() == null ? "" : bean.getRatio3());
                ratioJson.put("ratio4", bean.getRatio4() == null ? "" : bean.getRatio4());
                ratioJson.put("ratio5", bean.getRatio5() == null ? "" : bean.getRatio5());
                ratioJson.put("ratio6", bean.getRatio6() == null ? "" : bean.getRatio6());
                ratioJson.put("ratio7", bean.getRatio7() == null ? "" : bean.getRatio7());
                ratioJson.put("ratio8", bean.getRatio8() == null ? "" : bean.getRatio8());
            }
        } else if (CommonConstant.ISSUER_TYPE_ESTATE.equals(modelId)) {// 房地产模型9个指标：RATIO1-RATIO9
            if(bean.getRatio1() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio1为空，");
            }
            if(bean.getRatio2() == null) { 
                hasNullValue = true;
                nullValuePrompt.append("ratio2为空，");
            } 
            if(bean.getRatio3() == null) {
                hasNullValue = true; 
                nullValuePrompt.append("ratio3为空，");
            }
            if(bean.getRatio4() == null ) {
                hasNullValue = true;
                nullValuePrompt.append("ratio4为空，");
            }  
            if(bean.getRatio5() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio5为空，");
            }  
            if(bean.getRatio6() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio6为空，");
            } 
            if(bean.getRatio7() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio7为空，");
            }
            if(bean.getRatio8() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio8为空，");
            }  
            if(bean.getRatio9() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio9为空，");
            } 
            if(!hasNullValue) {
                requiredModel = CommonConstant.MODEL_NAME_ESTATE;
                ratioJson.put("requiredModel", requiredModel);
                ratioJson.put("ratio1", bean.getRatio1() == null ? "" : bean.getRatio1());
                ratioJson.put("ratio2", bean.getRatio2() == null ? "" : bean.getRatio2());
                ratioJson.put("ratio3", bean.getRatio3() == null ? "" : bean.getRatio3());
                ratioJson.put("ratio4", bean.getRatio4() == null ? "" : bean.getRatio4());
                ratioJson.put("ratio5", bean.getRatio5() == null ? "" : bean.getRatio5());
                ratioJson.put("ratio6", bean.getRatio6() == null ? "" : bean.getRatio6());
                ratioJson.put("ratio7", bean.getRatio7() == null ? "" : bean.getRatio7());
                ratioJson.put("ratio8", bean.getRatio8() == null ? "" : bean.getRatio8());
                ratioJson.put("ratio9", bean.getRatio9() == null ? "" : bean.getRatio9());
            }
        } else if (CommonConstant.ISSUER_TYPE_SECU.equals(modelId)) {// 证券模型7个指标：RATIO1-RATIO7
            if(bean.getRatio1() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio1为空，");
            }
            if(bean.getRatio2() == null) { 
                hasNullValue = true;
                nullValuePrompt.append("ratio2为空，");
            } 
            if(bean.getRatio3() == null) {
                hasNullValue = true; 
                nullValuePrompt.append("ratio3为空，");
            }
            if(bean.getRatio4() == null ) {
                hasNullValue = true;
                nullValuePrompt.append("ratio4为空，");
            }  
            if(bean.getRatio5() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio5为空，");
            }  
            if(bean.getRatio6() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio6为空，");
            } 
            if(bean.getRatio7() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio7为空，");
            } 
            if(!hasNullValue) {
                requiredModel = CommonConstant.MODEL_NAME_SECU;
                ratioJson.put("requiredModel", requiredModel);
                ratioJson.put("ratio1", bean.getRatio1() == null ? "" : bean.getRatio1());
                ratioJson.put("ratio2", bean.getRatio2() == null ? "" : bean.getRatio2());
                ratioJson.put("ratio3", bean.getRatio3() == null ? "" : bean.getRatio3());
                ratioJson.put("ratio4", bean.getRatio4() == null ? "" : bean.getRatio4());
                ratioJson.put("ratio5", bean.getRatio5() == null ? "" : bean.getRatio5());
                ratioJson.put("ratio6", bean.getRatio6() == null ? "" : bean.getRatio6());
                ratioJson.put("ratio7", bean.getRatio7() == null ? "" : bean.getRatio7());
            }
        } else if (CommonConstant.ISSUER_TYPE_INSU.equals(modelId)) {// 保险模型8个指标：RATIO1-RATIO8
            if(bean.getRatio1() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio1为空，");
            }
            if(bean.getRatio2() == null) { 
                hasNullValue = true;
                nullValuePrompt.append("ratio2为空，");
            } 
            if(bean.getRatio3() == null) {
                hasNullValue = true; 
                nullValuePrompt.append("ratio3为空，");
            }
            if(bean.getRatio4() == null ) {
                hasNullValue = true;
                nullValuePrompt.append("ratio4为空，");
            }  
            if(bean.getRatio5() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio5为空，");
            }  
            if(bean.getRatio6() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio6为空，");
            } 
            if(bean.getRatio7() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio7为空，");
            }
            if(bean.getRatio8() == null) {
                hasNullValue = true;
                nullValuePrompt.append("ratio8为空，");
            }  
            if(!hasNullValue) {
                requiredModel = CommonConstant.MODEL_NAME_INSU;
                ratioJson.put("requiredModel", requiredModel);
                ratioJson.put("ratio1", bean.getRatio1() == null ? "" : bean.getRatio1());
                ratioJson.put("ratio2", bean.getRatio2() == null ? "" : bean.getRatio2());
                ratioJson.put("ratio3", bean.getRatio3() == null ? "" : bean.getRatio3());
                ratioJson.put("ratio4", bean.getRatio4() == null ? "" : bean.getRatio4());
                ratioJson.put("ratio5", bean.getRatio5() == null ? "" : bean.getRatio5());
                ratioJson.put("ratio6", bean.getRatio6() == null ? "" : bean.getRatio6());
                ratioJson.put("ratio7", bean.getRatio7() == null ? "" : bean.getRatio7());
                ratioJson.put("ratio8", bean.getRatio8() == null ? "" : bean.getRatio8());
            }
        }
        if(hasNullValue) {//有空值不调用安硕接口
            result.setResponseCode(CommonConstant.DMCallbackCode.SYSTEM_INTERNAL_ERROR.getCode());
            result.setResponseMessage(nullValuePrompt.toString()+" 安硕无法计算评级，不调用接口");
            result.setResponseData("");
            return result;
        }
        paramMap.add("ratioJson", ratioJson.toJSONString());// 要转换为JSONString
        JSONObject resultJson = restTemplate.postForEntity(url, paramMap, JSONObject.class).getBody();
        String responseCode = resultJson.getString("responseCode");
        result.setResponseCode(responseCode);
        result.setResponseMessage(resultJson.getString("responseMessage"));
        result.setResponseData(resultJson.toString());
        JSONObject ratingData = JSONObject.parseObject(resultJson.getString("ratingData"));
        if (!StringUtils.isEmpty(responseCode) && CommonConstant.AmaresunCallbackCode.NOMAL_RETURN.test(responseCode)) {
            finalRating = ratingData.getString("finalRating");
            bean.setRating(finalRating);
            if (CommonConstant.ISSUER_TYPE_BANK.equals(modelId)) {
                bean.setRatio1Score(new BigDecimal(ratingData.getString("ratio1Score")));
                bean.setRatio2Score(new BigDecimal(ratingData.getString("ratio2Score")));
                bean.setRatio3Score(new BigDecimal(ratingData.getString("ratio3Score")));
                bean.setRatio4Score(new BigDecimal(ratingData.getString("ratio4Score")));
                bean.setRatio5Score(new BigDecimal(ratingData.getString("ratio5Score")));
                bean.setRatio6Score(new BigDecimal(ratingData.getString("ratio6Score")));
                bean.setRatio7Score(new BigDecimal(ratingData.getString("ratio7Score")));
            } else if (CommonConstant.ISSUER_TYPE_INDU.equals(modelId)) {
                bean.setRatio1Score(new BigDecimal(ratingData.getString("ratio1Score")));
                bean.setRatio2Score(new BigDecimal(ratingData.getString("ratio2Score")));
                bean.setRatio3Score(new BigDecimal(ratingData.getString("ratio3Score")));
                bean.setRatio4Score(new BigDecimal(ratingData.getString("ratio4Score")));
                bean.setRatio5Score(new BigDecimal(ratingData.getString("ratio5Score")));
                bean.setRatio6Score(new BigDecimal(ratingData.getString("ratio6Score")));
                bean.setRatio7Score(new BigDecimal(ratingData.getString("ratio7Score")));
                bean.setRatio8Score(new BigDecimal(ratingData.getString("ratio8Score")));
                bean.setRatio9Score(new BigDecimal(ratingData.getString("ratio9Score")));
                bean.setRatio10Score(new BigDecimal(ratingData.getString("ratio10Score")));
            } else if (CommonConstant.ISSUER_TYPE_BUSIN.equals(modelId)) {
                bean.setRatio1Score(new BigDecimal(ratingData.getString("ratio1Score")));
                bean.setRatio2Score(new BigDecimal(ratingData.getString("ratio2Score")));
                bean.setRatio3Score(new BigDecimal(ratingData.getString("ratio3Score")));
                bean.setRatio4Score(new BigDecimal(ratingData.getString("ratio4Score")));
                bean.setRatio5Score(new BigDecimal(ratingData.getString("ratio5Score")));
                bean.setRatio6Score(new BigDecimal(ratingData.getString("ratio6Score")));
                bean.setRatio7Score(new BigDecimal(ratingData.getString("ratio7Score")));
                bean.setRatio8Score(new BigDecimal(ratingData.getString("ratio8Score")));
            } else if (CommonConstant.ISSUER_TYPE_ESTATE.equals(modelId)) {
                bean.setRatio1Score(new BigDecimal(ratingData.getString("ratio1Score")));
                bean.setRatio2Score(new BigDecimal(ratingData.getString("ratio2Score")));
                bean.setRatio3Score(new BigDecimal(ratingData.getString("ratio3Score")));
                bean.setRatio4Score(new BigDecimal(ratingData.getString("ratio4Score")));
                bean.setRatio5Score(new BigDecimal(ratingData.getString("ratio5Score")));
                bean.setRatio6Score(new BigDecimal(ratingData.getString("ratio6Score")));
                bean.setRatio7Score(new BigDecimal(ratingData.getString("ratio7Score")));
                bean.setRatio8Score(new BigDecimal(ratingData.getString("ratio8Score")));
                bean.setRatio9Score(new BigDecimal(ratingData.getString("ratio9Score")));
            } else if (CommonConstant.ISSUER_TYPE_SECU.equals(modelId)) {
                bean.setRatio1Score(new BigDecimal(ratingData.getString("ratio1Score")));
                bean.setRatio2Score(new BigDecimal(ratingData.getString("ratio2Score")));
                bean.setRatio3Score(new BigDecimal(ratingData.getString("ratio3Score")));
                bean.setRatio4Score(new BigDecimal(ratingData.getString("ratio4Score")));
                bean.setRatio5Score(new BigDecimal(ratingData.getString("ratio5Score")));
                bean.setRatio6Score(new BigDecimal(ratingData.getString("ratio6Score")));
                bean.setRatio7Score(new BigDecimal(ratingData.getString("ratio7Score")));
            } else if (CommonConstant.ISSUER_TYPE_INSU.equals(modelId)) {
                bean.setRatio1Score(new BigDecimal(ratingData.getString("ratio1Score")));
                bean.setRatio2Score(new BigDecimal(ratingData.getString("ratio2Score")));
                bean.setRatio3Score(new BigDecimal(ratingData.getString("ratio3Score")));
                bean.setRatio4Score(new BigDecimal(ratingData.getString("ratio4Score")));
                bean.setRatio5Score(new BigDecimal(ratingData.getString("ratio5Score")));
                bean.setRatio6Score(new BigDecimal(ratingData.getString("ratio6Score")));
                bean.setRatio7Score(new BigDecimal(ratingData.getString("ratio7Score")));
                bean.setRatio8Score(new BigDecimal(ratingData.getString("ratio8Score")));
            }
        } else {
            log.error("getRatingData error :" + resultJson.getString("responseMessage"));
            throw new Exception(resultJson.getString("responseMessage"));
        }
        return result;
    }

    /**
     * 
     * getQuanData:(调用安硕接口，获取财务质量分析评分)
     * 
     * @param @param
     *            bean
     * @param @param
     *            token
     * @param @return
     *            设定文件
     * @return String DOM对象
     * @throws @since
     *             CodingExample Ver 1.1
     */
    private ResponseData getQuanData(BondCreditRating bean, String token) throws Exception {
        ResponseData result = new ResponseData();
        boolean hasNullValue= false;
        StringBuilder nullValuePrompt = new StringBuilder();
        if(bean.getRatio1() == null) {
            hasNullValue = true;
            nullValuePrompt.append("ratio1为空，");
        }
        if(bean.getRatio2() == null) { 
            hasNullValue = true;
            nullValuePrompt.append("ratio2为空，");
        } 
        if(bean.getRatio3() == null) {
            hasNullValue = true; 
            nullValuePrompt.append("ratio3为空，");
        }
        if(bean.getRatio4() == null ) {
            hasNullValue = true;
            nullValuePrompt.append("ratio4为空，");
        }  
        if(bean.getRatio5() == null) {
            hasNullValue = true;
            nullValuePrompt.append("ratio5为空，");
        }  
        if(bean.getRatio6() == null) {
            hasNullValue = true;
            nullValuePrompt.append("ratio6为空，");
        } 
        if(hasNullValue) {//有空值不调用安硕接口
            result.setResponseCode(CommonConstant.DMCallbackCode.SYSTEM_INTERNAL_ERROR.getCode());
            result.setResponseMessage(nullValuePrompt.toString()+" 安硕无法计算财务质量分析，不调用接口");
            result.setResponseData("");
            return result;
        }
        String url = appConfig.getAmaresunQualityUrl();
        String userId = appConfig.getAmaresunUserId();
        String finQualityScore = "";// 财务质量分析评分
        // 封装请求参数：restTemplate要求为MultiValueMap
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("userId", userId);
        paramMap.add("token", token);
        JSONObject ratioJson = new JSONObject();
        ratioJson.put("ratio1", bean.getRatio1() == null ? "" : bean.getRatio1());// null属性值会被去掉，所以要处理
        ratioJson.put("ratio2", bean.getRatio2() == null ? "" : bean.getRatio2());
        ratioJson.put("ratio3", bean.getRatio3() == null ? "" : bean.getRatio3());
        ratioJson.put("ratio4", bean.getRatio4() == null ? "" : bean.getRatio4());
        ratioJson.put("ratio5", bean.getRatio5() == null ? "" : bean.getRatio5());
        ratioJson.put("ratio6", bean.getRatio6() == null ? "" : bean.getRatio6());
        paramMap.add("ratioJson", ratioJson.toJSONString());// 要转换为JSONString
        JSONObject resultJson = restTemplate.postForObject(url, paramMap, JSONObject.class);
        String responseCode = resultJson.getString("responseCode");
        result.setResponseCode(responseCode);
        result.setResponseMessage(resultJson.getString("responseMessage"));
        result.setResponseData(resultJson.toString());
        JSONObject ratingData = JSONObject.parseObject(resultJson.getString("ratingData"));
        if (!StringUtils.isEmpty(responseCode) && CommonConstant.AmaresunCallbackCode.NOMAL_RETURN.test(responseCode)) {
            finQualityScore = ratingData.getString("finQualityScore");
            bean.setFinQualityScore(new BigDecimal(finQualityScore));
        } else {
            log.error("getQuanData error :" + resultJson.getString("responseMessage"));
            throw new Exception(resultJson.getString("responseMessage"));
        }
        return result;
    }

    /**
     * 
     * getAuth:(调用安硕接口，获取评级接口、财报质量分析接口令牌)
     * 
     * @param @return
     *            设定文件
     * @return String DOM对象
     * @throws @since
     *             CodingExample Ver 1.1
     */
    public ResponseData getAuth() {
        ResponseData result = new ResponseData();
        String url = appConfig.getAmaresunAuthUrl();
        String userId = appConfig.getAmaresunUserId();
        String userName = appConfig.getAmaresunUserName();
        String password = appConfig.getAmaresunPassword();
        Map<String, String> sortMap = new HashMap<>();
        sortMap.put("userId", userId);
        sortMap.put("userName", userName);
        sortMap.put("password", password);
        String paramResult = SafeUtils.formatUrlMap(sortMap, false, false);
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());

        try {
            String sign = MD5Utils.Bit32(paramResult);// 签名
            MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
            paramMap.add("userId", userId);
            paramMap.add("userName", userName);
            paramMap.add("password", password);
            paramMap.add("sign", sign);
            JSONObject resultJson = restTemplate.postForObject(url, paramMap, JSONObject.class);
            String responseCode = resultJson.getString("responseCode");
            if (!StringUtils.isEmpty(responseCode) && CommonConstant.AmaresunCallbackCode.NOMAL_RETURN.test(responseCode)) {
                result.setResponseData(resultJson.getString("token"));
                result.setResponseCode(responseCode);
                result.setResponseMessage(resultJson.getString("responseMessage"));
            } else {
                log.error("getAuth error :" + resultJson.getString("responseMessage"));
                result.setResponseCode(responseCode);
                result.setResponseMessage(resultJson.getString("responseMessage"));
            }
        } catch (Exception e) {
            log.error("getAuth error :" + e.getMessage());
            result.setResponseCode(CommonConstant.DMCallbackCode.SYSTEM_INTERNAL_ERROR.getCode());
            result.setResponseMessage(CommonConstant.DMCallbackCode.SYSTEM_INTERNAL_ERROR.getCodeMessage() + ": " + e.getMessage());
        }
        return result;
    }
    
    /**
     * 
     * getToken:(获取安硕接口令牌)
     * @param  @return    设定文件
     * @return String    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private synchronized String getToken() {
        //读取redis
        String token="";
        String nowDate = DateUtils.getCurrentDate(DateUtils.YYYY_MM_DD);
        if(redisUtil.exists(CommonConstant.SHEET_TOKEN_CACHE)) {
            JSONObject jo = JSONObject.parseObject(String.valueOf(redisUtil.get(CommonConstant.SHEET_TOKEN_CACHE))) ;
            String date = jo.getString("date");
            if(nowDate.equals(date)) {
                token = jo.getString("token");
                if(StringUtils.isEmpty(token)) {//值为空再调用一次
                    redisUtil.remove(CommonConstant.SHEET_TOKEN_CACHE);
                    ResponseData rs = getAuth();// 获取安硕接口令牌
                    token = (String) rs.getResponseData();
                    JSONObject to = new JSONObject();
                    to.put("date", nowDate);
                    to.put("token", token);
                    redisUtil.set(CommonConstant.SHEET_TOKEN_CACHE, to.toJSONString(),SafeUtils.getRestTodayTime());
                }
            } else {
                redisUtil.remove(CommonConstant.SHEET_TOKEN_CACHE);
                ResponseData rs = getAuth();// 获取安硕接口令牌
                token = (String) rs.getResponseData();
                JSONObject to = new JSONObject();
                to.put("date", nowDate);
                to.put("token", token);
                redisUtil.set(CommonConstant.SHEET_TOKEN_CACHE, to.toJSONString(),SafeUtils.getRestTodayTime());
            }
        } else {
            ResponseData rs = getAuth();// 获取安硕接口令牌
            token = (String) rs.getResponseData();
            JSONObject to = new JSONObject();
            to.put("date", nowDate);
            to.put("token", token);
            redisUtil.set(CommonConstant.SHEET_TOKEN_CACHE, to.toJSONString(),SafeUtils.getRestTodayTime());
        }
        return token;
    }
    
    /**
     * 
     * splitList:(分割数组为几个小数组)
     * @param  @param list
     * @param  @param size
     * @param  @return    设定文件
     * @return List<List>    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private  List<List<Long>>  splitList(List<Long> list,int size) {  
        List<List<Long>> listArr = new ArrayList<List<Long>>();  
        //获取被拆分的数组个数  
        int arrSize = list.size()%size==0?list.size()/size:list.size()/size+1;  
        for(int i=0;i<arrSize;i++) {  
            List<Long> sub = new ArrayList<Long>();  
            //把指定索引数据放入到list中  
            for(int j=i*size;j<=size*(i+1)-1;j++) {  
                if(j<=list.size()-1) {  
                    sub.add(list.get(j));  
                }  
            }  
            listArr.add(sub);  
        }  
        return listArr;  
    } 
    
    /**
     * 
     * getClassMap:(获取行业分类Map)
     * @param      设定文件
     * @return void    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private  Map<Long, Map<String, Object>> getClassMap() {
        if(null == classiFicationMap || classiFicationMap.keySet().size()==0) {
            classiFicationMap = bondIndustryClassificationDao.findAllAmaCompidClassification();
        } 
        return classiFicationMap;
    }
    
    /**
     * 
     * getModelExpMap:(获取财报模型指标公式)
     * @param      设定文件
     * @return void    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private  Map<String,List<BondFinanceSheetIndicatorExpression>> getModelExpMap() {
        if(null == sheetExpsMap || sheetExpsMap.keySet().size()==0) {
            sheetExpsMap = bondIndustryClassificationDao.getAllFinanceModelExpression();
        }
        return sheetExpsMap;
    }
    
    /**
     * 
     * getComFinaSheetGroups:(获取财报主体财报期限分组)
     * @param  @param table
     * @param  @param amaComId
     * @param  @return    设定文件
     * @return Map<String,List<Map<String,Object>>>    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private Map<String, List<Map<String, Object>>> getComFinaSheetGroups(List<Map<String,Object>> list){
        Map<String,List<Map<String, Object>>> comFinaSheetGroups = null;
        if(null != list && list.size()>0){
            comFinaSheetGroups = new HashMap<>();
            List<Map<String, Object>> threeList = new ArrayList<>();
            List<Map<String, Object>> sexList = new ArrayList<>();
            List<Map<String, Object>> nineList = new ArrayList<>();
            List<Map<String, Object>> twelveList = new ArrayList<>();
            String amaComId = "";
            for(Map<String, Object> map : list){
                String finPeriod = map.get("FIN_PERIOD").toString();
                amaComId= map.get("COMP_ID").toString();
                if("3".equals(finPeriod)) {
                    threeList.add(map);
                } else if("6".equals(finPeriod)) {
                    sexList.add(map);
                } else if("9".equals(finPeriod)) {
                    nineList.add(map);
                } else if("12".equals(finPeriod)) {
                    twelveList.add(map);
                }
            }
            comFinaSheetGroups.put(amaComId+"_"+"3", threeList);
            comFinaSheetGroups.put(amaComId+"_"+"6", sexList);
            comFinaSheetGroups.put(amaComId+"_"+"9", nineList);
            comFinaSheetGroups.put(amaComId+"_"+"12", twelveList);
        }
        return comFinaSheetGroups;
    }
    
    /**
     * 
     * getModelExpressionMap:(获取模型指标Map)
     * @param  @param expressionModelId
     * @param  @return    设定文件
     * @return Map<String,String>    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private Map<String, String> getModelExpressionMap(String expressionModelId) {
        List<BondFinanceSheetIndicatorExpression> expressionList = getModelExpMap().get(expressionModelId);
        Map<String,String> expressions = new HashMap<>();
        if(null != expressionList && expressionList.size()>0) {
            expressionList.forEach(item -> {
                expressions.put(item.getField(), item.getExpressFormat());
            });
        }
        return expressions;
    }

    /**
     * 
     * setFieldValue:(设置指标值)
     * @param  @param bean
     * @param  @param fieldName
     * @param  @param fieldValue    设定文件
     * @return void    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private void setFieldValue(BondCreditRating bean, String fieldName, String fieldValue) {
        if("ratio1".equalsIgnoreCase(fieldName)) {
            bean.setRatio1(new BigDecimal(fieldValue));
        }
        if("ratio2".equalsIgnoreCase(fieldName)) {
            bean.setRatio2(new BigDecimal(fieldValue));
        }
        if("ratio3".equalsIgnoreCase(fieldName)) {
            bean.setRatio3(new BigDecimal(fieldValue));
        }
        if("ratio4".equalsIgnoreCase(fieldName)) {
            bean.setRatio4(new BigDecimal(fieldValue));
        }
        if("ratio5".equalsIgnoreCase(fieldName)) {
            bean.setRatio5(new BigDecimal(fieldValue));
        }
        if("ratio6".equalsIgnoreCase(fieldName)) {
            bean.setRatio6(new BigDecimal(fieldValue));
        }
        if("ratio7".equalsIgnoreCase(fieldName)) {
            bean.setRatio7(new BigDecimal(fieldValue));
        }
        if("ratio8".equalsIgnoreCase(fieldName)) {
            bean.setRatio8(new BigDecimal(fieldValue));
        }
        if("ratio9".equalsIgnoreCase(fieldName)) {
            bean.setRatio9(new BigDecimal(fieldValue));
        }
        if("ratio10".equalsIgnoreCase(fieldName)) {
            bean.setRatio10(new BigDecimal(fieldValue));
        }
    }
    
    /**
     * 
     * convertAnnual:(将主体某期季报所有同期季报转为年化数值)
     * <p>
     * 确定年化的公式是：本季度数据-去年本季度数据+去年年报数据,
     * 如果上述数据有一个为空，就用去年年报数据
     * </P>
     * @param  @param indicatorSameTerms 同期财报数据（含本期）
     * @param  @param indicatorYearTerms 主体年报数据
     * @param  @param tableName 主体财报表名称
     * @param  @param amaId 主体|发行人id（安硕）
     * @param  @param comUniCode 公司统一编码
     * @param  @return    设定文件
     * @return OriginalData    
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private OriginalData convertAnnual(List<Map<String, Object>> indicatorSameTerms, List<Map<String, Object>> indicatorYearTerms,String tableName,
            Long amaId, Long comUniCode) {
        List<Map<String, Object>> annualList = new ArrayList<Map<String, Object>>();
        Map<String, Map<String, Object>> sameTermsMap = convertSheetMap(indicatorSameTerms);
        Map<String, Map<String, Object>> yearTermsMap =convertSheetMap(indicatorYearTerms);

        List<BondCalculateErrorLog> errorLogList = new ArrayList<>();
        for(Map<String, Object> sheetMap : indicatorSameTerms) {
            String finDate = sheetMap.get("FIN_DATE").toString();//财报日期
            Map<String, Object> annualMap = new HashMap<>();
            BondCalculateErrorLog errLog = new BondCalculateErrorLog();
            StringBuilder errRemark = new StringBuilder();
            boolean converErr = false;//是否有字段不能转换年报
            //BBS101转换年报失败：当前季报数据缺失，上一年同期季报数据缺失，上一年年报数据缺失。\n BBS101_1转换年报失败：当前季报数据缺失，上一年同期季报数据缺失，上一年年报数据缺失。
            for(Entry<String, Object> entry : sheetMap.entrySet()) {
                String key = entry.getKey();
                if(checkFinSheetNotDecimalField(key)) {
                    String ppFinDate = DateUtils.prevYear(finDate, 1);
                    Map<String, Object> ppMap = sameTermsMap.get(ppFinDate);//去年同期财报数据
                    String ppYearFinDate = DateUtils.ppAnnual(finDate, 1);
                    Map<String, Object> ppYearMap = yearTermsMap.get(ppYearFinDate);//去年年报数据
                    /** 如果有值为空则计算为空,则值为上年年报值 */
                    if((null == entry.getValue()) || (null == ppMap || null == ppMap.get(key)) || 
                            (null == ppYearMap || null == ppYearMap.get(key))) {
                        //去年年报数据不为空，则本季度年化数据为去年年报数据
                        if(null != ppYearMap &&  null != ppYearMap.get(key)) {
                            BigDecimal ppYearDecimal = new BigDecimal(ppYearMap.get(key).toString());
                            annualMap.put(key, ppYearDecimal);
                        } else {
                            converErr = true;
                            errRemark.append(key +"  ：");
                            if((null == entry.getValue())){
                                errRemark.append(CommonConstant.CAL_ERROR_CODE_Q_NO).append("，");
                            }
                            if(null == ppMap || null == ppMap.get(key)){
                                errRemark.append(CommonConstant.CAL_ERROR_CODE_PP_Q_NO).append("，");
                            }
                            if(null == ppYearMap || null == ppYearMap.get(key)){
                                errRemark.append(CommonConstant.CAL_ERROR_CODE_PP_Y_NO).append("，");
                            }
                            errRemark.append("。\n");
                            annualMap.put(key, null);
                        }
                    } else {
                        BigDecimal valueDecimal = new BigDecimal(entry.getValue().toString());//当前财报数值
                        BigDecimal ppDecimal = new BigDecimal(ppMap.get(key).toString());//去年同期财报数值
                        BigDecimal ppYearDecimal = new BigDecimal(ppYearMap.get(key).toString());//去年年报数值
                        BigDecimal yearDecimal = new BigDecimal(0);
                        yearDecimal = valueDecimal.subtract(ppDecimal).add(ppYearDecimal);
                        annualMap.put(key, yearDecimal);
                    }
                } else {
                    annualMap.put(entry.getKey(), entry.getValue());
                }
            }
            annualList.add(annualMap);
            if(converErr) {
                Date nowDate = new Date();
                errLog.setAmaComId(amaId);
                errLog.setComUniCode(comUniCode);
                errLog.setCreateTime(DateUtils.convertSqlTime(nowDate));
                errLog.setErrorRemark(errRemark.toString().replaceAll("，。", "。"));
                errLog.setErrorType("年化错误");
                errLog.setFinDate((java.sql.Date) sheetMap.get("FIN_DATE"));
                errLog.setLastUpdateTime(DateUtils.convertSqlTime(nowDate));
                errLog.setTableName(tableName);
                errorLogList.add(errLog);
            }
        }
        bondCalculateErrorLogDao.batchInsertBondCalculateErrorLog(errorLogList);
        return new OriginalData(annualList);
    }
    
    /**
     * 
     * checkFinSheetNotDecimalField:(判断是否是报表非金融字段)
     * @param  @param key
     * @param  @return    设定文件
     * @return boolean    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private  boolean checkFinSheetNotDecimalField(String key) {
        boolean rs = true;
        if("COMP_ID".equalsIgnoreCase(key)) {
            rs = false;
        }
        if("FIN_DATE".equalsIgnoreCase(key)) {
            rs = false;
        }
        if("FIN_ENTITY".equalsIgnoreCase(key)) {
            rs = false;
        }
        if("FIN_STATE_TYPE".equalsIgnoreCase(key)) {
            rs = false;
        }
        if("FIN_PERIOD".equalsIgnoreCase(key)) {
            rs = false;
        }
        if("check_true".equalsIgnoreCase(key)) {
            rs = false;
        }
        if("last_update_timestamp".equalsIgnoreCase(key)) {
            rs = false;
        }
        if("ETL_TIMESTAMP".equalsIgnoreCase(key)) {
            rs = false;
        }
        if("create_time".equalsIgnoreCase(key)) {
            rs = false;
        }
        if("VISIBLE".equalsIgnoreCase(key)) {
            rs = false;
        }
        return rs;
    }

    /**
     * 
     * convertSheetMap:(按日期重组财报数据)
     * @param  @param sheetList
     * @param  @return    设定文件
     * @return Map<String,Map<String,Object>>    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private Map<String, Map<String, Object>> convertSheetMap(List<Map<String, Object>> sheetList) {
        Map<String, Map<String, Object>> itemMap = new LinkedHashMap<String,Map<String,Object>>();
        sheetList.forEach(item -> {
            Map<String, Object> mapNew = new HashMap<String, Object>();
            item.forEach((k,v) -> {
                    if(v != null && v instanceof BigDecimal){
                        mapNew.put(k, new BigDecimal(v.toString()));
                    }else{
                        mapNew.put(k, v);
                    }
            });
            itemMap.put(item.get("FIN_DATE").toString(), mapNew);
        });
        return itemMap;
    }

}
