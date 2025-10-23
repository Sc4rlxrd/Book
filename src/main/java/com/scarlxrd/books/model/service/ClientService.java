package com.scarlxrd.books.model.service;

import com.scarlxrd.books.model.DTO.BookRequestDTO;

import com.scarlxrd.books.model.DTO.ClientRequestDTO;
import com.scarlxrd.books.model.DTO.ClientResponseDTO;
import com.scarlxrd.books.model.config.rabbitmq.ClientProducer;
import com.scarlxrd.books.model.entity.Book;
import com.scarlxrd.books.model.entity.Client;
import com.scarlxrd.books.model.entity.Cpf;
import com.scarlxrd.books.model.exception.ClientNotFoundException;
import com.scarlxrd.books.model.repository.ClientRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.stream.Collectors;

@Service
public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;

    }

   @Transactional// Garante que a operação seja atômica no banco de dados
    public ClientResponseDTO createClient(ClientRequestDTO requestDTO) {
        Cpf cpf = new Cpf(requestDTO.getCpfNumber());
        if (clientRepository.existsByCpf(cpf)) {
            throw new ClientNotFoundException("Já existe um cliente cadastrado com este CPF: " + cpf);
        }

        Client client = new Client(null, requestDTO.getName(), requestDTO.getLastName(), cpf);
        // 3. Adiciona os livros ao cliente
        if (requestDTO.getBooks() != null && !requestDTO.getBooks().isEmpty()) {
            for (BookRequestDTO bookDTO : requestDTO.getBooks()) {
                Book book = new Book(bookDTO.getTitle(), bookDTO.getAuthor(), bookDTO.getIsbn(), client);
                client.addBook(book); // Usa o mét0do auxiliar para manter a bidirecionalidade
            }
        }

        // 4. Salva o cliente (e os livros em cascata devido a CascadeType.ALL)
        Client savedClient = clientRepository.save(client);
        return new ClientResponseDTO(savedClient);


    }

   @Transactional(readOnly = true)
    public List<ClientResponseDTO> getAllClients() {
        // Busca todos os clientes. FetchType.LAZY para books significa que os livros
        // só serão carregados quando acessados. Para carregar tudo de uma vez,
        // pode ser necessário um @EntityGraph ou join fetch no repositório.
        // No entanto, para o DTO, o acesso a getBooks() já acionará o carregamento.
        return clientRepository.findAll().stream().map(ClientResponseDTO::new) // Converte cada Client para ClientResponseDTO
                .collect(Collectors.toList());
    }

   @Transactional
    public void deleteByCpf(String cpfNumber) {
        Cpf cpf = new Cpf(cpfNumber);
        Client client = clientRepository.findByCpf(cpf).orElseThrow(() -> new ClientNotFoundException("Cliente com CPF " + cpfNumber + " não encontrado"));
        clientRepository.delete(client);
    }

}
