package com.scarlxrd.books.model.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scarlxrd.books.model.DTO.BookRequestDTO;
import com.scarlxrd.books.model.DTO.ClientRequestDTO;
import com.scarlxrd.books.model.DTO.ClientResponseDTO;

import com.scarlxrd.books.model.config.security.TokenService;
import com.scarlxrd.books.model.repository.ClientRepository;
import com.scarlxrd.books.model.repository.UserRepository;
import com.scarlxrd.books.model.service.ClientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;


import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import java.util.List;
import java.util.UUID;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("application-test.properties")

class ClientControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;
    @MockitoBean
    private ClientRepository clientRepository;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private TokenService tokenService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve criar um cliente e retornar status 201 CREATED com Role ADMIN")
    void shouldCreateClientAndReturnCreatedStatusWithTheRoleAdmin() throws Exception {
        ClientRequestDTO requestDTO = new ClientRequestDTO();
        requestDTO.setName("Guilherme");
        requestDTO.setLastName("Silva");
        requestDTO.setCpfNumber("158.248.900-99");

        BookRequestDTO bookRequestDTO = new BookRequestDTO();
        bookRequestDTO.setAuthor("Robert C. Martin");
        bookRequestDTO.setTitle("Clean Code");
        bookRequestDTO.setIsbn("9780132350884");
        requestDTO.setBooks(List.of(bookRequestDTO));

        ClientResponseDTO responseDTO = new ClientResponseDTO(
                UUID.randomUUID(),
                "Guilherme",
                "Silva",
                "158.248.900-99",
                List.of()
        );

        when(clientService.createClient(any(ClientRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/clients")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Guilherme"))
                .andExpect(jsonPath("$.lastName").value("Silva"))
                .andExpect(jsonPath("$.cpf").value("158.248.900-99"));
    }

    @Test
    @DisplayName("Deve criar um cliente e retornar status 201 CREATED com Role USER")
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldCreateClientAndReturnCreatedStatusWithTheRoleUser() throws Exception {
        ClientRequestDTO requestDTO = new ClientRequestDTO();
        requestDTO.setName("Guilherme");
        requestDTO.setLastName("Silva");
        requestDTO.setCpfNumber("158.248.900-99");

        BookRequestDTO bookRequestDTO = new BookRequestDTO();
        bookRequestDTO.setAuthor("Robert C. Martin");
        bookRequestDTO.setTitle("Clean Code");
        bookRequestDTO.setIsbn("9780132350884");
        requestDTO.setBooks(List.of(bookRequestDTO));

        ClientResponseDTO responseDTO = new ClientResponseDTO(
                UUID.randomUUID(),
                "Guilherme",
                "Silva",
                "158.248.900-99",
                List.of()
        );

        when(clientService.createClient(any(ClientRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Guilherme"))
                .andExpect(jsonPath("$.lastName").value("Silva"))
                .andExpect(jsonPath("$.cpf").value("158.248.900-99"));
    }

    @Test
    @DisplayName("Deve retornar a lista de clientes com status 200 OK com Role USER")
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnListOfClientsWithTheRoleUser() throws Exception {
        ClientResponseDTO response1 = new ClientResponseDTO(
                UUID.randomUUID(),
                "Guilherme",
                "Silva",
                "158.248.900-99",
                List.of()
        );
        ClientResponseDTO response2 = new ClientResponseDTO(
                UUID.randomUUID(),
                "Maria",
                "Silva",
                "971.456.040-35",
                List.of()
        );
        List<ClientResponseDTO> clients = List.of(response1,response2);

        //  Comportamento do mock: quando clientService.getAllClients() for chamado, retorne a lista
        when(clientService.getAllClients()).thenReturn(clients);

        // Simula a requisição GET e valida o resultado
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk()) // Espera status 200
                .andExpect(jsonPath("$").isArray()) // Espera uma lista
                .andExpect(jsonPath("$.length()").value(2)) // Verifica o tamanho da lista
                .andExpect(jsonPath("$[0].name").value("Guilherme")) // Verifica o nome do primeiro item
                .andExpect(jsonPath("$[0].lastName").value("Silva"))
                .andExpect(jsonPath("$[0]cpf").value("158.248.900-99"))
                .andExpect(jsonPath("$[1].name").value("Maria"))   // Verifica o nome do segundo item
                .andExpect(jsonPath("$[1].lastName").value("Silva"))
                .andExpect(jsonPath("$[1]cpf").value("971.456.040-35"));
    }

    @Test
    @DisplayName("Deve retornar a lista de clientes com status 200 OK com Role ADMIN")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldReturnListOfClientsWithTheRoleAdmin() throws Exception {
        ClientResponseDTO response1 = new ClientResponseDTO(
                UUID.randomUUID(),
                "Guilherme",
                "Silva",
                "158.248.900-99",
                List.of()
        );
        ClientResponseDTO response2 = new ClientResponseDTO(
                UUID.randomUUID(),
                "Maria",
                "Silva",
                "971.456.040-35",
                List.of()
        );
        List<ClientResponseDTO> clients = List.of(response1,response2);

        //  Comportamento do mock: quando clientService.getAllClients() for chamado, retorne a lista
        when(clientService.getAllClients()).thenReturn(clients);

        // Simula a requisição GET e valida o resultado
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk()) // Espera status 200
                .andExpect(jsonPath("$").isArray()) // Espera uma lista
                .andExpect(jsonPath("$.length()").value(2)) // Verifica o tamanho da lista
                .andExpect(jsonPath("$[0].name").value("Guilherme")) // Verifica o nome do primeiro item
                .andExpect(jsonPath("$[0].lastName").value("Silva"))
                .andExpect(jsonPath("$[0]cpf").value("158.248.900-99"))
                .andExpect(jsonPath("$[1].name").value("Maria"))   // Verifica o nome do segundo item
                .andExpect(jsonPath("$[1].lastName").value("Silva"))
                .andExpect(jsonPath("$[1]cpf").value("971.456.040-35"));
    }

    // Teste para o endpoint DELETE /v1/clients/{cpf}
    @Test
    @DisplayName("Deve deletar um cliente e retornar status 200 OK Com Role ADMIN")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldDeleteClientAndReturnOkStatusWithTheRoleAdmin() throws Exception {
        String cpf = "971.456.040-35";
        //  Comportamento do mock: quando clientService.deleteByCpf() for chamado, não faça nada (void)
        doNothing().when(clientService).deleteByCpf(cpf);
        //  Simula a requisição DELETE e valida o resultado
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/clients/{cpf}", cpf))
                .andExpect(MockMvcResultMatchers.status().isOk()); // Espera status 200
        //  Verifica se o mét0do do serviço foi realmente chamado
        verify(clientService, times(1)).deleteByCpf(cpf);
    }

}
@WebMvcTest(ClientControllerSecurityTest.class)
@AutoConfigureMockMvc
@ActiveProfiles("application-test.properties")
class ClientControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;
    @MockitoBean
    private ClientRepository clientRepository;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private TokenService tokenService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName(" Deve retornar 401 se não autenticado")
    void getClientsUnauthorizedIfNoUser() throws Exception {
        mockMvc.perform(get("/v1/clients"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    @DisplayName(" Deve retornar 403 se USER tenta excluir sem ADMIN")
    @WithMockUser(username = "user", roles = {"USER"})
    void deleteClientForbiddenForUser() throws Exception {
        mockMvc.perform(delete("/v1/clients/{cpf}", "971.456.040-35"))
                .andExpect(status().isForbidden());
    }
    @Test
    @DisplayName(" Deve retornar 403 se não autenticar")
    void postClientUnauthorizedIfNoToken() throws Exception {
        ClientRequestDTO requestDTO = new ClientRequestDTO();
        requestDTO.setName("Guilherme");
        requestDTO.setLastName("Silva");
        requestDTO.setCpfNumber("158.248.900-99");

        mockMvc.perform(post("/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }

}
