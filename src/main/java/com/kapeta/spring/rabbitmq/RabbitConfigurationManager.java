/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */
package com.kapeta.spring.rabbitmq;

import com.kapeta.spring.config.providers.KapetaConfigurationProvider;
import com.kapeta.spring.config.providers.types.BlockInstanceDetails;
import com.kapeta.spring.config.providers.types.DefaultCredentials;
import com.kapeta.spring.config.providers.types.InstanceOperator;
import com.kapeta.spring.rabbitmq.types.RabbitMQBlockDefinition;
import com.kapeta.spring.rabbitmq.types.RabbitOperatorOptions;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

public class RabbitConfigurationManager {

    private final KapetaConfigurationProvider config;

    public RabbitConfigurationManager(KapetaConfigurationProvider config) {
        this.config = config;
    }

    public List<BlockInstanceDetails<RabbitMQBlockDefinition>> getInstancesForProvider(String providerName) {
        try {
            return config.getInstancesForProvider(providerName, RabbitMQBlockDefinition.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BlockInstanceDetails<RabbitMQBlockDefinition> getInstanceForConsumer(String providerName) {
        try {
            return config.getInstanceForConsumer(providerName, RabbitMQBlockDefinition.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InstanceOperator<RabbitOperatorOptions, DefaultCredentials> getInstanceOperator(String instanceId) {
        try {
            return config.getInstanceOperator(instanceId, RabbitOperatorOptions.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
