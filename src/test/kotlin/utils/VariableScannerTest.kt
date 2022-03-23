package utils

import dev.shellbook.utils.VariableScanner
import dev.shellbook.utils.VariableScanner.Result.Match
import junit.framework.AssertionFailedError
import org.junit.Assert.assertEquals
import kotlin.test.Test

class VariableScannerTest {

    @Test
    fun scanOneLineAndExtractsValues() {
        val matcher = VariableScanner("trivy config --timeout 300s --format json [file]")
        assertEquals(
            Match(mapOf("file" to "tmp/file")),
            matcher.scan("trivy config --timeout 300s --format json tmp/file")
        )
    }

    @Test
    fun scanSeveralLinesAndExtractsValues() {
        val matcher = VariableScanner("""
            - Hello [name], how are you?
            - I'm fine, [otherName]
            """)
        assertEquals(
            Match(mapOf("name" to "Peter", "otherName" to "Mary")),
            matcher.scan("""
            - Hello Peter, how are you?
            - I'm fine, Mary
            """)
        )
    }

    @Test
    fun scanSeveralLinesButFailsBecauseTheVariableHasDifferentValues() {
        val matcher = VariableScanner("""
            - Hello [name], how are you?
            - I'm fine, [name]
            """)
        try {
            matcher.scan("""
              - Hello Peter, how are you?
              - I'm fine, Mary
              """)
        } catch (error: AssertionFailedError) {
            assertEquals("Variable name with different values: Peter != Mary", error.message)
        }
    }

    @Test
    fun noMatch() {
        val matcher = VariableScanner("""
            - Hello [name], how are you?
            - I'm fine, [name]
            """)
        try {
            matcher.scan("""
                - Hello Peter, how are you?
                - I'm fine,
                """)
        } catch (error: AssertionFailedError) {
            assertEquals("""
                  expected: <- Hello [name], how are you?
                  - I'm fine, [name]
                  > but was: <- Hello Peter, how are you?
                  - I'm fine,
                  >""",
                error.message
            )
        }
    }
}