/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */
package com.kapeta.spring.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapeta.spring.config.KapetaDefaultConfig;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessagingMessageConverter;

public class KapetaMessageListenerContainer<T> extends SimpleMessageListenerContainer {
    private final RabbitMQConsumer<T> consumer;

    private final ObjectMapper objectMapper;

    public KapetaMessageListenerContainer(RabbitMQConsumer<T> consumer, KapetaMessageListener<T> listener) {
        this(KapetaDefaultConfig.createDefaultObjectMapper(), consumer, listener);
    }

    public KapetaMessageListenerContainer(ObjectMapper objectMapper, RabbitMQConsumer<T> consumer, KapetaMessageListener<T> listener) {
        super(consumer.getConnectionFactory());
        this.consumer = consumer;
        this.objectMapper = objectMapper;
        setQueueNames(consumer.getQueueName());
        setMessageListener(createAdapter(listener));
    }

    private MessageListenerAdapter createAdapter(KapetaMessageListener<T> listener) {
        var adapter = new MessageListenerAdapter(listener, "onMessage");

        var messaging = new MessagingMessageConverter();
        messaging.setPayloadConverter(new TypedMessageConverter<T>(objectMapper, consumer.getPayloadType()));
        adapter.setMessageConverter(messaging);
        return adapter;
    }


}
