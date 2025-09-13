package com.scarlxrd.books.model.controller;

import com.scarlxrd.books.model.DTO.AuthenticationDTO;
import com.scarlxrd.books.model.DTO.LoginResponseDto;
import com.scarlxrd.books.model.DTO.RegisterDTO;
import com.scarlxrd.books.model.config.security.TokenService;
import com.scarlxrd.books.model.entity.Role;
import com.scarlxrd.books.model.entity.User;
import com.scarlxrd.books.model.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid AuthenticationDTO data){
        var auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(data.email(),data.password()));
        var token = tokenService.generateToken((User) auth.getPrincipal());
        return ResponseEntity.ok(new LoginResponseDto(token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterDTO data){
        if(this.repository.findByEmail(data.email()) != null) return ResponseEntity.badRequest().build();

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = new User(data.email(), encryptedPassword, Set.of(Role.USER));

        this.repository.save(newUser);

        return ResponseEntity.ok().build();
    }
}
