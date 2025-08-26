package fr.valentinjdt.lib.plugin

import java.net.URL
import java.net.URLClassLoader
import java.util.Properties

abstract class Command: IPlugin {

    /** Command name. */
    abstract override val name: String

    override val properties: Properties by lazy {
        getProperties(URLClassLoader(arrayOf(jarUrl)), "plugin.properties")
    }

    override val description: String
        get() = properties.getProperty("description", "No description")

    override val author: String
        get() = properties.getProperty("author", "Unknown")

    override val version: String
        get() = properties.getProperty("version", "Unknown")

    override val website: String?
        get() = properties.getProperty("website")

    /** List of sub-commands for tab completion. */
    abstract val subCommandsCompletions: List<SubCommandCompletion>

    override var jarUrl: URL by InitOnceProperty()
    override var pluginLoader: PluginLoader<*> by InitOnceProperty()

    /**
     * Execute the command with the given arguments.
     * @param args The arguments passed to the command.
     */
    abstract fun execute(args: List<String>)
    override fun onEnable() {}
    override fun onDisable() {}
}

/**
 * Represents a sub-command for tab completion.
 * @param command The sub-command string. If null or empty, an input is needed that isn't a command.
 * @param subCommands A list of further sub-commands for nested completion.
 */
data class SubCommandCompletion(
    /** If null or empty, a input is needed who isn't a command. */
    val command: String?,
    val subCommands: List<SubCommandCompletion>?
)