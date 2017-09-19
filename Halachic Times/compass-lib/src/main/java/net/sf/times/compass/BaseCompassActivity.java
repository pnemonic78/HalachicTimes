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
package net.sf.times.compass;

import android.content.Context;
import android.content.res.TypedArray;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import net.sf.preference.ThemePreferences;
import net.sf.times.compass.lib.R;
import net.sf.times.compass.preference.CompassPreferences;
import net.sf.times.compass.preference.SimpleCompassPreferences;
import net.sf.times.location.LocatedActivity;
import net.sf.times.location.LocationsProvider;

/**
 * Show the direction in which to pray. Points to the Holy of Holies in
 * Jerusalem in Israel.
 *
 * @author Moshe Waisberg
 */
public abstract class BaseCompassActivity extends LocatedActivity<ThemePreferences> {

    /** The main fragment. */
    private CompassFragment fragment;
    /** The preferences. */
    private CompassPreferences preferences;
    /** The location header location. */
    private TextView headerLocation;
    /** The location header for formatted address. */
    private TextView headerAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = this;
        preferences = new SimpleCompassPreferences(context);

        setContentView(R.layout.compass);
        headerLocation = findViewById(R.id.coordinates);
        headerAddress = findViewById(R.id.address);
        fragment = (CompassFragment) getFragmentManager().findFragmentById(R.id.compass);

        TextView summary = findViewById(android.R.id.summary);
        TypedArray a = context.obtainStyledAttributes(preferences.getTheme(), R.styleable.CompassTheme);
        summary.setTextColor(a.getColor(R.styleable.CompassTheme_compassColorTarget, summary.getSolidColor()));
        a.recycle();
    }

    @Override
    protected void onResume() {
        super.onResume();

        TextView summary = findViewById(android.R.id.summary);
        summary.setVisibility(preferences.isSummariesVisible() ? View.VISIBLE : View.GONE);
    }

    public CompassPreferences getCompassPreferences() {
        return preferences;
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
        locationLabel.setVisibility(getLocationPreferences().isCoordinatesVisible() ? View.VISIBLE : View.GONE);
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
