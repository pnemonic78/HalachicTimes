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
package com.github.times.location.impl;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.github.database.CursorFilter;
import com.github.times.location.City;
import com.github.times.location.Country;
import com.github.times.location.GeocoderBase;
import com.github.times.location.ZmanimAddress;
import com.github.times.location.ZmanimLocation;
import com.github.times.location.provider.LocationContract.AddressColumns;
import com.github.times.location.provider.LocationContract.CityColumns;
import com.github.times.location.provider.LocationContract.ElevationColumns;
import com.github.util.LocaleUtils;

import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.github.times.location.provider.LocationContract.Addresses;
import static com.github.times.location.provider.LocationContract.Cities;
import static com.github.times.location.provider.LocationContract.Elevations;

/**
 * A class for handling geocoding and reverse geocoding. This geocoder uses the
 * Android SQLite database.
 *
 * @author Moshe Waisberg
 */
public class DatabaseGeocoder extends GeocoderBase {

    private static final String TAG = "DatabaseGeocoder";

    /** Database */
    private static final String DB_PROVIDER = "db";

    private static final String[] PROJECTION_ADDRESS = {
            BaseColumns._ID,
            AddressColumns.LOCATION_LATITUDE,
            AddressColumns.LOCATION_LONGITUDE,
            AddressColumns.LATITUDE,
            AddressColumns.LONGITUDE,
            AddressColumns.ADDRESS,
            AddressColumns.LANGUAGE,
            AddressColumns.FAVORITE
    };
    private static final int INDEX_ADDRESS_ID = 0;
    private static final int INDEX_ADDRESS_LOCATION_LATITUDE = 1;
    private static final int INDEX_ADDRESS_LOCATION_LONGITUDE = 2;
    private static final int INDEX_ADDRESS_LATITUDE = 3;
    private static final int INDEX_ADDRESS_LONGITUDE = 4;
    private static final int INDEX_ADDRESS_ADDRESS = 5;
    private static final int INDEX_ADDRESS_LANGUAGE = 6;
    private static final int INDEX_ADDRESS_FAVORITE = 7;

    private static final String[] PROJECTION_ELEVATION = {
            BaseColumns._ID,
            ElevationColumns.LATITUDE,
            ElevationColumns.LONGITUDE,
            ElevationColumns.ELEVATION,
            ElevationColumns.TIMESTAMP
    };
    private static final int INDEX_ELEVATION_ID = 0;
    private static final int INDEX_ELEVATION_LATITUDE = 1;
    private static final int INDEX_ELEVATION_LONGITUDE = 2;
    private static final int INDEX_ELEVATION_ELEVATION = 3;
    private static final int INDEX_ELEVATION_TIMESTAMP = 4;

    private static final String[] PROJECTION_CITY = {
            BaseColumns._ID,
            CityColumns.TIMESTAMP,
            CityColumns.FAVORITE};
    private static final int INDEX_CITY_ID = 0;
    private static final int INDEX_CITY_TIMESTAMP = 1;
    private static final int INDEX_CITY_FAVORITE = 2;

    private final Context context;

    /**
     * Creates a new database geocoder.
     *
     * @param context the context.
     */
    public DatabaseGeocoder(Context context) {
        this(context, LocaleUtils.getDefaultLocale(context));
    }

    /**
     * Creates a new database geocoder.
     *
     * @param context the context.
     * @param locale  the locale.
     */
    public DatabaseGeocoder(Context context, Locale locale) {
        super(locale);
        this.context = context;
    }

    /** Close database resources. */
    public void close() {
    }

    @Override
    public List<Address> getFromLocation(final double latitude, final double longitude, int maxResults) throws IOException {
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX)
            throw new IllegalArgumentException("longitude == " + longitude);

        final CursorFilter filter = new CursorFilter() {

            private final float[] distance = new float[1];

            @Override
            public boolean accept(Cursor cursor) {
                double locationLatitude = cursor.getDouble(INDEX_ADDRESS_LOCATION_LATITUDE);
                double locationLongitude = cursor.getDouble(INDEX_ADDRESS_LOCATION_LONGITUDE);
                Location.distanceBetween(latitude, longitude, locationLatitude, locationLongitude, distance);
                if (distance[0] <= SAME_LOCATION) {
                    return true;
                }

                double addressLatitude = cursor.getDouble(INDEX_ADDRESS_LATITUDE);
                double addressLongitude = cursor.getDouble(INDEX_ADDRESS_LONGITUDE);
                Location.distanceBetween(latitude, longitude, addressLatitude, addressLongitude, distance);
                return (distance[0] <= SAME_LOCATION);
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

        final CursorFilter filter = new CursorFilter() {

            private final float[] distance = new float[1];

            @Override
            public boolean accept(Cursor cursor) {
                double locationLatitude = cursor.getDouble(INDEX_ELEVATION_LATITUDE);
                double locationLongitude = cursor.getDouble(INDEX_ELEVATION_LONGITUDE);
                Location.distanceBetween(latitude, longitude, locationLatitude, locationLongitude, distance);
                return (distance[0] <= SAME_PLATEAU);
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
     * Format the address.
     *
     * @param a the address.
     * @return the formatted address name.
     */
    protected static CharSequence formatAddress(ZmanimAddress a) {
        return a.getFormatted();
    }

    /**
     * Fetch addresses from the database.
     *
     * @param filter a cursor filter.
     * @return the list of addresses.
     */
    public List<ZmanimAddress> queryAddresses(CursorFilter filter) {
        final String language = locale.getLanguage();
        final String country = locale.getCountry();

        List<ZmanimAddress> addresses = new ArrayList<>();
        String selection = "(" + AddressColumns.LANGUAGE + " IS NULL) OR (" + AddressColumns.LANGUAGE + "=?)";
        String[] selectionArgs = {language};
        Cursor cursor = context.getContentResolver().query(Addresses.CONTENT_URI(context), PROJECTION_ADDRESS, selection, selectionArgs, null);
        if ((cursor == null) || cursor.isClosed()) {
            return addresses;
        }

        try {
            if (cursor.moveToFirst()) {
                String locationLanguage;
                Locale locale;

                do {
                    if ((filter != null) && !filter.accept(cursor)) {
                        continue;
                    }

                    locationLanguage = cursor.getString(INDEX_ADDRESS_LANGUAGE);
                    if (locationLanguage == null) {
                        locale = this.locale;
                    } else {
                        locale = new Locale(locationLanguage, country);
                    }

                    ZmanimAddress address = new ZmanimAddress(locale);
                    address.setFormatted(cursor.getString(INDEX_ADDRESS_ADDRESS));
                    address.setId(cursor.getLong(INDEX_ADDRESS_ID));
                    address.setLatitude(cursor.getDouble(INDEX_ADDRESS_LATITUDE));
                    address.setLongitude(cursor.getDouble(INDEX_ADDRESS_LONGITUDE));
                    address.setFavorite(cursor.getInt(INDEX_ADDRESS_FAVORITE) != 0);
                    addresses.add(address);
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
     * Insert or update the address in the local database. The local database is
     * supposed to reduce redundant network requests.
     *
     * @param location the location.
     * @param address  the address.
     */
    public void insertOrUpdateAddress(Location location, ZmanimAddress address) {
        if (address == null)
            return;
        long id = address.getId();
        if (id < 0L)
            return;
        // Cities have their own table.
        if (address instanceof City) {
            insertOrUpdateCity((City) address);
            return;
        }
        // Nothing to save.
        if (address instanceof Country) {
            return;
        }
        boolean insert = id == 0L;

        ContentValues values = new ContentValues();
        if (insert) {
            if (location == null) {
                values.put(AddressColumns.LOCATION_LATITUDE, address.getLatitude());
                values.put(AddressColumns.LOCATION_LONGITUDE, address.getLongitude());
            } else {
                values.put(AddressColumns.LOCATION_LATITUDE, location.getLatitude());
                values.put(AddressColumns.LOCATION_LONGITUDE, location.getLongitude());
            }
        }
        values.put(AddressColumns.ADDRESS, formatAddress(address).toString());
        values.put(AddressColumns.LANGUAGE, address.getLocale().getLanguage());
        values.put(AddressColumns.LATITUDE, address.getLatitude());
        values.put(AddressColumns.LONGITUDE, address.getLongitude());
        values.put(AddressColumns.TIMESTAMP, System.currentTimeMillis());
        values.put(AddressColumns.FAVORITE, address.isFavorite());

        final ContentResolver resolver = context.getContentResolver();
        try {
            if (insert) {
                Uri uri = resolver.insert(Addresses.CONTENT_URI(context), values);
                if (uri != null) {
                    id = ContentUris.parseId(uri);
                    if (id > 0L) {
                        address.setId(id);
                    }
                }
            } else {
                Uri uri = ContentUris.withAppendedId(Addresses.CONTENT_URI(context), id);
                resolver.update(uri, values, null, null);
            }
        } catch (Exception e) {
            // Caused by: java.lang.IllegalArgumentException: Unknown URL content://net.sf.times.debug.locations/address
            Log.e(TAG, "Error inserting address: " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Delete the list of cached addresses.
     */
    public void deleteAddresses() {
        context.getContentResolver().delete(Addresses.CONTENT_URI(context), null, null);
    }

    /**
     * Fetch elevations from the database.
     *
     * @param filter a cursor filter.
     * @return the list of locations with elevations.
     */
    public List<ZmanimLocation> queryElevations(CursorFilter filter) {
        List<ZmanimLocation> locations = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(Elevations.CONTENT_URI(context), PROJECTION_ELEVATION, null, null, null);
        if ((cursor == null) || cursor.isClosed()) {
            return locations;
        }

        try {
            if (cursor.moveToFirst()) {
                do {
                    if ((filter != null) && !filter.accept(cursor)) {
                        continue;
                    }

                    ZmanimLocation location = new ZmanimLocation(DB_PROVIDER);
                    location.setId(cursor.getLong(INDEX_ELEVATION_ID));
                    location.setLatitude(cursor.getDouble(INDEX_ELEVATION_LATITUDE));
                    location.setLongitude(cursor.getDouble(INDEX_ELEVATION_LONGITUDE));
                    location.setAltitude(cursor.getDouble(INDEX_ELEVATION_ELEVATION));
                    location.setTime(cursor.getLong(INDEX_ELEVATION_TIMESTAMP));
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
     * Insert or update the location with elevation in the local database. The
     * local database is supposed to reduce redundant network requests.
     *
     * @param location the location.
     */
    public void insertOrUpdateElevation(ZmanimLocation location) {
        if ((location == null) || !location.hasAltitude())
            return;
        long id = location.getId();
        if (id < 0L)
            return;

        ContentValues values = new ContentValues();
        values.put(ElevationColumns.LATITUDE, location.getLatitude());
        values.put(ElevationColumns.LONGITUDE, location.getLongitude());
        values.put(ElevationColumns.ELEVATION, location.getAltitude());
        values.put(ElevationColumns.TIMESTAMP, System.currentTimeMillis());

        final ContentResolver resolver = context.getContentResolver();
        final Uri contentUri = Elevations.CONTENT_URI(context);
        try {
            if (id == 0L) {
                Uri uri = resolver.insert(contentUri, values);
                if (uri != null) {
                    id = ContentUris.parseId(uri);
                    if (id > 0L) {
                        location.setId(id);
                    }
                }
            } else {
                Uri uri = ContentUris.withAppendedId(contentUri, id);
                resolver.update(uri, values, null, null);
            }
        } catch (Exception e) {
            // Caused by: java.lang.IllegalArgumentException: Unknown URL content://net.sf.times.debug.locations/elevation
            Log.e(TAG, "Error inserting elevation" + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Delete the list of cached elevations.
     */
    public void deleteElevations() {
        context.getContentResolver().delete(Elevations.CONTENT_URI(context), null, null);
    }

    /**
     * Fetch cities from the database.
     *
     * @param filter a cursor filter.
     * @return the list of cities.
     */
    public List<City> queryCities(CursorFilter filter) {
        List<City> cities = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(Cities.CONTENT_URI(context), PROJECTION_CITY, null, null, null);
        if ((cursor == null) || cursor.isClosed()) {
            return cities;
        }

        try {
            if (cursor.moveToFirst()) {
                do {
                    if ((filter != null) && !filter.accept(cursor)) {
                        continue;
                    }

                    City city = new City(locale);
                    city.setId(cursor.getLong(INDEX_CITY_ID));
                    city.setFavorite(cursor.getInt(INDEX_CITY_FAVORITE) != 0);
                    cities.add(city);
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException se) {
            Log.e(TAG, "Query cities: " + se.getLocalizedMessage(), se);
        } finally {
            cursor.close();
        }

        return cities;
    }

    /**
     * Insert or update the city in the local database.
     *
     * @param city the city.
     */
    public void insertOrUpdateCity(City city) {
        if (city == null)
            return;

        ContentValues values = new ContentValues();
        values.put(CityColumns.TIMESTAMP, System.currentTimeMillis());
        values.put(CityColumns.FAVORITE, city.isFavorite());

        final ContentResolver resolver = context.getContentResolver();
        long id = city.getId();
        try {
            if (id == 0L) {
                values.put(BaseColumns._ID, City.generateCityId(city));

                Uri uri = resolver.insert(Cities.CONTENT_URI(context), values);
                if (uri != null) {
                    id = ContentUris.parseId(uri);
                    if (id > 0L) {
                        city.setId(id);
                    }
                }
            } else {
                Uri uri = ContentUris.withAppendedId(Cities.CONTENT_URI(context), id);
                resolver.update(uri, values, null, null);
            }
        } catch (Exception e) {
            // Caused by: java.lang.IllegalArgumentException: Unknown URL content://net.sf.times.debug.locations/city
            Log.e(TAG, "Error inserting city: " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Delete the list of cached cities and re-populate.
     */
    public void deleteCities() {
        context.getContentResolver().delete(Cities.CONTENT_URI(context), null, null);
    }

}
