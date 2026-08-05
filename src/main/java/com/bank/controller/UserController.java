package com.bank.controller;

import com.bank.entity.User;
import com.bank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Получить профиль текущего пользователя
     * Доступно всем аутентифицированным пользователям
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getUserProfile(@AuthenticationPrincipal User user) {
        User currentUser = userService.getUserByUsername(user.getUsername());
        // Не возвращаем пароль в ответе
        currentUser.setPassword(null);
        return ResponseEntity.ok(currentUser);
    }

    /**
     * Получить профиль пользователя по ID
     * Только для администраторов
     */
    @GetMapping("/profile/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        user.setPassword(null); // Не возвращаем пароль
        return ResponseEntity.ok(user);
    }

    /**
     * Обновить профиль текущего пользователя
     * Доступно всем аутентифицированным пользователям
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> updateUserProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestBody User updatedUser) {

        // Пользователь может обновлять только свой профиль
        User user = userService.getUserByUsername(currentUser.getUsername());

        // Обновляем разрешенные поля
        if (updatedUser.getEmail() != null) {
            user.setEmail(updatedUser.getEmail());
        }

        // Сохраняем обновленного пользователя
        User savedUser = userService.updateUser(user);
        savedUser.setPassword(null);

        return ResponseEntity.ok(savedUser);
    }
}
