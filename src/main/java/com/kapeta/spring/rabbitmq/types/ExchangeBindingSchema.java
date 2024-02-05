/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */
package com.kapeta.spring.rabbitmq.types;

import lombok.Data;

@Data
public class ExchangeBindingSchema {
    private String name;
    private Type type;
    private Object routing;

    public enum Type {
        queue,
        exchange
    }
}
