package fr.valentin.lib.vallib.plugin

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class InitOnceProperty<T>: ReadWriteProperty<Any, T> {

    private object EMPTY

    private var value: Any? = EMPTY

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (value == EMPTY) {
            throw IllegalStateException("Value isn't initialized")
        } else {
            return value as T
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        if (this.value != EMPTY) {
            throw IllegalStateException("Value is initialized")
        }
        this.value = value
    }


}
