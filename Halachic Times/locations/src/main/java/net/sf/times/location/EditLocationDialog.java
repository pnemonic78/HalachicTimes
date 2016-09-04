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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

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

    protected static final double LATITUDE_MIN = ZmanimLocation.LATITUDE_MIN;
    protected static final double LATITUDE_MAX = ZmanimLocation.LATITUDE_MAX;
    protected static final double LONGITUDE_MIN = ZmanimLocation.LONGITUDE_MIN;
    protected static final double LONGITUDE_MAX = ZmanimLocation.LONGITUDE_MAX;

    private Location location;
    private OnLocationEditListener locationAddedListener;
    private EditText latitudeEdit;
    private EditText longitudeEdit;

    public void setOnLocationAddedListener(OnLocationEditListener listener) {
        this.locationAddedListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.location_add, null);
        init(view);

        Dialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.title_dialog_add_location)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.cancel, this)
                .setView(view)
                .create();
        return dialog;
    }

    private void init(View view) {
        latitudeEdit = (EditText) view.findViewById(R.id.latitude_edit);
        //TODO add latitude filter [LATITUDE_MIN,LATITUDE_MAX]
        longitudeEdit = (EditText) view.findViewById(R.id.longitude_edit);
        //TODO add longitude filter [LONGITUDE_MIN,LONGITUDE_MAX]

        Bundle args = getArguments();
        if (args != null) {
            location = args.getParcelable(EXTRA_LOCATION);
        }
        if (location == null) {
            location = new Location(GeocoderBase.USER_PROVIDER);
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            String latitudeString = latitudeEdit.getText().toString();
            if (TextUtils.isEmpty(latitudeString)) {
                return;
            }
            double latitude = Double.parseDouble(latitudeString);
            if ((latitude < LATITUDE_MIN) || (latitude > LATITUDE_MAX))
                return;

            String longitudeString = longitudeEdit.getText().toString();
            if (TextUtils.isEmpty(longitudeString)) {
                return;
            }
            double longitude = Double.parseDouble(longitudeString);
            if ((longitude < LONGITUDE_MIN) || (longitude > LONGITUDE_MAX))
                return;

            location.setLatitude(latitude);
            location.setLongitude(longitude);

            if (locationAddedListener != null) {
                locationAddedListener.onLocationEdited(location);
            }
        }
    }
}
