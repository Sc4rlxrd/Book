package com.scarlxrd.books.model.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_NAME = "client.book.queue";
    public static final String EXCHANGE = "minha-exchange";
    public static final String ROUTING_KEY = "minha-routing-key";

    // dlq
    public static final String DLQ_NAME = "client.book.queue.dlq";
    public static final String DLX_EXCHANGE = "minha-exchange.dlx";
    public static final String DLQ_ROUTING_KEY = "minha-routing-key.dlq";

    // retry
    public static final String RETRY_QUEUE_NAME = "client.book.queue.retry";
    public static final int RETRY_TTL = 10000; // 10s

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
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding mainBinding(Queue mainQueue, DirectExchange exchange) {
        return BindingBuilder.bind(mainQueue).to(exchange).with(ROUTING_KEY);
    }

    //dlq
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Binding dlqBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DLQ_ROUTING_KEY);
    }

    // Retry Queue -> TTL + volta para fila principal
    @Bean
    public Queue retryQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", RETRY_TTL);
        args.put("x-dead-letter-exchange", EXCHANGE);
        args.put("x-dead-letter-routing-key", ROUTING_KEY);
        return QueueBuilder.durable(RETRY_QUEUE_NAME).withArguments(args).build();
    }

    @Bean
    public Binding retryBinding(Queue retryQueue, DirectExchange exchange) {
        return BindingBuilder.bind(retryQueue).to(exchange).with(RETRY_QUEUE_NAME);
    }

}
