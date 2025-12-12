package com.bank.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {

    @Test
    void builder_ShouldCreateAuditLogWithAllFields() {
        LocalDateTime timestamp = LocalDateTime.now();

        AuditLog auditLog = AuditLog.builder()
                .id(1L)
                .actionType("LOGIN_SUCCESS")
                .description("User authenticated successfully")
                .username("testuser")
                .ipAddress("192.168.1.100")
                .userAgent("Mozilla/5.0")
                .success(true)
                .errorMessage(null)
                .timestamp(timestamp)
                .resourceId("user-123")
                .requestDetails("{\"username\":\"testuser\"}")
                .build();

        assertNotNull(auditLog);
        assertEquals(1L, auditLog.getId());
        assertEquals("LOGIN_SUCCESS", auditLog.getActionType());
        assertEquals("User authenticated successfully", auditLog.getDescription());
        assertEquals("testuser", auditLog.getUsername());
        assertEquals("192.168.1.100", auditLog.getIpAddress());
        assertEquals("Mozilla/5.0", auditLog.getUserAgent());
        assertTrue(auditLog.isSuccess());
        assertNull(auditLog.getErrorMessage());
        assertEquals(timestamp, auditLog.getTimestamp());
        assertEquals("user-123", auditLog.getResourceId());
        assertEquals("{\"username\":\"testuser\"}", auditLog.getRequestDetails());
    }

    @Test
    void builder_WithErrorMessage_ShouldSetErrorMessage() {
        AuditLog auditLog = AuditLog.builder()
                .id(2L)
                .actionType("LOGIN_FAILED")
                .description("Failed login attempt")
                .username("testuser")
                .success(false)
                .errorMessage("Invalid credentials")
                .build();

        assertEquals(2L, auditLog.getId());
        assertEquals("LOGIN_FAILED", auditLog.getActionType());
        assertEquals("Failed login attempt", auditLog.getDescription());
        assertEquals("testuser", auditLog.getUsername());
        assertFalse(auditLog.isSuccess());
        assertEquals("Invalid credentials", auditLog.getErrorMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void equals_ShouldHandleBlankActionType(String actionType) {
        AuditLog log1 = AuditLog.builder()
                .id(1L)
                .actionType(actionType)
                .username("user")
                .success(true)
                .build();

        AuditLog log2 = AuditLog.builder()
                .id(1L)
                .actionType(actionType)
                .username("user")
                .success(true)
                .build();

        assertEquals(log1, log2);
        assertEquals(log1.hashCode(), log2.hashCode());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void equals_ShouldHandleBlankUsername(String username) {
        AuditLog log1 = AuditLog.builder()
                .id(1L)
                .actionType("ACTION")
                .username(username)
                .success(true)
                .build();

        AuditLog log2 = AuditLog.builder()
                .id(1L)
                .actionType("ACTION")
                .username(username)
                .success(true)
                .build();

        assertEquals(log1, log2);
        assertEquals(log1.hashCode(), log2.hashCode());
    }

    @Test
    void equalsAndHashCode_ShouldWorkCorrectly() {
        AuditLog auditLog1 = AuditLog.builder()
                .id(1L)
                .actionType("LOGIN")
                .username("user1")
                .success(true)
                .build();

        AuditLog auditLog2 = AuditLog.builder()
                .id(1L)
                .actionType("LOGIN")
                .username("user1")
                .success(true)
                .build();

        AuditLog auditLog3 = AuditLog.builder()
                .id(2L)
                .actionType("LOGOUT")
                .username("user2")
                .success(false)
                .build();

        AuditLog auditLog4 = AuditLog.builder()
                .id(1L)
                .actionType("TRANSACTION")
                .username("user1")
                .success(true)
                .build();

        assertEquals(auditLog1, auditLog2);
        assertNotEquals(auditLog1, auditLog3);
        assertNotEquals(auditLog1, auditLog4);
        assertEquals(auditLog1.hashCode(), auditLog2.hashCode());
        assertNotEquals(auditLog1.hashCode(), auditLog3.hashCode());
        assertNotEquals(auditLog1.hashCode(), auditLog4.hashCode());
    }

    @Test
    void equals_ShouldHandleNullAndDifferentClass() {
        AuditLog auditLog = AuditLog.builder()
                .id(1L)
                .actionType("TEST")
                .username("testuser")
                .success(true)
                .build();

        assertNotEquals(null, auditLog);
        assertNotEquals("string", auditLog);
        assertEquals(auditLog, auditLog);
    }

    @Test
    void equals_WithNullFields_ShouldHandleGracefully() {
        AuditLog auditLog1 = AuditLog.builder()
                .id(1L)
                .actionType(null)
                .username(null)
                .success(true)
                .build();

        AuditLog auditLog2 = AuditLog.builder()
                .id(1L)
                .actionType(null)
                .username(null)
                .success(true)
                .build();

        assertEquals(auditLog1, auditLog2);
        assertEquals(auditLog1.hashCode(), auditLog2.hashCode());
    }

    @Test
    void equals_WithMixedNullAndNonNull_ShouldBeDifferent() {
        AuditLog auditLog1 = AuditLog.builder()
                .id(1L)
                .actionType("ACTION")
                .username(null)
                .success(true)
                .build();

        AuditLog auditLog2 = AuditLog.builder()
                .id(1L)
                .actionType(null)
                .username("user")
                .success(true)
                .build();

        assertNotEquals(auditLog1, auditLog2);
    }

    @Test
    void inSet_ShouldRespectEqualsAndHashCode() {
        Set<AuditLog> auditLogSet = new HashSet<>();

        AuditLog auditLog1 = AuditLog.builder()
                .id(1L)
                .actionType("LOGIN")
                .username("user1")
                .success(true)
                .build();

        AuditLog auditLog2 = AuditLog.builder()
                .id(1L)
                .actionType("LOGIN")
                .username("user1")
                .success(true)
                .build();

        AuditLog auditLog3 = AuditLog.builder()
                .id(2L)
                .actionType("LOGOUT")
                .username("user2")
                .success(false)
                .build();

        auditLogSet.add(auditLog1);
        auditLogSet.add(auditLog2);
        auditLogSet.add(auditLog3);

        assertEquals(2, auditLogSet.size());
        assertTrue(auditLogSet.contains(auditLog1));
        assertTrue(auditLogSet.contains(auditLog2));
        assertTrue(auditLogSet.contains(auditLog3));
    }

    @Test
    void toString_ShouldIncludeAllFields() {
        AuditLog auditLog = AuditLog.builder()
                .id(1L)
                .actionType("TEST_ACTION")
                .description("Test audit log")
                .username("testuser")
                .ipAddress("127.0.0.1")
                .success(true)
                .timestamp(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .build();

        String toString = auditLog.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("actionType=TEST_ACTION"));
        assertTrue(toString.contains("description=Test audit log"));
        assertTrue(toString.contains("username=testuser"));
        assertTrue(toString.contains("ipAddress=127.0.0.1"));
        assertTrue(toString.contains("success=true"));
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyAuditLog() {
        AuditLog auditLog = new AuditLog();

        assertNotNull(auditLog);
        assertNull(auditLog.getId());
        assertNull(auditLog.getActionType());
        assertNull(auditLog.getDescription());
        assertNull(auditLog.getUsername());
        assertNull(auditLog.getIpAddress());
        assertNull(auditLog.getUserAgent());
        assertFalse(auditLog.isSuccess());
        assertNull(auditLog.getErrorMessage());
        assertNull(auditLog.getTimestamp());
        assertNull(auditLog.getResourceId());
        assertNull(auditLog.getRequestDetails());
    }

    @Test
    void allArgsConstructor_ShouldCreateCompleteAuditLog() {
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

        AuditLog auditLog = new AuditLog(
                1L,
                "TRANSACTION",
                "Money transfer completed",
                "testuser",
                "192.168.1.1",
                "Mozilla/5.0",
                true,
                null,
                timestamp,
                "transaction-123",
                "{\"amount\":100.00}"
        );

        assertNotNull(auditLog);
        assertEquals(1L, auditLog.getId());
        assertEquals("TRANSACTION", auditLog.getActionType());
        assertEquals("Money transfer completed", auditLog.getDescription());
        assertEquals("testuser", auditLog.getUsername());
        assertEquals("192.168.1.1", auditLog.getIpAddress());
        assertEquals("Mozilla/5.0", auditLog.getUserAgent());
        assertTrue(auditLog.isSuccess());
        assertNull(auditLog.getErrorMessage());
        assertEquals(timestamp, auditLog.getTimestamp());
        assertEquals("transaction-123", auditLog.getResourceId());
        assertEquals("{\"amount\":100.00}", auditLog.getRequestDetails());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        AuditLog auditLog = new AuditLog();
        LocalDateTime timestamp = LocalDateTime.now();

        auditLog.setId(1L);
        auditLog.setActionType("LOGIN");
        auditLog.setDescription("User logged in");
        auditLog.setUsername("testuser");
        auditLog.setIpAddress("192.168.1.100");
        auditLog.setUserAgent("Test Browser/1.0");
        auditLog.setSuccess(true);
        auditLog.setErrorMessage(null);
        auditLog.setTimestamp(timestamp);
        auditLog.setResourceId("user-456");
        auditLog.setRequestDetails("{\"action\":\"login\"}");

        assertEquals(1L, auditLog.getId());
        assertEquals("LOGIN", auditLog.getActionType());
        assertEquals("User logged in", auditLog.getDescription());
        assertEquals("testuser", auditLog.getUsername());
        assertEquals("192.168.1.100", auditLog.getIpAddress());
        assertEquals("Test Browser/1.0", auditLog.getUserAgent());
        assertTrue(auditLog.isSuccess());
        assertNull(auditLog.getErrorMessage());
        assertEquals(timestamp, auditLog.getTimestamp());
        assertEquals("user-456", auditLog.getResourceId());
        assertEquals("{\"action\":\"login\"}", auditLog.getRequestDetails());
    }

    @Test
    void settersAndGetters_WithErrorMessage_ShouldWork() {
        AuditLog auditLog = new AuditLog();

        auditLog.setSuccess(false);
        auditLog.setErrorMessage("Database connection failed");

        assertFalse(auditLog.isSuccess());
        assertEquals("Database connection failed", auditLog.getErrorMessage());
    }

    @Test
    void equals_Consistency() {
        AuditLog auditLog1 = AuditLog.builder()
                .id(1L)
                .actionType("ACTION")
                .username("user")
                .success(true)
                .build();

        AuditLog auditLog2 = AuditLog.builder()
                .id(1L)
                .actionType("ACTION")
                .username("user")
                .success(true)
                .build();

        assertTrue(auditLog1.equals(auditLog2));
        assertTrue(auditLog2.equals(auditLog1));
        assertTrue(auditLog1.equals(auditLog1));
    }

    @Test
    void equals_Transitivity() {
        AuditLog auditLog1 = AuditLog.builder()
                .id(1L)
                .actionType("ACTION")
                .username("user")
                .success(true)
                .build();

        AuditLog auditLog2 = AuditLog.builder()
                .id(1L)
                .actionType("ACTION")
                .username("user")
                .success(true)
                .build();

        AuditLog auditLog3 = AuditLog.builder()
                .id(1L)
                .actionType("ACTION")
                .username("user")
                .success(true)
                .build();

        assertTrue(auditLog1.equals(auditLog2));
        assertTrue(auditLog2.equals(auditLog3));
        assertTrue(auditLog1.equals(auditLog3));
    }

    @Test
    void hashCode_Consistency() {
        AuditLog auditLog = AuditLog.builder()
                .id(1L)
                .actionType("TEST")
                .username("user")
                .success(true)
                .build();

        int hashCode1 = auditLog.hashCode();
        int hashCode2 = auditLog.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void equals_WithDifferentSuccessStatus_ShouldBeDifferent() {
        AuditLog auditLog1 = AuditLog.builder()
                .id(1L)
                .actionType("ACTION")
                .username("user")
                .success(true)
                .build();

        AuditLog auditLog2 = AuditLog.builder()
                .id(1L)
                .actionType("ACTION")
                .username("user")
                .success(false)
                .build();

        assertNotEquals(auditLog1, auditLog2);
    }

    @Test
    void equals_SameInstance_ShouldReturnTrue() {
        AuditLog auditLog = AuditLog.builder()
                .id(1L)
                .actionType("TEST")
                .username("user")
                .success(true)
                .build();

        assertEquals(auditLog, auditLog);
    }

    @Test
    void equals_NullObject_ShouldReturnFalse() {
        AuditLog auditLog = AuditLog.builder()
                .id(1L)
                .actionType("TEST")
                .username("user")
                .success(true)
                .build();

        assertNotEquals(null, auditLog);
    }
}
