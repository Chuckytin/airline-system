package com.airline.config;

import com.airline.enums.UserRole;
import com.airline.model.User;
import com.airline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea automáticamente un usuario administrador al iniciar la aplicación, si no existe.
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.full-name}")
    private String adminFullName;

    @Override
    public void run(String @NonNull ... args) {
        try {
            if (userRepository.existsByEmail(adminEmail)) {
                log.info("Admin seeder skipped: user with email {} already exists", adminEmail);
                return;
            }

            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .fullName(adminFullName)
                    .role(UserRole.ROLE_ADMIN)
                    .active(true)
                    .build();

            userRepository.save(admin);
            log.info("Admin seeder completed: admin user created with email {}", adminEmail);

        } catch (Exception e) {
            log.error("Admin seeder failed: {}", e.getMessage(), e);
            throw e;
        }
    }

}