package fr.valentinjdt.lib.plugin

import java.net.URL

interface IPlugin {
    val name: String
    val description: String
    var url: URL
    fun onEnable()
    fun onDisable()
}