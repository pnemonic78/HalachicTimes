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
import android.os.Bundle;
import android.text.TextUtils;

/**
 * A class for handling geocoding and reverse geocoding. This geocoder uses the
 * Google Geocoding API.
 * <p>
 * <a href="http://code.google.com/apis/maps/documentation/geocoding/">http://
 * code.google.com/apis/maps/documentation/geocoding/</a>
 * 
 * @author Moshe Waisberg
 */
public class GoogleGeocoder {

	/** URL that accepts latitude and longitude coordinates as parameters. */
	private static final String URL_LATLNG = "http://maps.googleapis.com/maps/api/geocode/xml?latlng=%f,%f&language=%s&sensor=true";
	/** URL that accepts an address as parameters. */
	private static final String URL_ADDRESS = "http://maps.googleapis.com/maps/api/geocode/xml?address=%s&language=%s&sensor=true";
	/** URL that accepts a bounded address as parameters. */
	private static final String URL_ADDRESS_BOUNDED = "http://maps.googleapis.com/maps/api/geocode/xml?address=%s&bounds=%f,%f|%f,%f&language=%s&sensor=true";

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
	 * Creates a new Google geocoder.
	 * 
	 * @param context
	 *            the context.
	 */
	public GoogleGeocoder(Context context) {
		this(context, Locale.getDefault());
	}

	/**
	 * Creates a new Google geocoder.
	 * 
	 * @param context
	 *            the context.
	 * @param locale
	 *            the locale.
	 */
	public GoogleGeocoder(Context context, Locale locale) {
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
		String queryUrl = String.format(Locale.US, URL_LATLNG, latitude, longitude, mLocale.getLanguage());
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
		if (locationName == null)
			throw new IllegalArgumentException("locationName == null");
		String queryUrl = String.format(Locale.US, URL_ADDRESS, locationName, mLocale.getLanguage());
		return getFromURL(queryUrl, maxResults);
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
		String queryUrl = String.format(Locale.US, URL_ADDRESS_BOUNDED, locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude,
				mLocale.getLanguage());
		return getFromURL(queryUrl, maxResults);
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
		byte[] data = HTTPReader.read(url, HTTPReader.CONTENT_XML);
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
		// Minimum length for "<GeocodeResponse/>"
		if ((data == null) || (data.length < 18))
			return null;

		List<Address> results = new ArrayList<Address>();
		InputStream in = new ByteArrayInputStream(data);
		SAXParser parser = getParser();
		DefaultHandler handler = new GeocodeResponseHandler(results, maxResults, mLocale);
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
	protected static class GeocodeResponseHandler extends DefaultHandler2 {

		/** Parse state. */
		private enum State {
			START, ROOT, STATUS, RESULT, RESULT_TYPE, RESULT_FORMATTED, ADDRESS, ADDRESS_TYPE, ADDRESS_LONG, ADDRESS_SHORT, GEOMETRY, LOCATION, LATITUDE, LONGITUDE, FINISH
		};

		private static final String STATUS_OK = "OK";

		private static final String TAG_ROOT = "GeocodeResponse";
		private static final String TAG_STATUS = "status";
		private static final String TAG_RESULT = "result";
		private static final String TAG_TYPE = "type";
		private static final String TAG_FORMATTED = "formatted_address";
		private static final String TAG_ADDRESS = "address_component";
		private static final String TAG_LONG_NAME = "long_name";
		private static final String TAG_SHORT_NAME = "short_name";
		private static final String TAG_GEOMETRY = "geometry";
		private static final String TAG_LOCATION = "location";
		private static final String TAG_LATITUDE = "lat";
		private static final String TAG_LONGITUDE = "lng";

		private static final String TYPE_ADMIN = "administrative_area_level_1";
		private static final String TYPE_COUNTRY = "country";
		private static final String TYPE_FEATURE = "feature_name";
		private static final String TYPE_LOCALITY = "locality";
		private static final String TYPE_POLITICAL = "political";
		private static final String TYPE_POSTAL_CODE = "postal_code";
		private static final String TYPE_ROUTE = "route";
		private static final String TYPE_STREET = "street_address";
		private static final String TYPE_STREET_NUMBER = "street_number";
		private static final String TYPE_SUBADMIN = "administrative_area_level_2";
		private static final String TYPE_SUBLOCALITY = "sublocality";

		private State mState = State.START;
		private final List<Address> mResults;
		private final int mMaxResults;
		private final Locale mLocale;
		private Address mAddress;
		private String mLongName;
		private String mShortName;
		private String mAddressType;

		/**
		 * Constructs a new parse handler.
		 * 
		 * @param results
		 *            the destination results.
		 * @param maxResults
		 *            the maximum number of results.
		 */
		public GeocodeResponseHandler(List<Address> results, int maxResults, Locale locale) {
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
				if (TAG_STATUS.equals(localName))
					mState = State.STATUS;
				else if (TAG_RESULT.equals(localName))
					mState = State.RESULT;
				break;
			case RESULT:
				if (TAG_TYPE.equals(localName))
					mState = State.RESULT_TYPE;
				else if (TAG_FORMATTED.equals(localName))
					mState = State.RESULT_FORMATTED;
				else if (TAG_ADDRESS.equals(localName))
					mState = State.ADDRESS;
				else if (TAG_GEOMETRY.equals(localName))
					mState = State.GEOMETRY;
				break;
			case ADDRESS:
				if (TAG_LONG_NAME.equals(localName))
					mState = State.ADDRESS_LONG;
				else if (TAG_SHORT_NAME.equals(localName))
					mState = State.ADDRESS_SHORT;
				else if (TAG_TYPE.equals(localName))
					mState = State.ADDRESS_TYPE;
				break;
			case GEOMETRY:
				if (TAG_LOCATION.equals(localName))
					mState = State.LOCATION;
				break;
			case LOCATION:
				if (TAG_LATITUDE.equals(localName))
					mState = State.LATITUDE;
				else if (TAG_LONGITUDE.equals(localName))
					mState = State.LONGITUDE;
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
			case STATUS:
				if (TAG_STATUS.equals(localName))
					mState = State.ROOT;
				break;
			case RESULT:
				if (TAG_RESULT.equals(localName)) {
					if (mAddress != null) {
						if ((mResults.size() < mMaxResults) && mAddress.hasLatitude() && mAddress.hasLongitude())
							mResults.add(mAddress);
						else
							mState = State.FINISH;
						mAddress = null;
					}
					mLongName = null;
					mShortName = null;
					mAddressType = null;
					mState = State.ROOT;
				}
				break;
			case RESULT_TYPE:
				if (TAG_TYPE.equals(localName))
					mState = State.RESULT;
				break;
			case RESULT_FORMATTED:
				if (TAG_FORMATTED.equals(localName))
					mState = State.RESULT;
				break;
			case ADDRESS:
				if (TAG_ADDRESS.equals(localName)) {
					if (mAddress != null) {
						if (TYPE_ADMIN.equals(mAddressType)) {
							mAddress.setAdminArea(mLongName);
						} else if (TYPE_SUBADMIN.equals(mAddressType)) {
							mAddress.setSubAdminArea(mLongName);
						} else if (TYPE_COUNTRY.equals(mAddressType)) {
							mAddress.setCountryCode(mShortName);
							mAddress.setCountryName(mLongName);
						} else if (TYPE_FEATURE.equals(mAddressType)) {
							mAddress.setFeatureName(mLongName);
						} else if (TYPE_LOCALITY.equals(mAddressType)) {
							mAddress.setLocality(mLongName);
						} else if (TYPE_POSTAL_CODE.equals(mAddressType)) {
							mAddress.setPostalCode(mLongName);
						} else if (TYPE_ROUTE.equals(mAddressType) || TYPE_STREET.equals(mAddressType) || TYPE_STREET_NUMBER.equals(mAddressType)) {
							mAddress.setAddressLine(mAddress.getMaxAddressLineIndex() + 1, mLongName);
						} else if (TYPE_SUBLOCALITY.equals(mAddressType)) {
							mAddress.setSubLocality(mLongName);
						}
						mLongName = null;
						mShortName = null;
						mAddressType = null;
					}
					mState = State.RESULT;
				}
				break;
			case ADDRESS_LONG:
				if (TAG_LONG_NAME.equals(localName))
					mState = State.ADDRESS;
				break;
			case ADDRESS_SHORT:
				if (TAG_SHORT_NAME.equals(localName))
					mState = State.ADDRESS;
				break;
			case ADDRESS_TYPE:
				if (TAG_TYPE.equals(localName))
					mState = State.ADDRESS;
				break;
			case GEOMETRY:
				if (TAG_GEOMETRY.equals(localName))
					mState = State.RESULT;
				break;
			case LOCATION:
				if (TAG_LOCATION.equals(localName))
					mState = State.GEOMETRY;
				break;
			case LATITUDE:
				if (TAG_LATITUDE.equals(localName))
					mState = State.LOCATION;
				break;
			case LONGITUDE:
				if (TAG_LONGITUDE.equals(localName))
					mState = State.LOCATION;
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
			case STATUS:
				if (!STATUS_OK.equals(s))
					mState = State.FINISH;
				break;
			case RESULT_TYPE:
				if (TYPE_POLITICAL.equals(s))
					break;
				mAddress = new Address(mLocale);
				break;
			case RESULT_FORMATTED:
				if (mAddress != null) {
					Bundle extras = mAddress.getExtras();
					if (extras == null) {
						extras = new Bundle();
						mAddress.setExtras(extras);
						extras = mAddress.getExtras();
					}
					extras.putString(KEY_FORMATTED, s);
				}
				break;
			case ADDRESS_LONG:
				mLongName = s;
				break;
			case ADDRESS_SHORT:
				mShortName = s;
				break;
			case ADDRESS_TYPE:
				if (TYPE_POLITICAL.equals(s))
					break;
				if (mAddressType == null)
					mAddressType = s;
				break;
			case LATITUDE:
				if (mAddress != null) {
					try {
						mAddress.setLatitude(Double.parseDouble(s));
					} catch (NumberFormatException nfe) {
						throw new SAXException(nfe);
					}
				}
				break;
			case LONGITUDE:
				if (mAddress != null) {
					try {
						mAddress.setLongitude(Double.parseDouble(s));
					} catch (NumberFormatException nfe) {
						throw new SAXException(nfe);
					}
				}
				break;
			case FINISH:
				return;
			default:
				break;
			}
		}
	}
}
