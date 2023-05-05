package fr.valentin.lib.vallib.plugin.event

import fr.valentin.lib.vallib.event.Cancellable
import fr.valentin.lib.vallib.plugin.IPlugin


data class PluginLoadEvent(val plugin: IPlugin): PluginEvent(plugin.url.path), Cancellable {
    override var cancel: Boolean = false
}