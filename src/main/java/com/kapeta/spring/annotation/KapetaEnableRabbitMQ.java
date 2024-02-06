/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */

package com.kapeta.spring.annotation;

import com.kapeta.spring.rabbitmq.RabbitBeanConfig;
import com.kapeta.spring.rabbitmq.RabbitHealthConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Add this to your Application class to enable rabbitmq support
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({RabbitBeanConfig.class, RabbitHealthConfiguration.class})
public @interface KapetaEnableRabbitMQ {

}
