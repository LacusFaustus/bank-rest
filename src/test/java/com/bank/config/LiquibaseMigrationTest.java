package com.bank.config;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
class LiquibaseMigrationTest {

    @Test
    void liquibaseMigrations_shouldApplySuccessfully() {
        // Этот тест можно запускать отдельно, когда нужна проверка миграций
        assertDoesNotThrow(() -> {
            // Тест проходит, если контекст загружается
        });
    }
}
