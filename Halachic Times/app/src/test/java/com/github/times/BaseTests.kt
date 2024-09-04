package com.github.times

import android.content.Context
import com.github.util.applyLocale
import java.util.Locale
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowInstrumentation

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
abstract class BaseTests {

    protected val context: Context = ShadowInstrumentation.getInstrumentation().context
        .apply { applyLocale(Locale.US) }

    @Before
    fun before() {
        assertNotNull(context)
    }
}
