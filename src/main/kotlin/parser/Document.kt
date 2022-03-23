package dev.shellbook.parser

data class Document(
    val metadata: Map<String, Any?>,
    val variables: List<Variable>,
    val children: List<Element>
) {

    fun find(fragment: String) : Element? {
        for (child in children) {
            child.find(fragment)?.let { return it }
        }

        return null
    }
}
