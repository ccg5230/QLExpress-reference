package com.innodealing.amqp.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import com.innodealing.amqp.BondAmqpReceiver;

@Configuration
@EnableRabbit
public class RabbitmqConfig implements RabbitListenerConfigurer {

	@Value("${rabbitmq.exchange}")
	String exchangeName;

	@Value("${rabbitmq.creditrating.exchange}")
	String creditratingExchangeName;
	
	@Value("${rabbitmq.queue.work-dist}")
	String workDistqueueName;
	
	@Value("${rabbitmq.queue.financesheet.creditrating}")
    String creditRatingqueueName;

	@Value("${rabbitmq.prefetch-count}")
	Integer prefetchCount;

	@Bean
	public DefaultMessageHandlerMethodFactory msgHandlerMethodFactory() {
		DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
		factory.setMessageConverter(new MappingJackson2MessageConverter());
		return factory;
	}

	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setPrefetchCount(prefetchCount);//每个消费者获取最大投递数量
		//factory.setConcurrentConsumers(4);  //消费者数量
		factory.setAcknowledgeMode(AcknowledgeMode.AUTO);//设置确认模式自动确认，如果为MANUAL则发送端需实现RabbitTemplate.ConfirmCallback
		return factory;
	}

	@Bean
	RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
		return new RabbitAdmin(connectionFactory);
	}

	@Bean
	Queue queueInduWorkDist(RabbitAdmin rabbitAdmin) {
		Queue queue = new Queue(workDistqueueName, true);
		rabbitAdmin.declareQueue(queue);
		return queue;
	}

    /**
     * 第二个队列
     */  
    @Bean
    Queue queueCreditRating(RabbitAdmin rabbitAdmin) {
        Queue queue = new Queue(creditRatingqueueName, true);
        rabbitAdmin.declareQueue(queue);
        return queue;
    }

	@Bean
	DirectExchange exchange(RabbitAdmin rabbitAdmin) {
		DirectExchange directExchange = new DirectExchange(exchangeName, false, false);
		rabbitAdmin.declareExchange(directExchange);
		return directExchange;
	}

	/** 绑定策略 */
	@Bean
	Binding bindingInduWorkDis(Queue queueInduWorkDist, DirectExchange exchange) {
		return BindingBuilder.bind(queueInduWorkDist).to(exchange).with(workDistqueueName);
	}
	
    @Bean
    Binding bindingCreditRating(Queue queueCreditRating, DirectExchange exchange) {
        return BindingBuilder.bind(queueCreditRating).to(exchange).with(creditRatingqueueName);
    }
    
	@Override
	public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
		registrar.setMessageHandlerMethodFactory(msgHandlerMethodFactory());
	}

}