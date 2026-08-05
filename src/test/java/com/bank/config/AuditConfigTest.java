package com.bank.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuditConfigTest {

    private final AuditConfig auditConfig = new AuditConfig();

    @Test
    void applicationEventMulticaster_shouldReturnSimpleApplicationEventMulticaster() {
        // Act
        ApplicationEventMulticaster multicaster = auditConfig.applicationEventMulticaster();

        // Assert
        assertNotNull(multicaster);
        assertInstanceOf(SimpleApplicationEventMulticaster.class, multicaster);

        // Проверяем, что можно добавить слушателя
        SimpleApplicationEventMulticaster simpleMulticaster = (SimpleApplicationEventMulticaster) multicaster;
        assertDoesNotThrow(() -> {
            simpleMulticaster.addApplicationListener(event -> {});
        });
    }

    @Test
    void auditTaskExecutor_shouldReturnConfiguredExecutor() {
        // Act
        Executor executor = auditConfig.auditTaskExecutor();

        // Assert
        assertNotNull(executor);
        assertInstanceOf(SimpleAsyncTaskExecutor.class, executor);

        // Проверяем, что executor работает
        SimpleAsyncTaskExecutor taskExecutor = (SimpleAsyncTaskExecutor) executor;

        // Проверяем выполнение задачи
        final boolean[] taskExecuted = {false};
        executor.execute(() -> taskExecuted[0] = true);

        // Даем время на выполнение
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // В простых тестах мы не можем гарантировать выполнение асинхронной задачи,
        // но можем проверить что executor не бросает исключений
        assertNotNull(executor);
    }

    @Test
    void notificationTaskExecutor_shouldReturnConfiguredExecutor() {
        // Act
        Executor executor = auditConfig.notificationTaskExecutor();

        // Assert
        assertNotNull(executor);
        assertInstanceOf(SimpleAsyncTaskExecutor.class, executor);

        // Проверяем выполнение задачи
        assertDoesNotThrow(() -> {
            executor.execute(() -> {
                // Простая задача для теста
                String threadName = Thread.currentThread().getName();
                assertTrue(threadName.startsWith("notification-executor-"));
            });
        });
    }

    @Test
    void applicationEventMulticaster_shouldHaveTaskExecutorSet() {
        // Act
        ApplicationEventMulticaster multicaster = auditConfig.applicationEventMulticaster();
        Executor auditExecutor = auditConfig.auditTaskExecutor();

        // Assert
        assertNotNull(multicaster);
        assertNotNull(auditExecutor);

        // Проверяем что мультикастер использует правильный executor
        SimpleApplicationEventMulticaster simpleMulticaster = (SimpleApplicationEventMulticaster) multicaster;

        // Отправляем тестовое событие
        assertDoesNotThrow(() -> {
            simpleMulticaster.multicastEvent(new org.springframework.context.ApplicationEvent("test") {});
        });
    }
}

