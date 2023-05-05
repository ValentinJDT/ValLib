package fr.valentin.lib.vallib.event

object EventRegister {

    private val listeners = mutableListOf<Listener>();

    fun runEvent(event: Event, parent: Boolean = false): Boolean = if(listeners.size == 0) true else listeners.all { it.execute(event, parent) }

    fun registerListener(listener: Listener) {
        listeners += listener
    }

    fun removeListener(listener: Listener) {
        listeners -= listener
    }

}