package fr.valentinjdt.lib.plugin.event

import fr.valentinjdt.lib.event.Cancellable
import fr.valentinjdt.lib.plugin.IPlugin

data class PluginUnLoadEvent(val plugin: IPlugin): PluginEvent(plugin.url.path), Cancellable {
    override var cancel: Boolean = false
}