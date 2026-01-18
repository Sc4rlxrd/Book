package com.scarlxrd.books.model.controller;


import com.scarlxrd.books.model.DTO.BookRequestDTO;
import com.scarlxrd.books.model.DTO.BookResponseDTO;
import com.scarlxrd.books.model.DTO.ClientRequestDTO;
import com.scarlxrd.books.model.DTO.ClientResponseDTO;
import com.scarlxrd.books.model.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/v1/clients")
@Tag(name = "Clients", description = "Gerenciamento de clientes")
@SecurityRequirement(name = "bearerAuth")
public class ClientController {

    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @Operation(
            summary = "Criar cliente",
            description = "Cria um novo cliente no sistema",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                    @ApiResponse(responseCode = "409", description = "Cliente já existe")
            }
    )
    @PostMapping
    public ResponseEntity<ClientResponseDTO> createClient(@Valid @RequestBody ClientRequestDTO requestDTO) {

        ClientResponseDTO newClientResponse = clientService.createClient(requestDTO);
        return new ResponseEntity<>(newClientResponse, HttpStatus.CREATED);

    }
    @Operation(
            summary = "Listar clientes",
            description = "Retorna todos os clientes cadastrados"
    )
    @GetMapping
    public List<ClientResponseDTO> get() {
        // Chama o serviço para obter todos os clientes (já em formato DTO)
        return clientService.getAllClients();
    }
    @Operation(
            summary = "Listar clientes paginados",
            description = "Retorna clientes com paginação"
    )
    @GetMapping("/page") // Rota específica para paginação
    public ResponseEntity<Page<ClientResponseDTO>> getPaged(@ParameterObject Pageable pageable) {
        Page<ClientResponseDTO> pagedResponse = clientService.getAllClientsPage(pageable);
        return ResponseEntity.ok(pagedResponse);
    }
    @Operation(
            summary = "Excluir cliente",
            description = "Remove um cliente pelo CPF (somente ADMIN)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cliente removido"),
                    @ApiResponse(responseCode = "403", description = "Acesso negado"),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
            }
    )
    @DeleteMapping("/{cpf}")
    public ResponseEntity<Void> delete(@PathVariable String cpf) {
        clientService.deleteByCpf(cpf);
        return ResponseEntity.ok().build();

    }
    @Operation(
            summary = "Adicionar livro ao cliente",
            description = "Adiciona um livro a um cliente existente",
           responses = {
                   @ApiResponse(responseCode = "200", description = "Livro adicionado"),
                   @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
           }
    )
    @PostMapping("/{cpf}")
    public ResponseEntity<BookResponseDTO> addBookToClient(@PathVariable String cpf, @Valid @RequestBody BookRequestDTO bookRequestDTO) {
        BookResponseDTO response = clientService.addBookToClient(cpf, bookRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @Operation(
            summary = "Buscar cliente por CPF",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
            }
    )
    @GetMapping("/{cpf}")
    public ResponseEntity<ClientResponseDTO> getClientByCpf(@PathVariable String cpf) {
        ClientResponseDTO response = clientService.getClientByCpf(cpf);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}