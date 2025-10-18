package com.bank.aspect;

import com.bank.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final MonitoringService monitoringService;

    @Around("execution(* com.bank.service.CardService.transferBetweenCards(..))")
    public Object logTransferOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String username = getCurrentUsername();
        Object[] args = joinPoint.getArgs();
        Long fromCardId = (Long) args[1];
        Long toCardId = (Long) args[2];
        BigDecimal amount = (BigDecimal) args[3];

        log.info("🔄 Transfer initiated by {}: {} from card {} to card {}",
                username, amount, fromCardId, toCardId);

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.info("✅ Transfer completed by {}: {} from card {} to card {} ({} ms)",
                    username, amount, fromCardId, toCardId, duration);

            monitoringService.recordTransfer(username, "target_user", amount);
            return result;
        } catch (Exception e) {
            log.error("❌ Transfer failed by {}: {} from card {} to card {} - Error: {}",
                    username, amount, fromCardId, toCardId, e.getMessage());
            throw e;
        }
    }

    @Around("execution(* com.bank.controller.AuthController.authenticateUser(..))")
    public Object logAuthentication(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.debug("🔐 Authentication completed in {} ms", duration);
            return result;
        } catch (Exception e) {
            monitoringService.recordFailedLogin("unknown");
            throw e;
        }
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "SYSTEM";
        }
    }
}
