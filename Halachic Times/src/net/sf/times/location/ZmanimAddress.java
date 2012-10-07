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

import java.util.Locale;

import android.location.Address;
import android.os.Bundle;

/**
 * Address that is stored in the local database.
 * 
 * @author Moshe
 */
public class ZmanimAddress extends Address {

	/** Address field separator. */
	private static final String ADDRESS_SEPARATOR = ", ";

	private long id;
	private String formatted;

	/**
	 * Constructs a new address.
	 * 
	 * @param locale
	 *            the locale.
	 */
	public ZmanimAddress(Locale locale) {
		super(locale);
	}

	/**
	 * Constructs a new address.
	 * 
	 * @param address
	 *            the source address.
	 */
	public ZmanimAddress(Address address) {
		super(address.getLocale());

		int index = 0;
		String line = address.getAddressLine(index);
		while (line != null) {
			setAddressLine(index, line);
			index++;
			line = address.getAddressLine(index);
		}
		setAdminArea(address.getAdminArea());
		setCountryCode(address.getCountryCode());
		setCountryName(address.getCountryName());
		setExtras(address.getExtras());
		setFeatureName(address.getFeatureName());
		setLatitude(address.getLatitude());
		setLocality(address.getLocality());
		setLongitude(address.getLongitude());
		setPhone(address.getPhone());
		setPostalCode(address.getPostalCode());
		setPremises(address.getPremises());
		setSubAdminArea(address.getSubAdminArea());
		setSubLocality(address.getSubLocality());
		setSubThoroughfare(address.getSubThoroughfare());
		setThoroughfare(address.getThoroughfare());
		setUrl(address.getUrl());
	}

	/**
	 * Get the id.
	 * 
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Set the id.
	 * 
	 * @param id
	 *            the id.
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Get the formatted address.
	 * 
	 * @return the address
	 */
	public String getFormatted() {
		if (formatted == null)
			formatted = format();
		return formatted;
	}

	/**
	 * Set the formatted address.
	 * 
	 * @param formatted
	 *            the address.
	 */
	public void setFormatted(String formatted) {
		this.formatted = formatted;
	}

	/**
	 * Format the address.
	 * 
	 * @return the formatted address.
	 */
	protected String format() {
		Bundle extras = getExtras();
		String formatted = (extras == null) ? null : extras.getString(GoogleGeocoder.KEY_FORMATTED);
		if (formatted != null)
			return formatted;

		StringBuilder buf = new StringBuilder();
		String feature = getFeatureName();
		String subloc = getSubLocality();
		String locality = getLocality();
		String subadmin = getSubAdminArea();
		String admin = getAdminArea();
		String country = getCountryName();

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

		if (buf.length() == 0)
			return getLocale().getDisplayCountry();

		return buf.toString();
	}
}
