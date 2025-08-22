package fr.valentinjdt.lib.plugin

import java.net.URL

abstract class Command(override val name: String, override val description: String = "", override val version: String): IPlugin {

    /** List of sub-commands for tab completion : works only with Empty Terminal.
     *
     *
     */
    abstract val subCommandsCompletions: List<SubCommandCompletion>

    override var url: URL by InitOnceProperty()
    /**
     * Execute the command with the given arguments.
     * @param args The arguments passed to the command.
     * @throws IllegalArgumentException if the command is not valid.
     */
    abstract fun execute(args: List<String>)
    override fun onEnable() {}
    override fun onDisable() {}
}

data class SubCommandCompletion(val command: String, val subCommands: List<SubCommandCompletion>?)