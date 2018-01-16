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
package net.sf.times.location;

import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Location;

import net.sf.database.CursorFilter;
import net.sf.util.LocaleUtils;

import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A class for handling geocoding and reverse geocoding. This geocoder uses the
 * Android SQLite database.
 *
 * @author Moshe Waisberg
 */
public class DatabaseGeocoder extends GeocoderBase {

    /** Database provider. */
    public static final String DB_PROVIDER = "db";

    private final AddressProvider provider;

    /**
     * Creates a new database geocoder.
     *
     * @param context
     *         the context.
     * @param provider
     *         the address provider.
     */
    public DatabaseGeocoder(Context context, AddressProvider provider) {
        this(context, LocaleUtils.getDefaultLocale(context), provider);
    }

    /**
     * Creates a new database geocoder.
     *
     * @param context
     *         the context.
     * @param locale
     *         the locale.
     * @param provider
     *         the address provider.
     */
    public DatabaseGeocoder(Context context, Locale locale, AddressProvider provider) {
        super(locale);
        this.provider = provider;
    }

    @Override
    public List<Address> getFromLocation(final double latitude, final double longitude, int maxResults) throws IOException {
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX)
            throw new IllegalArgumentException("longitude == " + longitude);

        CursorFilter filter = new CursorFilter() {

            private final float[] mDistance = new float[1];

            @Override
            public boolean accept(Cursor cursor) {
                double locationLatitude = cursor.getDouble(AddressProvider.INDEX_LOCATION_LATITUDE);
                double locationLongitude = cursor.getDouble(AddressProvider.INDEX_LOCATION_LONGITUDE);
                Location.distanceBetween(latitude, longitude, locationLatitude, locationLongitude, mDistance);
                if (mDistance[0] <= SAME_LOCATION)
                    return true;

                double addressLatitude = cursor.getDouble(AddressProvider.INDEX_LATITUDE);
                double addressLongitude = cursor.getDouble(AddressProvider.INDEX_LONGITUDE);
                Location.distanceBetween(latitude, longitude, addressLatitude, addressLongitude, mDistance);
                return (mDistance[0] <= SAME_LOCATION);
            }
        };
        List<ZmanimAddress> q = provider.query(filter);
        List<Address> addresses = new ArrayList<Address>(q);

        return addresses;
    }

    @Override
    protected DefaultHandler createAddressResponseHandler(List<Address> results, int maxResults, Locale locale) {
        return null;
    }

    @Override
    public ZmanimLocation getElevation(final double latitude, final double longitude) throws IOException {
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX)
            throw new IllegalArgumentException("longitude == " + longitude);

        CursorFilter filter = new CursorFilter() {
            private final float[] mDistance = new float[1];

            @Override
            public boolean accept(Cursor cursor) {
                double locationLatitude = cursor.getDouble(AddressProvider.INDEX_ELEVATIONS_LATITUDE);
                double locationLongitude = cursor.getDouble(AddressProvider.INDEX_ELEVATIONS_LONGITUDE);
                Location.distanceBetween(latitude, longitude, locationLatitude, locationLongitude, mDistance);
                return (mDistance[0] <= SAME_PLATEAU);
            }
        };
        List<ZmanimLocation> locations = provider.queryElevations(filter);

        int locationsCount = locations.size();
        if (locationsCount == 0)
            return null;

        float distance;
        float[] distanceLoc = new float[1];
        double d;
        double distancesSum = 0;
        int n = 0;
        double[] distances = new double[locationsCount];
        double[] elevations = new double[locationsCount];

        for (ZmanimLocation loc : locations) {
            Location.distanceBetween(latitude, longitude, loc.getLatitude(), loc.getLongitude(), distanceLoc);
            distance = distanceLoc[0];
            elevations[n] = loc.getAltitude();
            d = distance * distance;
            distances[n] = d;
            distancesSum += d;
            n++;
        }

        if ((n == 1) && (distanceLoc[0] <= SAME_CITY))
            return locations.get(0);
        if (n <= 1)
            return null;

        double weightSum = 0;
        for (int i = 0; i < n; i++) {
            weightSum += (1 - (distances[i] / distancesSum)) * elevations[i];
        }

        ZmanimLocation elevated = new ZmanimLocation(DB_PROVIDER);
        elevated.setTime(System.currentTimeMillis());
        elevated.setLatitude(latitude);
        elevated.setLongitude(longitude);
        elevated.setAltitude(weightSum / (n - 1));
        elevated.setId(-1);
        return elevated;
    }

    @Override
    protected DefaultHandler createElevationResponseHandler(List<ZmanimLocation> results) {
        return null;
    }

}
