package com.innodealing.service;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.innodealing.dao.IndicatorDao;
import com.innodealing.datasource.DataSource;
import com.innodealing.datasource.DataSourceFactory;
import com.innodealing.domain.BondFinanceSheetIndicatorExpression;
import com.innodealing.engine.OriginalData;
import com.innodealing.engine.innodealing.CalculateAlibaba;
import com.innodealing.engine.innodealing.DateConvertUtil;
import com.innodealing.util.DateUtils;
import com.innodealing.util.NumberUtils;
import com.innodealing.vo.IndicatorVo;

/**
 * 计算业务
 * @author 赵正来
 *
 */


@Service
public class CalculateService {

	/**
	 * 阿里表达式计算器
	 */
	
	@Autowired private IndicatorDao indicatorDao;
	
	@Autowired private DataSourceFactory dataSourceFactory;
	
	private static final Logger log = LoggerFactory.getLogger(CalculateService.class);
	
	/**
	 * 计算指标
	 * @param indicatorCode 指标代码
	 * @param indicatorName 指标名称
	 * @param expression
	 * @param issuerId
	 * @param finDate
	 * @return
	 * @throws Exception
	 */
	public IndicatorVo calculate(String indicatorCode, String indicatorName, String expression, Long issuerId, Date finDate) throws Exception{
		
		//当期的财务数据
		Map<String, Object> data = null;
		if(finDate != null){
			data = indicatorDao.findIndicatorByIssuerIdAndFinDate(issuerId, finDate);
		}else{
			data = indicatorDao.findNewestIndicatorByIssuerId(issuerId);
		}
		
		List<Map<String, Object>> indicatorItems = new ArrayList<>();
		indicatorItems.add(data);
		
		BigDecimal result = new CalculateAlibaba(new OriginalData(indicatorItems) , getExpression(issuerId)).calculate(data, expression);
		IndicatorVo indicatorVo = new IndicatorVo();
		indicatorVo.setIndicatorCode(indicatorCode);
		indicatorVo.setIndicatorName(indicatorName);
		indicatorVo.setIndicatorValue(result == null ? null : result.setScale(2, BigDecimal.ROUND_HALF_UP));
		return indicatorVo;
	}
	
	/**
	 * 
	 * calculateFinSheet:(计算财报年化指标)
	 * @param  @param sheetData 主体财报数据
	 * @param  @param indicatorxpression 指标公式
	 * @param  @param finDate 财报日期
	 * @param  @param modelExpression 该模型所有指标公式
	 * @param  @return    设定文件
	 * @return IndicatorVo    DOM对象
	 * @throws 
	 * @since  CodingExample　Ver 1.1
	 */
	public IndicatorVo calculateFinSheet(OriginalData sheetData, BondFinanceSheetIndicatorExpression indicatorxpression, Date finDate,
	        Map<String, String> modelExpression) throws Exception{
	    IndicatorVo indicatorVo = new IndicatorVo();
	    String finDateStr = "";
	    BigDecimal result = null;
        try {
            finDateStr = DateUtils.convert2String(finDate, DateUtils.YYYY_MM_DD);
            result = DateConvertUtil.formatterExpressionByTaobao(indicatorxpression.getExpressFormat(), sheetData, modelExpression, 
                    finDateStr);
            if(null == result) {
                throw new Exception("指标计算结果为空");
            } 
            indicatorVo.setIndicatorCode(indicatorxpression.getField());
            indicatorVo.setIndicatorName(indicatorxpression.getFieldName());
            indicatorVo.setIndicatorValue(result == null ? null : result.setScale(4, BigDecimal.ROUND_HALF_UP));
            return indicatorVo;
        } catch(RuntimeException e) { 
            String exceptionName = e.getMessage();
            exceptionName = StringUtils.isEmpty(exceptionName) ? "" : exceptionName;
            String errorReason = getNullAndZeroString(sheetData,finDateStr,indicatorxpression.getExpressDescription());
            if(StringUtils.isEmpty(exceptionName) && StringUtils.isEmpty(errorReason)) {
                errorReason = "";
            } else if(StringUtils.isEmpty(exceptionName)) {
                errorReason = "" + errorReason;
            } else {
                errorReason = StringUtils.isEmpty(errorReason) ? exceptionName+"。" : exceptionName+"。\n"+errorReason;
            }
            throw new Exception(errorReason);
        }catch(Exception e) {
            String exceptionName = e.getMessage();
            String errorReason = getNullAndZeroString(sheetData,finDateStr,indicatorxpression.getExpressDescription());
            if(StringUtils.isEmpty(exceptionName) && StringUtils.isEmpty(errorReason)) {
                errorReason = "";
            } else if(StringUtils.isEmpty(exceptionName)) {
                errorReason = "" + errorReason;
            } else {
                errorReason = StringUtils.isEmpty(errorReason) ? exceptionName+"。" : exceptionName+"。\n"+errorReason;
            }
            throw new Exception(errorReason);
        }
    }
	
    /**
	 * 获取主体的表达式
	 * @param issuerId
	 * @return
	 */
	public Map<String, String> getExpression(Long issuerId){
		String type = indicatorDao.findIssuerType(issuerId);
		DataSource dataSource = dataSourceFactory.createData(type);
		return dataSource.getExpressions();
	}
	
    /**
     * 
     * getNullAndZeroString:(获取算式出错可能原因：字段空值和0值字符串)
     * @param  @param od 财报数据
     * @param  @param finDate
     * @param  @param includeColumns 计算表达式需要用到的字段值，以“,”分隔
     * @param  @return    设定文件
     * @return String    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private String getNullAndZeroString(OriginalData od, String finDate, String includeColumns) {
        if(null==od || StringUtils.isEmpty(finDate)) {
            return "";
        }
        //当前季度数据
        Map<String,Object> currentDatas = od.getIndicatorItems().get(finDate);
        //上年同期财报数据
        String ppFinDate = DateUtils.prevYear(finDate, 1);
        Map<String,Object> ppDatas = od.getIndicatorItems().get(ppFinDate);
        //上2年同期财报数据
        String pp2FinDate = DateUtils.prevYear(finDate, 2);
        Map<String,Object> pp2Datas = od.getIndicatorItems().get(pp2FinDate);
        //上3年同期财报数据
        String pp3FinDate = DateUtils.prevYear(finDate, 3);
        Map<String,Object> pp3Datas = od.getIndicatorItems().get(pp3FinDate);
       
        StringBuilder sb = new StringBuilder();
        if(!StringUtils.isEmpty(includeColumns)) {
            String[] columns = includeColumns.split(",");
            if(null==currentDatas || currentDatas.keySet().size()==0) {
                sb.append(finDate).append("财报没有。\n");
            } else {
                StringBuilder sub = new StringBuilder();
                for(String col : columns) {
                    if(StringUtils.isEmpty(col)) {
                        continue;
                    }
                    if(null == currentDatas.get(col)) {
                        sub.append("    ").append(col).append(" 为空。\n"); 
                    } else if(currentDatas.get(col) instanceof Number && NumberUtils.comparison(currentDatas.get(col).toString(), "0")==0) {
                        sub.append("    ").append(col).append(" 为0。\n");
                    }
                }
                if(!StringUtils.isEmpty(sub.toString())) {
                    String fStr = finDate + ":\n";
                    sb.append(fStr).append(sub.toString());
                }
            }
            if(null==ppDatas || ppDatas.keySet().size()==0) {
                sb.append(ppFinDate).append("财报没有。\n"); 
            } else {
                StringBuilder sub = new StringBuilder();
                for(String col : columns) {
                    if(StringUtils.isEmpty(col)) {
                        continue;
                    }
                    if(null == ppDatas.get(col)) {
                        sub.append("    ").append(col).append(" 为空。\n"); 
                    } else if(ppDatas.get(col) instanceof Number && NumberUtils.comparison(ppDatas.get(col).toString(), "0")==0) {
                        sub.append("    ").append(col).append(" 为0。\n");
                    }
                }
                if(!StringUtils.isEmpty(sub.toString())) {
                    String fStr = ppFinDate + ":\n";
                    sb.append(fStr).append(sub.toString());
                }
            }
            if(null==pp2Datas || pp2Datas.keySet().size()==0) {
                sb.append(pp2FinDate).append("财报没有。\n");
            } else {
                StringBuilder sub = new StringBuilder();
                for(String col : columns) {
                    if(StringUtils.isEmpty(col)) {
                        continue;
                    }
                    if(null == pp2Datas.get(col)) {
                        sub.append("    ").append(col).append(" 为空。\n"); 
                    } else if(pp2Datas.get(col) instanceof Number && NumberUtils.comparison(pp2Datas.get(col).toString(), "0")==0) {
                        sub.append("    ").append(col).append(" 为0。\n");
                    }
                }
                if(!StringUtils.isEmpty(sub.toString())) {
                    String fStr = pp2FinDate + ":\n";
                    sb.append(fStr).append(sub.toString());
                }
            }
            if(null==pp3Datas || pp3Datas.keySet().size()==0) {
                sb.append(pp3FinDate).append("财报没有。\n");
            } else {
                StringBuilder sub = new StringBuilder();
                for(String col : columns) {
                    if(StringUtils.isEmpty(col)) {
                        continue;
                    }
                    if(null == pp3Datas.get(col)) {
                        sub.append("    ").append(col).append(" 为空。\n"); 
                    } else if(pp3Datas.get(col) instanceof Number && NumberUtils.comparison(pp3Datas.get(col).toString(), "0")==0) {
                        sub.append("    ").append(col).append(" 为0。\n");
                    }
                }
                if(!StringUtils.isEmpty(sub.toString())) {
                    String fStr = pp3FinDate + ":\n";
                    sb.append(fStr).append(sub.toString());
                }
            }
        } else {
            if(null==currentDatas || currentDatas.keySet().size()==0) {
                sb.append(finDate).append("财报没有。\n");
            } else {
                sb.append(finDate).append(":\n");
                for(Entry<String, Object> entry : currentDatas.entrySet()) {
                    String key = entry.getKey();
                    if(isIgnoreKey(key)) {
                        continue;
                    }
                    if(null == entry.getValue()) {
                        sb.append("    ").append(key).append(" 为空。\n"); 
                    } else if(entry.getValue() instanceof Number && NumberUtils.comparison(entry.getValue().toString(), "0")==0) {
                        sb.append("    ").append(key).append(" 为0。\n");
                    }
                }
            }
            if(null==ppDatas || ppDatas.keySet().size()==0) {
                sb.append(ppFinDate).append("财报没有。\n"); 
            } else {
                sb.append(ppFinDate).append(":\n");
                for(Entry<String, Object> entry : ppDatas.entrySet()) {
                    String key = entry.getKey();
                    if(isIgnoreKey(key)) {
                        continue;
                    }
                    if(null == entry.getValue()) {
                        sb.append("    ").append(key).append(" 为空。\n"); 
                    } else if(entry.getValue() instanceof Number && NumberUtils.comparison(entry.getValue().toString(), "0")==0) {
                        sb.append("    ").append(key).append(" 为0。\n");
                    }
                }
            }
            if(null==pp2Datas || pp2Datas.keySet().size()==0) {
                sb.append(pp2FinDate).append("财报没有。\n");
            } else {
                sb.append(pp2FinDate).append(":\n");
                for(Entry<String, Object> entry : pp2Datas.entrySet()) {
                    String key = entry.getKey();
                    if(isIgnoreKey(key)) {
                        continue;
                    }
                    if(null == entry.getValue()) {
                        sb.append("    ").append(key).append(" 为空。\n"); 
                    } else if(entry.getValue() instanceof Number && NumberUtils.comparison(entry.getValue().toString(), "0")==0) {
                        sb.append("    ").append(key).append(" 为0。\n");
                    }
                }
            }
            if(null==pp3Datas || pp3Datas.keySet().size()==0) {
                sb.append(pp3FinDate).append("财报没有。\n");
            } else {
                sb.append(pp3FinDate).append(":\n");
                for(Entry<String, Object> entry : pp3Datas.entrySet()) {
                    String key = entry.getKey();
                    if(isIgnoreKey(key)) {
                        continue;
                    }
                    if(null == entry.getValue()) {
                        sb.append("    ").append(key).append(" 为空。\n"); 
                    } else if(entry.getValue() instanceof Number && NumberUtils.comparison(entry.getValue().toString(), "0")==0) {
                        sb.append("    ").append(key).append(" 为0。\n");
                    }
                }
            }
        }
        return sb.toString();
    }
    
    /**
     * 
     * isIgnoreKey:(是否是忽略的key)
     * @param  @param key
     * @param  @return    设定文件
     * @return boolean    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    private boolean isIgnoreKey(String key) {
        boolean rs = false;
        if(StringUtils.isEmpty(key)) {
            return true;
        }
        if(NumberUtils.isNumeric(key)) {
            return true;
        }
        if("if".equalsIgnoreCase(key)) {
            rs = true;
        }
        if("else".equalsIgnoreCase(key)) {
            rs = true;
        }
        if("null".equalsIgnoreCase(key)) {
            rs = true;
        }
        if("COMP_ID".equalsIgnoreCase(key)) {
            rs = true;
        }
        if("FIN_DATE".equalsIgnoreCase(key)) {
            rs = true;
        }
        if("FIN_ENTITY".equalsIgnoreCase(key)) {
            rs = true;
        }
        if("FIN_STATE_TYPE".equalsIgnoreCase(key)) {
            rs = true;
        }
        if("FIN_PERIOD".equalsIgnoreCase(key)) {
            rs = true;
        }
        if("last_update_timestamp".equalsIgnoreCase(key)) {
            rs = true;
        }
        if("create_time".equalsIgnoreCase(key)) {
            rs = true;
        }
        if("VISIBLE".equalsIgnoreCase(key)) {
            rs = true;
        }
        if("pp".equalsIgnoreCase(key)) {
            rs = true;
        }
        if("pp2".equalsIgnoreCase(key)) {
            rs = true;
        }
        if("pp3".equalsIgnoreCase(key)) {
            rs = true;
        }
        if("avg".equalsIgnoreCase(key)) {
            rs = true;
        }
        if("sqrt".equalsIgnoreCase(key)) {
            rs = true;
        }
        if("std".equalsIgnoreCase(key)) {
            rs = true;
        }
        if("dmmax".equalsIgnoreCase(key)) {
            rs = true;
        }
        return rs;
        
    }
    
	public static void main(String[] args) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		OutputStream out = null;
		OutputStream err = null ;
		int result = compiler.run(null, out, err, "", "Test.java");
		String a="bs001"; 
		String[] r= a.split(",");
		System.out.println(r);
		a="bs001,bs002,";
		 r= a.split(",");
		 System.out.println(r);
		 a="";
		 r= a.split(",");
         System.out.println(r);
	}

    
}
