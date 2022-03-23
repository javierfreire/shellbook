package dev.shellbook.parser

data class Meta(
    val id: String? = null,
    val classes: Set<String> = setOf(),
    val label: String? = null
) {

    companion object {

        private val idRegex = Regex("#([\\w_-]+)")
        private val classRegex = Regex("\\.([\\w_-]+)")
        private val labelRegex = Regex("\"([\\w_.-/]+)\"")

        fun parse(
            text: String,
            idGenerator: (() -> String)? = null,
            classToAdd: String? = null
        ) : Meta {
            val classes = mutableSetOf<String>()
            var startIndex = 0
            val label = labelRegex.find(text)?.groupValues?.get(1)
            val textWithoutLabel = if (label != null) text.replaceFirst(label, "") else text

            do {
                val ended = classRegex.find(textWithoutLabel, startIndex)?.also {
                    classes.add(it.groupValues[1])
                    startIndex = it.range.last
                } == null
            } while (!ended)

            classToAdd?.let { classes.add(it) }

            return Meta(
                id = idRegex.find(textWithoutLabel)?.groupValues?.get(1) ?: idGenerator?.invoke(),
                classes = classes,
                label = label
            )
        }
    }
}