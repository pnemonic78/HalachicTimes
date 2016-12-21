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

import android.content.Context;
import android.content.res.Resources;
import android.location.Address;
import android.location.Location;
import android.text.format.DateUtils;

import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Maintains the lists of countries.
 *
 * @author Moshe Waisberg
 */
public class CountriesGeocoder extends GeocoderBase {

    /** The time zone location provider. */
    public static final String TIMEZONE_PROVIDER = "timezone";

    /** Degrees per globe. */
    private static final double LONGITUDE_GLOBE = 360;
    /** Degrees per time zone hour. */
    private static final double TZ_HOUR = LONGITUDE_GLOBE / 24;
    /** Middle of a time zone, in degrees. */
    private static final double TZ_HOUR_HALF = TZ_HOUR * 0.5;

    /** Factor to convert a fixed-point integer to double. */
    private static final double RATIO = CountryPolygon.RATIO;

    /**
     * Not physically possible for more than 20 countries to overlap each other.
     */
    private static final int MAX_COUNTRIES_OVERLAP = 20;

    /** Maximum radius for which a zman is the same (20 kilometres). */
    private static final float CITY_RADIUS = 20000f;

    /** The results index for the distance. */
    private static final int INDEX_DISTANCE = 0;

    private static CountryPolygon[] countryBorders;
    private String[] citiesNames;
    private static String[] citiesCountries;
    private static double[] citiesLatitudes;
    private static double[] citiesLongitudes;
    private static double[] citiesElevations;
    private static String[] citiesTimeZones;

    /**
     * Constructs a new cities provider.
     *
     * @param context
     *         the context.
     */
    public CountriesGeocoder(Context context) {
        this(context, Locale.getDefault());
    }

    /**
     * Constructs a new cities provider.
     *
     * @param context
     *         the context.
     * @param locale
     *         the locale.
     */
    public CountriesGeocoder(Context context, Locale locale) {
        super(context, locale);

        // Populate arrays from "countries.xml"
        Resources res = context.getResources();
        if (countryBorders == null) {
            String[] countryCodes = res.getStringArray(R.array.countries);
            int countriesCount = countryCodes.length;
            CountryPolygon[] borders = new CountryPolygon[countriesCount];
            countryBorders = borders;
            int[] verticesCounts = res.getIntArray(R.array.vertices_count);
            int[] latitudes = res.getIntArray(R.array.latitudes);
            int[] longitudes = res.getIntArray(R.array.longitudes);
            int verticesCount;
            CountryPolygon country;
            int i = 0;

            for (int c = 0; c < countriesCount; c++) {
                verticesCount = verticesCounts[c];
                country = new CountryPolygon(countryCodes[c]);
                for (int v = 0; v < verticesCount; v++, i++) {
                    country.addPoint(latitudes[i], longitudes[i]);
                }
                borders[c] = country;
            }
        }
        if (citiesCountries == null) {
            citiesCountries = res.getStringArray(R.array.cities_countries);
            citiesTimeZones = res.getStringArray(R.array.cities_time_zones);

            int citiesCount = citiesCountries.length;
            String[] latitudes = res.getStringArray(R.array.cities_latitudes);
            String[] longitudes = res.getStringArray(R.array.cities_longitudes);
            String[] elevations = res.getStringArray(R.array.cities_elevations);
            citiesLatitudes = new double[citiesCount];
            citiesLongitudes = new double[citiesCount];
            citiesElevations = new double[citiesCount];
            for (int i = 0; i < citiesCount; i++) {
                citiesLatitudes[i] = Double.parseDouble(latitudes[i]);
                citiesLongitudes[i] = Double.parseDouble(longitudes[i]);
                citiesElevations[i] = Double.parseDouble(elevations[i]);
            }
        }
        citiesNames = res.getStringArray(R.array.cities);
    }

    /**
     * Find the nearest city to the location.
     *
     * @param location
     *         the location.
     * @return the country - {@code null} otherwise.
     */
    public Country findCountry(Location location) {
        return findCountry(location.getLatitude(), location.getLongitude());
    }

    /**
     * Find the nearest country for the location.
     *
     * @param latitude
     *         the latitude.
     * @param longitude
     *         the longitude.
     * @return the country - {@code null} otherwise.
     */
    public Country findCountry(double latitude, double longitude) {
        int countryIndex = findCountryIndex(latitude, longitude);
        if (countryIndex < 0) {
            return null;
        }
        countryIndex = Math.min(countryIndex, countryBorders.length - 1);

        Locale locale = new Locale(getLanguage(), countryBorders[countryIndex].countryCode);
        Country country = new Country(locale);
        country.setLatitude(latitude);
        country.setLongitude(longitude);
        country.setCountryCode(locale.getCountry());
        country.setCountryName(locale.getDisplayCountry());
        country.setId(Country.generateCountryId(country));

        return country;
    }

    /**
     * Find the nearest country index for the location.
     *
     * @param latitude
     *         the latitude.
     * @param longitude
     *         the longitude.
     * @return the country index - {@code -1} otherwise.
     */
    public int findCountryIndex(double latitude, double longitude) {
        final int fixedPointLatitude = (int) Math.rint(latitude * RATIO);
        final int fixedPointLongitude = (int) Math.rint(longitude * RATIO);
        double distanceToBorder;
        double distanceMin = Double.MAX_VALUE;
        int found = -1;
        CountryPolygon[] borders = countryBorders;
        final int countriesSize = borders.length;
        CountryPolygon country;
        int[] matches = new int[MAX_COUNTRIES_OVERLAP];
        int matchesCount = 0;

        for (int c = 0; (c < countriesSize) && (matchesCount < MAX_COUNTRIES_OVERLAP); c++) {
            country = borders[c];
            if (country.containsBox(fixedPointLatitude, fixedPointLongitude)) {
                matches[matchesCount++] = c;
            }
        }
        if (matchesCount == 0) {
            // Find the nearest border.
            for (int c = 0; c < countriesSize; c++) {
                country = borders[c];
                distanceToBorder = country.minimumDistanceToBorders(fixedPointLatitude, fixedPointLongitude);
                if (distanceToBorder < distanceMin) {
                    distanceMin = distanceToBorder;
                    found = c;
                }
            }
        } else if (matchesCount == 1) {
            found = matches[0];
        } else {
            // Case 1: Smaller country inside a larger country.
            CountryPolygon other;
            country = borders[matches[0]];
            int matchCountryIndex;
            for (int m = 1; m < matchesCount; m++) {
                matchCountryIndex = matches[m];
                other = borders[matchCountryIndex];
                if (country.containsBox(other)) {
                    country = other;
                    found = matchCountryIndex;
                } else if ((found < 0) && other.containsBox(country)) {
                    found = matches[0];
                }
            }

            // Case 2: Country rectangle intersects another country's rectangle.
            if (found < 0) {
                // Only include countries foe which the location is actually
                // inside the defined borders.
                for (int m = 0; m < matchesCount; m++) {
                    matchCountryIndex = matches[m];
                    country = borders[matchCountryIndex];
                    if (country.contains(fixedPointLatitude, fixedPointLongitude)) {
                        distanceToBorder = country.minimumDistanceToBorders(fixedPointLatitude, fixedPointLongitude);
                        if (distanceToBorder < distanceMin) {
                            distanceMin = distanceToBorder;
                            found = matchCountryIndex;
                        }
                    }
                }

                if (found < 0) {
                    // Find the nearest border.
                    for (int m = 0; m < matchesCount; m++) {
                        matchCountryIndex = matches[m];
                        country = borders[matchCountryIndex];
                        distanceToBorder = country.minimumDistanceToBorders(fixedPointLatitude, fixedPointLongitude);
                        if (distanceToBorder < distanceMin) {
                            distanceMin = distanceToBorder;
                            found = matchCountryIndex;
                        }
                    }
                }
            }
        }

        return found;
    }

    /**
     * Find the first corresponding location for the time zone.
     *
     * @param tz
     *         the time zone.
     * @return the location - {@code null} otherwise.
     */
    public Location findLocation(TimeZone tz) {
        Location loc = new Location(TIMEZONE_PROVIDER);
        if (tz == null) {
            return loc;
        }

        String tzId = tz.getID();
        long offsetMillis = tz.getRawOffset();
        double longitudeTZ = (TZ_HOUR * offsetMillis) / DateUtils.HOUR_IN_MILLIS;
        if (longitudeTZ > ZmanimLocation.LONGITUDE_MAX) {
            longitudeTZ -= LONGITUDE_GLOBE;
        } else if (longitudeTZ < ZmanimLocation.LONGITUDE_MIN) {
            longitudeTZ += LONGITUDE_GLOBE;
        }
        loc.setLongitude(longitudeTZ);

        // Find a close city in the timezone.
        final String[] names = citiesNames;
        final String[] zones = citiesTimeZones;
        final double[] latitudes = citiesLatitudes;
        final double[] longitudes = citiesLongitudes;
        final double[] elevations = citiesElevations;
        final int citiesCount = names.length;
        double latitude;
        double longitude;
        float distanceMin = Float.MAX_VALUE;
        int nearestCityIndex = -1;
        int[] matches = new int[citiesCount];
        int matchesCount = 0;
        int cityIndex;

        // First filter for all cities with the same time zone.
        for (cityIndex = 0; cityIndex < citiesCount; cityIndex++) {
            if (zones[cityIndex].equals(tzId)) {
                matches[matchesCount++] = cityIndex;
            }
        }
        if (matchesCount == 1) {
            nearestCityIndex = matches[0];
            loc.setLatitude(latitudes[nearestCityIndex]);
            loc.setLongitude(longitudes[nearestCityIndex]);
            loc.setAltitude(elevations[nearestCityIndex]);
            loc.setAccuracy(distanceMin);
            return loc;
        }

        if (matchesCount == 0) {
            // Maybe find the cities within the time zone.
            double longitudeWest = longitudeTZ - TZ_HOUR_HALF;
            double longitudeEast = longitudeTZ + TZ_HOUR_HALF;

            // In case longitudeTZ is edge case like +/-170 degrees.
            double longitudeWest2 = longitudeWest;
            double longitudeEast2 = longitudeEast;
            if (longitudeEast > ZmanimLocation.LONGITUDE_MAX) {
                longitudeEast = ZmanimLocation.LONGITUDE_MAX;
                longitudeWest2 = ZmanimLocation.LONGITUDE_MIN;
                longitudeEast2 -= LONGITUDE_GLOBE;
            } else if (longitudeWest < ZmanimLocation.LONGITUDE_MIN) {
                longitudeWest = ZmanimLocation.LONGITUDE_MIN;
                longitudeWest2 += LONGITUDE_GLOBE;
                longitudeEast2 = ZmanimLocation.LONGITUDE_MAX;
            }

            for (cityIndex = 0; cityIndex < citiesCount; cityIndex++) {
                longitude = longitudes[cityIndex];
                if (((longitudeWest <= longitude) && (longitude <= longitudeEast)) || ((longitudeWest2 <= longitude) && (longitude <= longitudeEast2))) {
                    matches[matchesCount++] = cityIndex;
                } else if (longitude > longitudeEast) {
                    // Cities are sorted by longitude ascending.
                    break;
                }
            }

            if (matchesCount == 1) {
                nearestCityIndex = matches[0];
                loc.setLatitude(latitudes[nearestCityIndex]);
                loc.setLongitude(longitudes[nearestCityIndex]);
                loc.setAltitude(elevations[nearestCityIndex]);
                loc.setAccuracy(distanceMin);
                return loc;
            }

            if (matchesCount == 0) {
                // Maybe find the cities within related time zones.
                longitudeWest = longitudeTZ - TZ_HOUR;
                longitudeEast = longitudeTZ + TZ_HOUR;

                // In case longitudeTZ is edge case like +/-170 degrees.
                longitudeWest2 = longitudeWest;
                longitudeEast2 = longitudeEast;
                if (longitudeEast > ZmanimLocation.LONGITUDE_MAX) {
                    longitudeEast = ZmanimLocation.LONGITUDE_MAX;
                    longitudeWest2 = ZmanimLocation.LONGITUDE_MIN;
                    longitudeEast2 -= LONGITUDE_GLOBE;
                } else if (longitudeWest < ZmanimLocation.LONGITUDE_MIN) {
                    longitudeWest = ZmanimLocation.LONGITUDE_MIN;
                    longitudeWest2 += LONGITUDE_GLOBE;
                    longitudeEast2 = ZmanimLocation.LONGITUDE_MAX;
                }

                for (cityIndex = 0; cityIndex < citiesCount; cityIndex++) {
                    longitude = longitudes[cityIndex];
                    if (((longitudeWest <= longitude) && (longitude <= longitudeEast)) || ((longitudeWest2 <= longitude) && (longitude <= longitudeEast2))) {
                        matches[matchesCount++] = cityIndex;
                    } else if (longitude > longitudeEast) {
                        // Cities are sorted by longitude ascending.
                        break;
                    }
                }

                if (matchesCount == 1) {
                    nearestCityIndex = matches[0];
                    loc.setLatitude(latitudes[nearestCityIndex]);
                    loc.setLongitude(longitudes[nearestCityIndex]);
                    loc.setAltitude(elevations[nearestCityIndex]);
                    loc.setAccuracy(distanceMin);
                    return loc;
                }
            }
        }

        // Next find the nearest city within the time zone.
        if (matchesCount > 0) {
            double searchLatitude = 0;
            double searchLongitude = 0;
            for (int i = 0; i < matchesCount; i++) {
                cityIndex = matches[i];
                searchLatitude += latitudes[cityIndex];
                searchLongitude += longitudes[cityIndex];
            }
            searchLatitude /= matchesCount;
            searchLongitude /= matchesCount;

            float[] distances = new float[1];

            for (int i = 0; i < matchesCount; i++) {
                cityIndex = matches[i];
                latitude = latitudes[cityIndex];
                longitude = longitudes[cityIndex];
                Location.distanceBetween(searchLatitude, searchLongitude, latitude, longitude, distances);
                if (distances[INDEX_DISTANCE] <= distanceMin) {
                    distanceMin = distances[INDEX_DISTANCE];
                    nearestCityIndex = cityIndex;
                }
            }

            if (nearestCityIndex >= 0) {
                loc.setLatitude(latitudes[nearestCityIndex]);
                loc.setLongitude(longitudes[nearestCityIndex]);
                loc.setAltitude(elevations[nearestCityIndex]);
                loc.setAccuracy(distanceMin);
            }
        }

        return loc;
    }

    /**
     * Find the nearest valid city for the location.
     *
     * @param location
     *         the location.
     * @return the city - {@code null} otherwise.
     */
    public City findCity(Location location) {
        City city = null;
        final String[] names = citiesNames;
        final String[] countries = citiesCountries;
        final double[] latitudes = citiesLatitudes;
        final double[] longitudes = citiesLongitudes;
        final double[] elevations = citiesElevations;
        final int citiesCount = names.length;
        double searchLatitude = location.getLatitude();
        double searchLongitude = location.getLongitude();
        double latitude;
        double longitude;
        float distanceMin = Float.MAX_VALUE;
        float[] distances = new float[1];
        Locale cityLocale;
        int nearestCityIndex = -1;

        for (int i = 0; i < citiesCount; i++) {
            latitude = latitudes[i];
            longitude = longitudes[i];
            Location.distanceBetween(searchLatitude, searchLongitude, latitude, longitude, distances);
            if (distances[INDEX_DISTANCE] <= distanceMin) {
                distanceMin = distances[INDEX_DISTANCE];
                if (distanceMin <= CITY_RADIUS) {
                    nearestCityIndex = i;
                }
            }
        }
        if (nearestCityIndex >= 0) {
            cityLocale = new Locale(getLanguage(), countries[nearestCityIndex]);

            city = new City(locale);
            city.setLatitude(latitudes[nearestCityIndex]);
            city.setLongitude(longitudes[nearestCityIndex]);
            city.setElevation(elevations[nearestCityIndex]);
            city.setCountryCode(cityLocale.getCountry());
            city.setCountryName(cityLocale.getDisplayCountry());
            city.setLocality(names[nearestCityIndex]);
        }

        return city;
    }

    /**
     * Get the list of cities.
     *
     * @return the list of addresses.
     */
    public List<City> getCities() {
        final String[] names = citiesNames;
        final String[] countries = citiesCountries;
        final double[] latitudes = citiesLatitudes;
        final double[] longitudes = citiesLongitudes;
        final double[] elevations = citiesElevations;
        final int citiesCount = names.length;
        List<City> cities = new ArrayList<>(citiesCount);
        double latitude;
        double longitude;
        double elevation;
        String cityName;
        Locale locale = this.locale;
        Locale cityLocale;
        String languageCode = locale.getLanguage();
        City city;

        for (int i = 0; i < citiesCount; i++) {
            latitude = latitudes[i];
            longitude = longitudes[i];
            elevation = elevations[i];
            cityName = names[i];
            cityLocale = new Locale(languageCode, countries[i]);

            city = new City(locale);
            city.setLatitude(latitude);
            city.setLongitude(longitude);
            city.setElevation(elevation);
            city.setCountryCode(cityLocale.getCountry());
            city.setCountryName(cityLocale.getDisplayCountry());
            city.setLocality(cityName);

            cities.add(city);
        }

        return cities;
    }

    @Override
    public List<Address> getFromLocation(double latitude, double longitude, int maxResults) throws IOException {
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX)
            throw new IllegalArgumentException("longitude == " + longitude);

        List<Address> cities = new ArrayList<>(maxResults);
        City city;
        final String[] names = citiesNames;
        final String[] countries = citiesCountries;
        final double[] latitudes = citiesLatitudes;
        final double[] longitudes = citiesLongitudes;
        final double[] elevations = citiesElevations;
        final int citiesCount = names.length;
        double cityLatitude;
        double cityLongitude;
        float[] distances = new float[1];
        Locale cityLocale;
        Locale locale = this.locale;

        for (int i = 0; i < citiesCount; i++) {
            cityLatitude = latitudes[i];
            cityLongitude = longitudes[i];
            Location.distanceBetween(latitude, longitude, cityLatitude, cityLongitude, distances);
            if (distances[INDEX_DISTANCE] <= CITY_RADIUS) {
                cityLocale = new Locale(getLanguage(), countries[i]);

                city = new City(locale);
                city.setLatitude(cityLatitude);
                city.setLongitude(cityLongitude);
                city.setElevation(elevations[i]);
                city.setCountryCode(cityLocale.getCountry());
                city.setCountryName(cityLocale.getDisplayCountry());
                city.setLocality(names[i]);
                city.setId(-1 - i);//Don't persist in db.

                cities.add(city);
            }
        }

        return cities;
    }

    @Override
    protected DefaultHandler createAddressResponseHandler(List<Address> results, int maxResults, Locale locale) {
        return null;
    }

    @Override
    public ZmanimLocation getElevation(double latitude, double longitude) throws IOException {
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX)
            throw new IllegalArgumentException("latitude == " + latitude);
        if (longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX)
            throw new IllegalArgumentException("longitude == " + longitude);

        List<City> cities = getCities();
        int citiesCount = cities.size();

        float distance;
        float[] distanceCity = new float[1];
        double d;
        double distancesSum = 0;
        int n = 0;
        double[] distances = new double[citiesCount];
        double[] elevations = new double[citiesCount];
        ZmanimLocation elevated;
        City cityNearest = null;
        double distanceCityMin = SAME_CITY;

        for (City city : cities) {
            if (!city.hasElevation())
                continue;
            Location.distanceBetween(latitude, longitude, city.getLatitude(), city.getLongitude(), distanceCity);
            distance = distanceCity[INDEX_DISTANCE];
            if (distance <= SAME_PLATEAU) {
                if (distance < distanceCityMin) {
                    cityNearest = city;
                    distanceCityMin = distance;
                }
                elevations[n] = city.getElevation();
                d = distance * distance;
                distances[n] = d;
                distancesSum += d;
                n++;
            }
        }

        if ((n == 1) && (cityNearest != null)) {
            elevated = new ZmanimLocation(USER_PROVIDER);
            elevated.setTime(System.currentTimeMillis());
            elevated.setLatitude(cityNearest.getLatitude());
            elevated.setLongitude(cityNearest.getLongitude());
            elevated.setAltitude(cityNearest.getElevation());
            elevated.setId(cityNearest.getId());
            return elevated;
        }
        if (n <= 1)
            return null;

        double weightSum = 0;
        for (int i = 0; i < n; i++) {
            weightSum += (1 - (distances[i] / distancesSum)) * elevations[i];
        }

        elevated = new ZmanimLocation(USER_PROVIDER);
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
