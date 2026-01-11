package com.tradingapp.metatrader

import com.tradingapp.metatrader.app.core.expert.dsl.ExpertDslParser
import com.tradingapp.metatrader.app.core.expert.dsl.ExpertDslParseException
import org.junit.Assert.*
import org.junit.Test

class ParserTest {

    @Test
    fun parse_ok() {
        val txt = """
            name: Demo
            input lot=0.10
            rule BUY when ema(20) crosses_above ema(50)
        """.trimIndent()

        val m = ExpertDslParser().parse(txt)
        assertEquals("Demo", m.name)
        assertEquals(1, m.rules.size)
        assertTrue(m.inputs.containsKey("lot"))
    }

    @Test
    fun parse_error_line() {
        val txt = """
            name: X
            rule BUY when ema(xx) crosses_above ema(50)
        """.trimIndent()

        try {
            ExpertDslParser().parse(txt)
            fail("Expected exception")
        } catch (e: ExpertDslParseException) {
            assertEquals(2, e.lineNumber)
        }
    }
}
