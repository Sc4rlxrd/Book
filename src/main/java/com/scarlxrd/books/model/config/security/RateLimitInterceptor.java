package com.scarlxrd.books.model.config.security;

import com.scarlxrd.books.model.config.redis.RedisService;
import com.scarlxrd.books.model.exception.TooManyRequestsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.support.RequestContextUtils;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisService redisService;
    @Value("${api.security.rate-limit.login-attempts}")
    private int loginAttempts;

    @Value("${api.security.rate-limit.login-timeout-seconds}")
    private int loginTimeout;

    public RateLimitInterceptor(RedisService redisService) {
        this.redisService = redisService;

    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getRequestURI().equals("/auth/login")) {
            String ip = request.getRemoteAddr();
            if (!redisService.isAllowed(ip, loginAttempts, loginTimeout)) {
                Long secondsLeft = redisService.getExpire(ip);
                WebApplicationContext context = RequestContextUtils.findWebApplicationContext(request);
                if (context != null) {
                    HandlerExceptionResolver resolver = context.getBean("handlerExceptionResolver", HandlerExceptionResolver.class);
                    resolver.resolveException(request, response, handler,
                            new TooManyRequestsException("Limit exceeded.", secondsLeft != null ? secondsLeft : loginTimeout));
                }

                return false;
            }
        }
        return true;
    }
}
