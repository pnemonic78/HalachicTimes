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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import net.sf.app.ThemedActivity;
import net.sf.times.location.text.LatitudeInputFilter;
import net.sf.times.location.text.LongitudeInputFilter;

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

    private Location location;
    private Spinner coordsFormatSpinner;
    private ViewSwitcher latitudeSwitcher;
    private EditText latitudeDecimalEdit;
    private EditText latitudeDegreesEdit;
    private EditText latitudeMinutesEdit;
    private EditText latitudeSecondsEdit;
    private Spinner latitudeDirection;
    private ViewSwitcher longitudeSwitcher;
    private EditText longitudeDecimalEdit;
    private EditText longitudeDegreesEdit;
    private EditText longitudeMinutesEdit;
    private EditText longitudeSecondsEdit;
    private Spinner longitudeDirection;
    private TextView addressView;
    private LocationFormatter formatter;
    /** Provider for locations. */
    private LocationsProvider locations;
    private ZmanimAddress address;
    private Runnable addressFormatRunnable;
    private Location locationForConvert;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = this;
        this.formatter = createLocationFormatter(context);

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
            setDecimalTexts(location.getLatitude(), latitudeDecimalEdit, latitudeDirection);
            setDecimalTexts(location.getLongitude(), longitudeDecimalEdit, longitudeDirection);
        }
    }

    private void initView() {
        coordsFormatSpinner = (Spinner) findViewById(R.id.coords_format);
        coordsFormatSpinner.setOnItemSelectedListener(this);

        latitudeSwitcher = (ViewSwitcher) findViewById(R.id.latitude_switch);
        latitudeDecimalEdit = (EditText) findViewById(R.id.latitude_decimal_edit);
        latitudeDecimalEdit.setFilters(new InputFilter[]{new LatitudeInputFilter(), new InputFilter.LengthFilter(10)});
        latitudeDegreesEdit = (EditText) findViewById(R.id.latitude_degrees_edit);
        latitudeMinutesEdit = (EditText) findViewById(R.id.latitude_minutes_edit);
        latitudeSecondsEdit = (EditText) findViewById(R.id.latitude_seconds_edit);
        latitudeDirection = (Spinner) findViewById(R.id.latitude_direction);

        longitudeSwitcher = (ViewSwitcher) findViewById(R.id.longitude_switch);
        longitudeDecimalEdit = (EditText) findViewById(R.id.longitude_decimal_edit);
        longitudeDecimalEdit.setFilters(new InputFilter[]{new LongitudeInputFilter(), new InputFilter.LengthFilter(10)});
        longitudeDegreesEdit = (EditText) findViewById(R.id.longitude_degrees_edit);
        longitudeMinutesEdit = (EditText) findViewById(R.id.longitude_minutes_edit);
        longitudeSecondsEdit = (EditText) findViewById(R.id.longitude_seconds_edit);
        longitudeDirection = (Spinner) findViewById(R.id.longitude_direction);

        addressView = (TextView) findViewById(R.id.address);
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

        if (coordsFormat == FORMAT_DECIMAL) {
            CharSequence latitudeString = latitudeDecimalEdit.getText();
            if (TextUtils.isEmpty(latitudeString)) {
                return false;
            }
            latitude = Double.parseDouble(latitudeString.toString());

            CharSequence longitudeString = longitudeDecimalEdit.getText();
            if (TextUtils.isEmpty(longitudeString)) {
                return false;
            }
            longitude = Double.parseDouble(longitudeString.toString());
        } else {
            CharSequence latitudeDegreesString = latitudeDegreesEdit.getText();
            if (TextUtils.isEmpty(latitudeDegreesString)) {
                return false;
            }
            int latitudeDegrees = Integer.parseInt(latitudeDegreesString.toString(), 10);

            CharSequence longitudeDegreesString = longitudeDegreesEdit.getText();
            if (TextUtils.isEmpty(longitudeDegreesString)) {
                return false;
            }
            int longitudeDegrees = Integer.parseInt(longitudeDegreesString.toString(), 10);

            CharSequence latitudeMinutesString = latitudeMinutesEdit.getText();
            int latitudeMinutes = TextUtils.isEmpty(latitudeMinutesString) ? 0 : Integer.parseInt(latitudeMinutesString.toString(), 10);

            CharSequence latitudeSecondsString = latitudeSecondsEdit.getText();
            double latitudeSeconds = TextUtils.isEmpty(latitudeSecondsString) ? 0 : Double.parseDouble(latitudeSecondsString.toString());

            CharSequence longitudeMinutesString = longitudeMinutesEdit.getText();
            int longitudeMinutes = TextUtils.isEmpty(longitudeMinutesString) ? 0 : Integer.parseInt(longitudeMinutesString.toString(), 10);

            CharSequence longitudeSecondsString = longitudeSecondsEdit.getText();
            double longitudeSeconds = TextUtils.isEmpty(longitudeSecondsString) ? 0 : Double.parseDouble(longitudeSecondsString.toString());

            latitude = ZmanimLocation.toDecimal(latitudeDegrees, latitudeMinutes, latitudeSeconds);
            longitude = ZmanimLocation.toDecimal(longitudeDegrees, longitudeMinutes, longitudeSeconds);
        }

        latitude = Math.abs(latitude) * ((latitudeDirection.getSelectedItemPosition() == POSITION_POSITIVE) ? DIRECTION_NORTH : DIRECTION_SOUTH);
        longitude = Math.abs(longitude) * ((longitudeDirection.getSelectedItemPosition() == POSITION_POSITIVE) ? DIRECTION_EAST : DIRECTION_WEST);

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
        locations.findAddress(location);
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
            setSexagesimalTexts(locationForConvert.getLatitude(), latitudeDegreesEdit, latitudeMinutesEdit, latitudeSecondsEdit, latitudeDirection);
            setSexagesimalTexts(locationForConvert.getLongitude(), longitudeDegreesEdit, longitudeMinutesEdit, longitudeSecondsEdit, longitudeDirection);
        }
    }

    private void setSexagesimalTexts(double coordinate, TextView degreesView, TextView minutesView, TextView secondsView, Spinner directionView) {
        int direction = (coordinate >= 0) ? POSITION_POSITIVE : POSITION_NEGATIVE;
        coordinate = Math.abs(coordinate);
        int degrees = (int) Math.floor(coordinate);
        coordinate -= degrees;
        coordinate *= 60.0;
        int minutes = (int) Math.floor(coordinate);
        coordinate -= minutes;
        coordinate *= 60.0;
        double seconds = coordinate;

        Locale locale = Locale.getDefault();
        degreesView.setText(String.format(locale, "%02d", degrees));
        minutesView.setText(String.format(locale, "%02d", minutes));
        secondsView.setText(String.format(locale, "%02.3f", seconds));
        directionView.setSelection(direction);
    }

    private void convertToDecimal() {
        if (locationForConvert == null) {
            locationForConvert = new Location(location);
        }
        if (saveLocation(locationForConvert, FORMAT_SEXAGESIMAL)) {
            setDecimalTexts(locationForConvert.getLatitude(), latitudeDecimalEdit, latitudeDirection);
            setDecimalTexts(locationForConvert.getLongitude(), longitudeDecimalEdit, longitudeDirection);
        }
    }

    private void setDecimalTexts(double coordinate, EditText decimalView, Spinner directionView) {
        int direction = (coordinate >= 0) ? POSITION_POSITIVE : POSITION_NEGATIVE;
        decimalView.setText(String.format(Locale.getDefault(), "%02.6f", Math.abs(coordinate)));
        decimalView.setSelection(0, decimalView.getText().length());
        directionView.setSelection(direction);
    }
}
