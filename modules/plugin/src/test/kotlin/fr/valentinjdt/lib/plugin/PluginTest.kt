package fr.valentinjdt.lib.plugin

import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PluginTest {

    @Test
    fun `test plugin initialization`() {
        val plugin = TestPlugin("test-plugin", "Test description", "15w2b")
        assertEquals("test-plugin", plugin.name)
        assertEquals("Test description", plugin.description)

        // URL should not be initialized yet
        assertFailsWith<IllegalStateException> {
            plugin.jarUrl
        }

        // Set the URL
        val testUrl = URL("file:///test/plugin.jar")
        plugin.jarUrl = testUrl
        assertEquals(testUrl, plugin.jarUrl)

        // Trying to set the URL again should throw an exception
        assertFailsWith<IllegalStateException> {
            plugin.jarUrl = URL("file:///another/path.jar")
        }
    }

    @Test
    fun `test lifecycle methods`() {
        val plugin = TestPlugin("test-plugin", "Test description", "5.2.1")

        // Test that these methods can be called without exceptions
        plugin.onEnable()
        plugin.onDisable()

        // Verify that the plugin recorded the lifecycle calls
        assertEquals(1, plugin.enableCalls)
        assertEquals(1, plugin.disableCalls)
    }

    private class TestPlugin(override val name: String, override val description: String, override val version: String) : Plugin() {

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
