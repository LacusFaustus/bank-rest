package com.bank.service;

import com.bank.entity.User;
import com.bank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(User.Role.ROLE_USER);
    }

    @Test
    void getUserById_UserExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_UserNotFound_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.getUserById(1L));

        assertEquals("User not found with id: 1", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_WithNullId_ShouldThrowException() {
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.getUserById(null));

        assertEquals("User not found with id: null", exception.getMessage());
        verify(userRepository, times(1)).findById(null);
    }

    @Test
    void getUserByUsername_UserExists_ShouldReturnUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        User result = userService.getUserByUsername("testuser");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void getUserByUsername_UserNotFound_ShouldThrowException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.getUserByUsername("nonexistent"));

        assertEquals("User not found: nonexistent", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    void getUserByUsername_WithNullUsername_ShouldThrowException() {
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.getUserByUsername(null));

        assertEquals("User not found: null", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(null);
    }

    @Test
    void getUserByUsername_WithEmptyUsername_ShouldThrowException() {
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.getUserByUsername(""));

        assertEquals("User not found: ", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("");
    }

    @Test
    void getAllUsers_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        List<User> users = Arrays.asList(user1, user2);
        Page<User> page = new PageImpl<>(users, pageable, 2);

        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<User> result = userService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("user1", result.getContent().get(0).getUsername());
        assertEquals("user2", result.getContent().get(1).getUsername());
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    void getAllUsers_WithEmptyDatabase_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<User> result = userService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    void userExists_ShouldReturnTrueWhenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        boolean exists = userService.userExists(1L);

        assertTrue(exists);
        verify(userRepository, times(1)).existsById(1L);
    }

    @Test
    void userExists_ShouldReturnFalseWhenUserDoesNotExist() {
        when(userRepository.existsById(999L)).thenReturn(false);

        boolean exists = userService.userExists(999L);

        assertFalse(exists);
        verify(userRepository, times(1)).existsById(999L);
    }

    @Test
    void userExists_WithNullId_ShouldReturnFalse() {
        when(userRepository.existsById(null)).thenReturn(false);

        boolean exists = userService.userExists(null);

        assertFalse(exists);
        verify(userRepository, times(1)).existsById(null);
    }

    @Test
    void updateUser_ShouldSaveAndReturnUser() {
        User userToUpdate = new User();
        userToUpdate.setId(1L);
        userToUpdate.setUsername("updateduser");
        userToUpdate.setEmail("updated@example.com");

        when(userRepository.save(userToUpdate)).thenReturn(userToUpdate);

        User result = userService.updateUser(userToUpdate);

        assertNotNull(result);
        assertEquals("updateduser", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        verify(userRepository, times(1)).save(userToUpdate);
    }

    @Test
    void updateUser_WithNullUser_ShouldThrowIllegalArgumentException() {
        // Проверяем, что метод бросает IllegalArgumentException при null
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(null));

        assertEquals("User cannot be null", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_WithNewUser_ShouldSave() {
        User newUser = new User();
        newUser.setUsername("newuser");

        when(userRepository.save(newUser)).thenReturn(newUser);

        User result = userService.updateUser(newUser);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(userRepository, times(1)).save(newUser);
    }

    @Test
    void countUsers_ShouldReturnCount() {
        when(userRepository.count()).thenReturn(42L);

        long count = userService.countUsers();

        assertEquals(42L, count);
        verify(userRepository, times(1)).count();
    }

    @Test
    void countUsers_WithEmptyDatabase_ShouldReturnZero() {
        when(userRepository.count()).thenReturn(0L);

        long count = userService.countUsers();

        assertEquals(0L, count);
        verify(userRepository, times(1)).count();
    }

    @Test
    void existsByEmail_ShouldReturnTrueWhenEmailExists() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        boolean exists = userService.existsByEmail("test@example.com");

        assertTrue(exists);
        verify(userRepository, times(1)).existsByEmail("test@example.com");
    }

    @Test
    void existsByEmail_ShouldReturnFalseWhenEmailDoesNotExist() {
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        boolean exists = userService.existsByEmail("nonexistent@example.com");

        assertFalse(exists);
        verify(userRepository, times(1)).existsByEmail("nonexistent@example.com");
    }

    @Test
    void existsByEmail_WithNullEmail_ShouldReturnFalse() {
        when(userRepository.existsByEmail(null)).thenReturn(false);

        boolean exists = userService.existsByEmail(null);

        assertFalse(exists);
        verify(userRepository, times(1)).existsByEmail(null);
    }

    @Test
    void existsByEmail_WithEmptyEmail_ShouldReturnFalse() {
        when(userRepository.existsByEmail("")).thenReturn(false);

        boolean exists = userService.existsByEmail("");

        assertFalse(exists);
        verify(userRepository, times(1)).existsByEmail("");
    }

    @Test
    void existsByUsername_ShouldReturnTrueWhenUsernameExists() {
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        boolean exists = userService.existsByUsername("existinguser");

        assertTrue(exists);
        verify(userRepository, times(1)).existsByUsername("existinguser");
    }

    @Test
    void existsByUsername_ShouldReturnFalseWhenUsernameDoesNotExist() {
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        boolean exists = userService.existsByUsername("nonexistent");

        assertFalse(exists);
        verify(userRepository, times(1)).existsByUsername("nonexistent");
    }

    @Test
    void existsByUsername_WithNullUsername_ShouldReturnFalse() {
        when(userRepository.existsByUsername(null)).thenReturn(false);

        boolean exists = userService.existsByUsername(null);

        assertFalse(exists);
        verify(userRepository, times(1)).existsByUsername(null);
    }

    @Test
    void existsByUsername_WithEmptyUsername_ShouldReturnFalse() {
        when(userRepository.existsByUsername("")).thenReturn(false);

        boolean exists = userService.existsByUsername("");

        assertFalse(exists);
        verify(userRepository, times(1)).existsByUsername("");
    }

    @Test
    void getAllUsers_WithDifferentPageable_ShouldWork() {
        Pageable pageable = PageRequest.of(2, 5); // Page 3, 5 items per page

        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        List<User> users = Collections.singletonList(testUser);
        Page<User> page = new PageImpl<>(users, pageable, 11); // Исправлено: 11 всего элементов

        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<User> result = userService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size()); // 1 элемент на странице
        assertEquals(11, result.getTotalElements()); // Всего 11 элементов
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    void updateUser_ShouldHandleRoleUpdates() {
        User user = new User();
        user.setId(1L);
        user.setUsername("adminuser");
        user.setRole(User.Role.ROLE_ADMIN);

        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateUser(user);

        assertNotNull(result);
        assertEquals(User.Role.ROLE_ADMIN, result.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUser_ShouldHandlePasswordUpdates() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user");
        user.setPassword("newEncryptedPassword");

        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateUser(user);

        assertNotNull(result);
        assertEquals("newEncryptedPassword", result.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUser_WhenSaveThrowsException_ShouldPropagate() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user");

        when(userRepository.save(user)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> userService.updateUser(user));
    }

    @Test
    void updateUser_WithMinimalUserData_ShouldWork() {
        User minimalUser = new User();
        minimalUser.setUsername("minimal");

        when(userRepository.save(minimalUser)).thenReturn(minimalUser);

        User result = userService.updateUser(minimalUser);

        assertNotNull(result);
        assertEquals("minimal", result.getUsername());
        verify(userRepository, times(1)).save(minimalUser);
    }
}
