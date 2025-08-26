package fr.valentinjdt.lib.plugin.event

import fr.valentinjdt.lib.event.Cancellable
import fr.valentinjdt.lib.plugin.IPlugin

/**
 * Called when a plugin is already loaded.
 * @param path The path of the plugin jar file.
 */
data class PluginAlreadyLoadedEvent(val plugin: IPlugin): PluginEvent(plugin.jarUrl.path)

/**
 * Called when a plugin can't be loaded.
 * @param plugin The loaded plugin.
 */
open class PluginCanNotLoadEvent(override val path: String): PluginEvent(path)

/**
 * Called when a plugin is loaded.
 * Can be cancelled.
 * @param plugin The loaded plugin.
 */
data class PluginLoadEvent(val plugin: IPlugin): PluginEvent(plugin.jarUrl.path), Cancellable {
    override var cancel: Boolean = false
}

/**
 * Called when a plugin is unloaded.
 * Can be cancelled.
 * @param plugin The unloaded plugin.
 */
data class PluginUnLoadEvent(val plugin: IPlugin): PluginEvent(plugin.jarUrl.path), Cancellable {
    override var cancel: Boolean = false
}