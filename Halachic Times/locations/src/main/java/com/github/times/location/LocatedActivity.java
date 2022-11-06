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
package com.github.times.location;

import static android.os.Build.VERSION;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.app.SimpleThemeCallbacks;
import com.github.app.ThemeCallbacks;
import com.github.preference.ThemePreferences;

import java.util.TimeZone;

import timber.log.Timber;

/**
 * Activity that needs locations.
 *
 * @author Moshe Waisberg
 */
public abstract class LocatedActivity<P extends ThemePreferences> extends AppCompatActivity implements
        ThemeCallbacks<P>,
        ZmanimLocationListener {

    /** The location parameter. */
    public static final String EXTRA_LOCATION = LocationManager.KEY_LOCATION_CHANGED;

    /** Activity id for searching locations. */
    protected static final int ACTIVITY_LOCATIONS = 0x10C;
    /** Activity id for requesting location permissions. */
    protected static final int ACTIVITY_PERMISSIONS = 0xA110;

    private ThemeCallbacks<P> themeCallbacks;
    /** The address location. */
    private Location addressLocation;
    /** The address. */
    private ZmanimAddress address;
    /** Bind the header in UI thread. */
    private Runnable bindHeader;
    /** The location header location. */
    protected TextView headerLocation;
    /** The location header for formatted address. */
    protected TextView headerAddress;

    /**
     * Get the locations provider.
     *
     * @return hte provider.
     */
    public LocationsProvider getLocations() {
        LocationApplication<?, ?, ?> app = (LocationApplication<?, ?, ?>) getApplication();
        return app.getLocations();
    }

    /**
     * Get the location.
     *
     * @return the location.
     */
    protected Location getLocation() {
        return getLocations().getLocation();
    }

    /**
     * Get the time zone.
     *
     * @return the time zone.
     */
    protected TimeZone getTimeZone() {
        return getLocations().getTimeZone();
    }

    protected Location getAddressLocation() {
        return addressLocation;
    }

    protected ZmanimAddress getAddress() {
        return address;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        onPreCreate();
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Location location = intent.getParcelableExtra(EXTRA_LOCATION);
        if (location != null) {
            getLocations().setLocation(location);
        } else if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initLocationPermissions();
        }
    }

    @Override
    public void onPreCreate() {
        getThemeCallbacks().onPreCreate();
    }

    @Override
    public P getThemePreferences() {
        return getThemeCallbacks().getThemePreferences();
    }

    protected ThemeCallbacks<P> getThemeCallbacks() {
        ThemeCallbacks<P> themeCallbacks = this.themeCallbacks;
        if (themeCallbacks == null) {
            themeCallbacks = createThemeCallbacks(this);
            this.themeCallbacks = themeCallbacks;
        }
        return themeCallbacks;
    }

    protected ThemeCallbacks<P> createThemeCallbacks(Context context) {
        return new SimpleThemeCallbacks<>(context);
    }

    protected LocationPreferences getLocationPreferences() {
        return getLocations().getLocationPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLocations().start(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getLocations().stop(this);
    }

    @Override
    public void onAddressChanged(Location location, ZmanimAddress address) {
        Timber.v("onAddressChanged %s %s", location, address);
        addressLocation = location;
        this.address = address;
        Runnable populateHeader = this.bindHeader;
        if (populateHeader == null) {
            populateHeader = createBindHeaderRunnable();
            this.bindHeader = populateHeader;
        }
        runOnUiThread(populateHeader);
    }

    protected Runnable createBindHeaderRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                bindHeader();
            }
        };
    }

    @Override
    public void onElevationChanged(Location location) {
        onLocationChanged(location);
    }

    @Override
    public void onLocationChanged(Location location) {
        Timber.v("onLocationChanged %s <= %s", location, addressLocation);
        if (ZmanimLocation.compare(addressLocation, location) != 0) {
            address = null;
        }
        addressLocation = location;
        Runnable updateLocation = createUpdateLocationRunnable(location);
        runOnUiThread(updateLocation);
        getLocations().findAddress(location);
    }

    protected abstract Runnable createUpdateLocationRunnable(Location location);

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    protected void startLocations() {
        Location location = getLocations().getLocation();
        // Have we been destroyed?
        if (location == null)
            return;

        final Activity activity = this;
        Intent intent = new Intent(activity, getLocationActivityClass());
        intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
        activity.startActivityForResult(intent, ACTIVITY_LOCATIONS);
    }

    protected abstract Class<? extends Activity> getLocationActivityClass();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_LOCATIONS) {
            if (resultCode == RESULT_OK) {
                Location location = data.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
                getLocations().setLocation(location);
            }
        }
    }

    /**
     * Search key was pressed.
     */
    @Override
    public boolean onSearchRequested() {
        Location location = getLocation();
        // Have we been destroyed?
        if (location == null)
            return super.onSearchRequested();

        ZmanimAddress address = this.address;
        String query = (address != null) ? address.getFormatted() : null;

        Bundle appData = new Bundle();
        appData.putParcelable(LocationManager.KEY_LOCATION_CHANGED, location);
        startSearch(query, false, appData, false);
        return true;
    }

    /**
     * Format the address for the current location.
     *
     * @param address
     *         the address.
     * @return the formatted address.
     */
    protected CharSequence formatAddress(ZmanimAddress address) {
        if (address != null)
            return address.getFormatted();
        return getString(R.string.location_unknown);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initLocationPermissions() {
        if (LocationsProvider.hasNoLocationPermission(this)) {
            requestPermissions(LocationsProvider.PERMISSIONS, ACTIVITY_PERMISSIONS);
        }
    }

    /** Bind the header. */
    protected void bindHeader() {
        // Have we been destroyed?
        LocationsProvider locations = getLocations();
        Location addressLocation = getAddressLocation();
        Location location = (addressLocation == null) ? locations.getLocation() : addressLocation;
        bindHeader(location);
    }

    /**
     * Bind the header.
     * @param location the location to format.
     */
    protected void bindHeader(Location location) {
        if (location == null)
            return;
        TextView locationLabel = headerLocation;
        TextView addressLabel = headerAddress;
        // Have we been destroyed?
        if ((locationLabel == null) || (addressLabel == null))
            return;
        final ZmanimAddress address = getAddress();

        LocationFormatter formatter = getLocations();
        final CharSequence locationText = formatter.formatCoordinates(location);
        final CharSequence locationName = formatAddress(address);
        Timber.d("header [" + locationText +"] => [" + locationName + "]");

        // Update the location.
        locationLabel.setText(locationText);
        locationLabel.setVisibility(getLocationPreferences().isCoordinatesVisible() ? View.VISIBLE : View.GONE);
        addressLabel.setText(locationName);
    }
}
