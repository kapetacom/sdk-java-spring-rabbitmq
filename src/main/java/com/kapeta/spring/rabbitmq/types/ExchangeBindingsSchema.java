/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */
package com.kapeta.spring.rabbitmq.types;

import lombok.Data;

import java.util.List;

@Data
public class ExchangeBindingsSchema {
    private String exchange;
    private List<ExchangeBindingSchema> bindings;
}
