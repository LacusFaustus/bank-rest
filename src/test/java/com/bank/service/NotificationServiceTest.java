package com.bank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
    }

    @Test
    void sendBlockRequestNotification_ShouldNotThrow() {
        assertDoesNotThrow(() -> {
            notificationService.sendBlockRequestNotification(1L, "testuser");
        });

        // Дополнительные тесты
        assertDoesNotThrow(() -> {
            notificationService.sendBlockRequestNotification(null, "testuser");
        });

        assertDoesNotThrow(() -> {
            notificationService.sendBlockRequestNotification(1L, null);
        });

        assertDoesNotThrow(() -> {
            notificationService.sendBlockRequestNotification(null, null);
        });
    }

    @Test
    void sendTransferNotification_ShouldNotThrow() {
        assertDoesNotThrow(() -> {
            notificationService.sendTransferNotification(1L, 2L, "100.00");
        });

        // Дополнительные тесты
        assertDoesNotThrow(() -> {
            notificationService.sendTransferNotification(null, 2L, "100.00");
        });

        assertDoesNotThrow(() -> {
            notificationService.sendTransferNotification(1L, null, "100.00");
        });

        assertDoesNotThrow(() -> {
            notificationService.sendTransferNotification(1L, 2L, null);
        });

        assertDoesNotThrow(() -> {
            notificationService.sendTransferNotification(null, null, null);
        });

        // Тест с разными форматами суммы
        assertDoesNotThrow(() -> {
            notificationService.sendTransferNotification(1L, 2L, "");
        });

        assertDoesNotThrow(() -> {
            notificationService.sendTransferNotification(1L, 2L, "0");
        });

        assertDoesNotThrow(() -> {
            notificationService.sendTransferNotification(1L, 2L, "-100.00");
        });
    }

    @Test
    void sendSecurityAlert_ShouldNotThrow() {
        assertDoesNotThrow(() -> {
            notificationService.sendSecurityAlert("testuser", "192.168.1.1", "Failed login");
        });

        // Дополнительные тесты
        assertDoesNotThrow(() -> {
            notificationService.sendSecurityAlert(null, "192.168.1.1", "Failed login");
        });

        assertDoesNotThrow(() -> {
            notificationService.sendSecurityAlert("testuser", null, "Failed login");
        });

        assertDoesNotThrow(() -> {
            notificationService.sendSecurityAlert("testuser", "192.168.1.1", null);
        });

        assertDoesNotThrow(() -> {
            notificationService.sendSecurityAlert(null, null, null);
        });

        // Тест с пустыми строками
        assertDoesNotThrow(() -> {
            notificationService.sendSecurityAlert("", "", "");
        });

        // Тест с длинными строками
        assertDoesNotThrow(() -> {
            notificationService.sendSecurityAlert(
                    "verylongusername".repeat(10),
                    "192.168.1.1",
                    "Event description ".repeat(50)
            );
        });
    }

    @Test
    void sendBlockRequestNotification_WithLargeNumbers() {
        assertDoesNotThrow(() -> {
            notificationService.sendBlockRequestNotification(Long.MAX_VALUE, "user");
        });

        assertDoesNotThrow(() -> {
            notificationService.sendBlockRequestNotification(0L, "user");
        });

        assertDoesNotThrow(() -> {
            notificationService.sendBlockRequestNotification(-1L, "user");
        });
    }

    @Test
    void sendTransferNotification_WithSpecialCharacters() {
        assertDoesNotThrow(() -> {
            notificationService.sendTransferNotification(1L, 2L, "100,00 €");
        });

        assertDoesNotThrow(() -> {
            notificationService.sendTransferNotification(1L, 2L, "₽100.00");
        });

        assertDoesNotThrow(() -> {
            notificationService.sendTransferNotification(1L, 2L, "💵100.00");
        });
    }

    @Test
    void sendSecurityAlert_WithVariousEventTypes() {
        String[] events = {
                "Multiple failed login attempts",
                "Suspicious login from new location",
                "Password changed",
                "Account locked",
                "Unusual transaction pattern detected",
                "",
                " ",
                "\n",
                "\t"
        };

        for (String event : events) {
            assertDoesNotThrow(() -> {
                notificationService.sendSecurityAlert("user", "127.0.0.1", event);
            });
        }
    }

    @Test
    void asyncMethods_ShouldCompleteSuccessfully() throws InterruptedException {
        // Тестируем, что асинхронные методы не бросают исключений
        // даже при быстрых последовательных вызовах

        for (int i = 0; i < 10; i++) {
            final int index = i;
            assertDoesNotThrow(() -> {
                notificationService.sendBlockRequestNotification((long) index, "user" + index);
                notificationService.sendTransferNotification((long) index, (long) (index + 1), index + ".00");
                notificationService.sendSecurityAlert("user" + index, "192.168.1." + index, "Event " + index);
            });
        }

        // Даем время для асинхронного выполнения
        Thread.sleep(100);
    }

    @Test
    void asyncMethods_ShouldCompleteWithoutException() throws Exception {
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() ->
                notificationService.sendBlockRequestNotification(1L, "user"));
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() ->
                notificationService.sendTransferNotification(1L, 2L, "100.00"));

        CompletableFuture.allOf(future1, future2).get(5, TimeUnit.SECONDS);
    }
}
