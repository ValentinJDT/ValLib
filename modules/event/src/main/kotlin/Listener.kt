package fr.valentinjdt.lib.event

open class Listener {

    fun execute(event: Event, parent: Boolean = false): Boolean {
        val functions = this::class.java.declaredMethods

        for(function in functions.filter { it.isAnnotationPresent(EventHandler::class.java) }) {
            for(parameter in function.parameters.filter { it.type === event::class.java || (parent && it.type === event::class.java.superclass) }) {
                function.invoke(this, event)
                if(event is Cancellable && event.cancel)
                    return false
            }
        }

        return true
    }

}