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

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import net.sf.times.compass.lib.R;
import net.sf.times.compass.preference.CompassSettings;
import net.sf.times.location.LocatedActivity;
import net.sf.times.location.LocationsProvider;

/**
 * Show the direction in which to pray. Points to the Holy of Holies in
 * Jerusalem in Israel.
 *
 * @author Moshe Waisberg
 */
public abstract class BaseCompassActivity extends LocatedActivity {

    /** The main fragment. */
    private CompassFragment fragment;
    /** The settings and preferences. */
    private CompassSettings settings;
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
    }

    @Override
    protected Runnable createUpdateLocationRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                // Have we been destroyed?
                Location loc = getAddressLocation();
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

    /** Populate the header item. */
    private void populateHeader() {
        TextView locationLabel = headerLocation;
        TextView addressLabel = headerAddress;
        // Have we been destroyed?
        if ((locationLabel == null) || (addressLabel == null))
            return;
        // Have we been destroyed?
        LocationsProvider locations = getLocations();
        Location addressLocation = getAddressLocation();
        Location loc = (addressLocation == null) ? locations.getLocation() : addressLocation;
        if (loc == null)
            return;

        final CharSequence locationText = locations.formatCoordinates(loc);
        final CharSequence locationName = formatAddress(getAddress());

        // Update the location.
        locationLabel.setText(locationText);
        locationLabel.setVisibility(settings.isCoordinates() ? View.VISIBLE : View.GONE);
        addressLabel.setText(locationName);
    }

    @Override
    protected Runnable createPopulateHeaderRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                populateHeader();
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compass, menu);
        return true;
    }
}
