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

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.github.app.SimpleThemeCallbacks
import com.github.app.ThemeCallbacks
import com.github.os.getParcelableCompat
import com.github.preference.ThemePreferences
import com.github.times.location.LocationsProvider.Companion.hasNoLocationPermission
import com.github.times.location.ZmanimLocation.Companion.compare
import java.util.TimeZone
import timber.log.Timber

/**
 * Activity that needs locations.
 *
 * @author Moshe Waisberg
 */
abstract class LocatedActivity<P : ThemePreferences> : AppCompatActivity(),
    ThemeCallbacks<P>,
    ZmanimLocationListener {

    private val themeCallbacks: ThemeCallbacks<P> by lazy { createThemeCallbacks(this) }

    override val themePreferences: P
        get() = themeCallbacks.themePreferences

    /**
     * The address location.
     */
    protected var addressLocation: Location? = null
        private set

    /**
     * The address.
     */
    protected var address: ZmanimAddress? = null
        private set

    /**
     * Bind the header in UI thread.
     */
    private val bindHeader: Runnable by lazy { createBindHeaderRunnable() }

    /**
     * The location header location.
     */
    @JvmField
    protected var headerLocation: TextView? = null

    /**
     * The location header for formatted address.
     */
    @JvmField
    protected var headerAddress: TextView? = null

    /**
     * Get the locations provider.
     *
     * @return the provider.
     */
    val locations: LocationsProvider
        get() {
            val app = application as LocationApplication<*, *, *>
            return app.locations
        }

    /**
     * Get the location.
     *
     * @return the location.
     */
    protected val location: Location?
        get() = locations.getLocation()

    /**
     * Get the time zone.
     *
     * @return the time zone.
     */
    protected val timeZone: TimeZone
        get() = locations.timeZone

    protected val locationPreferences: LocationPreferences
        get() = locations.preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        onPreCreate()
        super.onCreate(savedInstanceState)
        val location = intent.getParcelableCompat<Location>(EXTRA_LOCATION, Location::class.java)
        if (location != null) {
            locations.setLocation(location)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initLocationPermissions()
        }
    }

    override fun onPreCreate() {
        themeCallbacks.onPreCreate()
    }

    protected open fun createThemeCallbacks(context: Context): ThemeCallbacks<P> {
        return SimpleThemeCallbacks(context)
    }

    override fun onStart() {
        super.onStart()
        locations.start(this)
    }

    override fun onStop() {
        super.onStop()
        locations.stop(this)
    }

    override fun onAddressChanged(location: Location, address: ZmanimAddress) {
        Timber.v("onAddressChanged %s %s", location, address)
        addressLocation = location
        this.address = address
        runOnUiThread(bindHeader)
    }

    protected open fun createBindHeaderRunnable(): Runnable {
        return Runnable { bindHeader() }
    }

    override fun onElevationChanged(location: Location) {
        onLocationChanged(location)
    }

    override fun onLocationChanged(location: Location) {
        Timber.v("onLocationChanged %s <= %s", location, addressLocation)
        if (compare(addressLocation, location) != 0) {
            address = null
        }
        addressLocation = location
        val updateLocation = createUpdateLocationRunnable(location)
        runOnUiThread(updateLocation)
        locations.findAddress(location)
    }

    protected abstract fun createUpdateLocationRunnable(location: Location): Runnable

    override fun onProviderDisabled(provider: String) = Unit

    override fun onProviderEnabled(provider: String) = Unit

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) = Unit

    protected fun startLocations() {
        val location = locations.getLocation() ?: return
        // Have we been destroyed?
        val activity: Activity = this
        val activityClass = locationActivityClass ?: return
        val intent = Intent(activity, locationActivityClass)
            .putExtra(LocationManager.KEY_LOCATION_CHANGED, location)
        activity.startActivityForResult(intent, ACTIVITY_LOCATIONS)
    }

    protected abstract val locationActivityClass: Class<out Activity>?

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACTIVITY_LOCATIONS) {
            if (resultCode == RESULT_OK) {
                val location = data?.getParcelableCompat(
                    LocationManager.KEY_LOCATION_CHANGED,
                    Location::class.java
                )
                locations.setLocation(location)
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Search key was pressed.
     */
    override fun onSearchRequested(): Boolean {
        val location = location ?: return super.onSearchRequested()
        // Have we been destroyed?
        val address = address ?: return false
        val query = address.formatted
        val appData = Bundle()
        appData.putParcelable(LocationManager.KEY_LOCATION_CHANGED, location)
        startSearch(query, false, appData, false)
        return true
    }

    /**
     * Format the address for the current location.
     *
     * @param address the address.
     * @return the formatted address.
     */
    protected fun formatAddress(address: ZmanimAddress?): CharSequence {
        return address?.formatted ?: getString(R.string.location_unknown)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun initLocationPermissions() {
        if (hasNoLocationPermission(this)) {
            requestPermissions(LocationsProvider.PERMISSIONS, ACTIVITY_PERMISSIONS)
        }
    }

    /**
     * Bind the header.
     */
    protected fun bindHeader() {
        val location = addressLocation ?: locations.getLocation()
        bindHeader(location)
    }

    /**
     * Bind the header.
     *
     * @param location the location to format.
     */
    protected fun bindHeader(location: Location?) {
        if (location == null) return
        val locationLabel = headerLocation ?: return
        val addressLabel = headerAddress ?: return
        val formatter: LocationFormatter = locations
        val locationText: CharSequence = formatter.formatCoordinates(location)
        val locationName = formatAddress(address)
        Timber.d("header [$locationText] => [$locationName]")

        // Update the location.
        locationLabel.text = locationText
        locationLabel.isVisible = locationPreferences.isCoordinatesVisible
        addressLabel.text = locationName
    }

    companion object {
        /**
         * The location parameter.
         */
        const val EXTRA_LOCATION = LocationManager.KEY_LOCATION_CHANGED

        /**
         * Activity id for searching locations.
         */
        protected const val ACTIVITY_LOCATIONS = 0x10C

        /**
         * Activity id for requesting location permissions.
         */
        protected const val ACTIVITY_PERMISSIONS = 0xA110
    }
}