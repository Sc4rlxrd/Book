package com.scarlxrd.books.model.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "client.book.queue";

    public static final String EXCHANGE = "book.events";
    public static final String ROUTING_KEY = "client.created";

    // dlq
    public static final String DLQ_NAME = "client.book.queue.dlq";
    public static final String DLX_EXCHANGE = "book.events.dlx";
    public static final String DLQ_ROUTING_KEY = "client.created.dlq";

    // retry
    public static final String RETRY_QUEUE_NAME = "client.book.queue.retry";

    public static final String EVENTS_EXCHANGE = "book.events";

    public static final String CLIENT_CREATED_ROUTING_KEY = "client.created";

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setObservationEnabled(true);

        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setObservationEnabled(true);

        return factory;
    }

}