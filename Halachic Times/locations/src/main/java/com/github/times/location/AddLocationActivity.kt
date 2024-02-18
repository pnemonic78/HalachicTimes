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
package com.github.times.location

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.text.InputFilter
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.appcompat.app.AppCompatActivity
import com.github.app.SimpleThemeCallbacks
import com.github.app.ThemeCallbacks
import com.github.os.getParcelableCompat
import com.github.preference.ThemePreferences
import com.github.text.method.RangeInputFilter
import com.github.times.location.ZmanimLocation.Companion.toDecimal
import com.github.times.location.databinding.LocationAddBinding
import com.github.times.location.text.LatitudeInputFilter
import com.github.times.location.text.LongitudeInputFilter
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor

/**
 * Add a location by specifying its coordinates.
 *
 * @author Moshe Waisberg
 */
open class AddLocationActivity<P : ThemePreferences> : AppCompatActivity(),
    ThemeCallbacks<P>,
    ZmanimLocationListener {
    private val themeCallbacks: ThemeCallbacks<P> by lazy { createThemeCallbacks(this) }
    private lateinit var coordsFormatSpinner: Spinner
    private lateinit var latitudeSwitcher: ViewSwitcher
    private lateinit var latitudeDegreesEdit: NumberPicker
    private lateinit var latitudeDecimalEdit: EditText
    private lateinit var latitudeMinutesEdit: NumberPicker
    private lateinit var latitudeSecondsEdit: NumberPicker
    private lateinit var latitudeMillisecondsEdit: EditText
    private lateinit var latitudeDirection: Spinner
    private lateinit var longitudeSwitcher: ViewSwitcher
    private lateinit var longitudeDegreesEdit: NumberPicker
    private lateinit var longitudeDecimalEdit: EditText
    private lateinit var longitudeMinutesEdit: NumberPicker
    private lateinit var longitudeSecondsEdit: NumberPicker
    private lateinit var longitudeMillisecondsEdit: EditText
    private lateinit var longitudeDirection: Spinner
    private lateinit var addressView: TextView

    /**
     * Provider for locations.
     */
    private lateinit var locations: LocationsProvider
    private var location: Location = Location(GeocoderBase.USER_PROVIDER)
    private var address: ZmanimAddress? = null
    private var locationForConvert: Location? = null

    /**
     * Formatter for for displaying the current value.
     */
    private val formatter: NumberPicker.Formatter
    private var coordsFormatSpinnerSelectedFirst = false

    override val themePreferences: P
        get() = themeCallbacks.themePreferences

    init {
        val formatter = NumberFormat.getIntegerInstance()
        formatter.isGroupingUsed = false
        this.formatter = NumberPicker.Formatter { value -> formatter.format(value) }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        onPreCreate()
        super.onCreate(savedInstanceState)
        val app = application as LocationApplication<*, *, *>
        locations = app.locations

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val binding = LocationAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView(binding)

        if (savedInstanceState == null) {
            onNewIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val args = intent.extras
        var location: Location = location
        if (args != null) {
            LocationData.from(args, EXTRA_LOCATION)?.let {
                location = it
            }
            if (args.containsKey(EXTRA_LATITUDE)) {
                location.latitude = args.getDouble(EXTRA_LATITUDE)
            }
            if (args.containsKey(EXTRA_LONGITUDE)) {
                location.longitude = args.getDouble(EXTRA_LONGITUDE)
            }
        }
        this.location = location
        setDecimalTexts(
            location.latitude,
            latitudeDegreesEdit,
            latitudeDecimalEdit,
            latitudeDirection
        )
        setDecimalTexts(
            location.longitude,
            longitudeDegreesEdit,
            longitudeDecimalEdit,
            longitudeDirection
        )
    }

    override fun onPreCreate() {
        themeCallbacks.onPreCreate()
    }

    protected open fun createThemeCallbacks(context: Context): ThemeCallbacks<P> {
        return SimpleThemeCallbacks(context)
    }

    private fun initView(binding: LocationAddBinding) {
        coordsFormatSpinner = binding.coordsFormat
        coordsFormatSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (coordsFormatSpinnerSelectedFirst) {
                    coordsFormatSpinnerSelectedFirst = false
                    return
                }
                if (position == FORMAT_DECIMAL) {
                    convertToDecimal()
                } else {
                    convertFromDecimal()
                }
                latitudeSwitcher.displayedChild = position
                longitudeSwitcher.displayedChild = position
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }
        coordsFormatSpinnerSelectedFirst = true
        latitudeSwitcher = binding.latitude.latitudeSwitch
        latitudeDegreesEdit = binding.latitude.latitudeDegreesEdit
        latitudeDegreesEdit.minValue = DEGREES_MIN
        latitudeDegreesEdit.maxValue = LatitudeInputFilter.LATITUDE_MAX.toInt()
        latitudeDecimalEdit = binding.latitude.latitudeDecimalEdit
        latitudeDecimalEdit.filters = arrayOf<InputFilter>(
            RangeInputFilter(DECIMAL_MIN, DECIMAL_MAX)
        )
        latitudeMinutesEdit = binding.latitude.latitudeMinutesEdit
        latitudeMinutesEdit.minValue = MINUTES_MIN
        latitudeMinutesEdit.maxValue = MINUTES_MAX
        latitudeSecondsEdit = binding.latitude.latitudeSecondsEdit
        latitudeSecondsEdit.minValue = SECONDS_MIN
        latitudeSecondsEdit.maxValue = SECONDS_MAX
        latitudeMillisecondsEdit = binding.latitude.latitudeMillisecondsEdit
        latitudeMillisecondsEdit.filters = arrayOf<InputFilter>(
            RangeInputFilter(MILLISECONDS_MIN, MILLISECONDS_MAX)
        )
        latitudeDirection = binding.latitude.latitudeDirection
        longitudeSwitcher = binding.longitude.longitudeSwitch
        longitudeDegreesEdit = binding.longitude.longitudeDegreesEdit
        longitudeDegreesEdit.minValue = DEGREES_MIN
        longitudeDegreesEdit.maxValue = LongitudeInputFilter.LONGITUDE_MAX.toInt()
        longitudeDecimalEdit = binding.longitude.longitudeDecimalEdit
        longitudeDecimalEdit.filters = arrayOf<InputFilter>(
            RangeInputFilter(DECIMAL_MIN, DECIMAL_MAX)
        )
        longitudeMinutesEdit = binding.longitude.longitudeMinutesEdit
        longitudeMinutesEdit.minValue = MINUTES_MIN
        longitudeMinutesEdit.maxValue = MINUTES_MAX
        longitudeSecondsEdit = binding.longitude.longitudeSecondsEdit
        longitudeSecondsEdit.minValue = SECONDS_MIN
        longitudeSecondsEdit.maxValue = SECONDS_MAX
        longitudeMillisecondsEdit = binding.longitude.longitudeMillisecondsEdit
        longitudeMillisecondsEdit.filters = arrayOf<InputFilter>(
            RangeInputFilter(MILLISECONDS_MIN, MILLISECONDS_MAX)
        )
        longitudeDirection = binding.longitude.longitudeDirection
        addressView = binding.address
        updateNumberPickers()
    }

    override fun onStart() {
        super.onStart()
        locations.start(this)
    }

    override fun onStop() {
        super.onStop()
        locations.stop(this)
    }

    override fun onResume() {
        super.onResume()
        val location = location
        val address = address
        if (address != null) {
            onAddressChanged(location, address)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        menuInflater.inflate(R.menu.add_location, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            // Cannot use 'switch' here because library ids are not final.
            R.id.menu_location_cancel -> {
                setResult(RESULT_CANCELED)
                finish()
                true
            }

            R.id.menu_location_add -> {
                if (saveLocation(location, coordsFormatSpinner.selectedItemPosition)) {
                    Intent().apply {
                        put(EXTRA_LOCATION, location)
                        setResult(RESULT_OK, this)
                    }
                    finish()
                }
                true
            }

            R.id.menu_location_show -> {
                if (saveLocation(location, coordsFormatSpinner.selectedItemPosition)) {
                    fetchAddress(location)
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveLocation(location: Location, coordsFormat: Int): Boolean {
        var latitude: Double
        var longitude: Double
        val latitudeDegrees = latitudeDegreesEdit.value
        val longitudeDegrees = longitudeDegreesEdit.value
        if (coordsFormat == FORMAT_DECIMAL) {
            val latitudeString: CharSequence = latitudeDecimalEdit.text
            latitude = latitudeDegrees + "0.$latitudeString".toDouble()
            val longitudeString: CharSequence = longitudeDecimalEdit.text
            longitude = longitudeDegrees + "0.$longitudeString".toDouble()
        } else {
            val latitudeMinutes = latitudeMinutesEdit.value
            val latitudeSeconds = latitudeSecondsEdit.value
            val latitudeMillisecondsString: CharSequence = latitudeMillisecondsEdit.text
            val latitudeMilliseconds = if (latitudeMillisecondsString.isEmpty()) 0
            else latitudeMillisecondsString.toString().toInt(10)
            val longitudeMinutes = longitudeMinutesEdit.value
            val longitudeSeconds = longitudeSecondsEdit.value
            val longitudeMillisecondsString: CharSequence = longitudeMillisecondsEdit.text
            val longitudeMilliseconds = if (longitudeMillisecondsString.isEmpty()) 0
            else longitudeMillisecondsString.toString().toInt(10)
            latitude = toDecimal(
                latitudeDegrees,
                latitudeMinutes,
                "$latitudeSeconds.$latitudeMilliseconds".toDouble()
            )
            longitude = toDecimal(
                longitudeDegrees,
                longitudeMinutes,
                "$longitudeSeconds.$longitudeMilliseconds".toDouble()
            )
        }
        latitude = abs(latitude) *
            if (latitudeDirection.selectedItemPosition == POSITION_POSITIVE) DIRECTION_NORTH
            else DIRECTION_SOUTH
        if (latitude < ZmanimLocation.LATITUDE_MIN || latitude > ZmanimLocation.LATITUDE_MAX) {
            return false
        }
        longitude = abs(longitude) *
            if (longitudeDirection.selectedItemPosition == POSITION_POSITIVE) DIRECTION_EAST
            else DIRECTION_WEST
        if (longitude < ZmanimLocation.LONGITUDE_MIN || longitude > ZmanimLocation.LONGITUDE_MAX) {
            return false
        }
        location.latitude = latitude
        location.longitude = longitude
        return true
    }

    private fun fetchAddress(location: Location) {
        addressView.setText(R.string.location_unknown)
        val locations = locations
        locations.findAddress(location, false)
    }

    override fun onAddressChanged(location: Location, address: ZmanimAddress) {
        val locationOld = this.location
        if (location.latitude != locationOld.latitude || location.longitude != locationOld.longitude) {
            return
        }
        this.address = address
        runOnUiThread {
            addressView.text = address.formatted
        }
    }

    override fun onElevationChanged(location: Location) = Unit

    override fun onLocationChanged(location: Location) = Unit

    @Deprecated("Deprecated in Java", ReplaceWith("Unit"))
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) = Unit

    override fun onProviderEnabled(provider: String) = Unit

    override fun onProviderDisabled(provider: String) = Unit

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(SAVE_STATE_LOCATION, location)
        outState.putParcelable(SAVE_STATE_ADDRESS, address)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        location = savedInstanceState.getParcelableCompat(SAVE_STATE_LOCATION, Location::class.java)
            ?: location
        address =
            savedInstanceState.getParcelableCompat(SAVE_STATE_ADDRESS, ZmanimAddress::class.java)
    }

    private fun convertFromDecimal() {
        var locationForConvert = locationForConvert
        if (locationForConvert == null) {
            locationForConvert = Location(location)
            this.locationForConvert = locationForConvert
        }
        if (saveLocation(locationForConvert, FORMAT_DECIMAL)) {
            setSexagesimalTexts(
                locationForConvert.latitude,
                latitudeDegreesEdit,
                latitudeMinutesEdit,
                latitudeSecondsEdit,
                latitudeMillisecondsEdit,
                latitudeDirection
            )
            setSexagesimalTexts(
                locationForConvert.longitude,
                longitudeDegreesEdit,
                longitudeMinutesEdit,
                longitudeSecondsEdit,
                longitudeMillisecondsEdit,
                longitudeDirection
            )
        }
    }

    private fun setSexagesimalTexts(
        coordinate: Double,
        degreesView: NumberPicker,
        minutesView: NumberPicker,
        secondsView: NumberPicker,
        millisecondsView: TextView,
        directionView: Spinner
    ) {
        var coordinate = coordinate
        val direction = if (coordinate >= 0) POSITION_POSITIVE else POSITION_NEGATIVE
        coordinate = abs(coordinate)
        val degrees = floor(coordinate).toInt()
        coordinate -= degrees.toDouble()
        coordinate *= 60.0
        val minutes = floor(coordinate).toInt()
        coordinate -= minutes.toDouble()
        coordinate *= 60.0
        val seconds = floor(coordinate).toInt()
        coordinate -= seconds.toDouble()
        coordinate *= 10000.0 /* 0 - 9999 */
        val milliseconds = floor(coordinate).toInt()
        degreesView.value = degrees
        minutesView.value = minutes
        secondsView.value = seconds
        millisecondsView.text = formatNumber(milliseconds)
        directionView.setSelection(direction)
    }

    private fun convertToDecimal() {
        var locationForConvert = locationForConvert
        if (locationForConvert == null) {
            locationForConvert = Location(location)
            this.locationForConvert = locationForConvert
        }
        if (saveLocation(locationForConvert, FORMAT_SEXAGESIMAL)) {
            setDecimalTexts(
                locationForConvert.latitude,
                latitudeDegreesEdit,
                latitudeDecimalEdit,
                latitudeDirection
            )
            setDecimalTexts(
                locationForConvert.longitude,
                longitudeDegreesEdit,
                longitudeDecimalEdit,
                longitudeDirection
            )
        }
    }

    private fun setDecimalTexts(
        coordinate: Double,
        degreesView: NumberPicker,
        decimalView: EditText,
        directionView: Spinner
    ) {
        var coordinate = coordinate
        val direction = if (coordinate >= 0) POSITION_POSITIVE else POSITION_NEGATIVE
        coordinate = abs(coordinate)
        val degrees = floor(coordinate).toInt()
        coordinate -= degrees.toDouble()
        coordinate *= 1000000.0 /* 0 - 999999 */
        val milliseconds = floor(coordinate).toInt()
        degreesView.value = degrees
        decimalView.setText(formatNumber(milliseconds))
        directionView.setSelection(direction)
    }

    private fun formatNumber(value: Int): String {
        return formatter.format(value) ?: formatNumberWithLocale(value)
    }

    private fun updateNumberPickers() {
        latitudeDegreesEdit.setFormatter(formatter)
        latitudeMinutesEdit.setFormatter(formatter)
        latitudeSecondsEdit.setFormatter(formatter)
        longitudeDegreesEdit.setFormatter(formatter)
        longitudeMinutesEdit.setFormatter(formatter)
        longitudeSecondsEdit.setFormatter(formatter)
    }

    companion object {
        /**
         * The location parameter.
         */
        const val EXTRA_LOCATION = ZmanimLocationListener.EXTRA_LOCATION

        /**
         * The location's latitude parameter.
         */
        const val EXTRA_LATITUDE = "latitude"

        /**
         * The location's longitude parameter.
         */
        const val EXTRA_LONGITUDE = "longitude"

        /**
         * The location state.
         */
        private const val SAVE_STATE_LOCATION = EXTRA_LOCATION

        /**
         * The address state.
         */
        private const val SAVE_STATE_ADDRESS = "address"
        private const val FORMAT_DECIMAL = 0
        private const val FORMAT_SEXAGESIMAL = 1
        private const val POSITION_POSITIVE = 0
        private const val POSITION_NEGATIVE = 1
        private const val DIRECTION_NORTH = +1
        private const val DIRECTION_SOUTH = -1
        private const val DIRECTION_EAST = +1
        private const val DIRECTION_WEST = -1
        private const val DEGREES_MIN = 0
        private const val DECIMAL_MIN = 0
        private const val DECIMAL_MAX = 999999
        private const val MINUTES_MIN = 0
        private const val MINUTES_MAX = 59
        private const val SECONDS_MIN = 0
        private const val SECONDS_MAX = 59
        private const val MILLISECONDS_MIN = 0
        private const val MILLISECONDS_MAX = 9999

        private fun formatNumberWithLocale(value: Int): String {
            return String.format(Locale.getDefault(), "%d", value)
        }
    }
}