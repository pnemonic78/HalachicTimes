package com.github

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.github.util.applyLocale
import java.util.Locale

open class BaseTests {
    protected val context: Context = ApplicationProvider.getApplicationContext<Context>().apply {
        applyLocale(Locale.US)
    }
}