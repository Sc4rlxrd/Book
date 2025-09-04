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
    public ResponseEntity<ClientResponseDTO> createClient(@Valid @RequestBody ClientRequestDTO requestDTO) {

        ClientResponseDTO newClientResponse = clientService.createClient(requestDTO);
        return new ResponseEntity<>(newClientResponse, HttpStatus.CREATED);

    }

    @GetMapping
    public List<ClientResponseDTO> get() {
        // Chama o serviço para obter todos os clientes (já em formato DTO)
        return clientService.getAllClients();
    }

    @DeleteMapping("/{cpf}")
    public ResponseEntity<Void> delete(@PathVariable String cpf) {
        clientService.deleteByCpf(cpf);
        return ResponseEntity.ok().build();

    }
}