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

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;

import net.sf.database.CursorFilter;
import net.sf.times.location.impl.BingGeocoder;
import net.sf.times.location.impl.GeoNamesGeocoder;
import net.sf.times.location.impl.GoogleGeocoder;
import net.sf.util.LocaleUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static net.sf.times.location.AddressOpenHelper.TABLE_ADDRESSES;
import static net.sf.times.location.AddressOpenHelper.TABLE_CITIES;
import static net.sf.times.location.AddressOpenHelper.TABLE_ELEVATIONS;
import static net.sf.times.location.DatabaseGeocoder.INDEX_CITY_FAVORITE;
import static net.sf.times.location.DatabaseGeocoder.INDEX_CITY_ID;
import static net.sf.times.location.DatabaseGeocoder.PROJECTION_CITY;
import static net.sf.times.location.DatabaseGeocoder.WHERE_ID;
import static net.sf.times.location.GeocoderBase.SAME_CITY;
import static net.sf.times.location.GeocoderBase.SAME_PLANET;
import static net.sf.times.location.GeocoderBase.SAME_PLATEAU;

/**
 * Address provider.<br>
 * Fetches addresses from various Internet providers, such as Google Maps.
 *
 * @author Moshe Waisberg
 */
public class AddressProvider {
    private static final String TAG = "AddressProvider";

    public interface OnFindAddressListener {

        /**
         * Called when an address is found.
         *
         * @param provider
         *         the address provider.
         * @param location
         *         the requested location.
         * @param address
         *         the found address.
         */
        void onFindAddress(AddressProvider provider, Location location, Address address);

        /**
         * Called when a location with an elevation is found.
         *
         * @param provider
         *         the address provider.
         * @param location
         *         the requested location.
         * @param elevated
         *         the location with elevation.
         */
        void onFindElevation(AddressProvider provider, Location location, ZmanimLocation elevated);

    }

    protected static final double LATITUDE_MIN = ZmanimLocation.LATITUDE_MIN;
    protected static final double LATITUDE_MAX = ZmanimLocation.LATITUDE_MAX;
    protected static final double LONGITUDE_MIN = ZmanimLocation.LONGITUDE_MIN;
    protected static final double LONGITUDE_MAX = ZmanimLocation.LONGITUDE_MAX;

    private final Context context;
    private final Locale locale;
    /** The list of countries. */
    private CountriesGeocoder countriesGeocoder;
    private Geocoder geocoder;
    private GeocoderBase googleGeocoder;
    private GeocoderBase bingGeocoder;
    private GeocoderBase geonamesGeocoder;
    private DatabaseGeocoder databaseGeocoder;
    private boolean online = true;

    /**
     * Constructs a new provider.
     *
     * @param context
     *         the context.
     */
    public AddressProvider(Context context) {
        this(context, LocaleUtils.getDefaultLocale(context));
    }

    /**
     * Constructs a new provider.
     *
     * @param context
     *         the context.
     * @param locale
     *         the locale.
     */
    public AddressProvider(Context context, Locale locale) {
        this.context = context;
        this.locale = locale;
        this.countriesGeocoder = new CountriesGeocoder(context, locale);
        this.databaseGeocoder = new DatabaseGeocoder(context, locale);

        ApplicationInfo applicationInfo = context.getApplicationInfo();
        Bundle metaData = applicationInfo.metaData;
        if (metaData == null) {
            try {
                applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                metaData = applicationInfo.metaData;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (metaData != null) {
            online = !metaData.getBoolean("net.sf.times.offline", false);
        }
    }

    /**
     * Find the nearest address of the location.
     *
     * @param location
     *         the location.
     * @param listener
     *         the listener.
     * @return the address - {@code null} otherwise.
     */
    public Address findNearestAddress(Location location, OnFindAddressListener listener) {
        if (location == null)
            return null;
        final double latitude = location.getLatitude();
        if ((latitude < LATITUDE_MIN) || (latitude > LATITUDE_MAX))
            return null;
        final double longitude = location.getLongitude();
        if ((longitude < LONGITUDE_MIN) || (longitude > LONGITUDE_MAX))
            return null;

        List<Address> addresses;
        Address best = null;
        Address bestCountry;
        Address bestPlateau = null;
        Address bestCity;

        if (listener != null)
            listener.onFindAddress(this, location, best);

        // Find the best country.
        addresses = findNearestCountry(location);
        best = findBestAddress(location, addresses, SAME_PLANET);
        if ((best != null) && (listener != null))
            listener.onFindAddress(this, location, best);
        bestCountry = best;

        // Find the best XML city.
        addresses = findNearestCity(location);
        best = findBestAddress(location, addresses, SAME_CITY);
        if ((best != null) && (listener != null))
            listener.onFindAddress(this, location, best);
        bestCity = best;

        // Find the best cached city.
        addresses = findNearestAddressDatabase(location);
        best = findBestAddress(location, addresses);
        if ((best != null) && (listener != null))
            listener.onFindAddress(this, location, best);

        // Find the best city from some Geocoder provider.
        if ((best == null) && online) {
            addresses = findNearestAddressGeocoder(location);
            bestPlateau = findBestAddress(location, addresses, SAME_PLATEAU);
            if ((bestPlateau != null) && (listener != null))
                listener.onFindAddress(this, location, bestPlateau);
            best = findBestAddress(location, addresses, SAME_CITY);
            if ((best != null) && (best != bestPlateau) && (listener != null))
                listener.onFindAddress(this, location, best);
        }

        // Find the best city from Google.
        if ((best == null) && online) {
            addresses = findNearestAddressGoogle(location);
            bestPlateau = findBestAddress(location, addresses, SAME_PLATEAU);
            if ((bestPlateau != null) && (listener != null))
                listener.onFindAddress(this, location, bestPlateau);
            best = findBestAddress(location, addresses, SAME_CITY);
            if ((best != null) && (best != bestPlateau) && (listener != null))
                listener.onFindAddress(this, location, best);
        }

        // Find the best city from Bing.
        if ((best == null) && online) {
            addresses = findNearestAddressBing(location);
            bestPlateau = findBestAddress(location, addresses, SAME_PLATEAU);
            if ((bestPlateau != null) && (listener != null))
                listener.onFindAddress(this, location, bestPlateau);
            best = findBestAddress(location, addresses, SAME_CITY);
            if ((best != null) && (best != bestPlateau) && (listener != null))
                listener.onFindAddress(this, location, best);
        }

        // Find the best city from GeoNames.
        if ((best == null) && online) {
            addresses = findNearestAddressGeoNames(location);
            bestPlateau = findBestAddress(location, addresses, SAME_PLATEAU);
            if ((bestPlateau != null) && (listener != null))
                listener.onFindAddress(this, location, bestPlateau);
            best = findBestAddress(location, addresses, SAME_CITY);
            if ((best != null) && (best != bestPlateau) && (listener != null))
                listener.onFindAddress(this, location, best);
        }

        if (best == null) {
            best = bestCity;
            if (best == null) {
                best = bestPlateau;
                if (best == null) {
                    best = bestCountry;
                }
            }
        }

        return best;
    }

    /**
     * Find addresses that are known to describe the area immediately
     * surrounding the given latitude and longitude.
     * <p/>
     * Uses the built-in Android {@link Geocoder} API.
     *
     * @param location
     *         the location.
     * @return the list of addresses.
     */
    private List<Address> findNearestAddressGeocoder(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        List<Address> addresses = null;
        Geocoder geocoder = this.geocoder;
        if (geocoder == null) {
            geocoder = new Geocoder(context);
            this.geocoder = geocoder;
        }
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 5);
        } catch (Exception e) {
            Log.e(TAG, "Geocoder: " + e.getLocalizedMessage() + " at " + longitude + ";" + latitude, e);
        }
        return addresses;
    }

    /**
     * Find addresses that are known to describe the area immediately
     * surrounding the given latitude and longitude.
     * <p/>
     * Uses the Google Maps API.
     *
     * @param location
     *         the location.
     * @return the list of addresses.
     */
    private List<Address> findNearestAddressGoogle(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        List<Address> addresses = null;
        GeocoderBase geocoder = googleGeocoder;
        if (geocoder == null) {
            geocoder = new GoogleGeocoder(locale);
            googleGeocoder = geocoder;
        }
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 5);
        } catch (Exception e) {
            Log.e(TAG, "Google geocoder: " + e.getLocalizedMessage() + " at " + longitude + ";" + latitude, e);
        }
        return addresses;
    }

    /**
     * Finds the nearest street and address for a given lat/lng pair.
     * <p/>
     * Uses the GeoNames API.
     *
     * @param location
     *         the location.
     * @return the list of addresses.
     */
    private List<Address> findNearestAddressGeoNames(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        List<Address> addresses = null;
        GeocoderBase geocoder = geonamesGeocoder;
        if (geocoder == null) {
            geocoder = new GeoNamesGeocoder(locale);
            geonamesGeocoder = geocoder;
        }
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 10);
        } catch (Exception e) {
            Log.e(TAG, "GeoNames geocoder: " + e.getLocalizedMessage() + " at " + longitude + ";" + latitude, e);
        }
        return addresses;
    }

    /**
     * Finds the nearest street and address for a given lat/lng pair.
     * <p/>
     * Uses the Bing API.
     *
     * @param location
     *         the location.
     * @return the list of addresses.
     */
    private List<Address> findNearestAddressBing(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        List<Address> addresses = null;
        GeocoderBase geocoder = bingGeocoder;
        if (geocoder == null) {
            geocoder = new BingGeocoder(locale);
            bingGeocoder = geocoder;
        }
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 5);
        } catch (Exception e) {
            Log.e(TAG, "Bing geocoder: " + e.getLocalizedMessage() + " at " + longitude + ";" + latitude, e);
        }
        return addresses;
    }

    /**
     * Find the best address by checking relevant fields.
     *
     * @param location
     *         the location.
     * @param addresses
     *         the list of addresses.
     * @return the best address - {@code null} otherwise.
     */
    private Address findBestAddress(Location location, List<Address> addresses) {
        return findBestAddress(location, addresses, SAME_CITY);
    }

    /**
     * Find the best address by checking relevant fields.
     *
     * @param location
     *         the location.
     * @param addresses
     *         the list of addresses.
     * @param radius
     *         the maximum radius.
     * @return the best address - {@code null} otherwise.
     */
    private Address findBestAddress(Location location, List<Address> addresses, float radius) {
        if ((addresses == null) || addresses.isEmpty())
            return null;

        // First, find the closest location.
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        float distanceMin = radius;
        Address addrMin = null;
        float[] distances = new float[1];
        List<Address> near = new ArrayList<>(addresses.size());

        for (Address a : addresses) {
            if (!a.hasLatitude() || !a.hasLongitude())
                continue;
            Location.distanceBetween(latitude, longitude, a.getLatitude(), a.getLongitude(), distances);
            if (distances[0] <= radius) {
                near.add(a);
                if (distances[0] <= distanceMin) {
                    distanceMin = distances[0];
                    addrMin = a;
                }
            }
        }

        if (addrMin != null)
            return addrMin;
        if (near.isEmpty())
            return null;
        if (near.size() == 1)
            return near.get(0);

        // Next, find the best address part.
        for (Address a : near) {
            if (a.getFeatureName() != null)
                return a;
        }
        for (Address a : near) {
            if (a.getLocality() != null)
                return a;
        }
        for (Address a : near) {
            if (a.getSubLocality() != null)
                return a;
        }
        for (Address a : near) {
            if (a.getAdminArea() != null)
                return a;
        }
        for (Address a : near) {
            if (a.getSubAdminArea() != null)
                return a;
        }
        for (Address a : near) {
            if (a.getCountryName() != null)
                return a;
        }
        return near.get(0);
    }

    /**
     * Format the address.
     *
     * @param a
     *         the address.
     * @return the formatted address name.
     */
    public static CharSequence formatAddress(ZmanimAddress a) {
        return a.getFormatted();
    }

    /**
     * Find addresses that are known to describe the area immediately
     * surrounding the given latitude and longitude.
     * <p/>
     * Uses the local database.
     *
     * @param location
     *         the location.
     * @return the list of addresses.
     */
    private List<Address> findNearestAddressDatabase(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        List<Address> addresses = null;
        GeocoderBase geocoder = databaseGeocoder;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 10);
        } catch (Exception e) {
            Log.e(TAG, "Database geocoder: " + e.getLocalizedMessage() + " at " + longitude + ";" + latitude, e);
        }
        return addresses;
    }

    @Deprecated
    private SQLiteDatabase getReadableDatabase() {
        return databaseGeocoder.getReadableDatabase();
    }

    @Deprecated
    private SQLiteDatabase getWritableDatabase() {
        return databaseGeocoder.getWritableDatabase();
    }

    /**
     * Insert or update the address in the local database. The local database is
     * supposed to reduce redundant network requests.
     *
     * @param location
     *         the location.
     * @param address
     *         the address.
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

        SQLiteDatabase db = getWritableDatabase();
        if (db == null)
            return;
        if (insert) {
            id = db.insert(TABLE_ADDRESSES, null, values);
            if (id > 0L) {
                address.setId(id);
            }
        } else {
            String[] whereArgs = {Long.toString(id)};
            db.update(TABLE_ADDRESSES, values, WHERE_ID, whereArgs);
        }
    }

    /** Close resources. */
    public void close() {
        databaseGeocoder.close();
    }

    /**
     * Find the nearest country to the latitude and longitude.
     * <p/>
     * Uses the pre-compiled array of countries from GeoNames.
     *
     * @param location
     *         the location.
     * @return the list of addresses with at most 1 entry.
     */
    private List<Address> findNearestCountry(Location location) {
        List<Address> countries = null;
        Address country = countriesGeocoder.findCountry(location);
        if (country != null) {
            countries = new ArrayList<>();
            countries.add(country);
        }
        return countries;
    }

    /**
     * Find the nearest city to the latitude and longitude.
     * <p/>
     * Uses the pre-compiled array of cities from GeoNames.
     *
     * @param location
     *         the location.
     * @return the list of addresses with at most 1 entry.
     */
    private List<Address> findNearestCity(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        List<Address> addresses = null;
        GeocoderBase geocoder = countriesGeocoder;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 10);
        } catch (Exception e) {
            Log.e(TAG, "City: " + e.getLocalizedMessage() + " at " + longitude + ";" + latitude, e);
        }
        return addresses;
    }

    /**
     * Fetch addresses from the database.
     *
     * @param filter
     *         a cursor filter.
     * @return the list of addresses.
     */
    public List<ZmanimAddress> queryAddresses(CursorFilter filter) {
        return databaseGeocoder.queryAddresses(filter);
    }

    /**
     * Find the elevation (altitude).
     *
     * @param location
     *         the location.
     * @param listener
     *         the listener.
     * @return the elevated location - {@code null} otherwise.
     */
    public Location findElevation(Location location, OnFindAddressListener listener) {
        ZmanimLocation elevated;

        if (location.hasAltitude()) {
            elevated = findElevationCities(location);
            if (elevated == null) {
                elevated = findElevationDatabase(location);
                if (elevated == null)
                    elevated = new ZmanimLocation(location);
                else if (ZmanimLocation.compareTo(location, elevated) == 0)
                    elevated.setAltitude(location.getAltitude());
            } else if (ZmanimLocation.compareTo(location, elevated) == 0)
                elevated.setAltitude(location.getAltitude());
            if (listener != null)
                listener.onFindElevation(this, location, elevated);
            return elevated;
        }

        elevated = findElevationCities(location);
        if ((elevated != null) && elevated.hasAltitude()) {
            if (listener != null)
                listener.onFindElevation(this, location, elevated);
            return elevated;
        }

        elevated = findElevationDatabase(location);
        if ((elevated != null) && elevated.hasAltitude()) {
            if (listener != null)
                listener.onFindElevation(this, location, elevated);
            return elevated;
        }

        if (online) {
            elevated = findElevationBing(location);
            if ((elevated != null) && elevated.hasAltitude()) {
                if (listener != null)
                    listener.onFindElevation(this, location, elevated);
                return elevated;
            }

            elevated = findElevationGeoNames(location);
            if ((elevated != null) && elevated.hasAltitude()) {
                if (listener != null)
                    listener.onFindElevation(this, location, elevated);
                return elevated;
            }

            elevated = findElevationGoogle(location);
            if ((elevated != null) && elevated.hasAltitude()) {
                if (listener != null)
                    listener.onFindElevation(this, location, elevated);
                return elevated;
            }
        }

        return null;
    }

    /**
     * Find elevation of nearest cities. Calculates the average elevation of
     * neighbouring cities if more than {@code 1} is found.
     *
     * @param location
     *         the location.
     * @return the elevated location - {@code null} otherwise.
     */
    private ZmanimLocation findElevationCities(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        try {
            return countriesGeocoder.getElevation(latitude, longitude);
        } catch (Exception e) {
            Log.e(TAG, "Countries geocoder: " + e.getLocalizedMessage() + " at " + longitude + ";" + latitude, e);
        }
        return null;
    }

    /**
     * Find elevation according to Google Maps.
     *
     * @param location
     *         the location.
     * @return the location with elevation - {@code null} otherwise.
     */
    private ZmanimLocation findElevationGoogle(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        GeocoderBase geocoder = googleGeocoder;
        if (geocoder == null) {
            geocoder = new GoogleGeocoder(locale);
            googleGeocoder = geocoder;
        }
        try {
            return geocoder.getElevation(latitude, longitude);
        } catch (Exception e) {
            Log.e(TAG, "Google geocoder: " + e.getLocalizedMessage() + " at " + longitude + ";" + latitude, e);
        }
        return null;
    }

    /**
     * Find elevation according to GeoNames.
     *
     * @param location
     *         the location.
     * @return the elevated location - {@code null} otherwise.
     */
    private ZmanimLocation findElevationGeoNames(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        GeocoderBase geocoder = geonamesGeocoder;
        if (geocoder == null) {
            geocoder = new GeoNamesGeocoder(locale);
            geonamesGeocoder = geocoder;
        }
        try {
            return geocoder.getElevation(latitude, longitude);
        } catch (Exception e) {
            Log.e(TAG, "GeoNames geocoder: " + e.getLocalizedMessage() + " at " + longitude + ";" + latitude, e);
        }
        return null;
    }

    /**
     * Find elevation according to Bing.
     *
     * @param location
     *         the location.
     * @return the elevated location - {@code null} otherwise.
     */
    private ZmanimLocation findElevationBing(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        GeocoderBase geocoder = bingGeocoder;
        if (geocoder == null) {
            geocoder = new BingGeocoder(locale);
            bingGeocoder = geocoder;
        }
        try {
            return geocoder.getElevation(latitude, longitude);
        } catch (Exception e) {
            Log.e(TAG, "Bing geocoder: " + e.getLocalizedMessage() + " at " + longitude + ";" + latitude, e);
        }
        return null;
    }

    /**
     * Find elevation of nearest locations cached in the database. Calculates
     * the average elevation of neighbouring locations if more than {@code 1} is
     * found.
     *
     * @param location
     *         the location.
     * @return the elevated location - {@code null} otherwise.
     */
    private ZmanimLocation findElevationDatabase(Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        GeocoderBase geocoder = databaseGeocoder;
        try {
            return geocoder.getElevation(latitude, longitude);
        } catch (Exception e) {
            Log.e(TAG, "Database geocoder: " + e.getLocalizedMessage() + " at " + longitude + ";" + latitude, e);
        }
        return null;
    }

    /**
     * Insert or update the location with elevation in the local database. The
     * local database is supposed to reduce redundant network requests.
     *
     * @param location
     *         the location.
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

        SQLiteDatabase db = getWritableDatabase();
        if (db == null)
            return;
        if (id == 0L) {
            id = db.insert(TABLE_ELEVATIONS, null, values);
            if (id > 0L) {
                location.setId(id);
            }
        } else {
            String[] whereArgs = {Long.toString(id)};
            db.update(TABLE_ELEVATIONS, values, WHERE_ID, whereArgs);
        }
    }

    /**
     * Populate the cities with data from the table.
     *
     * @param cities
     *         the list of cities to populate.
     */
    public void populateCities(Collection<City> cities) {
        Map<Long, City> citiesById = new HashMap<>();
        long id;
        for (City city : cities) {
            id = city.getId();
            if (id == 0L) {
                id = City.generateCityId(city);
            }
            citiesById.put(id, city);
        }

        SQLiteDatabase db = getReadableDatabase();
        if (db == null)
            return;
        Cursor cursor = db.query(TABLE_CITIES, PROJECTION_CITY, null, null, null, null, null);
        if ((cursor == null) || cursor.isClosed()) {
            return;
        }

        try {
            if (cursor.moveToFirst()) {
                boolean favorite;
                City city;

                do {
                    id = cursor.getLong(INDEX_CITY_ID);
                    favorite = cursor.getShort(INDEX_CITY_FAVORITE) != 0;

                    city = citiesById.get(id);
                    if (city != null) {
                        city.setId(id);
                        city.setFavorite(favorite);
                    }
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException se) {
            Log.e(TAG, "Populate cities: " + se.getLocalizedMessage(), se);
        } finally {
            cursor.close();
        }
    }

    /**
     * Insert or update the city in the local database.
     *
     * @param city
     *         the city.
     */
    public void insertOrUpdateCity(City city) {
        if (city == null)
            return;

        SQLiteDatabase db = getWritableDatabase();
        if (db == null)
            return;

        ContentValues values = new ContentValues();
        values.put(CitiesColumns.TIMESTAMP, System.currentTimeMillis());
        values.put(CitiesColumns.FAVORITE, city.isFavorite());

        long id = city.getId();
        if (id == 0L) {
            id = City.generateCityId(city);

            values.put(BaseColumns._ID, id);
            id = db.insert(TABLE_CITIES, null, values);
            if (id > 0L) {
                city.setId(id);
            }
        } else {
            String[] whereArgs = {Long.toString(id)};
            db.update(TABLE_CITIES, values, WHERE_ID, whereArgs);
        }
    }

    /**
     * Delete the list of cached addresses.
     */
    public void deleteAddresses() {
        databaseGeocoder.deleteAddresses();
    }

    /**
     * Delete the list of cached elevations.
     */
    public void deleteElevations() {
        databaseGeocoder.deleteElevations();
    }

    /**
     * Get the list of internal cities.
     *
     * @return the list of cities.
     */
    public List<City> getCities() {
        List<City> cities = countriesGeocoder.getCities();
        populateCities(cities);
        return cities;
    }

    /**
     * Delete the list of cached cities and re-populate.
     */
    public void deleteCities() {
        databaseGeocoder.deleteCities();
    }
}
