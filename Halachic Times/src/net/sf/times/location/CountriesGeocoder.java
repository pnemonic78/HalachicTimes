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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.xml.sax.helpers.DefaultHandler;

import net.sf.times.R;
import android.content.Context;
import android.content.res.Resources;
import android.location.Address;
import android.location.Location;
import android.text.format.DateUtils;

/**
 * Maintains the lists of countries.
 * 
 * @author Moshe Waisberg
 */
public class CountriesGeocoder extends GeocoderBase {

	/** The time zone location provider. */
	public static final String TIMEZONE_PROVIDER = "timezone";
	/** The "user pick a city" location provider. */
	public static final String USER_PROVIDER = "user";

	/** Degrees per time zone hour. */
	private static final double TZ_HOUR = 360 / 24;

	/** Factor to convert a fixed-point integer to double. */
	private static final double RATIO = 1e+6;

	/**
	 * Not physically possible for more than 20 countries to overlap each other.
	 */
	private static final int MAX_COUNTRIES_OVERLAP = 20;

	/** Maximum radius for which a zman is the same (20 kilometres). */
	private static final float CITY_RADIUS = 20000f;

	private static CountryPolygon[] mCountryBorders;
	private String[] mCitiesNames;
	private static String[] mCitiesCountries;
	private static double[] mCitiesLatitudes;
	private static double[] mCitiesLongitudes;
	private static double[] mCitiesElevations;

	/**
	 * Constructs a new cities provider.
	 * 
	 * @param context
	 *            the context.
	 */
	public CountriesGeocoder(Context context) {
		super(context);
	}

	/**
	 * Constructs a new cities provider.
	 * 
	 * @param context
	 *            the context.
	 * @param locale
	 *            the locale.
	 */
	public CountriesGeocoder(Context context, Locale locale) {
		super(context, locale);

		// Populate arrays from "countries.xml"
		Resources res = context.getResources();
		if (mCountryBorders == null) {
			String[] countryCodes = res.getStringArray(R.array.countries);
			int countriesCount = countryCodes.length;
			mCountryBorders = new CountryPolygon[countriesCount];
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
				mCountryBorders[c] = country;
			}
		}
		if (mCitiesCountries == null) {
			mCitiesCountries = res.getStringArray(R.array.cities_countries);
			int citiesCount = mCitiesCountries.length;
			String[] latitudes = res.getStringArray(R.array.cities_latitudes);
			String[] longitudes = res.getStringArray(R.array.cities_longitudes);
			String[] elevations = res.getStringArray(R.array.cities_elevations);
			mCitiesLatitudes = new double[citiesCount];
			mCitiesLongitudes = new double[citiesCount];
			mCitiesElevations = new double[citiesCount];
			for (int i = 0; i < citiesCount; i++) {
				mCitiesLatitudes[i] = Double.parseDouble(latitudes[i]);
				mCitiesLongitudes[i] = Double.parseDouble(longitudes[i]);
				mCitiesElevations[i] = Double.parseDouble(elevations[i]);
			}
		}
		mCitiesNames = res.getStringArray(R.array.cities);
	}

	/**
	 * Find the nearest city to the location.
	 * 
	 * @param location
	 *            the location.
	 * @return the city - {@code null} otherwise.
	 */
	public Address findCountry(Location location) {
		return findCountry(location.getLatitude(), location.getLongitude());
	}

	/**
	 * Find the nearest city to the location.
	 * 
	 * @param latitude
	 *            the latitude.
	 * @param longitude
	 *            the longitude.
	 * @return the city - {@code null} otherwise.
	 */
	public Address findCountry(double latitude, double longitude) {
		final int fixedpointLatitude = (int) Math.rint(latitude * RATIO);
		final int fixedpointLongitude = (int) Math.rint(longitude * RATIO);
		double distanceToBorder;
		double distanceMin = Double.MAX_VALUE;
		int found = -1;
		final int countriesSize = mCountryBorders.length;
		CountryPolygon country;
		int[] matches = new int[MAX_COUNTRIES_OVERLAP];
		int matchesCount = 0;

		for (int c = 0; (c < countriesSize) && (matchesCount < MAX_COUNTRIES_OVERLAP); c++) {
			country = mCountryBorders[c];
			if (country.containsBox(fixedpointLatitude, fixedpointLongitude))
				matches[matchesCount++] = c;
		}
		if (matchesCount == 0) {
			// Find the nearest border.
			for (int c = 0; c < countriesSize; c++) {
				country = mCountryBorders[c];
				distanceToBorder = country.minimumDistanceToBorders(fixedpointLatitude, fixedpointLongitude);
				if (distanceToBorder < distanceMin) {
					distanceMin = distanceToBorder;
					found = c;
				}
			}

			if (found < 0)
				return null;
		} else if (matchesCount == 1) {
			found = matches[0];
		} else {
			// Case 1: Smaller country inside a larger country.
			CountryPolygon other;
			country = mCountryBorders[matches[0]];
			int matchCountryIndex;
			for (int m = 1; m < matchesCount; m++) {
				matchCountryIndex = matches[m];
				other = mCountryBorders[matchCountryIndex];
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
					country = mCountryBorders[matchCountryIndex];
					if (country.contains(fixedpointLatitude, fixedpointLongitude)) {
						distanceToBorder = country.minimumDistanceToBorders(fixedpointLatitude, fixedpointLongitude);
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
						country = mCountryBorders[matchCountryIndex];
						distanceToBorder = country.minimumDistanceToBorders(fixedpointLatitude, fixedpointLongitude);
						if (distanceToBorder < distanceMin) {
							distanceMin = distanceToBorder;
							found = matchCountryIndex;
						}
					}
				}
			}
		}

		Locale locale = new Locale(getLanguage(), mCountryBorders[found].countryCode);
		ZmanimAddress city = new ZmanimAddress(locale);
		city.setId(-found);
		city.setLatitude(latitude);
		city.setLongitude(longitude);
		city.setCountryCode(locale.getCountry());
		city.setCountryName(locale.getDisplayCountry());

		return city;
	}

	/**
	 * Find the first corresponding location for the time zone.
	 * 
	 * @param tz
	 *            the time zone.
	 * @return the location - {@code null} otherwise.
	 */
	public Location findLocation(TimeZone tz) {
		Location loc = new Location(TIMEZONE_PROVIDER);
		if (tz != null) {
			int offset = tz.getRawOffset() % 43200000;
			double longitude = (TZ_HOUR * offset) / DateUtils.HOUR_IN_MILLIS;
			loc.setLongitude(longitude);
		}
		return loc;
	}

	/**
	 * Find the nearest valid city for the location.
	 * 
	 * @param location
	 *            the location.
	 * @return the city - {@code null} otherwise.
	 */
	public Address findCity(Location location) {
		ZmanimAddress city = null;
		final int citiesCount = mCitiesNames.length;
		double searchLatitude = location.getLatitude();
		double searchLongitude = location.getLongitude();
		double latitude;
		double longitude;
		float distanceMin = Float.MAX_VALUE;
		float[] distances = new float[1];
		Locale cityLocale;
		int nearestCityIndex = -1;

		for (int i = 0; i < citiesCount; i++) {
			latitude = mCitiesLatitudes[i];
			longitude = mCitiesLongitudes[i];
			Location.distanceBetween(searchLatitude, searchLongitude, latitude, longitude, distances);
			if (distances[0] <= distanceMin) {
				distanceMin = distances[0];
				if (distanceMin <= CITY_RADIUS) {
					nearestCityIndex = i;
				}
			}
		}
		if (nearestCityIndex >= 0) {
			cityLocale = new Locale(getLanguage(), mCitiesCountries[nearestCityIndex]);

			city = new ZmanimAddress(mLocale);
			city.setId(-nearestCityIndex - 1);
			city.setLatitude(mCitiesLatitudes[nearestCityIndex]);
			city.setLongitude(mCitiesLongitudes[nearestCityIndex]);
			city.setElevation(mCitiesElevations[nearestCityIndex]);
			city.setCountryCode(cityLocale.getCountry());
			city.setCountryName(cityLocale.getDisplayCountry());
			city.setLocality(mCitiesNames[nearestCityIndex]);
		}

		return city;
	}

	/**
	 * Get the list of cities.
	 * 
	 * @return the list of addresses.
	 */
	public List<ZmanimAddress> getCities() {
		final int citiesCount = mCitiesNames.length;
		List<ZmanimAddress> cities = new ArrayList<ZmanimAddress>(citiesCount);
		double latitude;
		double longitude;
		double elevation;
		String cityName;
		Locale locale = mLocale;
		Locale cityLocale;
		String languageCode = locale.getLanguage();
		ZmanimAddress city;

		for (int i = 0, j = -1; i < citiesCount; i++, j--) {
			latitude = mCitiesLatitudes[i];
			longitude = mCitiesLongitudes[i];
			elevation = mCitiesElevations[i];
			cityName = mCitiesNames[i];
			cityLocale = new Locale(languageCode, mCitiesCountries[i]);

			city = new ZmanimAddress(locale);
			city.setId(j);
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
		if (latitude < -90.0 || latitude > 90.0)
			throw new IllegalArgumentException("latitude == " + latitude);
		if (longitude < -180.0 || longitude > 180.0)
			throw new IllegalArgumentException("longitude == " + longitude);

		List<Address> cities = new ArrayList<Address>(maxResults);
		ZmanimAddress city = null;
		final int citiesCount = mCitiesNames.length;
		double cityLatitude;
		double cityLongitude;
		float[] distances = new float[1];
		Locale cityLocale;

		for (int i = 0; i < citiesCount; i++) {
			cityLatitude = mCitiesLatitudes[i];
			cityLongitude = mCitiesLongitudes[i];
			Location.distanceBetween(latitude, longitude, cityLatitude, cityLongitude, distances);
			if (distances[0] <= CITY_RADIUS) {
				cityLocale = new Locale(getLanguage(), mCitiesCountries[i]);

				city = new ZmanimAddress(mLocale);
				city.setId(-i - 1);
				city.setLatitude(cityLatitude);
				city.setLongitude(cityLongitude);
				city.setElevation(mCitiesElevations[i]);
				city.setCountryCode(cityLocale.getCountry());
				city.setCountryName(cityLocale.getDisplayCountry());
				city.setLocality(mCitiesNames[i]);

				cities.add(city);
			}
		}

		return cities;
	}

	@Override
	protected DefaultHandler createResponseHandler(List<Address> results, int maxResults, Locale locale) {
		return null;
	}

	@Override
	public double getElevation(double latitude, double longitude) throws IOException {
		// TODO Auto-generated method stub
		return Double.NaN;
	}

}
