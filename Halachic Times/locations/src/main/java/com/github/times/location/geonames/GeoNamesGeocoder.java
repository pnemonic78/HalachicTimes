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
package com.github.times.location.geonames;

import android.content.Context;
import android.location.Address;
import android.location.Location;

import com.github.times.location.AddressResponseParser;
import com.github.times.location.BuildConfig;
import com.github.times.location.ElevationResponseParser;
import com.github.times.location.GeocoderBase;
import com.github.times.location.LocationException;
import com.github.times.location.TextElevationResponseParser;
import com.github.util.LocaleUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.text.TextUtils.isEmpty;

/**
 * A class for handling geocoding and reverse geocoding. This geocoder uses the
 * GeoNames WebServices API.
 * <p/>
 * <a
 * href="http://www.geonames.org/export/web-services.html">http://www.geonames
 * .org/export/web-services.html</a>
 *
 * @author Moshe Waisberg
 */
public class GeoNamesGeocoder extends GeocoderBase {

    private static final String TAG = "GeoNamesGeocoder";

    /**
     * URL that accepts latitude and longitude coordinates as parameters.
     */
    private static final String URL_LATLNG = "http://api.geonames.org/findNearbyJSON?lat=%f&lng=%f&lang=%s&username=%s";
    /**
     * URL that accepts latitude and longitude coordinates as parameters for an
     * elevation.<br>
     * Uses Shuttle Radar Topography Mission (SRTM) elevation data.
     */
    private static final String URL_ELEVATION_SRTM3 = "http://api.geonames.org/srtm3?lat=%f&lng=%f&username=%s";
    /**
     * URL that accepts latitude and longitude coordinates as parameters for an
     * elevation.<br>
     * Uses Aster Global Digital Elevation Model data.
     */
    private static final String URL_ELEVATION_AGDEM = "http://api.geonames.org/astergdem?lat=%f&lng=%f&username=%s";

    /**
     * GeoNames user name.
     */
    private static final String USERNAME = BuildConfig.GEONAMES_USERNAME;

    /**
     * Creates a new GeoNames geocoder.
     *
     * @param context the context.
     */
    public GeoNamesGeocoder(Context context) {
        this(LocaleUtils.getDefaultLocale(context));
    }

    /**
     * Creates a new GeoNames geocoder.
     *
     * @param locale the locale.
     */
    public GeoNamesGeocoder(Locale locale) {
        super(locale);
    }

    @Override
    public List<Address> getFromLocation(double latitude, double longitude, int maxResults) throws IOException {
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX)
            throw new IllegalArgumentException("longitude == " + longitude);
        if (isEmpty(USERNAME))
            return null;
        String queryUrl = String.format(Locale.US, URL_LATLNG, latitude, longitude, getLanguage(), USERNAME);
        return getJsonAddressesFromURL(queryUrl, maxResults);
    }

    @Override
    protected AddressResponseParser createAddressResponseParser(Locale locale, List<Address> results, int maxResults) throws LocationException {
        return new GeoNamesAddressResponseParser(locale, results, maxResults);
    }

    @Override
    public Location getElevation(double latitude, double longitude) throws IOException {
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX)
            throw new IllegalArgumentException("longitude == " + longitude);
        if (isEmpty(USERNAME))
            return null;
        String queryUrl = String.format(Locale.US, URL_ELEVATION_SRTM3, latitude, longitude, USERNAME);
        return getTextElevationFromURL(latitude, longitude, queryUrl);
    }

    @Override
    protected ElevationResponseParser createElevationResponseHandler(double latitude, double longitude, List<Location> results, int maxResults) throws LocationException {
        return new TextElevationResponseParser(latitude, longitude, results, maxResults);
    }
}
