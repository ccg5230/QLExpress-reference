/**
 * CreditRatingAmqpSender.java
 * com.innodealing.amqp
 *
 * Function： TODO 
 *
 *   ver     date      		author
 * ──────────────────────────────────
 *   		 2017年6月19日 		chungaochen
 *
 * Copyright (c) 2017, DealingMatrix All Rights Reserved.
*/

package com.innodealing.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.innodealing.json.CreditRatingCalculateJson;

/**
 * ClassName:CreditRatingAmqpSender
 * Function: TODO ADD FUNCTION
 * Reason:	 TODO ADD REASON
 *
 * @author   chungaochen
 * @version  
 * @since    Ver 1.1
 * @Date	 2017年6月19日		上午9:26:31
 *
 * @see 	 
 */
@Component
public class CreditRatingAmqpSender {
    
    Logger log = LoggerFactory.getLogger(CreditRatingAmqpSender.class);
    
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.queue.financesheet.creditrating}")
    private String queueName;
    
    /**
     * 
     * send:(发送MQ消息)
     * @param  @param json    设定文件
     * @return void    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public void send(CreditRatingCalculateJson json){
        if(json == null){
            log.error("消息不能为空");
            return;
        }
        
        log.info("mq消息:" + json.toString());
        try {
            rabbitTemplate.convertAndSend(queueName, json);
        } catch(AmqpException e) {
            log.error(e.getMessage());
        }
    }     
    
}

