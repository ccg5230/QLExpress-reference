package com.innodealing.amqp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.innodealing.json.IndicatorCalculateTask;
import com.innodealing.service.FinanceIndicatorService;
import com.rabbitmq.client.Channel;

@Component
public class BondAmqpReceiver {

	Logger log = LoggerFactory.getLogger(BondAmqpReceiver.class);

	@Value("${rabbitmq.queue.work-dist}")
	private String workDistQueueName;

	@Autowired
	private Gson gson;

	@Autowired
	private FinanceIndicatorService financeIndicatorService;

	@Value("${calculate.indu.use-dist-work}")
	private Boolean useDistWork;

	@RabbitListener(queues = "${rabbitmq.queue.work-dist}")
	public void processIndicatorCalculateWork(IndicatorCalculateTask task, Channel channel) {
		if (!useDistWork) {
			return;
		}
		
		ExecutorService pool = Executors.newFixedThreadPool(6);
		for (final Long compId : task.getCompId()) {
			pool.execute(
				new Runnable() {
					@Override
					public void run() {
						try {
							financeIndicatorService.save(compId, task.getIssuerType(),null);
						} catch (Exception e) {
							log.error("主体" + compId + "计算出错！");
							e.printStackTrace();
						}
					}
				}
			);
		}
		pool.shutdown();
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			log.error("等待任务完成中发生异常 ", e);
			e.printStackTrace();
			//channel.basicReject(deliveryTag, true);
		}
	}

	/*
	 * @RabbitListener(queues = "${rabbitmq.queue.work-dist}") public void
	 * processIndicatorCalculateWork(String payload) { IndicatorCalculateTask
	 * task = gson.fromJson(payload, IndicatorCalculateTask.class);
	 * financeIndicatorService.save(task.getCompId(), task.getIssuerType()); }
	 */
}
