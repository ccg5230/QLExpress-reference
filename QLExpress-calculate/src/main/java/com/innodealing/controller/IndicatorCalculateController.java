package com.innodealing.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.innodealing.handler.WebExceptionHandler;
import com.innodealing.service.SpecialIndicatorCalculateService;
import com.innodealing.vo.IndicatorVo;
import com.innodealing.vo.JsonResult;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 
 * <p>债券专项指标计算控制器
 * @author 赵正来
 *
 */

@RestController
@RequestMapping("api/calculate/")
public class IndicatorCalculateController extends WebExceptionHandler{
	
	@Autowired private SpecialIndicatorCalculateService calculateService;
	
	@ApiOperation(value="计算债券专项指标")
	@RequestMapping(value = "/bond/special/{issuerId}/indicator", method = RequestMethod.GET, produces = "application/json")
	public JsonResult<IndicatorVo > calculate(@ApiParam(name = "expression", value = "计算表达式") @RequestParam String expression, 
			@ApiParam(name = "issuerId", value = "主体|发行人id") @PathVariable Long issuerId, 
			@ApiParam(name = "finDate", value = "财报日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd")Date finDate,
			@ApiParam(name = "indicatorCode", value = "自定义指标代码") @RequestParam(required = false) String indicatorCode,
			@ApiParam(name = "indicatorName", value = "自定义指标名称") @RequestParam(required = false) String indicatorName,
			@RequestHeader("userid") long userid) throws Exception{
		return new JsonResult<IndicatorVo>().ok(calculateService.calculate(indicatorCode, indicatorName, expression, issuerId, finDate));
	}
	
}
