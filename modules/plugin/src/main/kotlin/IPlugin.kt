package fr.valentinjdt.lib.plugin

import fr.valentinjdt.lib.plugin.config.Configuration
import java.io.File
import java.net.URL
import java.util.Properties

/**
 * Represents a plugin with its metadata and lifecycle methods.
 */
interface IPlugin {
    /** The properties of the plugin, loaded from the `plugin.properties` file. */
    val properties: Properties
    /** The name of the plugin. */
    val name: String
    /** A short description of the plugin. */
    val description: String
    /** The version of the plugin, used for compatibility checks. */
    val version: String
    /** The author of the plugin. */
    val author: String
    /** The website of the plugin or the author. */
    val website: String?
    /** The URL of the plugin, used for loading resources. [PluginLoader] define it. */
    var jarUrl: URL
    /** The configuration. [PluginLoader] define it. */
    val configuration: Configuration
    /** The loader that manages this plugin. [PluginLoader] define it. */
    var pluginLoader: PluginLoader<*>
    /** Call when the plugin is enabled. */
    fun onEnable()
    /** Call when the plugin is disabled. */
    fun onDisable()

    /** Get the specific plugin folder. */
    fun getPluginFolder(): File {
        val dir = File(pluginLoader.directory, name)
        if(!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
}