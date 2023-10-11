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

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

import com.github.app.SimpleThemeCallbacks;
import com.github.app.ThemeCallbacks;
import com.github.preference.ThemePreferences;
import com.github.text.method.RangeInputFilter;
import com.github.times.location.text.LatitudeInputFilter;
import com.github.times.location.text.LongitudeInputFilter;

import static com.github.times.location.GeocoderBase.USER_PROVIDER;

/**
 * Add a location by specifying its coordinates.
 *
 * @author Moshe Waisberg
 */
public class AddLocationActivity<P extends ThemePreferences> extends AppCompatActivity implements
        ThemeCallbacks<P>,
        AdapterView.OnItemSelectedListener,
        ZmanimLocationListener {

    /** The location parameter. */
    public static final String EXTRA_LOCATION = LocationManager.KEY_LOCATION_CHANGED;
    /** The location's latitude parameter. */
    public static final String EXTRA_LATITUDE = "latitude";
    /** The location's longitude parameter. */
    public static final String EXTRA_LONGITUDE = "longitude";

    /** The location state. */
    private static final String SAVE_STATE_LOCATION = EXTRA_LOCATION;
    /** The address state. */
    private static final String SAVE_STATE_ADDRESS = "address";

    private static final int FORMAT_DECIMAL = 0;
    private static final int FORMAT_SEXAGESIMAL = 1;

    private static final int POSITION_POSITIVE = 0;
    private static final int POSITION_NEGATIVE = 1;

    private static final int DIRECTION_NORTH = +1;
    private static final int DIRECTION_SOUTH = -1;
    private static final int DIRECTION_EAST = +1;
    private static final int DIRECTION_WEST = -1;

    private static final int DEGREES_MIN = 0;
    private static final int DECIMAL_MIN = 0;
    private static final int DECIMAL_MAX = 999999;
    private static final int MINUTES_MIN = 0;
    private static final int MINUTES_MAX = 59;
    private static final int SECONDS_MIN = 0;
    private static final int SECONDS_MAX = 59;
    private static final int MILLISECONDS_MIN = 0;
    private static final int MILLISECONDS_MAX = 9999;

    private ThemeCallbacks<P> themeCallbacks;
    private Location location;
    private Spinner coordsFormatSpinner;
    private ViewSwitcher latitudeSwitcher;
    private NumberPicker latitudeDegreesEdit;
    private EditText latitudeDecimalEdit;
    private NumberPicker latitudeMinutesEdit;
    private NumberPicker latitudeSecondsEdit;
    private EditText latitudeMillisecondsEdit;
    private Spinner latitudeDirection;
    private ViewSwitcher longitudeSwitcher;
    private NumberPicker longitudeDegreesEdit;
    private EditText longitudeDecimalEdit;
    private NumberPicker longitudeMinutesEdit;
    private NumberPicker longitudeSecondsEdit;
    private EditText longitudeMillisecondsEdit;
    private Spinner longitudeDirection;
    private TextView addressView;
    /** Provider for locations. */
    private LocationsProvider locations;
    private ZmanimAddress address;
    private Runnable addressFormatRunnable;
    private Location locationForConvert;
    /**
     * Formatter for for displaying the current value.
     */
    private NumberPicker.Formatter formatter;
    private boolean coordsFormatSpinnerSelectedFirst;

    public AddLocationActivity() {
        final NumberFormat formatter = NumberFormat.getIntegerInstance();
        formatter.setGroupingUsed(false);

        setFormatter(new NumberPicker.Formatter() {

            @Override
            public String format(int value) {
                return formatter.format(value);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        onPreCreate();
        super.onCreate(savedInstanceState);

        LocationApplication app = (LocationApplication) getApplication();
        locations = app.getLocations();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.location_add);
        initView();

        if (savedInstanceState == null) {
            Bundle args = getIntent().getExtras();
            Location location = this.location;
            if (args != null) {
                if (args.containsKey(EXTRA_LOCATION)) {
                    location = args.getParcelable(EXTRA_LOCATION);
                }
                if (location == null) {
                    location = new Location(USER_PROVIDER);

                    if (args.containsKey(EXTRA_LATITUDE)) {
                        location.setLatitude(args.getDouble(EXTRA_LATITUDE));
                    }
                    if (args.containsKey(EXTRA_LONGITUDE)) {
                        location.setLongitude(args.getDouble(EXTRA_LONGITUDE));
                    }
                }
            } else {
                location = new Location(USER_PROVIDER);
            }
            this.location = location;

            setDecimalTexts(location.getLatitude(), latitudeDegreesEdit, latitudeDecimalEdit, latitudeDirection);
            setDecimalTexts(location.getLongitude(), longitudeDegreesEdit, longitudeDecimalEdit, longitudeDirection);
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

    private void initView() {
        coordsFormatSpinner = findViewById(R.id.coords_format);
        coordsFormatSpinner.setOnItemSelectedListener(this);
        coordsFormatSpinnerSelectedFirst = true;

        latitudeSwitcher = findViewById(R.id.latitude_switch);
        latitudeDegreesEdit = findViewById(R.id.latitude_degrees_edit);
        latitudeDegreesEdit.setMinValue(DEGREES_MIN);
        latitudeDegreesEdit.setMaxValue((int) LatitudeInputFilter.LATITUDE_MAX);
        latitudeDecimalEdit = findViewById(R.id.latitude_decimal_edit);
        latitudeDecimalEdit.setFilters(new InputFilter[]{new RangeInputFilter(DECIMAL_MIN, DECIMAL_MAX)});
        latitudeMinutesEdit = findViewById(R.id.latitude_minutes_edit);
        latitudeMinutesEdit.setMinValue(MINUTES_MIN);
        latitudeMinutesEdit.setMaxValue(MINUTES_MAX);
        latitudeSecondsEdit = findViewById(R.id.latitude_seconds_edit);
        latitudeSecondsEdit.setMinValue(SECONDS_MIN);
        latitudeSecondsEdit.setMaxValue(SECONDS_MAX);
        latitudeMillisecondsEdit = findViewById(R.id.latitude_milliseconds_edit);
        latitudeMillisecondsEdit.setFilters(new InputFilter[]{new RangeInputFilter(MILLISECONDS_MIN, MILLISECONDS_MAX)});
        latitudeDirection = findViewById(R.id.latitude_direction);

        longitudeSwitcher = findViewById(R.id.longitude_switch);
        longitudeDegreesEdit = findViewById(R.id.longitude_degrees_edit);
        longitudeDegreesEdit.setMinValue(DEGREES_MIN);
        longitudeDegreesEdit.setMaxValue((int) LongitudeInputFilter.LONGITUDE_MAX);
        longitudeDecimalEdit = findViewById(R.id.longitude_decimal_edit);
        longitudeDecimalEdit.setFilters(new InputFilter[]{new RangeInputFilter(DECIMAL_MIN, DECIMAL_MAX)});
        longitudeMinutesEdit = findViewById(R.id.longitude_minutes_edit);
        longitudeMinutesEdit.setMinValue(MINUTES_MIN);
        longitudeMinutesEdit.setMaxValue(MINUTES_MAX);
        longitudeSecondsEdit = findViewById(R.id.longitude_seconds_edit);
        longitudeSecondsEdit.setMinValue(SECONDS_MIN);
        longitudeSecondsEdit.setMaxValue(SECONDS_MAX);
        longitudeMillisecondsEdit = findViewById(R.id.longitude_milliseconds_edit);
        longitudeMillisecondsEdit.setFilters(new InputFilter[]{new RangeInputFilter(MILLISECONDS_MIN, MILLISECONDS_MAX)});
        longitudeDirection = findViewById(R.id.longitude_direction);

        addressView = findViewById(R.id.address);

        updateNumberPickers();
    }

    @Override
    protected void onStart() {
        super.onStart();
        locations.start(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        locations.stop(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onAddressChanged(location, address);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.add_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }
        // Cannot use 'switch' here because library ids are not final.
        if (id == R.id.menu_location_cancel) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        if (id == R.id.menu_location_add) {
            if (saveLocation(location, coordsFormatSpinner.getSelectedItemPosition())) {
                Intent data = new Intent();
                data.putExtra(EXTRA_LOCATION, location);
                setResult(RESULT_OK, data);
                finish();
            }
            return true;
        }
        if (id == R.id.menu_location_show) {
            if (saveLocation(location, coordsFormatSpinner.getSelectedItemPosition())) {
                fetchAddress(location);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (coordsFormatSpinnerSelectedFirst) {
            coordsFormatSpinnerSelectedFirst = false;
            return;
        }
        if (position == FORMAT_DECIMAL) {
            convertToDecimal();
        } else {
            convertFromDecimal();
        }
        latitudeSwitcher.setDisplayedChild(position);
        longitudeSwitcher.setDisplayedChild(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private boolean saveLocation(Location location, int coordsFormat) {
        double latitude;
        double longitude;

        int latitudeDegrees = latitudeDegreesEdit.getValue();
        int longitudeDegrees = longitudeDegreesEdit.getValue();

        if (coordsFormat == FORMAT_DECIMAL) {
            CharSequence latitudeString = latitudeDecimalEdit.getText();
            latitude = latitudeDegrees + Double.parseDouble("0." + latitudeString.toString());

            CharSequence longitudeString = longitudeDecimalEdit.getText();
            longitude = longitudeDegrees + Double.parseDouble("0." + longitudeString.toString());
        } else {
            int latitudeMinutes = latitudeMinutesEdit.getValue();
            int latitudeSeconds = latitudeSecondsEdit.getValue();

            CharSequence latitudeMillisecondsString = latitudeMillisecondsEdit.getText();
            int latitudeMilliseconds = TextUtils.isEmpty(latitudeMillisecondsString) ? 0 : Integer.parseInt(latitudeMillisecondsString.toString(), 10);

            int longitudeMinutes = longitudeMinutesEdit.getValue();
            int longitudeSeconds = longitudeSecondsEdit.getValue();

            CharSequence longitudeMillisecondsString = longitudeMillisecondsEdit.getText();
            int longitudeMilliseconds = TextUtils.isEmpty(longitudeMillisecondsString) ? 0 : Integer.parseInt(longitudeMillisecondsString.toString(), 10);

            latitude = ZmanimLocation.toDecimal(latitudeDegrees, latitudeMinutes, Double.parseDouble(latitudeSeconds + "." + latitudeMilliseconds));
            longitude = ZmanimLocation.toDecimal(longitudeDegrees, longitudeMinutes, Double.parseDouble(longitudeSeconds + "." + longitudeMilliseconds));
        }

        latitude = Math.abs(latitude) * ((latitudeDirection.getSelectedItemPosition() == POSITION_POSITIVE) ? DIRECTION_NORTH : DIRECTION_SOUTH);
        if ((latitude < ZmanimLocation.LATITUDE_MIN) || (latitude > ZmanimLocation.LATITUDE_MAX)) {
            return false;
        }

        longitude = Math.abs(longitude) * ((longitudeDirection.getSelectedItemPosition() == POSITION_POSITIVE) ? DIRECTION_EAST : DIRECTION_WEST);
        if ((longitude < ZmanimLocation.LONGITUDE_MIN) || (longitude > ZmanimLocation.LONGITUDE_MAX)) {
            return false;
        }

        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return true;
    }

    /**
     * Get the locations provider.
     *
     * @return hte provider.
     */
    public LocationsProvider getLocations() {
        return locations;
    }

    private void fetchAddress(Location location) {
        addressView.setText(R.string.location_unknown);

        LocationsProvider locations = getLocations();
        locations.findAddress(location, false);
    }

    @Override
    public void onAddressChanged(Location location, ZmanimAddress address) {
        if ((location == null) || (address == null)) {
            return;
        }
        if ((location.getLatitude() != this.location.getLatitude()) || (location.getLongitude() != this.location.getLongitude())) {
            return;
        }
        this.address = address;

        if (addressView != null) {
            Runnable addressFormatRunnable = this.addressFormatRunnable;
            if (addressFormatRunnable == null) {
                addressFormatRunnable = new Runnable() {
                    @Override
                    public void run() {
                        addressView.setText(AddLocationActivity.this.address.getFormatted());
                    }
                };
                this.addressFormatRunnable = addressFormatRunnable;
            }
            runOnUiThread(addressFormatRunnable);
        }
    }

    @Override
    public void onElevationChanged(Location location) {
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_STATE_LOCATION, location);
        outState.putParcelable(SAVE_STATE_ADDRESS, address);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        location = savedInstanceState.getParcelable(SAVE_STATE_LOCATION);
        address = savedInstanceState.getParcelable(SAVE_STATE_ADDRESS);
    }

    private void convertFromDecimal() {
        Location locationForConvert = this.locationForConvert;
        if (locationForConvert == null) {
            locationForConvert = new Location(location);
            this.locationForConvert = locationForConvert;
        }
        if (saveLocation(locationForConvert, FORMAT_DECIMAL)) {
            setSexagesimalTexts(locationForConvert.getLatitude(), latitudeDegreesEdit, latitudeMinutesEdit, latitudeSecondsEdit, latitudeMillisecondsEdit, latitudeDirection);
            setSexagesimalTexts(locationForConvert.getLongitude(), longitudeDegreesEdit, longitudeMinutesEdit, longitudeSecondsEdit, longitudeMillisecondsEdit, longitudeDirection);
        }
    }

    private void setSexagesimalTexts(double coordinate, NumberPicker degreesView, NumberPicker minutesView, NumberPicker secondsView, TextView millisecondsView, Spinner directionView) {
        int direction = (coordinate >= 0) ? POSITION_POSITIVE : POSITION_NEGATIVE;
        coordinate = Math.abs(coordinate);
        int degrees = (int) Math.floor(coordinate);
        coordinate -= degrees;
        coordinate *= 60.0;
        int minutes = (int) Math.floor(coordinate);
        coordinate -= minutes;
        coordinate *= 60.0;
        int seconds = (int) Math.floor(coordinate);
        coordinate -= seconds;
        coordinate *= 10000; /* 0 - 9999 */
        int milliseconds = (int) Math.floor(coordinate);

        degreesView.setValue(degrees);
        minutesView.setValue(minutes);
        secondsView.setValue(seconds);
        millisecondsView.setText(formatNumber(milliseconds));
        directionView.setSelection(direction);
    }

    private void convertToDecimal() {
        Location locationForConvert = this.locationForConvert;
        if (locationForConvert == null) {
            locationForConvert = new Location(location);
            this.locationForConvert = locationForConvert;
        }
        if (saveLocation(locationForConvert, FORMAT_SEXAGESIMAL)) {
            setDecimalTexts(locationForConvert.getLatitude(), latitudeDegreesEdit, latitudeDecimalEdit, latitudeDirection);
            setDecimalTexts(locationForConvert.getLongitude(), longitudeDegreesEdit, longitudeDecimalEdit, longitudeDirection);
        }
    }

    private void setDecimalTexts(double coordinate, NumberPicker degreesView, EditText decimalView, Spinner directionView) {
        int direction = (coordinate >= 0) ? POSITION_POSITIVE : POSITION_NEGATIVE;
        coordinate = Math.abs(coordinate);
        int degrees = (int) Math.floor(coordinate);
        coordinate -= degrees;
        coordinate *= 1000000;/* 0 - 999999 */
        int milliseconds = (int) Math.floor(coordinate);

        degreesView.setValue(degrees);
        decimalView.setText(formatNumber(milliseconds));
        directionView.setSelection(direction);
    }

    /**
     * Set the formatter to be used for formatting the numbers.
     *
     * @param formatter
     *         The formatter object. If formatter is <code>null</code>,
     *         {@link String#valueOf(int)} will be used.
     */
    public void setFormatter(NumberPicker.Formatter formatter) {
        if (formatter == this.formatter) {
            return;
        }
        this.formatter = formatter;
        updateNumberPickers();
    }

    private String formatNumber(int value) {
        return (formatter != null) ? formatter.format(value) : formatNumberWithLocale(value);
    }

    static private String formatNumberWithLocale(int value) {
        return String.format(Locale.getDefault(), "%d", value);
    }

    private void updateNumberPickers() {
        if (latitudeDegreesEdit != null) {
            latitudeDegreesEdit.setFormatter(formatter);
        }
        if (latitudeMinutesEdit != null) {
            latitudeMinutesEdit.setFormatter(formatter);
        }
        if (latitudeSecondsEdit != null) {
            latitudeSecondsEdit.setFormatter(formatter);
        }
        if (longitudeDegreesEdit != null) {
            longitudeDegreesEdit.setFormatter(formatter);
        }
        if (longitudeMinutesEdit != null) {
            longitudeMinutesEdit.setFormatter(formatter);
        }
        if (longitudeSecondsEdit != null) {
            longitudeSecondsEdit.setFormatter(formatter);
        }
    }
}
