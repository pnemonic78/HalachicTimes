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
package com.github.compass.bahai;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.github.times.compass.BaseCompassActivity;
import com.github.times.compass.preference.CompassPreferenceActivity;

/**
 * Show the direction in which to pray.
 * Points to the Points to to the tomb of Bahá'u'lláh in Bahjí in Israel.
 *
 * @author Moshe Waisberg
 */
public class CompassActivity extends BaseCompassActivity {

    /**
     * Constructs a new compass.
     */
    public CompassActivity() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compass, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_location:
                startLocations();
                return true;
            case R.id.menu_settings:
                startSettings();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Class<? extends Activity> getLocationActivityClass() {
        return LocationActivity.class;
    }

    protected void startSettings() {
        final Context context = this;
        startActivity(new Intent(context, CompassPreferenceActivity.class));
    }
}
