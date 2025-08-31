package com.scarlxrd.books.model.service;

import com.scarlxrd.books.model.DTO.ClientResponseDTO;
import com.scarlxrd.books.model.config.RabbitMQConfig;
import com.scarlxrd.books.model.entity.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class ClientConsumer {
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumer(Client client){
        System.out.println("Recebido do RabbitMq: " + client);
    }
}
