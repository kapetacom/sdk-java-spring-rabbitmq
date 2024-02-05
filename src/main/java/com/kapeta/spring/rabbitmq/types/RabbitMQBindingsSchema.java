/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */
package com.kapeta.spring.rabbitmq.types;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RabbitMQBindingsSchema {
    private List<ExchangeBindingsSchema> exchanges = new ArrayList<>();
}
