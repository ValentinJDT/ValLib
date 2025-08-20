@file:Suppress("MUST_BE_INITIALIZED_OR_FINAL_OR_ABSTRACT_WARNING")

package fr.valentinjdt.lib.html

typealias Body = Tag.() -> Unit

/**
 * Use this class only in Kotlin.
 */
open class Tag {

    val body: Body
    val tag: String
    var parent: Tag? = null

    private val properties = LinkedHashMap<String, String?>()

    var className: String?
        get() = properties["class"]
        set(value) { properties["class"] = value ?: "" }

    val innerTags = ArrayList<Tag>()
    var str: String? = null

    constructor(body: Body, tag: String, parent: Tag? = null) {
        this.body = body
        this.tag = tag
        this.parent = parent
        body(this)
    }

    operator fun String.unaryPlus(): Boolean = innerTags.add(text(this))

    fun set(key: String, value: String) = properties.put(key, value)

    fun get(key: String): String? = properties[key]

    fun has(key: String): Boolean = properties.containsKey(key)

    private fun String.tirets() = replace(Regex("([A-Z])"), " $1").lowercase().replace(" ", "-")

    operator fun String.invoke(body: Body = {}): Tag {
        val tag = Tag(body, this.tirets(), this@Tag)
        innerTags.add(tag)
        return tag
    }

    private fun render(): String = innerTags.joinToString("")

    override fun toString(): String {
        val rendered = render()
        return if (str == null) "<$tag${properties.map { " ${it.key}=\"${it.value}\"" }.joinToString("")}${if(rendered != "") ">${rendered}</$tag>" else "/>"}" else str!!
    }
}

class html(parent: Tag? = null, body: Body) : Tag(body, "html", parent)

class body(parent: Tag? = null, body: Body) : Tag(body, "body", parent)

private class text(str: String) : Tag({}, "") {
    init {
        super.str = str
    }
}
