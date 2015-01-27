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
import java.util.List;
import java.util.Locale;

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
 * Microsoft Bing API.
 * <p>
 * <a href="http://msdn.microsoft.com/en-us/library/ff701710.aspx">http://msdn.
 * microsoft.com/en-us/library/ff701710.aspx</a>
 * 
 * @author Moshe Waisberg
 */
public class BingGeocoder extends GeocoderBase {

	/** Bing API key. */
	private static final String API_KEY = "BingMapsKey";

	/** URL that accepts latitude and longitude coordinates as parameters. */
	private static final String URL_LATLNG = "http://dev.virtualearth.net/REST/v1/Locations/%f,%f?o=xml&c=%s&key=%s";
	/**
	 * URL that accepts latitude and longitude coordinates as parameters for an
	 * elevation.
	 */
	private static final String URL_ELEVATION = "http://dev.virtualearth.net/REST/v1/Elevation/List?o=xml&points=%f,%f&key=%s";

	/**
	 * Creates a new Bing geocoder.
	 * 
	 * @param context
	 *            the context.
	 */
	public BingGeocoder(Context context) {
		super(context);
	}

	/**
	 * Creates a new Bing geocoder.
	 * 
	 * @param context
	 *            the context.
	 * @param locale
	 *            the locale.
	 */
	public BingGeocoder(Context context, Locale locale) {
		super(context, locale);
	}

	@Override
	public List<Address> getFromLocation(double latitude, double longitude, int maxResults) throws IOException {
		if (latitude < -90.0 || latitude > 90.0)
			throw new IllegalArgumentException("latitude == " + latitude);
		if (longitude < -180.0 || longitude > 180.0)
			throw new IllegalArgumentException("longitude == " + longitude);
		String queryUrl = String.format(Locale.US, URL_LATLNG, latitude, longitude, getLanguage(), API_KEY);
		return getAddressXMLFromURL(queryUrl, maxResults);
	}

	@Override
	protected DefaultHandler createAddressResponseHandler(List<Address> results, int maxResults, Locale locale) {
		return new AddressResponseHandler(results, maxResults, locale);
	}

	/**
	 * Handler for parsing the XML response.
	 * 
	 * @author Moshe
	 */
	protected static class AddressResponseHandler extends DefaultHandler2 {

		/** Parse state. */
		private enum State {
			START, ROOT, STATUS, RESOURCE_SETS, RESOURCE_SET, RESOURCES, LOCATION, POINT, ADDRESS, FINISH
		};

		private static final String STATUS_OK = "200";

		private static final String TAG_ROOT = "Response";
		private static final String TAG_STATUS = "StatusCode";
		private static final String TAG_RESOURCE_SETS = "ResourceSets";
		private static final String TAG_RESOURCE_SET = "ResourceSet";
		private static final String TAG_RESOURCES = "Resources";
		private static final String TAG_LOCATION = "Location";
		private static final String TAG_NAME = "Name";
		private static final String TAG_ADDRESS = "Address";
		private static final String TAG_ADDRESS_LINE = "AddressLine";
		private static final String TAG_ADDRESS_DISTRICT = "AdminDistrict";
		private static final String TAG_ADDRESS_COUNTRY = "CountryRegion";
		private static final String TAG_FORMATTED = "FormattedAddress";
		private static final String TAG_LOCALITY = "Locality";
		private static final String TAG_POINT = "Point";
		private static final String TAG_LATITUDE = "Latitude";
		private static final String TAG_LONGITUDE = "Longitude";

		private State mState = State.START;
		private final List<Address> mResults;
		private final int mMaxResults;
		private final Locale mLocale;
		private Address mAddress;
		private String mTag;

		/**
		 * Constructs a new parse handler.
		 * 
		 * @param results
		 *            the destination results.
		 * @param maxResults
		 *            the maximum number of results.
		 */
		public AddressResponseHandler(List<Address> results, int maxResults, Locale locale) {
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

			mTag = localName;

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
				else if (TAG_RESOURCE_SETS.equals(localName))
					mState = State.RESOURCE_SETS;
				break;
			case RESOURCE_SETS:
				if (TAG_RESOURCE_SET.equals(localName))
					mState = State.RESOURCE_SET;
				break;
			case RESOURCE_SET:
				if (TAG_RESOURCES.equals(localName))
					mState = State.RESOURCES;
				break;
			case RESOURCES:
				if (TAG_LOCATION.equals(localName)) {
					mState = State.LOCATION;
					mAddress = new ZmanimAddress(mLocale);
				}
				break;
			case LOCATION:
				if (TAG_POINT.equals(localName))
					mState = State.POINT;
				else if (TAG_ADDRESS.equals(localName))
					mState = State.ADDRESS;
				break;
			case POINT:
				break;
			case ADDRESS:
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

			mTag = localName;

			switch (mState) {
			case ROOT:
				if (TAG_ROOT.equals(localName))
					mState = State.FINISH;
				break;
			case STATUS:
				if (TAG_STATUS.equals(localName))
					mState = State.ROOT;
				break;
			case RESOURCE_SETS:
				if (TAG_RESOURCE_SETS.equals(localName))
					mState = State.ROOT;
				break;
			case RESOURCE_SET:
				if (TAG_RESOURCE_SET.equals(localName))
					mState = State.RESOURCE_SETS;
				break;
			case RESOURCES:
				if (TAG_RESOURCES.equals(localName))
					mState = State.RESOURCE_SET;
				break;
			case LOCATION:
				if (TAG_LOCATION.equals(localName)) {
					if (mAddress != null) {
						if ((mResults.size() < mMaxResults) && mAddress.hasLatitude() && mAddress.hasLongitude())
							mResults.add(mAddress);
						else
							mState = State.FINISH;
						mAddress = null;
					}
					mState = State.RESOURCES;
				}
				break;
			case POINT:
				if (TAG_POINT.equals(localName))
					mState = State.LOCATION;
				break;
			case ADDRESS:
				if (TAG_ADDRESS.equals(localName))
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
			String prev;

			switch (mState) {
			case STATUS:
				if (!STATUS_OK.equals(s))
					mState = State.FINISH;
				break;
			case LOCATION:
				if (mAddress != null) {
					if (TAG_NAME.equals(mTag)) {
						prev = mAddress.getFeatureName();
						mAddress.setFeatureName((prev == null) ? s : prev + s);
					}
				}
			case POINT:
				if (mAddress != null) {
					if (TAG_LATITUDE.equals(mTag)) {
						try {
							mAddress.setLatitude(Double.parseDouble(s));
						} catch (NumberFormatException nfe) {
							throw new SAXException(nfe);
						}
					} else if (TAG_LONGITUDE.equals(mTag)) {
						try {
							mAddress.setLongitude(Double.parseDouble(s));
						} catch (NumberFormatException nfe) {
							throw new SAXException(nfe);
						}
					}
				}
				break;
			case ADDRESS:
				if (mAddress != null) {
					if (TAG_ADDRESS_LINE.equals(mTag)) {
						prev = mAddress.getAddressLine(0);
						mAddress.setAddressLine(0, (prev == null) ? s : prev + s);
					} else if (TAG_ADDRESS_DISTRICT.equals(mTag)) {
						prev = mAddress.getAdminArea();
						mAddress.setAdminArea((prev == null) ? s : prev + s);
					} else if (TAG_ADDRESS_COUNTRY.equals(mTag)) {
						prev = mAddress.getCountryName();
						mAddress.setCountryName((prev == null) ? s : prev + s);
					} else if (TAG_FORMATTED.equals(mTag)) {
						Bundle extras = mAddress.getExtras();
						if (extras == null) {
							extras = new Bundle();
							mAddress.setExtras(extras);
							extras = mAddress.getExtras();
						}
						prev = extras.getString(ZmanimAddress.KEY_FORMATTED);
						extras.putString(ZmanimAddress.KEY_FORMATTED, (prev == null) ? s : prev + s);
					} else if (TAG_LOCALITY.equals(mTag)) {
						prev = mAddress.getLocality();
						mAddress.setLocality((prev == null) ? s : prev + s);
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

	@Override
	public ZmanimLocation getElevation(double latitude, double longitude) throws IOException {
		if (latitude < -90.0 || latitude > 90.0)
			throw new IllegalArgumentException("latitude == " + latitude);
		if (longitude < -180.0 || longitude > 180.0)
			throw new IllegalArgumentException("longitude == " + longitude);
		String queryUrl = String.format(Locale.US, URL_ELEVATION, latitude, longitude, API_KEY);
		ZmanimLocation location = getElevationXMLFromURL(queryUrl);
		if (location != null) {
			location.setLatitude(latitude);
			location.setLongitude(longitude);
		}
		return location;
	}

	@Override
	protected DefaultHandler createElevationResponseHandler(List<ZmanimLocation> results) {
		return new ElevationResponseHandler(results);
	}

	/**
	 * Handler for parsing the XML response.
	 * 
	 * @author Moshe
	 */
	protected static class ElevationResponseHandler extends DefaultHandler2 {

		/** Parse state. */
		private enum State {
			START, ROOT, STATUS, RESOURCE_SETS, RESOURCE_SET, RESOURCES, ELEVATION_DATA, ELEVATION, FINISH
		};

		private static final String STATUS_OK = "200";

		private static final String TAG_ROOT = "Response";
		private static final String TAG_STATUS = "StatusCode";
		private static final String TAG_RESOURCE_SETS = "ResourceSets";
		private static final String TAG_RESOURCE_SET = "ResourceSet";
		private static final String TAG_RESOURCES = "Resources";
		private static final String TAG_ELEVATION_DATA = "ElevationData";
		private static final String TAG_ELEVATIONS = "Elevations";
		private static final String TAG_ELEVATION = "int";

		private State mState = State.START;
		private final List<ZmanimLocation> mResults;
		private ZmanimLocation mLocation;
		private String mTag;

		/**
		 * Constructs a new parse handler.
		 * 
		 * @param results
		 *            the destination results.
		 */
		public ElevationResponseHandler(List<ZmanimLocation> results) {
			super();
			mResults = results;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);
			if (TextUtils.isEmpty(localName))
				localName = qName;

			mTag = localName;

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
				else if (TAG_RESOURCE_SETS.equals(localName))
					mState = State.RESOURCE_SETS;
				break;
			case RESOURCE_SETS:
				if (TAG_RESOURCE_SET.equals(localName))
					mState = State.RESOURCE_SET;
				break;
			case RESOURCE_SET:
				if (TAG_RESOURCES.equals(localName))
					mState = State.RESOURCES;
				break;
			case RESOURCES:
				if (TAG_ELEVATION_DATA.equals(localName)) {
					mState = State.ELEVATION_DATA;
					mLocation = new ZmanimLocation(USER_PROVIDER);
					mLocation.setTime(System.currentTimeMillis());
				}
				break;
			case ELEVATION_DATA:
				if (TAG_ELEVATIONS.equals(localName))
					mState = State.ELEVATION;
				break;
			case ELEVATION:
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

			mTag = localName;

			switch (mState) {
			case ROOT:
				if (TAG_ROOT.equals(localName))
					mState = State.FINISH;
				break;
			case STATUS:
				if (TAG_STATUS.equals(localName))
					mState = State.ROOT;
				break;
			case RESOURCE_SETS:
				if (TAG_RESOURCE_SETS.equals(localName))
					mState = State.ROOT;
				break;
			case RESOURCE_SET:
				if (TAG_RESOURCE_SET.equals(localName))
					mState = State.RESOURCE_SETS;
				break;
			case RESOURCES:
				if (TAG_RESOURCES.equals(localName))
					mState = State.RESOURCE_SET;
				break;
			case ELEVATION_DATA:
				if (TAG_ELEVATION_DATA.equals(localName)) {
					if (mLocation != null) {
						if (mLocation.hasAltitude())
							mResults.add(mLocation);
						else
							mState = State.FINISH;
						mLocation = null;
					}
					mState = State.RESOURCES;
				}
				break;
			case ELEVATION:
				if (TAG_ELEVATIONS.equals(localName))
					mState = State.ELEVATION_DATA;
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
			case ELEVATION:
				if (mLocation != null) {
					if (TAG_ELEVATION.equals(mTag)) {
						try {
							mLocation.setAltitude(Double.parseDouble(s));
						} catch (NumberFormatException nfe) {
							throw new SAXException(nfe);
						}
					}
				}
			case FINISH:
				return;
			default:
				break;
			}
		}
	}

}
