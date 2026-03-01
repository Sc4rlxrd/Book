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

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue mainQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding mainBinding(Queue mainQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(mainQueue)
                .to(exchange)
                .with(ROUTING_KEY);
    }

    // DLQ
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLX_EXCHANGE);
    }

    @Bean
    public Binding dlqBinding(Queue deadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder
                .bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(DLQ_ROUTING_KEY);
    }

    // Retry
    @Bean
    public Queue retryQueue() {

        Map<String, Object> args = new HashMap<>();

        args.put("x-dead-letter-exchange", EXCHANGE);
        args.put("x-dead-letter-routing-key", ROUTING_KEY);

        return QueueBuilder
                .durable(RETRY_QUEUE_NAME)
                .withArguments(args)
                .build();
    }

    @Bean
    public Binding retryBinding(Queue retryQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(retryQueue)
                .to(exchange)
                .with(RETRY_QUEUE_NAME);
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