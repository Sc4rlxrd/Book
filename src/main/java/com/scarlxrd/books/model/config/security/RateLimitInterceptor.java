package com.scarlxrd.books.model.config.security;

import com.scarlxrd.books.model.config.redis.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisService redisService;
    @Value("${api.security.rate-limit.login-attempts}")
    private int loginAttempts;

    @Value("${api.security.rate-limit.login-timeout-seconds}")
    private int loginTimeout;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getRequestURI().equals("/auth/login")) {
            String ip = request.getRemoteAddr();

            if (!redisService.isAllowed(ip, loginAttempts, loginTimeout)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Too many attempts. Try again in " + loginTimeout + " seconds.\"}");
                return false;
            }
        }
        return true;
    }
}
