package parser

import dev.shellbook.parser.Meta
import kotlin.test.Test
import kotlin.test.assertEquals

class MetaTest {

    @Test
    fun parseIdClassesAndLabel() {
        assertEquals(Meta("id", setOf("class1", "class2"), "hola"), Meta.parse("""#id.class1.class2 "hola""""))
    }

    @Test
    fun parseLabelWithFileCharacters() {
        assertEquals(Meta(label="./folder/file_1.text"), Meta.parse(""""./folder/file_1.text""""))
    }
}