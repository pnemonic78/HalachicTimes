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
        return generateCityId(city.getLatitude(),city.getLongitude());
    }

    public static long generateCityId(double latitude, double longitude) {
        final long fixedPointLatitude = (long) Math.rint(latitude * RATIO) & 0x7FFFFFFFL;
        final long fixedPointLongitude = (long) Math.rint(longitude * RATIO) & 0xFFFFFFFFL;
        return (fixedPointLatitude << 31) | fixedPointLongitude;
    }
}
