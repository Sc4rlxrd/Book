package com.scarlxrd.books.model.service;
import com.scarlxrd.books.model.DTO.ClientDTO;
import com.scarlxrd.books.model.entity.Client;
import com.scarlxrd.books.model.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {
    private final ClientRepository repository;
    public ClientService(ClientRepository repository) {
        this.repository = repository;
    }

    public List<Client> get(){
        return repository.findAll();
    }

    public Client createUser(ClientDTO clientDTO){
        Client newUser = new Client(clientDTO);
        return repository.save(newUser);
    }

}
