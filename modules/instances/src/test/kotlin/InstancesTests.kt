package fr.valentinjdt.lib.instances

import kotlin.test.*

class InstancesTests {

    @BeforeTest
    fun setUp() {
        // Assurons-nous que les instances sont nettoyées avant chaque test
        clearAllInstances()
    }

    @Test
    fun testGetWithNoArguments() {
        // Test pour obtenir une instance sans arguments
        val testInstance = Instances.get(TestClass::class.java)

        // Vérifications
        assertNotNull(testInstance, "L'instance ne devrait pas être null")
        assertEquals("Default Value", testInstance.value, "La valeur devrait être celle par défaut")

        // Vérifier que la même instance est retournée lors d'un second appel
        val secondInstance = Instances.get(TestClass::class.java)
        assertSame(testInstance, secondInstance, "La même instance devrait être retournée")
    }

    @Test
    fun testGetWithArguments() {
        // Test pour obtenir une instance avec des arguments
        val customValue = "Custom Value"
        val testInstance = Instances.get(TestClassWithArgs::class.java, customValue)

        // Vérifications
        assertNotNull(testInstance, "L'instance ne devrait pas être null")
        assertEquals(customValue, testInstance.value, "La valeur devrait être celle passée en argument")

        // Vérifier que la même instance est retournée lors d'un second appel
        val secondInstance = Instances.get(TestClassWithArgs::class.java)
        assertSame(testInstance, secondInstance, "La même instance devrait être retournée")
    }

    @Test
    fun testRemoveExistingInstance() {
        // Créer une instance
        val testInstance = Instances.get(TestClass::class.java)
        assertNotNull(testInstance, "L'instance devrait être créée avec succès")

        // Supprimer l'instance
        val removed = Instances.remove(TestClass::class.java)

        // Vérifier que l'instance a été supprimée
        assertTrue(removed, "La méthode remove devrait retourner true")

        // Vérifier qu'une nouvelle instance est créée après suppression
        val newInstance = Instances.get(TestClass::class.java)
        assertNotSame(testInstance, newInstance, "Une nouvelle instance devrait être créée après suppression")
    }

    @Test
    fun testRemoveNonExistingInstance() {
        // Tenter de supprimer une instance qui n'existe pas
        val removed = Instances.remove(NonExistingClass::class.java)

        // Vérifier que rien n'a été supprimé
        assertFalse(removed, "La méthode remove devrait retourner false pour une classe non instanciée")
    }

    @Test
    fun testMultipleClasses() {
        // Tester avec plusieurs classes différentes
        val testClass1 = Instances.get(TestClass::class.java)
        val testClass2 = Instances.get(TestClassWithArgs::class.java, "Test")
        val testClass3 = Instances.get(AnotherTestClass::class.java)

        // Vérifier que les instances sont distinctes
        assertNotSame<Any>(testClass1!!, testClass2!!, "Les instances de classes différentes devraient être distinctes")
        assertNotSame<Any>(testClass1, testClass3!!, "Les instances de classes différentes devraient être distinctes")
        assertNotSame<Any>(testClass2, testClass3, "Les instances de classes différentes devraient être distinctes")
    }

    @Test
    fun testConstructorException() {
        // Tester avec une classe qui lance une exception dans son constructeur
        assertFailsWith<RuntimeException> {
            Instances.get(ExceptionThrowingClass::class.java)
        }
    }

    /**
     * Méthode utilitaire pour nettoyer toutes les instances (via réflexion)
     * Cette méthode est utilisée uniquement pour les tests et ne devrait pas être nécessaire en production
     */
    private fun clearAllInstances() {
        try {
            val instancesClass = Instances::class.java
            val instancesField = instancesClass.getDeclaredField("instances")
            instancesField.isAccessible = true
            val instances = instancesField.get(Instances) as MutableMap<*, *>
            instances.clear()
        } catch (e: Exception) {
            fail("Impossible de nettoyer les instances: ${e.message}")
        }
    }

    // Classes de test

    class TestClass {
        val value: String = "Default Value"
    }

    class TestClassWithArgs(val value: String = "Default Value")

    class AnotherTestClass {
        val someValue: Int = 42
    }

    class ExceptionThrowingClass {
        init {
            throw IllegalStateException("Test exception")
        }
    }

    class NonExistingClass
}
