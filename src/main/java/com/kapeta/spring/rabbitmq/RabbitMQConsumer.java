/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */

package com.kapeta.spring.rabbitmq;


import com.kapeta.spring.config.BeanHelper;
import com.kapeta.spring.config.providers.types.BlockInstanceDetails;
import com.kapeta.spring.rabbitmq.types.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.ArrayList;
import java.util.List;

import static com.kapeta.spring.rabbitmq.RabbitHelper.registerTemplate;


/**
 * RabbitMQ consumer configuration class.
 */
@Slf4j
public class RabbitMQConsumer<T> implements BeanFactoryPostProcessor {

    private final RabbitConnectionManager rabbitConnectionManager;

    private final String resourceName;

    private final RabbitConnection connection;

    private final RabbitHelper rabbitHelper;

    private final BlockInstanceDetails<RabbitMQBlockDefinition> instance;

    private final RabbitMQQueueResource queue;

    private final ConnectionFactory connectionFactory;

    private final Class<T> payloadType;

    private final List<Runnable> reconnectionListeners = new ArrayList<>();

    private String queueName;

    public RabbitMQConsumer(RabbitConnectionManager rabbitConnectionManager, String resourceName, Class<T> payloadType) {
        this.rabbitConnectionManager = rabbitConnectionManager;
        this.resourceName = resourceName;
        this.connection = rabbitConnectionManager.forConsumer(resourceName);
        this.rabbitHelper = new RabbitHelper(rabbitConnectionManager.getAdmin(connection));
        this.instance = connection.getInstance();
        this.payloadType = payloadType;

        List<RabbitMQQueueResource> queueResources = getTargetedQueues(instance);

        if (queueResources.isEmpty()) {
            throw new RuntimeException("No defined queues found for %s".formatted(resourceName));
        }

        if (queueResources.size() > 1) {
            throw new RuntimeException("Multiple defined queues found. Only 1 expected for %s".formatted(resourceName));
        }

        this.queue = queueResources.get(0);
        this.queueName = queue.getMetadata().getName();
        this.connectionFactory = rabbitConnectionManager.getConnectionFactory(connection);

        connectionFactory.addConnectionListener((connection) -> {
            log.info("Got connection for consumer {}", resourceName);
            configureConsumer();
            reconnectionListeners.forEach(Runnable::run);
        });
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * Add a listener to be called when the connection is re-established.
     *
     * Returns a runnable that can be used to remove the listener.
     */
    public Runnable addReconnectionListener(Runnable listener) {
        reconnectionListeners.add(listener);
        return () -> reconnectionListeners.remove(listener);
    }

    public String getQueueName() {
        return queueName;
    }

    public Class<T> getPayloadType() {
        return payloadType;
    }

    private void configureConsumer() {

        RabbitMQBlockDefinition rabbitBlock = instance.getBlock();

        List<Exchange> exchanges = new ArrayList<>();
        List<Binding> queueBindings = new ArrayList<>();

        Queue queueOptions = rabbitHelper.asQueue(queue);

        rabbitBlock.getSpec()
                .getBindings()
                .getExchanges()
                .forEach(exchangeBindings -> {
                    var exchangeName = exchangeBindings.getExchange();
                    var exchange = getExchangeForName(rabbitBlock, exchangeName);

                    exchanges.add(rabbitHelper.asExchange(exchange));

                    if (!isValidBindings(exchangeBindings)) {
                        log.info("Not binding exchange {} to queues because there are no bindings", exchangeName);
                        return;
                    }

                    exchangeBindings.getBindings().stream()
                            .filter(bindings -> isBindingForQueue(bindings, resourceName))
                            .forEach(bindings -> {
                                log.info("Binding exchange {} to queue {} with routing key {}", exchangeName, queueOptions.getName(), bindings.getRouting());
                                queueBindings.add(rabbitHelper.asBinding(
                                        Binding.DestinationType.QUEUE,
                                        bindings,
                                        queueOptions.getName(),
                                        exchangeName
                                ));
                            });
                });

        queueName = rabbitHelper.queueEnsure(queueOptions);
        exchanges.forEach(rabbitHelper::exchangeEnsure);
        queueBindings.forEach(rabbitHelper::bindingEnsure);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        var beanHelper = new BeanHelper(beanFactory);

        registerTemplate(beanHelper, rabbitConnectionManager.getTemplate(connection, payloadType));
    }

    private String beanName(String type, String name) {
        return "%s$%s$%s".formatted(resourceName, type, name);
    }

    private static boolean isBindingForQueue(ExchangeBindingSchema bindings, String queueName) {
        return ExchangeBindingSchema.Type.queue.equals(bindings.getType()) &&
                bindings.getName().equals(queueName);
    }

    private static boolean isValidBindings(ExchangeBindingsSchema exchangeBindings) {
        return exchangeBindings.getBindings() != null && !exchangeBindings.getBindings().isEmpty();
    }

    private RabbitMQExchangeResource getExchangeForName(RabbitMQBlockDefinition rabbitBlock, String exchangeName) {
        var exchangeOptional = rabbitBlock.getSpec().getConsumers().stream()
                .filter(consumer -> consumer.getMetadata().getName().equals(exchangeName))
                .findFirst();

        if (exchangeOptional.isEmpty()) {
            throw new RuntimeException("Could not find exchange %s".formatted(exchangeName));
        }
        return exchangeOptional.get();
    }

    private List<RabbitMQQueueResource> getTargetedQueues(BlockInstanceDetails<RabbitMQBlockDefinition> instance) {
        List<RabbitMQQueueResource> queues = new ArrayList<>();
        for (var conn : instance.getConnections()) {
            instance.getBlock().getSpec().getProviders().stream()
                    .filter(consumer -> consumer.getMetadata().getName().equals(conn.getProvider().getResourceName())
                            && conn.getConsumer().getResourceName().equals(resourceName))
                    .forEach(queues::add);
        }
        return queues;
    }

}
