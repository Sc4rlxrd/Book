package com.scarlxrd.books.model.service;

import com.scarlxrd.books.model.DTO.BookRequestDTO;

import com.scarlxrd.books.model.DTO.BookResponseDTO;
import com.scarlxrd.books.model.DTO.ClientRequestDTO;
import com.scarlxrd.books.model.DTO.ClientResponseDTO;

import com.scarlxrd.books.model.entity.Book;
import com.scarlxrd.books.model.entity.Client;
import com.scarlxrd.books.model.entity.Cpf;
import com.scarlxrd.books.model.exception.ClientAlreadyExistsException;
import com.scarlxrd.books.model.exception.ClientNotFoundException;
import com.scarlxrd.books.model.mapper.ClientMapper;
import com.scarlxrd.books.model.repository.BookRepository;
import com.scarlxrd.books.model.repository.ClientRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.stream.Collectors;

@Service
public class ClientService {
    private final ClientRepository clientRepository;
    private final BookRepository bookRepository;

    private final ClientMapper clientMapper;

    public ClientService(ClientRepository clientRepository, BookRepository bookRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.bookRepository = bookRepository;

        this.clientMapper = clientMapper;
    }

   @Transactional
    public ClientResponseDTO createClient(ClientRequestDTO requestDTO) {
        Cpf cpf = new Cpf(requestDTO.getCpfNumber());
        if (clientRepository.existsByCpf(cpf)) {
            throw new ClientAlreadyExistsException("Já existe um cliente cadastrado com este CPF: " + cpf);
        }

        Client client = clientMapper.toEntity(requestDTO);
        if (client.getBooks() != null) {
            client.getBooks().forEach(book -> book.setClient(client));
        }

        Client savedClient = clientRepository.save(client);
        return clientMapper.toResponse(savedClient);

    }

    @Transactional(readOnly = true)
    public List<ClientResponseDTO> getAllClients() {
       // arrumando o problema de N+1
        return clientRepository.findAllWithBooks().stream().map(clientMapper::toResponse).collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public Page<ClientResponseDTO> getAllClientsPage(Pageable pageable){
        Page<Client> clientPage = clientRepository.findAll(pageable);
        return clientPage.map(clientMapper::toResponse);
    }

   @Transactional
    public void deleteByCpf(String cpfNumber) {
        Cpf cpf = new Cpf(cpfNumber);
        Client client = clientRepository.findByCpf(cpf).orElseThrow(() -> new ClientNotFoundException("Cliente com CPF " + cpfNumber + " não encontrado"));
        clientRepository.delete(client);
    }
    @Transactional
    public BookResponseDTO addBookToClient(String cpfNumber , BookRequestDTO bookDTO) {
        Cpf cpf = new Cpf(cpfNumber);
        Client client = clientRepository.findByCpf(cpf).orElseThrow(()-> new ClientNotFoundException("Cliente com CPF " + cpfNumber + " não encontrado"));
        Book book = new Book(bookDTO.getTitle(), bookDTO.getAuthor(), bookDTO.getIsbn(), client);
        client.addBook(book);
        Book savedBook = bookRepository.save(book);
        return new BookResponseDTO(savedBook);
    }

    public ClientResponseDTO getClientByCpf(String cpfNumber) {
        Cpf cpf = new Cpf(cpfNumber);
        Client client = clientRepository.findByCpf(cpf).orElseThrow(()-> new ClientNotFoundException("Cliente com CPF " + cpfNumber + " não encontrado"));
        return clientMapper.toResponse(client);
    }


}
