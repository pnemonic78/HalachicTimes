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

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.github.app.SimpleThemeCallbacks;
import com.github.app.ThemeCallbacks;
import com.github.preference.ThemePreferences;

import java.util.TimeZone;

import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES.M;

/**
 * Activity that needs locations.
 *
 * @author Moshe Waisberg
 */
public abstract class LocatedActivity<P extends ThemePreferences> extends Activity implements
        ThemeCallbacks<P>,
        ZmanimLocationListener {

    /** The location parameter. */
    public static final String EXTRA_LOCATION = LocationManager.KEY_LOCATION_CHANGED;

    /** Activity id for searching locations. */
    protected static final int ACTIVITY_LOCATIONS = 1;
    /** Activity id for requesting location permissions. */
    protected static final int ACTIVITY_PERMISSIONS = 2;

    protected static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private ThemeCallbacks<P> themeCallbacks;
    /** Provider for locations. */
    private LocationsProvider locations;
    /** The address location. */
    private Location addressLocation;
    /** The address. */
    private ZmanimAddress address;
    /** Populate the header in UI thread. */
    private Runnable populateHeader;
    /** Update the location in UI thread. */
    private Runnable updateLocation;

    /**
     * Get the locations provider.
     *
     * @return hte provider.
     */
    public LocationsProvider getLocations() {
        return locations;
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
        super.onCreate(savedInstanceState);
        onCreate();

        LocationApplication app = (LocationApplication) getApplication();
        locations = app.getLocations();

        Intent intent = getIntent();
        Location location = intent.getParcelableExtra(EXTRA_LOCATION);
        if (location != null) {
            locations.setLocation(location);
        } else if (VERSION.SDK_INT >= M) {
            initLocationPermissions();
        }
    }

    @Override
    public void onCreate() {
        getThemeCallbacks().onCreate();
    }

    @Override
    public P getThemePreferences() {
        return getThemeCallbacks().getThemePreferences();
    }

    protected ThemeCallbacks<P> getThemeCallbacks() {
        if (themeCallbacks == null) {
            themeCallbacks = createThemeCallbacks(this);
        }
        return themeCallbacks;
    }

    protected ThemeCallbacks<P> createThemeCallbacks(ContextWrapper context) {
        return new SimpleThemeCallbacks<P>(context);
    }

    protected LocationPreferences getLocationPreferences() {
        return locations.getLocationPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        locations.start(this);
    }

    @Override
    protected void onStop() {
        locations.stop(this);
        super.onStop();
    }

    @Override
    public void onAddressChanged(Location location, ZmanimAddress address) {
        addressLocation = location;
        this.address = address;
        if (populateHeader == null) {
            populateHeader = createPopulateHeaderRunnable();
        }
        runOnUiThread(populateHeader);
    }

    protected abstract Runnable createPopulateHeaderRunnable();

    @Override
    public void onElevationChanged(Location location) {
        onLocationChanged(location);
    }

    @Override
    public boolean isPassive() {
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (ZmanimLocation.compareTo(addressLocation, location) != 0) {
            address = null;
        }
        addressLocation = location;
        if (updateLocation == null) {
            updateLocation = createUpdateLocationRunnable();
        }
        runOnUiThread(updateLocation);
    }

    protected abstract Runnable createUpdateLocationRunnable();

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
        Location loc = locations.getLocation();
        // Have we been destroyed?
        if (loc == null)
            return;

        final Activity activity = this;
        Intent intent = new Intent(activity, getLocationActivityClass());
        intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, loc);
        activity.startActivityForResult(intent, ACTIVITY_LOCATIONS);
    }

    protected abstract Class<? extends Activity> getLocationActivityClass();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_LOCATIONS) {
            if (resultCode == RESULT_OK) {
                Location loc = data.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
                if (loc == null) {
                    locations.setLocation(null);
                    loc = locations.getLocation();
                }
                locations.setLocation(loc);
            }
        }
    }

    /**
     * Search key was pressed.
     */
    @Override
    public boolean onSearchRequested() {
        Location loc = getLocation();
        // Have we been destroyed?
        if (loc == null)
            return super.onSearchRequested();

        ZmanimAddress address = this.address;
        String query = (address != null) ? address.getFormatted() : null;

        Bundle appData = new Bundle();
        appData.putParcelable(LocationManager.KEY_LOCATION_CHANGED, loc);
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

    @TargetApi(M)
    private void initLocationPermissions() {
        if ((checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                && (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(PERMISSIONS, ACTIVITY_PERMISSIONS);
        }
    }
}
