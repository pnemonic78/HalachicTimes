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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Location;
import android.provider.BaseColumns;
import android.util.Log;

import net.sf.database.CursorFilter;
import net.sf.util.LocaleUtils;

import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.sf.times.location.AddressOpenHelper.TABLE_ADDRESSES;
import static net.sf.times.location.AddressOpenHelper.TABLE_CITIES;
import static net.sf.times.location.AddressOpenHelper.TABLE_ELEVATIONS;

/**
 * A class for handling geocoding and reverse geocoding. This geocoder uses the
 * Android SQLite database.
 *
 * @author Moshe Waisberg
 */
public class DatabaseGeocoder extends GeocoderBase {

    private static final String TAG = "DatabaseGeocoder";

    /** Database  */
    public static final String DB_PROVIDER = "db";

    static final String[] PROJECTION_ADDRESS = {
            BaseColumns._ID,
            AddressColumns.LOCATION_LATITUDE,
            AddressColumns.LOCATION_LONGITUDE,
            AddressColumns.LATITUDE,
            AddressColumns.LONGITUDE,
            AddressColumns.ADDRESS,
            AddressColumns.LANGUAGE,
            AddressColumns.FAVORITE
    };
    static final int INDEX_ID = 0;
    static final int INDEX_LOCATION_LATITUDE = 1;
    static final int INDEX_LOCATION_LONGITUDE = 2;
    static final int INDEX_LATITUDE = 3;
    static final int INDEX_LONGITUDE = 4;
    static final int INDEX_ADDRESS = 5;
    static final int INDEX_LANGUAGE = 6;
    static final int INDEX_FAVORITE = 7;

    static final String[] PROJECTION_ELEVATION = {
            BaseColumns._ID,
            ElevationColumns.LATITUDE,
            ElevationColumns.LONGITUDE,
            ElevationColumns.ELEVATION,
            ElevationColumns.TIMESTAMP
    };
    static final int INDEX_ELEVATIONS_ID = 0;
    static final int INDEX_ELEVATIONS_LATITUDE = 1;
    static final int INDEX_ELEVATIONS_LONGITUDE = 2;
    static final int INDEX_ELEVATIONS_ELEVATION = 3;
    static final int INDEX_ELEVATIONS_TIMESTAMP = 4;

    static final String[] PROJECTION_CITY = {
            BaseColumns._ID,
            CitiesColumns.TIMESTAMP,
            CitiesColumns.FAVORITE};
    static final int INDEX_CITIES_ID = 0;
    static final int INDEX_CITIES_TIMESTAMP = 1;
    static final int INDEX_CITIES_FAVORITE = 2;

    static final String WHERE_ID = BaseColumns._ID + "=?";

    private final Context context;
    private SQLiteOpenHelper dbHelper;

    /**
     * Creates a new database geocoder.
     *
     * @param context
     *         the context.
     */
    public DatabaseGeocoder(Context context) {
        this(context, LocaleUtils.getDefaultLocale(context));
    }

    /**
     * Creates a new database geocoder.
     *
     * @param context
     *         the context.
     * @param locale
     *         the locale.
     */
    public DatabaseGeocoder(Context context, Locale locale) {
        super(locale);
        this.context = context;
    }

    private SQLiteOpenHelper getDatabaseHelper() {
        if (dbHelper == null) {
            synchronized (this) {
                dbHelper = new AddressOpenHelper(context);
            }
        }
        return dbHelper;
    }

    /**
     * Get the readable addresses database.
     *
     * @return the database - {@code null} otherwise.
     */
    public SQLiteDatabase getReadableDatabase() {
        try {
            return getDatabaseHelper().getReadableDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, "no readable db", e);
        }
        return null;
    }

    /**
     * Get the writable addresses database.
     *
     * @return the database - {@code null} otherwise.
     */
    public SQLiteDatabase getWritableDatabase() {
        try {
            return getDatabaseHelper().getWritableDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, "no writable db", e);
        }
        return null;
    }

    /** Close database resources. */
    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
            dbHelper = null;
        }
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
                double locationLatitude = cursor.getDouble(INDEX_LOCATION_LATITUDE);
                double locationLongitude = cursor.getDouble(INDEX_LOCATION_LONGITUDE);
                Location.distanceBetween(latitude, longitude, locationLatitude, locationLongitude, mDistance);
                if (mDistance[0] <= SAME_LOCATION)
                    return true;

                double addressLatitude = cursor.getDouble(INDEX_LATITUDE);
                double addressLongitude = cursor.getDouble(INDEX_LONGITUDE);
                Location.distanceBetween(latitude, longitude, addressLatitude, addressLongitude, mDistance);
                return (mDistance[0] <= SAME_LOCATION);
            }
        };
        List<ZmanimAddress> q = queryAddresses(filter);
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
                double locationLatitude = cursor.getDouble(INDEX_ELEVATIONS_LATITUDE);
                double locationLongitude = cursor.getDouble(INDEX_ELEVATIONS_LONGITUDE);
                Location.distanceBetween(latitude, longitude, locationLatitude, locationLongitude, mDistance);
                return (mDistance[0] <= SAME_PLATEAU);
            }
        };
        List<ZmanimLocation> locations = queryElevations(filter);

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

    /**
     * Fetch addresses from the database.
     *
     * @param filter
     *         a cursor filter.
     * @return the list of addresses.
     */
    public List<ZmanimAddress> queryAddresses(CursorFilter filter) {
        final String language = locale.getLanguage();
        final String country = locale.getCountry();

        List<ZmanimAddress> addresses = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        if (db == null)
            return addresses;
        Cursor cursor = db.query(TABLE_ADDRESSES, PROJECTION_ADDRESS, null, null, null, null, null);
        if ((cursor == null) || cursor.isClosed()) {
            return addresses;
        }

        try {
            if (cursor.moveToFirst()) {
                long id;
                double addressLatitude;
                double addressLongitude;
                String formatted;
                String locationLanguage;
                Locale locale;
                ZmanimAddress address;
                boolean favorite;

                do {
                    locationLanguage = cursor.getString(INDEX_LANGUAGE);
                    if ((locationLanguage == null) || locationLanguage.equals(language)) {
                        if ((filter != null) && !filter.accept(cursor))
                            continue;

                        addressLatitude = cursor.getDouble(INDEX_LATITUDE);
                        addressLongitude = cursor.getDouble(INDEX_LONGITUDE);
                        id = cursor.getLong(INDEX_ID);
                        formatted = cursor.getString(INDEX_ADDRESS);
                        favorite = cursor.getShort(INDEX_FAVORITE) != 0;
                        if (locationLanguage == null)
                            locale = this.locale;
                        else
                            locale = new Locale(locationLanguage, country);

                        address = new ZmanimAddress(locale);
                        address.setFormatted(formatted);
                        address.setId(id);
                        address.setLatitude(addressLatitude);
                        address.setLongitude(addressLongitude);
                        address.setFavorite(favorite);
                        addresses.add(address);
                    }
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException se) {
            Log.e(TAG, "Query addresses: " + se.getLocalizedMessage(), se);
        } finally {
            cursor.close();
        }

        return addresses;
    }

    /**
     * Delete the list of cached addresses.
     */
    public void deleteAddresses() {
        SQLiteDatabase db = getWritableDatabase();
        if (db == null)
            return;
        db.delete(TABLE_ADDRESSES, null, null);
    }

    /**
     * Fetch elevations from the database.
     *
     * @param filter
     *         a cursor filter.
     * @return the list of locations with elevations.
     */
    public List<ZmanimLocation> queryElevations(CursorFilter filter) {
        List<ZmanimLocation> locations = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        if (db == null)
            return locations;
        Cursor cursor = db.query(TABLE_ELEVATIONS, PROJECTION_ELEVATION, null, null, null, null, null);
        if ((cursor == null) || cursor.isClosed()) {
            return locations;
        }

        try {
            if (cursor.moveToFirst()) {
                ZmanimLocation location;

                do {
                    if ((filter != null) && !filter.accept(cursor))
                        continue;

                    location = new ZmanimLocation(DB_PROVIDER);
                    location.setId(cursor.getLong(INDEX_ELEVATIONS_ID));
                    location.setLatitude(cursor.getDouble(INDEX_ELEVATIONS_LATITUDE));
                    location.setLongitude(cursor.getDouble(INDEX_ELEVATIONS_LONGITUDE));
                    location.setAltitude(cursor.getDouble(INDEX_ELEVATIONS_ELEVATION));
                    location.setTime(cursor.getLong(INDEX_ELEVATIONS_TIMESTAMP));
                    locations.add(location);
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException se) {
            Log.e(TAG, "Query elevations: " + se.getLocalizedMessage(), se);
        } finally {
            cursor.close();
        }

        return locations;
    }

    /**
     * Delete the list of cached elevations.
     */
    public void deleteElevations() {
        SQLiteDatabase db = getWritableDatabase();
        if (db == null)
            return;
        db.delete(TABLE_ELEVATIONS, null, null);
    }

    /**
     * Delete the list of cached cities and re-populate.
     */
    public void deleteCities() {
        SQLiteDatabase db = getWritableDatabase();
        if (db == null)
            return;
        db.delete(TABLE_CITIES, null, null);
    }

}
