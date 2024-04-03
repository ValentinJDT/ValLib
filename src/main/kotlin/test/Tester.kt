package fr.valentin.lib.vallib.test

import kotlin.reflect.KClass

class Tester(testedClasses: Set<KClass<*>>) {
    init {
        for(clazz in testedClasses) run {
            clazz.java.getDeclaredConstructor().newInstance()
        }
    }
}
