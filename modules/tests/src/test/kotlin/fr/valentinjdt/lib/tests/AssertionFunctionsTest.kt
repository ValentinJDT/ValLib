package fr.valentinjdt.lib.tests

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName

/**
 * Tests JUnit pour les fonctions d'assertion
 */
class AssertionFunctionsTest {

    @Test
    @DisplayName("assertNotNull avec valeur non nulle doit réussir")
    fun testAssertNotNull_NonNullValue_ShouldPass() {
        // Arrange
        val value = "test"

        // Act & Assert - ne devrait pas lever d'exception
        assertDoesNotThrow { assertNotNull(value) }
    }

    @Test
    @DisplayName("assertNotNull avec valeur nulle doit échouer")
    fun testAssertNotNull_NullValue_ShouldThrow() {
        // Act & Assert - devrait lever une AssertionError
        val exception = assertThrows(AssertionError::class.java) {
            assertNotNull(null)
        }

        // Vérifier le message d'erreur
        assertEquals("null", exception.message)
    }

    @Test
    @DisplayName("assertNull avec valeur nulle doit réussir")
    fun testAssertNull_NullValue_ShouldPass() {
        // Act & Assert - ne devrait pas lever d'exception
        assertDoesNotThrow { assertNull(null) }
    }

    @Test
    @DisplayName("assertNull avec valeur non nulle doit échouer")
    fun testAssertNull_NonNullValue_ShouldThrow() {
        // Act & Assert - devrait lever une AssertionError
        val exception = assertThrows(AssertionError::class.java) {
            assertNull("test")
        }

        // Vérifier le message d'erreur
        assertEquals("null", exception.message)
    }

    @Test
    @DisplayName("assertTrue avec true doit réussir")
    fun testAssertTrue_TrueValue_ShouldPass() {
        // Act & Assert - ne devrait pas lever d'exception
        assertDoesNotThrow { assertTrue(true) }
    }

    @Test
    @DisplayName("assertTrue avec false doit échouer")
    fun testAssertTrue_FalseValue_ShouldThrow() {
        // Act & Assert - devrait lever une AssertionError
        val exception = assertThrows(AssertionError::class.java) {
            assertTrue(false)
        }

        // Vérifier le message d'erreur
        assertEquals("false", exception.message)
    }

    @Test
    @DisplayName("assertTrue avec null doit échouer")
    fun testAssertTrue_NullValue_ShouldThrow() {
        // Act & Assert - devrait lever une AssertionError
        val exception = assertThrows(AssertionError::class.java) {
            assertTrue(null)
        }

        // Vérifier le message d'erreur
        assertEquals("false", exception.message)
    }

    @Test
    @DisplayName("assertFalse avec false doit réussir")
    fun testAssertFalse_FalseValue_ShouldPass() {
        // Act & Assert - ne devrait pas lever d'exception
        assertDoesNotThrow { assertFalse(false) }
    }

    @Test
    @DisplayName("assertFalse avec true doit échouer")
    fun testAssertFalse_TrueValue_ShouldThrow() {
        // Act & Assert - devrait lever une AssertionError
        val exception = assertThrows(AssertionError::class.java) {
            assertFalse(true)
        }

        // Vérifier le message d'erreur
        assertEquals("true", exception.message)
    }

    @Test
    @DisplayName("assertFalse avec null doit échouer")
    fun testAssertFalse_NullValue_ShouldThrow() {
        // Act & Assert - devrait lever une AssertionError
        val exception = assertThrows(AssertionError::class.java) {
            assertFalse(null)
        }

        // Vérifier le message d'erreur
        assertEquals("true", exception.message)
    }

    @Test
    @DisplayName("assertEq avec valeurs égales doit réussir")
    fun testAssertEq_EqualValues_ShouldPass() {
        // Act & Assert - ne devrait pas lever d'exception
        assertDoesNotThrow { assertEq("test", "test") }
        assertDoesNotThrow { assertEq(42, 42) }
        assertDoesNotThrow { assertEq(null, null) }

        // Test avec listes
        val list1 = listOf(1, 2, 3)
        val list2 = listOf(1, 2, 3)
        assertDoesNotThrow { assertEq(list1, list2) }
    }

    @Test
    @DisplayName("assertEq avec valeurs différentes doit échouer")
    fun testAssertEq_DifferentValues_ShouldThrow() {
        // Act & Assert - devrait lever une AssertionError
        val exception = assertThrows(AssertionError::class.java) {
            assertEq("test1", "test2")
        }

        // Vérifier le message d'erreur
        assertEquals("Not equals", exception.message)
    }

    @Test
    @DisplayName("assertNotEq avec valeurs différentes doit réussir")
    fun testAssertNotEq_DifferentValues_ShouldPass() {
        // Act & Assert - ne devrait pas lever d'exception
        assertDoesNotThrow { assertNotEq("test1", "test2") }
        assertDoesNotThrow { assertNotEq(42, 43) }
        assertDoesNotThrow { assertNotEq(null, "test") }
        assertDoesNotThrow { assertNotEq("test", null) }
    }

    @Test
    @DisplayName("assertNotEq avec valeurs égales doit échouer")
    fun testAssertNotEq_EqualValues_ShouldThrow() {
        // Act & Assert - devrait lever une AssertionError
        val exception = assertThrows(AssertionError::class.java) {
            assertNotEq("test", "test")
        }

        // Vérifier le message d'erreur
        assertEquals("Equals", exception.message)
    }
}
