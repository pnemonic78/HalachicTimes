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
package com.github.compass

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.github.times.location.LocationApplication
import com.github.times.location.LocationFormatter

/**
 * Show the compass.
 *
 * @author Moshe Waisberg
 */
class CompassFragment : com.github.times.compass.CompassFragment() {

    private var bearingView: TextView? = null
    private lateinit var formatter: LocationFormatter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = requireActivity().application as LocationApplication<*, *, *>
        formatter = app.locations
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = view.context
        val a = context.obtainStyledAttributes(
            preferences.compassTheme, com.github.times.compass.lib.R.styleable.CompassView
        )
        val bearingColor = a.getColorStateList(com.github.times.compass.lib.R.styleable.CompassView_compassColorLabel2)
        a.recycle()

        bearingView = view.findViewById(R.id.bearing)
        bearingView!!.setTextColor(bearingColor)
    }

    override fun setAzimuth(azimuth: Float) {
        super.setAzimuth(azimuth)
        bearingView?.text = formatter.formatBearing(azimuth.toDouble())
    }
}