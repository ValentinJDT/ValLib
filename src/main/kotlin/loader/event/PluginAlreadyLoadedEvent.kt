package fr.valentin.lib.vallib.plugin.event

import fr.valentin.lib.vallib.plugin.IPlugin

data class PluginAlreadyLoadedEvent(val plugin: IPlugin): PluginCanNotLoadEvent(plugin.url.path)
