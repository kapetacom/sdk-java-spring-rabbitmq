/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */
package com.kapeta.spring.rabbitmq.types;

import lombok.Data;

@Data
public class RabbitMQExchangeSpec {
    private ExchangeType exchangeType;
    private boolean durable;
    private boolean autoDelete;

    public enum ExchangeType {
        direct,
        fanout,
        topic,
        headers
    }
}
