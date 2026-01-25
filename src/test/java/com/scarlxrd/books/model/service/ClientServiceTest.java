package com.scarlxrd.books.model.service;

import com.scarlxrd.books.model.DTO.BookRequestDTO;
import com.scarlxrd.books.model.DTO.ClientRequestDTO;
import com.scarlxrd.books.model.DTO.ClientResponseDTO;
import com.scarlxrd.books.model.entity.Book;
import com.scarlxrd.books.model.entity.Client;
import com.scarlxrd.books.model.entity.Cpf;
import com.scarlxrd.books.model.exception.ClientNotFoundException;
import com.scarlxrd.books.model.mapper.BookMapper;
import com.scarlxrd.books.model.mapper.ClientMapper;
import com.scarlxrd.books.model.mapper.ClientMapperImpl;
import com.scarlxrd.books.model.repository.BookRepository;
import com.scarlxrd.books.model.repository.ClientRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClientServiceTest {

    private ClientService clientService;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private BookRepository bookRepository;
    private final ClientMapper clientMapper = Mappers.getMapper(ClientMapper.class);
    private final BookMapper bookMapper = Mappers.getMapper(BookMapper.class);

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);

        ClientMapperImpl impl = (ClientMapperImpl) clientMapper;
        var field = ClientMapperImpl.class.getDeclaredField("bookMapper");
        field.setAccessible(true);
        field.set(impl, bookMapper);
        clientService = new ClientService(
                clientRepository,
                bookRepository,
                clientMapper,
                bookMapper
        );
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

        when(clientRepository.existsByCpf(any(Cpf.class))).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        // Act
        ClientResponseDTO responseDTO = clientService.createClient(requestDTO);
        // Assert
        Assertions.assertAll(
                () -> assertNotNull(responseDTO.getId()),
                () -> assertEquals("Guilherme", responseDTO.getName()),
                () -> assertEquals("Silva", responseDTO.getLastName()),
                () -> assertEquals("158.248.900-99", responseDTO.getCpf()),
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
        when(clientRepository.findAllWithBooks()).thenReturn(List.of(client));
        List<ClientResponseDTO> response = clientService.getAllClients();
        Assertions.assertAll(
                () -> assertEquals(1, response.size()),
                () -> assertEquals("Guilherme", response.getFirst().getName()),
                () -> assertEquals("Silva", response.getFirst().getLastName()),
                () -> assertEquals("635.815.640-33", response.getFirst().getCpf())
        );

        verify(clientRepository, times(1)).findAllWithBooks();

    }
    @Test
    @DisplayName("Returns the clients from the repository with pagination.")
    void getAllClientPage(){
        UUID clientId = UUID.randomUUID();
        Client client = new Client(clientId,"Guilherme" ,"Silva" , new Cpf("635.815.640-33"));
        client.addBook(new Book("Clean Code","Robert C. Martin","9780132350884", client));
        int pageNumber = 0;
        int pageSize = 1;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<Client> clientsList = List.of(client);
        Page<Client> mockedPage = new PageImpl<>(clientsList,pageable,clientsList.size());
        when(clientRepository.findAll(pageable)).thenReturn(mockedPage);
        Page<ClientResponseDTO> responsePage = clientService.getAllClientsPage(pageable);
        ClientResponseDTO responseDTO = responsePage.getContent().get(0);
        Assertions.assertAll(
                ()-> assertNotNull(responsePage),
                ()-> assertEquals(pageSize, responsePage.getContent().size()),
                ()-> assertEquals(pageNumber, responsePage.getNumber()),
                ()-> assertEquals(clientId, responseDTO.getId()),
                ()-> assertEquals("Guilherme", responseDTO.getName()),
                ()-> assertEquals("Silva", responseDTO.getLastName()),
                ()-> assertEquals("635.815.640-33", responseDTO.getCpf()),
                ()-> assertEquals(1, responseDTO.getBooks().size()), // Obs: troque para  1, precisa adicionar um book à entidade person antes
                () -> assertEquals("Clean Code" , responseDTO.getBooks().getFirst().getTitle()),
                () -> assertEquals("Robert C. Martin" , responseDTO.getBooks().getFirst().getAuthor()),
                () -> assertEquals("9780132350884" , responseDTO.getBooks().getFirst().getIsbn())
        );
        verify(clientRepository, times(1)).findAll(pageable);


    }

    @Test
    @DisplayName("Successfully adds a book to an existing client")
    void addBookToClient_Success(){

        String cpfNumber = "262.466.650-80";
        BookRequestDTO bookRequestDTO = new BookRequestDTO();
        bookRequestDTO.setTitle("The Pragmatic Programmer");
        bookRequestDTO.setAuthor("Andrew Hunt");
        bookRequestDTO.setIsbn("9780201616224");

        Client client  = new Client(UUID.randomUUID(), "Guilherme","Silva", new Cpf(cpfNumber));

        Book savedBook = new Book(bookRequestDTO.getTitle(), bookRequestDTO.getAuthor(), bookRequestDTO.getIsbn(), client);

        when(clientRepository.findByCpf(any(Cpf.class))).thenReturn(Optional.of(client));
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        var responseDto = clientService.addBookToClient(cpfNumber, bookRequestDTO);

        Assertions.assertAll(
                () -> assertNotNull(responseDto),
                () -> assertEquals(bookRequestDTO.getTitle(), responseDto.getTitle()),
                () -> assertEquals(bookRequestDTO.getAuthor(), responseDto.getAuthor()),
                () -> assertEquals(bookRequestDTO.getIsbn(), responseDto.getIsbn())
        );
        verify(clientRepository, times(1)).findByCpf(any(Cpf.class));
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Throws ClientNotFoundException when adding book to non-existent CPF")
    void addBookToClient_ThrowsClientNotFoundException(){

        String cpfNumber = "262.466.650-80";
        BookRequestDTO bookRequestDTO = new BookRequestDTO();
        bookRequestDTO.setTitle("The Pragmatic Programmer");
        bookRequestDTO.setAuthor("Andrew Hunt");
        bookRequestDTO.setIsbn("9780201616224");

        // Client client  = new Client(UUID.randomUUID(), "Guilherme","Silva", new Cpf(cpfNumber));

        // Book savedBook = new Book(bookRequestDTO.getTitle(), bookRequestDTO.getAuthor(), bookRequestDTO.getIsbn(), client);

        Assertions.assertThrows(ClientNotFoundException.class,() -> clientService.addBookToClient(cpfNumber, bookRequestDTO));

        verify(bookRepository, never()).save(any(Book.class));

    }
}