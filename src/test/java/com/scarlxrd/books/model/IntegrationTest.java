package com.scarlxrd.books.model;

import com.scarlxrd.books.model.DTO.BookRequestDTO;
import com.scarlxrd.books.model.DTO.ClientRequestDTO;
import com.scarlxrd.books.model.config.rabbitmq.ClientProducer;
import com.scarlxrd.books.model.entity.Book;
import com.scarlxrd.books.model.entity.Client;
import com.scarlxrd.books.model.repository.ClientRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
@ActiveProfiles("application-integration-test.properties")
@Transactional
public class IntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {

        // PostgreSQL
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // RabbitMQ
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
    }

    @Autowired
    private ClientProducer clientProducer;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void cleanDb() {
        clientRepository.deleteAll();
    }

    record ClientDto(String name, String lastName, String cpf, List<Book> books) {}

    @Test
    @DisplayName("Cliente válido enviado pelo RabbitMQ é persistido no banco.")
    void testProducerSendsAndConsumerPersists() {

        ClientRequestDTO dto = new ClientRequestDTO();
        dto.setName("João");
        dto.setLastName("Silva");
        dto.setCpfNumber("508.396.410-44");
        dto.setBooks(List.of(new BookRequestDTO("Domain-Driven Design", "Eric Evans", "123-456")));

        // Act → envia para RabbitMQ
        clientProducer.sendClient(dto);

        // Assert → aguarda consumidor processar e salvar no Postgres
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            tx.execute(status -> {
                List<ClientDto> dtos = clientRepository.findAll().stream().map(c-> new ClientDto(c.getName(),c.getLastName(),c.getCpf().toString(), c.getBooks())).toList();
                assertThat(dtos).hasSize(1);
                assertThat(dtos.getFirst().name).isEqualTo("João");
                assertThat(dtos.getFirst().lastName).isEqualTo("Silva");
                assertThat(dtos.getFirst().cpf).isEqualTo("508.396.410-44");
                assertThat(dtos.getFirst().books().size()).isEqualTo(1);
                assertThat(dtos.getFirst().books().getFirst().getTitle()).isEqualTo("Domain-Driven Design");
                assertThat(dtos.getFirst().books().getFirst().getAuthor()).isEqualTo("Eric Evans");
                assertThat(dtos.getFirst().books().getFirst().getIsbn()).isEqualTo("123-456");
                return  null;
            });

        });
    }

    @Test
    @DisplayName("Cliente com CPF inválido é descartado pelo consumer")
    void testProducerSendsInvalidCpfMessage() {

        ClientRequestDTO dto = new ClientRequestDTO();
        dto.setName("João");
        dto.setLastName("Silva");
        dto.setCpfNumber("111.111.111-11");
        dto.setBooks(List.of(new BookRequestDTO("Domain-Driven Design", "Eric Evans", "123-456")));

        // Act → envia para RabbitMQ
        clientProducer.sendClient(dto);

        // Assert → aguarda consumidor processar e salvar no Postgres
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            tx.execute(status -> {
                List<Client> clients = clientRepository.findAll();
                assertThat(clients).isEmpty();
                return  null;
            });

        });
    }
}
