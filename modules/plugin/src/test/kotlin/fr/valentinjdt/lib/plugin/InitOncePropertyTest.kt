package fr.valentinjdt.lib.plugin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InitOncePropertyTest {

    @Test
    fun `test initialization of property`() {
        val propertyHolder = PropertyHolder()
        assertFailsWith<IllegalStateException> {
            // Accessing before initialization should throw an exception
            propertyHolder.value
        }

        // Set the value
        propertyHolder.value = "test"
        assertEquals("test", propertyHolder.value)

        // Trying to set the value again should throw an exception
        assertFailsWith<IllegalStateException> {
            propertyHolder.value = "another test"
        }
    }

    private class PropertyHolder {
        var value: String by InitOnceProperty()
    }
}
