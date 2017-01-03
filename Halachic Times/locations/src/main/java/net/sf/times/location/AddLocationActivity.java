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
package net.sf.times.location;

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

import net.sf.app.ThemedActivity;
import net.sf.text.method.RangeInputFilter;
import net.sf.times.location.text.LatitudeInputFilter;
import net.sf.times.location.text.LongitudeInputFilter;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Add a location by specifying its coordinates.
 *
 * @author Moshe Waisberg
 */
public class AddLocationActivity extends ThemedActivity implements
        AdapterView.OnItemSelectedListener,
        ZmanimLocationListener {

    /** The location parameter. */
    public static final String EXTRA_LOCATION = LocationManager.KEY_LOCATION_CHANGED;

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

    public AddLocationActivity() {
        setFormatter(new NumberPicker.Formatter() {

            private final NumberFormat formatter = NumberFormat.getIntegerInstance();

            @Override
            public String format(int value) {
                return formatter.format(value);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocationApplication app = (LocationApplication) getApplication();
        locations = app.getLocations();

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.location_add);
        initView();

        Bundle args = getIntent().getExtras();
        if (args != null) {
            location = args.getParcelable(EXTRA_LOCATION);
        } else {
            location = null;
        }
        if (location == null) {
            location = new Location(GeocoderBase.USER_PROVIDER);
        } else {
            setDecimalTexts(location.getLatitude(), latitudeDegreesEdit, latitudeDecimalEdit, latitudeDirection);
            setDecimalTexts(location.getLongitude(), longitudeDegreesEdit, longitudeDecimalEdit, longitudeDirection);
        }
    }

    private void initView() {
        coordsFormatSpinner = (Spinner) findViewById(R.id.coords_format);
        coordsFormatSpinner.setOnItemSelectedListener(this);

        latitudeSwitcher = (ViewSwitcher) findViewById(R.id.latitude_switch);
        latitudeDegreesEdit = (NumberPicker) findViewById(R.id.latitude_degrees_edit);
        latitudeDegreesEdit.setMinValue(DEGREES_MIN);
        latitudeDegreesEdit.setMaxValue((int) LatitudeInputFilter.LATITUDE_MAX);
        latitudeDecimalEdit = (EditText) findViewById(R.id.latitude_decimal_edit);
        latitudeDecimalEdit.setFilters(new InputFilter[]{new RangeInputFilter(DECIMAL_MIN, DECIMAL_MAX)});
        latitudeMinutesEdit = (NumberPicker) findViewById(R.id.latitude_minutes_edit);
        latitudeMinutesEdit.setMinValue(MINUTES_MIN);
        latitudeMinutesEdit.setMaxValue(MINUTES_MAX);
        latitudeSecondsEdit = (NumberPicker) findViewById(R.id.latitude_seconds_edit);
        latitudeSecondsEdit.setMinValue(SECONDS_MIN);
        latitudeSecondsEdit.setMaxValue(SECONDS_MAX);
        latitudeMillisecondsEdit = (EditText) findViewById(R.id.latitude_milliseconds_edit);
        latitudeMillisecondsEdit.setFilters(new InputFilter[]{new RangeInputFilter(MILLISECONDS_MIN, MILLISECONDS_MAX)});
        latitudeDirection = (Spinner) findViewById(R.id.latitude_direction);

        longitudeSwitcher = (ViewSwitcher) findViewById(R.id.longitude_switch);
        longitudeDegreesEdit = (NumberPicker) findViewById(R.id.longitude_degrees_edit);
        longitudeDegreesEdit.setMinValue(DEGREES_MIN);
        longitudeDegreesEdit.setMaxValue((int) LongitudeInputFilter.LONGITUDE_MAX);
        longitudeDecimalEdit = (EditText) findViewById(R.id.longitude_decimal_edit);
        longitudeDecimalEdit.setFilters(new InputFilter[]{new RangeInputFilter(DECIMAL_MIN, DECIMAL_MAX)});
        longitudeMinutesEdit = (NumberPicker) findViewById(R.id.longitude_minutes_edit);
        longitudeMinutesEdit.setMinValue(MINUTES_MIN);
        longitudeMinutesEdit.setMaxValue(MINUTES_MAX);
        longitudeSecondsEdit = (NumberPicker) findViewById(R.id.longitude_seconds_edit);
        longitudeSecondsEdit.setMinValue(SECONDS_MIN);
        longitudeSecondsEdit.setMaxValue(SECONDS_MAX);
        longitudeMillisecondsEdit = (EditText) findViewById(R.id.longitude_milliseconds_edit);
        longitudeMillisecondsEdit.setFilters(new InputFilter[]{new RangeInputFilter(MILLISECONDS_MIN, MILLISECONDS_MAX)});
        longitudeDirection = (Spinner) findViewById(R.id.longitude_direction);

        addressView = (TextView) findViewById(R.id.address);

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

    /**
     * Create a location formatter helper.
     *
     * @param context
     *         the context.
     * @return the formatter.
     */
    protected LocationFormatter createLocationFormatter(Context context) {
        return new SimpleLocationFormatter(context);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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
            if (addressFormatRunnable == null) {
                addressFormatRunnable = new Runnable() {
                    @Override
                    public void run() {
                        addressView.setText(AddLocationActivity.this.address.getFormatted());
                    }
                };
            }
            runOnUiThread(addressFormatRunnable);
        }
    }

    @Override
    public void onElevationChanged(Location location) {
    }

    @Override
    public boolean isPassive() {
        return true;
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
        if (locationForConvert == null) {
            locationForConvert = new Location(location);
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
        if (locationForConvert == null) {
            locationForConvert = new Location(location);
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
