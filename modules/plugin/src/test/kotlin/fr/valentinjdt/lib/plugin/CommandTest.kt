package fr.valentinjdt.lib.plugin

import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CommandTest {

    @Test
    fun `test command initialization`() {
        val command = TestCommand("test-command", "Test command description", "3.6.18")
        assertEquals("test-command", command.name)
        assertEquals("Test command description", command.description)

        // URL should not be initialized yet
        assertFailsWith<IllegalStateException> {
            command.url
        }

        // Set the URL
        val testUrl = URL("file:///test/command.jar")
        command.url = testUrl
        assertEquals(testUrl, command.url)

        // Trying to set the URL again should throw an exception
        assertFailsWith<IllegalStateException> {
            command.url = URL("file:///another/path.jar")
        }
    }

    @Test
    fun `test command execution`() {
        val command = TestCommand("test-command", "Test command description", "0.2.1")

        // Test command execution with empty args
        command.execute(emptyList())
        assertEquals(1, command.executeCalls)
        assertEquals(0, command.executeArgs.size)

        // Test command execution with args
        val args = listOf("arg1", "arg2")
        command.execute(args)
        assertEquals(2, command.executeCalls)
        assertEquals(args, command.executeArgs)
    }

    @Test
    fun `test lifecycle methods`() {
        val command = TestCommand("test-command", "Test command description", "1.12.5")

        // Test that these methods can be called without exceptions
        command.onEnable()
        command.onDisable()

        // Verify that the command recorded the lifecycle calls
        assertEquals(1, command.enableCalls)
        assertEquals(1, command.disableCalls)
    }

    private class TestCommand(name: String, description: String, version: String, override val subCommandsCompletions: List<SubCommandCompletion> = listOf()) : Command(name, description, version) {
        var executeCalls = 0
        var executeArgs: List<String> = emptyList()
        var enableCalls = 0
        var disableCalls = 0

        override fun execute(args: List<String>) {
            executeCalls++
            executeArgs = args
        }

        override fun onEnable() {
            enableCalls++
        }

        override fun onDisable() {
            disableCalls++
        }
    }
}
