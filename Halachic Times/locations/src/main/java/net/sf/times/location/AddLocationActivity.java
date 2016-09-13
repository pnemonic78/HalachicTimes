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

    private static final int POS_DECIMAL = 0;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = this;
        this.formatter = createLocationFormatter(context);

        Bundle args = getIntent().getExtras();
        if (args != null) {
            location = args.getParcelable(EXTRA_LOCATION);
        } else {
            location = null;
        }

        LocationApplication app = (LocationApplication) getApplication();
        locations = app.getLocations();

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.location_add);
        initView();
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

        if (location == null) {
            location = new Location(GeocoderBase.USER_PROVIDER);
        } else {
            latitudeDecimalEdit.setText(formatter.formatLatitudeDecimal(location.getLatitude()));
            latitudeDecimalEdit.setSelection(0, latitudeDecimalEdit.length());
            longitudeDecimalEdit.setText(formatter.formatLongitudeDecimal(location.getLongitude()));
            longitudeDecimalEdit.setSelection(0, longitudeDecimalEdit.length());
        }
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
            if (saveLocation(location)) {
                Intent data = new Intent();
                data.putExtra(EXTRA_LOCATION, location);
                setResult(RESULT_OK, data);
                finish();
            }
            return true;
        }
        if (id == R.id.menu_location_show) {
            if (saveLocation(location)) {
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
        latitudeSwitcher.setDisplayedChild(position);
        longitudeSwitcher.setDisplayedChild(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private boolean saveLocation(Location location) {
        double latitude = 0;
        double longitude = 0;

        if (coordsFormatSpinner.getSelectedItemPosition() == POS_DECIMAL) {
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
            int latitudeSeconds = TextUtils.isEmpty(latitudeSecondsString) ? 0 : Integer.parseInt(latitudeSecondsString.toString(), 10);

            CharSequence longitudeMinutesString = longitudeMinutesEdit.getText();
            int longitudeMinutes = TextUtils.isEmpty(longitudeMinutesString) ? 0 : Integer.parseInt(longitudeMinutesString.toString(), 10);

            CharSequence longitudeSecondsString = longitudeSecondsEdit.getText();
            int longitudeSeconds = TextUtils.isEmpty(longitudeSecondsString) ? 0 : Integer.parseInt(longitudeSecondsString.toString(), 10);

            latitude = ZmanimLocation.toDecimal(latitudeDegrees, latitudeMinutes, latitudeSeconds);
            longitude = ZmanimLocation.toDecimal(longitudeDegrees, longitudeMinutes, longitudeSeconds);
        }

        // North = +1; South = -1
        latitude = Math.abs(latitude) * ((latitudeDirection.getSelectedItemPosition() == 0) ? +1 : -1);
        // East = +1; West = -1
        longitude = Math.abs(longitude) * ((longitudeDirection.getSelectedItemPosition() == 0) ? +1 : -1);

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
    public void onAddressChanged(Location location, final ZmanimAddress address) {
        if ((location == null) || (address == null)) {
            return;
        }
        if ((location.getLatitude() != this.location.getLatitude()) || (location.getLongitude() != this.location.getLongitude())) {
            return;
        }

        if (addressView != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addressView.setText(address.getFormatted());
                }
            });
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
}
