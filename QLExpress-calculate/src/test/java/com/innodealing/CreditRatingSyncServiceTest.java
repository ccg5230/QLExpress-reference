package com.innodealing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.innodealing.service.CreditRatingSyncService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BondCalculateApplication.class)
@WebIntegrationTest
public class CreditRatingSyncServiceTest {

	private @Autowired CreditRatingSyncService creditRatingSyncService;
	
	@Test
	public void syncTest() throws Exception{
		
		long s = System.currentTimeMillis();
		boolean result = creditRatingSyncService.sync(603L);
		
		long e = System.currentTimeMillis();
		
		System.out.println("耗时" + (e-s) + "ms");
	}
}
