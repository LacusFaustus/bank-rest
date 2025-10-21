package com.bank.repository;

import com.bank.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_UserExists_ShouldReturnUser() {
        // Given
        User user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@bank.com")
                .role(User.Role.ROLE_USER)
                .build();
        entityManager.persistAndFlush(user);

        // When
        Optional<User> found = userRepository.findByUsername("testuser");

        // Then
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void findByUsername_UserNotExists_ShouldReturnEmpty() {
        // When
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void existsByUsername_UserExists_ShouldReturnTrue() {
        // Given
        User user = User.builder()
                .username("existinguser")
                .password("password")
                .email("existing@bank.com")
                .role(User.Role.ROLE_USER)
                .build();
        entityManager.persistAndFlush(user);

        // When
        boolean exists = userRepository.existsByUsername("existinguser");

        // Then
        assertTrue(exists);
    }
}
