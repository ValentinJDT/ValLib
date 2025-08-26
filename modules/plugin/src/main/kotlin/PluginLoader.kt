@file:Suppress("UNCHECKED_CAST")

package fr.valentinjdt.lib.plugin

import fr.valentinjdt.lib.plugin.event.*
import fr.valentinjdt.lib.event.EventRegister
import fr.valentinjdt.lib.utils.normalize
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.Locale
import java.util.Properties

class PluginLoader<T : IPlugin>(val directory: String) {

    private val plugins = mutableMapOf<T, URLClassLoader>()

    init {
        val dir = File(directory)

        if(!dir.exists())
            dir.mkdirs()

        loadPlugins()
    }

    companion object {

        val loaders = mutableMapOf<String, PluginLoader<out IPlugin>>()

        /**
         * It creates an instance if the type and the directory doesn't have instance.
         * If you prefer to receive the instance rather than a boolean value call [getInstance].
         *
         * @property dir The directory path where plugins are loaded. By default, it takes the class name of [T] as path.
         * @param T This is the type who is extends from [IPlugin].
         * @return true if an instance is created.
         */
        inline fun <reified T : IPlugin> createInstance(dir: String = getClassName(T::class.java) + "s"): Boolean {

            val fixedDir = dir.normalize().replace(" ", "_").lowercase()

            val created = !loaders.containsKey(T::class.java.name + "_" + fixedDir)

            getInstance<T>(fixedDir)

            return created
        }

        /**
         * It creates and return an instance if the type and the directory doesn't have instance.
         * To create an instance without using it, use [createInstance] instead of this function.
         *
         * @property dir The directory path where plugins are loaded. By default, it takes the class name of [T] as path.
         * @param T This is the type who is extends from IPlugin.
         * @return An instance of [PluginLoader]
         */
        inline fun <reified T : IPlugin> getInstance(dir: String = getClassName(T::class.java) + "s"): PluginLoader<T> {

            val fixedDir = dir.normalize().replace(" ", "_").lowercase()

            synchronized(PluginLoader::class.java) {

                val loader = loaders[T::class.java.name + "_" + fixedDir] ?: run {
                    val newLoader = PluginLoader<T>(fixedDir)
                    loaders[T::class.java.name + "_" + fixedDir] = newLoader
                    newLoader.enableAllPlugins()
                    newLoader
                }

                return loader as PluginLoader<T>
            }
        }

        fun unloadAll() = loaders.values.forEach { it.unloadPluginsFiles() }

        fun executeCommand(name: String, args: List<String>) =
            loaders.entries.filter { it.key.startsWith(Command::class.java.name) }
                .any { it.value.executeCommand(name, args) }

        fun getClassName(clazz: Class<*>): String {
            return clazz.name.substring(clazz.name.lastIndexOf(".") + 1).lowercase(Locale.getDefault());
        }

    }

    fun enableAllPlugins() {
        plugins.keys.forEach {
            try {
                enablePlugin(it)
            } catch(exception: Exception) {
                exception.printStackTrace()
                unloadPlugin(it)
            }
        }
    }

    fun enablePlugin(plugin: T) {
        plugin.onEnable()
    }

    fun reloadPlugin(name: String) {
        plugins.keys.find { it.name == name }?.apply { reloadPlugin(this) }
    }

    fun reloadPlugin(plugin: T) {
        unloadPlugin(plugin)
        loadPlugin(File(plugin.url.path), true)
    }

    fun reloadAllPlugins() {
        plugins.keys.forEach {
            unloadPlugin(it)
            loadPlugin(File(it.url.path), true)
        }
    }

    fun disableAllPlugins() = plugins.keys.forEach(IPlugin::onDisable)

    fun disablePlugin(name: String) {
        plugins.keys.find { it.name == name }?.apply { disablePlugin(this) }
    }

    fun disablePlugin(plugin: T) = plugin.onDisable()

    fun unloadPlugin(plugin: T) = unloadPlugin(plugin.name)

    fun unloadPlugin(name: String) {
        plugins.entries.find { it.key.name == name }?.apply {
            if(EventRegister.runEvent(PluginUnLoadEvent(this.key))) {
                disablePlugin(this.key)
                this.value.close()
                plugins.remove(this.key)
            }
        }
    }

    private fun loadPlugins() {
        for(file in File(directory).listFiles { _, filename -> filename.endsWith(".jar") }!!) {
            loadPlugin(file)
        }
    }

    fun loadPlugin(file: File, callEnable: Boolean = false): T? {

        var (plugin, classLoader) = loadMainClassPlugin(file.toURI().toURL())

        plugin?.run {
            if(!EventRegister.runEvent(PluginLoadEvent(this))) {
                classLoader?.close()
                plugins.remove(this)
                plugin = null
            } else {
                if(callEnable) {
                    enablePlugin(this)
                }
            }
        }

        return plugin
    }

    fun unloadPluginsFiles() {
        val iter = plugins.iterator()

        while(iter.hasNext()) {
            val entry = iter.next()

            if(EventRegister.runEvent(PluginUnLoadEvent(entry.key))) {
                disablePlugin(entry.key)
                entry.value.close()
                iter.remove()
            }
        }
    }

    fun getPlugins() = plugins.keys

    private fun loadMainClassPlugin(jarUrl: URL): Pair<T?, URLClassLoader?> {
        val classLoader = URLClassLoader(arrayOf(jarUrl))

        val properties = getProperties(classLoader, "plugin.properties")

        if(!properties.containsKey("main")) {
            classLoader.close()
            EventRegister.runEvent(PluginCanNotLoadEvent(jarUrl.toString()))
            return Pair(null, null)
        }

        val defaultClass = properties.getProperty("main")

        val clazz = try {
            classLoader.loadClass(defaultClass) as Class<out T>
        } catch(exception: ClassNotFoundException) {
            exception.printStackTrace()
            EventRegister.runEvent(PluginCanNotLoadEvent(jarUrl.toString()))
            null
        } ?: return Pair(null, null)

        val plugin = clazz.getDeclaredConstructor().newInstance() as T
        plugin.jarUrl = jarUrl
        plugin.pluginLoader = this

        val matchedPlugin = plugins.keys.find { it.name == plugin.name }

        if(matchedPlugin != null) {
            EventRegister.runEvent(PluginAlreadyLoadedEvent(matchedPlugin))
            classLoader.close()
            return Pair(null, null)
        }

        plugins[plugin] = classLoader

        return Pair(plugin, classLoader)
    }

    fun executeCommand(name: String, args: List<String>): Boolean {
        var found = false
        val command = plugins.keys.find { command -> command is Command && command.name.trim() == name } as Command?

        command?.run {
            try {
                this.execute(args)
            } catch(exception: Exception) {
                exception.printStackTrace()
            }
            found = true
        }

        return found
    }

}

fun getProperties(classLoader: URLClassLoader, fileName: String): Properties = Properties().apply {
    classLoader.getResourceAsStream(fileName)?.run { load(this) }
}