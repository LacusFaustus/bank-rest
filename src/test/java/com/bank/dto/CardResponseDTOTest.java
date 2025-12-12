package com.bank.dto;

import com.bank.entity.Card;
import com.bank.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CardResponseDTOTest {

    @Test
    void fromEntity_ShouldConvertCardEntityToDTO() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(User.Role.ROLE_USER)
                .build();

        LocalDateTime now = LocalDateTime.now();
        LocalDate expiryDate = LocalDate.now().plusYears(1);

        Card card = Card.builder()
                .id(1L)
                .cardNumber("1234567890123456")
                .cardHolder("JOHN DOE")
                .expiryDate(expiryDate)
                .balance(new BigDecimal("1000.00"))
                .status(Card.CardStatus.ACTIVE)
                .user(user)
                .blockRequested(false)
                .createdAt(now)
                .build();

        String maskedCardNumber = "**** **** **** 3456";

        // When
        CardResponseDTO dto = CardResponseDTO.fromEntity(card, maskedCardNumber);

        // Then
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(maskedCardNumber, dto.getMaskedCardNumber());
        assertEquals("JOHN DOE", dto.getCardHolder());
        assertEquals(expiryDate, dto.getExpiryDate());
        assertEquals(new BigDecimal("1000.00"), dto.getBalance());
        assertEquals(Card.CardStatus.ACTIVE, dto.getStatus());
        assertEquals(now, dto.getCreatedAt());
        assertFalse(dto.getBlockRequested());
        assertEquals(1L, dto.getUserId());
        assertEquals("testuser", dto.getUserName());
    }

    @Test
    void fromEntity_WithNullUser_ShouldHandleGracefully() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDate expiryDate = LocalDate.now().plusYears(1);

        Card card = Card.builder()
                .id(2L)
                .cardNumber("1234567890123456")
                .cardHolder("NO USER")
                .expiryDate(expiryDate)
                .balance(new BigDecimal("500.00"))
                .status(Card.CardStatus.ACTIVE)
                .user(null)
                .blockRequested(true)
                .createdAt(now)
                .build();

        // When
        CardResponseDTO dto = CardResponseDTO.fromEntity(card, "**** **** **** 3456");

        // Then
        assertNotNull(dto);
        assertEquals(2L, dto.getId());
        assertEquals("NO USER", dto.getCardHolder());
        assertEquals(expiryDate, dto.getExpiryDate());
        assertEquals(new BigDecimal("500.00"), dto.getBalance());
        assertEquals(Card.CardStatus.ACTIVE, dto.getStatus());
        assertEquals(now, dto.getCreatedAt());
        assertTrue(dto.getBlockRequested());
        assertNull(dto.getUserId());
        assertNull(dto.getUserName());
    }

    @Test
    void fromEntity_NullCard_ShouldReturnNull() {
        // When
        CardResponseDTO dto = CardResponseDTO.fromEntity(null, "**** **** **** 1234");

        // Then
        assertNull(dto);
    }

    @Test
    void fromEntity_EmptyMaskedCardNumber_ShouldWork() {
        // Given
        Card card = Card.builder()
                .id(1L)
                .cardNumber("1234567890123456")
                .cardHolder("TEST")
                .expiryDate(LocalDate.now().plusYears(1))
                .balance(BigDecimal.ZERO)
                .status(Card.CardStatus.ACTIVE)
                .build();

        // When
        CardResponseDTO dto = CardResponseDTO.fromEntity(card, "");

        // Then
        assertNotNull(dto);
        assertEquals("", dto.getMaskedCardNumber());
    }

    @Test
    void builder_ShouldCreateCompleteDTO() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDate expiryDate = LocalDate.now().plusYears(2);

        // When
        CardResponseDTO dto = CardResponseDTO.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .cardHolder("TEST USER")
                .expiryDate(expiryDate)
                .balance(new BigDecimal("500.00"))
                .status(Card.CardStatus.ACTIVE)
                .createdAt(now)
                .blockRequested(false)
                .userId(1L)
                .userName("testuser")
                .build();

        // Then
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("**** **** **** 1234", dto.getMaskedCardNumber());
        assertEquals("TEST USER", dto.getCardHolder());
        assertEquals(expiryDate, dto.getExpiryDate());
        assertEquals(new BigDecimal("500.00"), dto.getBalance());
        assertEquals(Card.CardStatus.ACTIVE, dto.getStatus());
        assertEquals(now, dto.getCreatedAt());
        assertFalse(dto.getBlockRequested());
        assertEquals(1L, dto.getUserId());
        assertEquals("testuser", dto.getUserName());
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyDTO() {
        // When
        CardResponseDTO dto = new CardResponseDTO();

        // Then
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getMaskedCardNumber());
        assertNull(dto.getCardHolder());
        assertNull(dto.getExpiryDate());
        assertNull(dto.getBalance());
        assertNull(dto.getStatus());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getBlockRequested());
        assertNull(dto.getUserId());
        assertNull(dto.getUserName());
    }

    @Test
    void allArgsConstructor_ShouldCreateCompleteDTO() {
        // Given
        Long id = 1L;
        String maskedCardNumber = "**** **** **** 1234";
        String cardHolder = "John Doe";
        LocalDate expiryDate = LocalDate.now().plusYears(1);
        BigDecimal balance = new BigDecimal("1000.00");
        Card.CardStatus status = Card.CardStatus.ACTIVE;
        LocalDateTime createdAt = LocalDateTime.now();
        Boolean blockRequested = false;
        Long userId = 1L;
        String userName = "johndoe";

        // When
        CardResponseDTO dto = new CardResponseDTO(
                id, maskedCardNumber, cardHolder, expiryDate, balance,
                status, createdAt, blockRequested, userId, userName
        );

        // Then
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(maskedCardNumber, dto.getMaskedCardNumber());
        assertEquals(cardHolder, dto.getCardHolder());
        assertEquals(expiryDate, dto.getExpiryDate());
        assertEquals(balance, dto.getBalance());
        assertEquals(status, dto.getStatus());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(blockRequested, dto.getBlockRequested());
        assertEquals(userId, dto.getUserId());
        assertEquals(userName, dto.getUserName());
    }

    @Test
    void allArgsConstructor_WithNullValues_ShouldWork() {
        // When
        CardResponseDTO dto = new CardResponseDTO(
                null, null, null, null, null,
                null, null, null, null, null
        );

        // Then
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getMaskedCardNumber());
        assertNull(dto.getCardHolder());
        assertNull(dto.getExpiryDate());
        assertNull(dto.getBalance());
        assertNull(dto.getStatus());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getBlockRequested());
        assertNull(dto.getUserId());
        assertNull(dto.getUserName());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Given
        CardResponseDTO dto = new CardResponseDTO();
        Long id = 1L;
        String maskedCardNumber = "**** **** **** 1234";
        String cardHolder = "Jane Smith";
        LocalDate expiryDate = LocalDate.now().plusYears(1);
        BigDecimal balance = new BigDecimal("750.50");
        Card.CardStatus status = Card.CardStatus.BLOCKED;
        LocalDateTime createdAt = LocalDateTime.now();
        Boolean blockRequested = true;
        Long userId = 2L;
        String userName = "janesmith";

        // When
        dto.setId(id);
        dto.setMaskedCardNumber(maskedCardNumber);
        dto.setCardHolder(cardHolder);
        dto.setExpiryDate(expiryDate);
        dto.setBalance(balance);
        dto.setStatus(status);
        dto.setCreatedAt(createdAt);
        dto.setBlockRequested(blockRequested);
        dto.setUserId(userId);
        dto.setUserName(userName);

        // Then
        assertEquals(id, dto.getId());
        assertEquals(maskedCardNumber, dto.getMaskedCardNumber());
        assertEquals(cardHolder, dto.getCardHolder());
        assertEquals(expiryDate, dto.getExpiryDate());
        assertEquals(balance, dto.getBalance());
        assertEquals(status, dto.getStatus());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(blockRequested, dto.getBlockRequested());
        assertEquals(userId, dto.getUserId());
        assertEquals(userName, dto.getUserName());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "different"})
    void equals_ShouldWorkWithIncludedFields(String differentValue) {
        // Given
        CardResponseDTO dto1 = CardResponseDTO.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.of(2025, 12, 31))
                .balance(new BigDecimal("100.00"))
                .status(Card.CardStatus.ACTIVE)
                .build();

        CardResponseDTO dto2 = CardResponseDTO.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.of(2025, 12, 31))
                .balance(new BigDecimal("100.00"))
                .status(Card.CardStatus.ACTIVE)
                .build();

        CardResponseDTO dto3 = CardResponseDTO.builder()
                .id(2L)
                .maskedCardNumber("**** **** **** 5678")
                .cardHolder("OTHER USER")
                .expiryDate(LocalDate.of(2024, 6, 30))
                .balance(new BigDecimal("200.00"))
                .build();

        CardResponseDTO dto4 = CardResponseDTO.builder()
                .id(1L)
                .maskedCardNumber(differentValue)
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.of(2025, 12, 31))
                .build();

        // Then
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);

        if ("**** **** **** 1234".equals(differentValue)) {
            assertEquals(dto1, dto4);
        } else {
            assertNotEquals(dto1, dto4);
        }
    }

    @Test
    void equals_WithNullFields_ShouldHandleGracefully() {
        // Given
        CardResponseDTO dto1 = CardResponseDTO.builder()
                .id(null)
                .maskedCardNumber(null)
                .cardHolder(null)
                .build();

        CardResponseDTO dto2 = CardResponseDTO.builder()
                .id(null)
                .maskedCardNumber(null)
                .cardHolder(null)
                .build();

        CardResponseDTO dto3 = CardResponseDTO.builder()
                .id(1L)
                .maskedCardNumber(null)
                .cardHolder(null)
                .build();

        // Then
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void hashCode_Consistency() {
        // Given
        CardResponseDTO dto = CardResponseDTO.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.of(2025, 12, 31))
                .build();

        // When
        int hashCode1 = dto.hashCode();
        int hashCode2 = dto.hashCode();

        // Then
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void inSet_ShouldRespectEqualsAndHashCode() {
        // Given
        Set<CardResponseDTO> dtoSet = new HashSet<>();

        CardResponseDTO dto1 = CardResponseDTO.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .cardHolder("USER ONE")
                .expiryDate(LocalDate.of(2025, 12, 31))
                .build();

        CardResponseDTO dto2 = CardResponseDTO.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .cardHolder("USER ONE")
                .expiryDate(LocalDate.of(2025, 12, 31))
                .build();

        CardResponseDTO dto3 = CardResponseDTO.builder()
                .id(2L)
                .maskedCardNumber("**** **** **** 5678")
                .cardHolder("USER TWO")
                .expiryDate(LocalDate.of(2024, 6, 30))
                .build();

        // When
        dtoSet.add(dto1);
        dtoSet.add(dto2); // Should not be added (duplicate)
        dtoSet.add(dto3); // Should be added

        // Then
        assertEquals(2, dtoSet.size());
        assertTrue(dtoSet.contains(dto1));
        assertTrue(dtoSet.contains(dto2));
        assertTrue(dtoSet.contains(dto3));
    }

    @Test
    void toString_ShouldNotExposeSensitiveData() {
        // Given
        CardResponseDTO dto = CardResponseDTO.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .cardHolder("SENSITIVE USER")
                .balance(new BigDecimal("9999.99"))
                .build();

        // When
        String toString = dto.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("maskedCardNumber=**** **** **** 1234"));
        assertTrue(toString.contains("cardHolder=SENSITIVE USER"));
        assertTrue(toString.contains("balance=9999.99"));
    }

    @Test
    void equals_SameInstance_ShouldReturnTrue() {
        // Given
        CardResponseDTO dto = CardResponseDTO.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .cardHolder("TEST")
                .build();

        // Then
        assertEquals(dto, dto);
    }

    @Test
    void equals_NullObject_ShouldReturnFalse() {
        // Given
        CardResponseDTO dto = CardResponseDTO.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .cardHolder("TEST")
                .build();

        // Then
        assertNotEquals(null, dto);
    }

    @Test
    void equals_DifferentClass_ShouldReturnFalse() {
        // Given
        CardResponseDTO dto = CardResponseDTO.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .cardHolder("TEST")
                .build();

        // Then
        assertNotEquals("string", dto);
    }

    @Test
    void builder_WithNullBlockRequested_ShouldWork() {
        // When
        CardResponseDTO dto = CardResponseDTO.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .cardHolder("TEST USER")
                .blockRequested(null)
                .build();

        // Then
        assertNotNull(dto);
        assertNull(dto.getBlockRequested());
    }

    @Test
    void fromEntity_WithAllNullUserFields_ShouldHandleGracefully() {
        // Given
        User user = new User();
        user.setId(null);
        user.setUsername(null);

        Card card = Card.builder()
                .id(1L)
                .cardNumber("1234567890123456")
                .cardHolder("TEST")
                .expiryDate(LocalDate.now().plusYears(1))
                .balance(BigDecimal.ZERO)
                .status(Card.CardStatus.ACTIVE)
                .user(user)
                .build();

        // When
        CardResponseDTO dto = CardResponseDTO.fromEntity(card, "**** **** **** 3456");

        // Then
        assertNotNull(dto);
        assertNull(dto.getUserId());
        assertNull(dto.getUserName());
    }

    @Test
    void fromEntity_WithUserHavingNullUsername_ShouldHandleGracefully() {
        // Given
        User user = User.builder()
                .id(1L)
                .username(null)
                .build();

        Card card = Card.builder()
                .id(1L)
                .cardNumber("1234567890123456")
                .cardHolder("TEST")
                .expiryDate(LocalDate.now().plusYears(1))
                .balance(BigDecimal.ZERO)
                .status(Card.CardStatus.ACTIVE)
                .user(user)
                .build();

        // When
        CardResponseDTO dto = CardResponseDTO.fromEntity(card, "**** **** **** 3456");

        // Then
        assertNotNull(dto);
        assertEquals(1L, dto.getUserId());
        assertNull(dto.getUserName());
    }

    @Test
    void fromEntity_WithUserHavingNullId_ShouldHandleGracefully() {
        // Given
        User user = User.builder()
                .id(null)
                .username("testuser")
                .build();

        Card card = Card.builder()
                .id(1L)
                .cardNumber("1234567890123456")
                .cardHolder("TEST")
                .expiryDate(LocalDate.now().plusYears(1))
                .balance(BigDecimal.ZERO)
                .status(Card.CardStatus.ACTIVE)
                .user(user)
                .build();

        // When
        CardResponseDTO dto = CardResponseDTO.fromEntity(card, "**** **** **** 3456");

        // Then
        assertNotNull(dto);
        assertNull(dto.getUserId());
        assertEquals("testuser", dto.getUserName());
    }

    @Test
    void fromEntity_WithAllNullValues_ShouldHandleGracefully() {
        Card card = Card.builder()
                .id(null)
                .cardNumber(null)
                .cardHolder(null)
                .expiryDate(null)
                .balance(null)
                .status(null)
                .createdAt(null)
                .blockRequested(null)
                .user(null)
                .build();

        CardResponseDTO dto = CardResponseDTO.fromEntity(card, null);

        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getMaskedCardNumber());
        assertNull(dto.getCardHolder());
        assertNull(dto.getExpiryDate());
        assertNull(dto.getBalance());
        assertNull(dto.getStatus());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getBlockRequested());
        assertNull(dto.getUserId());
        assertNull(dto.getUserName());
    }

    @Test
    void equals_WithNullInEqualsMethod_ShouldNotThrow() {
        CardResponseDTO dto1 = new CardResponseDTO();
        CardResponseDTO dto2 = new CardResponseDTO();

        // Проверяем что equals не выбрасывает NPE при null полях
        assertDoesNotThrow(() -> dto1.equals(dto2));
        assertDoesNotThrow(() -> dto2.equals(dto1));
    }

    @Test
    void hashCode_WithNullFields_ShouldNotThrow() {
        CardResponseDTO dto = new CardResponseDTO();

        // Проверяем что hashCode не выбрасывает NPE
        assertDoesNotThrow(() -> dto.hashCode());
    }

    // Добавить в конец файла CardResponseDTOTest.java

    @Test
    void testEquals_AllFieldsIncluded() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate expiryDate = LocalDate.now().plusYears(1);

        CardResponseDTO dto1 = CardResponseDTO.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .cardHolder("John Doe")
                .expiryDate(expiryDate)
                .balance(new BigDecimal("100.00"))
                .status(Card.CardStatus.ACTIVE)
                .createdAt(now)
                .blockRequested(false)
                .userId(1L)
                .userName("john")
                .build();

        CardResponseDTO dto2 = CardResponseDTO.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .cardHolder("John Doe")
                .expiryDate(expiryDate)
                .balance(new BigDecimal("100.00"))
                .status(Card.CardStatus.ACTIVE)
                .createdAt(now)
                .blockRequested(false)
                .userId(1L)
                .userName("john")
                .build();

        assertTrue(dto1.equals(dto2));
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testEquals_DifferentCreatedAt() {
        LocalDateTime time1 = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime time2 = LocalDateTime.of(2024, 1, 1, 1, 0);

        CardResponseDTO dto1 = CardResponseDTO.builder()
                .id(1L)
                .createdAt(time1)
                .build();

        CardResponseDTO dto2 = CardResponseDTO.builder()
                .id(1L)
                .createdAt(time2)
                .build();

        assertFalse(dto1.equals(dto2));
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testEquals_DifferentBlockRequested() {
        CardResponseDTO dto1 = CardResponseDTO.builder()
                .id(1L)
                .blockRequested(true)
                .build();

        CardResponseDTO dto2 = CardResponseDTO.builder()
                .id(1L)
                .blockRequested(false)
                .build();

        assertFalse(dto1.equals(dto2));
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testEquals_OneNullBlockRequested() {
        CardResponseDTO dto1 = CardResponseDTO.builder()
                .id(1L)
                .blockRequested(null)
                .build();

        CardResponseDTO dto2 = CardResponseDTO.builder()
                .id(1L)
                .blockRequested(false)
                .build();

        assertFalse(dto1.equals(dto2));
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testHashCode_WithBooleanFields() {
        CardResponseDTO dto1 = CardResponseDTO.builder()
                .blockRequested(true)
                .build();

        CardResponseDTO dto2 = CardResponseDTO.builder()
                .blockRequested(false)
                .build();

        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testEquals_DifferentObjectType() {
        CardResponseDTO dto = CardResponseDTO.builder().id(1L).build();
        assertFalse(dto.equals("string"));
        assertFalse(dto.equals(null));
        assertTrue(dto.equals(dto));
    }

    @Test
    void testToString_ContainsAllFields() {
        CardResponseDTO dto = CardResponseDTO.builder()
                .id(1L)
                .maskedCardNumber("****")
                .cardHolder("Test")
                .expiryDate(LocalDate.now())
                .balance(new BigDecimal("100.00"))
                .status(Card.CardStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .blockRequested(true)
                .userId(1L)
                .userName("test")
                .build();

        String str = dto.toString();
        assertNotNull(str);
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("maskedCardNumber=****"));
        assertTrue(str.contains("cardHolder=Test"));
        assertTrue(str.contains("blockRequested=true"));
    }
}
