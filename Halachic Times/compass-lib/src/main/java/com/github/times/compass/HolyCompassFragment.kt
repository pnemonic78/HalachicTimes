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

import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.FloatRange
import com.github.os.VibratorCompat
import com.github.times.compass.lib.databinding.HolyCompassFragmentBinding
import com.github.times.compass.preference.CompassPreferences.Values.BEARING_GREAT_CIRCLE
import com.github.times.location.GeocoderBase
import com.github.times.location.ZmanimLocation
import kotlin.math.abs

/**
 * Show the direction in which to pray.
 * Points to a holy place.
 *
 * @author Moshe Waisberg
 */
open class HolyCompassFragment : CompassFragment() {
    /**
     * Location of the holy place.
     */
    private val holiest = Location(GeocoderBase.USER_PROVIDER)
    private var bearing = 0f
    private var vibrator: VibratorCompat? = null
    private var vibrationTime = 0L

    private var _binding: HolyCompassFragmentBinding? = null

    init {
        setHoliest(Double.NaN, Double.NaN, Double.NaN)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = HolyCompassFragmentBinding.inflate(inflater, container, false)
        _binding = binding
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun setAzimuth(azimuth: Float) {
        val binding = _binding ?: return
        binding.compass.setAzimuth(azimuth)
        maybeVibrate(azimuth)
    }

    /**
     * Set the current location.
     *
     * @param location the location.
     */
    override fun setLocation(location: Location) {
        super.setLocation(location)
        val bearingType = preferences.bearing
        bearing = if (BEARING_GREAT_CIRCLE == bearingType) {
            location.bearingTo(holiest)
        } else {
            ZmanimLocation.angleTo(location, holiest)
        }
        _binding?.compass?.setHoliest(bearing)
    }

    fun setHoliest(
        @FloatRange(from = -90.0, to = 90.0) latitude: Double,
        @FloatRange(from = -180.0, to = 180.0) longitude: Double,
        elevation: Double
    ) {
        holiest.latitude = latitude
        holiest.longitude = longitude
        holiest.altitude = elevation
    }

    private fun maybeVibrate(azimuth: Float) {
        val now = SystemClock.elapsedRealtime()
        if (abs(azimuth - bearing) < EPSILON_BEARING) {
            if (now - vibrationTime >= VIBRATE_DELAY_MS) {
                vibrate()
                // Disable the vibration until accurate again.
                vibrationTime = Long.MAX_VALUE
            }
        } else {
            vibrationTime = now
        }
    }

    private fun vibrate() {
        var vibrator = vibrator
        if (vibrator == null) {
            val context = this.context ?: return
            vibrator = VibratorCompat(context)
            this.vibrator = vibrator
        }
        vibrator.vibrate(VIBRATE_LENGTH_MS, VibratorCompat.USAGE_TOUCH)
    }

    companion object {
        /**
         * Accuracy of the device's bearing relative to the holiest bearing, in degrees.
         */
        private const val EPSILON_BEARING = 2f

        /** Duration to consider the bearing match accurate and stable.  */
        private const val VIBRATE_DELAY_MS = DateUtils.SECOND_IN_MILLIS

        /** Duration of a vibration.  */
        private const val VIBRATE_LENGTH_MS: Long = 50
    }
}