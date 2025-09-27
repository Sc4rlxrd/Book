package com.scarlxrd.books.model.controller;

import com.scarlxrd.books.model.DTO.*;
import com.scarlxrd.books.model.config.redis.RedisService;
import com.scarlxrd.books.model.config.security.TokenService;
import com.scarlxrd.books.model.entity.Role;
import com.scarlxrd.books.model.entity.User;
import com.scarlxrd.books.model.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Set;

@RestController
@RequestMapping("auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository repository;
    @Autowired
    TokenService tokenService;
    @Autowired
    RedisService redisService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody @Valid AuthenticationDTO data){
        var auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(data.email(), data.password()));
        User user = (User) auth.getPrincipal();
        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        // salve o refresh no redis
        var jti = tokenService.getJti(refreshToken);
        long ttl = tokenService.getExpiration(refreshToken).getEpochSecond() - Instant.now().getEpochSecond();
        redisService.saveRefreshToken(jti,ttl);
        return ResponseEntity.ok(new TokenResponseDto(accessToken,refreshToken));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterDTO data){
        if(this.repository.findByEmail(data.email()) != null) return ResponseEntity.badRequest().build();

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = new User(data.email(), encryptedPassword, Set.of(Role.USER));

        this.repository.save(newUser);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(@RequestBody @Valid RefreshRequestDto requestDto){
        var decoded = tokenService.decode(requestDto.refreshToken());
        String refreshJti = decoded.getId();
        if(!redisService.isRefreshTokenValid(refreshJti)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = (User) repository.findByEmail(decoded.getSubject());
        String newAccessToken = tokenService.generateAccessToken(user);
        String newRefreshToken = tokenService.generateRefreshToken(user);

        // invalida o antigo refresh
        redisService.deleteRefreshToken(refreshJti);

        // salva um novo refresh token
        String newJti = tokenService.getJti(newRefreshToken);
        long ttl = tokenService.getExpiration(newRefreshToken).getEpochSecond() - Instant.now().getEpochSecond();
        redisService.saveRefreshToken(newJti,ttl);

        return ResponseEntity.ok(new TokenResponseDto(newAccessToken,newRefreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader ){
        if(authHeader!= null && authHeader.startsWith("Bearer ")){
            String token = authHeader.substring(7);
            String jti = tokenService.getJti(token);
            long ttl = tokenService.getExpiration(token).getEpochSecond() - Instant.now().getEpochSecond();
            redisService.blackListToken(jti,ttl);
        }
        return ResponseEntity.ok().build();
    }

}
