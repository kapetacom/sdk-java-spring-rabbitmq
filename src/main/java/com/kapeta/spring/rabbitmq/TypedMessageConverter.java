/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */
package com.kapeta.spring.rabbitmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.MessagingMessageConverter;

import java.io.IOException;

/**
 * Message converter for a specific payload type using JSON.
 * @param <T>
 */
public class TypedMessageConverter<T> implements MessageConverter {
    private final ObjectMapper objectMapper;
    private final Class<T> payloadClass;

    public static <T> MessagingMessageConverter createMessagingConverter(ObjectMapper objectMapper, Class<T> payloadClass) {
        var messaging = new MessagingMessageConverter();
        messaging.setPayloadConverter(new TypedMessageConverter<T>(objectMapper, payloadClass));
        return messaging;
    }

    public TypedMessageConverter(ObjectMapper objectMapper, Class<T> payloadClass) {
        this.objectMapper = objectMapper;
        this.payloadClass = payloadClass;
    }

    @Override
    public Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
        try {
            var data = objectMapper.writeValueAsBytes(object);
            return new Message(data, messageProperties);
        } catch (JsonProcessingException e) {
            throw new MessageConversionException("Failed to serialize payload", e);
        }

    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        try {
            return objectMapper.readValue(message.getBody(), payloadClass);
        } catch (IOException e) {
            throw new MessageConversionException("Failed to deserialize payload", e);
        }
    }
}
