package com.kapeta.spring.rabbitmq;

import com.rabbitmq.client.ConfirmCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.ReceiveAndReplyCallback;
import org.springframework.amqp.core.ReplyToAddressCallback;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitOperations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Wrapper for interacting with multiple RabbitMQ templates.
 * <p>
 * Only used if multiple rabbitmq hosts are being published to from
 * the same application.
 */
@Slf4j
public class RabbitTemplateContainer implements IRabbitTemplate {

    private final List<RabbitTemplate> inner;

    public RabbitTemplateContainer(Collection<? extends RabbitTemplate> inner) {
        if (inner.isEmpty()) {
            throw new IllegalArgumentException("At least one RabbitTemplate is required");
        }

        if (inner.size() == 1) {
            log.warn("Only one RabbitTemplate provided, consider using the single template directly");
        }

        this.inner = new ArrayList<>(inner);
    }

    @Override
    public <T> T execute(ChannelCallback<T> action) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).execute(action);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T invoke(OperationsCallback<T> action, ConfirmCallback acks, ConfirmCallback nacks) {
        if (inner.size() == 1) {
            return inner.get(0).invoke(action, acks, nacks);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean waitForConfirms(long timeout) throws AmqpException {
        for(RabbitOperations op : inner) {
            if(!op.waitForConfirms(timeout)) {
                return false;
            }
        }
        return false;
    }

    @Override
    public void waitForConfirmsOrDie(long timeout) throws AmqpException {
        for (RabbitOperations op : inner) {
            op.waitForConfirmsOrDie(timeout);
        }
    }

    @Override
    public ConnectionFactory getConnectionFactory() {
        if (inner.size() == 1) {
            return inner.get(0).getConnectionFactory();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(String exchange, String routingKey, Message message, CorrelationData correlationData) throws AmqpException {
        for (RabbitOperations op : inner) {
            op.send(exchange, routingKey, message, correlationData);
        }
    }

    @Override
    public void correlationConvertAndSend(Object message, CorrelationData correlationData) throws AmqpException {
        for (RabbitOperations op : inner) {
            op.correlationConvertAndSend(message, correlationData);
        }
    }

    @Override
    public void convertAndSend(String routingKey, Object message, CorrelationData correlationData) throws AmqpException {
        for (RabbitOperations op : inner) {
            op.convertAndSend(routingKey, message, correlationData);
        }
    }

    @Override
    public void convertAndSend(String exchange, String routingKey, Object message, CorrelationData correlationData) throws AmqpException {
        for (RabbitOperations op : inner) {
            op.convertAndSend(exchange, routingKey, message, correlationData);
        }
    }

    @Override
    public void convertAndSend(Object message, MessagePostProcessor messagePostProcessor, CorrelationData correlationData) throws AmqpException {
        for (RabbitOperations op : inner) {
            op.convertAndSend(message, messagePostProcessor, correlationData);
        }
    }

    @Override
    public void convertAndSend(String routingKey, Object message, MessagePostProcessor messagePostProcessor, CorrelationData correlationData) throws AmqpException {
        for (RabbitOperations op : inner) {
            op.convertAndSend(routingKey, message, messagePostProcessor, correlationData);
        }
    }

    @Override
    public void convertAndSend(String exchange, String routingKey, Object message, MessagePostProcessor messagePostProcessor, CorrelationData correlationData) throws AmqpException {
        for (RabbitOperations op : inner) {
            op.convertAndSend(exchange, routingKey, message, messagePostProcessor, correlationData);
        }
    }

    @Override
    public Object convertSendAndReceive(Object message, CorrelationData correlationData) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceive(message, correlationData);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object convertSendAndReceive(String routingKey, Object message, CorrelationData correlationData) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceive(routingKey, message, correlationData);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object convertSendAndReceive(String exchange, String routingKey, Object message, CorrelationData correlationData) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceive(exchange, routingKey, message, correlationData);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object convertSendAndReceive(Object message, MessagePostProcessor messagePostProcessor, CorrelationData correlationData) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceive(message, messagePostProcessor, correlationData);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object convertSendAndReceive(String routingKey, Object message, MessagePostProcessor messagePostProcessor, CorrelationData correlationData) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceive(routingKey, message, messagePostProcessor, correlationData);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object convertSendAndReceive(String exchange, String routingKey, Object message, MessagePostProcessor messagePostProcessor, CorrelationData correlationData) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceive(exchange, routingKey, message, messagePostProcessor, correlationData);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T convertSendAndReceiveAsType(Object message, CorrelationData correlationData, ParameterizedTypeReference<T> responseType) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceiveAsType(message, correlationData, responseType);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T convertSendAndReceiveAsType(String routingKey, Object message, CorrelationData correlationData, ParameterizedTypeReference<T> responseType) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceiveAsType(routingKey, message, correlationData, responseType);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T convertSendAndReceiveAsType(Object message, MessagePostProcessor messagePostProcessor, CorrelationData correlationData, ParameterizedTypeReference<T> responseType) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceiveAsType(message, messagePostProcessor, correlationData, responseType);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T convertSendAndReceiveAsType(String routingKey, Object message, MessagePostProcessor messagePostProcessor, CorrelationData correlationData, ParameterizedTypeReference<T> responseType) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceiveAsType(routingKey, message, messagePostProcessor, correlationData, responseType);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T convertSendAndReceiveAsType(String exchange, String routingKey, Object message, MessagePostProcessor messagePostProcessor, CorrelationData correlationData, ParameterizedTypeReference<T> responseType) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceiveAsType(exchange, routingKey, message, messagePostProcessor, correlationData, responseType);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(Message message) throws AmqpException {
        for (RabbitOperations op : inner) {
            op.send(message);
        }
    }

    @Override
    public void send(String s, Message message) throws AmqpException {
        for (RabbitOperations op : inner) {
            op.send(s, message);
        }
    }

    @Override
    public void send(String s, String s1, Message message) throws AmqpException {
        for (RabbitOperations op : inner) {
            op.send(s, s1, message);
        }
    }

    @Override
    public void convertAndSend(Object o) throws AmqpException {
        for (RabbitOperations op : inner) {
            op.convertAndSend(o);
        }
    }

    @Override
    public void convertAndSend(String s, Object o) throws AmqpException {
        for (RabbitOperations op : inner) {
            op.convertAndSend(s, o);
        }
    }

    @Override
    public void convertAndSend(String s, String s1, Object o) throws AmqpException {
        for (RabbitOperations op : inner) {
            op.convertAndSend(s, s1, o);
        }
    }

    @Override
    public void convertAndSend(Object o, MessagePostProcessor messagePostProcessor) throws AmqpException {
        for (RabbitOperations op : inner) {
            op.convertAndSend(o, messagePostProcessor);
        }
    }

    @Override
    public void convertAndSend(String s, Object o, MessagePostProcessor messagePostProcessor) throws AmqpException {
        for (RabbitOperations op : inner) {
            op.convertAndSend(s, o, messagePostProcessor);
        }
    }

    @Override
    public void convertAndSend(String s, String s1, Object o, MessagePostProcessor messagePostProcessor) throws AmqpException {
        for (RabbitOperations op : inner) {
            op.convertAndSend(s, s1, o, messagePostProcessor);
        }
    }

    @Override
    public Message receive() throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).receive();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Message receive(String s) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).receive(s);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Message receive(long l) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).receive(l);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Message receive(String s, long l) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).receive(s, l);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object receiveAndConvert() throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).receiveAndConvert();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object receiveAndConvert(String s) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).receiveAndConvert(s);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object receiveAndConvert(long l) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).receiveAndConvert(l);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object receiveAndConvert(String s, long l) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).receiveAndConvert(s, l);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T receiveAndConvert(ParameterizedTypeReference<T> parameterizedTypeReference) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).receiveAndConvert(parameterizedTypeReference);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T receiveAndConvert(String s, ParameterizedTypeReference<T> parameterizedTypeReference) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).receiveAndConvert(s, parameterizedTypeReference);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T receiveAndConvert(long l, ParameterizedTypeReference<T> parameterizedTypeReference) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).receiveAndConvert(l, parameterizedTypeReference);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T receiveAndConvert(String s, long l, ParameterizedTypeReference<T> parameterizedTypeReference) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).receiveAndConvert(s, l, parameterizedTypeReference);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <R, S> boolean receiveAndReply(ReceiveAndReplyCallback<R, S> receiveAndReplyCallback) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).receiveAndReply(receiveAndReplyCallback);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <R, S> boolean receiveAndReply(String s, ReceiveAndReplyCallback<R, S> receiveAndReplyCallback) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).receiveAndReply(s, receiveAndReplyCallback);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <R, S> boolean receiveAndReply(ReceiveAndReplyCallback<R, S> receiveAndReplyCallback, String s, String s1) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).receiveAndReply(receiveAndReplyCallback, s, s1);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <R, S> boolean receiveAndReply(String s, ReceiveAndReplyCallback<R, S> receiveAndReplyCallback, String s1, String s2) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).receiveAndReply(s, receiveAndReplyCallback, s1, s2);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <R, S> boolean receiveAndReply(ReceiveAndReplyCallback<R, S> receiveAndReplyCallback, ReplyToAddressCallback<S> replyToAddressCallback) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).receiveAndReply(receiveAndReplyCallback, replyToAddressCallback);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <R, S> boolean receiveAndReply(String s, ReceiveAndReplyCallback<R, S> receiveAndReplyCallback, ReplyToAddressCallback<S> replyToAddressCallback) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).receiveAndReply(s, receiveAndReplyCallback, replyToAddressCallback);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Message sendAndReceive(Message message) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).sendAndReceive(message);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Message sendAndReceive(String s, Message message) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).sendAndReceive(s, message);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Message sendAndReceive(String s, String s1, Message message) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).sendAndReceive(s, s1, message);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object convertSendAndReceive(Object o) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceive(o);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object convertSendAndReceive(String s, Object o) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceive(s, o);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object convertSendAndReceive(String s, String s1, Object o) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceive(s, s1, o);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object convertSendAndReceive(Object o, MessagePostProcessor messagePostProcessor) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceive(o, messagePostProcessor);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object convertSendAndReceive(String s, Object o, MessagePostProcessor messagePostProcessor) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceive(s, o, messagePostProcessor);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Object convertSendAndReceive(String s, String s1, Object o, MessagePostProcessor messagePostProcessor) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceive(s, s1, o, messagePostProcessor);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T convertSendAndReceiveAsType(Object o, ParameterizedTypeReference<T> parameterizedTypeReference) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceiveAsType(o, parameterizedTypeReference);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T convertSendAndReceiveAsType(String s, Object o, ParameterizedTypeReference<T> parameterizedTypeReference) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceiveAsType(s, o, parameterizedTypeReference);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T convertSendAndReceiveAsType(String s, String s1, Object o, ParameterizedTypeReference<T> parameterizedTypeReference) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceiveAsType(s, s1, o, parameterizedTypeReference);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T convertSendAndReceiveAsType(Object o, MessagePostProcessor messagePostProcessor, ParameterizedTypeReference<T> parameterizedTypeReference) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceiveAsType(o, messagePostProcessor, parameterizedTypeReference);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T convertSendAndReceiveAsType(String s, Object o, MessagePostProcessor messagePostProcessor, ParameterizedTypeReference<T> parameterizedTypeReference) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceiveAsType(s, o, messagePostProcessor, parameterizedTypeReference);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T convertSendAndReceiveAsType(String s, String s1, Object o, MessagePostProcessor messagePostProcessor, ParameterizedTypeReference<T> parameterizedTypeReference) throws AmqpException {
        if (inner.size() == 1) {
            return inner.get(0).convertSendAndReceiveAsType(s, s1, o, messagePostProcessor, parameterizedTypeReference);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> expectedQueueNames() {
        if (inner.size() == 1) {
            return inner.get(0).expectedQueueNames();
        }
        return null;
    }
}
