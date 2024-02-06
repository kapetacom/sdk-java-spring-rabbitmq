/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */
package com.kapeta.spring.rabbitmq.types;

import com.kapeta.schemas.entity.Metadata;
import lombok.Data;

@Data
public class RabbitMQBlockDefinition {
    private String kind;
    private Metadata metadata;
    private RabbitMQBlockSpec spec;
}
