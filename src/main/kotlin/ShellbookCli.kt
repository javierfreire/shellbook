package dev.shellbook

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.check
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.file
import dev.shellbook.parser.parseDocument
import dev.shellbook.utils.ApplicationInfo
import java.io.File
import java.io.FileWriter

class ShellbookCli: CliktCommand(
    name= "shellbook",
    printHelpOnEmptyArgs = true,
    help = "Shellbook allows playing a Markdown document, executing the shell commands that it contains, and interacting with it."
) {

    private val document: DocumentContent by argument(help="URL or path to the Markdown file to reproduce. Can include a reference to a fragment.")
        .convert { DocumentContent.of(it) }
        .check(lazyMessage= { (it as DocumentContent.Invalid).message }) { it.valid }

    private val output: File? by option("-o", "--output", help="Output file. TODO. Don't work")
        .file(canBeDir = false, mustBeWritable = true)

    init {
        versionOption(ApplicationInfo.load().version)
    }

    override fun run() {
        val doc = document as DocumentContent.Valid
        val document = parseDocument(doc.content)
        val player = ScenePlayer(document)
        player.run(doc.fragment)

        output?.let {
            FileWriter(it).write(player.written())
        }
    }
}

fun main(args: Array<String>) = ShellbookCli().main(args)


