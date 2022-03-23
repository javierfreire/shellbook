package dev.shellbook.render

import com.github.ajalt.mordant.terminal.Terminal

class TerminalRender(
    private val stepWaitMillis: Long = 300L
) {

    private val terminal = Terminal()
    private var separated = 0
    private var column = 0

    init {
        terminal.info.updateTerminalSize()
    }

    fun nextLine() : TerminalRender {
        Thread.sleep(stepWaitMillis)
        terminal.println()
        separated = 0
        column = 0
        return this
    }

    fun separate() {
        Thread.sleep(stepWaitMillis)
        while (separated++ < 1) {
            terminal.println()
            column = 0
        }
    }

    fun println(mode: RenderMode, message: Any) {
        Thread.sleep(stepWaitMillis)
        print(mode, message)
        terminal.println()
        separated = 0
        column = 0
    }

    fun print(mode: RenderMode, message: Any) {
        separated = -1

        if (message is String) {
            val width = terminal.info.width
            val words = message.split(" ")
            var first = true

            for (word in words) {
                if (column + word.length < width) {
                    if (!first) {
                        terminal.print(mode.style(" "))
                        column += 1
                    }

                    column += word.length
                } else {
                    terminal.println()
                    terminal.print(mode.indent)
                    column = word.length + mode.indent.length
                }

                if (first) {
                    first = false
                }

                terminal.print(mode.style(word))
            }
        } else {
            terminal.print(message)
        }
    }

    fun printLine(character: Char, mode: RenderMode) {
        Thread.sleep(stepWaitMillis)
        terminal.println(mode.style("".padEnd(terminal.info.width, padChar = character)))
        separated = 0
        column = 0
    }
}