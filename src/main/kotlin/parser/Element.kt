package dev.shellbook.parser

sealed class Element {

    companion object {
        val metaRegex = Regex("\\s*\\{(.*)}$")
    }

    open fun find(id: String) : Element? {
        return null
    }

    object HorizontalRule : Element()

    sealed class Text : Element() {

        abstract fun removeMeta() : Pair<String, Text>

        abstract fun asText() : String
    }

    data class Simple(
        val value: String
    ) : Text() {

        override fun asText() = value

        override fun removeMeta() : Pair<String, Text> {
            val match = metaRegex.find(value)
            return if (match != null) (match.groupValues[1] to Simple(value.substring(0, match.range.first))) else ("" to this)
        }
    }

    data class Compound(
        val style: Style?,
        val children: List<Text>
    ) : Text() {

        override fun asText() = children.joinToString(separator = "") { it.asText() }

        override fun removeMeta() : Pair<String, Text> {
            return children.last().removeMeta().let {
                it.first to Compound(style, children.subList(0, children.size - 1) + it.second)
            }
        }
    }

    data class Link(
        val destination: String,
        val text: Text
    ) : Text() {

        override fun asText() = text.asText()

        override fun removeMeta() : Pair<String, Text> = "" to this
    }

    data class Image(
        val destination: String,
        val text: Text
    ) : Text() {

        override fun asText() = text.asText()

        override fun removeMeta() : Pair<String, Text> = "" to this
    }

    data class Block(
        val id: String,
        val classes: Set<String> = setOf(),
        val header: Text,
        val level: Int,
        val children: List<Element>
    ) : Element() {

        override fun find(id: String): Element? {
            if (this.id == id) return this

            for (child in children) {
                child.find(id)?.let { return it }
            }

            return null
        }
    }

    data class Paragraph(
        val child: Text
    ) : Element()

    data class MList(
        val ordered: Boolean,
        val children: List<Item>
    ) : Element() {

        override fun find(id: String): Element? {
            for (item in children) {
                for (child in item.children) {
                    child.find(id)?.let { return it }
                }
            }

            return null
        }
    }

    data class Item(
        val index: String?,
        val children: List<Element>
    )

    data class CodeBlock(
        val id: String?,
        val lang: String?,
        val classes: Set<String> = setOf(),
        val file: String?,
        val content: String
    ) : Element() {
        override fun find(id: String): Element? {
            return if (id == this.id) this else null
        }
    }

    enum class Style {
        emphasis,
        strong,
        blockquote,
        code
    }
}