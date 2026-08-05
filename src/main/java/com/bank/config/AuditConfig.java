package com.bank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AuditConfig {

    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(auditTaskExecutor());
        return eventMulticaster;
    }

    @Bean(name = "auditTaskExecutor")
    public Executor auditTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setThreadNamePrefix("audit-executor-");
        executor.setConcurrencyLimit(Runtime.getRuntime().availableProcessors() * 2);
        executor.setThreadPriority(Thread.NORM_PRIORITY - 1);
        return executor;
    }

    @Bean(name = "notificationTaskExecutor")
    public Executor notificationTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setThreadNamePrefix("notification-executor-");
        executor.setConcurrencyLimit(5);
        executor.setThreadPriority(Thread.NORM_PRIORITY - 2);
        return executor;
    }
}
