package dev.shellbook.utils

class VariableScanner(template: String) {

    companion object {
        private const val VARIABLE_START_MARK: String = "["

        private const val VARIABLE_END_MARK: String = "]"
    }

    private val tokens: List<Token>
    private val template: String

    init {
        val trimmed = template.trim()
        this.template = trimmed
        val tokens = mutableListOf<Token>()
        var currentIndex = 0

        while(true) {
            val variableIndex = trimmed.indexOf(VARIABLE_START_MARK, currentIndex)

            if (variableIndex == -1) {
                break
            } else {
                tokens.add(Token.Text(trimmed.substring(currentIndex, variableIndex)))
                currentIndex = variableIndex
                val endVariableIndex = trimmed.indexOf(VARIABLE_END_MARK, currentIndex)

                if (endVariableIndex > -1) {
                    val nextCurrentIndex = endVariableIndex + VARIABLE_END_MARK.length
                    tokens.add(Token.Variable(
                        trimmed.substring(currentIndex + VARIABLE_START_MARK.length, endVariableIndex),
                        if (nextCurrentIndex + 1 < trimmed.length) trimmed[nextCurrentIndex] else null
                    ))
                    currentIndex = nextCurrentIndex
                }
            }
        }

        if (currentIndex < trimmed.length) {
            tokens.add(Token.Text(trimmed.substring(currentIndex)))
        }

        this.tokens = tokens
    }

    fun scan(text: String) : Result {
        val trimmed = text.trim()
        var index = 0
        val values = mutableMapOf<String, String>()

        for (token in tokens) {
            when(token) {
                is Token.Text -> {
                    if (trimmed.startsWith(token.value, index)) {
                        index += token.value.length
                    } else {
                        break
                    }
                }
                is Token.Variable -> {
                    var i = index

                    while (i < trimmed.length) {
                        val ch = trimmed[i]

                        if ((token.nextCharacter != null && ch == token.nextCharacter) || Character.isWhitespace(ch)) {
                            break
                        }

                        i++
                    }

                    val value = trimmed.substring(index, i)

                    if (values.containsKey(token.name) && values[token.name] != value) {
                        return Result.Fail("Variable ${token.name} with different values: ${values[token.name]} != $value")
                    }

                    values[token.name] = value
                    index = i
                }
            }
        }

        if (index < trimmed.length) {
          return Result.Fail("expected $template but was: $trimmed")
        }

        return Result.Match(values)
    }


    private sealed class Token {

        data class Text(val value: String) : Token()

        data class Variable(val name: String, val nextCharacter: Char?) : Token()
    }

    sealed class Result {
        data class Fail(val message: String) : Result() {
            override operator fun get(key: String): String? {
                throw IllegalArgumentException("$message don't match")
            }

            override val values: Map<String, String>
                get() = mapOf()
        }

        data class Match(override val values: Map<String, String>) : Result() {
            override operator fun get(key: String): String? {
                return values[key]
            }
        }

        abstract operator fun get(key: String): String?

        abstract val values: Map<String, String>
    }
}
