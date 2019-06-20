package com.innodealing;



import java.util.HashMap;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

import com.innodealing.vo.JsonResult;

public class RestTest {
	

	public static void main(String[] args) {
		Map<String,Object> urlVariables = new HashMap<String,Object>();

		urlVariables.put("comUniCode", Long.parseLong("200000161"));
		urlVariables.put("finDate", "2007-06-30");
		urlVariables.put("fields", "Tot_Asst");
		urlVariables.put("indicatorType", 2);
		urlVariables.put("userid", "500115");
		
		JsonResult<Map> j = new RestTemplate().getForObject("http://localhost:18080/api/bond/iss/indu/rank?comUniCode=200000161&finDate=2007-06-30&fields=Tot_Asst,Turnover&indicatorType=2", JsonResult.class, urlVariables);
		//getForObject("http://localhost:18080/api/bond/iss/indu/rank?comUniCode=200000161&finDate=2007-06-30&fields=Tot_Asst%2CTurnover&indicatorType=2", JsonResult.class, null);
		//ResponseEntity<JsonResult> j =new RestTemplate().postForEntity("http://localhost:18080/api/bond/iss/indu/rank",null, JsonResult.class, urlVariables);
		System.out.println(j.getData());
	}
	
	
}
