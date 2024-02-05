package com.kapeta.spring.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
public class RabbitManagementService {

    public static final String PORT_API = "management";

    private final RestTemplate restTemplate = new RestTemplate();;

    public void ensureVHost(RabbitConnection connection) {
        int port = 15672;
        var operator = connection.getOperator();
        var vhost = connection.getVhost();

        if (operator.getPorts().containsKey(PORT_API)) {
            port = operator.getPorts().get(PORT_API).getPort();
        }
        String rabbitMQServer = "http://" + operator.getHostname() + ":" + port;
        String username = operator.getCredentials().getUsername();
        String password = operator.getCredentials().getPassword();

        String url = rabbitMQServer + "/api/vhosts/" + URLEncoder.encode(vhost, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        headers.add("Content-Type", "application/json");

        var entity = new HttpEntity<>(headers);

        log.info("Ensuring RabbitMQ vhost: {} @ {}:{}", vhost, operator.getHostname(), port);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to create vhost: %s - HTTP status: %s".formatted(vhost, response.getStatusCode()));
        }

        log.info("RabbitMQ vhost: {} @ {}:{} is ensured", vhost, operator.getHostname(), port);
    }
}
