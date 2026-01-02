package com.scarlxrd.books.model.config.rabbitmq;

import com.scarlxrd.books.model.DTO.ClientRequestDTO;
import com.scarlxrd.books.model.exception.BusinessException;
import com.scarlxrd.books.model.service.ClientRabbitService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ClientConsumer {

    private final ClientRabbitService clientRabbitService;
    private final RabbitTemplate rabbitTemplate;
    private final Validator validator;
    private static final Logger log = LoggerFactory.getLogger(ClientConsumer.class);
    private final int MAX_RETRIES = 3;

    public ClientConsumer(ClientRabbitService clientRabbitService, RabbitTemplate rabbitTemplate, Validator validator) {
        this.clientRabbitService = clientRabbitService;
        this.rabbitTemplate = rabbitTemplate;
        this.validator = validator;

    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(ClientRequestDTO clientRequestDTO, Message message) {

        Set<ConstraintViolation<ClientRequestDTO>> violations = validator.validate(clientRequestDTO);
        if(!violations.isEmpty()) {
            String errorMsg = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            log.warn("Validação falhou para ClientRequestDTO: {}", errorMsg);
            sendToDLQ(clientRequestDTO, message, errorMsg);
            return;
        }
        try {
            clientRabbitService.process(clientRequestDTO);
        }catch (BusinessException e) {
            sendToDLQ(clientRequestDTO, message, e.getMessage());
        }
        catch (Exception e) {
            sendToRetry(clientRequestDTO, message);
        }
    }

    private void sendToDLQ(ClientRequestDTO dto, Message message, String reason) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.DLX_EXCHANGE,
                RabbitMQConfig.DLQ_ROUTING_KEY,
                dto,
                m ->{
                    m.getMessageProperties().setHeader("x-reason", reason);
                    return m;
                });
        log.error("Mensagem movida para DLQ. Motivo: {}", reason);
    }

    private void sendToRetry(ClientRequestDTO dto, Message message) {
        Integer retryCount = (Integer) message.getMessageProperties()
                .getHeaders().getOrDefault("x-retry-count", 0);

        if (retryCount < MAX_RETRIES) {
            int nextRetry = retryCount + 1;
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.RETRY_QUEUE_NAME,
                    dto,
                    m -> {
                        m.getMessageProperties().setHeader("x-retry-count", nextRetry);
                        return m;
                    }
            );
            log.warn("Retry {}/{} enviado para retry queue", nextRetry, MAX_RETRIES);
        } else {
            sendToDLQ(dto, message, "Máximo de retries atingido");
        }
    }
}
