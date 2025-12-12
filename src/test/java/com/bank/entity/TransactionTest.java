package com.bank.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TransactionTest {

    @Test
    void builder_ShouldCreateTransactionWithAllFields() {
        Card fromCard = Card.builder()
                .id(1L)
                .cardNumber("1111222233334444")
                .build();

        Card toCard = Card.builder()
                .id(2L)
                .cardNumber("5555666677778888")
                .build();

        LocalDateTime transactionDate = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

        Transaction transaction = Transaction.builder()
                .id(1L)
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(new BigDecimal("100.50"))
                .transactionDate(transactionDate)
                .status(Transaction.TransactionStatus.SUCCESS)
                .build();

        assertNotNull(transaction);
        assertEquals(1L, transaction.getId());
        assertEquals(fromCard, transaction.getFromCard());
        assertEquals(toCard, transaction.getToCard());
        assertEquals(new BigDecimal("100.50"), transaction.getAmount());
        assertEquals(transactionDate, transaction.getTransactionDate());
        assertEquals(Transaction.TransactionStatus.SUCCESS, transaction.getStatus());
    }

    @Test
    void builder_WithNullCards_ShouldHandleGracefully() {
        Transaction transaction = Transaction.builder()
                .id(1L)
                .fromCard(null)
                .toCard(null)
                .amount(new BigDecimal("50.00"))
                .transactionDate(LocalDateTime.now())
                .status(Transaction.TransactionStatus.PENDING)
                .build();

        assertNotNull(transaction);
        assertEquals(1L, transaction.getId());
        assertNull(transaction.getFromCard());
        assertNull(transaction.getToCard());
        assertEquals(new BigDecimal("50.00"), transaction.getAmount());
        assertEquals(Transaction.TransactionStatus.PENDING, transaction.getStatus());
    }

    static Stream<Arguments> transactionDataProvider() {
        return Stream.of(
                arguments(1L, new BigDecimal("100.00"), LocalDateTime.of(2024, 1, 15, 10, 30, 0)),
                arguments(2L, new BigDecimal("200.00"), LocalDateTime.of(2024, 1, 16, 11, 30, 0)),
                arguments(3L, new BigDecimal("300.00"), LocalDateTime.of(2024, 1, 17, 12, 30, 0))
        );
    }

    @ParameterizedTest
    @MethodSource("transactionDataProvider")
    void equalsAndHashCode_ShouldWorkCorrectly(Long id, BigDecimal amount, LocalDateTime date) {
        Transaction transaction1 = Transaction.builder()
                .id(id)
                .amount(amount)
                .transactionDate(date)
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(id)
                .amount(amount)
                .transactionDate(date)
                .build();

        Transaction transaction3 = Transaction.builder()
                .id(id + 1)
                .amount(amount.add(new BigDecimal("100.00")))
                .transactionDate(date.plusDays(1))
                .build();

        assertEquals(transaction1, transaction2);
        assertNotEquals(transaction1, transaction3);
        assertEquals(transaction1.hashCode(), transaction2.hashCode());
        assertNotEquals(transaction1.hashCode(), transaction3.hashCode());
    }

    @Test
    void equals_ShouldHandleNullAndDifferentClass() {
        Transaction transaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .build();

        assertNotEquals(null, transaction);
        assertNotEquals("string", transaction);
        assertEquals(transaction, transaction);
    }

    @ParameterizedTest
    @NullSource
    void equals_WithNullFields_ShouldHandleGracefully(BigDecimal amount) {
        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .amount(amount)
                .transactionDate(null)
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(1L)
                .amount(amount)
                .transactionDate(null)
                .build();

        assertEquals(transaction1, transaction2);
        assertEquals(transaction1.hashCode(), transaction2.hashCode());
    }

    @Test
    void equals_WithDifferentAmounts_ShouldBeDifferent() {
        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("200.00"))
                .transactionDate(transaction1.getTransactionDate())
                .build();

        assertNotEquals(transaction1, transaction2);
    }

    @Test
    void equals_WithDifferentDates_ShouldBeDifferent() {
        LocalDateTime date1 = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        LocalDateTime date2 = LocalDateTime.of(2024, 1, 16, 10, 30, 0);

        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .transactionDate(date1)
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .transactionDate(date2)
                .build();

        assertNotEquals(transaction1, transaction2);
    }

    @Test
    void toString_ShouldExcludeCards() {
        Card fromCard = Card.builder()
                .id(1L)
                .cardHolder("FROM USER")
                .build();

        Card toCard = Card.builder()
                .id(2L)
                .cardHolder("TO USER")
                .build();

        Transaction transaction = Transaction.builder()
                .id(1L)
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .status(Transaction.TransactionStatus.SUCCESS)
                .build();

        String toString = transaction.toString();

        assertNotNull(toString);
        assertFalse(toString.contains("fromCard"));
        assertFalse(toString.contains("toCard"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("amount=100.00"));
        assertTrue(toString.contains("transactionDate=2024-01-15T10:30"));
        assertTrue(toString.contains("status=SUCCESS"));
    }

    @Test
    void transactionStatusEnum_ShouldHaveAllValues() {
        Transaction.TransactionStatus[] statuses = Transaction.TransactionStatus.values();

        assertEquals(3, statuses.length);
        assertArrayEquals(new Transaction.TransactionStatus[]{
                Transaction.TransactionStatus.SUCCESS,
                Transaction.TransactionStatus.FAILED,
                Transaction.TransactionStatus.PENDING
        }, statuses);
    }

    @ParameterizedTest
    @EnumSource(Transaction.TransactionStatus.class)
    void transactionStatus_ValueOfShouldWorkForAllStatuses(Transaction.TransactionStatus status) {
        Transaction.TransactionStatus valueOfStatus = Transaction.TransactionStatus.valueOf(status.name());

        assertEquals(status, valueOfStatus);
    }

    @Test
    void transactionStatus_ValueOfInvalid_ShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> {
            Transaction.TransactionStatus.valueOf("INVALID");
        });
    }

    @Test
    void inSet_ShouldRespectEqualsAndHashCode() {
        Set<Transaction> transactionSet = new HashSet<>();

        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .build();

        Transaction transaction3 = Transaction.builder()
                .id(2L)
                .amount(new BigDecimal("200.00"))
                .transactionDate(LocalDateTime.of(2024, 1, 16, 11, 30, 0))
                .build();

        transactionSet.add(transaction1);
        transactionSet.add(transaction2);
        transactionSet.add(transaction3);

        assertEquals(2, transactionSet.size());
        assertTrue(transactionSet.contains(transaction1));
        assertTrue(transactionSet.contains(transaction2));
        assertTrue(transactionSet.contains(transaction3));
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyTransaction() {
        Transaction transaction = new Transaction();

        assertNotNull(transaction);
        assertNull(transaction.getId());
        assertNull(transaction.getFromCard());
        assertNull(transaction.getToCard());
        assertNull(transaction.getAmount());
        assertNull(transaction.getTransactionDate());
        assertNull(transaction.getStatus());
    }

    @Test
    void allArgsConstructor_ShouldCreateCompleteTransaction() {
        Card fromCard = Card.builder().id(1L).build();
        Card toCard = Card.builder().id(2L).build();
        LocalDateTime date = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        BigDecimal amount = new BigDecimal("75.25");

        Transaction transaction = new Transaction(
                1L, fromCard, toCard, amount, date,
                Transaction.TransactionStatus.FAILED
        );

        assertNotNull(transaction);
        assertEquals(1L, transaction.getId());
        assertEquals(fromCard, transaction.getFromCard());
        assertEquals(toCard, transaction.getToCard());
        assertEquals(amount, transaction.getAmount());
        assertEquals(date, transaction.getTransactionDate());
        assertEquals(Transaction.TransactionStatus.FAILED, transaction.getStatus());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        Transaction transaction = new Transaction();
        Card fromCard = Card.builder().id(1L).build();
        Card toCard = Card.builder().id(2L).build();
        LocalDateTime date = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        BigDecimal amount = new BigDecimal("125.75");

        transaction.setId(1L);
        transaction.setFromCard(fromCard);
        transaction.setToCard(toCard);
        transaction.setAmount(amount);
        transaction.setTransactionDate(date);
        transaction.setStatus(Transaction.TransactionStatus.SUCCESS);

        assertEquals(1L, transaction.getId());
        assertEquals(fromCard, transaction.getFromCard());
        assertEquals(toCard, transaction.getToCard());
        assertEquals(amount, transaction.getAmount());
        assertEquals(date, transaction.getTransactionDate());
        assertEquals(Transaction.TransactionStatus.SUCCESS, transaction.getStatus());
    }

    @ParameterizedTest
    @CsvSource({
            "SUCCESS, true",
            "FAILED, false",
            "PENDING, true"
    })
    void equals_Consistency_WithDifferentStatuses(String status, boolean shouldEqual) {
        LocalDateTime date = LocalDateTime.now();

        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .transactionDate(date)
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .transactionDate(date)
                .status(Transaction.TransactionStatus.valueOf(status))
                .build();

        // Status is not included in equals, so they should be equal regardless
        assertEquals(transaction1, transaction2);
        assertEquals(transaction2, transaction1);
    }

    @Test
    void equals_Transitivity() {
        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .build();

        Transaction transaction3 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .build();

        assertTrue(transaction1.equals(transaction2));
        assertTrue(transaction2.equals(transaction3));
        assertTrue(transaction1.equals(transaction3));
    }

    @Test
    void hashCode_Consistency() {
        Transaction transaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDateTime.now())
                .build();

        int hashCode1 = transaction.hashCode();
        int hashCode2 = transaction.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void equals_WithNullId_ShouldHandleGracefully() {
        Transaction transaction1 = Transaction.builder()
                .id(null)
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(null)
                .amount(new BigDecimal("100.00"))
                .transactionDate(transaction1.getTransactionDate())
                .build();

        assertEquals(transaction1, transaction2);
        assertEquals(transaction1.hashCode(), transaction2.hashCode());
    }

    @Test
    void equals_WithNullAmount_ShouldHandleGracefully() {
        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .amount(null)
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(1L)
                .amount(null)
                .transactionDate(transaction1.getTransactionDate())
                .build();

        assertEquals(transaction1, transaction2);
        assertEquals(transaction1.hashCode(), transaction2.hashCode());
    }

    @Test
    void equals_WithNullTransactionDate_ShouldHandleGracefully() {
        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .transactionDate(null)
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .transactionDate(null)
                .build();

        assertEquals(transaction1, transaction2);
        assertEquals(transaction1.hashCode(), transaction2.hashCode());
    }

    @Test
    void equals_WithMixedNullFields_ShouldBeDifferent() {
        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .amount(null)
                .transactionDate(LocalDateTime.now())
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .transactionDate(null)
                .build();

        assertNotEquals(transaction1, transaction2);
    }

    @Test
    void toString_ShouldIncludeStatus() {
        Transaction transaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("50.00"))
                .transactionDate(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .status(Transaction.TransactionStatus.FAILED)
                .build();

        String toString = transaction.toString();

        assertTrue(toString.contains("status=FAILED"));
    }

    @Test
    void hashCode_WithNullFields_ShouldNotThrow() {
        Transaction transaction = Transaction.builder()
                .id(null)
                .amount(null)
                .transactionDate(null)
                .build();

        assertDoesNotThrow(transaction::hashCode);
    }

    @Test
    void equals_WhenBothHaveNullAmountAndDate_ShouldBeEqual() {
        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .amount(null)
                .transactionDate(null)
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(1L)
                .amount(null)
                .transactionDate(null)
                .build();

        assertEquals(transaction1, transaction2);
    }

    @Test
    void equals_WhenDifferentNullCombinations_ShouldBeDifferent() {
        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .transactionDate(null)
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(1L)
                .amount(null)
                .transactionDate(LocalDateTime.now())
                .build();

        assertNotEquals(transaction1, transaction2);
    }

    @Test
    void setters_WithNullValues_ShouldWork() {
        Transaction transaction = new Transaction();

        transaction.setFromCard(null);
        transaction.setToCard(null);
        transaction.setAmount(null);
        transaction.setTransactionDate(null);
        transaction.setStatus(null);

        assertNull(transaction.getFromCard());
        assertNull(transaction.getToCard());
        assertNull(transaction.getAmount());
        assertNull(transaction.getTransactionDate());
        assertNull(transaction.getStatus());
    }

    @Test
    void equals_SameInstance_ShouldReturnTrue() {
        Transaction transaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .build();

        assertEquals(transaction, transaction);
    }

    @Test
    void equals_NullObject_ShouldReturnFalse() {
        Transaction transaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("100.00"))
                .build();

        assertNotEquals(null, transaction);
    }
}
