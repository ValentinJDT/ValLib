package fr.valentin.lib.vallib.utils

import java.text.Normalizer
import java.util.logging.Logger
import kotlin.reflect.KClass

/**
 * Can do everything with that function.
 * @return [R]
 */
fun <T : Any, R : Any> T.function(body: T.() -> R) = body(this)


/**
 * Get logger of a class.
 * @return [java.util.logging.Logger]
 */
fun KClass<*>.logger() = Logger.getLogger(this.java.name)

private val REGEX_UNACCENT = "\\p{InCOMBINING_DIACRITICAL_MARKS}+".toRegex()

/**
 * Remove accents from string.
 */
fun String.normalize(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return REGEX_UNACCENT.replace(temp, "")
}
