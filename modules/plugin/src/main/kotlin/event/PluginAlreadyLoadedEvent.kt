package fr.valentinjdt.lib.plugin.event

import fr.valentinjdt.lib.plugin.IPlugin

data class PluginAlreadyLoadedEvent(val plugin: IPlugin): PluginCanNotLoadEvent(plugin.url.path)
