package fr.valentinjdt.lib.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import java.text.Normalizer
import kotlin.reflect.KClass

class UtilsTest {

    @Test
    fun `test function extension`() {
        // Arrange
        val testString = "Hello World"

        // Act
        val result = testString.function {
            this.length
        }

        // Assert
        assertEquals(11, result)
    }

    @Test
    fun `test KClass logger extension`() {
        // Act
        val logger = UtilsTest::class.logger()

        // Assert
        assertNotNull(logger)
        assertEquals("fr.valentinjdt.lib.utils.UtilsTest", logger.name)
    }

    @Test
    fun `test normalize string without accents`() {
        // Arrange
        val testString = "Hello World"

        // Act
        val result = testString.normalize()

        // Assert
        assertEquals("Hello World", result)
    }

    @Test
    fun `test normalize string with accents`() {
        // Arrange
        val testStringWithAccents = "Héllô Wôrld àéèêë üû ïî çñ"

        // Act
        val result = testStringWithAccents.normalize()

        // Assert
        assertEquals("Hello World aeeee uu ii cn", result)
    }

    @Test
    fun `test normalize string with special characters`() {
        // Arrange
        val testStringWithSpecial = "Café-crème & Über naïf"

        // Act
        val result = testStringWithSpecial.normalize()

        // Assert
        assertEquals("Cafe-creme & Uber naif", result)
    }
}
