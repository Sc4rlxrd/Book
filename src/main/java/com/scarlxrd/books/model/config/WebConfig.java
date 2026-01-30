package com.scarlxrd.books.model.config;

import com.scarlxrd.books.model.config.security.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Aplica o interceptor apenas nas rotas de autenticação
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/auth/**");
    }
}
