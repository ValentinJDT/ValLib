package fr.valentin.lib.vallib.plugin

import java.net.URL

interface IPlugin {
    val name: String
    val description: String
    var url: URL
    fun onEnable()
    fun onDisable()
}