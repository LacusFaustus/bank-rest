package com.bank.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    private Counter loginSuccessCounter;
    private Counter loginFailureCounter;
    private Counter transferCounter;
    private Counter cardBlockRequestCounter;
    private Timer transferTimer;

    public void recordSuccessfulLogin(String username) {
        if (loginSuccessCounter == null) {
            loginSuccessCounter = Counter.builder("bank.login.success")
                    .description("Successful login attempts")
                    .tag("application", "bank-card-management")
                    .register(meterRegistry);
        }
        loginSuccessCounter.increment();
    }

    public void recordFailedLogin(String username) {
        if (loginFailureCounter == null) {
            loginFailureCounter = Counter.builder("bank.login.failure")
                    .description("Failed login attempts")
                    .tag("application", "bank-card-management")
                    .register(meterRegistry);
        }
        loginFailureCounter.increment();
    }

    public void recordTransfer(long durationMs) {
        if (transferCounter == null) {
            transferCounter = Counter.builder("bank.transfer.completed")
                    .description("Completed transfers")
                    .tag("application", "bank-card-management")
                    .register(meterRegistry);
        }
        transferCounter.increment();

        if (transferTimer == null) {
            transferTimer = Timer.builder("bank.transfer.duration")
                    .description("Transfer operation duration")
                    .tag("application", "bank-card-management")
                    .register(meterRegistry);
        }
        transferTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordCardBlockRequest() {
        if (cardBlockRequestCounter == null) {
            cardBlockRequestCounter = Counter.builder("bank.card.block.requests")
                    .description("Card block requests")
                    .tag("application", "bank-card-management")
                    .register(meterRegistry);
        }
        cardBlockRequestCounter.increment();
    }
}
