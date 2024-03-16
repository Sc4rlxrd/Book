package com.scarlxrd.books.model.controller;

import com.scarlxrd.books.model.DTO.ClientDTO;
import com.scarlxrd.books.model.entity.Client;
import com.scarlxrd.books.model.service.ClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ResponseEntity<Client> createUser(@RequestBody ClientDTO clientDTO){
        Client newUser = clientService.createUser(clientDTO);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);

    }

    @GetMapping
    public List<Client> get(){
        return clientService.get() ;
    }
}
