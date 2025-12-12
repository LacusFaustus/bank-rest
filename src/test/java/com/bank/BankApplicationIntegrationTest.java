package com.bank;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для BankApplication.
 * Эти тесты проверяют аннотации и структуру класса без загрузки Spring контекста.
 */
@ExtendWith(MockitoExtension.class)
class BankApplicationIntegrationTest {

    @Test
    void shouldHaveSpringBootApplicationAnnotation() {
        assertTrue(BankApplication.class.isAnnotationPresent(
                org.springframework.boot.autoconfigure.SpringBootApplication.class));
    }

    @Test
    void shouldHaveEnableCachingAnnotation() {
        assertTrue(BankApplication.class.isAnnotationPresent(
                org.springframework.cache.annotation.EnableCaching.class));
    }

    @Test
    void shouldHaveEnableAsyncAnnotation() {
        assertTrue(BankApplication.class.isAnnotationPresent(
                org.springframework.scheduling.annotation.EnableAsync.class));
    }

    @Test
    void shouldHaveEnableSchedulingAnnotation() {
        assertTrue(BankApplication.class.isAnnotationPresent(
                org.springframework.scheduling.annotation.EnableScheduling.class));
    }

    @Test
    void shouldHaveAllRequiredAnnotations() {
        Class<BankApplication> clazz = BankApplication.class;

        assertTrue(clazz.isAnnotationPresent(
                        org.springframework.boot.autoconfigure.SpringBootApplication.class),
                "@SpringBootApplication аннотация должна присутствовать");

        assertTrue(clazz.isAnnotationPresent(
                        org.springframework.cache.annotation.EnableCaching.class),
                "@EnableCaching аннотация должна присутствовать");

        assertTrue(clazz.isAnnotationPresent(
                        org.springframework.scheduling.annotation.EnableAsync.class),
                "@EnableAsync аннотация должна присутствовать");

        assertTrue(clazz.isAnnotationPresent(
                        org.springframework.scheduling.annotation.EnableScheduling.class),
                "@EnableScheduling аннотация должна присутствовать");
    }

    @Test
    void shouldHaveCorrectPackage() {
        assertEquals("com.bank", BankApplication.class.getPackage().getName());
    }

    @Test
    void shouldBePublicClass() {
        assertTrue(Modifier.isPublic(BankApplication.class.getModifiers()));
    }

    @Test
    void shouldExtendObject() {
        assertEquals(Object.class, BankApplication.class.getSuperclass());
    }

    @Test
    void shouldNotImplementAnyInterfaces() {
        assertEquals(0, BankApplication.class.getInterfaces().length);
    }

    @Test
    void shouldHaveMainMethod() throws Exception {
        var mainMethod = BankApplication.class.getMethod("main", String[].class);
        assertNotNull(mainMethod);
        assertTrue(Modifier.isStatic(mainMethod.getModifiers()));
        assertTrue(Modifier.isPublic(mainMethod.getModifiers()));
    }

    @Test
    void shouldLoadWithAllSpringAnnotations() {
        // Проверяем все аннотации Spring Boot
        assertTrue(BankApplication.class.isAnnotationPresent(
                org.springframework.boot.autoconfigure.SpringBootApplication.class));

        // Проверяем аннотации кэширования
        assertTrue(BankApplication.class.isAnnotationPresent(
                org.springframework.cache.annotation.EnableCaching.class));

        // Проверяем аннотации асинхронности
        assertTrue(BankApplication.class.isAnnotationPresent(
                org.springframework.scheduling.annotation.EnableAsync.class));

        // Проверяем аннотации планировщика
        assertTrue(BankApplication.class.isAnnotationPresent(
                org.springframework.scheduling.annotation.EnableScheduling.class));
    }

    @Test
    void shouldHaveCorrectAnnotationOrder() {
        // Получаем все аннотации и проверяем их типы
        Annotation[] annotations = BankApplication.class.getAnnotations();

        boolean hasSpringBoot = false;
        boolean hasEnableCaching = false;
        boolean hasEnableAsync = false;
        boolean hasEnableScheduling = false;

        for (Annotation annotation : annotations) {
            Class<?> annotationType = annotation.annotationType();

            if (annotationType.equals(org.springframework.boot.autoconfigure.SpringBootApplication.class)) {
                hasSpringBoot = true;
            } else if (annotationType.equals(org.springframework.cache.annotation.EnableCaching.class)) {
                hasEnableCaching = true;
            } else if (annotationType.equals(org.springframework.scheduling.annotation.EnableAsync.class)) {
                hasEnableAsync = true;
            } else if (annotationType.equals(org.springframework.scheduling.annotation.EnableScheduling.class)) {
                hasEnableScheduling = true;
            }
        }

        assertTrue(hasSpringBoot, "Отсутствует @SpringBootApplication");
        assertTrue(hasEnableCaching, "Отсутствует @EnableCaching");
        assertTrue(hasEnableAsync, "Отсутствует @EnableAsync");
        assertTrue(hasEnableScheduling, "Отсутствует @EnableScheduling");
    }

    @Test
    void shouldBeProperSpringBootApplication() {
        // Проверяем что класс соответствует требованиям Spring Boot
        var springBootAnnotation = BankApplication.class
                .getAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class);
        assertNotNull(springBootAnnotation);

        // Проверяем свойства аннотации
        assertArrayEquals(new String[0], springBootAnnotation.scanBasePackages());
        assertArrayEquals(new Class<?>[0], springBootAnnotation.scanBasePackageClasses());
    }

    @Test
    void shouldHaveConsistentClassStructure() {
        // Проверяем согласованность структуры класса
        assertEquals(Object.class, BankApplication.class.getSuperclass());
        assertEquals(0, BankApplication.class.getInterfaces().length);
        assertEquals(0, BankApplication.class.getDeclaredFields().length);

        // Проверяем модификаторы класса
        int classModifiers = BankApplication.class.getModifiers();
        assertTrue(Modifier.isPublic(classModifiers));
        assertFalse(Modifier.isAbstract(classModifiers));
        assertFalse(Modifier.isFinal(classModifiers));
    }

    @Test
    void shouldHaveValidPackageStructure() {
        Package pkg = BankApplication.class.getPackage();
        assertNotNull(pkg);
        assertEquals("com.bank", pkg.getName());

        // Проверяем что это корневой пакет приложения
        assertTrue(pkg.getName().matches("^com\\.bank$"));
    }

    @Test
    void shouldSupportJavaModuleSystem() {
        // Проверяем совместимость с модульной системой Java
        Module module = BankApplication.class.getModule();
        assertNotNull(module);
    }

    @Test
    void testClassForName() throws ClassNotFoundException {
        // Проверяем что класс можно загрузить по имени
        Class<?> loadedClass = Class.forName("com.bank.BankApplication");
        assertEquals(BankApplication.class, loadedClass);
    }

    @Test
    void testClassLoaderConsistency() {
        // Проверяем что класс загружен тем же ClassLoader'ом что и тестовый класс
        ClassLoader testClassLoader = getClass().getClassLoader();
        ClassLoader appClassLoader = BankApplication.class.getClassLoader();
        assertSame(testClassLoader, appClassLoader);
    }

    @Test
    void testAnnotationRetention() {
        // Проверяем политику удержания аннотаций
        var springBootAnnotation = BankApplication.class
                .getAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class);
        var annotationAnnotation = springBootAnnotation.annotationType()
                .getAnnotation(java.lang.annotation.Retention.class);

        assertEquals(java.lang.annotation.RetentionPolicy.RUNTIME, annotationAnnotation.value());
    }

    @Test
    void testAnnotationTarget() {
        // Проверяем цель аннотаций
        var springBootAnnotation = BankApplication.class
                .getAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class);
        var targetAnnotation = springBootAnnotation.annotationType()
                .getAnnotation(java.lang.annotation.Target.class);

        assertNotNull(targetAnnotation);
        assertTrue(Arrays.stream(targetAnnotation.value())
                .anyMatch(e -> e == java.lang.annotation.ElementType.TYPE));
    }

    @Test
    void testClassVisibility() {
        // Проверяем видимость класса
        assertTrue(Modifier.isPublic(BankApplication.class.getModifiers()));

        // Проверяем что класс доступен из других пакетов
        assertDoesNotThrow(() -> {
            Class<?> clazz = Class.forName("com.bank.BankApplication");
            assertEquals(BankApplication.class, clazz);
        });
    }

    @Test
    void testMainMethodIntegration() throws Exception {
        // Проверяем что main метод соответствует требованиям Java
        var mainMethod = BankApplication.class.getMethod("main", String[].class);

        // Проверяем сигнатуру метода main как точку входа
        assertEquals("main", mainMethod.getName());
        assertEquals(1, mainMethod.getParameterCount());
        assertEquals(String[].class, mainMethod.getParameterTypes()[0]);

        // Проверяем что метод может быть точкой входа
        assertTrue(Modifier.isPublic(mainMethod.getModifiers()));
        assertTrue(Modifier.isStatic(mainMethod.getModifiers()));
    }

    @Test
    void testNoArgsConstructorIntegration() throws Exception {
        // Проверяем конструктор без параметров
        var constructor = BankApplication.class.getDeclaredConstructor();
        assertTrue(Modifier.isPublic(constructor.getModifiers()));

        // Проверяем что можно создать экземпляр
        BankApplication instance = constructor.newInstance();
        assertNotNull(instance);
        assertInstanceOf(BankApplication.class, instance);
    }

    @Test
    void testClassHierarchyIntegration() {
        // Проверяем иерархию классов
        Class<?> superClass = BankApplication.class.getSuperclass();
        assertEquals(Object.class, superClass);

        Class<?>[] interfaces = BankApplication.class.getInterfaces();
        assertEquals(0, interfaces.length);
    }
}
