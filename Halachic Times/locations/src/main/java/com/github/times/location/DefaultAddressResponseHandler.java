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

import android.text.TextUtils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * Handler for parsing the XML response.
 *
 * @author Moshe Waisberg
 */
@Deprecated
public abstract class DefaultAddressResponseHandler extends DefaultHandler2 {

    private final StringBuffer text = new StringBuffer();

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
