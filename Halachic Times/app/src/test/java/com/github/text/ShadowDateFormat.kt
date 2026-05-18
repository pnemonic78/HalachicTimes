package com.github.text

import android.text.format.DateFormat
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import java.util.Locale

@Implements(DateFormat::class)
class ShadowDateFormat {
    companion object {
        @Implementation
        @JvmStatic
        fun getBestDateTimePattern(locale: Locale, skeleton: String): String {
            return when (skeleton) {
                "Hm" -> "HH:mm"
                "hm" -> "hh:mm a"
                "Hms" -> "HH:mm:ss"
                "hms" -> "hh:mm:ss a"
                else -> skeleton
            }
        }
    }
}