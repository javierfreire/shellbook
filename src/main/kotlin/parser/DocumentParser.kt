package dev.shellbook.parser

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.accept
import org.intellij.markdown.ast.acceptChildren
import org.intellij.markdown.ast.visitors.Visitor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import java.util.Stack

fun parseDocument(content: String) : Document {
    val flavour = GFMFlavourDescriptor()
    val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(content)
    val visitor = DocumentVisitor(content)
    parsedTree.accept(visitor)

    return visitor.document()
}

private class DocumentVisitor(
    val content: String
) : Visitor {

    private val stack = Stack<ElementBuilder>()
    private val variables = mutableListOf<Variable>()

    init {
        stack.push(RootBuilder())
    }

    override fun visitNode(node: ASTNode) {
        //println("** ${node.type} ${node.text()}")
        when (node.type) {
            MarkdownElementTypes.MARKDOWN_FILE -> node.acceptChildren(this)
            MarkdownElementTypes.SETEXT_1 ->
                popUntilLevel(0).push(BlockBuilder(1, node.child(MarkdownTokenTypes.SETEXT_CONTENT).parseText(trimStart = true)))
            MarkdownElementTypes.ATX_1 ->
                popUntilLevel(0).push(BlockBuilder(1, node.child(MarkdownTokenTypes.ATX_CONTENT).parseText(trimStart = true)))
            MarkdownElementTypes.SETEXT_2 ->
                popUntilLevel(1).push(BlockBuilder(2, node.child(MarkdownTokenTypes.SETEXT_CONTENT).parseText(trimStart = true)))
            MarkdownElementTypes.ATX_2 ->
                popUntilLevel(1).push(BlockBuilder(2, node.child(MarkdownTokenTypes.ATX_CONTENT).parseText(trimStart = true)))
            MarkdownElementTypes.ATX_3 ->
                popUntilLevel(2).push(BlockBuilder(3, node.child(MarkdownTokenTypes.ATX_CONTENT).parseText(trimStart = true)))
            MarkdownElementTypes.ATX_4 ->
                popUntilLevel(3).push(BlockBuilder(4, node.child(MarkdownTokenTypes.ATX_CONTENT).parseText(trimStart = true)))
            MarkdownElementTypes.ATX_5 ->
                popUntilLevel(4).push(BlockBuilder(5, node.child(MarkdownTokenTypes.ATX_CONTENT).parseText(trimStart = true)))
            MarkdownTokenTypes.HORIZONTAL_RULE ->
                (stack.peek() as MainElementBuilder).add(Element.HorizontalRule)
            MarkdownElementTypes.PARAGRAPH ->
                (stack.peek() as MainElementBuilder).add(Element.Paragraph(node.parseText(trimStart = true)))
            MarkdownTokenTypes.TEXT,
            MarkdownTokenTypes.EMPH,
            MarkdownTokenTypes.LBRACKET,
            MarkdownTokenTypes.RBRACKET,
            MarkdownTokenTypes.SINGLE_QUOTE,
            MarkdownTokenTypes.DOUBLE_QUOTE,
            MarkdownTokenTypes.COLON ->
                (stack.peek() as TextBuilder).addText(node.text())
            MarkdownElementTypes.CODE_SPAN ->
                stack.peek().add(node.parseText(Element.Style.code, skipFirst = true, skipLast = true))
            MarkdownElementTypes.STRONG ->
                stack.peek().add(node.parseText(Element.Style.strong, skipFirst = true, skipLast = true, subroundBy = 2))
            MarkdownElementTypes.EMPH ->
                stack.peek().add(node.parseText(Element.Style.emphasis, skipFirst = true, skipLast = true))
            MarkdownTokenTypes.WHITE_SPACE ->
                if (stack.peek() is TextBuilder) {
                    (stack.peek() as TextBuilder).addText(" ")
                }
            MarkdownElementTypes.ORDERED_LIST ->
                (stack.peek() as MainElementBuilder).add(pushAndPop(ListBuilder(true), node).build())
            MarkdownElementTypes.LIST_ITEM ->
                (stack.peek() as ListBuilder).add(node.parseItem())
            MarkdownElementTypes.UNORDERED_LIST ->
                (stack.peek() as MainElementBuilder).add(pushAndPop(ListBuilder(false), node).build())
            MarkdownTokenTypes.EOL ->
                stack.peek().eol()
            MarkdownElementTypes.BLOCK_QUOTE ->
                (stack.peek() as MainElementBuilder).add(node.parseText(Element.Style.blockquote, skipFirst = true, skipLast = true))
            MarkdownElementTypes.CODE_FENCE ->
                (stack.peek() as MainElementBuilder).add(node.parseCodeBlock())
            MarkdownElementTypes.INLINE_LINK ->
                stack.peek().add(Element.Link(
                    destination = node.child(MarkdownElementTypes.LINK_DESTINATION).text(),
                    text = node.child(MarkdownElementTypes.LINK_TEXT).parseText(skipFirst = true, skipLast = true)
                ))
            MarkdownElementTypes.FULL_REFERENCE_LINK -> {
                stack.peek().add(Element.Link(
                    destination = node.child(MarkdownElementTypes.LINK_LABEL).text(),
                    text = node.child(MarkdownElementTypes.LINK_TEXT).parseText(skipFirst = true, skipLast = true)
                ))
            }
            MarkdownElementTypes.LINK_DEFINITION -> {
                val title = node.child(MarkdownElementTypes.LINK_TITLE).text()

                variables.add(Variable(
                    node.child(MarkdownElementTypes.LINK_LABEL).text(),
                    node.child(MarkdownElementTypes.LINK_DESTINATION).text(),
                    title.substring(1, title.length - 1)
                ))
            }
            MarkdownElementTypes.SHORT_REFERENCE_LINK -> {
                stack.peek().add(Element.Link(
                    destination = node.child(MarkdownElementTypes.LINK_LABEL).text(),
                    text = Element.Simple("")
                ))
            }
            MarkdownElementTypes.IMAGE -> {
                val linkNode = node.child(MarkdownElementTypes.INLINE_LINK)
                stack.peek().add(
                    Element.Image(
                        destination = linkNode.child(MarkdownElementTypes.LINK_DESTINATION).text(),
                        text = linkNode.child(MarkdownElementTypes.LINK_TEXT).parseText(skipFirst = true, skipLast = true)
                    )
                )
            }
            else -> (stack.peek() as TextBuilder).addText(node.text())
        }
    }

    private fun popUntilLevel(level: Int) : Stack<ElementBuilder> {
        while (stack.peek() is BlockBuilder && (stack.peek() as BlockBuilder).level > level) {
            val block = (stack.pop() as BlockBuilder).build()
            (stack.peek() as MainElementBuilder).add(block)
        }

        return stack
    }

    private fun <B : ElementBuilder> pushAndPop(
        builder: B,
        node: ASTNode,
        skipFirst: Boolean = false,
        skipLast: Boolean = false,
        subroundBy: Int = 1
    ) : B {
        stack.push(builder)

        for (child in node.children.subList(if (skipFirst) subroundBy else 0, node.children.size - if (skipLast) subroundBy else 0)) {
            child.accept(this)
        }

        stack.pop()

        return builder
    }

    fun document() : Document {
        popUntilLevel(0)
        val root = stack.pop() as RootBuilder

        return Document(metadata = mapOf(), variables = variables, children = root.children)
    }

    private fun ASTNode.parseText(
        style: Element.Style? = null,
        skipFirst: Boolean = false,
        skipLast: Boolean = false,
        subroundBy: Int = 1,
        trimStart: Boolean = false
    ) : Element.Text {
        return pushAndPop(TextBuilder(style, trimStart), this,
            skipFirst = skipFirst,
            skipLast = skipLast,
            subroundBy = subroundBy
        ).build()
    }

    private fun ASTNode.parseItem() : Element.Item {
        val index = if (children[0].type == MarkdownTokenTypes.LIST_NUMBER) children[0].text() else null

        return pushAndPop(RootBuilder(), this, skipFirst = true).let {
            Element.Item(index = index, children = it.children)
        }
    }

    private fun ASTNode.parseCodeBlock() : Element.CodeBlock {
        var lang = child(MarkdownTokenTypes.FENCE_LANG).text()
        val content = children.filter { it.type == MarkdownTokenTypes.CODE_FENCE_CONTENT }.joinToString("\n") { it.text() }
        val match = Element.metaRegex.find(lang)
        val meta: Meta?

        if (match != null) {
            lang = lang.substring(0, match.range.first)
            meta = Meta.parse(match.groupValues[1], classToAdd = lang)
        } else {
            meta = Meta()
        }

        return Element.CodeBlock(
            id = meta.id,
            classes = meta.classes,
            lang = lang,
            file = meta.label,
            content = content
        )
    }

    private sealed class ElementBuilder {

        abstract fun eol()

        abstract fun add(text: Element.Text)
    }

    private sealed class MainElementBuilder : ElementBuilder() {

        val children = mutableListOf<Element>()

        fun add(element: Element) {
            children.add(element)
        }

        override fun add(text: Element.Text) {
            children.add(text)
        }
    }

    private class RootBuilder : MainElementBuilder() {

        override fun eol() {

        }
    }

    private class BlockBuilder(
        val level: Int,
        completeHeader: Element.Text
    ) : MainElementBuilder() {

        val header: Element.Text
        val meta: Meta

        init {
            val (metaText, headerWithoutMeta) = completeHeader.removeMeta()
            header = headerWithoutMeta
            meta = Meta.parse(metaText, idGenerator = { headerWithoutMeta.asText().lowercase().replace(' ', '_') } )
        }

        fun build() : Element.Block {
            return Element.Block(
                id = meta.id!!,
                classes = meta.classes,
                header = header,
                level = level,
                children = children
            )
        }

        override fun eol() {

        }
    }

    private class ListBuilder(val ordered: Boolean) : ElementBuilder() {

        val children = mutableListOf<Element.Item>()

        fun add(item: Element.Item) {
            children.add(item)
        }

        override fun add(text: Element.Text) = TODO()

        fun build() : Element.MList {
            return Element.MList(
                ordered = ordered,
                children = children
            )
        }

        override fun eol() {

        }
    }

    private class TextBuilder(
        val style: Element.Style?,
        val trimStart: Boolean
    ) : ElementBuilder() {

        val children = mutableListOf<Element.Text>()

        override fun add(text: Element.Text) {
            if (text is Element.Simple) {
                addText(text.value)
            } else {
                children.add(text)
            }
        }

        fun addText(text: String) {
            if (children.isEmpty()) {
                children.add(Element.Simple(if (trimStart) text.trimStart() else text))
            } else {
                val last = children.last()

                if (last is Element.Simple) {
                    children.removeAt(children.lastIndex)
                    children.add(Element.Simple("${last.value}$text"))
                } else {
                    children.add(Element.Simple(text))
                }
            }
        }

        fun build() : Element.Text {
            if (style != null || children.size > 1) {
                return Element.Compound(style, children)
            }

            return children[0]
        }

        override fun eol() {
            addText(" ")
        }
    }

    private fun ASTNode.child(type: IElementType) : ASTNode {
        return children.first { it.type == type }
    }

    private fun ASTNode.text(): String {
        return content.subSequence(startOffset, endOffset).toString()
    }
}