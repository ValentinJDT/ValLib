package fr.valentinjdt.lib.plugin.event

import fr.valentinjdt.lib.event.Event

/**
 * Base event for plugin events.
 * @param path The path of the plugin.
 */
open class PluginEvent(open val path: String): Event