package com.bank.config;

import com.bank.entity.User;
import com.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class SimpleDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Creating test users...");

        // Создаем администратора
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@bank.com")
                    .role(User.Role.ROLE_ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("✅ Admin user created: admin/admin123");
        }

        // Создаем обычного пользователя
        if (userRepository.findByUsername("user1").isEmpty()) {
            User user = User.builder()
                    .username("user1")
                    .password(passwordEncoder.encode("user123"))
                    .email("user1@bank.com")
                    .role(User.Role.ROLE_USER)
                    .build();
            userRepository.save(user);
            log.info("✅ User created: user1/user123");
        }

        log.info("✅ Test users created successfully");
        log.info("=== Test Credentials ===");
        log.info("Admin: admin/admin123");
        log.info("User:  user1/user123");
        log.info("=========================");
    }
}
