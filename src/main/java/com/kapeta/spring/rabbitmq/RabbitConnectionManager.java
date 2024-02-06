/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */
package com.kapeta.spring.rabbitmq;

import com.kapeta.spring.config.providers.types.BlockInstanceDetails;
import com.kapeta.spring.rabbitmq.types.RabbitMQBlockDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * RabbitMQ connection manager - keeps track of rabbitmq connections and their respective vhosts.
 */
@Slf4j
public class RabbitConnectionManager {

    private final RabbitConfigurationManager config;

    private final RabbitManagementService managementService;

    private final KapetaRabbitConnectionFactory connectionFactory;

    private final Set<String> seenInstances = new HashSet<>();


    public RabbitConnectionManager(RabbitConfigurationManager config, RabbitManagementService managementService, KapetaRabbitConnectionFactory connectionFactory) {
        this.config = config;
        this.managementService = managementService;
        this.connectionFactory = connectionFactory;
    }

    public MessageConverter getMessageConverter() {
        return connectionFactory.getMessageConverter();
    }

    public List<RabbitConnection> forProvider(String providerResourceName) {
        return config.getInstancesForProvider(providerResourceName).stream().map(this::addInstance).toList();
    }

    public RabbitConnection forConsumer(String consumerResourceName) {
        return addInstance(config.getInstanceForConsumer(
                consumerResourceName
        ));
    }

    public RabbitAdmin getAdmin(RabbitConnection connection) {
        return connectionFactory.createAdmin(connection);
    }

    public RabbitTemplate getTemplate(RabbitConnection connection) {
        return connectionFactory.createTemplate(connection);
    }

    public ConnectionFactory getConnectionFactory(RabbitConnection connection) {
        return connectionFactory.createConnectionFactory(connection);
    }

    private void verifyBlock(RabbitMQBlockDefinition rabbitBlock) {
        if (rabbitBlock.getSpec() == null ||
                rabbitBlock.getSpec().getBindings() == null ||
                CollectionUtils.isEmpty(rabbitBlock.getSpec().getBindings().getExchanges()) ||
                CollectionUtils.isEmpty(rabbitBlock.getSpec().getConsumers()) ||
                CollectionUtils.isEmpty(rabbitBlock.getSpec().getProviders())) {
            throw new RuntimeException("Invalid rabbitmq block definition. Missing consumers, providers and/or bindings");
        }
    }

    private RabbitConnection addInstance(BlockInstanceDetails<RabbitMQBlockDefinition> instance) {

        var operator = config.getInstanceOperator(
                instance.getInstanceId()
        );

        var connection = new RabbitConnection(instance, operator);

        if (seenInstances.contains(instance.getInstanceId())) {
            return connection;
        }

        verifyBlock(instance.getBlock());

        managementService.ensureVHost(connection);
        connectionFactory.verifyConnection(connection);

        if (!seenInstances.isEmpty()) {
            log.warn("Note that having multiple rabbitmq connections that connect to different vhosts means you can not use the bean context to autowire the rabbitmq connection.");
        }

        seenInstances.add(instance.getInstanceId());

        return connection;
    }

}
