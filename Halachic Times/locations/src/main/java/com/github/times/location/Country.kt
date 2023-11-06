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
