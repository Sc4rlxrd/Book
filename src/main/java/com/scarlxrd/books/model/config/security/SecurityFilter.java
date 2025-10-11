package com.scarlxrd.books.model.config.security;

import com.scarlxrd.books.model.config.redis.RedisService;
import com.scarlxrd.books.model.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Slf4j
@Component
public class SecurityFilter extends OncePerRequestFilter {
    @Autowired
    TokenService tokenService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RedisService redisService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = this.recoverToken(request);
        if (token != null) {
            try {
                var decoded = tokenService.decode(token);
                String jti = decoded.getId();

                // verificar se não está na blackList
                if (redisService.isBlackListed(jti)){
                    log.warn("Token revogado detectado: jti={}", jti);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                String login = decoded.getSubject();
                UserDetails user = userRepository.findByEmail(login);
                var authentication = new UsernamePasswordAuthenticationToken(user,null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Usuário autenticado: {}", login);
//
//                if ("/auth/refresh".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod())) {
//                    log.info("Refresh token usado: jti={}, user={}", jti, login);
//                    filterChain.doFilter(request, response);
//                    return;
//                }
                if ("/auth/logout".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod())) {
                    long ttl = Math.max(decoded.getExpiresAt().toInstant().getEpochSecond() - Instant.now().getEpochSecond(), 0);
                    redisService.blackListToken(jti, ttl);
                    log.info("Token colocado na blacklist (logout): jti={}, user={}", jti, login);
                    filterChain.doFilter(request, response);
                    return;
                }
            } catch (Exception e) {
                log.error("Falha na autenticação do token: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        filterChain.doFilter(request,response);
    }
    private String recoverToken(HttpServletRequest request){
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            return null;
        }
        String normalizedHeader = authHeader.trim().toUpperCase();
        if (!normalizedHeader.startsWith("BEARER ")) {
            return null;
        }
        return authHeader.substring(authHeader.indexOf("Bearer ") + 7).trim();
    }

}