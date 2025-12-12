package com.bank.controller;

import com.bank.entity.User;
import com.bank.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void getUserProfile_ShouldReturnUserWithoutPassword() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .email("test@bank.com")
                .role(User.Role.ROLE_USER)
                .build();

        when(userService.getUserByUsername("testuser")).thenReturn(user);

        // When
        ResponseEntity<User> response = userController.getUserProfile(user);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getPassword()); // Пароль не должен возвращаться
        assertEquals("testuser", response.getBody().getUsername());
        assertEquals("test@bank.com", response.getBody().getEmail());
    }

    @Test
    void getUserById_AdminAccess_ShouldReturnUserWithoutPassword() {
        // Given
        User user = User.builder()
                .id(2L)
                .username("otheruser")
                .password("encodedPassword")
                .email("other@bank.com")
                .build();

        when(userService.getUserById(2L)).thenReturn(user);

        // When
        ResponseEntity<User> response = userController.getUserById(2L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getPassword());
        assertEquals("otheruser", response.getBody().getUsername());
    }

    @Test
    void updateUserProfile_ShouldUpdateEmail() {
        // Given
        User currentUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("old@bank.com")
                .build();

        User updatedUser = User.builder()
                .email("new@bank.com")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("new@bank.com")
                .build();

        when(userService.getUserByUsername("testuser")).thenReturn(currentUser);
        when(userService.updateUser(any(User.class))).thenReturn(savedUser);

        // When
        ResponseEntity<User> response = userController.updateUserProfile(currentUser, updatedUser);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("new@bank.com", response.getBody().getEmail());
        verify(userService).updateUser(argThat(user ->
                user.getEmail().equals("new@bank.com")
        ));
    }

    @Test
    void updateUserProfile_ShouldOnlyUpdateAllowedFields() {
        // Given
        User currentUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("old@bank.com")
                .role(User.Role.ROLE_USER)
                .build();

        User updatedUser = User.builder()
                .username("hacker") // Это поле не должно обновляться
                .email("new@bank.com")
                .role(User.Role.ROLE_ADMIN) // Это поле не должно обновляться
                .password("newpassword") // Это поле не должно обновляться
                .build();

        User savedUser = User.builder()
                .id(1L)
                .username("testuser") // Должно остаться прежним
                .email("new@bank.com") // Должно обновиться
                .role(User.Role.ROLE_USER) // Должно остаться прежним
                .build();

        when(userService.getUserByUsername("testuser")).thenReturn(currentUser);
        when(userService.updateUser(any(User.class))).thenReturn(savedUser);

        // When
        ResponseEntity<User> response = userController.updateUserProfile(currentUser, updatedUser);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("testuser", response.getBody().getUsername()); // username не изменился
        assertEquals("new@bank.com", response.getBody().getEmail()); // email изменился
        assertEquals(User.Role.ROLE_USER, response.getBody().getRole()); // role не изменилась
    }

    @Test
    void updateUserProfile_EmptyUpdate_ShouldReturnSameUser() {
        // Given
        User currentUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@bank.com")
                .build();

        User updatedUser = new User(); // Пустой объект

        when(userService.getUserByUsername("testuser")).thenReturn(currentUser);
        when(userService.updateUser(currentUser)).thenReturn(currentUser);

        // When
        ResponseEntity<User> response = userController.updateUserProfile(currentUser, updatedUser);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("test@bank.com", response.getBody().getEmail());
    }

    @Test
    void getUserProfile_UserNotFound_ShouldThrowException() {
        // Given
        User user = User.builder()
                .username("nonexistent")
                .build();

        when(userService.getUserByUsername("nonexistent"))
                .thenThrow(new RuntimeException("User not found"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userController.getUserProfile(user);
        });
    }

    @Test
    void getUserById_UserNotFound_ShouldThrowException() {
        // Given
        when(userService.getUserById(999L))
                .thenThrow(new RuntimeException("User not found"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userController.getUserById(999L);
        });
    }

    @Test
    void updateUserProfile_NullEmail_ShouldNotUpdate() {
        // Given
        User currentUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@bank.com")
                .build();

        User updatedUser = User.builder()
                .email(null)
                .build();

        when(userService.getUserByUsername("testuser")).thenReturn(currentUser);
        when(userService.updateUser(currentUser)).thenReturn(currentUser);

        // When
        ResponseEntity<User> response = userController.updateUserProfile(currentUser, updatedUser);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("test@bank.com", response.getBody().getEmail());
    }

    @Test
    void getUserProfile_UserWithNullPassword_ShouldReturnUser() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password(null) // Уже null
                .email("test@bank.com")
                .build();

        when(userService.getUserByUsername("testuser")).thenReturn(user);

        // When
        ResponseEntity<User> response = userController.getUserProfile(user);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody().getPassword());
    }

    @Test
    void getUserById_ShouldAlwaysSetPasswordToNull() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("verySecretPassword")
                .email("test@bank.com")
                .build();

        when(userService.getUserById(1L)).thenReturn(user);

        // When
        ResponseEntity<User> response = userController.getUserById(1L);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getPassword());

        // Verify the original user object was modified
        assertNull(user.getPassword());
    }
}
