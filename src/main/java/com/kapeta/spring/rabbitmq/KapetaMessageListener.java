package com.kapeta.spring.rabbitmq;

import org.springframework.messaging.Message;

public interface KapetaMessageListener<T> {

    void onMessage(Message<T> message);
}
