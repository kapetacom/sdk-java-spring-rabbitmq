/*
 * Copyright 2023 Kapeta Inc.
 * SPDX-License-Identifier: MIT
 */
package com.kapeta.spring.rabbitmq;

import com.kapeta.spring.config.providers.types.DefaultCredentials;
import com.kapeta.spring.config.providers.types.InstanceOperator;
import com.kapeta.spring.rabbitmq.types.RabbitOperatorOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.kapeta.spring.rabbitmq.RabbitHelper.defaultRetryTemplate;

/**
 * RabbitMQ management service - used to ensure vhosts and other management operations.
 */
@Slf4j
public class RabbitManagementService {

    public static final String PORT_API = "management";

    private final RestTemplate restTemplate = new RestTemplate();

    public void ensureVHost(RabbitConnection connection) {

        final var operator = connection.getOperator();
        final var vhost = connection.getVhost();
        final var rabbitMQServer = getBaseUrl(operator);
        final String getQueuesUrl = "%s/queues/%s".formatted(rabbitMQServer, URLEncoder.encode(vhost, StandardCharsets.UTF_8));
        final String createVhostsUrl = "%s/vhosts/%s".formatted(rabbitMQServer, URLEncoder.encode(vhost, StandardCharsets.UTF_8));

        var wasFound = defaultRetryTemplate().execute(context -> {
            var entity = createEntity(operator);

            log.info("Checking RabbitMQ vhost: {} @ {}", vhost, rabbitMQServer);

            // We ask for queues on the vhost since that does not require any special permissions.
            // and will return 404 if the vhost does not exist.
            // It will return 401 if the vhost exist, but we do not have access.
            try {
                restTemplate.exchange(getQueuesUrl, HttpMethod.GET, entity, String.class);
                log.info("RabbitMQ vhost: {} @ {} was found", vhost, rabbitMQServer);
                return true;
            } catch (RestClientResponseException e) {
                if (e.getStatusCode().value() != 404) {
                    // If we get here it likely means we do not have access to the vhost
                    // or we do not have access to the management API
                    log.error("Failed to get vhost: %s @ %s - HTTP status: %s".formatted(vhost, rabbitMQServer, e.getStatusCode().value()), e);
                    return false;
                }
            }

            try {
                restTemplate.exchange(createVhostsUrl, HttpMethod.PUT, entity, String.class);
                log.info("RabbitMQ vhost: {} @ {} was created", vhost, rabbitMQServer);
                return true;
            } catch (RestClientResponseException e) {
                if (e.getStatusCode().value() != 499) {
                    // If we get here it likely means we do not have access to the vhost
                    // or we do not have access to the management API
                    throw new IllegalStateException("Failed to create vhost: %s @ %s - HTTP status: %s".formatted(vhost, rabbitMQServer, e.getStatusCode()));
                }
                log.error("Failed to create vhost: {} @ {}", vhost, rabbitMQServer, e);
            }
            return false;
        });

        if (!wasFound) {
            throw new IllegalStateException("Failed to ensure vhost: %s".formatted(vhost));
        }

    }

    private HttpEntity<Void> createEntity(InstanceOperator<RabbitOperatorOptions, DefaultCredentials> operator) {
        String username = operator.getCredentials().getUsername();
        String password = operator.getCredentials().getPassword();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.add("Content-Type", "application/json");
        return new HttpEntity<>(headers);
    }

    private String getBaseUrl(InstanceOperator<RabbitOperatorOptions, DefaultCredentials> operator) {
        int port = 15672;
        if (operator.getPorts().containsKey(PORT_API)) {
            port = operator.getPorts().get(PORT_API).getPort();
        }
        return "http://%s:%d/api".formatted(operator.getHostname(), port);
    }
}
