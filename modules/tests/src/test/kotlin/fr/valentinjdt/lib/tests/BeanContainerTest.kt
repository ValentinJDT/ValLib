package fr.valentinjdt.lib.tests

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName

/**
 * Tests JUnit pour vérifier le fonctionnement de l'injection de dépendances
 */
class BeanContainerTest {

    // Classes simples pour tester l'injection de dépendances
    class StringService {
        fun getValue(): String = "Hello World"
    }

    class NumberService {
        fun getValue(): Int = 42
    }

    /**
     * Classe de test avec injection de dépendances par qualificateur
     */
    class QualifierInjectionTestClass : TestClass() {
        var injectedStringService: StringService? = null

        override fun beans(): Map<String, Any> {
            return mapOf(
                "stringService" to StringService()
            )
        }

        @fr.valentinjdt.lib.tests.Test
        fun testWithQualifier(@Qualifier("stringService") service: StringService) {
            injectedStringService = service
            assertNotNull(service)
            assertEquals("Hello World", service.getValue())
        }
    }

    /**
     * Classe de test avec injection de dépendances par type
     */
    class TypeInjectionTestClass : TestClass() {
        var injectedNumberService: NumberService? = null

        override fun beans(): Map<String, Any> {
            return mapOf(
                NumberService::class.java.name to NumberService()
            )
        }

        @fr.valentinjdt.lib.tests.Test
        fun testWithTypeInjection(service: NumberService) {
            injectedNumberService = service
            assertNotNull(service)
            assertEquals(42, service.getValue())
        }
    }

    /**
     * Classe de test avec multiples injections dans une même méthode
     */
    class MultipleInjectionTestClass : TestClass() {
        var stringInjected = false
        var numberInjected = false

        override fun beans(): Map<String, Any> {
            return mapOf(
                "stringService" to StringService(),
                NumberService::class.java.name to NumberService()
            )
        }

        @fr.valentinjdt.lib.tests.Test
        fun testMultipleInjections(
            @Qualifier("stringService") stringService: StringService,
            numberService: NumberService
        ) {
            stringInjected = true
            numberInjected = true

            assertNotNull(stringService)
            assertNotNull(numberService)
            assertEquals("Hello World", stringService.getValue())
            assertEquals(42, numberService.getValue())
        }
    }

    @Test
    @DisplayName("Vérifier l'injection de dépendances par qualificateur")
    fun testQualifierInjection() {
        // Arrange & Act
        val testClass = QualifierInjectionTestClass()

        // Assert
        assertNotNull(testClass.injectedStringService, "Le service aurait dû être injecté")
        assertEquals("Hello World", testClass.injectedStringService?.getValue())
    }

    @Test
    @DisplayName("Vérifier l'injection de dépendances par type")
    fun testTypeInjection() {
        // Arrange & Act
        val testClass = TypeInjectionTestClass()

        // Assert
        assertNotNull(testClass.injectedNumberService, "Le service aurait dû être injecté")
        assertEquals(42, testClass.injectedNumberService?.getValue())
    }

    @Test
    @DisplayName("Vérifier l'injection multiple de dépendances")
    fun testMultipleInjections() {
        // Arrange & Act
        val testClass = MultipleInjectionTestClass()

        // Assert
        assertTrue(testClass.stringInjected, "Le service String aurait dû être injecté")
        assertTrue(testClass.numberInjected, "Le service Number aurait dû être injecté")
    }

    @Test
    @DisplayName("Vérifier la méthode beans par défaut")
    fun testDefaultBeans() {
        // Arrange
        class DefaultBeansTestClass : TestClass() {
            // Utilise l'implémentation par défaut de beans()
        }

        // Act & Assert - Ne devrait pas lever d'exception
        assertDoesNotThrow { DefaultBeansTestClass() }
    }
}
