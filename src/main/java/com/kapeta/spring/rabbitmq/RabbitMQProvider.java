/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */

package com.kapeta.spring.rabbitmq;


import com.kapeta.spring.config.BeanHelper;
import com.kapeta.spring.config.providers.types.BlockInstanceDetails;
import com.kapeta.spring.rabbitmq.types.ExchangeBindingSchema;
import com.kapeta.spring.rabbitmq.types.ExchangeBindingsSchema;
import com.kapeta.spring.rabbitmq.types.RabbitMQBlockDefinition;
import com.kapeta.spring.rabbitmq.types.RabbitMQExchangeResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kapeta.spring.rabbitmq.RabbitHelper.registerTemplate;


/**
 * RabbitMQ provider configuration class.
 */
@Slf4j
public class RabbitMQProvider<T> implements BeanFactoryPostProcessor {

    protected final RabbitConnectionManager rabbitConnectionManager;

    protected final String resourceName;

    private final List<RabbitConnection> instances;
    private final Set<RabbitTemplate> templates;
    private final RabbitOperations template;
    private final Class<T> payloadType;

    public RabbitMQProvider(RabbitConnectionManager rabbitConnectionManager, String resourceName, Class<T> payloadType) {
        this.rabbitConnectionManager = rabbitConnectionManager;
        this.resourceName = resourceName;
        this.payloadType = payloadType;
        this.instances = rabbitConnectionManager.forProvider(resourceName);


        this.templates = instances.stream()
                .map(this::configureExchanges)
                .collect(Collectors.toSet());

        if (templates.isEmpty()) {
            throw new RuntimeException("No defined exchanges found");
        }

        this.template = templates.size() > 1 ? new RabbitTemplateContainer(templates) : templates.iterator().next();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        var beanHelper = new BeanHelper(beanFactory);

        var templateName = resourceName + "RabbitTemplate";

        templates.forEach(template -> {
            registerTemplate(beanHelper, template);
        });

        log.info("Registered RabbitMQ template as {}", templateName);
        beanHelper.registerBean(templateName, RabbitOperations.class, template);
    }

    public Set<String> getTargetedExchangeNames() {
        return instances.stream()
                .flatMap(connection -> getTargetedExchanges(connection.getInstance()).stream())
                .map(exchange -> exchange.getMetadata().getName())
                .collect(Collectors.toSet());
    }

    public RabbitOperations getTemplate() {
        return template;
    }

    private RabbitTemplate configureExchanges(RabbitConnection connection) {

        var admin = rabbitConnectionManager.getAdmin(connection);
        var rabbitHelper = new RabbitHelper(admin);
        var instance = connection.getInstance();
        var rabbitBlock = instance.getBlock();

        var exchangeResources = rabbitBlock.getSpec().getConsumers();
        var exchangeBindingDefinitions = rabbitBlock.getSpec().getBindings().getExchanges();

        List<Exchange> exchanges = new ArrayList<>();
        List<Binding> exchangeBindings = new ArrayList<>();

        // Get the defined exchanges that this publisher should publish to
        List<RabbitMQExchangeResource> exchangeDefinitions = getTargetedExchanges(instance);

        var template = rabbitConnectionManager.getTemplate(connection, payloadType);

        exchangeDefinitions.forEach(exchange -> {
            String exchangeName = exchange.getMetadata().getName();
            exchanges.add(rabbitHelper.asExchange(exchange));

            exchangeBindingDefinitions.stream()
                    .filter(bindings -> isValidBindingsForExchange(bindings, exchangeName))
                    .forEach(bindings -> bindings.getBindings()
                            .stream()
                            .filter(RabbitMQProvider::isExchangeBinding)
                            .forEach(binding -> {

                                var boundExchange = getExchangeByName(exchangeResources, binding.getName());

                                String boundExchangeName = boundExchange.getMetadata().getName();

                                exchanges.add(rabbitHelper.asExchange(boundExchange));

                                exchangeBindings.add(rabbitHelper.asBinding(
                                        Binding.DestinationType.EXCHANGE,
                                        binding,
                                        boundExchangeName,
                                        exchangeName
                                ));

                            }));

        });

        rabbitConnectionManager.getConnectionFactory(connection)
                .addConnectionListener((c) -> {
            log.info("Got connection for provider {}", resourceName);
            exchanges.forEach(rabbitHelper::exchangeEnsure);
            exchangeBindings.forEach(rabbitHelper::bindingEnsure);
        });

        return template;
    }


    private static boolean isExchangeBinding(ExchangeBindingSchema binding) {
        return ExchangeBindingSchema.Type.exchange.equals(binding.getType());
    }

    private static boolean isValidBindingsForExchange(ExchangeBindingsSchema bindings, String exchangeName) {
        return bindings.getBindings() != null && bindings.getExchange().equals(exchangeName);
    }

    private static RabbitMQExchangeResource getExchangeByName(List<RabbitMQExchangeResource> exchangeResources, String exchangeName) {
        return exchangeResources.stream()
                .filter(c -> c.getMetadata().getName().equals(exchangeName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find exchange %s".formatted(exchangeName)));
    }

    private List<RabbitMQExchangeResource> getTargetedExchanges(BlockInstanceDetails<RabbitMQBlockDefinition> instance) {
        List<RabbitMQExchangeResource> exchanges = new ArrayList<>();
        for (var conn : instance.getConnections()) {
            instance.getBlock().getSpec().getConsumers()
                    .stream()
                    .filter(consumer -> consumer.getMetadata().getName().equals(conn.getConsumer().getResourceName())
                            && conn.getProvider().getResourceName().equals(resourceName))
                    .forEach(exchanges::add);
        }
        return exchanges;
    }
}
