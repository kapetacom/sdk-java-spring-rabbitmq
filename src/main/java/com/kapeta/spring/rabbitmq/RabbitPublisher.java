/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */
package com.kapeta.spring.rabbitmq;

import lombok.Getter;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitOperations;

import java.util.Map;
import java.util.Set;

/**
 * Publisher for a specific payload type using JSON.
 * <p>
 * Use this class to publish messages to RabbitMQ.
 */
@Getter
public class RabbitPublisher<T> {

    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    private static final String DEFAULT_ENCODING = "UTF-8";

    private final RabbitOperations rabbitTemplate;

    private final Set<String> exchangeNames;


    public RabbitPublisher(RabbitOperations rabbitTemplate, Set<String> exchangeNames) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeNames = exchangeNames;
        if (exchangeNames.isEmpty()) {
            throw new IllegalArgumentException("Exchange names cannot be empty");
        }
    }

    public void publish(T message, String routingKey , MessageProperties properties) {
        var defaultProps = createMessageProperties();
        for(String exchangeNames : exchangeNames) {
            rabbitTemplate.convertAndSend(exchangeNames, routingKey, message, m -> {
                m.getMessageProperties().setContentType(defaultProps.getContentType());
                m.getMessageProperties().setContentEncoding(defaultProps.getContentEncoding());
                m.getMessageProperties().setCorrelationId(properties.getCorrelationId());
                m.getMessageProperties().setReplyTo(properties.getReplyTo());
                m.getMessageProperties().setHeaders(properties.getHeaders());

                return m;
            });
        }
    }

    public void publish(T message, String routingKey , Map<String, Object> headers) {
        MessageProperties properties = createMessageProperties();
        properties.setHeaders(headers);
        publish(message, routingKey, properties);
    }

    public void publish(T message, String routingKey) {
        publish(message, routingKey, createMessageProperties());
    }

    public void publish(T message) {
        publish(message, "");
    }

    private MessageProperties createMessageProperties() {
        var properties = new MessageProperties();
        properties.setContentType(DEFAULT_CONTENT_TYPE);
        properties.setContentEncoding(DEFAULT_ENCODING);
        return properties;
    }
}
