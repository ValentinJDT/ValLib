package fr.valentinjdt.lib.event

import kotlin.test.*

class EventSystemTests {

    @Test
    fun testBasicEventHandling() {
        // Création d'un événement simple
        val event = TestEvent()

        // Création et enregistrement d'un listener
        val listener = TestListener()
        EventRegister.registerListener(listener)

        // Exécution de l'événement
        val result = EventRegister.runEvent(event)

        // Vérifications
        assertTrue(result, "L'événement devrait être exécuté avec succès")
        assertTrue(listener.eventHandled, "Le gestionnaire d'événement devrait avoir été appelé")

        // Nettoyage
        EventRegister.removeListener(listener)
    }

    @Test
    fun testCancellableEvent() {
        // Création d'un événement annulable
        val event = TestCancellableEvent()

        // Création et enregistrement d'un listener qui annule l'événement
        val cancellingListener = CancellingListener()
        EventRegister.registerListener(cancellingListener)

        // Exécution de l'événement
        val result = EventRegister.runEvent(event)

        // Vérifications
        assertFalse(result, "L'événement devrait être annulé")
        assertTrue(event.cancel, "La propriété cancel de l'événement devrait être true")

        // Nettoyage
        EventRegister.removeListener(cancellingListener)
    }

    @Test
    fun testMultipleListeners() {
        // Création d'un événement
        val event = TestEvent()

        // Création et enregistrement de plusieurs listeners
        val listener1 = TestListener()
        val listener2 = TestListener()
        EventRegister.registerListener(listener1)
        EventRegister.registerListener(listener2)

        // Exécution de l'événement
        val result = EventRegister.runEvent(event)

        // Vérifications
        assertTrue(result, "L'événement devrait être exécuté avec succès")
        assertTrue(listener1.eventHandled, "Le premier listener devrait avoir traité l'événement")
        assertTrue(listener2.eventHandled, "Le second listener devrait avoir traité l'événement")

        // Nettoyage
        EventRegister.removeListener(listener1)
        EventRegister.removeListener(listener2)
    }

    @Test
    fun testParentEventHandling() {
        // Création d'un événement enfant
        val childEvent = ChildEvent()

        // Création et enregistrement d'un listener pour l'événement parent
        val parentListener = ParentEventListener()
        EventRegister.registerListener(parentListener)

        // Exécution de l'événement enfant avec parent=true
        val result = EventRegister.runEvent(childEvent, true)

        // Vérifications
        assertTrue(result, "L'événement devrait être exécuté avec succès")
        assertTrue(parentListener.parentEventHandled, "Le gestionnaire d'événement parent devrait avoir été appelé")

        // Nettoyage
        EventRegister.removeListener(parentListener)
    }

    @Test
    fun testListenerRemoval() {
        // Création d'un événement
        val event = TestEvent()

        // Création et enregistrement d'un listener
        val listener = TestListener()
        EventRegister.registerListener(listener)

        // Suppression du listener
        EventRegister.removeListener(listener)

        // Reset de l'état du listener
        listener.eventHandled = false

        // Exécution de l'événement
        EventRegister.runEvent(event)

        // Vérification
        assertFalse(listener.eventHandled, "Le listener supprimé ne devrait pas être appelé")
    }
}

// Événements de test
class TestEvent : Event
class ChildEvent : ParentEvent()
open class ParentEvent : Event
class TestCancellableEvent : Event, Cancellable {
    override var cancel: Boolean = false
}

// Listeners de test
class TestListener : Listener() {
    var eventHandled = false

    @EventHandler
    fun onTestEvent(event: TestEvent) {
        eventHandled = true
    }
}

class CancellingListener : Listener() {
    @EventHandler
    fun onTestCancellableEvent(event: TestCancellableEvent) {
        event.cancel = true
    }
}

class ParentEventListener : Listener() {
    var parentEventHandled = false

    @EventHandler
    fun onParentEvent(event: ParentEvent) {
        parentEventHandled = true
    }
}
