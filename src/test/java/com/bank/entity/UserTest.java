package com.bank.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void builder_ShouldCreateUserWithAllFields() {
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .email("test@bank.com")
                .role(User.Role.ROLE_ADMIN)
                .createdAt(now)
                .build();

        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("encodedPassword", user.getPassword());
        assertEquals("test@bank.com", user.getEmail());
        assertEquals(User.Role.ROLE_ADMIN, user.getRole());
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    void getAuthorities_ShouldReturnCorrectRole() {
        User user = User.builder()
                .username("testuser")
                .role(User.Role.ROLE_USER)
                .build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @ParameterizedTest
    @EnumSource(User.Role.class)
    void getAuthorities_ShouldReturnCorrectRoleForAllRoles(User.Role role) {
        User user = User.builder()
                .username("testuser")
                .role(role)
                .build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(role.name())));
    }

    @Test
    void userAccountStatus_ShouldAlwaysBeActive() {
        User user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@bank.com")
                .role(User.Role.ROLE_USER)
                .build();

        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }

    @Test
    void nullUserAccountStatus_ShouldAlsoBeActive() {
        User nullUser = new User();

        assertTrue(nullUser.isAccountNonExpired());
        assertTrue(nullUser.isAccountNonLocked());
        assertTrue(nullUser.isCredentialsNonExpired());
        assertTrue(nullUser.isEnabled());
    }

    @Test
    void userRoleEnum_ShouldHaveAllValues() {
        User.Role[] roles = User.Role.values();

        assertEquals(2, roles.length);
        assertArrayEquals(new User.Role[]{
                User.Role.ROLE_USER,
                User.Role.ROLE_ADMIN
        }, roles);
    }

    @Test
    void userRole_ValueOfShouldWork() {
        assertEquals(User.Role.ROLE_USER, User.Role.valueOf("ROLE_USER"));
        assertEquals(User.Role.ROLE_ADMIN, User.Role.valueOf("ROLE_ADMIN"));
    }

    @Test
    void userRole_ValueOfInvalid_ShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> {
            User.Role.valueOf("INVALID_ROLE");
        });
    }

    @Test
    void equalsAndHashCode_ShouldWorkWithIncludedFields() {
        User user1 = User.builder()
                .id(1L)
                .username("user1")
                .email("user1@bank.com")
                .build();

        User user2 = User.builder()
                .id(1L)
                .username("user1")
                .email("user1@bank.com")
                .password("differentPassword")
                .role(User.Role.ROLE_ADMIN)
                .build();

        User user3 = User.builder()
                .id(2L)
                .username("user2")
                .email("user2@bank.com")
                .build();

        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "bob"})
    void equals_WithDifferentUsernames_ShouldBeDifferent(String username) {
        User user1 = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@bank.com")
                .build();

        User user2 = User.builder()
                .id(1L)
                .username(username)
                .email("alice@bank.com")
                .build();

        if ("alice".equals(username)) {
            assertEquals(user1, user2);
        } else {
            assertNotEquals(user1, user2);
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "alice2@bank.com"})
    void equals_WithDifferentEmails_ShouldBeDifferent(String email) {
        User user1 = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@bank.com")
                .build();

        User user2 = User.builder()
                .id(1L)
                .username("alice")
                .email(email)
                .build();

        if ("alice@bank.com".equals(email)) {
            assertEquals(user1, user2);
        } else {
            assertNotEquals(user1, user2);
        }
    }

    @Test
    void equals_WithDifferentIds_ShouldBeDifferent() {
        User user1 = User.builder()
                .id(1L)
                .username("user")
                .email("user@bank.com")
                .build();

        User user2 = User.builder()
                .id(2L)
                .username("user")
                .email("user@bank.com")
                .build();

        assertNotEquals(user1, user2);
    }

    @Test
    void equals_ShouldHandleNullAndDifferentClass() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@bank.com")
                .build();

        assertNotEquals(null, user);
        assertNotEquals("string", user);
        assertEquals(user, user);
    }

    @Test
    void toString_ShouldExcludePassword() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("secretPassword123")
                .email("test@example.com")
                .role(User.Role.ROLE_USER)
                .build();

        String toString = user.toString();

        assertNotNull(toString);
        assertFalse(toString.contains("secretPassword123"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("username=testuser"));
        assertTrue(toString.contains("email=test@example.com"));
    }

    @Test
    void inSet_ShouldRespectEqualsAndHashCode() {
        User user1 = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@bank.com")
                .build();

        User user2 = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@bank.com")
                .build();

        User user3 = User.builder()
                .id(2L)
                .username("bob")
                .email("bob@bank.com")
                .build();

        Set<User> userSet = new HashSet<>();
        userSet.add(user1);
        userSet.add(user2);
        userSet.add(user3);

        assertEquals(2, userSet.size());
        assertTrue(userSet.contains(user1));
        assertTrue(userSet.contains(user2));
        assertTrue(userSet.contains(user3));
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyUser() {
        User user = new User();

        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertNull(user.getEmail());
        assertNull(user.getRole());
        assertNull(user.getCreatedAt());
    }

    @Test
    void allArgsConstructor_ShouldCreateCompleteUser() {
        LocalDateTime createdAt = LocalDateTime.now();

        User user = new User(
                1L,
                "testuser",
                "encodedPassword",
                "test@example.com",
                User.Role.ROLE_ADMIN,
                createdAt
        );

        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("encodedPassword", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(User.Role.ROLE_ADMIN, user.getRole());
        assertEquals(createdAt, user.getCreatedAt());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        User user = new User();
        LocalDateTime createdAt = LocalDateTime.now();

        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("newPassword");
        user.setEmail("test@example.com");
        user.setRole(User.Role.ROLE_USER);
        user.setCreatedAt(createdAt);

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("newPassword", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(User.Role.ROLE_USER, user.getRole());
        assertEquals(createdAt, user.getCreatedAt());
    }

    @Test
    void hashCode_Consistency() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        int hashCode1 = user.hashCode();
        int hashCode2 = user.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void equals_EdgeCase_NullId() {
        User user1 = User.builder()
                .id(null)
                .username("user")
                .email("user@test.com")
                .build();

        User user2 = User.builder()
                .id(null)
                .username("user")
                .email("user@test.com")
                .build();

        assertEquals(user1, user2);
    }

    @Test
    void equals_EdgeCase_NullUsername() {
        User user1 = User.builder()
                .id(1L)
                .username(null)
                .email("user@test.com")
                .build();

        User user2 = User.builder()
                .id(1L)
                .username(null)
                .email("user@test.com")
                .build();

        assertEquals(user1, user2);
    }

    @Test
    void equals_EdgeCase_NullEmail() {
        User user1 = User.builder()
                .id(1L)
                .username("user")
                .email(null)
                .build();

        User user2 = User.builder()
                .id(1L)
                .username("user")
                .email(null)
                .build();

        assertEquals(user1, user2);
    }

    @Test
    void equals_EdgeCase_AllNullFields() {
        User user1 = User.builder()
                .id(null)
                .username(null)
                .email(null)
                .build();

        User user2 = User.builder()
                .id(null)
                .username(null)
                .email(null)
                .build();

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void equals_EdgeCase_SameIdDifferentIncludedFields() {
        User user1 = User.builder()
                .id(1L)
                .username("user1")
                .email("user1@test.com")
                .build();

        User user2 = User.builder()
                .id(1L)
                .username("user2")
                .email("user2@test.com")
                .build();

        assertNotEquals(user1, user2);
    }

    @Test
    void equals_EdgeCase_DifferentIdSameOtherFields() {
        User user1 = User.builder()
                .id(1L)
                .username("user")
                .email("user@test.com")
                .build();

        User user2 = User.builder()
                .id(2L)
                .username("user")
                .email("user@test.com")
                .build();

        assertNotEquals(user1, user2);
    }

    @Test
    void equals_EdgeCase_MixedNullAndNonNull() {
        User user1 = User.builder()
                .id(1L)
                .username(null)
                .email("user@test.com")
                .build();

        User user2 = User.builder()
                .id(1L)
                .username("user")
                .email(null)
                .build();

        assertNotEquals(user1, user2);
    }

    @Test
    void getAuthorities_WhenRoleIsNull_ShouldThrow() {
        User user = new User();
        user.setRole(null);

        assertThrows(NullPointerException.class, user::getAuthorities);
    }

    @Test
    void getAuthorities_WhenRoleIsSet_ShouldReturnAuthority() {
        User user = User.builder()
                .role(User.Role.ROLE_ADMIN)
                .build();

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertEquals(1, authorities.size());
        assertEquals("ROLE_ADMIN", authorities.iterator().next().getAuthority());
    }

    @ParameterizedTest
    @CsvSource({
            "ROLE_USER, ROLE_USER",
            "ROLE_ADMIN, ROLE_ADMIN"
    })
    void roleEnum_ShouldHaveCorrectName(String roleName, String expected) {
        User.Role role = User.Role.valueOf(roleName);
        assertEquals(expected, role.name());
    }

    @Test
    void userDetailsMethods_ShouldAlwaysReturnTrue() {
        User user = new User();

        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }

    @Test
    void equals_Consistency() {
        User user1 = User.builder()
                .id(1L)
                .username("user")
                .email("user@test.com")
                .build();

        User user2 = User.builder()
                .id(1L)
                .username("user")
                .email("user@test.com")
                .build();

        assertTrue(user1.equals(user2));
        assertTrue(user2.equals(user1));
        assertTrue(user1.equals(user1));
    }

    @Test
    void equals_Transitivity() {
        User user1 = User.builder()
                .id(1L)
                .username("user")
                .email("user@test.com")
                .build();

        User user2 = User.builder()
                .id(1L)
                .username("user")
                .email("user@test.com")
                .build();

        User user3 = User.builder()
                .id(1L)
                .username("user")
                .email("user@test.com")
                .build();

        assertTrue(user1.equals(user2));
        assertTrue(user2.equals(user3));
        assertTrue(user1.equals(user3));
    }

    @Test
    void hashCode_WithNullFields_ShouldNotThrow() {
        User user = User.builder()
                .id(null)
                .username(null)
                .email(null)
                .build();

        assertDoesNotThrow(user::hashCode);
    }

    @Test
    void equals_SameInstance_ShouldReturnTrue() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        assertEquals(user, user);
    }

    @Test
    void equals_NullObject_ShouldReturnFalse() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        assertNotEquals(null, user);
    }

    @Test
    void builder_WithMinimalFields_ShouldWork() {
        User user = User.builder()
                .username("minimal")
                .password("pass")
                .email("min@test.com")
                .role(User.Role.ROLE_USER)
                .build();

        assertNotNull(user);
        assertEquals("minimal", user.getUsername());
        assertEquals("pass", user.getPassword());
        assertEquals("min@test.com", user.getEmail());
        assertEquals(User.Role.ROLE_USER, user.getRole());
    }

    @Test
    void toString_ShouldIncludeRole() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(User.Role.ROLE_ADMIN)
                .build();

        String toString = user.toString();
        assertTrue(toString.contains("role=ROLE_ADMIN"));
    }
}
