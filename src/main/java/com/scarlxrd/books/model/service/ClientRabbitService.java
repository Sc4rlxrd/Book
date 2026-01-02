package com.scarlxrd.books.model.service;

import com.scarlxrd.books.model.DTO.ClientRequestDTO;
import com.scarlxrd.books.model.entity.Book;
import com.scarlxrd.books.model.entity.Client;
import com.scarlxrd.books.model.entity.Cpf;
import com.scarlxrd.books.model.entity.CpfValidator;
import com.scarlxrd.books.model.exception.BusinessException;
import com.scarlxrd.books.model.exception.ClientAlreadyExistsException;
import com.scarlxrd.books.model.repository.ClientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
public class ClientRabbitService {

    private final ClientRepository clientRepository;
    private final TransactionTemplate transactionTemplate;

    public ClientRabbitService(ClientRepository clientRepository, TransactionTemplate transactionTemplate) {
        this.clientRepository = clientRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public void process(ClientRequestDTO dto) {
        if(!CpfValidator.isValidCPF(dto.getCpfNumber())){
            throw new BusinessException("Invalid CPF number");
        }

        Cpf cpf = new Cpf(dto.getCpfNumber());

        if(clientRepository.existsByCpf(cpf)){
            throw new ClientAlreadyExistsException("Client already exists");
        }

        try {
            transactionTemplate.execute(status ->  {
                Client client = new Client(
                        null,
                        dto.getName(),
                        dto.getLastName(),
                        cpf
                );
                if (dto.getBooks() != null) {
                    dto.getBooks().forEach(bookdto -> {
                        Book book = new Book(
                                bookdto.getTitle(),
                                bookdto.getAuthor(),
                                bookdto.getIsbn(),
                                client
                        );
                        client.addBook(book);
                    });
                }
                clientRepository.save(client);
                return null;
            });

        }catch (BusinessException e){
            log.error("An error occurred while processing the client.");
            throw e;
        }

    }
}
