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

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.location.Address;

/**
 * A class for handling geocoding and reverse geocoding.
 * 
 * @author Moshe Waisberg
 */
public abstract class GeocoderBase {

	protected final Context mContext;
	protected final Locale mLocale;
	private static SAXParserFactory mParserFactory;
	private static SAXParser mParser;

	/**
	 * Creates a new geocoder.
	 * 
	 * @param context
	 *            the context.
	 */
	public GeocoderBase(Context context) {
		this(context, Locale.getDefault());
	}

	/**
	 * Creates a new geocoder.
	 * 
	 * @param context
	 *            the context.
	 * @param locale
	 *            the locale.
	 */
	public GeocoderBase(Context context, Locale locale) {
		super();
		mContext = context;
		mLocale = locale;
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
	 * Returns an array of Addresses that are known to describe the area
	 * immediately surrounding the given latitude and longitude.
	 * 
	 * @param latitude
	 *            the latitude a point for the search.
	 * @param longitude
	 *            the longitude a point for the search.
	 * @param maxResults
	 *            maximum number of addresses to return. Smaller numbers (1 to
	 *            5) are recommended.
	 * @return a list of addresses. Returns {@code null} or empty list if no
	 *         matches were found or there is no backend service available.
	 * @throws IOException
	 *             if the network is unavailable or any other I/O problem
	 *             occurs.
	 */
	public abstract List<Address> getFromLocation(double latitude, double longitude, int maxResults) throws IOException;

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
	protected List<Address> getXMLFromURL(String queryUrl, int maxResults) throws IOException {
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
	protected List<Address> parseLocations(byte[] data, int maxResults) throws ParserConfigurationException, SAXException, IOException {
		// Minimum length for "<X/>"
		if ((data == null) || (data.length <= 4))
			return null;

		List<Address> results = new ArrayList<Address>(maxResults);
		InputStream in = new ByteArrayInputStream(data);
		SAXParser parser = getParser();
		DefaultHandler handler = createResponseHandler(results, maxResults, mLocale);
		parser.parse(in, handler);

		return results;
	}

	/**
	 * Create an SAX XML handler.
	 * 
	 * @param results
	 *            the list of results to populate.
	 * @param maxResults
	 *            the maximum number of results.
	 * @param locale
	 *            the locale.
	 * @return the XML handler.
	 */
	protected abstract DefaultHandler createResponseHandler(List<Address> results, int maxResults, Locale locale);

	/**
	 * Get the ISO 639 language code.
	 * 
	 * @return the language code.
	 */
	protected String getLanguage() {
		String language = mLocale.getLanguage();
		if ("in".equals(language))
			return "id";
		if ("iw".equals(language))
			return "he";
		if ("ji".equals(language))
			return "yi";
		return language;
	}
}
