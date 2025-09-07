package com.scarlxrd.books.model.service;

import com.scarlxrd.books.model.DTO.BookRequestDTO;
import com.scarlxrd.books.model.DTO.ClientRequestDTO;
import com.scarlxrd.books.model.DTO.ClientResponseDTO;
import com.scarlxrd.books.model.config.rabbitmq.ClientProducer;
import com.scarlxrd.books.model.entity.Book;
import com.scarlxrd.books.model.entity.Client;
import com.scarlxrd.books.model.entity.Cpf;
import com.scarlxrd.books.model.repository.ClientRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClientServiceTest {
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ClientService clientService;
    @Mock
    private ClientProducer clientProducer;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clientService = new ClientService(clientRepository,clientProducer);
    }

    @Test
    @DisplayName("Creates a client with a list of books associated with it")
    void createClient_Success_WithAListOfBooks() {
        // Arrange
        ClientRequestDTO requestDTO = new ClientRequestDTO();
        requestDTO.setName("Guilherme");
        requestDTO.setLastName("Silva");
        requestDTO.setCpfNumber("158.248.900-99");
        BookRequestDTO bookRequestDTO = new BookRequestDTO();
        bookRequestDTO.setAuthor("Robert C. Martin");
        bookRequestDTO.setTitle("Clean Code");
        bookRequestDTO.setIsbn("9780132350884");
        requestDTO.setBooks(List.of(bookRequestDTO));

        Client client = new Client(UUID.randomUUID(), requestDTO.getName(), requestDTO.getLastName(), new Cpf(requestDTO.getCpfNumber()));
        client.addBook(new Book(bookRequestDTO.getTitle(), bookRequestDTO.getAuthor(), bookRequestDTO.getIsbn(), client));

        when(clientRepository.save(any(Client.class))).thenReturn(client);

        // Act
        ClientResponseDTO responseDTO = clientService.createClient(requestDTO);
        // Assert
        Assertions.assertAll(
                () -> assertNotNull(responseDTO.getId()),
                () -> assertEquals("Guilherme", responseDTO.getName()),
                () -> assertEquals("Guilherme", responseDTO.getName()),
                () -> assertEquals("Silva", responseDTO.getLastName()),
                () -> assertEquals(1, responseDTO.getBooks().size()),
                () -> assertEquals("Clean Code", responseDTO.getBooks().getFirst().getTitle())

        );

        verify(clientRepository, times(1)).save(any(Client.class));

    }

    @Test
    @DisplayName("Creates a client with null/invalid CPF and throws an exception")
    void createClient_Fail_CpfInvalid() {
        ClientRequestDTO requestDTO = new ClientRequestDTO();
        requestDTO.setName("Guilherme");
        requestDTO.setLastName("Silva");
        requestDTO.setCpfNumber(null);
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> clientService.createClient(requestDTO));
        Assertions.assertTrue(ex.getMessage().contains("CPF não pode ser nulo ou vazio."));
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Returns all clients in the repository")
    void getAllClients() {
        Client client = new Client(UUID.randomUUID(), "Guilherme", "Silva", new Cpf("635.815.640-33"));
        when(clientRepository.findAll()).thenReturn(List.of(client));
        List<ClientResponseDTO> response = clientService.getAllClients();
        Assertions.assertAll(
                () -> assertEquals(1, response.size()),
                () -> assertEquals("Guilherme", response.getFirst().getName()),
                () -> assertEquals("Silva", response.getFirst().getLastName()),
                () -> assertEquals("635.815.640-33", response.getFirst().getCpf())
        );

        verify(clientRepository, times(1)).findAll();

    }
}