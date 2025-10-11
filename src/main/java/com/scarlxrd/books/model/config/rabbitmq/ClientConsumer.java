package com.scarlxrd.books.model.config.rabbitmq;

import com.scarlxrd.books.model.DTO.BookRequestDTO;
import com.scarlxrd.books.model.DTO.ClientRequestDTO;

import com.scarlxrd.books.model.entity.Book;
import com.scarlxrd.books.model.entity.Client;
import com.scarlxrd.books.model.entity.Cpf;
import com.scarlxrd.books.model.entity.CpfValidator;
import com.scarlxrd.books.model.repository.ClientRepository;



import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import org.springframework.transaction.support.TransactionTemplate;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ClientConsumer {

    private final ClientRepository clientRepository;
    private final RabbitTemplate rabbitTemplate;
    private final Validator validator;
    private final TransactionTemplate transactionTemplate;

    private static final Logger log = LoggerFactory.getLogger(ClientConsumer.class);

    private final int MAX_RETRIES = 3;

    public ClientConsumer(ClientRepository clientRepository,
                          RabbitTemplate rabbitTemplate,
                          Validator validator, TransactionTemplate transactionTemplate) {
        this.clientRepository = clientRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.validator = validator;
        this.transactionTemplate = transactionTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(ClientRequestDTO clientRequestDTO, Message message) {

        Set<ConstraintViolation<ClientRequestDTO>> violations = validator.validate(clientRequestDTO);
        if (!violations.isEmpty()) {
            String errorMsg = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            log.warn("Validação falhou para ClientRequestDTO: {}", errorMsg);
            sendToDLQ(clientRequestDTO, message, errorMsg);
            return;
        }

       String cpfNumber = clientRequestDTO.getCpfNumber();
        if (!CpfValidator.isValidCPF(cpfNumber)) {
            log.warn("CPF inválido: {}", cpfNumber);
            sendToDLQ(clientRequestDTO, message, "CPF inválido");
            return;
        }
        Cpf cpf = new Cpf(cpfNumber);

        // Validação dos livros antes de criar entidades
        List<BookRequestDTO> booksDto = clientRequestDTO.getBooks();
        if (booksDto != null) {
            Set<String> bookErrors = booksDto.stream()
                    .flatMap(bookDto -> validator.validate(bookDto).stream())
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.toSet());

            if (!bookErrors.isEmpty()) {
                String errorMsg = String.join(", ", bookErrors);
                log.warn("Validação falhou para livros: {}", errorMsg);
                sendToDLQ(clientRequestDTO, message, errorMsg);
                return;
            }
        }

        //  Persistência com tratamento de erro temporário
        try {
            transactionTemplate.execute(status -> {
                Client client = new Client();
                client.setName(clientRequestDTO.getName());
                client.setLastName(clientRequestDTO.getLastName());
                client.setCpf(cpf);

                if (booksDto != null) {
                    booksDto.stream().forEach(bookDto -> {
                        Book book = new Book();
                        book.setTitle(bookDto.getTitle());
                        book.setAuthor(bookDto.getAuthor());
                        book.setIsbn(bookDto.getIsbn());

                        client.addBook(book);
                    });
                }

                clientRepository.save(client);
                log.info("Cliente salvo: {} {}", client.getName(), client.getLastName());
                return null;
            });

        }
        catch (DataIntegrityViolationException e) {
             //Chave duplicada detectada pelo banco. Mova para a DLQ.
            // Isso garante que clientes duplicados não causem retries infinitos.
            log.error("Erro: Cliente duplicado (CPF) detectado pelo DB. CPF: {}", cpf.getNumber());
            sendToDLQ(clientRequestDTO, message, "Cliente duplicado (violação de chave única)");
        }
        catch (Exception e) {
            log.error("Erro temporário ao salvar cliente. Tentativa de retry.", e);
            retryMessage(clientRequestDTO, message);
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

    private void retryMessage(ClientRequestDTO dto, Message message) {
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
