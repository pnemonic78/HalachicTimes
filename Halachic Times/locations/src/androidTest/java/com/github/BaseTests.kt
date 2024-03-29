package com.github

import android.content.Context
import androidx.test.core.app.ApplicationProvider

open class BaseTests {
    protected val context= ApplicationProvider.getApplicationContext<Context>()
}