package com.scarlxrd.books.model.config.rabbitmq;

import com.rabbitmq.client.Channel;
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
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.List;

@Component
public class ClientConsumer {
    private final ClientRepository clientRepository;
    private static final Logger log = LoggerFactory.getLogger(ClientConsumer.class);
    private final int maxRetries = 3;
    public ClientConsumer(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME, ackMode = "MANUAL")
    public void receiveMessage(ClientRequestDTO clientRequestDTO, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Object header = message.getMessageProperties().getHeaders().get("x-retry-count");
        int retryCount = header != null ? Integer.parseInt(header.toString()) : 0;
        try {

            Cpf cpf = new Cpf(clientRequestDTO.getCpfNumber());
            if (!CpfValidator.isValidCPF(String.valueOf(cpf))) {
                log.warn("CPF inválido, descartando mensagem: {}", clientRequestDTO.getCpfNumber());
                channel.basicAck(deliveryTag, false);
                return;
            }
            if (clientRepository.existsByCpf(cpf)) {
                log.warn("Cliente já existe: {}", cpf);
                channel.basicAck(deliveryTag, false);
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
            channel.basicAck(deliveryTag, false);
            log.info("Cliente salvo no consumer: {} {}", client.getName(), client.getLastName());
        } catch (IllegalArgumentException e) {
            log.warn("CPF inválido.: {}", e.getMessage());
            channel.basicAck(deliveryTag, false);
        }
        catch (Exception e) {
            retryCount++;
            if (retryCount> maxRetries){
                log.error("Falha persistente ao processar mensagem. Descartando. {}", e.getMessage(), e);
                channel.basicAck(deliveryTag, false); // descarta mensagem
            }else {
                log.warn("Erro ao processar mensagem, retry {}/{}: {}", retryCount, maxRetries, e.getMessage());
                // Reenvia a mensagem com o contador de retries
                message.getMessageProperties().setHeader("x-retry-count", retryCount);
                channel.basicNack(deliveryTag, false, true); // requeue
            }
        }
    }
}
