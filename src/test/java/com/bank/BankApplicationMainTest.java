package com.bank;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки main метода.
 */
class BankApplicationMainTest {

    @Test
    void mainMethod_SignatureShouldBeCorrect() throws Exception {
        // Проверяем сигнатуру метода main
        Method mainMethod = BankApplication.class.getMethod("main", String[].class);

        assertEquals("main", mainMethod.getName());
        assertEquals(String[].class, mainMethod.getParameterTypes()[0]);
        assertEquals(void.class, mainMethod.getReturnType());
        assertTrue(Modifier.isStatic(mainMethod.getModifiers()));
        assertTrue(Modifier.isPublic(mainMethod.getModifiers()));
    }

    @Test
    void bankApplication_ShouldNotHaveOtherMethods() {
        // Проверяем что кроме main нет других не-синтетических методов
        Method[] methods = BankApplication.class.getDeclaredMethods();

        for (Method method : methods) {
            if (!method.isSynthetic() && !"main".equals(method.getName())) {
                fail("BankApplication не должен иметь других не-синтетических методов кроме main. Найден: " + method.getName());
            }
        }
    }

    @Test
    void bankApplication_ShouldNotHaveAnyFields() {
        // Проверяем что нет полей
        var fields = BankApplication.class.getDeclaredFields();
        assertEquals(0, fields.length, "BankApplication не должен иметь полей");
    }

    @Test
    void testAllAnnotationsAreSpringAnnotations() {
        // Проверяем что все аннотации Spring
        var annotations = BankApplication.class.getAnnotations();
        for (var annotation : annotations) {
            String packageName = annotation.annotationType().getPackageName();
            assertTrue(packageName.startsWith("org.springframework"),
                    "Все аннотации должны быть Spring аннотациями: " + annotation.annotationType().getName());
        }
    }

    @Test
    void mainMethod_ShouldHaveVarargsParameter() throws Exception {
        Method mainMethod = BankApplication.class.getMethod("main", String[].class);
        Parameter parameter = mainMethod.getParameters()[0];

        assertTrue(parameter.getType().isArray());
        assertEquals(String.class, parameter.getType().getComponentType());
    }

    @Test
    void mainMethod_ShouldBeOnlyPublicMethod() throws Exception {
        Method mainMethod = BankApplication.class.getMethod("main", String[].class);
        assertTrue(Modifier.isPublic(mainMethod.getModifiers()));
    }

    @Test
    void mainMethod_ShouldNotBeFinal() throws Exception {
        Method mainMethod = BankApplication.class.getMethod("main", String[].class);
        assertFalse(Modifier.isFinal(mainMethod.getModifiers()));
    }

    @Test
    void mainMethod_ShouldNotThrowCheckedExceptions() throws Exception {
        Method mainMethod = BankApplication.class.getMethod("main", String[].class);
        Class<?>[] exceptionTypes = mainMethod.getExceptionTypes();

        // main метод может выбрасывать только RuntimeException
        for (Class<?> exceptionType : exceptionTypes) {
            assertTrue(RuntimeException.class.isAssignableFrom(exceptionType) ||
                    Error.class.isAssignableFrom(exceptionType));
        }
    }

    @Test
    void mainMethod_AnnotationAccess() throws Exception {
        Method mainMethod = BankApplication.class.getMethod("main", String[].class);

        // Проверяем что можем получить аннотации метода
        Annotation[] methodAnnotations = mainMethod.getAnnotations();
        assertNotNull(methodAnnotations);

        // main метод обычно не имеет аннотаций
        assertEquals(0, methodAnnotations.length);
    }

    @Test
    void mainMethod_ParameterAnnotations() throws Exception {
        Method mainMethod = BankApplication.class.getMethod("main", String[].class);
        Parameter parameter = mainMethod.getParameters()[0];

        // Проверяем аннотации параметра
        Annotation[] paramAnnotations = parameter.getAnnotations();
        assertNotNull(paramAnnotations);
        assertEquals(0, paramAnnotations.length);
    }

    @Test
    void mainMethod_GenericSignature() throws Exception {
        Method mainMethod = BankApplication.class.getMethod("main", String[].class);

        // Проверяем что метод не является дженерик-методом
        assertEquals(0, mainMethod.getTypeParameters().length);
    }

    @Test
    void mainMethod_DeclaringClass() throws Exception {
        Method mainMethod = BankApplication.class.getMethod("main", String[].class);

        assertEquals(BankApplication.class, mainMethod.getDeclaringClass());
    }

    @Test
    void mainMethod_ModifiersConsistency() throws Exception {
        Method mainMethod = BankApplication.class.getMethod("main", String[].class);

        String modifierString = Modifier.toString(mainMethod.getModifiers());
        assertTrue(modifierString.contains("public"));
        assertTrue(modifierString.contains("static"));
        assertFalse(modifierString.contains("final"));
        assertFalse(modifierString.contains("synchronized"));
        assertFalse(modifierString.contains("abstract"));
        assertFalse(modifierString.contains("native"));
    }
}
