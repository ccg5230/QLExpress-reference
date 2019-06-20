package com.innodealing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.innodealing.amqp.BondAmqpSender;
import com.innodealing.json.FinSpclindicatorJson;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BondCalculateApplication.class)
@WebIntegrationTest
public class BondAmqpSenderTest {

	private @Autowired BondAmqpSender bondAmqpSender;
	
	@Test
	public void senderTest(){
		FinSpclindicatorJson json = new FinSpclindicatorJson();
		json.setComUniCode(200035802L);
		//json.setFinDate("");
		bondAmqpSender.send(json);
	}
}
