package com.innodealing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.innodealing.amqp.BondAmqpSender;
import com.innodealing.handler.WebExceptionHandler;

/**
 * 
 * <p>Mq test
 * @author 赵正来
 *
 */

@RestController
@RequestMapping("api/calculate/")
public class MqSenderController extends WebExceptionHandler{
	
	@Autowired private BondAmqpSender bondAmqpSender;
	
	/*@Autowired private IndicatorChangeService indicatorChangeService;
	
	@ApiOperation(value="发送mq，指标变化-[zzl]")
	@RequestMapping(value = "/bond/sender/finance/change", method = RequestMethod.POST, produces = "application/json")
	public JsonResult<String > calculate(@RequestBody IndicatorChangeVo indicatorChange,
			@RequestHeader("userid") long userid) throws Exception{
		FinSpclindicatorJson json = indicatorChangeService.checkChange(indicatorChange);
		bondAmqpSender.send(json);
		return new JsonResult<String>().ok("success");
	}*/
	
}
