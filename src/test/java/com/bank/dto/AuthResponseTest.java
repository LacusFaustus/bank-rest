package com.bank.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class AuthResponseTest {

    @Test
    void noArgsConstructor_ShouldCreateEmptyAuthResponse() {
        AuthResponse authResponse = new AuthResponse();

        assertNotNull(authResponse);
        assertNull(authResponse.getToken());
        assertNull(authResponse.getType());
    }

    @Test
    void constructorWithTokenAndType_ShouldCreateCompleteAuthResponse() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        String type = "Bearer";

        AuthResponse authResponse = new AuthResponse(token, type);

        assertEquals(token, authResponse.getToken());
        assertEquals(type, authResponse.getType());
    }

    @Test
    void constructorWithTokenOnly_ShouldCreateAuthResponseWithDefaultType() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

        AuthResponse authResponse = new AuthResponse(token);

        assertEquals(token, authResponse.getToken());
        assertEquals("Bearer", authResponse.getType());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        AuthResponse authResponse = new AuthResponse();
        String token = "new-token";
        String type = "Custom";

        authResponse.setToken(token);
        authResponse.setType(type);

        assertEquals(token, authResponse.getToken());
        assertEquals(type, authResponse.getType());
    }

    @Test
    void testEquals_WhenObjectsHaveSameValues_ShouldBeEqual() {
        AuthResponse response1 = new AuthResponse("token123", "Bearer");
        AuthResponse response2 = new AuthResponse("token123", "Bearer");

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testEquals_WhenTokensAreDifferent_ShouldNotBeEqual() {
        AuthResponse response1 = new AuthResponse("token1", "Bearer");
        AuthResponse response2 = new AuthResponse("token2", "Bearer");

        assertNotEquals(response1, response2);
        assertNotEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testEquals_WhenTypesAreDifferent_ShouldNotBeEqual() {
        AuthResponse response1 = new AuthResponse("token", "Bearer");
        AuthResponse response2 = new AuthResponse("token", "Basic");

        assertNotEquals(response1, response2);
        assertNotEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testEquals_WhenComparedWithNull_ShouldReturnFalse() {
        AuthResponse response = new AuthResponse("token", "Bearer");

        assertNotEquals(null, response);
        assertFalse(response.equals(null));
    }

    @Test
    void testEquals_WhenComparedWithSameInstance_ShouldReturnTrue() {
        AuthResponse response = new AuthResponse("token", "Bearer");

        assertEquals(response, response);
        assertTrue(response.equals(response));
    }

    @Test
    void testEquals_WhenComparedWithDifferentClass_ShouldReturnFalse() {
        AuthResponse response = new AuthResponse("token", "Bearer");
        String stringObject = "string object";

        assertNotEquals(response, stringObject);
        assertFalse(response.equals(stringObject));
    }

    @Test
    void testHashCode_Consistency_ShouldReturnSameValueMultipleTimes() {
        AuthResponse authResponse = new AuthResponse("token", "Bearer");

        int hashCode1 = authResponse.hashCode();
        int hashCode2 = authResponse.hashCode();
        int hashCode3 = authResponse.hashCode();

        assertEquals(hashCode1, hashCode2);
        assertEquals(hashCode2, hashCode3);
        assertEquals(hashCode1, hashCode3);
    }

    @Test
    void testHashCode_WhenObjectsHaveDifferentValues_ShouldReturnDifferentHashCodes() {
        AuthResponse response1 = new AuthResponse("token1", "Bearer");
        AuthResponse response2 = new AuthResponse("token2", "Bearer");

        assertNotEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testHashCode_WhenFieldsAreNull_ShouldNotThrowException() {
        AuthResponse response = new AuthResponse(null, null);

        assertDoesNotThrow(() -> response.hashCode());
    }

    @Test
    void testToString_ShouldReturnNonNullString() {
        AuthResponse authResponse = new AuthResponse("test-token", "Bearer");

        String toString = authResponse.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("AuthResponse"));
        assertTrue(toString.contains("token") || toString.contains("type"));
    }

    @Test
    void testToString_ShouldContainTokenAndTypeValues() {
        AuthResponse authResponse = new AuthResponse("my-token", "Bearer");

        String toString = authResponse.toString();

        assertTrue(toString.contains("my-token"));
        assertTrue(toString.contains("Bearer"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "JWT-Token", "VeryLongTokenString1234567890"})
    void shouldHandleDifferentTokens(String token) {
        AuthResponse authResponse = new AuthResponse(token, "Bearer");

        assertEquals(token, authResponse.getToken());
        assertEquals("Bearer", authResponse.getType());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "Basic", "Custom", "JWT"})
    void shouldHandleDifferentTypes(String type) {
        AuthResponse authResponse = new AuthResponse("token", type);

        assertEquals("token", authResponse.getToken());
        assertEquals(type, authResponse.getType());
    }

    @Test
    void testConstructorWithTokenOnly_SetsDefaultType() {
        AuthResponse withTokenOnly = new AuthResponse("token123");

        assertEquals("token123", withTokenOnly.getToken());
        assertEquals("Bearer", withTokenOnly.getType());
    }

    @Test
    void testConstructorWithTokenAndType_SetsBothValues() {
        AuthResponse withTokenAndType = new AuthResponse("token456", "Custom");

        assertEquals("token456", withTokenAndType.getToken());
        assertEquals("Custom", withTokenAndType.getType());
    }

    @Test
    void testSerializationDeserializationCompatibility() {
        AuthResponse original = new AuthResponse("test-token", "Bearer");

        String token = original.getToken();
        String type = original.getType();

        AuthResponse deserialized = new AuthResponse(token, type);

        assertEquals(original.getToken(), deserialized.getToken());
        assertEquals(original.getType(), deserialized.getType());
        assertEquals(original, deserialized);
    }

    @Test
    void testConstructorWithNullValues() {
        AuthResponse response = new AuthResponse(null, null);

        assertNull(response.getToken());
        assertNull(response.getType());
    }

    @Test
    void testSetterUpdatesTokenValue() {
        AuthResponse response = new AuthResponse("old-token", "Bearer");

        response.setToken("new-token");

        assertEquals("new-token", response.getToken());
        assertEquals("Bearer", response.getType());
    }

    @Test
    void testSetterUpdatesTypeValue() {
        AuthResponse response = new AuthResponse("token", "Bearer");

        response.setType("Basic");

        assertEquals("token", response.getToken());
        assertEquals("Basic", response.getType());
    }

    @Test
    void testTokenOnlyConstructor_WithNullToken() {
        AuthResponse response = new AuthResponse(null);

        assertNull(response.getToken());
        assertEquals("Bearer", response.getType());
    }

    @Test
    void testEquals_WithNullFields() {
        AuthResponse response1 = new AuthResponse(null, null);
        AuthResponse response2 = new AuthResponse(null, null);
        AuthResponse response3 = new AuthResponse("token", null);
        AuthResponse response4 = new AuthResponse(null, "Bearer");

        // Оба null - должны быть равны
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());

        // Разные комбинации null - не должны быть равны
        assertNotEquals(response1, response3);
        assertNotEquals(response1, response4);
        assertNotEquals(response3, response4);
    }

    @Test
    void testEquals_WithOneNullField() {
        AuthResponse response1 = new AuthResponse("token", null);
        AuthResponse response2 = new AuthResponse("token", null);
        AuthResponse response3 = new AuthResponse(null, "Bearer");
        AuthResponse response4 = new AuthResponse(null, "Bearer");

        // Одинаковые значения с одним null полем - должны быть равны
        assertEquals(response1, response2);
        assertEquals(response3, response4);

        assertNotEquals(response1, response3);
    }

    @Test
    void testHashCode_WithNullToken() {
        AuthResponse response = new AuthResponse(null, "Bearer");

        assertDoesNotThrow(() -> response.hashCode());
        int hashCode = response.hashCode();
        assertNotNull(hashCode);
    }

    @Test
    void testHashCode_WithNullType() {
        AuthResponse response = new AuthResponse("token", null);

        assertDoesNotThrow(() -> response.hashCode());
        int hashCode = response.hashCode();
        assertNotNull(hashCode);
    }

    @Test
    void testEquals_Reflexivity() {
        AuthResponse response = new AuthResponse("token", "Bearer");

        assertTrue(response.equals(response));
    }

    @Test
    void testEquals_Symmetry() {
        AuthResponse response1 = new AuthResponse("token", "Bearer");
        AuthResponse response2 = new AuthResponse("token", "Bearer");

        assertTrue(response1.equals(response2));
        assertTrue(response2.equals(response1));
    }

    @Test
    void testEquals_Transitivity() {
        AuthResponse response1 = new AuthResponse("token", "Bearer");
        AuthResponse response2 = new AuthResponse("token", "Bearer");
        AuthResponse response3 = new AuthResponse("token", "Bearer");

        assertTrue(response1.equals(response2));
        assertTrue(response2.equals(response3));
        assertTrue(response1.equals(response3));
    }

    @Test
    void testEquals_Consistency() {
        AuthResponse response1 = new AuthResponse("token", "Bearer");
        AuthResponse response2 = new AuthResponse("token", "Bearer");

        // Множественные вызовы должны возвращать одинаковый результат
        assertTrue(response1.equals(response2));
        assertTrue(response1.equals(response2));
        assertTrue(response1.equals(response2));
    }

    @Test
    void testToString_WithNullFields() {
        AuthResponse response = new AuthResponse(null, null);

        String toString = response.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("AuthResponse"));
        assertTrue(toString.contains("null"));
    }

    @Test
    void testToString_WithEmptyStrings() {
        AuthResponse response = new AuthResponse("", "");

        String toString = response.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("AuthResponse"));
        assertTrue(toString.contains("''"));
    }

    @Test
    void testEquals_WithEmptyStrings() {
        AuthResponse response1 = new AuthResponse("", "");
        AuthResponse response2 = new AuthResponse("", "");

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testEquals_WithWhitespaceStrings() {
        AuthResponse response1 = new AuthResponse("  ", "  ");
        AuthResponse response2 = new AuthResponse("  ", "  ");

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }
}
