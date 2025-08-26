package fr.valentinjdt.lib.plugin.event

import fr.valentinjdt.lib.plugin.IPlugin

/**
 * Called before a plugin is enabled.
 * @param plugin The pre-enabled plugin.
 */
data class PluginPreEnableEvent(val plugin: IPlugin): PluginEvent(plugin.jarUrl.path)

/**
 * Called when a plugin is enabled.
 * @param plugin The enabled plugin.
 */
data class PluginEnableEvent(val plugin: IPlugin): PluginEvent(plugin.jarUrl.path)

/**
 * Called before a plugin is disabled.
 * @param plugin The pre-disabled plugin.
 */
data class PluginPreDisableEvent(val plugin: IPlugin): PluginEvent(plugin.jarUrl.path)

/**
 * Called when a plugin is disabled.
 * @param plugin The disabled plugin.
 */
data class PluginDisableEvent(val plugin: IPlugin): PluginEvent(plugin.jarUrl.path)
