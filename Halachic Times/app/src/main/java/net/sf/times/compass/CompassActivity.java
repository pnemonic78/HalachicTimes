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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import net.sf.app.LocaleCallbacks;
import net.sf.app.LocaleHelper;
import net.sf.preference.LocalePreferences;
import net.sf.times.R;
import net.sf.times.location.LocationActivity;
import net.sf.times.preference.CompassPreferenceActivity;

import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localeCallbacks.onCreate(this);
        if (VERSION.SDK_INT < JELLY_BEAN) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compass, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (VERSION.SDK_INT < JELLY_BEAN) {
                    finish();
                    return true;
                }
                break;
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

    private void startSettings() {
        final Context context = this;
        startActivity(new Intent(context, CompassPreferenceActivity.class));
    }
}
