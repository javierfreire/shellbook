package dev.shellbook.utils

import java.lang.System.lineSeparator

data class ShellCommand(val command: String, val result: String) {

    companion object {

        private val CONTINUE_COMMAND = Regex("""\\\s*$""")

        fun parse(text: String) : List<ShellCommand> {
            val commands = mutableListOf<ShellCommand>()
            val commandLines = mutableListOf<String>()
            val resultLines = mutableListOf<String>()
            var parsingCommand = false

            for (line in text.lines()) {
                if (line.trim().startsWith("$")) {
                    if (commandLines.isNotEmpty()) {
                        commands.add(ShellCommand(commandLines.joinLines(), resultLines.joinLines()))
                    }

                    commandLines.clear()
                    resultLines.clear()
                    commandLines.add(line)

                    parsingCommand = CONTINUE_COMMAND.find(line) != null
                } else if (parsingCommand) {
                    commandLines.add(line)
                    parsingCommand = CONTINUE_COMMAND.find(line) != null
                } else {
                    resultLines.add(line)
                }
            }

            commands.add(ShellCommand(commandLines.joinLines(), resultLines.joinLines()))

            return commands
        }

        private fun List<String>.joinLines() = joinToString(lineSeparator())
    }
}
