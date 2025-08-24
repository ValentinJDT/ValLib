package fr.valentinjdt.lib.plugin

import java.net.URL

abstract class Command(override val name: String, override val description: String = "", override val version: String): IPlugin {

    /** List of sub-commands for tab completion, used for Empty Terminal. */
    abstract val subCommandsCompletions: List<SubCommandCompletion>

    override var url: URL by InitOnceProperty()
    /**
     * Execute the command with the given arguments.
     * @param args The arguments passed to the command.
     */
    abstract fun execute(args: List<String>)
    override fun onEnable() {}
    override fun onDisable() {}
}

data class SubCommandCompletion(
    /** If null or empty, a input is needed who isn't a command. */
    val command: String?,
    val subCommands: List<SubCommandCompletion>?
)