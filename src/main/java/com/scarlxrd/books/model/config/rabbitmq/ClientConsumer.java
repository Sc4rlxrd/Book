package com.scarlxrd.books.model.config.rabbitmq;

import com.scarlxrd.books.model.DTO.ClientRequestDTO;

import com.scarlxrd.books.model.entity.Book;
import com.scarlxrd.books.model.entity.Client;
import com.scarlxrd.books.model.entity.Cpf;
import com.scarlxrd.books.model.entity.CpfValidator;
import com.scarlxrd.books.model.repository.ClientRepository;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.List;

@Component
public class ClientConsumer {

    private final ClientRepository clientRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final Logger log = LoggerFactory.getLogger(ClientConsumer.class);

    private final int MAX_RETRIES = 3;

    public ClientConsumer(ClientRepository clientRepository, RabbitTemplate rabbitTemplate) {
        this.clientRepository = clientRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(ClientRequestDTO clientRequestDTO, Message message){
        try {
            Cpf cpf = new Cpf(clientRequestDTO.getCpfNumber());
            if (!CpfValidator.isValidCPF(String.valueOf(cpf))) {
                log.warn("CPF inválido, descartando mensagem: {}", clientRequestDTO.getCpfNumber());
                return;
            }
            if (clientRepository.existsByCpf(cpf)) {
                log.warn("Cliente já existe: {}", cpf);
                return;
            }
            // Converte DTO para entidade
            Client client = new Client();
            client.setName(clientRequestDTO.getName());
            client.setLastName(clientRequestDTO.getLastName());
            client.setCpf(new Cpf(clientRequestDTO.getCpfNumber()));

            if (clientRequestDTO.getBooks() != null) {
                List<Book> books = clientRequestDTO.getBooks().stream().map(bookDto -> {
                    Book book = new Book();
                    book.setTitle(bookDto.getTitle());
                    book.setAuthor(bookDto.getAuthor());
                    book.setIsbn(bookDto.getIsbn());
                    book.setClient(client);
                    return book;
                }).toList();
                client.setBooks(books);
            }

            clientRepository.save(client);
            log.info("Cliente salvo no consumer: {} {}", client.getName(), client.getLastName());
        } catch (IllegalArgumentException e) {
            log.warn("CPF inválido.: {}", e.getMessage());
        }
        catch (Exception e) {
            Integer retryCount = (Integer) message.getMessageProperties().getHeaders().getOrDefault("x-retry-count",0);
            if (retryCount < MAX_RETRIES){
                int nextRetry = retryCount + 1;
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE,
                        RabbitMQConfig.RETRY_QUEUE_NAME,
                        clientRequestDTO,
                        m ->{
                            m.getMessageProperties().setHeader("x-retry-count", nextRetry);
                            return m;
                        }
                );
                log.warn("Erro ao processar mensagem, retry {}/{}. Reenviando para retry queue.", nextRetry, MAX_RETRIES, e);
            }else {
               rabbitTemplate.convertAndSend(
                       RabbitMQConfig.DLX_EXCHANGE,
                       RabbitMQConfig.DLQ_ROUTING_KEY,
                       clientRequestDTO
               );
               log.error("Mensagem movida para DLQ após {} tentativas. Erro: {}", MAX_RETRIES, e.getMessage(), e);
            }
        }
    }
}
