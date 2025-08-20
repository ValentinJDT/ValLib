package fr.valentinjdt.lib.plugin

import java.net.URL

interface IPlugin {
    /** The name of the plugin. */
    val name: String
    /** A short description of the plugin. */
    val description: String
    /** The version of the plugin, used for compatibility checks. */
    val version: String
    /** The URL of the plugin, used for loading resources. [PluginLoader] define it. */
    var url: URL
    /** Call when the plugin is enabled. */
    fun onEnable()
    /** Call when the plugin is disabled. */
    fun onDisable()
}