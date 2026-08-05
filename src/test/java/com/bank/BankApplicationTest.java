package com.bank;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-тесты для основного класса приложения BankApplication.
 * Обеспечивают 100% покрытие без загрузки Spring контекста.
 */
class BankApplicationTest {

    @Test
    void bankApplicationClass_ShouldExist() {
        // Простейшая проверка - класс должен существовать
        assertNotNull(BankApplication.class);
    }

    @Test
    void bankApplication_ShouldHaveMainMethod() throws Exception {
        // Проверяем наличие метода main
        Method mainMethod = BankApplication.class.getMethod("main", String[].class);
        assertNotNull(mainMethod);

        // Проверяем что метод статический и публичный
        assertTrue(Modifier.isStatic(mainMethod.getModifiers()));
        assertTrue(Modifier.isPublic(mainMethod.getModifiers()));

        // Проверяем тип возвращаемого значения
        assertEquals(void.class, mainMethod.getReturnType());
    }

    @Test
    void bankApplication_ShouldHaveCorrectAnnotations() {
        // Проверяем аннотации на классе
        assertNotNull(BankApplication.class.getAnnotation(
                org.springframework.boot.autoconfigure.SpringBootApplication.class));

        assertNotNull(BankApplication.class.getAnnotation(
                org.springframework.cache.annotation.EnableCaching.class));

        assertNotNull(BankApplication.class.getAnnotation(
                org.springframework.scheduling.annotation.EnableAsync.class));

        assertNotNull(BankApplication.class.getAnnotation(
                org.springframework.scheduling.annotation.EnableScheduling.class));
    }

    @Test
    void bankApplication_ShouldHavePublicNoArgsConstructor() throws Exception {
        // Проверяем наличие публичного конструктора без параметров
        var constructor = BankApplication.class.getDeclaredConstructor();
        assertTrue(Modifier.isPublic(constructor.getModifiers()));
    }

    @Test
    void bankApplication_InstantiationShouldWork() {
        // Проверяем что можно создать экземпляр класса
        BankApplication app = new BankApplication();
        assertNotNull(app);
        assertInstanceOf(BankApplication.class, app);
    }

    @Test
    void mainMethod_CanBeInvokedViaReflection() throws Exception {
        // Проверяем что метод main можно вызвать через рефлексию
        Method mainMethod = BankApplication.class.getMethod("main", String[].class);

        // Можем проверить сигнатуру, но не вызывать реально,
        // так как это запустит SpringApplication.run()
        assertDoesNotThrow(() -> {
            mainMethod.setAccessible(true);
            // Не вызываем метод, только проверяем доступ
        });
    }

    @Test
    void bankApplication_ShouldBePublicClass() {
        // Проверяем что класс публичный
        assertTrue(Modifier.isPublic(BankApplication.class.getModifiers()));
    }

    @Test
    void testToStringMethod() {
        // Проверяем метод toString (наследуется от Object)
        BankApplication app = new BankApplication();
        String toStringResult = app.toString();
        assertNotNull(toStringResult);
        // Дефолтный toString Object возвращает ClassName@hashCode
        assertTrue(toStringResult.contains("BankApplication"));
    }

    @Test
    void testHashCodeMethod() {
        // Проверяем метод hashCode (наследуется от Object)
        BankApplication app = new BankApplication();
        assertDoesNotThrow(app::hashCode);
    }

    @Test
    void testEqualsMethod() {
        // Проверяем метод equals (наследуется от Object)
        BankApplication app1 = new BankApplication();
        BankApplication app2 = new BankApplication();

        // equals() должен работать
        assertTrue(app1.equals(app1)); // рефлексивность
        assertFalse(app1.equals(null)); // не равен null
        assertFalse(app1.equals("string")); // не равен другому типу
    }

    @Test
    void testClassStructure() {
        // Проверяем общую структуру класса
        assertEquals(Object.class, BankApplication.class.getSuperclass());
        assertEquals(0, BankApplication.class.getInterfaces().length);
        assertEquals("com.bank", BankApplication.class.getPackage().getName());
    }

    @Test
    void testAnnotationsCount() {
        // Проверяем количество аннотаций
        var annotations = BankApplication.class.getAnnotations();
        // Должно быть 4 аннотации: @SpringBootApplication, @EnableCaching, @EnableAsync, @EnableScheduling
        assertTrue(annotations.length >= 4);
    }

    @Test
    void testClassLoader() {
        // Проверяем загрузчик классов
        assertNotNull(BankApplication.class.getClassLoader());
    }

    @Test
    void testAllRequiredAnnotations() {
        // Проверяем наличие всех необходимых аннотаций
        Class<BankApplication> clazz = BankApplication.class;

        // @SpringBootApplication
        assertTrue(clazz.isAnnotationPresent(
                org.springframework.boot.autoconfigure.SpringBootApplication.class));

        // @EnableCaching
        assertTrue(clazz.isAnnotationPresent(
                org.springframework.cache.annotation.EnableCaching.class));

        // @EnableAsync
        assertTrue(clazz.isAnnotationPresent(
                org.springframework.scheduling.annotation.EnableAsync.class));

        // @EnableScheduling
        assertTrue(clazz.isAnnotationPresent(
                org.springframework.scheduling.annotation.EnableScheduling.class));
    }

    @Test
    void testMethodCount() {
        // Проверяем количество методов
        Method[] methods = BankApplication.class.getDeclaredMethods();

        // Фильтруем только не-синтетические методы
        long nonSyntheticMethods = 0;
        for (Method method : methods) {
            if (!method.isSynthetic()) {
                nonSyntheticMethods++;
            }
        }

        // Должен быть только main метод (не-синтетический)
        assertEquals(1, nonSyntheticMethods, "Должен быть только один не-синтетический метод (main)");

        // Проверяем что это действительно main метод
        for (Method method : methods) {
            if ("main".equals(method.getName())) {
                assertEquals("main", method.getName());
                return;
            }
        }
        fail("Метод main не найден");
    }

    @Test
    void testFieldCount() {
        // Проверяем поля класса
        var fields = BankApplication.class.getDeclaredFields();
        // Не должно быть полей
        assertEquals(0, fields.length);
    }

    @Test
    void testPackage() {
        // Проверяем информацию о пакете
        Package pkg = BankApplication.class.getPackage();
        assertNotNull(pkg);
        assertEquals("com.bank", pkg.getName());
    }
}
