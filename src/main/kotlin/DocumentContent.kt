package dev.shellbook

import java.net.URL
import kotlin.io.path.Path
import kotlin.io.path.readText

sealed class DocumentContent {

    companion object {

        fun of(text: String) : DocumentContent {
            val ref: String
            val fragment: String?

            if (text.contains("#")) {
                ref = text.substringBefore("#")
                fragment = text.substringAfter("#")
            } else {
                ref = text
                fragment = null
            }

            return try {
                val content = when {
                    ref.startsWith("http://") || ref.startsWith("https://") -> URL(ref).openStream().reader().readText()
                    else -> Path(ref).readText()
                }

                Valid(text, fragment, content)
            } catch (exception: Exception) {
                Invalid(text, exception.message ?: "$text invalid")
            }
        }
    }

    abstract val valid: Boolean

    data class Valid(
        val value: String,
        val fragment: String?,
        val content: String
    ) : DocumentContent() {

        override val valid: Boolean
            get() = true
    }


    data class Invalid(
        val value: String,
        val message: String
    ) : DocumentContent() {

        override val valid: Boolean
            get() = false
    }
}