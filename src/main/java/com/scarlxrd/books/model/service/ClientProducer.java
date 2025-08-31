package com.scarlxrd.books.model.service;

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

    public void sendClientCreated(ClientResponseDTO client) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, client);
        System.out.println("Cliente criado:  " + client.getName() + " com esse livros: " + client.getBooks().size());
    }

    public void sendClientDeleted(ClientResponseDTO client) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, client);
        System.out.println("Cliente deletado:  " + client);
    }

}
