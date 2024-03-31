package com.github.util

import com.github.geonames.util.LocaleUtils
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageTests {
    @Test
    fun java_hebrew() {
        val l1 = Locale("he")
        assertEquals("he", l1.language)

        LocaleUtils.applyAndroid()
        val l2 = Locale("he")
        assertEquals("he", l2.language)
    }
}