package com.scarlxrd.books.model.config.actuator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class MonitoringUserConfig {

    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager(PasswordEncoder encoder){
        UserDetails prometheusUser = User.builder()
                .username("admin")
                .password(encoder.encode("admin"))
                .roles("PROMETHEUS")
                .build();
        return new InMemoryUserDetailsManager(prometheusUser);
    }
}

