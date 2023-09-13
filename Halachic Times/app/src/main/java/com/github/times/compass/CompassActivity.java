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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.github.app.LocaleCallbacks;
import com.github.app.LocaleHelper;
import com.github.preference.LocalePreferences;
import com.github.times.R;
import com.github.times.compass.preference.ThemeCompassPreferences;
import com.github.times.location.LocationActivity;
import com.github.times.preference.CompassPreferenceActivity;
import com.github.times.preference.ZmanimCompassPreferences;

/**
 * Show the direction in which to pray.
 * Points to the Holy of Holies in Jerusalem in Israel.
 *
 * @author Moshe Waisberg
 */
public class CompassActivity extends BaseCompassActivity {

    private LocaleCallbacks<LocalePreferences> localeCallbacks;

    @Override
    protected void attachBaseContext(Context newBase) {
        this.localeCallbacks = new LocaleHelper<>(newBase);
        Context context = localeCallbacks.attachBaseContext(newBase);
        super.attachBaseContext(context);

        applyOverrideConfiguration(context.getResources().getConfiguration());
    }

    @Override
    public void onPreCreate() {
        super.onPreCreate();
        localeCallbacks.onPreCreate(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compass, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.menu_location) {
            startLocations();
            return true;
        } else if (itemId == R.id.menu_settings) {
            startSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Class<? extends Activity> getLocationActivityClass() {
        return LocationActivity.class;
    }

    private void startSettings() {
        final Context context = this;
        startActivity(new Intent(context, CompassPreferenceActivity.class));
    }

    @Override
    protected ThemeCompassPreferences createCompassPreferences(Context context) {
        return new ZmanimCompassPreferences(context);
    }
}
