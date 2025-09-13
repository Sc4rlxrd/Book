package com.scarlxrd.books.model.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.scarlxrd.books.model.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class TokenService {
    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            List<String> roles = user.getAuthorities().stream().map(auth -> auth.getAuthority()).toList();
            String token = JWT.create()
                    // quem está criando o token
                    .withIssuer("book-api")
                    // quem vai usar no caso vai o user passado anteriormente
                    .withSubject(user.getEmail())
                    // os papeis dele
                    .withClaim("roles",roles)
                    // quando vai expirar o token
                    .withExpiresAt(generateExpirationDate()).
                    sign(algorithm);
            return token;

        } catch (JWTCreationException exception) {
            throw  new RuntimeException("Error while generating token", exception);
        }
    }
    public DecodedJWT validateToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm) // use esse algoritimo para validar
                    .withIssuer("book-api") // quem criou a chave
                    .build()
                    .verify(token); // verificar o token para ver se é valido

        } catch (JWTVerificationException e) {
            throw new RuntimeException("Error while verify token", e);
        }
    }


    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusHours(1).toInstant(ZoneOffset.of("-03:00"));
    }
}