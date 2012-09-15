/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/MPL-1.1.html
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

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

/**
 * Address provider.<br>
 * Fetches addresses from various Internet providers, such as Google Maps.
 * 
 * @author Moshe
 */
public class AddressProvider {

	/** Address field separator. */
	private static final String ADDRESS_SEPARATOR = ", ";

	private final Context mContext;

	/**
	 * Constructs a new provider.
	 */
	public AddressProvider(Context context) {
		super();
		mContext = context;
	}

	/**
	 * Find the nearest address of the location.
	 * 
	 * @param location
	 *            the location.
	 * @return the address.
	 */
	public Address findNearestAddress(Location location) {
		List<Address> addresses = null;

		if ((addresses == null) || addresses.isEmpty()) {
			addresses = findNearestAddressGeocoder(location);
		}
		if ((addresses == null) || addresses.isEmpty()) {
			addresses = findNearestAddressGoogle(location);
		}
		if ((addresses == null) || addresses.isEmpty()) {
			addresses = findNearestAddressGeoNames(location);
		}

		return findBestAddress(location, addresses);
	}

	/**
	 * Find addresses that are known to describe the area immediately
	 * surrounding the given latitude and longitude.
	 * <p>
	 * Uses the built-in Android {@link Geocoder} API.
	 * 
	 * @param location
	 *            the location.
	 * @return the list of addresses.
	 */
	protected List<Address> findNearestAddressGeocoder(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		List<Address> addresses;
		Geocoder geocoder = new Geocoder(mContext);
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 5);
		} catch (IOException e) {
			e.printStackTrace();
			addresses = new ArrayList<Address>();
		}
		return addresses;
	}

	/**
	 * Find addresses that are known to describe the area immediately
	 * surrounding the given latitude and longitude.
	 * <p>
	 * Uses the Google Maps API.
	 * 
	 * @param location
	 *            the location.
	 * @return the list of addresses.
	 */
	protected List<Address> findNearestAddressGoogle(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		List<Address> addresses;
		GoogleGeocoder geocoder = new GoogleGeocoder(mContext);
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 5);
		} catch (IOException e) {
			e.printStackTrace();
			addresses = new ArrayList<Address>();
		}
		return addresses;
	}

	/**
	 * Finds the nearest street and address for a given lat/lng pair.
	 * <p>
	 * Uses the GeoNames API.
	 * 
	 * @param location
	 *            the location.
	 * @return the list of addresses.
	 */
	protected List<Address> findNearestAddressGeoNames(Location location) {
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		List<Address> addresses;
		GeoNamesGeocoder geocoder = new GeoNamesGeocoder(mContext);
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 10);
		} catch (IOException e) {
			e.printStackTrace();
			addresses = new ArrayList<Address>();
		}
		return addresses;
	}

	/**
	 * Find the best address by checking relevant fields.
	 * 
	 * @param location
	 *            the location.
	 * @param addresses
	 *            the list of addresses.
	 * @return the best address.
	 */
	private Address findBestAddress(Location location, List<Address> addresses) {
		if ((addresses == null) || addresses.isEmpty())
			return null;

		// First find the closest location.
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		float distanceMin = Float.MAX_VALUE;
		Address addrMin = null;
		float[] distances = new float[3];
		for (Address a : addresses) {
			Location.distanceBetween(latitude, longitude, a.getLatitude(), a.getLongitude(), distances);
			if (distances[0] <= distanceMin) {
				distanceMin = distances[0];
				addrMin = a;
			}
		}
		if (addrMin != null)
			return addrMin;

		// Next, find the best address part.
		for (Address a : addresses) {
			if (a.getFeatureName() != null)
				return a;
		}
		for (Address a : addresses) {
			if (a.getLocality() != null)
				return a;
		}
		for (Address a : addresses) {
			if (a.getSubLocality() != null)
				return a;
		}
		for (Address a : addresses) {
			if (a.getAdminArea() != null)
				return a;
		}
		for (Address a : addresses) {
			if (a.getSubAdminArea() != null)
				return a;
		}
		for (Address a : addresses) {
			if (a.getCountryName() != null)
				return a;
		}
		return addresses.get(0);
	}

	/**
	 * Format the address.
	 * 
	 * @param a
	 *            the address.
	 * @return the formatted address name.
	 */
	public static String formatAddress(Address a) {
		Bundle extras = a.getExtras();
		String formatted = (extras == null) ? null : extras.getString(GoogleGeocoder.KEY_FORMATTED);
		if (formatted != null)
			return formatted;

		StringBuilder buf = new StringBuilder();
		String feature = a.getFeatureName();
		String subloc = a.getSubLocality();
		String locality = a.getLocality();
		String subadmin = a.getSubAdminArea();
		String admin = a.getAdminArea();
		String country = a.getCountryName();

		if (feature != null) {
			if (buf.length() > 0)
				buf.append(ADDRESS_SEPARATOR);
			buf.append(feature);
		}
		if ((subloc != null) && !subloc.equals(feature)) {
			if (buf.length() > 0)
				buf.append(ADDRESS_SEPARATOR);
			buf.append(subloc);
		}
		if ((locality != null) && !locality.equals(subloc) && !locality.equals(feature)) {
			if (buf.length() > 0)
				buf.append(ADDRESS_SEPARATOR);
			buf.append(locality);
		}
		if ((subadmin != null) && !subadmin.equals(locality) && !subadmin.equals(subloc) && !subadmin.equals(feature)) {
			if (buf.length() > 0)
				buf.append(ADDRESS_SEPARATOR);
			buf.append(subadmin);
		}
		if ((admin != null) && !admin.equals(subadmin) && !admin.equals(locality) && !admin.equals(subloc) && !admin.equals(feature)) {
			if (buf.length() > 0)
				buf.append(ADDRESS_SEPARATOR);
			buf.append(admin);
		}
		if ((country != null) && !country.equals(feature)) {
			if (buf.length() > 0)
				buf.append(ADDRESS_SEPARATOR);
			buf.append(country);
		}

		return buf.toString();
	}
}
