package com.scarlxrd.books.model.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scarlxrd.books.model.DTO.AuthenticationDTO;
import com.scarlxrd.books.model.DTO.RefreshRequestDto;
import com.scarlxrd.books.model.DTO.RegisterDTO;
import com.scarlxrd.books.model.config.redis.RedisService;
import com.scarlxrd.books.model.config.security.TokenService;
import com.scarlxrd.books.model.entity.Role;
import com.scarlxrd.books.model.entity.User;
import com.scarlxrd.books.model.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.MediaType;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.AuthenticationManager;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("application-test.properties")
@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RedisService redisService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Test
    @DisplayName("It should return 200 OK and new tokens on a successful refresh.")
    void refresh_Success() throws Exception{

            // Arrange
            String oldRefreshToken = "valid.refresh.token";
            String oldJti = UUID.randomUUID().toString();

            String email = "user@user.com";

            String newAccessToken = "new.access.token";
            String newRefreshToken = "new.refresh.token";
            String newJti = UUID.randomUUID().toString();


            DecodedJWT decodedOld = mock(DecodedJWT.class);
            when(tokenService.decode(oldRefreshToken)).thenReturn(decodedOld);
            when(decodedOld.getId()).thenReturn(oldJti);
            when(decodedOld.getSubject()).thenReturn(email);


            when(redisService.isRefreshTokenValid(oldJti)).thenReturn(true);

            User mockUser = new User(email, "pass", Set.of(Role.USER));
            when(userRepository.findByEmail(email)).thenReturn(mockUser);


            when(tokenService.generateAccessToken(mockUser)).thenReturn(newAccessToken);
            when(tokenService.generateRefreshToken(mockUser)).thenReturn(newRefreshToken);

            when(tokenService.getJti(newRefreshToken)).thenReturn(newJti);
            when(tokenService.getExpiration(newRefreshToken)).thenReturn(Instant.now().plusSeconds(3600));

            RefreshRequestDto requestDto = new RefreshRequestDto(oldRefreshToken);

            // Act + Assert
            mockMvc.perform(
                            post("/auth/refresh")
                                    .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                                    .content(objectMapper.writeValueAsString(requestDto))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value(newAccessToken))
                    .andExpect(jsonPath("$.refreshToken").value(newRefreshToken));

            verify(redisService, times(1)).isRefreshTokenValid(oldJti);
            verify(redisService, times(1)).deleteRefreshToken(oldJti);
            verify(redisService, times(1)).saveRefreshToken(eq(newJti), anyLong());
    }

    @Test
    @DisplayName("It should return 401 UNAUTHORIZED if the refresh token is invalid (Redis).")
    void refresh_InvalidRedisToken_ReturnsUnauthorized() throws Exception{

        // Arrange
        String mockRefreshToken = "invalid.refresh.token";
        String mockRefreshJti = UUID.randomUUID().toString();
        DecodedJWT decodedOld = mock(DecodedJWT.class);

        when(tokenService.decode(mockRefreshToken)).thenReturn(decodedOld);
        when(decodedOld.getId()).thenReturn(mockRefreshJti);
        when(redisService.isRefreshTokenValid(mockRefreshJti)).thenReturn(false);

        RefreshRequestDto requestDto = new RefreshRequestDto(mockRefreshToken);

        // Act + Assert

        mockMvc.perform(post("/auth/refresh").contentType(String.valueOf(MediaType.APPLICATION_JSON)).content(objectMapper.writeValueAsString(requestDto))).andExpect(status().isUnauthorized());

        verify(tokenService, never()).generateAccessToken(any());
        verify(redisService, never()).deleteRefreshToken(any());


    }
    @Test
    @DisplayName("It should return 200 OK and add the token to the blacklist on logout.")
    void logout_Success() throws Exception{

        // Arrange
        String mockAccessToken ="valid.access.token";
        String authHeader = "Bearer " + mockAccessToken;
        String mockJti = UUID.randomUUID().toString();

        when(tokenService.getJti(mockAccessToken)).thenReturn(mockJti);
        when(tokenService.getExpiration(mockAccessToken)).thenReturn(Instant.now());

        // Act e Assert
        mockMvc.perform(post("/auth/logout").header("Authorization", authHeader)).andExpect(status().isOk());

        verify(redisService,times(1)).blackListToken(eq(mockJti),anyLong());
    }

    @Test
    @DisplayName("It should return 200 OK and not attempt to blacklist if the 'Authorization' header is missing/malformed.")
    void logout_MissingOrInvalidHeader_ReturnsOkWithoutBlacklisting() throws Exception{

           // Act e Assert
        mockMvc.perform(post("/auth/logout").header("Authorization","")).andExpect(status().isOk());
        mockMvc.perform(post("/auth/logout").header("Authorization", "InvalidToken")).andExpect(status().isOk());
        verify(redisService, never()).blackListToken(anyString(),anyLong());

    }

    @Test
    @DisplayName("Should return 200 and tokens on successful authentication.")
    void login_Success_ReturnsTokens() throws Exception{

        // Arrange
        AuthenticationDTO dto = new AuthenticationDTO("user@user.com","plainPassword");
        User mockUser = new User("user@user.com","encrypted-pass",Set.of(Role.USER));
        var authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(mockUser);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        String accessToken = "access.token";
        String refreshToken = "refresh.token";
        String jti = "some-jti";

        when(tokenService.generateAccessToken(mockUser)).thenReturn(accessToken);
        when(tokenService.generateRefreshToken(mockUser)).thenReturn(refreshToken);
        when(tokenService.getJti(refreshToken)).thenReturn(jti);
        when(tokenService.getExpiration(refreshToken)).thenReturn(Instant.now().plusSeconds(3600));

        // Act e Assert
        mockMvc.perform(post("/auth/login").contentType(String.valueOf(MediaType.APPLICATION_JSON)).content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(accessToken))
                .andExpect(jsonPath("$.refreshToken").value(refreshToken));

        verify(authenticationManager, times(1)).authenticate(any());
        verify(tokenService,times(1)).generateAccessToken(mockUser);
        verify(tokenService,times(1)).generateRefreshToken(mockUser);
        verify(redisService, times(1)).saveRefreshToken(eq(jti),anyLong());
    }

    @Test
    @DisplayName("Should return 401 when credentials are invalid")
    void login_InvalidCredentials_ReturnsUnauthorized() throws Exception{

        AuthenticationDTO dto = new AuthenticationDTO("user@user.com", "wrong");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        // Act e Assert
        mockMvc.perform(post("/auth/login").contentType(String.valueOf(MediaType.APPLICATION_JSON)).content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should create user and return 200")
    void register_Success() throws Exception{
        RegisterDTO dto = new RegisterDTO("new@user.com","strongPassword",Set.of(Role.USER));

        when(userRepository.findByEmail(dto.email())).thenReturn(null);

        mockMvc.perform(post("/auth/register").contentType(String.valueOf(MediaType.APPLICATION_JSON)).content(objectMapper.writeValueAsString(dto))).andExpect(status().isOk());

        verify(userRepository, times(1)).save(argThat(saveUser ->
                saveUser.getEmail().equals(dto.email())
                && saveUser.getPassword() != null
                && !saveUser.getPassword().equals(dto.password())
                ));


    }

    @Test
    @DisplayName("Should return 400 when email already exists")
    void register_ExistingEmail_ReturnsBadRequest() throws Exception{
        RegisterDTO dto = new RegisterDTO("existing@user.com","anyPass",Set.of(Role.USER));

        when(userRepository.findByEmail(dto.email())).thenReturn(new User("existing@user.com","encrypted", Set.of(Role.USER)));

        mockMvc.perform(post("/auth/register")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(userRepository, never()).save(any());
    }
}
