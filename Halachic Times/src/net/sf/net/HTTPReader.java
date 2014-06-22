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
package net.sf.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import net.sf.io.StreamUtils;

/**
 * HTTP reader.
 * 
 * @author Moshe Waisberg
 */
public class HTTPReader {

	/** Content type that is XML text. */
	public static final String CONTENT_XML = "application/xml";
	/** Content type that is XML text. */
	public static final String CONTENT_TEXT_XML = "text/xml";

	/** Creates a new reader. */
	public HTTPReader() {
		super();
	}

	/**
	 * Read bytes from the network.
	 * 
	 * @param url
	 *            the URL.
	 * @param contentTypeExpected
	 *            the expected content type.
	 * @return the data - {@code null} otherwise.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public static byte[] read(URL url, String contentTypeExpected) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		int code = conn.getResponseCode();
		if (code != HttpURLConnection.HTTP_OK)
			return null;
		String contentType = conn.getContentType();
		if (contentType == null)
			return null;
		int indexSemi = contentType.indexOf(';');
		if (indexSemi >= 0)
			contentType = contentType.substring(0, indexSemi);
		if ((contentTypeExpected != null) && !contentType.equals(contentTypeExpected))
			return null;

		byte[] data = null;
		InputStream in = null;
		try {
			in = conn.getInputStream();
			// Do NOT use Content-Length header for an exact buffer size!
			// It is not always reliable / accurate.
			final int outSize = Math.max(in.available(), conn.getContentLength());
			ByteArrayOutputStream out = StreamUtils.readFully(in, outSize);
			data = out.toByteArray();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
			conn.disconnect();
		}
		return data;
	}

}
