package fr.valentin.lib.vallib.event

object EventRegister {

    private val listeners = mutableListOf<Listener>();

    fun runEvent(event: Event): Boolean = if(listeners.size == 0) true else listeners.all { it.execute(it, event) }

    fun runEvent(event: Event, parent: Boolean): Boolean = if(listeners.size == 0) true else listeners.all { it.execute(it, event, parent) }

    fun registerListener(listener: Listener) {
        listeners += listener
    }

    fun removeListener(listener: Listener) {
        listeners -= listener
    }

}