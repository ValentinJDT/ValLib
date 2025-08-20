package fr.valentinjdt.lib.tests

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName

/**
 * Tests JUnit pour vérifier le fonctionnement des classes StaticData et TypedStaticData
 */
class StaticDataTest {

    @Test
    @DisplayName("Vérifier le fonctionnement de StaticData")
    fun testStaticData() {
        // Arrange
        val staticData = StaticData()

        // Act
        staticData["key1"] = "value1"
        staticData["key2"] = 42
        staticData["key3"] = listOf(1, 2, 3)

        // Assert
        val value1: String? = staticData["key1"]
        val value2: Int? = staticData["key2"]
        val value3: List<Int>? = staticData["key3"]

        assertEquals("value1", value1)
        assertEquals(42, value2)
        assertEquals(listOf(1, 2, 3), value3)
        assertNull(staticData["nonExistentKey"])

        // Vérifier le toString
        val stringRepresentation = staticData.toString()
        assertTrue(stringRepresentation.contains("StaticData"))
        assertTrue(stringRepresentation.contains("key1"))
        assertTrue(stringRepresentation.contains("value1"))
    }

    @Test
    @DisplayName("Vérifier le fonctionnement de TypedStaticData")
    fun testTypedStaticData() {
        // Arrange
        val typedData = TypedStaticData<String>()

        // Act
        typedData["key1"] = "value1"
        typedData["key2"] = "value2"

        // Assert
        assertEquals("value1", typedData["key1"])
        assertEquals("value2", typedData["key2"])
        assertNull(typedData["nonExistentKey"])

        // Vérifier le toString
        val stringRepresentation = typedData.toString()
        assertTrue(stringRepresentation.contains("TypedStaticData"))
        assertTrue(stringRepresentation.contains("key1"))
        assertTrue(stringRepresentation.contains("value1"))
    }

    @Test
    @DisplayName("Vérifier l'injection automatique de StaticData dans TestClass")
    fun testStaticDataInjectionInTestClass() {
        // Arrange
        class TestClassWithStaticData : TestClass() {
            var injectedData: StaticData? = null

            @fr.valentinjdt.lib.tests.Test
            fun testWithStaticData(staticData: StaticData) {
                injectedData = staticData
                staticData["testKey"] = "testValue"
            }
        }

        // Act
        val testInstance = TestClassWithStaticData()

        // Assert
        assertNotNull(testInstance.injectedData)
        assertEquals("testValue", testInstance.injectedData?.get<String>("testKey"))
    }
}
