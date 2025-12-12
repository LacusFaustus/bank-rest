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

@Component
@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            log.info("Starting data initialization for local development...");

            if (userRepository.count() > 0) {
                log.info("Data already initialized. Skipping...");
                return;
            }

            initializeAdmin();
            initializeUsers();

            log.info("Data initialization completed");
            printCredentials();

        } catch (Exception e) {
            log.error("Error during data initialization: {}", e.getMessage(), e);
            // Не бросаем исключение дальше, чтобы приложение могло запуститься
        }
    }

    private void initializeAdmin() {
        if (userRepository.findByUsername("admin").isPresent()) {
            return;
        }

        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("Admin123!"))
                .email("admin@bank.com")
                .role(User.Role.ROLE_ADMIN)
                .build();

        User savedAdmin = userRepository.save(admin);
        createCardForUser(savedAdmin, "Admin Card", "4111111111111111",
                LocalDate.now().plusYears(3), new BigDecimal("100000.00"));

        log.info("✅ Admin user created: admin/Admin123!");
    }

    private void initializeUsers() {
        String[][] users = {
                {"john.doe", "john.doe@bank.com", "John's Primary Card", "4111111111111112"},
                {"jane.smith", "jane.smith@bank.com", "Jane's Primary Card", "5111111111111111"},
                {"mike.johnson", "mike.johnson@bank.com", "Mike's Business Card", "371111111111111"}
        };

        for (String[] userData : users) {
            createUserIfNotExists(userData[0], "User123!", userData[1],
                    User.Role.ROLE_USER, userData[2], userData[3]);
        }
    }

    private void createUserIfNotExists(String username, String password, String email,
                                       User.Role role, String cardName, String cardNumber) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .role(role)
                .build();

        User savedUser = userRepository.save(user);
        createCardForUser(savedUser, cardName, cardNumber,
                LocalDate.now().plusYears(2), new BigDecimal("5000.00"));

        log.info("✅ User created: {}/{}", username, password);
    }

    private void createCardForUser(User user, String cardHolder, String cardNumber,
                                   LocalDate expiryDate, BigDecimal balance) {
        String encryptedCardNumber = encryptionService.encrypt(cardNumber);

        Card card = Card.builder()
                .cardNumber(encryptedCardNumber)
                .cardHolder(cardHolder.toUpperCase())
                .expiryDate(expiryDate)
                .balance(balance)
                .status(Card.CardStatus.ACTIVE)
                .user(user)
                .blockRequested(false)
                .build();

        cardRepository.save(card);
        log.debug("Card created for user: {}", user.getUsername());
    }

    private void printCredentials() {
        log.info("""
                
                ==========================================
                🏦 BANK CARD MANAGEMENT SYSTEM - LOCAL DEV
                ==========================================
                
                📊 Application URL: http://localhost:8080
                📚 API Documentation: http://localhost:8080/swagger-ui.html
                📈 API Docs (JSON): http://localhost:8080/v3/api-docs
                🏥 Health Check: http://localhost:8080/actuator/health
                
                ==========================================
                👥 TEST CREDENTIALS
                ==========================================
                
                👑 ADMINISTRATOR:
                Username: admin
                Password: Admin123!
                
                👤 REGULAR USERS:
                Username: john.doe
                Password: User123!
                
                Username: jane.smith
                Password: User123!
                
                Username: mike.johnson
                Password: User123!
                
                ==========================================
                💾 DATABASE CONSOLE
                ==========================================
                
                H2 Console: http://localhost:8080/h2-console
                JDBC URL: jdbc:h2:mem:bankdb
                Username: sa
                Password: (empty)
                
                ==========================================
                """);
    }
}
