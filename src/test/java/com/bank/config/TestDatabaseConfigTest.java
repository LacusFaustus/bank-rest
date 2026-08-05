package com.bank.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class TestDatabaseConfigTest {

    @Test
    void dataSource_shouldReturnH2DataSource() throws SQLException {
        // Arrange
        TestDatabaseConfig config = new TestDatabaseConfig();

        // Act
        DataSource dataSource = config.dataSource();

        // Assert
        assertNotNull(dataSource);
        assertInstanceOf(DriverManagerDataSource.class, dataSource);

        DriverManagerDataSource h2DataSource = (DriverManagerDataSource) dataSource;
        assertEquals("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL",
                h2DataSource.getUrl());
        assertEquals("sa", h2DataSource.getUsername());

        // Проверяем, что можно получить соединение
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
        }
    }

    @Test
    void dataSource_shouldBeValidDataSource() {
        // Arrange
        TestDatabaseConfig config = new TestDatabaseConfig();

        // Act
        DataSource dataSource = config.dataSource();

        // Assert
        assertNotNull(dataSource);
        assertDoesNotThrow(() -> {
            try (Connection conn = dataSource.getConnection()) {
                // Соединение успешно получено
            }
        });
    }
}
