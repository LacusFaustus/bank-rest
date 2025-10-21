package com.bank.entity;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void userAuthorities_ShouldReturnCorrectRoles() {
        // Given
        User user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@bank.com")
                .role(User.Role.ROLE_ADMIN)
                .build();

        // When
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Then
        assertNotNull(authorities);
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void userAccountStatus_ShouldAlwaysBeActive() {
        // Given
        User user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@bank.com")
                .role(User.Role.ROLE_USER)
                .build();

        // Then
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }

    @Test
    void userEqualsAndHashCode_ShouldWorkCorrectly() {
        // Given
        User user1 = User.builder()
                .id(1L)
                .username("user1")
                .email("user1@bank.com")
                .build();

        User user2 = User.builder()
                .id(1L)
                .username("user1")
                .email("user1@bank.com")
                .build();

        User user3 = User.builder()
                .id(2L)
                .username("user2")
                .email("user2@bank.com")
                .build();

        // Then
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
    }
}
