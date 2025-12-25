package com.scarlxrd.books.model.controller;


import com.scarlxrd.books.model.DTO.BookRequestDTO;
import com.scarlxrd.books.model.DTO.BookResponseDTO;
import com.scarlxrd.books.model.DTO.ClientRequestDTO;
import com.scarlxrd.books.model.DTO.ClientResponseDTO;
import com.scarlxrd.books.model.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @GetMapping("/page") // Rota específica para paginação
    public ResponseEntity<Page<ClientResponseDTO>> getPaged(Pageable pageable) {
        Page<ClientResponseDTO> pagedResponse = clientService.getAllClientsPage(pageable);
        return ResponseEntity.ok(pagedResponse);
    }
    @DeleteMapping("/{cpf}")
    public ResponseEntity<Void> delete(@PathVariable String cpf) {
        clientService.deleteByCpf(cpf);
        return ResponseEntity.ok().build();

    }
    @PostMapping("/{cpf}")
    public ResponseEntity<BookResponseDTO> addBookToClient(@PathVariable String cpf, @Valid @RequestBody BookRequestDTO bookRequestDTO) {
        BookResponseDTO response = clientService.addBookToClient(cpf, bookRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}