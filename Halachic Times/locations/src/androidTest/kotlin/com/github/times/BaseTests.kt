package com.github.times

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.github.util.applyLocale
import java.util.Locale
import org.junit.Assert.assertNotNull
import org.junit.Before

abstract class BaseTests {

    protected val context: Context = ApplicationProvider.getApplicationContext<Context?>().applicationContext
        .apply { applyLocale(Locale.US) }

    @Before
    fun before() {
        assertNotNull(context)
    }
}
