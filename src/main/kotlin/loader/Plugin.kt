package fr.valentin.lib.vallib.plugin

import java.net.URL

abstract class Plugin(override val name: String, override val description: String = ""): IPlugin {
    override var url: URL by InitOnceProperty()
    override fun onEnable() {}
    override fun onDisable() {}
}