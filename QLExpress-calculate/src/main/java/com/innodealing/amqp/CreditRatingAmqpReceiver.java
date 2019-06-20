/**
 * CreditRatingAmqpReceiver.java
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
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.innodealing.json.CreditRatingCalculateJson;
import com.innodealing.service.CreditRatingCalculateService;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;

/**
 * ClassName:CreditRatingAmqpReceiver
 * Function: TODO ADD FUNCTION
 * Reason:	 TODO ADD REASON
 *
 * @author   chungaochen
 * @version  
 * @since    Ver 1.1
 * @Date	 2017年6月19日		上午10:06:20
 *
 * @see 	 
 */
@Component
public class CreditRatingAmqpReceiver {
    
    Logger log = LoggerFactory.getLogger(BondAmqpReceiver.class);

    @Value("${rabbitmq.queue.financesheet.creditrating}")
    private String queueName;
    
    @Autowired
    private CreditRatingCalculateService creditRatingCalculateService;

    @Value("${calculate.creditrating.use-dist-work}")
    private  Boolean useDistWork;
    
    @RabbitListener(queues = "${rabbitmq.queue.financesheet.creditrating}")
    public void processCalculateWork(CreditRatingCalculateJson comIds, Channel channel) {
        try {
            if(!useDistWork){
                return ;
            }
            Thread.sleep(3000);                 //监听队列先等待3秒，防止机器一开始就处理任务无法响应，而消息队列没有发送完
            creditRatingCalculateService.threadCalculateOperate(comIds);
            
        } catch (Exception e) {
            log.error("failed to cosume message", e);
            e.printStackTrace();
        }
    }
}

