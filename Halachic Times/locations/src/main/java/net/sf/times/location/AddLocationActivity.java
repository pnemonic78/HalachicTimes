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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import net.sf.times.location.text.LatitudeInputFilter;
import net.sf.times.location.text.LongitudeInputFilter;

/**
 * Add a location by specifying its coordinates.
 *
 * @author Moshe Waisberg
 */
public class AddLocationActivity extends Activity {

    /** The location parameter. */
    public static final String EXTRA_LOCATION = LocationManager.KEY_LOCATION_CHANGED;

    private Location location;
    private EditText latitudeDecimalEdit;
    private EditText longitudeDecimalEdit;
    private LocationFormatter formatter;

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

        setTheme(getThemeId());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.location_add);
        init();
    }

    protected int getThemeId() {
        return R.style.Theme_Base;
    }

    private void init() {
        latitudeDecimalEdit = (EditText) findViewById(R.id.latitude_decimal_edit);
        latitudeDecimalEdit.setFilters(new InputFilter[]{new LatitudeInputFilter()});
        longitudeDecimalEdit = (EditText) findViewById(R.id.longitude_decimal_edit);
        longitudeDecimalEdit.setFilters(new InputFilter[]{new LongitudeInputFilter()});

        if (location == null) {
            location = new Location(GeocoderBase.USER_PROVIDER);
        } else {
            latitudeDecimalEdit.setText(formatter.formatLatitudeDecimal(location.getLatitude()));
            latitudeDecimalEdit.setSelection(latitudeDecimalEdit.length());
            longitudeDecimalEdit.setText(formatter.formatLongitudeDecimal(location.getLongitude()));
            longitudeDecimalEdit.setSelection(longitudeDecimalEdit.length());
        }
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

        switch (id) {
            case android.R.id.home:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    finish();
                    return true;
                }
                break;
        }
        // Cannot use 'switch' here because library ids are not final.
        if (id == R.id.menu_location_cancel) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        if (id == R.id.menu_location_add) {
            CharSequence latitudeString = latitudeDecimalEdit.getText();
            if (TextUtils.isEmpty(latitudeString)) {
                return true;
            }
            double latitude = Double.parseDouble(latitudeString.toString());

            CharSequence longitudeString = longitudeDecimalEdit.getText();
            if (TextUtils.isEmpty(longitudeString)) {
                return true;
            }
            double longitude = Double.parseDouble(longitudeString.toString());

            location.setLatitude(latitude);
            location.setLongitude(longitude);

            Intent data = new Intent();
            data.putExtra(EXTRA_LOCATION, location);
            setResult(RESULT_OK, data);
            finish();
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
}
