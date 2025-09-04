package com.scarlxrd.books.model.config;

import com.scarlxrd.books.model.DTO.ClientRequestDTO;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;


@Component
public class ClientProducer {

    private final RabbitTemplate rabbitTemplate;

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
        System.out.println("Mensagem enviada para RabbitMQ: " + clientRequestDTO.getName());
    }
}
