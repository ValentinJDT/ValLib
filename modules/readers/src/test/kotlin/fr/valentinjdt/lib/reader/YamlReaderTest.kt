package fr.valentinjdt.lib.reader

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class YamlReaderTest {

    @Test
    fun `should read string values correctly`() {
        // Préparation
        val yamlContent = """
            name: John Doe
            description: This is a description
        """.trimIndent()
        val reader = YamlReader(yamlContent.byteInputStream())

        // Vérification
        assertEquals("John Doe", reader.getString("name"))
        assertEquals("This is a description", reader.getString("description"))
    }

    @Test
    fun `should read numeric values correctly`() {
        // Préparation
        val yamlContent = """
            age: 25
            height: 1.85
            weight: 75.5
        """.trimIndent()
        val reader = YamlReader(yamlContent.byteInputStream())

        // Vérification
        assertEquals(25, reader.getInt("age"))
        assertEquals(1.85, reader.getDouble("height"))
        assertEquals(75.5f, reader.getFloat("weight"))
    }

    @Test
    fun `should read boolean values correctly`() {
        // Préparation
        val yamlContent = """
            isActive: true
            isAdmin: false
        """.trimIndent()
        val reader = YamlReader(yamlContent.byteInputStream())

        // Vérification
        assertEquals(true, reader.getBoolean("isActive"))
        assertEquals(false, reader.getBoolean("isAdmin"))
    }

    @Test
    fun `should handle lists with array syntax`() {
        // Préparation
        val yamlContent = """
            colors: [red, green, blue]
        """.trimIndent()
        val reader = YamlReader(yamlContent.byteInputStream())

        // Vérification
        val colors = reader.getStringList("colors")
        assertNotNull(colors)
        assertEquals(3, colors?.size)
        assertEquals(listOf("red", "green", "blue"), colors)
    }

    @Test
    fun `should handle lists with dash syntax`() {
        // Préparation
        val yamlContent = """
            fruits:
            - apple
            - banana
            - orange
        """.trimIndent()
        val reader = YamlReader(yamlContent.byteInputStream())

        // Vérification
        val fruits = reader.getStringList("fruits")
        assertNotNull(fruits)
        assertEquals(3, fruits?.size)
        assertEquals(listOf("apple", "banana", "orange"), fruits)
    }

    @Test
    fun `should ignore comments and empty lines`() {
        // Préparation
        val yamlContent = """
            # This is a comment
            name: John Doe
            
            # Another comment
            age: 25
        """.trimIndent()
        val reader = YamlReader(yamlContent.byteInputStream())

        // Vérification
        assertEquals("John Doe", reader.getString("name"))
        assertEquals(25, reader.getInt("age"))
    }

    @Test
    fun `should handle nested properties`() {
        // Préparation
        val yamlContent = """
            person:
                name: John Doe
                age: 25
                address:
                    city: Paris
                    country: France
        """.trimIndent()
        val reader = YamlReader(yamlContent.byteInputStream())

        // Vérification
        assertEquals("John Doe", reader.getString("person.name"))
        assertEquals(25, reader.getInt("person.age"))
        assertEquals("Paris", reader.getString("person.address.city"))
        assertEquals("France", reader.getString("person.address.country"))
    }

    @Test
    fun `should handle mixed list formats`() {
        // Préparation
        val yamlContent = """
            simpleList: [a, b, c]
            complexList:
            - item1
            - item2
            - item3
        """.trimIndent()
        val reader = YamlReader(yamlContent.byteInputStream())

        // Vérification
        val simpleList = reader.getStringList("simpleList")
        val complexList = reader.getStringList("complexList")

        assertNotNull(simpleList)
        assertNotNull(complexList)
        assertEquals(listOf("a", "b", "c"), simpleList)
        assertEquals(listOf("item1", "item2", "item3"), complexList)
    }

    @Test
    fun `should handle default list key when no parent key is specified`() {
        // Préparation
        val yamlContent = """
            - item1
            - item2
            - item3
        """.trimIndent()
        val reader = YamlReader(yamlContent.byteInputStream())

        // Vérification
        val list = reader.getStringList("listKey")
        assertNotNull(list)
        assertEquals(listOf("item1", "item2", "item3"), list)
    }

    @Test
    fun `should return null for non-existent keys`() {
        // Préparation
        val yamlContent = """
            name: John Doe
        """.trimIndent()
        val reader = YamlReader(yamlContent.byteInputStream())

        // Vérification
        assertNull(reader.getString("nonExistentKey"))
        assertNull(reader.getInt("nonExistentKey"))
        assertNull(reader.getDouble("nonExistentKey"))
        assertNull(reader.getFloat("nonExistentKey"))
        assertNull(reader.getBoolean("nonExistentKey"))
        assertNull(reader.getStringList("nonExistentKey"))
    }

    @Test
    fun `should handle quoted strings`() {
        // Préparation
        val yamlContent = """
            quotedString: "This is a quoted string"
            listWithQuotes: ["item1", "item2", "item3"]
        """.trimIndent()
        val reader = YamlReader(yamlContent.byteInputStream())

        // Vérification
        assertEquals("This is a quoted string", reader.getString("quotedString"))
        val list = reader.getStringList("listWithQuotes")
        assertNotNull(list)
        assertEquals(listOf("item1", "item2", "item3"), list)
    }

    @Test
    fun `should correctly load yaml from file`(@TempDir tempDir: Path) {
        // Préparation
        val yamlFile = tempDir.resolve("test.yaml").toFile()
        yamlFile.writeText("""
            name: John Doe
            age: 25
            isActive: true
            skills: [Java, Kotlin, Python]
        """.trimIndent())

        val reader = YamlReader(yamlFile.inputStream())

        // Vérification
        assertEquals("John Doe", reader.getString("name"))
        assertEquals(25, reader.getInt("age"))
        assertEquals(true, reader.getBoolean("isActive"))
        assertEquals(listOf("Java", "Kotlin", "Python"), reader.getStringList("skills"))
    }
}
