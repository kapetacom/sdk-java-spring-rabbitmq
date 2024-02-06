/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */
package com.kapeta.spring.rabbitmq;

import com.kapeta.spring.config.providers.types.BlockInstanceDetails;
import com.kapeta.spring.config.providers.types.DefaultCredentials;
import com.kapeta.spring.config.providers.types.InstanceOperator;
import com.kapeta.spring.rabbitmq.types.RabbitMQBlockDefinition;
import com.kapeta.spring.rabbitmq.types.RabbitOperatorOptions;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Getter;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.PooledChannelConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * RabbitMQ connection details.
 */
@Getter
public class RabbitConnection {
    private final BlockInstanceDetails<RabbitMQBlockDefinition> instance;

    private final InstanceOperator<RabbitOperatorOptions, DefaultCredentials> operator;

    private final String vhost;

    public RabbitConnection(BlockInstanceDetails<RabbitMQBlockDefinition> instance, InstanceOperator<RabbitOperatorOptions, DefaultCredentials> operator) {
        this.instance = instance;
        this.operator = operator;
        var optionVhost = operator.getOptions() != null && StringUtils.hasText(operator.getOptions().getVhost()) ? operator.getOptions().getVhost() : null;
        this.vhost = StringUtils.hasText(optionVhost) ? optionVhost : instance.getInstanceId();
        if (this.operator.getOptions() == null) {
            this.operator.setOptions(new RabbitOperatorOptions());
        }
        this.operator.getOptions().setVhost(vhost);
    }

    public String getInstanceId() {
        return instance.getInstanceId();
    }
}
