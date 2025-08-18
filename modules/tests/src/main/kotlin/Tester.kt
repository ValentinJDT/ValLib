package fr.valentinjdt.lib.tests

import kotlin.reflect.KClass

/**
 * Test all classes in the set.
 * @param testedClasses [Set]<[KClass]>
 */
class Tester(testedClasses: Set<KClass<*>>) {
    init {
        for(clazz in testedClasses) run {
            clazz.java.getDeclaredConstructor().newInstance()
        }
    }
}
