package com.bank;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки пакета com.bank.
 */
class PackageInfoTest {

    @Test
    void testPackageInfo() {
        // Используем Class.forName для получения информации о пакете
        try {
            Class<?> clazz = Class.forName("com.bank.BankApplication");
            Package pkg = clazz.getPackage();
            assertNotNull(pkg, "Пакет com.bank должен существовать");
            assertEquals("com.bank", pkg.getName());
        } catch (ClassNotFoundException e) {
            fail("Класс BankApplication не найден: " + e.getMessage());
        }
    }

    @Test
    void testPackageResources() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources("com/bank");

        // Могут быть ресурсы или нет, но мы просто проверяем что не падает
        assertNotNull(resources);
    }

    @Test
    void testPackageAnnotation() {
        try {
            Class<?> clazz = Class.forName("com.bank.BankApplication");
            Package pkg = clazz.getPackage();
            if (pkg != null) {
                // Просто проверяем что метод доступен
                assertNotNull(pkg.getAnnotations());
            }
        } catch (ClassNotFoundException e) {
            fail("Класс BankApplication не найден: " + e.getMessage());
        }
    }

    @Test
    void testPackageSpecification() {
        try {
            Class<?> clazz = Class.forName("com.bank.BankApplication");
            Package pkg = clazz.getPackage();
            if (pkg != null) {
                // Просто проверяем что методы доступны
                assertNull(pkg.getSpecificationTitle()); // У нас нет спецификации
                assertNull(pkg.getSpecificationVersion());
                assertNull(pkg.getSpecificationVendor());
                assertNull(pkg.getImplementationTitle());
                assertNull(pkg.getImplementationVersion());
                assertNull(pkg.getImplementationVendor());
            }
        } catch (ClassNotFoundException e) {
            fail("Класс BankApplication не найден: " + e.getMessage());
        }
    }

    @Test
    void testPackageSealed() {
        try {
            Class<?> clazz = Class.forName("com.bank.BankApplication");
            Package pkg = clazz.getPackage();
            if (pkg != null) {
                // Проверяем что пакет не запечатан
                assertFalse(pkg.isSealed());
            }
        } catch (ClassNotFoundException e) {
            fail("Класс BankApplication не найден: " + e.getMessage());
        }
    }
}
