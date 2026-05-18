package com.github.times

import android.content.Context
import com.github.text.ShadowDateFormat
import com.github.util.applyLocale
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowInstrumentation
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 36], shadows = [ShadowDateFormat::class])
abstract class BaseTests {

    protected val context: Context = ShadowInstrumentation.getInstrumentation().context
        .apply { applyLocale(Locale.US) }

    @Before
    fun before() {
        assertNotNull(context)
    }
}
