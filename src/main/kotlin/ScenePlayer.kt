package dev.shellbook

import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.widgets.Padding
import com.github.ajalt.mordant.widgets.Panel
import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptConfirm
import com.github.kinquirer.components.promptInput
import com.github.kinquirer.components.promptListObject
import com.github.kinquirer.core.Choice
import dev.shellbook.parser.Document
import dev.shellbook.parser.Element
import dev.shellbook.render.RenderMode
import dev.shellbook.render.TerminalRender
import dev.shellbook.utils.ShellCommand
import dev.shellbook.utils.VariableScanner
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

class ScenePlayer(
    private val document: Document,
    private val stepWaitMillis: Long = 300L
) {

    private val render = TerminalRender()
    private val variableValues = mutableMapOf<String, String>()
    private val variablesRegex = Regex("\\[(\\w+)]")

    fun run(fragment: String?) {
        val renderMode = RenderMode(TextStyle())
        if (fragment == null) {
            document.children.forEach { process(it, renderMode) }
        } else {
            val element = document.find(fragment) ?: throw PrintMessage("Fragment not found: #$fragment")
            process(element, renderMode)
        }
    }

    fun written() : String = "TODO"

    private fun process(element: Element, mode: RenderMode) {
        when (element) {
            is Element.HorizontalRule -> render.printLine('-', mode)
            is Element.Simple -> render.print(mode, element.value)
            is Element.Compound -> (mode + element.style).let {
                for (child in element.children) {
                    process(child, it)
                }
            }
            is Element.Link -> {
                val value = variableValues[element.destination]

                if (value != null) {
                    render.print(mode.copy(style = mode.style + TextColors.black on TextColors.green), value)
                } else {
                    render.print(
                        mode.copy(style = mode.style + TextColors.black on TextColors.blue + TextStyles.hyperlink(element.destination)),
                        element.text.asText()
                    )
                }
            }
            is Element.Image -> render.print(mode, "[image:${element.text}:TODO]")
            is Element.Paragraph -> {
                element.child.checkVariables()
                process(element.child, mode)

                if (mode.inList) {
                    render.nextLine()
                } else {
                    render.separate()
                }
            }
            is Element.Block -> {
                element.header.checkVariables()
                processBlock(element, mode)
            }
            is Element.MList -> {
                for (item in element.children) {
                    render.print(mode, "${mode.indent}${item.index ?: "-"} ")

                    for (child in item.children) {
                        process(child, mode + "  ")
                    }
                }
            }
            is Element.CodeBlock -> processCode(element, mode)
        }
    }

    private fun processCode(block: Element.CodeBlock, mode: RenderMode) {
        block.checkVariables()
        render.separate()

        if (block.lang == "shell" && block.classes.contains("play")) {
            processShellCommands(ShellCommand.parse(block.content), mode)
        } else {
            var prettyContent = block.content

            variablesRegex.findAll(block.content).forEach {
                variableValues[it.value]?.let { value ->
                    prettyContent = prettyContent.replace(it.value, TextStyles.inverse(value))
                }
            }

            val panel = Panel(
                prettyContent,
                title = block.file,
                titleAlign = TextAlign.LEFT,
                bottomTitle = block.lang,
                bottomTitleAlign = TextAlign.RIGHT,
                expand = true,
                padding = Padding.horizontal(2),
            )

            render.println(mode, panel)
            render.separate()

            block.file?.let {
                if (KInquirer.promptConfirm("Write to file $it", default = false)) {
                    Files.writeString(Path.of(it), block.content)
                    render.print(mode, "Saved")
                }
            }
        }
    }

    private fun processShellCommands(commands: List<ShellCommand>, mode: RenderMode) {
        for (command in commands) {
            render.println(mode + TextColors.green, command.command)

            if (KInquirer.promptConfirm("Invoke", default = false)) {
                render.nextLine()

                val process = ProcessBuilder()
                    .command(listOf("/bin/sh", "-c", command.command.substringAfter("$")))
                    .start()

                val result = process.inputStream.readText()
                render.println(mode + TextColors.blue, result)
                render.println(mode + TextColors.red, process.errorStream.readText())

                if (command.result.isNotEmpty()) {
                    VariableScanner(command.result).scan(result).values.forEach { key, value ->
                        variableValues["[$key]"] = value
                    }
                }
            }
        }
    }

    private fun processBlock(block: Element.Block, mode: RenderMode) {
        if (block.classes.contains("menu")) {
            printHeader(block.header, block.level)

            val options = buildList<Choice<Element.Block?>> {
                addAll(block.children
                    .filterIsInstance<Element.Block>()
                    .map { Choice(it.header.asText(), it) })
                add(Choice("Continue", null))
            }

            do {
                render.separate()
                val option = KInquirer.promptListObject("Menu: Select an option", choices = options)
                option?.let {
                    render.separate()
                    process(option, mode)
                }
            } while  (option != null)
        } else if (block.classes.contains("choice")) {
            printHeader(block.header, block.level)

            val options = buildList<Choice<Element.Block?>> {
                addAll(block.children
                    .filterIsInstance<Element.Block>()
                    .map { Choice(it.header.asText(), it) })
                add(Choice("Skip", null))
            }

            render.separate()
            KInquirer.promptListObject("Choice: Select an option", choices = options)?.let {
                render.separate()
                process(it, mode)
            }
        } else if (block.classes.contains("optional")) {
            if (KInquirer.promptConfirm("Optional: ${block.header.asText()}?")) {
                printHeader(block.header, block.level)
                render.separate()

                for (child in block.children) {
                    process(child, mode)
                }
            } else {
                render.separate()
            }
        } else {
            printHeader(block.header, block.level)

            for (child in block.children) {
                process(child, mode)
            }
        }
    }

    private fun printHeader(text: Element.Text, level: Int) {
        render.separate()

        when (level) {
            1 -> with(RenderMode(TextColors.blue)) {
                process(text, this)
                render.nextLine().printLine('=',  this)
            }
            2 -> with(RenderMode(TextColors.brightBlue)) {
                process(text, this)
                render.nextLine().printLine('-',  this)
            }
            3 -> with(RenderMode(TextColors.cyan)) {
                render.print(this, "### ")
                process(text, this)
            }
            4 -> with(RenderMode(TextColors.brightCyan)) {
                render.print(this, "#### ")
                process(text, this)
            }
            5 -> with(RenderMode(TextColors.blue)) {
                render.print(this, "##### ")
                process(text, this)
            }
        }

        render.separate()
    }

    private fun Element.CodeBlock.checkVariables() {
        variablesRegex.findAll(content).forEach {
            checkVariable(it.value)
        }
    }

    private fun Element.Text.checkVariables() {
        when (this) {
            is Element.Compound -> children.map { it.checkVariables() }
            is Element.Link -> checkVariable(destination)
            else -> { }
        }
    }

    private fun checkVariable(name: String) {
        if (variableValues[name] == null) {
            val variable = document.variables.firstOrNull { it.name == name }

            variable?.let {
                if (it.destination == "{input}") {
                    variableValues[name] = KInquirer.promptInput(variable.description)
                }
            }
        }
    }

    private fun InputStream.readText() : String = reader().use { it.readText() }
}