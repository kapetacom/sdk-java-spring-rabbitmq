/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */
package com.kapeta.spring.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.retry.support.RetryTemplateBuilder;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.kapeta.spring.rabbitmq.RabbitHelper.defaultRetryTemplate;

/**
 * Creates and manages RabbitMQ connections.
 */
@Slf4j
public class KapetaRabbitConnectionFactory {

    public static final String PORT_AMQP = "amqp";

    private final Map<String, ConnectionFactory> rabbitFactories = new HashMap<>();
    private final Map<String, org.springframework.amqp.rabbit.connection.ConnectionFactory> factories = new HashMap<>();
    private final Map<String, RabbitTemplate> templates = new HashMap<>();
    private final Map<String, RabbitAdmin> admins = new HashMap<>();

    private final ObjectMapper objectMapper;

    public KapetaRabbitConnectionFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ConnectionFactory createRabbitConnectionFactory(RabbitConnection connection) {
        synchronized (rabbitFactories) {
            if (rabbitFactories.containsKey(connection.getInstanceId())) {
                return rabbitFactories.get(connection.getInstanceId());
            }

            ConnectionFactory rabbitConnectionFactory = new ConnectionFactory();
            rabbitConnectionFactory.setHost(connection.getOperator().getHostname());
            rabbitConnectionFactory.setPort(connection.getOperator().getPorts().get(PORT_AMQP).getPort());
            rabbitConnectionFactory.setUsername(connection.getOperator().getCredentials().getUsername());
            rabbitConnectionFactory.setPassword(connection.getOperator().getCredentials().getPassword());
            rabbitConnectionFactory.setVirtualHost(connection.getVhost());
            rabbitConnectionFactory.setAutomaticRecoveryEnabled(false); //Handled by Spring
            rabbitFactories.put(connection.getInstanceId(), rabbitConnectionFactory);
            return rabbitConnectionFactory;
        }
    }

    public org.springframework.amqp.rabbit.connection.ConnectionFactory createConnectionFactory(RabbitConnection connection) {
        synchronized (factories) {
            if (factories.containsKey(connection.getInstanceId())) {
                return factories.get(connection.getInstanceId());
            }
            var out = new CachingConnectionFactory(createRabbitConnectionFactory(connection));

            factories.put(connection.getInstanceId(), out);
            return out;
        }
    }

    public RabbitAdmin createAdmin(RabbitConnection connection) {
        synchronized (admins) {
            if (admins.containsKey(connection.getInstanceId())) {
                return admins.get(connection.getInstanceId());
            }
            var out = new RabbitAdmin(createConnectionFactory(connection));
            admins.put(connection.getInstanceId(), out);
            return out;
        }
    }

    public <T> RabbitTemplate createTemplate(RabbitConnection connection, Class<T> payloadType) {
        synchronized (templates) {
            if (templates.containsKey(connection.getInstanceId())) {
                return templates.get(connection.getInstanceId());
            }
            var out = new RabbitTemplate(createConnectionFactory(connection));
            out.setMessageConverter(new TypedMessageConverter<T>(objectMapper, payloadType));
            out.setRetryTemplate(defaultRetryTemplate());
            templates.put(connection.getInstanceId(), out);
            return out;
        }
    }

    public void verifyConnection(RabbitConnection connection) {
        var factory = createConnectionFactory(connection);

        defaultRetryTemplate().execute(context -> {
            try (var conn = factory.createConnection()) {
                conn.createChannel(false);
            }
            return null;
        });
    }
}
