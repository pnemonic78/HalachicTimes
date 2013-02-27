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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.net.HTTPReader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.location.Address;
import android.text.TextUtils;

/**
 * A class for handling geocoding and reverse geocoding. This geocoder uses the
 * GeoNames WebServices API.
 * <p>
 * <a
 * href="http://www.geonames.org/export/web-services.html">http://www.geonames
 * .org/export/web-services.html</a>
 * 
 * @author Moshe Waisberg
 */
public class GeoNamesGeocoder {

	/** GeoNames user name. */
	private static final String USERNAME = "pnemonic";

	/** URL that accepts latitude and longitude coordinates as parameters. */
	private static final String URL_LATLNG = "http://api.geonames.org/extendedFindNearby?lat=%f&lng=%f&lang=%s&username=%s";

	/**
	 * Key to store the formatted address, instead of formatting it ourselves
	 * elsewhere.
	 */
	public static final String KEY_FORMATTED = "formatted_address";

	protected final Context mContext;
	protected final Locale mLocale;
	private static SAXParserFactory mParserFactory;
	private static SAXParser mParser;

	/**
	 * Creates a new GeoNames geocoder.
	 * 
	 * @param context
	 *            the context.
	 */
	public GeoNamesGeocoder(Context context) {
		this(context, Locale.getDefault());
	}

	/**
	 * Creates a new GeoNames geocoder.
	 * 
	 * @param context
	 *            the context.
	 * @param locale
	 *            the locale.
	 */
	public GeoNamesGeocoder(Context context, Locale locale) {
		super();
		mContext = context;
		mLocale = locale;
	}

	/**
	 * Returns an array of Addresses that are known to describe the area
	 * immediately surrounding the given latitude and longitude. The returned
	 * addresses will be localized for the locale provided to this class's
	 * constructor.
	 * <p>
	 * The returned values may be obtained by means of a network lookup. The
	 * results are a best guess and are not guaranteed to be meaningful or
	 * correct. It may be useful to call this method from a thread separate from
	 * your primary UI thread.
	 * 
	 * @param latitude
	 *            the latitude a point for the search.
	 * @param longitude
	 *            the longitude a point for the search.
	 * @param maxResults
	 *            max number of addresses to return. Smaller numbers (1 to 5)
	 *            are recommended.
	 * @return a list of addresses. Returns {@code null} or empty list if no
	 *         matches were found or there is no backend service available.
	 * @throws IOException
	 *             if the network is unavailable or any other I/O problem
	 *             occurs.
	 */
	public List<Address> getFromLocation(double latitude, double longitude, int maxResults) throws IOException {
		if (latitude < -90.0 || latitude > 90.0)
			throw new IllegalArgumentException("latitude == " + latitude);
		if (longitude < -180.0 || longitude > 180.0)
			throw new IllegalArgumentException("longitude == " + longitude);
		String queryUrl = String.format(Locale.US, URL_LATLNG, latitude, longitude, getLanguage(mLocale), USERNAME);
		return getFromURL(queryUrl, maxResults);
	}

	/**
	 * Returns an array of Addresses that are known to describe the named
	 * location, which may be a place name such as "Dalvik, Iceland", an address
	 * such as "1600 Amphitheatre Parkway, Mountain View, CA", an airport code
	 * such as "SFO", etc.. The returned addresses will be localized for the
	 * locale provided to this class's constructor.
	 * <p>
	 * The query will block and returned values will be obtained by means of a
	 * network lookup. The results are a best guess and are not guaranteed to be
	 * meaningful or correct. It may be useful to call this method from a thread
	 * separate from your primary UI thread.
	 * 
	 * @param locationName
	 *            a user-supplied description of a location.
	 * @param maxResults
	 *            max number of addresses to return. Smaller numbers (1 to 5)
	 *            are recommended.
	 * @return a list of addresses. Returns {@code null} or empty list if no
	 *         matches were found or there is no backend service available.
	 * @throws IOException
	 *             if the network is unavailable or any other I/O problem
	 *             occurs.
	 */
	public List<Address> getFromLocationName(String locationName, int maxResults) throws IOException {
		return null;
	}

	/**
	 * Returns an array of Addresses that are known to describe the named
	 * location, which may be a place name such as "Dalvik, Iceland", an address
	 * such as "1600 Amphitheatre Parkway, Mountain View, CA", an airport code
	 * such as "SFO", etc.. The returned addresses will be localized for the
	 * locale provided to this class's constructor.
	 * <p>
	 * You may specify a bounding box for the search results by including the
	 * Latitude and Longitude of the Lower Left point and Upper Right point of
	 * the box.
	 * <p>
	 * The query will block and returned values will be obtained by means of a
	 * network lookup. The results are a best guess and are not guaranteed to be
	 * meaningful or correct. It may be useful to call this method from a thread
	 * separate from your primary UI thread.
	 * 
	 * @param locationName
	 *            a user-supplied description of a location.
	 * @param maxResults
	 *            max number of addresses to return. Smaller numbers (1 to 5)
	 *            are recommended.
	 * @param lowerLeftLatitude
	 *            the latitude of the lower left corner of the bounding box.
	 * @param lowerLeftLongitude
	 *            the longitude of the lower left corner of the bounding box.
	 * @param upperRightLatitude
	 *            the latitude of the upper right corner of the bounding box.
	 * @param upperRightLongitude
	 *            the longitude of the upper right corner of the bounding box.
	 * @return a list of addresses. Returns {@code null} or empty list if no
	 *         matches were found or there is no backend service available.
	 * @throws IOException
	 *             if the network is unavailable or any other I/O problem
	 *             occurs.
	 */
	public List<Address> getFromLocationName(String locationName, int maxResults, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude,
			double upperRightLongitude) throws IOException {
		if (locationName == null)
			throw new IllegalArgumentException("locationName == null");
		if (lowerLeftLatitude < -90.0 || lowerLeftLatitude > 90.0)
			throw new IllegalArgumentException("lowerLeftLatitude == " + lowerLeftLatitude);
		if (lowerLeftLongitude < -180.0 || lowerLeftLongitude > 180.0)
			throw new IllegalArgumentException("lowerLeftLongitude == " + lowerLeftLongitude);
		if (upperRightLatitude < -90.0 || upperRightLatitude > 90.0)
			throw new IllegalArgumentException("upperRightLatitude == " + upperRightLatitude);
		if (upperRightLongitude < -180.0 || upperRightLongitude > 180.0)
			throw new IllegalArgumentException("upperRightLongitude == " + upperRightLongitude);
		return null;
	}

	/**
	 * Get the correct language code.
	 * 
	 * @param locale
	 *            the locale.
	 * @return the language code.
	 */
	private String getLanguage(Locale locale) {
		String language = locale.getLanguage();
		if ("in".equals(language))
			language = "id";
		else if ("iw".equals(language))
			language = "he";
		else if ("ji".equals(language))
			language = "yi";
		return language;
	}

	/**
	 * Get the address by parsing the URL results.
	 * 
	 * @param queryUrl
	 *            the URL.
	 * @param maxResults
	 *            the maximum number of results.
	 * @return a list of addresses. Returns {@code null} or empty list if no
	 *         matches were found or there is no backend service available.
	 * @throws IOException
	 *             if the network is unavailable or any other I/O problem
	 *             occurs.
	 */
	private List<Address> getFromURL(String queryUrl, int maxResults) throws IOException {
		URL url = new URL(queryUrl);
		byte[] data = HTTPReader.read(url, HTTPReader.CONTENT_TEXT_XML);
		try {
			return parseLocations(data, maxResults);
		} catch (ParserConfigurationException pce) {
			throw new IOException(pce.getMessage());
		} catch (SAXException se) {
			throw new IOException(se.getMessage());
		}
	}

	/**
	 * Parse the XML response.
	 * 
	 * @param data
	 *            the XML data.
	 * @param maxResults
	 *            the maximum number of results.
	 * @return a list of addresses. Returns {@code null} or empty list if no
	 *         matches were found or there is no backend service available.
	 * @throws ParserConfigurationException
	 *             if an XML error occurs.
	 * @throws SAXException
	 *             if an XML error occurs.
	 * @throws IOException
	 *             if the network is unavailable or any other I/O problem
	 *             occurs.
	 */
	private List<Address> parseLocations(byte[] data, int maxResults) throws ParserConfigurationException, SAXException, IOException {
		// Minimum length for "<geonames/>"
		if ((data == null) || (data.length < 11))
			return null;

		List<Address> results = new ArrayList<Address>();
		InputStream in = new ByteArrayInputStream(data);
		SAXParser parser = getParser();
		DefaultHandler handler = new GeoNamesResponseHandler(results, maxResults, mLocale);
		parser.parse(in, handler);

		return results;
	}

	protected SAXParserFactory getParserFactory() {
		if (mParserFactory == null)
			mParserFactory = SAXParserFactory.newInstance();
		return mParserFactory;
	}

	protected SAXParser getParser() throws ParserConfigurationException, SAXException {
		if (mParser == null)
			mParser = getParserFactory().newSAXParser();
		return mParser;
	}

	/**
	 * Handler for parsing the XML response.
	 * 
	 * @author Moshe
	 */
	protected static class GeoNamesResponseHandler extends DefaultHandler2 {

		/** Parse state. */
		private enum State {
			START, ROOT, GEONAME, TOPONYM, TOPONYM_NAME, COUNTRY_CODE, COUNTRY, LATITUDE, LONGITUDE, STREET, STREET_NUMBER, MTFCC, LOCALITY, POSTAL_CODE, ADMIN_CODE, ADMIN, SUBADMIN_CODE, SUBADMIN, FINISH
		};

		private static final String TAG_ROOT = "geonames";
		private static final String TAG_LATITUDE = "lat";
		private static final String TAG_LONGITUDE = "lng";

		private static final String TAG_GEONAME = "geoname";
		private static final String TAG_TOPONYM = "toponymName";
		private static final String TAG_NAME = "name";
		private static final String TAG_CC = "countryCode";
		private static final String TAG_COUNTRY = "countryName";

		private static final String TAG_ADDRESS = "address";
		private static final String TAG_STREET = "street";
		private static final String TAG_MTFCC = "mtfcc";
		private static final String TAG_STREET_NUMBER = "streetNumber";
		private static final String TAG_POSTAL_CODE = "postalcode";
		private static final String TAG_LOCALITY = "placename";
		private static final String TAG_SUBADMIN_CODE = "adminCode2";
		private static final String TAG_SUBADMIN = "adminName2";
		private static final String TAG_ADMIN_CODE = "adminCode1";
		private static final String TAG_ADMIN = "adminName1";

		private State mState = State.START;
		private final List<Address> mResults;
		private final int mMaxResults;
		private final Locale mLocale;
		private Address mAddress;

		/**
		 * Constructs a new parse handler.
		 * 
		 * @param results
		 *            the destination results.
		 * @param maxResults
		 *            the maximum number of results.
		 */
		public GeoNamesResponseHandler(List<Address> results, int maxResults, Locale locale) {
			super();
			mResults = results;
			mMaxResults = maxResults;
			mLocale = locale;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);
			if (TextUtils.isEmpty(localName))
				localName = qName;

			switch (mState) {
			case START:
				if (TAG_ROOT.equals(localName))
					mState = State.ROOT;
				else
					throw new SAXException("Unexpected root element " + localName);
				break;
			case ROOT:
				if (TAG_GEONAME.equals(localName)) {
					mState = State.GEONAME;
					mAddress = new Address(mLocale);
				} else if (TAG_ADDRESS.equals(localName)) {
					mState = State.GEONAME;
					mAddress = new Address(mLocale);
				}
				break;
			case GEONAME:
				if (TAG_TOPONYM.equals(localName))
					mState = State.TOPONYM;
				else if (TAG_NAME.equals(localName))
					mState = State.TOPONYM_NAME;
				else if (TAG_LATITUDE.equals(localName))
					mState = State.LATITUDE;
				else if (TAG_LONGITUDE.equals(localName))
					mState = State.LONGITUDE;
				else if (TAG_CC.equals(localName))
					mState = State.COUNTRY_CODE;
				else if (TAG_COUNTRY.equals(localName))
					mState = State.COUNTRY;
				else if (TAG_STREET.equals(localName))
					mState = State.STREET;
				else if (TAG_MTFCC.equals(localName))
					mState = State.MTFCC;
				else if (TAG_STREET_NUMBER.equals(localName))
					mState = State.STREET_NUMBER;
				else if (TAG_POSTAL_CODE.equals(localName))
					mState = State.POSTAL_CODE;
				else if (TAG_LOCALITY.equals(localName))
					mState = State.LOCALITY;
				else if (TAG_ADMIN.equals(localName))
					mState = State.ADMIN;
				else if (TAG_ADMIN_CODE.equals(localName))
					mState = State.ADMIN_CODE;
				else if (TAG_SUBADMIN.equals(localName))
					mState = State.SUBADMIN;
				else if (TAG_SUBADMIN_CODE.equals(localName))
					mState = State.SUBADMIN_CODE;
				break;
			case FINISH:
				return;
			default:
				break;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			super.endElement(uri, localName, qName);
			if (TextUtils.isEmpty(localName))
				localName = qName;

			switch (mState) {
			case ROOT:
				if (TAG_ROOT.equals(localName))
					mState = State.FINISH;
				break;
			case GEONAME:
				if (TAG_GEONAME.equals(localName)) {
					mState = State.ROOT;
					if (mAddress != null) {
						if ((mResults.size() < mMaxResults) && mAddress.hasLatitude() && mAddress.hasLongitude())
							mResults.add(mAddress);
						else
							mState = State.FINISH;
						mAddress = null;
					}
				} else if (TAG_ADDRESS.equals(localName)) {
					mState = State.ROOT;
					if (mAddress != null) {
						if (mResults.size() < mMaxResults)
							mResults.add(mAddress);
						else
							mState = State.FINISH;
						mAddress = null;
					}
					mState = State.ROOT;
				}
				break;
			case ADMIN:
				if (TAG_ADMIN.equals(localName))
					mState = State.GEONAME;
				break;
			case ADMIN_CODE:
				if (TAG_ADMIN_CODE.equals(localName))
					mState = State.GEONAME;
				break;
			case COUNTRY:
				if (TAG_COUNTRY.equals(localName))
					mState = State.GEONAME;
				break;
			case COUNTRY_CODE:
				if (TAG_CC.equals(localName))
					mState = State.GEONAME;
				break;
			case LATITUDE:
				if (TAG_LATITUDE.equals(localName))
					mState = State.GEONAME;
				break;
			case LOCALITY:
				if (TAG_LOCALITY.equals(localName))
					mState = State.GEONAME;
				break;
			case LONGITUDE:
				if (TAG_LONGITUDE.equals(localName))
					mState = State.GEONAME;
				break;
			case MTFCC:
				if (TAG_MTFCC.equals(localName))
					mState = State.GEONAME;
				break;
			case POSTAL_CODE:
				if (TAG_POSTAL_CODE.equals(localName))
					mState = State.GEONAME;
				break;
			case STREET:
				if (TAG_STREET.equals(localName))
					mState = State.GEONAME;
				break;
			case STREET_NUMBER:
				if (TAG_STREET_NUMBER.equals(localName))
					mState = State.GEONAME;
				break;
			case SUBADMIN:
				if (TAG_SUBADMIN.equals(localName))
					mState = State.GEONAME;
				break;
			case SUBADMIN_CODE:
				if (TAG_SUBADMIN_CODE.equals(localName))
					mState = State.GEONAME;
				break;
			case TOPONYM:
				if (TAG_TOPONYM.equals(localName))
					mState = State.GEONAME;
				break;
			case TOPONYM_NAME:
				if (TAG_NAME.equals(localName))
					mState = State.GEONAME;
				break;
			case FINISH:
				return;
			default:
				break;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			super.characters(ch, start, length);

			if (length == 0)
				return;
			String s = new String(ch, start, length).trim();
			if (s.length() == 0)
				return;

			switch (mState) {
			case LATITUDE:
				if (mAddress != null)
					mAddress.setLatitude(Double.parseDouble(s));
				break;
			case LONGITUDE:
				if (mAddress != null)
					mAddress.setLongitude(Double.parseDouble(s));
				break;
			case ADMIN:
				if (mAddress != null)
					mAddress.setAdminArea(s);
				break;
			case ADMIN_CODE:
				if ((mAddress != null) && (mAddress.getAdminArea() == null))
					mAddress.setAdminArea(s);
				break;
			case COUNTRY:
				if (mAddress != null)
					mAddress.setCountryName(s);
				break;
			case COUNTRY_CODE:
				if (mAddress != null) {
					mAddress.setCountryCode(s);
					if (mAddress.getCountryName() == null)
						mAddress.setCountryName(new Locale(mLocale.getLanguage(), s).getDisplayCountry());
				}
				break;
			case LOCALITY:
				if (mAddress != null)
					mAddress.setLocality(s);
				break;
			case MTFCC:
				break;
			case POSTAL_CODE:
				if (mAddress != null)
					mAddress.setPostalCode(s);
				break;
			case STREET:
				if (mAddress != null)
					mAddress.setAddressLine(1, s);
				break;
			case STREET_NUMBER:
				if (mAddress != null)
					mAddress.setAddressLine(0, s);
				break;
			case SUBADMIN:
				if (mAddress != null)
					mAddress.setSubAdminArea(s);
				break;
			case SUBADMIN_CODE:
				if ((mAddress != null) && (mAddress.getSubAdminArea() == null))
					mAddress.setSubAdminArea(s);
				break;
			case TOPONYM:
				if ((mAddress != null) && (mAddress.getFeatureName() == null))
					mAddress.setFeatureName(s);
				break;
			case TOPONYM_NAME:
				if (mAddress != null)
					mAddress.setFeatureName(s);
				break;
			case FINISH:
				return;
			default:
				break;
			}
		}
	}
}
