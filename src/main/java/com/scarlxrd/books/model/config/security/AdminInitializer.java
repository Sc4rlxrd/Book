package com.scarlxrd.books.model.config.security;

import com.scarlxrd.books.model.entity.Role;
import com.scarlxrd.books.model.entity.User;
import com.scarlxrd.books.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;
@Component
public class AdminInitializer  implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail(adminEmail) == null) {
            User admin = new User(
                    adminEmail,
                    passwordEncoder.encode(adminPassword),
                    Set.of(Role.ADMIN, Role.USER) // admin também tem permissões de usuário
            );
            userRepository.save(admin);
            System.out.println(" Usuário ADMIN criado com sucesso: " + adminEmail);
        } else {
            System.out.println(" Usuário ADMIN já existe, não será recriado.");
        }
    }
}
