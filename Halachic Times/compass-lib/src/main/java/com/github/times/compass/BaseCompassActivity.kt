/*
 * Copyright 2012, Moshe Waisberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.times.compass

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import androidx.core.view.isVisible
import com.github.app.SimpleThemeCallbacks
import com.github.app.ThemeCallbacks
import com.github.preference.ThemePreferences
import com.github.times.compass.lib.R
import com.github.times.compass.preference.CompassPreferences
import com.github.times.compass.preference.SimpleCompassPreferences
import com.github.times.compass.preference.ThemeCompassPreferences
import com.github.times.location.LocatedActivity

/**
 * Show the direction in which to pray. Points to the Holy of Holies in
 * Jerusalem in Israel.
 *
 * @author Moshe Waisberg
 */
abstract class BaseCompassActivity : LocatedActivity<ThemePreferences>() {

    /** The main fragment.  */
    private var fragment: CompassFragment? = null

    /** The preferences.  */
    private lateinit var preferences: ThemeCompassPreferences

    val compassPreferences: CompassPreferences
        get() = preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        val context: Context = this
        preferences = createCompassPreferences(context)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.compass_activity)
        headerLocation = findViewById(com.github.times.location.R.id.coordinates)
        headerAddress = findViewById(com.github.times.location.R.id.address)
        fragment = supportFragmentManager.findFragmentById(R.id.compass) as? CompassFragment
        getCompassFragment()?.let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.compass, it)
                .commitAllowingStateLoss()
            fragment = it
        }

        val summary = findViewById<TextView>(R.id.summary)
        if (summary != null) {
            val a =
                context.obtainStyledAttributes(preferences.compassTheme, R.styleable.CompassView)
            summary.setTextColor(a.getColorStateList(R.styleable.CompassView_compassColorTarget))
            a.recycle()
        }
    }

    override fun onResume() {
        super.onResume()
        val summary = findViewById<TextView>(R.id.summary)
        summary?.isVisible = preferences.isSummariesVisible
    }

    override fun createUpdateLocationRunnable(location: Location): Runnable {
        return Runnable {
            bindHeader(location)
            val c = fragment ?: return@Runnable
            c.setLocation(location)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.compass, menu)
        return true
    }

    protected open fun createCompassPreferences(context: Context?): ThemeCompassPreferences {
        return SimpleCompassPreferences(context)
    }

    override fun createThemeCallbacks(context: Context): ThemeCallbacks<ThemePreferences?> {
        return SimpleThemeCallbacks(context, preferences)
    }

    open protected fun getCompassFragment(): CompassFragment? = null
}