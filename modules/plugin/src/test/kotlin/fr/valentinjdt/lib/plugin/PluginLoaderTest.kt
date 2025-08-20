package fr.valentinjdt.lib.plugin

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PluginLoaderTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var pluginLoader: PluginLoader<TestPlugin>
    private lateinit var pluginDirectory: String

    @BeforeEach
    fun setup() {
        pluginDirectory = tempDir.absolutePath
        pluginLoader = PluginLoader(pluginDirectory)

        // Clear existing plugin loaders to avoid test interference
        PluginLoader.loaders.clear()
    }

    @AfterEach
    fun tearDown() {
        // Ensure we clean up all plugins
        pluginLoader.unloadPluginsFiles()
        PluginLoader.loaders.clear()
    }

    @Test
    fun `test createInstance and getInstance`() {
        // Test creating a new instance
        val created = PluginLoader.createInstance<TestPlugin>(pluginDirectory)
        assertTrue(created)

        // Test getting the same instance again
        val notCreated = PluginLoader.createInstance<TestPlugin>(pluginDirectory)
        assertFalse(notCreated)

        // Test that getInstance returns the same instance
        val instance = PluginLoader.getInstance<TestPlugin>(pluginDirectory)
        assertNotNull(instance)
    }

    @Test
    fun `test getPlugins returns empty list when no plugins are loaded`() {
        val plugins = pluginLoader.getPlugins()
        assertTrue(plugins.isEmpty())
    }

    @Test
    fun `test unloadPluginsFiles`() {
        // Setup a test plugin manually
        val testPlugin = TestPlugin("test-plugin", "Test description")
        testPlugin.url = URL("file://" + tempDir.absolutePath + "/test-plugin.jar")

        // Créer un URLClassLoader vide pour éviter l'erreur de cast
        val emptyUrls = arrayOf<URL>()
        val mockClassLoader = URLClassLoader(emptyUrls)

        // Add plugin to the loader (using reflection as there's no public API for this)
        val field = PluginLoader::class.java.getDeclaredField("plugins")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val plugins = field.get(pluginLoader) as MutableMap<TestPlugin, ClassLoader>
        plugins[testPlugin] = mockClassLoader

        // Now test unloading
        pluginLoader.unloadPluginsFiles()

        // Verify the plugin is unloaded
        assertTrue(plugins.isEmpty())
        assertEquals(1, testPlugin.disableCalls)
    }

    // This class is used for testing plugin functionality
    class TestPlugin(name: String, description: String = "", version: String = "1.2.3") : Plugin(name, description, version) {
        var enableCalls = 0
        var disableCalls = 0

        override fun onEnable() {
            enableCalls++
        }

        override fun onDisable() {
            disableCalls++
        }
    }
}
