package fr.valentinjdt.lib.event

import kotlin.test.*

class ListenerTests {

    @Test
    fun testListenerExecuteMethod() {
        // Création d'un événement
        val event = SimpleEvent()

        // Création d'un listener
        val listener = ComplexListener()

        // Exécution directe de la méthode execute
        val result = listener.execute(event)

        // Vérifications
        assertTrue(result, "La méthode execute devrait retourner true")
        assertTrue(listener.simpleEventHandled, "Le gestionnaire d'événement simple devrait avoir été appelé")
        assertFalse(listener.otherEventHandled, "Le gestionnaire d'événement autre ne devrait pas avoir été appelé")
    }

    @Test
    fun testMultipleHandlersForSameEvent() {
        // Création d'un événement
        val event = SimpleEvent()

        // Création d'un listener avec plusieurs gestionnaires pour le même événement
        val listener = MultiHandlerListener()

        // Exécution de la méthode execute
        listener.execute(event)

        // Vérifications
        assertTrue(listener.handler1Called, "Le premier gestionnaire devrait être appelé")
        assertTrue(listener.handler2Called, "Le deuxième gestionnaire devrait être appelé")
    }

    @Test
    fun testCancellationInListener() {
        // Création d'un événement annulable
        val event = SimpleCancellableEvent()

        // Création d'un listener qui annule l'événement
        val listener = CancellationListener()

        // Exécution de la méthode execute
        val result = listener.execute(event)

        // Vérifications
        assertFalse(result, "La méthode execute devrait retourner false car l'événement est annulé")
        assertTrue(event.cancel, "L'événement devrait être annulé")
    }

    @Test
    fun testParentEventHandling() {
        // Création d'un événement enfant
        val childEvent = ChildTestEvent()

        // Création d'un listener pour l'événement parent
        val listener = ParentListener()

        // Exécution sans le flag parent
        var result = listener.execute(childEvent)

        // Vérification que le gestionnaire n'est pas appelé
        assertTrue(result, "La méthode execute devrait retourner true")
        assertFalse(listener.parentEventHandled, "Le gestionnaire d'événement parent ne devrait pas être appelé sans le flag parent")

        // Réinitialisation
        listener.parentEventHandled = false

        // Exécution avec le flag parent
        result = listener.execute(childEvent, true)

        // Vérification que le gestionnaire est appelé
        assertTrue(result, "La méthode execute devrait retourner true")
        assertTrue(listener.parentEventHandled, "Le gestionnaire d'événement parent devrait être appelé avec le flag parent")
    }

    @Test
    fun testNoMatchingHandlers() {
        // Création d'un événement pour lequel il n'y a pas de gestionnaire
        val event = UnhandledEvent()

        // Création d'un listener sans gestionnaire pour cet événement
        val listener = SimpleListener()

        // Exécution de la méthode execute
        val result = listener.execute(event)

        // Vérification
        assertTrue(result, "La méthode execute devrait retourner true même sans gestionnaire correspondant")
        assertFalse(listener.simpleEventHandled, "Aucun gestionnaire ne devrait être appelé")
    }
}

// Événements de test
class SimpleEvent : Event
class OtherEvent : Event
class UnhandledEvent : Event
class ChildTestEvent : ParentTestEvent()
open class ParentTestEvent : Event
class SimpleCancellableEvent : Event, Cancellable {
    override var cancel: Boolean = false
}

// Listeners de test
class SimpleListener : Listener() {
    var simpleEventHandled = false

    @EventHandler
    fun onSimpleEvent(event: SimpleEvent) {
        simpleEventHandled = true
    }
}

class ComplexListener : Listener() {
    var simpleEventHandled = false
    var otherEventHandled = false

    @EventHandler
    fun onSimpleEvent(event: SimpleEvent) {
        simpleEventHandled = true
    }

    @EventHandler
    fun onOtherEvent(event: OtherEvent) {
        otherEventHandled = true
    }

    fun notAnEventHandler(event: SimpleEvent) {
        // Cette méthode ne devrait pas être appelée car elle n'est pas annotée
    }
}

class MultiHandlerListener : Listener() {
    var handler1Called = false
    var handler2Called = false

    @EventHandler
    fun onSimpleEvent1(event: SimpleEvent) {
        handler1Called = true
    }

    @EventHandler
    fun onSimpleEvent2(event: SimpleEvent) {
        handler2Called = true
    }
}

class CancellationListener : Listener() {
    @EventHandler
    fun onCancellableEvent(event: SimpleCancellableEvent) {
        event.cancel = true
    }
}

class ParentListener : Listener() {
    var parentEventHandled = false

    @EventHandler
    fun onParentEvent(event: ParentTestEvent) {
        parentEventHandled = true
    }
}
