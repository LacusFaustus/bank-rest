package com.bank.controller;

import com.bank.entity.User;
import com.bank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(@AuthenticationPrincipal User user) {
        User currentUser = userService.getUserByUsername(user.getUsername());
        // Не возвращаем пароль в ответе
        currentUser.setPassword(null);
        return ResponseEntity.ok(currentUser);
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId, @AuthenticationPrincipal User currentUser) {
        // Только админ может смотреть профили других пользователей
        if (!currentUser.getRole().equals(User.Role.ROLE_ADMIN)) {
            return ResponseEntity.status(403).build();
        }

        User user = userService.getUserById(userId);
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }
}
