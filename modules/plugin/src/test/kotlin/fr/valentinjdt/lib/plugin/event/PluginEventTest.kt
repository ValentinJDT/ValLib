package fr.valentinjdt.lib.plugin.event

import fr.valentinjdt.lib.event.EventRegister
import fr.valentinjdt.lib.event.Listener
import fr.valentinjdt.lib.plugin.IPlugin
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PluginEventTest {

    private lateinit var testPlugin: TestPlugin
    private lateinit var testListener: TestPluginEventListener

    @BeforeEach
    fun setup() {
        // Reset the EventRegister before each test
        val field = EventRegister::class.java.getDeclaredField("listeners")
        field.isAccessible = true
        val listeners = field.get(null) as ArrayList<*>
        listeners.clear()

        testPlugin = TestPlugin("test-plugin", "Test Plugin Description", "4.1.2")
        testPlugin.url = URL("file:///test/plugin.jar")

        testListener = TestPluginEventListener()
        EventRegister.registerListener(testListener)
    }

    @Test
    fun `test PluginLoadEvent`() {
        val event = PluginLoadEvent(testPlugin)

        // Test event properties
        assertEquals(testPlugin, event.plugin)
        assertEquals(testPlugin.url.path, event.path)
        assertFalse(event.cancel)

        // Test cancellation
        event.cancel = true
        assertTrue(event.cancel)

        // Test event dispatching
        testListener.reset()
        EventRegister.runEvent(event)
        assertTrue(testListener.loadEventReceived)
        assertEquals(testPlugin.name, testListener.lastLoadedPluginName)
    }

    @Test
    fun `test PluginUnLoadEvent`() {
        val event = PluginUnLoadEvent(testPlugin)

        // Test event properties
        assertEquals(testPlugin, event.plugin)
        assertEquals(testPlugin.url.path, event.path)
        assertFalse(event.cancel)

        // Test cancellation
        event.cancel = true
        assertTrue(event.cancel)

        // Test event dispatching
        testListener.reset()
        EventRegister.runEvent(event)
        assertTrue(testListener.unloadEventReceived)
        assertEquals(testPlugin.name, testListener.lastUnloadedPluginName)
    }

    @Test
    fun `test PluginCanNotLoadEvent`() {
        val path = "/test/plugin.jar"
        val event = PluginCanNotLoadEvent(path)

        // Test event properties
        assertEquals(path, event.path)

        // Test event dispatching
        testListener.reset()
        EventRegister.runEvent(event)
        assertTrue(testListener.cannotLoadEventReceived)
        assertEquals(path, testListener.lastCannotLoadPath)
    }

    @Test
    fun `test PluginAlreadyLoadedEvent`() {
        val event = PluginAlreadyLoadedEvent(testPlugin)

        // Test event properties
        assertEquals(testPlugin, event.plugin)
        assertEquals(testPlugin.url.path, event.path)

        // Test event dispatching
        testListener.reset()
        EventRegister.runEvent(event)
        assertTrue(testListener.alreadyLoadedEventReceived)
        assertEquals(testPlugin.name, testListener.lastAlreadyLoadedPluginName)
    }

    class TestPlugin(override val name: String, override val description: String, override val version: String) : IPlugin {
        override lateinit var url: URL

        override fun onEnable() {}
        override fun onDisable() {}
    }

    class TestPluginEventListener : Listener() {
        var loadEventReceived = false
        var unloadEventReceived = false
        var cannotLoadEventReceived = false
        var alreadyLoadedEventReceived = false

        var lastLoadedPluginName: String? = null
        var lastUnloadedPluginName: String? = null
        var lastCannotLoadPath: String? = null
        var lastAlreadyLoadedPluginName: String? = null

        fun reset() {
            loadEventReceived = false
            unloadEventReceived = false
            cannotLoadEventReceived = false
            alreadyLoadedEventReceived = false

            lastLoadedPluginName = null
            lastUnloadedPluginName = null
            lastCannotLoadPath = null
            lastAlreadyLoadedPluginName = null
        }

        @fr.valentinjdt.lib.event.EventHandler
        fun onPluginLoad(event: PluginLoadEvent) {
            loadEventReceived = true
            lastLoadedPluginName = event.plugin.name
        }

        @fr.valentinjdt.lib.event.EventHandler
        fun onPluginUnload(event: PluginUnLoadEvent) {
            unloadEventReceived = true
            lastUnloadedPluginName = event.plugin.name
        }

        @fr.valentinjdt.lib.event.EventHandler
        fun onPluginCannotLoad(event: PluginCanNotLoadEvent) {
            cannotLoadEventReceived = true
            lastCannotLoadPath = event.path
        }

        @fr.valentinjdt.lib.event.EventHandler
        fun onPluginAlreadyLoaded(event: PluginAlreadyLoadedEvent) {
            alreadyLoadedEventReceived = true
            lastAlreadyLoadedPluginName = event.plugin.name
        }
    }
}
