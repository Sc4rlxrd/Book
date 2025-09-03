package com.scarlxrd.books.model.service;

import com.scarlxrd.books.model.DTO.ClientRequestDTO;
import com.scarlxrd.books.model.DTO.ClientResponseDTO;
import com.scarlxrd.books.model.config.RabbitMQConfig;

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
                clientRequestDTO
        );
        System.out.println("Mensagem enviada para RabbitMQ: " + clientRequestDTO.getName());
    }
}
