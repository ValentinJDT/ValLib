package fr.valentinjdt.lib.plugin

import java.net.URL

abstract class Command(override val name: String, override val description: String = ""): IPlugin {
    override var url: URL by InitOnceProperty()
    abstract fun execute(args: List<String>)
    override fun onEnable() {}
    override fun onDisable() {}
}