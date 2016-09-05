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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import net.sf.times.location.text.LatitudeInputFilter;
import net.sf.times.location.text.LongitudeInputFilter;

/**
 * Add a city to the list.
 *
 * @author Moshe Waisberg
 */
public class EditLocationDialog extends DialogFragment implements DialogInterface.OnClickListener {

    /** The location parameter. */
    public static final String EXTRA_LOCATION = LocationManager.KEY_LOCATION_CHANGED;

    public interface OnLocationEditListener {
        /**
         * A location was either added or modified.
         *
         * @param location
         *         the location.
         */
        void onLocationEdited(Location location);
    }

    private Location location;
    private OnLocationEditListener locationAddedListener;
    private EditText latitudeDecimalEdit;
    private EditText longitudeDecimalEdit;
    private LocationFormatter formatter;
    private int titleId;

    public void setOnLocationAddedListener(OnLocationEditListener listener) {
        this.locationAddedListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getActivity();
        this.formatter = createLocationFormatter(context);

        Bundle args = getArguments();
        if (args != null) {
            location = args.getParcelable(EXTRA_LOCATION);
        } else {
            location = null;
        }
        if (location != null) {
            titleId = R.string.title_dialog_edit_location;
        } else {
            titleId = R.string.title_dialog_add_location;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.location_add, null);
        init(view);

        Dialog dialog = new AlertDialog.Builder(context)
                .setTitle(titleId)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.cancel, this)
                .setView(view)
                .create();
        return dialog;
    }

    private void init(View view) {
        latitudeDecimalEdit = (EditText) view.findViewById(R.id.latitude_decimal_edit);
        latitudeDecimalEdit.setFilters(new InputFilter[]{new LatitudeInputFilter()});
        longitudeDecimalEdit = (EditText) view.findViewById(R.id.longitude_decimal_edit);
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
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            CharSequence latitudeString = latitudeDecimalEdit.getText();
            if (TextUtils.isEmpty(latitudeString)) {
                return;
            }
            double latitude = Double.parseDouble(latitudeString.toString());

            CharSequence longitudeString = longitudeDecimalEdit.getText();
            if (TextUtils.isEmpty(longitudeString)) {
                return;
            }
            double longitude = Double.parseDouble(longitudeString.toString());

            location.setLatitude(latitude);
            location.setLongitude(longitude);

            if (locationAddedListener != null) {
                locationAddedListener.onLocationEdited(location);
            }
        }
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
