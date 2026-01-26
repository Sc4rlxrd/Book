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
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ClientConsumer {

    private final ClientRabbitService clientRabbitService;
    private final RabbitTemplate rabbitTemplate;
    private final Validator validator;
    private static final Logger log = LoggerFactory.getLogger(ClientConsumer.class);
    private final int MAX_RETRIES = 3;

    private final Counter successCounter;
    private final Counter retryCounter;
    private final Counter dlqCounter;

    public ClientConsumer(ClientRabbitService clientRabbitService, RabbitTemplate rabbitTemplate, Validator validator,MeterRegistry registry) {
        this.clientRabbitService = clientRabbitService;
        this.rabbitTemplate = rabbitTemplate;
        this.validator = validator;

        this.successCounter = Counter.builder("rabbitmq_consumer_messages_total")
                .description("Total de mensagens processadas com sucesso")
                .tag("status", "success")
                .tag("queue", RabbitMQConfig.QUEUE_NAME)
                .register(registry);

        this.retryCounter = Counter.builder("rabbitmq_consumer_messages_total")
                .description("Total de mensagens enviadas para retry")
                .tag("status", "retry")
                .tag("queue", RabbitMQConfig.QUEUE_NAME)
                .register(registry);

        this.dlqCounter = Counter.builder("rabbitmq_consumer_messages_total")
                .description("Total de mensagens enviadas para DLQ")
                .tag("status", "dlq")
                .tag("queue", RabbitMQConfig.QUEUE_NAME)
                .register(registry);

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
            successCounter.increment();
        }catch (BusinessException e) {
            dlqCounter.increment();
            sendToDLQ(clientRequestDTO, message, e.getMessage());
        }
        catch (Exception e) {
            retryCounter.increment();
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
            // CALCULO ex: 10s * (2 ^ retryCount)
            // Tentativa 1: 10 * 1 = 10s
            // Tentativa 2: 10 * 2 = 20s
            // Tentativa 3: 10 * 4 = 40s
            long baseDelay = 5000;
            long delay = (long) (baseDelay*Math.pow(2,retryCount));
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.RETRY_QUEUE_NAME,
                    dto,
                    m -> {
                        m.getMessageProperties().setHeader("x-retry-count", nextRetry);
                        m.getMessageProperties().setExpiration(String.valueOf(delay));
                        return m;
                    }
            );
            log.warn("Retry {}/{} enviado com delay de {}s", nextRetry, MAX_RETRIES,delay / 1000);
        } else {
            sendToDLQ(dto, message, "Máximo de retries atingido");
        }
    }
}
