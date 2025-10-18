package com.bank.config;

import com.bank.entity.Card;
import com.bank.entity.User;
import com.bank.repository.CardRepository;
import com.bank.repository.UserRepository;
import com.bank.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

// Временно отключаем инициализатор данных для отладки
// @Component
// @Profile("local")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Data initializer is temporarily disabled for debugging");

        // Временно закомментируем всю логику инициализации
        /*
        log.info("Initializing data for local development...");

        // Создаем администратора
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@bank.com")
                    .role(User.Role.ROLE_ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Admin user created: admin/admin123");

            // Создаем карту для администратора
            String encryptedCardNumber = encryptionService.encrypt("1234567890123456");
            log.debug("Encrypted card number length: {}", encryptedCardNumber.length());

            Card adminCard = Card.builder()
                    .cardNumber(encryptedCardNumber)
                    .cardHolder("ADMIN USER")
                    .expiryDate(LocalDate.now().plusYears(2))
                    .balance(new BigDecimal("10000.00"))
                    .status(Card.CardStatus.ACTIVE)
                    .user(admin)
                    .build();
            cardRepository.save(adminCard);
            log.info("Admin card created with number: **** **** **** 3456");
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
            log.info("User created: user1/user123");

            // Создаем карту для пользователя
            String encryptedCardNumber = encryptionService.encrypt("9876543210987654");
            Card userCard = Card.builder()
                    .cardNumber(encryptedCardNumber)
                    .cardHolder("USER ONE")
                    .expiryDate(LocalDate.now().plusYears(1))
                    .balance(new BigDecimal("5000.00"))
                    .status(Card.CardStatus.ACTIVE)
                    .user(user)
                    .build();
            cardRepository.save(userCard);
            log.info("User card created with number: **** **** **** 7654");
        }

        log.info("Data initialization completed");

        // Выводим информацию для доступа
        log.info("=== Local Development Info ===");
        log.info("H2 Console: http://localhost:8080/h2-console");
        log.info("JDBC URL: jdbc:h2:mem:bankdb");
        log.info("Username: sa");
        log.info("Password: (empty)");
        log.info("Swagger UI: http://localhost:8080/swagger-ui.html");
        log.info("=== Available Users ===");
        log.info("Admin: admin/admin123");
        log.info("User: user1/user123");
        log.info("==============================");
        */
    }
}
