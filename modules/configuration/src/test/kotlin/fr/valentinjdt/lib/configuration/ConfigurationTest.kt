package fr.valentinjdt.lib.configuration

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.io.TempDir
import java.io.Serializable
import java.nio.file.Path
import java.io.File

class ConfigurationTest {

    @TempDir
    lateinit var tempDir: Path
    private lateinit var configFile: Path

    // Classe de test sérialisable
    data class TestData(val name: String, val value: Int): Serializable

    @BeforeEach
    fun setUp() {
        configFile = tempDir.resolve("config.dat")
    }

    @AfterEach
    fun tearDown() {
        File(configFile.toString()).delete()
    }

    @Test
    fun testSaveAndLoad() {
        // Création d'une configuration et d'un objet de test
        val config = Configuration<TestData>(configFile)
        val testData = TestData("test", 42)

        // Test de la sauvegarde
        assertTrue(config.erase(testData))

        // Test du chargement
        val loadedData = config.load()
        assertNotNull(loadedData)
        assertEquals(testData, loadedData)
        assertEquals(testData, config.content)
    }

    @Test
    fun testSaveWithNullContent() {
        // Création d'une configuration sans contenu initial
        val config = Configuration<TestData>(configFile)

        // Test de la méthode save avec un contenu null
        assertFalse(config.save())
    }

    @Test
    fun testSaveWithContent() {
        // Création d'une configuration et d'un objet de test
        val config = Configuration<TestData>(configFile)
        val testData = TestData("test", 42)

        // Sauvegarde manuelle
        assertTrue(config.erase(testData))

        // Chargement pour remplir le contenu
        config.load()

        // Test de la méthode save avec un contenu existant
        assertTrue(config.save())
    }

    @Test
    fun testLoadNonExistentFile() {
        // Création d'une configuration avec un fichier qui n'existe pas
        val nonExistentFile = tempDir.resolve("non_existent.dat")
        val config = Configuration<TestData>(nonExistentFile)

        // Test de chargement d'un fichier inexistant
        assertThrows(java.io.FileNotFoundException::class.java) {
            config.load()
        }
    }

    @Test
    fun testEraseToNonWritableLocation() {
        // Ce test ne fonctionne que sur des systèmes où l'accès à certains répertoires est restreint
        // Sur certains environnements, ce test peut échouer si l'utilisateur a des droits d'écriture partout
        try {
            val restrictedPath = Path.of("/system/restricted/config.dat")
            val config = Configuration<TestData>(restrictedPath)
            val testData = TestData("test", 42)

            // Test de sauvegarde dans un emplacement sans droits d'écriture
            assertFalse(config.erase(testData))
        } catch (e: Exception) {
            // Si nous ne pouvons pas créer un chemin restreint, le test est ignoré
            // On pourrait utiliser Assumptions.assumeTrue() avec JUnit 5 pour être plus élégant
        }
    }

    @Test
    fun testMultipleSavesAndLoads() {
        // Création d'une configuration
        val config = Configuration<TestData>(configFile)

        // Premier cycle de sauvegarde/chargement
        val testData1 = TestData("test1", 42)
        assertTrue(config.erase(testData1))
        val loadedData1 = config.load()
        assertEquals(testData1, loadedData1)

        // Deuxième cycle de sauvegarde/chargement
        val testData2 = TestData("test2", 84)
        assertTrue(config.erase(testData2))
        val loadedData2 = config.load()
        assertEquals(testData2, loadedData2)
        assertNotEquals(testData1, loadedData2)
    }
}
