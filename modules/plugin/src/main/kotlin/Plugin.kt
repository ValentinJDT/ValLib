package fr.valentinjdt.lib.plugin

import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import kotlin.lazy

/**
 * Base implementation of a plugin.
 * You can extend this class to create your own plugin.
 * The plugin.properties file must be present in the root of the jar file, or it's possible to override properties to avoid using plugin.properties file.
 */
abstract class Plugin: IPlugin {

    override val properties: Properties by lazy {
        if(Files.exists(Path.of(jarUrl.path))) {
            getProperties(URLClassLoader(arrayOf(jarUrl)), "plugin.properties")
        } else {
            Properties()
        }
    }

    override val name: String
        get() = properties.getProperty("name", "Unknown")

    override val description: String
        get() = properties.getProperty("description", "No description")

    override val author: String
        get() = properties.getProperty("author", "Unknown")

    override val version: String
        get() = properties.getProperty("version", "Unknown")

    override val website: String?
        get() = properties.getProperty("website")

    override var jarUrl: URL by InitOnceProperty()
    override var pluginLoader: PluginLoader<*> by InitOnceProperty()
    override fun onEnable() {}
    override fun onDisable() {}
}