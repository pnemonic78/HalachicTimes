package com.github.times

import android.content.Context
import com.github.util.applyLocale
import java.io.IOException
import java.io.InputStream
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

    /**
     * Helper method for getting an input stream for a resource file
     *
     * @param filename The resource file name.
     * @return An input stream.
     */
    @Throws(IOException::class)
    protected fun openRawResource(filename: String): InputStream {
        return javaClass.classLoader.getResourceAsStream(filename).buffered()
    }
}
