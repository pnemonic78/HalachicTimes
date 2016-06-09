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

import android.location.Address;
import android.os.Parcel;

import java.util.Locale;

/**
 * Country that is stored in the application binary.
 *
 * @author Moshe Waisberg
 */
public class Country extends ZmanimAddress {

    /** Factor to convert a fixed-point integer to double. */
    private static final double RATIO = CountryPolygon.RATIO;

    public Country(Locale locale) {
        super(locale);
    }

    public Country(Address address) {
        super(address);
    }

    public Country(ZmanimAddress address) {
        super(address);
    }

    public static final Creator<Country> CREATOR = new Creator<Country>() {
        @Override
        public Country createFromParcel(Parcel source) {
            ZmanimAddress a = ZmanimAddress.CREATOR.createFromParcel(source);
            Country city = new Country(a);
            return city;
        }

        @Override
        public Country[] newArray(int size) {
            return new Country[size];
        }
    };

    public static long generateCountryId(Country country) {
        final long fixedPointLatitude = (long) Math.rint(country.getLatitude() * RATIO) & 0x7FFFFFFFL;
        final long fixedPointLongitude = (long) Math.rint(country.getLongitude() * RATIO) & 0xFFFFFFFFL;
        return -((fixedPointLatitude << 31) | fixedPointLongitude);
    }
}
