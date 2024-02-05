package com.kapeta.spring.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.amqp.RabbitHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthContributorConfiguration;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;

import java.util.Map;

public class RabbitHealthConfiguration extends CompositeHealthContributorConfiguration<RabbitHealthIndicator, RabbitTemplate> {
    public RabbitHealthConfiguration() {
        super(RabbitHealthIndicator::new);
    }

    @Bean
    @ConditionalOnBean(RabbitTemplate.class)
    public HealthContributor rabbitHealthContributor(Map<String, RabbitTemplate> rabbitTemplates) {
        return createContributor(rabbitTemplates);
    }
}
