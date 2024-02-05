package com.kapeta.spring.rabbitmq;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapeta.spring.config.providers.KapetaConfigurationProvider;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitRetryTemplateCustomizer;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

public class RabbitBeanConfig {

    @Bean
    @ConditionalOnMissingBean(RabbitConfigurationManager.class)
    public RabbitConfigurationManager rabbitConfigurationManager(KapetaConfigurationProvider config) {
        return new RabbitConfigurationManager(config);
    }

    @Bean
    @ConditionalOnMissingBean(RabbitManagementService.class)
    public RabbitManagementService rabbitManagementService() {
        return new RabbitManagementService();
    }

    @Bean
    @ConditionalOnMissingBean(KapetaRabbitConnectionFactory.class)
    public KapetaRabbitConnectionFactory kapetaRabbitConnectionFactory(ObjectProvider<MessageConverter> messageConverter) {
        return new KapetaRabbitConnectionFactory(messageConverter.getIfUnique());
    }

    @Bean
    @ConditionalOnMissingBean(RabbitConnectionManager.class)
    public RabbitConnectionManager rabbitConnectionManager(
            RabbitConfigurationManager config,
            RabbitManagementService managementService,
            KapetaRabbitConnectionFactory connectionFactory) {
        return new RabbitConnectionManager(config, managementService, connectionFactory);
    }


    @Bean
    private MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper, "com.kapeta.*");
    }
}
