/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.times.compass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import net.sf.times.compass.lib.R;
import net.sf.times.compass.preference.CompassSettings;
import net.sf.times.location.LocationApplication;
import net.sf.times.location.LocationsProvider;
import net.sf.times.location.ZmanimAddress;
import net.sf.times.location.ZmanimLocation;
import net.sf.times.location.ZmanimLocationListener;

/**
 * Show the direction in which to pray. Points to the Holy of Holies in
 * Jerusalem in Israel.
 *
 * @author Moshe Waisberg
 */
public abstract class BaseCompassActivity extends Activity implements ZmanimLocationListener {

    /** Activity id for searching locations. */
    private static final int ACTIVITY_LOCATIONS = 1;

    /** Provider for locations. */
    private LocationsProvider locations;
    /** The main fragment. */
    private CompassFragment fragment;
    /** The settings and preferences. */
    private CompassSettings settings;
    /** The address location. */
    private Location addressLocation;
    /** The address. */
    private ZmanimAddress address;
    /** Populate the header in UI thread. */
    private Runnable populateHeader;
    /** Update the location in UI thread. */
    private Runnable updateLocation;
    /** The location header location. */
    private TextView headerLocation;
    /** The location header for formatted address. */
    private TextView headerAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = this;
        settings = new CompassSettings(context);

        setTheme(settings.getTheme());
        setContentView(R.layout.compass);
        headerLocation = (TextView) findViewById(R.id.coordinates);
        headerAddress = (TextView) findViewById(R.id.address);
        fragment = (CompassFragment) getFragmentManager().findFragmentById(R.id.compass);

        if (!settings.isSummaries()) {
            View summary = findViewById(android.R.id.summary);
            summary.setVisibility(View.GONE);
        }

        LocationApplication app = (LocationApplication) getApplication();
        locations = app.getLocations();
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
    public void onLocationChanged(Location location) {
        if (ZmanimLocation.compareTo(addressLocation, location) != 0) {
            address = null;
        }
        addressLocation = location;
        if (updateLocation == null) {
            updateLocation = new Runnable() {
                @Override
                public void run() {
                    // Have we been destroyed?
                    Location loc = addressLocation;
                    if (loc == null)
                        return;

                    populateHeader();

                    CompassFragment c = fragment;
                    if (c == null)
                        return;
                    c.setLocation(loc);
                }
            };
        }
        runOnUiThread(updateLocation);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    /** Populate the header item. */
    private void populateHeader() {
        TextView locationLabel = headerLocation;
        TextView addressLabel = headerAddress;
        // Have we been destroyed?
        if ((locationLabel == null) || (addressLabel == null))
            return;
        // Have we been destroyed?
        Location loc = (addressLocation == null) ? locations.getLocation() : addressLocation;
        if (loc == null)
            return;

        final CharSequence locationText = locations.formatCoordinates(loc);
        final CharSequence locationName = formatAddress(address);

        // Update the location.
        locationLabel.setText(locationText);
        locationLabel.setVisibility(settings.isCoordinates() ? View.VISIBLE : View.GONE);
        addressLabel.setText(locationName);
    }

    /**
     * Format the address for the current location.
     *
     * @param address
     *         the address.
     * @return the formatted address.
     */
    private CharSequence formatAddress(ZmanimAddress address) {
        if (address != null)
            return address.getFormatted();
        return getString(R.string.location_unknown);
    }

    @Override
    public void onAddressChanged(Location location, ZmanimAddress address) {
        addressLocation = location;
        this.address = address;
        if (populateHeader == null) {
            populateHeader = new Runnable() {
                @Override
                public void run() {
                    populateHeader();
                }
            };
        }
        runOnUiThread(populateHeader);
    }

    @Override
    public void onElevationChanged(Location location) {
        onLocationChanged(location);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compass, menu);
        return true;
    }

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

    protected void startLocations() {
        Location loc = locations.getLocation();
        // Have we been destroyed?
        if (loc == null)
            return;

        Activity activity = this;
        Intent intent = new Intent(activity, getLocationActivityClass());
        intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, loc);
        activity.startActivityForResult(intent, ACTIVITY_LOCATIONS);
    }

    protected abstract Class<? extends Activity> getLocationActivityClass();

    /**
     * Search key was pressed.
     */
    @Override
    public boolean onSearchRequested() {
        Location loc = locations.getLocation();
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
}
