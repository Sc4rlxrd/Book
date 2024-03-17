package com.scarlxrd.books.model.service;
import com.scarlxrd.books.model.DTO.ClientDTO;
import com.scarlxrd.books.model.entity.Client;
import com.scarlxrd.books.model.repository.ClientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public ResponseEntity<Client> updateUser(ClientDTO clientDTO , Long id){
        Optional<Client> oldPerson = repository.findById(id);
        if(oldPerson.isPresent()){
            Client person = oldPerson.get();
            person.setName(clientDTO.name());
            person.setPassword(clientDTO.password());
            person.setAge(clientDTO.age());
            person.setBooks(clientDTO.books());
            repository.save(person);
            return  new ResponseEntity<>(HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        }
    }

}
