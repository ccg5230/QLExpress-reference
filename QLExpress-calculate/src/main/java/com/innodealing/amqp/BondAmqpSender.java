package com.innodealing.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.innodealing.json.FinSpclindicatorJson;
import com.innodealing.json.IndicatorCalculateTask;

@Component
public class BondAmqpSender {

	Logger log = LoggerFactory.getLogger(BondAmqpSender.class);
	
	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Value("${rabbitmq.finnance.special.lindicator.json}")
	private String queueName;
	
	@Value("${rabbitmq.queue.work-dist}")
	private String workDistQueueName;
	
	public void send(FinSpclindicatorJson json){
		if(json == null){
			log.error("消息不能为空");
			return;
		}
		
		log.error("mq消息:" + json.toString());
		rabbitTemplate.convertAndSend(queueName, json.toString());
	}
	
	public void distIndicatorCalculateWork(IndicatorCalculateTask task)
	{
		
		rabbitTemplate.convertAndSend(workDistQueueName, task);
	}
}
