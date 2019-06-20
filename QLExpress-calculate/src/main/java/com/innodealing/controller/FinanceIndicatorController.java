package com.innodealing.controller;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Objects;
import com.innodealing.constant.ResponseData;
import com.innodealing.exception.BusinessException;
import com.innodealing.handler.WebExceptionHandler;
import com.innodealing.json.BondCreditRatingJson;
import com.innodealing.service.CreditRatingCalculateService;
import com.innodealing.service.CreditRatingSyncService;
import com.innodealing.service.FinanceIndicatorService;
import com.innodealing.util.DateUtils;
import com.innodealing.vo.JsonResult;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * <p>财务指标接口
 * @author 赵正来
 *
 */
@RestController
@RequestMapping("api/calculate")
public class FinanceIndicatorController extends WebExceptionHandler{
	
	
	 @Autowired private FinanceIndicatorService financeIndicatorService;
	
	@Autowired private CreditRatingCalculateService creditRatingCalculateService;
	
	@Autowired private CreditRatingSyncService creditRatingSyncService;
	
	
	@ApiOperation(value = "获取发行人具体季度的财务指标")
	@RequestMapping(value = "/bond/{issuerId}", method = RequestMethod.GET)
	public JsonResult<Map<String,Object>> findFinanceIndicator(
			@ApiParam(name = "issuerId", value = "主体|发行人id") @PathVariable Long issuerId, 
			@ApiParam(name = "finDate", value = "财报日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd")Date finDate, 
			@ApiParam(name = "fields[]", value = "财报代码code")  @RequestParam(name = "fields[]")String[] fields){
		return new JsonResult<Map<String,Object>>().ok(financeIndicatorService.findFinanceIndicators(issuerId, finDate, fields));
	}

	@ApiOperation(value = "获取发行人具体季度的财务指标")
	@RequestMapping(value = "/bond/{issuerId}/indicators", method = RequestMethod.GET)
	public JsonResult<Map<String,Object>> findByIssuerIdAndFinDate(
			@ApiParam(name = "issuerId", value = "主体|发行人id") @PathVariable Long issuerId, 
			@ApiParam(name = "finDate", value = "财报日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd")Date finDate) {
		Map<String,Object> result = financeIndicatorService.findFinanceIndicators(issuerId, finDate);
		return new JsonResult<Map<String,Object>>().ok( result);
	}
	
	
//	@ApiOperation(value = "初始化发行人类型",notes="该接口调用优先于初始化银行、非金融、保险、券商专项指标数据")
//	@RequestMapping(value = "/bond/issuerType", method = RequestMethod.POST)
//	public JsonResult<String> initIssuerType() throws BusinessException, SQLException {
//		boolean result = financeIndicatorService.initIssuerType();
//		return new JsonResult<String>().ok(result ? "初始化成功" : "初始化失败");
//	}
//	
	@ApiOperation(value = "初始化银行专项指标数据")
	@RequestMapping(value = "/bond/bank", method = RequestMethod.POST)
	public JsonResult<String> saveBank() {
		financeIndicatorService.saveBank();
		return new JsonResult<String>().ok( "初始化成功");
	}
	
	@ApiOperation(value = "初始化非金融专项指标数据")
	@RequestMapping(value = "/bond/indu", method = RequestMethod.POST)
	public JsonResult<String> saveIndu() {
		String msg = financeIndicatorService.saveIndu();
		return new JsonResult<String>().ok( "初始化成功" + msg);
	}
	
	@ApiOperation(value = "初始化保险专项指标数据")
	@RequestMapping(value = "/bond/insu", method = RequestMethod.POST)
	public JsonResult<String> saveInsu() {
		financeIndicatorService.saveInsu();
		return new JsonResult<String>().ok( "初始化成功");
	}
	
	@ApiOperation(value = "初始单个主体专项指标数据")
	@RequestMapping(value = "/bond/{compId}", method = RequestMethod.POST)
	public JsonResult<String> save(
			@ApiParam(name = "compId", value = "主体|发行人id（安硕）") @PathVariable Long compId, 
			@ApiParam(name = "issuerType", value = "主体类型(bank、indu、insu、secu)") @RequestParam(required = false) String issuerType,
			@ApiParam(name = "finDate", value = "财报日期(yyyy-MM-dd)") @RequestParam(required = false) String finDate) {
		boolean result = financeIndicatorService.save(compId, issuerType,finDate);
		return new JsonResult<String>(result ? "1" : "-1", result ? "success" : "false", "");
	}
	
	@ApiOperation(value = "初始化券商专项指标数据")
	@RequestMapping(value = "/bond/secu", method = RequestMethod.POST)
	public JsonResult<String> saveSecu() {
		financeIndicatorService.saveSecu();
		return new JsonResult<String>().ok( "初始化成功");
	}

	@ApiOperation(value = "获取安硕用户令牌",notes="获取安硕财报评级接口、财报质量分析接口令牌")
    @RequestMapping(value = "/bond/financeSheetRatio/auth", method = RequestMethod.POST)
    public JsonResult<ResponseData> getAuth()  {
        return new JsonResult<ResponseData>().ok(creditRatingCalculateService.getAuth());
    }
	
	@ApiOperation(value = "获取指标评分风险评级",notes="获取安硕财报指标评分风险评级")
    @RequestMapping(value = "/bond/financeSheetRatio/getRatingData", method = RequestMethod.POST, produces = "application/json")
    public JsonResult<ResponseData> getRatingData(@RequestBody BondCreditRatingJson jsonFilter) {
        return new JsonResult<ResponseData>().ok(creditRatingCalculateService.getRatingDataResult(jsonFilter));
    }
	
	@ApiOperation(value = "获取财务质量分析评分",notes="获取财务质量分析评分", produces = "application/json")
    @RequestMapping(value = "/bond/financeSheetRatio/getQuanData", method = RequestMethod.POST)
    public JsonResult<ResponseData> getQuanData(@RequestBody BondCreditRatingJson jsonFilter) {
        return new JsonResult<ResponseData>().ok(creditRatingCalculateService.getQuanDataResult(jsonFilter));
    }
	
   @ApiOperation(value = "初始化财报指标计算得分及评级",notes="计算财报指标值、获取指标得分、评级、质量得分")
    @RequestMapping(value = "/bond/financeSheetRatio", method = RequestMethod.POST)
    public JsonResult<String> initfinanceSheetRatio() throws BusinessException, SQLException {
        boolean result = creditRatingCalculateService.initfinanceSheetRatio();
        return new JsonResult<String>().ok(result ? "初始化成功" : "初始化失败");
    }

	@ApiOperation(value = "添加财报主体计算财报指标得分评级及质量")
    @RequestMapping(value = "/bond/addCalculate", method = RequestMethod.POST)
    public JsonResult<ResponseData> add(
            @ApiParam(name = "compId", value = "主体|发行人id（安硕）") @RequestParam(required = true) Long compId, 
            @ApiParam(name = "finDate", value = "财报日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd")Date finDate,
            @ApiParam(name = "sheetSource", value = "财报来源（0-中诚信/1-DM）") @RequestParam(required = true) String sheetSource) {
        return new JsonResult<ResponseData>().ok(creditRatingCalculateService.addCalculate(compId, finDate, sheetSource));
    }
	
	
	@ApiOperation(value = "同步所有主体评级和财务质量-[zzl]")
    @RequestMapping(value = "/bond/sync/creditRating", method = RequestMethod.POST)
    public JsonResult<Boolean> creditRatingSyncService() {
        return new JsonResult<Boolean>().ok(creditRatingSyncService.syncAll());
    }
	
	@ApiOperation(value = "同步单个主体评级和财务质量-[zzl]")
    @RequestMapping(value = "/bond/sync/creditRating/{compId}", method = RequestMethod.POST)
    public JsonResult<Boolean> creditRatingSyncService( @ApiParam(name = "compId", value = "主体|发行人id（安硕）") @PathVariable Long compId) throws Exception {
        return new JsonResult<Boolean>().ok(creditRatingSyncService.sync(compId));
    }
	
	@ApiOperation(value = "同步单个主体财务数据、评级和财务质量、同步到mongo-[zzl]")
    @RequestMapping(value = "/bond/sync/{comUniCode}/all", method = RequestMethod.POST)
    public JsonResult<Boolean> creditRatingSyncService(
    		@ApiParam(name = "comUniCode", value = "主体|发行人id（安硕）") @PathVariable Long comUniCode,
    		@ApiParam(name = "isAddComp", value = "是否是添加主体（主要用于后台添加主体）") @RequestParam Boolean isAddComp,
    		@ApiParam(name = "sheetSource", value = "0-中诚信/1-DM") @RequestParam String sheetSource,
    		@ApiParam(name = "finDate", value = "财报日期") @RequestParam(required = false) String finDate,
    		@ApiParam(value = "mysql在更新之前的财务最新数据") @RequestBody(required = false) Map<String,Object> dataInDb) throws Exception {
		Date date = null;
		if(finDate != null && !Objects.equal("", finDate)){
			date = DateUtils.convert2Date(finDate, DateUtils.YYYY_MM_DD);
		}
        return new JsonResult<Boolean>().ok(creditRatingSyncService.syncIndicatorAndRating(comUniCode, isAddComp, sheetSource, date, dataInDb));
    }
}
