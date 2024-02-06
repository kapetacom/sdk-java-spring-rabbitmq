/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */
package com.kapeta.spring.rabbitmq;

import org.springframework.messaging.Message;

/**
 * Interface for implementing a rabbitmq message listener.
 */
public interface KapetaMessageListener<T> {

    void onMessage(Message<T> message);
}
