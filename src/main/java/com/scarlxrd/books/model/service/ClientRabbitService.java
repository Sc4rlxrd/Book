package com.scarlxrd.books.model.service;

import com.scarlxrd.books.model.DTO.ClientRequestDTO;
import com.scarlxrd.books.model.entity.Client;
import com.scarlxrd.books.model.entity.Cpf;
import com.scarlxrd.books.model.entity.CpfValidator;
import com.scarlxrd.books.model.exception.BusinessException;
import com.scarlxrd.books.model.mapper.ClientMapper;
import com.scarlxrd.books.model.repository.ClientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import static net.logstash.logback.argument.StructuredArguments.kv;
@Slf4j
@Service
public class ClientRabbitService {

    private final ClientRepository clientRepository;
    private final TransactionTemplate transactionTemplate;
    private final ClientMapper clientMapper;

    public ClientRabbitService(ClientRepository clientRepository, TransactionTemplate transactionTemplate, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.transactionTemplate = transactionTemplate;
        this.clientMapper = clientMapper;
    }

    public void process(ClientRequestDTO dto) {
        if(!CpfValidator.isValidCPF(dto.getCpfNumber())){
            throw new BusinessException("Invalid CPF number: " + dto.getCpfNumber());
        }

        try {
            transactionTemplate.execute(status ->  {
                Client client = clientMapper.toEntity(dto);

                if (client.getBooks() != null) {
                    client.getBooks().forEach(book -> book.setClient(client));
                }

                clientRepository.save(client);
                log.info("Client successfully registered",
                        kv("cpf", dto.getCpfNumber()),
                        kv("event_type", "messaging_registration"));
                return null;
            });

        } catch (DataIntegrityViolationException e) {
            log.info("Idempotent duplicate event ignored",
                    kv("cpf", dto.getCpfNumber()),
                    kv("reason", "unique_constraint_violation"));
        }

        catch (Exception e){
            log.error("Error processing client persistence",
                    kv("error_message", e.getMessage()),
                    kv("cpf", dto.getCpfNumber()));
            throw e;
        }

    }
}
