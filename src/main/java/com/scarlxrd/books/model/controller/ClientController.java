package com.scarlxrd.books.model.controller;


import com.scarlxrd.books.model.DTO.ClientRequestDTO;
import com.scarlxrd.books.model.DTO.ClientResponseDTO;
import com.scarlxrd.books.model.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/clients") // Endpoint base para clientes
public class ClientController {

    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ResponseEntity<?> createClient(@Valid @RequestBody ClientRequestDTO requestDTO) {
        try {
            // Chama o serviço para criar o cliente e os seus livros
            ClientResponseDTO newClientResponse = clientService.createClient(requestDTO);
            // Retorna o DTO de resposta com status 201 Created
            return new ResponseEntity<>(newClientResponse, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            // Captura a exceção lançada pelo construtor de Cpf (CPF inválido)
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Captura outras exceções inesperadas
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao criar cliente: " + e.getMessage());
        }
    }

    @GetMapping
    public List<ClientResponseDTO> get() {
        // Chama o serviço para obter todos os clientes (já em formato DTO)
        return clientService.getAllClients();
    }
}