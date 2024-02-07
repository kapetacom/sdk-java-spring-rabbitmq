/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */
package com.kapeta.spring.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapeta.spring.config.BeanHelper;
import com.kapeta.spring.config.KapetaDefaultConfig;
import com.kapeta.spring.rabbitmq.types.ExchangeBindingSchema;
import com.kapeta.spring.rabbitmq.types.HeaderBindings;
import com.kapeta.spring.rabbitmq.types.RabbitMQExchangeResource;
import com.kapeta.spring.rabbitmq.types.RabbitMQQueueResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class for RabbitMQ operations.
 */
@Slf4j
public class RabbitHelper {

    private final ObjectMapper objectMapper = KapetaDefaultConfig.createDefaultObjectMapper();

    private final RabbitAdmin rabbitAdmin;

    public RabbitHelper(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    private String getRabbitName() {
        return "%s:%s/%s".formatted(
                rabbitAdmin.getRabbitTemplate().getConnectionFactory().getHost(),
                rabbitAdmin.getRabbitTemplate().getConnectionFactory().getPort(),
                rabbitAdmin.getRabbitTemplate().getConnectionFactory().getVirtualHost()
        );
    }

    public void bindingEnsure(Binding binding) {
        log.info("Ensuring binding {} on {}", binding, getRabbitName());
        rabbitAdmin.declareBinding(binding);
    }

    public void exchangeEnsure(Exchange exchange) {
        log.info("Ensuring exchange {} on {}", exchange, getRabbitName());
        rabbitAdmin.declareExchange(exchange);
    }

    public String queueEnsure(Queue queueOptions) {
        log.info("Ensuring queue {} on {}", queueOptions, getRabbitName());
        return rabbitAdmin.declareQueue(queueOptions);
    }

    public Queue asQueue(RabbitMQQueueResource queue) {
        var queueName = queue.getSpec().isExclusive() ? "" : queue.getMetadata().getName();
        return new Queue(
                queueName,
                queue.getSpec().isDurable(),
                queue.getSpec().isExclusive(),
                queue.getSpec().isAutoDelete()
        );
    }

    public Exchange asExchange(RabbitMQExchangeResource exchange) {
        return switch (exchange.getSpec().getExchangeType()) {
            case topic -> new TopicExchange(
                    exchange.getMetadata().getName(),
                    exchange.getSpec().isDurable(),
                    exchange.getSpec().isAutoDelete()
            );
            case fanout -> new FanoutExchange(
                    exchange.getMetadata().getName(),
                    exchange.getSpec().isDurable(),
                    exchange.getSpec().isAutoDelete()
            );
            case direct -> new DirectExchange(
                    exchange.getMetadata().getName(),
                    exchange.getSpec().isDurable(),
                    exchange.getSpec().isAutoDelete()
            );
            case headers -> new HeadersExchange(
                    exchange.getMetadata().getName(),
                    exchange.getSpec().isDurable(),
                    exchange.getSpec().isAutoDelete()
            );
        };
    }

    public Binding asBinding(Binding.DestinationType type, ExchangeBindingSchema binding, String destinationName, String exchangeName) {
        if (binding.getRouting() instanceof String) {
            return new Binding(
                    destinationName,
                    type,
                    exchangeName,
                    (String) binding.getRouting(),
                    null
            );
        }

        var headerBindings = objectMapper.convertValue(binding.getRouting(), HeaderBindings.class);
        // Handle the case where bindings.routing is not a string (e.g., headers)
        // This part of the code depends on the specific structure of the `bindings.routing` object
        var headers = headerBindings.getHeaders();
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put("x-match", headerBindings.isMatchAll() ? "all" : "any");

        return new Binding(
                destinationName,
                type,
                exchangeName,
                "",
                headers
        );
    }

    static void registerTemplate(BeanHelper beanHelper, RabbitTemplate template) {
        beanHelper.registerBean(RabbitTemplate.class, template);
        beanHelper.registerBean(AmqpTemplate.class, template);
        beanHelper.registerBean(RabbitOperations.class, template);
        beanHelper.registerBean(ConnectionFactory.class, template.getConnectionFactory());
    }

    static RetryTemplate defaultRetryTemplate() {
        return new RetryTemplateBuilder()
                .exponentialBackoff(
                        Duration.of(1, ChronoUnit.SECONDS),
                        2,
                        Duration.of(30, ChronoUnit.SECONDS)
                )
                .maxAttempts(100)
                .notRetryOn(List.of(
                        IllegalArgumentException.class,
                        IllegalStateException.class,
                        ListenerExecutionFailedException.class
                ))
                .build();
    }
}
