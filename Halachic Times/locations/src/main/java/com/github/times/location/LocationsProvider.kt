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

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Address
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
import androidx.core.content.PermissionChecker
import com.github.lang.isTrue
import com.github.times.location.AddressWorker.Companion.enqueueAddress
import com.github.times.location.AddressWorker.Companion.enqueueElevation
import com.github.times.location.ZmanimLocation.Companion.compare
import com.github.times.location.ZmanimLocation.Companion.compareAll
import com.github.times.location.ZmanimLocation.Companion.distanceBetween
import com.github.times.location.ZmanimLocationListener.Companion.ACTION_ADDRESS
import com.github.times.location.ZmanimLocationListener.Companion.ACTION_ELEVATION
import com.github.times.location.ZmanimLocationListener.Companion.ACTION_LOCATION_CHANGED
import com.github.times.location.ZmanimLocationListener.Companion.EXTRA_ADDRESS
import com.github.times.location.ZmanimLocationListener.Companion.EXTRA_LOCATION
import com.github.times.location.country.CountriesGeocoder
import java.util.TimeZone
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.min
import timber.log.Timber

/**
 * Locations provider.
 *
 * @author Moshe Waisberg
 */
@SuppressLint("UnspecifiedRegisterReceiverFlag", "WrongConstant")
open class LocationsProvider(private val context: Context) : ZmanimLocationListener,
    LocationFormatter {

    /**
     * The owner location listeners.
     */
    private val listeners: MutableCollection<ZmanimLocationListener> = CopyOnWriteArrayList()
    private val listener: LocationListener = this

    /**
     * Service provider for locations.
     */
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    /**
     * The location.
     */
    private var locationLocal: Location? = null

    /**
     * The preferences.
     */
    val preferences: LocationPreferences = SimpleLocationPreferences(context)

    /**
     * The list of countries.
     */
    private val countriesGeocoder: CountriesGeocoder =
        CountriesGeocoder(context)

    /**
     * The time zone.
     */
    var timeZone: TimeZone = TimeZone.getDefault()
        private set

    /**
     * The handler thread.
     */
    private val handlerThread: HandlerThread by lazy { HandlerThread(TAG).apply { start() } }

    /**
     * The handler.
     */
    private val handler: Handler by lazy { UpdatesHandler(handlerThread.looper) }

    /**
     * The next time to start update locations.
     */
    private var startTaskDelay = UPDATE_INTERVAL_START

    /**
     * The location that was externally set.
     */
    private var locationManual: Location? = null

    /**
     * The location formatter.
     */
    private val formatterHelper: LocationFormatter by lazy { createLocationFormatter(context) }

    /**
     * Register a location listener to receive location notifications.
     *
     * @param listener the listener.
     */
    private fun addLocationListener(listener: ZmanimLocationListener) {
        if (!listeners.contains(listener) && listener !== this) {
            listeners.add(listener)
        }
    }

    /**
     * Unregister a location listener to stop receiving location notifications.
     *
     * @param listener the listener.
     */
    private fun removeLocationListener(listener: ZmanimLocationListener) {
        listeners.remove(listener)
    }

    override fun onLocationChanged(location: Location) {
        Timber.v("onLocationChanged %s", location)
        onLocationChanged(location, findAddress = true, findElevation = true)
    }

    private fun onLocationChanged(
        location: Location,
        findAddress: Boolean,
        findElevation: Boolean
    ) {
        if (!isValid(location)) {
            return
        }
        val locationOld = this.locationLocal
        var locationNew = location
        if (compareAll(locationNew, locationOld) == 0) {
            return
        }
        // Ignore non-user locations after user selected from locations list.
        if (locationOld != null && GeocoderBase.USER_PROVIDER == locationOld.provider) {
            if (GeocoderBase.USER_PROVIDER != locationNew.provider) {
                return
            }
        }

        var keepLocation = true
        if (locationOld != null && compare(locationOld, locationNew) != 0) {
            // Ignore old locations.
            if (locationOld.time + LOCATION_EXPIRATION > locationNew.time) {
                keepLocation = false
            }
            // Does the new location have an elevation?
            if (locationNew.hasAltitude() && !locationOld.hasAltitude()) {
                val distance = distanceBetween(locationOld, location)
                if (distance <= GeocoderBase.SAME_CITY) {
                    locationOld.altitude = locationNew.altitude
                }
            }
            // Ignore manual locations.
            locationNew = locationManual ?: locationNew
        }
        if (keepLocation) {
            this.locationLocal = locationNew
            this.locationSaved = locationNew
        }
        notifyLocationChanged(locationNew)
        if (findAddress) {
            findAddress(locationNew)
        }
        if (findElevation) {
            findElevation(locationNew)
        }
    }

    private fun notifyLocationChanged(location: Location) {
        for (listener in listeners) {
            listener.onLocationChanged(location)
        }
        broadcastLocationChanged(location)
    }

    private fun broadcastLocationChanged(location: Location) {
        val intent = Intent(ACTION_LOCATION_CHANGED)
            .setPackage(context.packageName)
            .putExtra(EXTRA_LOCATION, location)
        context.sendBroadcast(intent)
    }

    private fun handleLocationChanged(location: Location?) {
        handler.obtainMessage(WHAT_CHANGED, location).sendToTarget()
    }

    override fun onProviderDisabled(provider: String) {
        for (listener in listeners) {
            listener.onProviderDisabled(provider)
        }
    }

    override fun onProviderEnabled(provider: String) {
        for (listener in listeners) {
            listener.onProviderEnabled(provider)
        }
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        for (listener in listeners) {
            listener.onStatusChanged(provider, status, extras)
        }
    }

    override fun onAddressChanged(location: Location, address: ZmanimAddress) {
        if (!isValid(location) || !isValid(address)) {
            return
        }
        for (listener in listeners) {
            listener.onAddressChanged(location, address)
        }
    }

    override fun onElevationChanged(location: Location) {
        onLocationChanged(location, findAddress = true, findElevation = false)
    }

    /**
     * Get a fused location from several providers.
     *
     * @return the location - `null` otherwise.
     */
    @get:RequiresApi(api = Build.VERSION_CODES.S)
    val locationFused: Location?
        get() {
            val locationManager = locationManager
            if (locationManager == null || hasNoLocationPermission(context)) {
                return null
            }
            try {
                return locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
            } catch (e: IllegalArgumentException) {
                Timber.e(e, "Fused: %s", e.message)
            } catch (e: SecurityException) {
                Timber.e(e, "Fused: %s", e.message)
            } catch (e: NullPointerException) {
                Timber.e(e, "Fused: %s", e.message)
            }
            return null
        }

    /**
     * Get a location from GPS.
     *
     * @return the location - `null` otherwise.
     */
    private val locationGPS: Location?
        get() {
            val locationManager = locationManager
            if (locationManager == null || hasNoLocationPermission(context)) {
                return null
            }
            try {
                return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } catch (e: IllegalArgumentException) {
                Timber.e(e, "GPS: %s", e.message)
            } catch (e: SecurityException) {
                Timber.e(e, "GPS: %s", e.message)
            } catch (e: NullPointerException) {
                Timber.e(e, "GPS: %s", e.message)
            }
            return null
        }

    /**
     * Get a location from the GSM network.
     *
     * @return the location - `null` otherwise.
     */
    private val locationNetwork: Location?
        get() {
            val locationManager = locationManager
            if (locationManager == null || hasNoLocationPermission(context)) {
                return null
            }
            try {
                return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (e: IllegalArgumentException) {
                Timber.e(e, "Network: %s", e.message)
            } catch (e: SecurityException) {
                Timber.e(e, "Network: %s", e.message)
            } catch (e: NullPointerException) {
                Timber.e(e, "Network: %s", e.message)
            }
            return null
        }

    /**
     * Get a passive location from other application's GPS.
     *
     * @return the location - `null` otherwise.
     */
    private val locationPassive: Location?
        get() {
            val locationManager = locationManager
            if (locationManager == null || hasNoLocationPermission(context)) {
                return null
            }
            try {
                return locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            } catch (e: IllegalArgumentException) {
                Timber.e(e, "Passive: %s", e.message)
            } catch (e: SecurityException) {
                Timber.e(e, "Passive: %s", e.message)
            } catch (e: NullPointerException) {
                Timber.e(e, "Passive: %s", e.message)
            }
            return null
        }

    /**
     * Get a location from the saved preferences.
     *
     * @return the location - {@code null} otherwise.
     */
    private var locationSaved: Location?
        get() = preferences.location
        set(value) {
            preferences.location = value
        }

    /**
     * Get a location from the time zone.
     *
     * @return the location - `null` otherwise.
     */
    private val locationTZ: Location
        get() = getLocationTZ(timeZone)

    /**
     * Get a location from the time zone.
     *
     * @param timeZone the time zone.
     * @return the location - `null` otherwise.
     */
    fun getLocationTZ(timeZone: TimeZone): Location {
        return countriesGeocoder.findLocation(timeZone)
    }

    /**
     * Get the best location.
     *
     * @return the location - `null` otherwise.
     */
    fun getLocation(): Location? {
        var location = locationLocal
        if (isValid(location)) return location
        location = locationSaved
        if (isValid(location)) return location
        location = locationGPS
        if (isValid(location)) return location
        location = locationNetwork
        if (isValid(location)) return location
        location = locationPassive
        if (isValid(location)) return location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location = locationFused
            if (isValid(location)) return location
        }
        location = locationTZ
        if (isValid(location)) return location
        return null
    }

    /**
     * Load available locations.
     */
    private fun loadLocation() {
        var location = locationLocal
        if (isValid(location)) {
            handleLocationChanged(location)
            return
        }
        location = locationSaved
        if (isValid(location)) {
            handleLocationChanged(location)
            return
        }
        location = locationGPS
        if (isValid(location)) {
            handleLocationChanged(location)
            return
        }
        location = locationNetwork
        if (isValid(location)) {
            handleLocationChanged(location)
            return
        }
        location = locationPassive
        if (isValid(location)) {
            handleLocationChanged(location)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location = locationFused
            if (isValid(location)) {
                handleLocationChanged(location)
                return
            }
        }
        location = locationTZ
        if (isValid(location)) {
            handleLocationChanged(location)
        }
    }

    /**
     * Is the location valid?
     *
     * @param location the location to check.
     * @return `false` if location is invalid.
     */
    private fun isValid(location: Location?): Boolean {
        return ZmanimLocation.isValid(location)
    }

    /**
     * Is the location valid?
     *
     * @param address the address to check.
     * @return `false` if address is invalid.
     */
    private fun isValid(address: ZmanimAddress?): Boolean {
        return ZmanimAddress.isValid(address)
    }

    /**
     * Stop listening.
     *
     * @param listener the listener who wants to stop listening.
     */
    fun stop(listener: ZmanimLocationListener?) {
        listener?.let { removeLocationListener(it) }
        if (!hasActiveListeners()) {
            removeUpdates()
        }
    }

    /**
     * Start or resume listening.
     *
     * @param listener the listener who wants to resume listening.
     */
    fun start(listener: ZmanimLocationListener?) {
        if (listener == null) {
            Timber.w("start with listener null")
            return
        }
        if (!handlerThread.isAlive) {
            Timber.w("start with dead handler")
            return
        }
        addLocationListener(listener)

        // Give the listener our latest known location and address.
        val location = getLocation()
        if (location != null) {
            listener.onLocationChanged(location)
        } else {
            loadLocation()
        }
        startTaskDelay = UPDATE_INTERVAL_START
        sendEmptyMessage(WHAT_START)
    }

    /**
     * Is the location in Israel?<br></br>
     * Used to determine if user is in diaspora for 2-day festivals.
     *
     * @param location the location.
     * @param timeZone the time zone.
     * @return `true` if user is in Israel - `false` otherwise.
     */
    private fun isInIsrael(location: Location?, timeZone: TimeZone?): Boolean {
        if (location == null) {
            val tz = timeZone ?: this.timeZone
            val id = tz.id
            if (TZ_JERUSALEM == id || TZ_BEIRUT == id) return true
            // Check offsets because "IST" could be "Ireland ST",
            // "JST" could be "Japan ST".
            val offset = tz.rawOffset + tz.dstSavings
            return (offset in TZ_OFFSET_ISRAEL..TZ_OFFSET_DST_ISRAEL) &&
                (TZ_IDT == id || TZ_IST == id || TZ_JST == id)
        }
        val latitude = location.latitude
        val longitude = location.longitude
        return (latitude in ISRAEL_SOUTH..ISRAEL_NORTH) && (longitude in ISRAEL_WEST..ISRAEL_EAST)
    }

    /**
     * Is the current location in Israel?<br></br>
     * Used to determine if user is in diaspora for 2-day festivals.
     *
     * @param timeZone the time zone.
     * @return `true` if user is in Israel - `false` otherwise.
     */
    private fun isInIsrael(timeZone: TimeZone?): Boolean {
        return isInIsrael(getLocation(), timeZone)
    }

    /**
     * Is the current location in Israel?<br></br>
     * Used to determine if user is in diaspora for 2-day festivals.
     *
     * @return `true` if user is in Israel - `false` otherwise.
     */
    val isInIsrael: Boolean
        get() = isInIsrael(timeZone)

    override fun formatCoordinates(location: Location) =
        formatterHelper.formatCoordinates(location)

    override fun formatCoordinates(address: Address) =
        formatterHelper.formatCoordinates(address)

    override fun formatCoordinates(latitude: Double, longitude: Double, elevation: Double) =
        formatterHelper.formatCoordinates(latitude, longitude, elevation)

    override fun formatLatitude(latitude: Double) =
        formatterHelper.formatLatitude(latitude)

    override fun formatLatitudeDecimal(latitude: Double) =
        formatterHelper.formatLatitudeDecimal(latitude)

    override fun formatLatitudeSexagesimal(latitude: Double) =
        formatterHelper.formatLatitudeSexagesimal(latitude)

    override fun formatLongitude(longitude: Double) =
        formatterHelper.formatLongitude(longitude)

    override fun formatLongitudeDecimal(longitude: Double) =
        formatterHelper.formatLongitudeDecimal(longitude)

    override fun formatLongitudeSexagesimal(longitude: Double) =
        formatterHelper.formatLongitudeSexagesimal(longitude)

    override fun formatElevation(elevation: Double) =
        formatterHelper.formatElevation(elevation)

    override fun formatBearing(azimuth: Double) =
        formatterHelper.formatBearing(azimuth)

    override fun formatBearingDecimal(azimuth: Double) =
        formatterHelper.formatBearingDecimal(azimuth)

    override fun formatBearingSexagesimal(azimuth: Double) =
        formatterHelper.formatBearingSexagesimal(azimuth)

    override fun parseLatitude(coordinate: String) =
        formatterHelper.parseLatitude(coordinate)

    override fun parseLongitude(coordinate: String) =
        formatterHelper.parseLongitude(coordinate)

    /**
     * Set the location.
     *
     * @param location the location - `null` to request the current location.
     */
    fun setLocation(location: Location?) {
        this.locationLocal = null
        this.locationManual = location
        this.locationSaved = null
        if (location == null) {
            handler.removeMessages(WHAT_CHANGED)
            handler.removeMessages(WHAT_START)
            handler.obtainMessage(WHAT_START, true).sendToTarget()
        } else {
            handleLocationChanged(location)
        }
    }

    private fun requestUpdates(reset: Boolean = false) {
        if (hasNoLocationPermission(context)) {
            Timber.w("No location permissions")
            return
        }
        val locationManager = locationManager
        if (locationManager == null) {
            Timber.w("No location manager")
            return
        }
        if (reset) {
            this.locationLocal = null
            this.locationSaved = null
        }
        loadLocation()
        val provider = getBestProvider(locationManager)
        if (provider == null) {
            Timber.w("No location provider")
            return
        }
        try {
            locationManager.removeUpdates(listener)
            locationManager.requestLocationUpdates(
                provider,
                UPDATE_TIME,
                UPDATE_DISTANCE,
                listener,
                handlerThread.looper
            )
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "request updates: %s", e.message)
        } catch (e: SecurityException) {
            Timber.e(e, "request updates: %s", e.message)
        } catch (e: NullPointerException) {
            Timber.e(e, "request updates: %s", e.message)
        }

        // Let the updates run for only a small while to save battery.
        sendEmptyMessageDelayed(WHAT_STOP, UPDATE_DURATION)
        startTaskDelay = min(UPDATE_INTERVAL_MAX, startTaskDelay shl 1)
    }

    private fun getBestProvider(locationManager: LocationManager): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return getBestProvider34(locationManager)
        }
        return getBestProviderCriteria(locationManager)
    }

    @TargetApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun getBestProvider34(locationManager: LocationManager): String? {
        val providers = locationManager.getProviders(true)
        val providerGPS = providers.firstOrNull { provider ->
            val props =
                locationManager.getProviderProperties(provider) ?: return@firstOrNull false
            return@firstOrNull props.hasSatelliteRequirement()
        }
        if (providerGPS != null) return providerGPS
        if (providers.contains(LocationManager.FUSED_PROVIDER)) {
            return LocationManager.FUSED_PROVIDER
        }
        return providers.firstOrNull()
    }

    @Suppress("DEPRECATION")
    private fun getBestProviderCriteria(locationManager: LocationManager): String? {
        val criteria = Criteria().apply {
            accuracy = Criteria.ACCURACY_COARSE
            isCostAllowed = true
        }
        return locationManager.getBestProvider(criteria, true)
    }

    private fun removeUpdates() {
        try {
            locationManager?.removeUpdates(listener)
        } catch (e: Exception) {
            Timber.e(e, "remove updates: %s", e.message)
        }
        if (hasActiveListeners()) {
            sendEmptyMessageDelayed(WHAT_START, startTaskDelay)
        } else {
            handler.removeMessages(WHAT_START)
        }
    }

    /**
     * Quit updating locations.
     */
    fun quit() {
        locationManual = null
        listeners.clear()
        removeUpdates()
        context.unregisterReceiver(broadcastReceiver)
        handler.removeMessages(WHAT_ADDRESS)
        handler.removeMessages(WHAT_CHANGED)
        handler.removeMessages(WHAT_ELEVATION)
        handler.removeMessages(WHAT_START)
        handler.removeMessages(WHAT_STOP)
        handlerThread.quit()
        handlerThread.interrupt()
    }

    private inner class UpdatesHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            var location: Location? = null
            var address: ZmanimAddress? = null

            when (msg.what) {
                WHAT_START -> {
                    var reset = false
                    if (msg.obj != null) {
                        reset = (msg.obj as? Boolean).isTrue
                    }
                    requestUpdates(reset)
                }

                WHAT_STOP -> removeUpdates()

                WHAT_CHANGED -> if (msg.obj != null) {
                    location = msg.obj as Location
                    onLocationChanged(location)
                }

                WHAT_ADDRESS -> if (msg.obj != null) {
                    if (msg.obj is ZmanimAddress) {
                        address = msg.obj as ZmanimAddress
                        location = LocationData.from(address.extras, EXTRA_LOCATION)
                    } else if (msg.obj is Location) {
                        location = msg.obj as Location
                    }
                    if (location != null && address != null) {
                        onAddressChanged(location, address)
                    }
                }

                WHAT_ELEVATION -> if (msg.obj != null) {
                    location = msg.obj as Location
                    onElevationChanged(location)
                }
            }
        }
    }

    fun findAddress(location: Location, persist: Boolean = true, force: Boolean = false) {
        enqueueAddress(context, location, persist, force)
    }

    fun findElevation(location: Location) {
        if (location.hasAltitude()) {
            return
        }
        enqueueElevation(context, location)
    }

    /**
     * Create a location formatter helper.
     *
     * @param context the context.
     * @return the formatter.
     */
    protected open fun createLocationFormatter(context: Context): LocationFormatter {
        return SimpleLocationFormatter(context, preferences)
    }

    /**
     * Are any listeners active?
     *
     * @return `true` if no listeners are passive.
     */
    private fun hasActiveListeners(): Boolean {
        return !listeners.isEmpty()
    }

    /**
     * The receiver for addresses and date/time settings.
     */
    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action.isNullOrEmpty()) {
                return
            }
            val intentPackage = intent.getPackage()
            var address: ZmanimAddress? = null
            val intentExtras = intent.extras
            val handler = this@LocationsProvider.handler

            when (action) {
                ACTION_ADDRESS -> {
                    if (intentPackage.isNullOrEmpty() || intentPackage != context.packageName) {
                        return
                    }
                    val location = LocationData.from(intentExtras, EXTRA_LOCATION)
                    if (intentExtras != null) {
                        address = intentExtras.getAddress(EXTRA_ADDRESS)
                    }
                    if (address != null) {
                        address.extras = (address.extras ?: Bundle()).apply {
                            putParcelable(EXTRA_LOCATION, location)
                        }
                        handler.obtainMessage(WHAT_ADDRESS, address).sendToTarget()
                    } else {
                        handler.obtainMessage(WHAT_ADDRESS, location).sendToTarget()
                    }
                }

                ACTION_ELEVATION -> {
                    if (intentPackage.isNullOrEmpty() || intentPackage != context.packageName) {
                        return
                    }
                    val location = LocationData.from(intentExtras, EXTRA_LOCATION)
                    handler.obtainMessage(WHAT_ELEVATION, location).sendToTarget()
                }

                Intent.ACTION_TIMEZONE_CHANGED -> timeZone = TimeZone.getDefault()
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(ACTION_ADDRESS)
            addAction(ACTION_ELEVATION)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(broadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(broadcastReceiver, filter, 0x4)
        } else {
            context.registerReceiver(broadcastReceiver, filter)
        }
    }

    private fun sendEmptyMessage(what: Int): Boolean {
        return handlerThread.isAlive && handler.sendEmptyMessage(what)
    }

    private fun sendEmptyMessageDelayed(what: Int, delayMillis: Long): Boolean {
        return handlerThread.isAlive && handler.sendEmptyMessageDelayed(what, delayMillis)
    }

    companion object {
        private const val TAG = "LocationProvider"

        val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        /**
         * The maximum time interval between location updates, in milliseconds.
         */
        private const val UPDATE_INTERVAL_MAX = 6 * DateUtils.HOUR_IN_MILLIS

        /**
         * The time interval between requesting location updates, in milliseconds.
         */
        private const val UPDATE_INTERVAL_START = 30 * DateUtils.SECOND_IN_MILLIS

        /**
         * The duration to receive updates, in milliseconds.<br></br>
         * Should be enough time to get a sufficiently accurate location.
         */
        private const val UPDATE_DURATION = DateUtils.MINUTE_IN_MILLIS

        /**
         * The minimum time interval between location updates, in milliseconds.
         */
        private const val UPDATE_TIME = 2 * DateUtils.SECOND_IN_MILLIS

        /**
         * The minimum distance between location updates, in metres.
         */
        private const val UPDATE_DISTANCE = 10f

        /**
         * Time zone ID for Jerusalem.
         */
        private const val TZ_JERUSALEM = "Asia/Jerusalem"

        /**
         * Time zone ID for Israeli Standard Time.
         */
        private const val TZ_IST = "IST"

        /**
         * Time zone ID for Israeli Daylight Time.
         */
        private const val TZ_IDT = "IDT"

        /**
         * Time zone ID for Jerusalem Standard Time.
         */
        private const val TZ_JST = "JST"

        /**
         * Time zone ID for Beirut (patch for Israeli law of DST 2013).
         */
        private const val TZ_BEIRUT = "Asia/Beirut"

        /**
         * The offset in milliseconds from UTC of Israeli time zone's standard time.
         */
        private const val TZ_OFFSET_ISRAEL = (2 * DateUtils.HOUR_IN_MILLIS).toInt()

        /**
         * Israeli time zone offset with daylight savings time.
         */
        private const val TZ_OFFSET_DST_ISRAEL =
            (TZ_OFFSET_ISRAEL + DateUtils.HOUR_IN_MILLIS).toInt()

        /**
         * Northern-most latitude for Israel.
         */
        private const val ISRAEL_NORTH = 33.289212

        /**
         * Southern-most latitude for Israel.
         */
        private const val ISRAEL_SOUTH = 29.489218

        /**
         * Eastern-most longitude for Israel.
         */
        private const val ISRAEL_EAST = 35.891876

        /**
         * Western-most longitude for Israel.
         */
        private const val ISRAEL_WEST = 34.215317

        /**
         * Start seeking locations.
         */
        private const val WHAT_START = 0

        /**
         * Stop seeking locations.
         */
        private const val WHAT_STOP = 1

        /**
         * Location has changed.
         */
        private const val WHAT_CHANGED = 2

        /**
         * Found an elevation.
         */
        private const val WHAT_ELEVATION = 3

        /**
         * Found an address.
         */
        private const val WHAT_ADDRESS = 4

        /**
         * If the current location is older than 1 second, then it is stale.
         */
        private const val LOCATION_EXPIRATION = DateUtils.SECOND_IN_MILLIS

        const val LATITUDE_MIN = ZmanimLocation.LATITUDE_MIN
        const val LATITUDE_MAX = ZmanimLocation.LATITUDE_MAX
        const val LONGITUDE_MIN = ZmanimLocation.LONGITUDE_MIN
        const val LONGITUDE_MAX = ZmanimLocation.LONGITUDE_MAX

        fun hasNoLocationPermission(context: Context): Boolean {
            return PermissionChecker.checkCallingOrSelfPermission(
                context,
                PERMISSIONS[0]
            ) != PermissionChecker.PERMISSION_GRANTED
        }
    }
}