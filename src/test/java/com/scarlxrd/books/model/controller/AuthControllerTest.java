package com.scarlxrd.books.model.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scarlxrd.books.model.DTO.RefreshRequestDto;
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
}
