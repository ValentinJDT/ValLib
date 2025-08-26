package fr.valentinjdt.lib.plugin.config


import fr.valentinjdt.lib.plugin.IPlugin
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration

class  Configuration(val plugin: IPlugin) {

    private val dataFolder = plugin.getPluginFolder()
    private val configFile = dataFolder.resolve("config.yml")

    private val yamlConfig: YamlConfiguration by lazy {
        if(!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        if(!configFile.exists()) {
            configFile.createNewFile()
        }

        YamlConfiguration.loadConfiguration(configFile)
    }

    fun <T> get(path: String, default: T? = null): T? = yamlConfig.get(path) as? T ?: default

    fun <T> set(path: String, value: T) = yamlConfig.set(path, value)

    fun loadDefaultConfig() = plugin.javaClass.classLoader.getResourceAsStream("config.yml")?.bufferedReader()?.also {
        val defaultConfig = YamlConfiguration.loadConfiguration(it)
        yamlConfig.setDefaults(defaultConfig)
        yamlConfig.options.copyDefaults = true
        save()
    }


    fun save() = yamlConfig.save(configFile)
}