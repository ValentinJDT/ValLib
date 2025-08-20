package fr.valentinjdt.lib.tests

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.reflect.KClass

/**
 * Tests JUnit pour vérifier le fonctionnement de la classe Tester
 */
class TesterTest {

    // Stockage des flux de sortie standard et d'erreur originaux
    private val stdOut = System.out
    private val stdErr = System.err
    private val outContent = ByteArrayOutputStream()
    private val errContent = ByteArrayOutputStream()

    /**
     * Classes de test simples pour tester le Tester
     */
    class TestClass1 : TestClass() {
        companion object {
            var executed = false
        }

        @fr.valentinjdt.lib.tests.Test
        fun simpleTest() {
            executed = true
            assertTrue(true)
        }
    }

    class TestClass2 : TestClass() {
        companion object {
            var executed = false
        }

        @fr.valentinjdt.lib.tests.Test
        fun anotherTest() {
            executed = true
            assertTrue(true)
        }
    }

    class TestClassWithFailure : TestClass() {
        companion object {
            var executed = false
        }

        @fr.valentinjdt.lib.tests.Test
        fun failureTest() {
            executed = true
            assertTrue(false) // Échec inattendu
        }
    }

    @Test
    @DisplayName("Vérifier que le Tester exécute toutes les classes de test")
    fun testTesterExecutesAllClasses() {
        try {
            // Réinitialiser les drapeaux d'exécution
            TestClass1.executed = false
            TestClass2.executed = false
            TestClassWithFailure.executed = false

            // Rediriger les flux de sortie
            System.setOut(PrintStream(outContent))
            System.setErr(PrintStream(errContent))

            // Act - Exécuter le Tester avec plusieurs classes
            val testClasses: Set<KClass<*>> = setOf(
                TestClass1::class,
                TestClass2::class,
                TestClassWithFailure::class
            )

            Tester(testClasses)

            // Assert - Vérifier que toutes les classes ont été exécutées
            assertTrue(TestClass1.executed, "TestClass1 aurait dû être exécutée")
            assertTrue(TestClass2.executed, "TestClass2 aurait dû être exécutée")
            assertTrue(TestClassWithFailure.executed, "TestClassWithFailure aurait dû être exécutée")

            // Vérifier les sorties
            val output = outContent.toString()
            val error = errContent.toString()

            assertTrue(output.contains(TestClass1::class.java.name) || error.contains(TestClass1::class.java.name))
            assertTrue(output.contains(TestClass2::class.java.name) || error.contains(TestClass2::class.java.name))
            assertTrue(error.contains(TestClassWithFailure::class.java.name))
            assertTrue(error.contains("failureTest"))
        } finally {
            // Restaurer les flux de sortie
            System.setOut(stdOut)
            System.setErr(stdErr)
        }
    }

    @Test
    @DisplayName("Vérifier que le Tester gère correctement un ensemble vide")
    fun testTesterWithEmptySet() {
        // Act & Assert - Ne devrait pas lever d'exception
        assertDoesNotThrow {
            Tester(emptySet())
        }
    }
}
