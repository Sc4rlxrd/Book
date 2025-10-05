package com.scarlxrd.books.model.config.rabbitmq;

import com.scarlxrd.books.model.DTO.ClientRequestDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ClientProducer {

    private final RabbitTemplate rabbitTemplate;
    private static final Logger log = LoggerFactory.getLogger(ClientProducer.class);
    public ClientProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendClient(ClientRequestDTO clientRequestDTO) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                clientRequestDTO,
                message -> {
                    message.getMessageProperties().setHeader("x-retry-count", 0);
                    return message;
                }
        );
        log.info("Mensagem enviada para RabbitMQ: {}", clientRequestDTO.getName());
    }
}
