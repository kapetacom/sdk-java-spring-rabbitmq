/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */
package com.kapeta.spring.rabbitmq.types;

import com.kapeta.schemas.entity.EntityList;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RabbitMQBlockSpec {
    private EntityList entities = new EntityList();
    private List<RabbitMQExchangeResource> consumers = new ArrayList<>();
    private List<RabbitMQQueueResource> providers = new ArrayList<>();
    private RabbitMQBindingsSchema bindings;
}
