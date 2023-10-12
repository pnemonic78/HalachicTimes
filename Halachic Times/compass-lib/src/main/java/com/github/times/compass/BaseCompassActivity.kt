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
package com.github.times.compass;

import android.content.Context;
import android.content.res.TypedArray;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.github.app.SimpleThemeCallbacks;
import com.github.app.ThemeCallbacks;
import com.github.preference.ThemePreferences;
import com.github.times.compass.lib.R;
import com.github.times.compass.preference.CompassPreferences;
import com.github.times.compass.preference.SimpleCompassPreferences;
import com.github.times.compass.preference.ThemeCompassPreferences;
import com.github.times.location.LocatedActivity;

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
    private ThemeCompassPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context context = this;
        preferences = createCompassPreferences(context);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.compass);
        headerLocation = findViewById(com.github.times.location.R.id.coordinates);
        headerAddress = findViewById(com.github.times.location.R.id.address);
        fragment = (CompassFragment) getSupportFragmentManager().findFragmentById(R.id.compass);

        TextView summary = findViewById(R.id.summary);
        if (summary != null) {
            TypedArray a = context.obtainStyledAttributes(preferences.getCompassTheme(), R.styleable.CompassView);
            summary.setTextColor(a.getColorStateList(R.styleable.CompassView_compassColorTarget));
            a.recycle();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        TextView summary = findViewById(R.id.summary);
        if (summary != null) {
            summary.setVisibility(preferences.isSummariesVisible() ? View.VISIBLE : View.GONE);
        }
    }

    public CompassPreferences getCompassPreferences() {
        return preferences;
    }

    @Override
    protected Runnable createUpdateLocationRunnable(Location location) {
        return new Runnable() {
            @Override
            public void run() {
                bindHeader(location);

                CompassFragment c = fragment;
                if (c == null)
                    return;
                c.setLocation(location);
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compass, menu);
        return true;
    }

    protected ThemeCompassPreferences createCompassPreferences(Context context) {
        return new SimpleCompassPreferences(context);
    }

    @Override
    protected ThemeCallbacks<ThemePreferences> createThemeCallbacks(Context context) {
        return new SimpleThemeCallbacks<ThemePreferences>(context, preferences);
    }
}
