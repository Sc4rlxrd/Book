package com.scarlxrd.books.model.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.scarlxrd.books.model.DTO.ClientRequestDTO;
import com.scarlxrd.books.model.config.security.TokenService;
import com.scarlxrd.books.model.exception.ClientAlreadyExistsException;
import com.scarlxrd.books.model.exception.ClientNotFoundException;
import com.scarlxrd.books.model.repository.ClientRepository;
import com.scarlxrd.books.model.repository.UserRepository;
import com.scarlxrd.books.model.service.ClientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("application-test.properties")
public class ClientControllerExceptionTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ClientService clientService;
    @MockitoBean
    private ClientRepository clientRepository;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private TokenService tokenService;

    @Test
    @DisplayName("Deve retornar 404 Not Found quando cliente não existe")
    void deleteClientNotFound() throws Exception {
        String cpf = "647.564.290-84";
        doThrow(new ClientNotFoundException("Cliente não encontrado"))
                .when(clientService).deleteByCpf(cpf);

        mockMvc.perform(delete("/v1/clients/{cpf}", cpf))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Client not found"))
                .andExpect(jsonPath("$.detail").value("Cliente não encontrado"));
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict quando cliente já existe")
    void createClientAlreadyExists() throws Exception {
        ClientRequestDTO requestDTO = new ClientRequestDTO();
        requestDTO.setName("Guilherme");
        requestDTO.setLastName("Silva");
        requestDTO.setCpfNumber("647.564.290-84");

        doThrow(new ClientAlreadyExistsException("Cliente já existe")).when(clientService).createClient(any(ClientRequestDTO.class));
        mockMvc.perform(post("/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Client already exists"))
                .andExpect(jsonPath("$.detail").value("Cliente já existe"));

    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando request de cliente é inválido")
    void createClientValidationError() throws Exception {
        ClientRequestDTO requestDTO = new ClientRequestDTO();
        requestDTO.setName(""); // inválido
        requestDTO.setLastName("Silva");
        requestDTO.setCpfNumber(""); // inválido

        mockMvc.perform(post("/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.detail").value("Um ou mais campos são inválidos"))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.cpfNumber").exists());
    }

    @Test
    @DisplayName("Deve retornar 500 Internal Server Error quando ocorre exceção inesperada")
    void createClientGenericError() throws Exception {
        ClientRequestDTO requestDTO = new ClientRequestDTO();
        requestDTO.setName("Guilherme");
        requestDTO.setLastName("Silva");
        requestDTO.setCpfNumber("158.248.900-99");

        doThrow(new RuntimeException("Erro inesperado"))
                .when(clientService).createClient(any(ClientRequestDTO.class));

        mockMvc.perform(post("/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal server error"))
                .andExpect(jsonPath("$.detail").value("Ocorreu um erro inesperado"));
    }
}
