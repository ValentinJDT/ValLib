package fr.valentin.lib.vallib.event

@Deprecated("Bad synchronization. Use EventRegister class instead.")
class EventRegisterLegacy private constructor() {

    private val listeners = mutableListOf<Listener>()

    companion object {
        private var instance: EventRegisterLegacy? = null

        fun getInstance(): EventRegisterLegacy {
            synchronized(EventRegisterLegacy::class.java) {
                if(instance == null) {
                    instance = EventRegisterLegacy()
                }
            }

            return instance!!
        }
    }

    fun runEvent(event: Event): Boolean = if(listeners.size == 0) true else listeners.all { it.execute(it, event) }

    fun registerListener(listener: Listener) {
        listeners += listener
    }

    fun removeListener(listener: Listener) {
        listeners -= listener
    }

}