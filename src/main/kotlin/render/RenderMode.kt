package dev.shellbook.render

import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles
import dev.shellbook.parser.Element

data class RenderMode(
    val style: TextStyle,
    val indent: String = "",
    val inList: Boolean = false
) {

    operator fun plus(style: Element.Style?) : RenderMode {
        return when(style) {
            null -> this
            Element.Style.emphasis -> copy(style = this.style + TextStyles.underline)
            Element.Style.strong -> copy(style = this.style + TextStyles.bold)
            Element.Style.blockquote -> copy(style = this.style + TextStyles.italic)
            Element.Style.code -> copy(style = this.style + TextStyles.inverse)
        }
    }

    operator fun plus(style: TextStyle) : RenderMode {
        return copy(style = this.style + style)
    }

    operator fun plus(indent: String) : RenderMode {
        return copy(indent = this.indent + indent, inList = true)
    }
}