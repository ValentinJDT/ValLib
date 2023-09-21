package html

typealias Body = Tag.() -> Unit

/**
 * Use this class only in Kotlin.
 */
open class Tag {

    open val body: Body
    open val tag: String
    open var parent: Tag? = null

    private val properties = HashMap<String, String>()

    @Deprecated("Use set(key, value) instead of.")
    var className: String? = null
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

    private fun String.tirets() = replace(Regex("([A-Z])"), " $1").lowercase().replace(" ", "-")

    operator fun String.invoke(body: Body = {}): Tag {
        val tag = Tag(body, this.tirets(), this@Tag)
        innerTags.add(tag)
        return tag
    }

    private fun render(): String = innerTags.joinToString("")

    override fun toString(): String {
        val rendered = render()
        return if (str == null) "<$tag${ if(className != null) " class=\"$className\"" else ""}${properties.map { " ${it.key}=\"${it.value}\"" }.joinToString("")}${if(rendered != "") ">${rendered}</$tag>" else "/>"}" else str!!
    }
}

class html(override var parent: Tag? = null, override val body: Body) : Tag(body, "html", parent)

class body(override var parent: Tag? = null, override val body: Body) : Tag(body, "body", parent)

private class text(str: String) : Tag({}, "") {
    init {
        super.str = str
    }
}
