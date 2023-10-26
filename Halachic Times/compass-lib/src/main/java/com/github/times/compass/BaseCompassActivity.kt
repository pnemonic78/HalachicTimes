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
import androidx.core.view.isVisible
import com.github.app.SimpleThemeCallbacks
import com.github.app.ThemeCallbacks
import com.github.preference.ThemePreferences
import com.github.times.compass.lib.R
import com.github.times.compass.lib.databinding.CompassActivityBinding
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
abstract class BaseCompassActivity<V : CompassView> : LocatedActivity<ThemePreferences>() {

    /** The main fragment.  */
    private var fragment: CompassFragment<V>? = null

    /** The preferences.  */
    private lateinit var preferences: ThemeCompassPreferences

    private lateinit var binding: CompassActivityBinding

    val compassPreferences: CompassPreferences
        get() = preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        val context: Context = this
        preferences = createCompassPreferences(context)

        super.onCreate(savedInstanceState)

        val binding = CompassActivityBinding.inflate(layoutInflater)
        this.binding = binding
        setContentView(binding.root)

        headerLocation = binding.header.location.coordinates
        headerAddress = binding.header.location.address
        fragment = supportFragmentManager.findFragmentById(R.id.compass) as? CompassFragment<V>
        getCompassFragment().let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.compass, it)
                .commitAllowingStateLoss()
            fragment = it
        }

        val summary = binding.header.summary
        val a = context.obtainStyledAttributes(preferences.compassTheme, R.styleable.CompassView)
        summary.setTextColor(a.getColorStateList(R.styleable.CompassView_compassColorTarget))
        a.recycle()
    }

    override fun onResume() {
        super.onResume()
        val summary = binding.header.summary
        summary.isVisible = preferences.isSummariesVisible
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

    override fun createThemeCallbacks(context: Context): ThemeCallbacks<ThemePreferences> {
        return SimpleThemeCallbacks(context, preferences)
    }

    protected abstract fun getCompassFragment(): CompassFragment<V>
}