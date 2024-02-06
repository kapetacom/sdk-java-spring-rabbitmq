/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */
package com.kapeta.spring.rabbitmq;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.amqp.rabbit.support.ListenerContainerAware;

/**
 * Interface for a RabbitMQ template.
 */
public interface IRabbitTemplate extends RabbitOperations, ListenerContainerAware, AmqpTemplate {
}
