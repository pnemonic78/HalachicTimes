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
package net.sf.compass;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import net.sf.times.location.LocationApplication;
import net.sf.times.location.LocationFormatter;

/**
 * Show the compass.
 *
 * @author Moshe Waisberg
 */
public class CompassFragment extends net.sf.times.compass.CompassFragment {

    private TextView degreesView;
    private LocationFormatter formatter;

    public CompassFragment() {
        setHoliest(Double.NaN, Double.NaN, 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocationApplication app = (LocationApplication) getActivity().getApplication();
        formatter = app.getLocations();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        degreesView = view.findViewById(R.id.degrees);
        compassView.setHoliest(Float.NaN);
        compassView.setTicks(true);

        Context context = view.getContext();
        TypedArray a = context.obtainStyledAttributes(preferences.getCompassTheme(), R.styleable.CompassView);
        if (a != null) {
            degreesView.setTextColor(a.getColorStateList(R.styleable.CompassView_compassColorLabel2));
            a.recycle();
        }
    }

    @Override
    protected void setAzimuth(float azimuth) {
        super.setAzimuth(azimuth);
        degreesView.setText(formatter.formatBearing(azimuth));
    }
}
