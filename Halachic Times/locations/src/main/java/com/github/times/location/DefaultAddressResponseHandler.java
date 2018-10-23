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
package com.github.times.location;

import android.location.Address;
import android.text.TextUtils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import java.util.List;
import java.util.Locale;

/**
 * Handler for parsing the XML response.
 *
 * @author Moshe Waisberg
 */
@Deprecated
public abstract class DefaultAddressResponseHandler extends DefaultHandler2 {

    protected final List<Address> results;
    protected final int maxResults;
    protected final Locale locale;
    private final StringBuffer text = new StringBuffer();

    /**
     * Constructs a new parse handler.
     *
     * @param results    the destination results.
     * @param maxResults max number of addresses to return. Smaller numbers (1 to 5) are recommended.
     * @param locale     the addresses' locale.
     */
    public DefaultAddressResponseHandler(List<Address> results, int maxResults, Locale locale) {
        this.results = results;
        this.maxResults = maxResults;
        this.locale = locale;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (TextUtils.isEmpty(localName))
            localName = qName;

        text.delete(0, text.length());

        startElement(uri, localName, attributes);
    }

    protected void startElement(String uri, String localName, Attributes attributes) throws SAXException {
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        if (length == 0)
            return;
        text.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (TextUtils.isEmpty(localName))
            localName = qName;

        endElement(uri, localName, qName, text.toString().trim());
    }

    protected void endElement(String uri, String localName, String qName, String text) throws SAXException {
    }
}
