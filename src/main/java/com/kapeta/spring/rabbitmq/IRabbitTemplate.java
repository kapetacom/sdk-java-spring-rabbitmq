package com.kapeta.spring.rabbitmq;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.amqp.rabbit.support.ListenerContainerAware;

public interface IRabbitTemplate extends RabbitOperations, ListenerContainerAware, AmqpTemplate {
}
