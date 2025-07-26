package com.scarlxrd.books.model.service;
import com.scarlxrd.books.model.DTO.BookRequestDTO;

import com.scarlxrd.books.model.DTO.ClientRequestDTO;
import com.scarlxrd.books.model.DTO.ClientResponseDTO;
import com.scarlxrd.books.model.entity.Book;
import com.scarlxrd.books.model.entity.Client;
import com.scarlxrd.books.model.entity.Cpf;
import com.scarlxrd.books.model.repository.ClientRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.List;

import java.util.stream.Collectors;

@Service
public class ClientService {
    private final ClientRepository clientRepository;

    @Autowired
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional // Garante que a operação seja atômica no banco de dados
    public ClientResponseDTO createClient(ClientRequestDTO requestDTO) {
        // 1. Validação do CPF (ocorre no construtor de Cpf)
        Cpf cpf = new Cpf(requestDTO.getCpfNumber());

        // 2. Cria a entidade Client
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

        // 5. Converte a entidade salva para o DTO de resposta
        return new ClientResponseDTO(savedClient);
    }

    @Transactional
    public List<ClientResponseDTO> getAllClients() {
        // Busca todos os clientes. FetchType.LAZY para books significa que os livros
        // só serão carregados quando acessados. Para carregar tudo de uma vez,
        // pode ser necessário um @EntityGraph ou join fetch no repositório.
        // No entanto, para o DTO, o acesso a getBooks() já acionará o carregamento.
        return clientRepository.findAll().stream()
                .map(ClientResponseDTO::new) // Converte cada Client para ClientResponseDTO
                .collect(Collectors.toList());
    }


}
