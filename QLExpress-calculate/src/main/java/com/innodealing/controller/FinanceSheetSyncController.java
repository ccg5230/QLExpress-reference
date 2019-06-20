package com.innodealing.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.innodealing.service.BondFinaSheetSyncService;
import com.innodealing.vo.JsonResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(description = "将财报数据从中诚信库同步到dmdb") 
@RestController
@RequestMapping("api/calculate")
public class FinanceSheetSyncController{
	
    @Autowired
    private BondFinaSheetSyncService finaSheetSyncService;
    
    @ApiOperation(value = "【增量】将财报数据从中诚信库同步到dmdb")
    @RequestMapping(value = "/finance/financeSheet/syncIncre", method = RequestMethod.POST, produces = "application/json")
    public JsonResult<String> syncIncre(
    	@ApiParam(name = "requireCreditRating", value = "是否需要再评级") @RequestParam(required = true) Boolean requireCreditRating, 
		@ApiParam(name = "isManuFinaSheetOnly", value = "是否非金融数据") @RequestParam(required = true) Boolean isManuFinaSheetOnly ) throws Exception {
        return new JsonResult<String>().ok(
        		finaSheetSyncService.processChangedFinaSheets(requireCreditRating, isManuFinaSheetOnly));
    }
    
    @ApiOperation(value = "【全量】将财报数据从中诚信库同步到dmdb/慢～ 慎用,同步完需要跑计算主体财报评级和质量全量接口")
    @RequestMapping(value = "/finance/financeSheet/syncAll", method = RequestMethod.POST, produces = "application/json")
    public JsonResult<String> syncAll(
    	@ApiParam(name = "isManuFinaSheetOnly", value = "是否非金融数据")  @RequestParam(required = true) Boolean isManuFinaSheetOnly 
    		) throws Exception {
        return new JsonResult<String>().ok(finaSheetSyncService.processAllFinaSheet(isManuFinaSheetOnly));
    }
    
    @ApiOperation(value = "【全量】按年份将财报数据从中诚信库同步到dmdb")
    @RequestMapping(value = "/finance/financeSheet/syncByYear", method = RequestMethod.POST, produces = "application/json")
    public JsonResult<String> syncByYear(
    	@ApiParam(name = "finDate", value = "财报日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date finDate,
    	@ApiParam(name = "requireCreditRating", value = "是否需要再评级")  @RequestParam(required = true) Boolean requireCreditRating,
    	@ApiParam(name = "isManuFinaSheetOnly", value = "是否非金融数据") @RequestParam(required = true) Boolean isManuFinaSheetOnly
    	) throws Exception {
        return new JsonResult<String>().ok(finaSheetSyncService.processFinaSheetByQuarter(finDate, requireCreditRating, isManuFinaSheetOnly));
    }
    
    
    @ApiOperation(value = "【ccxeid】将中诚信最新ccxeid设置成下次增量的起始位置, 该接口只同步ccxeid, 不会重新同步财报数据")
    @RequestMapping(value = "/finance/financeSheet/syncCcxeId", method = RequestMethod.POST, produces = "application/json")
    public JsonResult<String> syncCcxeId() throws Exception {
        return new JsonResult<String>().ok(finaSheetSyncService.syncCcxeId());
    }

    @ApiOperation(value = "【指定主体】将财报数据从中诚信库同步到dmdb")
    @RequestMapping(value = "/finance/financeSheet/syncIssuer/{issuerId}", method = RequestMethod.POST, produces = "application/json")
    public JsonResult<String> syncIssuer(
    		@ApiParam(name = "issuerId", value = "主体|发行人id") @PathVariable Long issuerId, 
    		@ApiParam(name = "requireCreditRating", value = "是否需要再评级") @RequestParam(required = true) Boolean requireCreditRating) throws Exception {
        return new JsonResult<String>().ok(finaSheetSyncService.processIssuerFinaSheet(issuerId, null, requireCreditRating));
    }
    
    @ApiOperation(value = "【指定主体和财报时间】将财报数据从中诚信库同步到dmdb")
    @RequestMapping(value = "/finance/financeSheet/syncIssuer/{issuerId}/{finDate}", method = RequestMethod.POST, produces = "application/json")
    public JsonResult<String> syncIssuerEndDate(
    		@ApiParam(name = "issuerId", value = "主体|发行人id") @PathVariable Long issuerId, 
    		@ApiParam(name = "finDate", value = "财报日期") @RequestParam(required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") Date finDate, 
    		@ApiParam(name = "requireCreditRating", value = "是否需要再评级") @RequestParam(required = true) Boolean requireCreditRating) throws Exception {
        return new JsonResult<String>().ok(finaSheetSyncService.processIssuerFinaSheet(issuerId, finDate, requireCreditRating));
    }
    
    @ApiOperation(value = "逐个主体财报检查评级指标依赖")
    @RequestMapping(value = "/finance/financeSheet/checkRatingDependency", method = RequestMethod.POST, produces = "application/json")
    public JsonResult<String> checkRatingDependency() throws Exception {
        return new JsonResult<String>().ok(finaSheetSyncService.checkRatingDependencyAll());
    }
}
