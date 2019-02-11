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
package com.github.times.location;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import com.github.database.CursorFilter;
import com.github.times.location.bing.BingGeocoder;
import com.github.times.location.geonames.GeoNamesGeocoder;
import com.github.times.location.google.GoogleGeocoder;
import com.github.times.location.impl.DatabaseGeocoder;
import com.github.util.LocaleUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

import static com.github.times.location.GeocoderBase.SAME_CITY;
import static com.github.times.location.GeocoderBase.SAME_PLANET;
import static com.github.times.location.GeocoderBase.SAME_PLATEAU;

/**
 * Address provider.<br>
 * Fetches addresses from various Internet providers, such as Google Maps.
 *
 * @author Moshe Waisberg
 */
public class AddressProvider {

    public interface OnFindAddressListener {

        /**
         * Called when an address is found.
         *
         * @param provider the address provider.
         * @param location the requested location.
         * @param address  the found address.
         */
        void onFindAddress(AddressProvider provider, Location location, Address address);

        /**
         * Called when a location with an elevation is found.
         *
         * @param provider the address provider.
         * @param location the requested location.
         * @param elevated the location with elevation.
         */
        void onFindElevation(AddressProvider provider, Location location, Location elevated);

    }

    protected static final double LATITUDE_MIN = ZmanimLocation.LATITUDE_MIN;
    protected static final double LATITUDE_MAX = ZmanimLocation.LATITUDE_MAX;
    protected static final double LONGITUDE_MIN = ZmanimLocation.LONGITUDE_MIN;
    protected static final double LONGITUDE_MAX = ZmanimLocation.LONGITUDE_MAX;

    private final Context context;
    private final Locale locale;
    /**
     * The list of countries.
     */
    private CountriesGeocoder countriesGeocoder;
    private Geocoder geocoder;
    private GoogleGeocoder googleGeocoder;
    private BingGeocoder bingGeocoder;
    private GeoNamesGeocoder geonamesGeocoder;
    private DatabaseGeocoder databaseGeocoder;
    private boolean online = true;

    /**
     * Constructs a new provider.
     *
     * @param context the context.
     */
    public AddressProvider(@NonNull Context context) {
        this(context, LocaleUtils.getDefaultLocale(context));
    }

    /**
     * Constructs a new provider.
     *
     * @param context the context.
     * @param locale  the locale.
     */
    public AddressProvider(@NonNull Context context, Locale locale) {
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
            online = !metaData.getBoolean("com.github.times.offline", false);
        }
    }

    /**
     * Find the nearest address of the location.
     *
     * @param location the location.
     * @param listener the listener.
     * @return the address - {@code null} otherwise.
     */
    @Nullable
    public Address findNearestAddress(@NonNull Location location, @Nullable OnFindAddressListener listener) {
        if (location == null) {
            return null;
        }
        final double latitude = location.getLatitude();
        if ((latitude < LATITUDE_MIN) || (latitude > LATITUDE_MAX)) {
            return null;
        }
        final double longitude = location.getLongitude();
        if ((longitude < LONGITUDE_MIN) || (longitude > LONGITUDE_MAX)) {
            return null;
        }

        List<Address> addresses;
        Address best = null;
        Address bestCountry;
        Address bestPlateau = null;
        Address bestCity;

        if (listener != null) {
            listener.onFindAddress(this, location, best);
        }

        // Find the best country.
        addresses = findNearestCountry(location);
        best = findBestAddress(location, addresses, SAME_PLANET);
        if ((best != null) && (listener != null)) {
            listener.onFindAddress(this, location, best);
        }
        bestCountry = best;

        // Find the best XML city.
        addresses = findNearestCity(location);
        best = findBestAddress(location, addresses, SAME_CITY);
        if ((best != null) && (listener != null)) {
            listener.onFindAddress(this, location, best);
        }
        bestCity = best;

        // Find the best cached city.
        addresses = findNearestAddressDatabase(location);
        best = findBestAddress(location, addresses);
        if ((best != null) && (listener != null)) {
            listener.onFindAddress(this, location, best);
        }

        // Find the best city from some Geocoder provider.
        if ((best == null) && online) {
            addresses = findNearestAddressGeocoder(location);
            bestPlateau = findBestAddress(location, addresses, SAME_PLATEAU);
            if ((bestPlateau != null) && (listener != null)) {
                listener.onFindAddress(this, location, bestPlateau);
            }
            best = findBestAddress(location, addresses, SAME_CITY);
            if ((best != null) && (best != bestPlateau) && (listener != null)) {
                listener.onFindAddress(this, location, best);
            }
        }

        // Find the best city remotely.
        if ((best == null) && online) {
            for (GeocoderBase geocoder : getRemoteAddressProviders()) {
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 10);
                } catch (Exception e) {
                    Timber.e(e, "Address geocoder: " + geocoder + " at " + latitude + "," + longitude + "; error: " + e.getLocalizedMessage());
                    continue;
                }
                bestPlateau = findBestAddress(location, addresses, SAME_PLATEAU);
                if ((bestPlateau != null) && (listener != null)) {
                    listener.onFindAddress(this, location, bestPlateau);
                }
                best = findBestAddress(location, addresses, SAME_CITY);
                if (best != null) {
                    if ((best != bestPlateau) && (listener != null)) {
                        listener.onFindAddress(this, location, best);
                    }
                    break;
                }
            }
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
     * @param location the location.
     * @return the list of addresses.
     */
    @Nullable
    private List<Address> findNearestAddressGeocoder(@NonNull Location location) {
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
        } catch (Exception ignore) {
        }
        return addresses;
    }

    /**
     * Get the list of remote geocoder providers for addresses.
     *
     * @return the list of providers.
     */
    private List<GeocoderBase> getRemoteAddressProviders() {
        final List<GeocoderBase> providers = new ArrayList<>();

        GoogleGeocoder googleGeocoder = this.googleGeocoder;
        if (googleGeocoder == null) {
            googleGeocoder = new GoogleGeocoder(locale);
            this.googleGeocoder = googleGeocoder;
        }
        providers.add(googleGeocoder);

        BingGeocoder bingGeocoder = this.bingGeocoder;
        if (bingGeocoder == null) {
            bingGeocoder = new BingGeocoder(locale);
            this.bingGeocoder = bingGeocoder;
        }
        providers.add(bingGeocoder);

        GeoNamesGeocoder geonamesGeocoder = this.geonamesGeocoder;
        if (geonamesGeocoder == null) {
            geonamesGeocoder = new GeoNamesGeocoder(locale);
            this.geonamesGeocoder = geonamesGeocoder;
        }
        providers.add(geonamesGeocoder);

        return providers;
    }

    /**
     * Find the best address by checking relevant fields.
     *
     * @param location  the location.
     * @param addresses the list of addresses.
     * @return the best address - {@code null} otherwise.
     */
    @Nullable
    private Address findBestAddress(@NonNull Location location, @Nullable List<Address> addresses) {
        return findBestAddress(location, addresses, SAME_CITY);
    }

    /**
     * Find the best address by checking relevant fields.
     *
     * @param location  the location.
     * @param addresses the list of addresses.
     * @param radius    the maximum radius.
     * @return the best address - {@code null} otherwise.
     */
    @Nullable
    protected Address findBestAddress(@NonNull Location location, @Nullable List<Address> addresses, float radius) {
        if ((addresses == null) || addresses.isEmpty()) {
            return null;
        }

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
     * Find addresses that are known to describe the area immediately
     * surrounding the given latitude and longitude.
     * <p/>
     * Uses the local database.
     *
     * @param location the location.
     * @return the list of addresses.
     */
    @Nullable
    private List<Address> findNearestAddressDatabase(@NonNull Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        List<Address> addresses = null;
        GeocoderBase geocoder = databaseGeocoder;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 10);
        } catch (Exception e) {
            Timber.e(e, "Database geocoder: " + e.getLocalizedMessage() + " at " + longitude + ";" + latitude);
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
    public void insertOrUpdateAddress(@NonNull Location location, @NonNull ZmanimAddress address) {
        databaseGeocoder.insertOrUpdateAddress(location, address);
    }

    /**
     * Close resources.
     */
    public void close() {
        databaseGeocoder.close();
    }

    /**
     * Find the nearest country to the latitude and longitude.
     * <p/>
     * Uses the pre-compiled array of countries from GeoNames.
     *
     * @param location the location.
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
     * @param location the location.
     * @return the list of addresses with at most 1 entry.
     */
    @Nullable
    private List<Address> findNearestCity(@NonNull Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        List<Address> addresses = null;
        GeocoderBase geocoder = countriesGeocoder;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 10);
        } catch (Exception e) {
            Timber.e(e, "City: " + e.getLocalizedMessage() + " at " + longitude + ";" + latitude);
        }
        return addresses;
    }

    /**
     * Fetch addresses from the database.
     *
     * @param filter a cursor filter.
     * @return the list of addresses.
     */
    public List<ZmanimAddress> queryAddresses(@Nullable CursorFilter filter) {
        return databaseGeocoder.queryAddresses(filter);
    }

    /**
     * Find the elevation (altitude).
     *
     * @param location the location.
     * @param listener the listener.
     * @return the elevated location - {@code null} otherwise.
     */
    @Nullable
    public Location findElevation(@NonNull Location location, @Nullable OnFindAddressListener listener) {
        if (location == null) {
            return null;
        }
        final double latitude = location.getLatitude();
        if ((latitude < LATITUDE_MIN) || (latitude > LATITUDE_MAX)) {
            return null;
        }
        final double longitude = location.getLongitude();
        if ((longitude < LONGITUDE_MIN) || (longitude > LONGITUDE_MAX)) {
            return null;
        }

        Location elevated;

        if (location.hasAltitude()) {
            elevated = findElevationDatabase(location);
            if (elevated == null) {
                elevated = findElevationCities(location);
                if (elevated == null) {
                    elevated = new ZmanimLocation(location);
                } else if (ZmanimLocation.compareTo(location, elevated) == 0) {
                    elevated.setAltitude(location.getAltitude());
                }
            } else if (ZmanimLocation.compareTo(location, elevated) == 0) {
                elevated.setAltitude(location.getAltitude());
            }
            if (listener != null) {
                listener.onFindElevation(this, location, elevated);
            }
            return elevated;
        }

        elevated = findElevationDatabase(location);
        if ((elevated != null) && elevated.hasAltitude()) {
            if (listener != null) {
                listener.onFindElevation(this, location, elevated);
            }
            return elevated;
        }

        elevated = findElevationCities(location);
        if ((elevated != null) && elevated.hasAltitude()) {
            if (listener != null) {
                listener.onFindElevation(this, location, elevated);
            }
            return elevated;
        }

        if (online) {
            for (GeocoderBase geocoder : getRemoteElevationProviders()) {
                try {
                    elevated = geocoder.getElevation(latitude, longitude);
                } catch (Exception e) {
                    Timber.e(e, "Elevation geocoder: " + geocoder + ", error: " + e.getLocalizedMessage() + " for " + longitude + ";" + latitude);
                    continue;
                }
                if ((elevated != null) && elevated.hasAltitude()) {
                    if (listener != null) {
                        listener.onFindElevation(this, location, elevated);
                    }
                    return elevated;
                }
            }
        }

        return null;
    }

    /**
     * Find elevation of nearest cities. Calculates the average elevation of
     * neighbouring cities if more than {@code 1} is found.
     *
     * @param location the location.
     * @return the elevated location - {@code null} otherwise.
     */
    @Nullable
    private Location findElevationCities(@NonNull Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        try {
            return countriesGeocoder.getElevation(latitude, longitude);
        } catch (Exception e) {
            Timber.e(e, "Countries geocoder: " + e.getLocalizedMessage() + " at " + longitude + ";" + latitude);
        }
        return null;
    }

    /**
     * Get the list of remote geocoder providers for elevations.
     *
     * @return the list of providers.
     */
    private List<GeocoderBase> getRemoteElevationProviders() {
        final List<GeocoderBase> providers = new ArrayList<>();

        GoogleGeocoder googleGeocoder = this.googleGeocoder;
        if (googleGeocoder == null) {
            googleGeocoder = new GoogleGeocoder(locale);
            this.googleGeocoder = googleGeocoder;
        }
        providers.add(googleGeocoder);

        BingGeocoder bingGeocoder = this.bingGeocoder;
        if (bingGeocoder == null) {
            bingGeocoder = new BingGeocoder(locale);
            this.bingGeocoder = bingGeocoder;
        }
        providers.add(bingGeocoder);

        GeoNamesGeocoder geonamesGeocoder = this.geonamesGeocoder;
        if (geonamesGeocoder == null) {
            geonamesGeocoder = new GeoNamesGeocoder(locale);
            this.geonamesGeocoder = geonamesGeocoder;
        }
        providers.add(geonamesGeocoder);

        return providers;
    }

    /**
     * Find elevation of nearest locations cached in the database. Calculates
     * the average elevation of neighbouring locations if more than {@code 1} is
     * found.
     *
     * @param location the location.
     * @return the elevated location - {@code null} otherwise.
     */
    @Nullable
    private Location findElevationDatabase(@NonNull Location location) {
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        GeocoderBase geocoder = databaseGeocoder;
        try {
            return geocoder.getElevation(latitude, longitude);
        } catch (Exception e) {
            Timber.e(e, "Database geocoder: " + e.getLocalizedMessage() + " at " + longitude + ";" + latitude);
        }
        return null;
    }

    /**
     * Insert or update the location with elevation in the local database. The
     * local database is supposed to reduce redundant network requests.
     *
     * @param location the location.
     */
    public void insertOrUpdateElevation(@NonNull ZmanimLocation location) {
        databaseGeocoder.insertOrUpdateElevation(location);
    }

    /**
     * Populate the cities with data from the table.
     *
     * @param cities the list of cities to populate.
     */
    private void populateCities(@NonNull Collection<City> cities) {
        Map<Long, City> citiesById = new HashMap<>();
        long id;

        for (City city : cities) {
            id = city.getId();
            if (id == 0L) {
                id = City.generateCityId(city);
            }
            citiesById.put(id, city);
        }

        List<City> citiesDb = databaseGeocoder.queryCities(null);
        City city;

        for (City cityDb : citiesDb) {
            id = cityDb.getId();
            city = citiesById.get(id);
            if (city != null) {
                city.setId(id);
                city.setFavorite(cityDb.isFavorite());
            }
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

    public boolean deleteAddress(@NonNull ZmanimAddress address) {
        return databaseGeocoder.deleteAddress(address);
    }
}
