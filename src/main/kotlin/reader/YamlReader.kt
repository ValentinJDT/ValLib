package fr.valentin.lib.vallib.reader

import java.io.InputStream
import java.util.*

class YamlReader(inputStream: InputStream) {

    private val properties = mutableMapOf<String, Any>()

    init {
        val scanner = Scanner(inputStream)
        var currentKey = ""

        while(scanner.hasNextLine()) {
            val line = scanner.nextLine()
            if(line.startsWith("#") || line.isEmpty()) {
                continue
            }

            if(line.contains("[") && line.contains("]")) {
                val listItems = line.substringAfter(":").replace("[", "").replace("]", "")
                    .split(",").map { it.trim().replace("\"", "") }.toMutableList()
                properties[line.substringBefore(":").trim()] = listItems
                continue
            }
            
            if(line.trim().startsWith("-")) {
                val listKey = currentKey.takeIf { it.isNotEmpty() } ?: "listKey"
                val existing = properties[listKey] as? MutableList<String> ?: mutableListOf()
                existing.add(line.trim().substring(1).trim().replace("\"", ""))
                properties[listKey] = existing
                continue
            }

            val elements = line.split(":")

            if(elements[1].isEmpty()) {
                if(!currentKey.isEmpty() && currentKey.last() != '.') {
                    currentKey += "."
                }

                if(elements[0].trim().length == elements[0].length) {
                    currentKey = ""
                }

                currentKey += elements[0].trim()

            } else if(!properties.containsKey(currentKey + "." + elements[0])) {

                if(elements[0].trim().length == elements[0].length) {
                    properties[elements[0].trim()] = elements[1]
                    currentKey = ""

                } else {
                    if(!currentKey.trim().isEmpty() && currentKey.last() != '.') {
                        currentKey += "."
                    }

                    properties[currentKey + elements[0].trim()] = elements[1]
                }
            }
        }
        scanner.close()
    }

    fun getString(key: String): String? = (properties[key] as? String)?.trim()?.convert()
    fun getDouble(key: String): Double? = (properties[key] as? String)?.convert()
    fun getFloat(key: String): Float? = (properties[key] as? String)?.convert()
    fun getInt(key: String): Int? = (properties[key] as? String)?.convert()
    fun getBoolean(key: String): Boolean? = (properties[key] as? String)?.convert()
    fun getStringList(key: String): List<String>? {
        return properties[key] as? List<String>
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> getValue(properties: Map<String, String>, key: String): T? {
        val value = properties[key]

        return when(T::class) {
            String::class -> value as? T
            Boolean::class -> value?.toBoolean() as? T
            Double::class -> value?.toDouble() as? T
            Float::class -> value?.toFloat() as? T
            Int::class -> value?.toInt() as? T
            else -> null
        }
    }

    private inline fun <reified T> String.convert(): T {
        return when(T::class) {
            Double::class -> toDouble()
            Int::class -> toInt()
            Float::class -> toFloat()
            String::class -> toString()
            Boolean::class -> this.trim().toBoolean()
            else -> error("Converter unavailable for ${T::class}")
        } as T
    }
}
