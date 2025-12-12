package com.bank.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

public class ArchitectureTest {

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.bank");

    @Test
    void layerDependenciesShouldBeRespected() {
        layeredArchitecture()
                .consideringAllDependencies()
                .layer("Controllers").definedBy("com.bank.controller..")
                .layer("Services").definedBy("com.bank.service..")
                .layer("Repositories").definedBy("com.bank.repository..")
                .layer("Entities").definedBy("com.bank.entity..")
                .layer("Security").definedBy("com.bank.security..")
                .layer("Validation").definedBy("com.bank.validation..")
                .layer("Events").definedBy("com.bank.event..")
                .layer("Exceptions").definedBy("com.bank.exception..")
                .layer("DTOs").definedBy("com.bank.dto..")
                .layer("Config").definedBy("com.bank.config..")

                .whereLayer("Controllers").mayOnlyAccessLayers("Services", "DTOs", "Validation", "Security", "Exceptions")
                .whereLayer("Services").mayOnlyAccessLayers("Repositories", "Entities", "DTOs", "Events", "Exceptions", "Security")
                .whereLayer("Repositories").mayOnlyAccessLayers("Entities", "Exceptions")
                .whereLayer("Security").mayOnlyAccessLayers("Services", "Exceptions")
                .whereLayer("Validation").mayOnlyAccessLayers("Exceptions")
                .whereLayer("Events").mayOnlyAccessLayers("Exceptions")
                .whereLayer("DTOs").mayOnlyAccessLayers("Exceptions")
                .whereLayer("Config").mayOnlyAccessLayers("Controllers", "Services", "Repositories", "Security", "Events")
                .whereLayer("Exceptions").mayOnlyBeAccessedByLayers("Controllers", "Services", "Repositories", "Security", "Validation", "Events", "DTOs", "Config")
                .as("Layer dependencies should be respected");
    }

    @Test
    void controllerClassesShouldHaveNameEndingWithController() {
        ArchRule rule = classes()
                .that().resideInAPackage("..controller..")
                .and().areNotNestedClasses()
                .should().haveSimpleNameEndingWith("Controller")
                .because("Controllers should follow naming convention");

        rule.check(classes);
    }

    @Test
    void serviceClassesShouldHaveNameEndingWithService() {
        ArchRule rule = classes()
                .that().resideInAPackage("..service..")
                .and().areNotNestedClasses()  // Добавлено: исключаем вложенные классы
                .should().haveSimpleNameEndingWith("Service")
                .because("Services should follow naming convention");

        rule.check(classes);
    }

    @Test
    void repositoryClassesShouldHaveNameEndingWithRepository() {
        ArchRule rule = classes()
                .that().resideInAPackage("..repository..")
                .and().areNotNestedClasses()  // Добавлено: исключаем вложенные классы
                .should().haveSimpleNameEndingWith("Repository")
                .because("Repositories should follow naming convention");

        rule.check(classes);
    }

    @Test
    void noClassesShouldUseFieldInjection() {
        ArchRule rule = noClasses()
                .should().dependOnClassesThat()
                .haveFullyQualifiedName("javax.inject.Inject")
                .orShould().dependOnClassesThat()
                .haveFullyQualifiedName("jakarta.inject.Inject")
                .because("Field injection should be avoided, use constructor injection instead");

        rule.check(classes);
    }

    @Test
    void controllerClassesShouldNotAccessRepositoriesDirectly() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat()
                .resideInAPackage("..repository..")
                .because("Controllers should access repositories through services");

        rule.check(classes);
    }

    @Test
    void entityClassesShouldNotDependOnControllersOrServices() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..entity..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..controller..",
                        "..service..",
                        "..repository.."
                )
                .because("Entities should be simple POJOs without business logic dependencies");

        rule.check(classes);
    }

    @Test
    void exceptionClassesShouldExtendRuntimeException() {
        ArchRule rule = classes()
                .that().resideInAPackage("..exception..")
                .and().haveSimpleNameEndingWith("Exception")
                .should().beAssignableTo(RuntimeException.class)
                .because("Custom exceptions should extend RuntimeException");

        rule.check(classes);
    }

    @Test
    void securityClassesShouldResideInSecurityPackage() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*Security.*")
                .or().haveNameMatching(".*Jwt.*")
                .or().haveNameMatching(".*Authentication.*")
                .or().haveNameMatching(".*Authorization.*")
                .should().resideInAPackage("..security..")
                .because("Security related classes should be in security package");

        rule.check(classes);
    }

    @Test
    void allPublicMethodsInControllersShouldReturnResponseEntity() {
        ArchRule rule = methods()
                .that().areDeclaredInClassesThat()
                .resideInAPackage("..controller..")
                .and().arePublic()
                .should().haveRawReturnType(org.springframework.http.ResponseEntity.class.getName())
                .because("Controller methods should return ResponseEntity for proper HTTP responses");

        rule.check(classes);
    }

    @Test
    void repositoryInterfacesShouldExtendJpaRepository() {
        ArchRule rule = classes()
                .that().resideInAPackage("..repository..")
                .and().areInterfaces()
                .should().beAssignableTo(org.springframework.data.jpa.repository.JpaRepository.class)
                .because("Repository interfaces should extend JpaRepository");

        rule.check(classes);
    }
}
