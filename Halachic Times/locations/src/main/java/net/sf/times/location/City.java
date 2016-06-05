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
import android.os.Parcelable;

import java.util.Locale;

/**
 * City that is stored in the application binary.
 *
 * @author Moshe Waisberg
 */
public class City extends ZmanimAddress {

    /** Factor to convert a fixed-point integer to double. */
    private static final double RATIO = CountryPolygon.RATIO;

    public City(Locale locale) {
        super(locale);
    }

    public City(Address address) {
        super(address);
    }

    public City(ZmanimAddress address) {
        super(address);
    }

    public static final Parcelable.Creator<City> CREATOR = new Parcelable.Creator<City>() {
        @Override
        public City createFromParcel(Parcel source) {
            ZmanimAddress a = ZmanimAddress.CREATOR.createFromParcel(source);
            City city = new City(a);
            return city;
        }

        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };

    public static long generateCityId(City city) {
        final long fixedPointLatitude = (long) Math.rint(city.getLatitude() * RATIO) & 0x7FFFFFFFL;
        final long fixedPointLongitude = (long) Math.rint(city.getLongitude() * RATIO) & 0xFFFFFFFFL;
        return (fixedPointLatitude << 31) | fixedPointLongitude;
    }
}
