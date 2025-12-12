package com.bank.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetricsServiceTest {

    private MeterRegistry meterRegistry;
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new MetricsService(meterRegistry);
    }

    @Test
    void recordSuccessfulLogin_ShouldCreateAndIncrementCounter() {
        metricsService.recordSuccessfulLogin("user");

        Counter counter = meterRegistry.find("bank.login.success").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count());
    }

    @Test
    void recordFailedLogin_ShouldCreateAndIncrementCounter() {
        metricsService.recordFailedLogin("user");

        Counter counter = meterRegistry.find("bank.login.failure").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count());
    }

    @Test
    void recordTransfer_ShouldCreateAndIncrementCounterAndTimer() {
        metricsService.recordTransfer(150L);

        Counter counter = meterRegistry.find("bank.transfer.completed").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count());

        Timer timer = meterRegistry.find("bank.transfer.duration").timer();
        assertNotNull(timer);
        assertEquals(1, timer.count());
    }

    @Test
    void recordCardBlockRequest_ShouldCreateAndIncrementCounter() {
        metricsService.recordCardBlockRequest();

        Counter counter = meterRegistry.find("bank.card.block.requests").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count());
    }

    @Test
    void multipleCalls_ShouldReuseExistingCounters() {
        metricsService.recordSuccessfulLogin("user1");
        metricsService.recordSuccessfulLogin("user2");

        Counter counter = meterRegistry.find("bank.login.success").counter();
        assertEquals(2.0, counter.count());
    }

    @Test
    void recordSuccessfulLogin_MultipleTimes_ShouldUseSameCounter() {
        metricsService.recordSuccessfulLogin("user1");
        metricsService.recordSuccessfulLogin("user2");

        Counter counter = meterRegistry.find("bank.login.success").counter();
        assertEquals(2.0, counter.count());
    }
}
