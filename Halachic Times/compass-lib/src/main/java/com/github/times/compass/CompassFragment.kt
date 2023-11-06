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

import android.app.Activity
import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.Display
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.math.toDegrees
import com.github.times.compass.lib.databinding.CompassFragmentBinding
import com.github.times.compass.preference.CompassPreferences
import com.github.times.compass.preference.SimpleCompassPreferences

/**
 * Show a compass.
 *
 * @author Moshe Waisberg
 */
open class CompassFragment : Fragment(), SensorEventListener {
    /**
     * The sensor manager.
     */
    private var sensorManager: SensorManager? = null

    /**
     * The accelerometer sensor.
     */
    private var accelerometer: Sensor? = null

    /**
     * The magnetic field sensor.
     */
    private var magnetometer: Sensor? = null

    /**
     * The main view.
     */
    private var _binding: CompassFragmentBinding? = null

    /**
     * The accelerometer values.
     */
    private val accelerometerValues = FloatArray(3)

    /**
     * The magnetometer field.
     */
    private val magnetometerValues = FloatArray(3)

    /**
     * Rotation matrix.
     */
    private val matrixR = FloatArray(9)

    /**
     * Remapped rotation matrix.
     */
    private val mapR = FloatArray(9)

    /**
     * Orientation matrix.
     */
    private val orientation = FloatArray(3)

    /**
     * The preferences.
     */
    protected lateinit var preferences: CompassPreferences

    /**
     * The location's geomagnetic field.
     */
    private var geomagneticField: GeomagneticField? = null

    /**
     * The display orientation.
     */
    private var displayRotation = Surface.ROTATION_0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = CompassFragmentBinding.inflate(inflater, container, false)
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateRotation(activity, view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = requireContext()
        preferences = getPreferences(context)
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        this.sensorManager = sensorManager
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        }
    }

    private fun getPreferences(context: Context): CompassPreferences {
        return if (context is BaseCompassActivity) {
            context.compassPreferences
        } else {
            SimpleCompassPreferences(context)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        val sensorManager = this.sensorManager
        if (sensorManager != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        updateRotation(activity, view)
    }

    override fun onStart() {
        super.onStart()
        updateRotation(activity, view)
    }

    private fun updateRotation(activity: Activity?, view: View?) {
        var display: Display? = null
        if (view != null) {
            display = view.display
        }
        if (display == null && activity != null) {
            display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.display
            } else {
                @Suppress("DEPRECATION")
                activity.windowManager.defaultDisplay
            }
        }
        if (display != null) {
            displayRotation = display.rotation
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> lowPass(event.values, accelerometerValues)
            Sensor.TYPE_MAGNETIC_FIELD -> lowPass(event.values, magnetometerValues)
            else -> return
        }
        if (SensorManager.getRotationMatrix(
                matrixR,
                null,
                accelerometerValues,
                magnetometerValues
            )
        ) {
            when (displayRotation) {
                Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
                    matrixR,
                    SensorManager.AXIS_Y,
                    SensorManager.AXIS_MINUS_X,
                    mapR
                )

                Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
                    matrixR,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_MINUS_Y,
                    mapR
                )

                Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
                    matrixR,
                    SensorManager.AXIS_MINUS_Y,
                    SensorManager.AXIS_MINUS_X,
                    mapR
                )

                else -> SensorManager.remapCoordinateSystem(
                    matrixR,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Y,
                    mapR
                )
            }
            SensorManager.getOrientation(mapR, orientation)
            var azimuth = orientation[0].toDegrees()
            val geomagneticField = this.geomagneticField
            if (geomagneticField != null) {
                // converts magnetic north to true north
                azimuth += geomagneticField.declination
            }
            setAzimuth(azimuth)
        }
    }

    protected open fun setAzimuth(azimuth: Float) {
        val binding = _binding ?: return
        binding.compass.setAzimuth(azimuth)
    }

    /**
     * Set the current location.
     *
     * @param location the location.
     */
    open fun setLocation(location: Location) {
        geomagneticField = GeomagneticField(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            location.altitude.toFloat(),
            location.time
        )
    }

    private fun lowPass(input: FloatArray, output: FloatArray?): FloatArray {
        if (output == null) {
            return input
        }
        val length = input.size.coerceAtMost(output.size)
        for (i in 0 until length) {
            output[i] += ALPHA * (input[i] - output[i])
        }
        return output
    }

    companion object {
        private const val ALPHA = 0.35f // if ALPHA = 1 OR 0, no filter applies.
    }
}