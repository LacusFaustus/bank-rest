package com.bank.repository;

import com.bank.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_ExistingUser_ShouldReturnUser() {
        // Given
        User user = User.builder()
                .username("testuser")
                .password("encodedPass")
                .email("test@example.com")
                .role(User.Role.ROLE_USER)
                .build();

        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByUsername("testuser");

        // Then
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void existsByUsername_ShouldWork() {
        // Given
        User user = User.builder()
                .username("existing")
                .password("pass")
                .email("email@test.com")
                .role(User.Role.ROLE_USER)
                .build();

        userRepository.save(user);

        // When
        boolean exists = userRepository.existsByUsername("existing");

        // Then
        assertTrue(exists);

        // When - несуществующий пользователь
        boolean notExists = userRepository.existsByUsername("nonexistent");

        // Then
        assertFalse(notExists);
    }

    @Test
    void findByEmail_ShouldReturnUser() {
        // Given
        User user = User.builder()
                .username("emailuser")
                .password("pass")
                .email("unique@email.com")
                .role(User.Role.ROLE_USER)
                .build();

        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByEmail("unique@email.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("unique@email.com", found.get().getEmail());
    }
}
