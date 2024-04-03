package fr.valentin.lib.vallib.instances

import java.lang.reflect.InvocationTargetException
import java.util.*

object Singletons {

    private val instances: MutableMap<String, Any> = HashMap()

    fun <T : Any> get(clazz: Class<T>): T? {
        return get(clazz, *emptyArray<Any>())
    }

    fun <T : Any, V> get(clazz: Class<T>, vararg arguments: V?): T? {
        return if (instances.containsKey(clazz.simpleName)) {
            instances[clazz.simpleName] as T?
        } else try {

            val classes = arguments.map { value: V? -> value!!::class.java }.toTypedArray()

            var instance: T

            if(arguments.isNotEmpty()) {
                instance = clazz.getDeclaredConstructor(*classes).newInstance(*arguments)
            } else {
                instance = clazz.getDeclaredConstructor(*classes).newInstance()
            }

            instances[clazz.simpleName] = instance

            instance
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        } catch (e: NoSuchMethodException) {
            throw RuntimeException(e)
        } catch (e: InstantiationException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }
    }

    fun <T> remove(clazz: Class<T>): Boolean {
        if (instances.containsKey(clazz.name)) {
            instances.remove(clazz.name)
            return true
        }
        return false
    }
}
