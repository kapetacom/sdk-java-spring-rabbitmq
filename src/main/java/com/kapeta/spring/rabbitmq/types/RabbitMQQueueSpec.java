/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */
package com.kapeta.spring.rabbitmq.types;

import lombok.Data;

@Data
public class RabbitMQQueueSpec {
    private boolean durable;
    private boolean exclusive;
    private boolean autoDelete;
}
