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
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

import static android.text.TextUtils.isEmpty;

/**
 * Address that is stored in the local database.
 *
 * @author Moshe Waisberg
 */
public class ZmanimAddress extends Address implements Comparable<ZmanimAddress> {

    /**
     * Key to store the formatted address, instead of formatting it ourselves
     * elsewhere.
     */
    public static final String KEY_FORMATTED = "formatted_address";

    /**
     * ISO 639 country code for Israel.
     */
    public static final String ISO639_ISRAEL = "IL";
    /**
     * ISO 639 country code for Palestine.
     */
    public static final String ISO639_PALESTINE = "PS";

    /**
     * Address field separator.
     */
    private static final String ADDRESS_SEPARATOR = ", ";
    /**
     * Double subtraction error.
     */
    private static final double EPSILON = 1e-6;

    private long id;
    private double elevation;
    private boolean hasElevation;
    private String formatted;
    private boolean favorite;

    /**
     * Constructs a new address.
     *
     * @param locale the locale.
     */
    public ZmanimAddress(Locale locale) {
        super(locale);
    }

    /**
     * Constructs a new address.
     *
     * @param address the source address.
     */
    public ZmanimAddress(Address address) {
        super(address.getLocale());

        int index = 0;
        String line = address.getAddressLine(index);
        while (line != null) {
            setAddressLine(index, line);
            index++;
            line = address.getAddressLine(index);
        }
        setAdminArea(address.getAdminArea());
        setCountryCode(address.getCountryCode());
        setCountryName(address.getCountryName());
        setExtras(address.getExtras());
        setFeatureName(address.getFeatureName());
        setLatitude(address.getLatitude());
        setLocality(address.getLocality());
        setLongitude(address.getLongitude());
        setPhone(address.getPhone());
        setPostalCode(address.getPostalCode());
        setPremises(address.getPremises());
        setSubAdminArea(address.getSubAdminArea());
        setSubLocality(address.getSubLocality());
        setSubThoroughfare(address.getSubThoroughfare());
        setThoroughfare(address.getThoroughfare());
        setUrl(address.getUrl());
    }

    /**
     * Constructs a new address.
     *
     * @param address the source address.
     */
    public ZmanimAddress(ZmanimAddress address) {
        this((Address) address);
        setId(address.getId());
        if (address.hasElevation()) {
            setElevation(address.getElevation());
        }
        setFormatted(address.getFormatted());
        setFavorite(address.isFavorite());
    }

    /**
     * Get the id.
     *
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Set the id.
     *
     * @param id the id.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Get the formatted address.
     *
     * @return the address
     */
    public String getFormatted() {
        String formatted = this.formatted;
        if (formatted == null) {
            formatted = format();
            this.formatted = formatted;
        }
        return formatted;
    }

    /**
     * Set the formatted address.
     *
     * @param formatted the address.
     */
    public void setFormatted(String formatted) {
        this.formatted = formatted;
    }

    /**
     * Is favourite address?
     *
     * @return {@code true} if favourite.
     */
    public boolean isFavorite() {
        return favorite;
    }

    /**
     * Mark the address as a favourite.
     *
     * @param favorite is favourite?
     */
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    /**
     * Set the elevation.
     *
     * @param elevation the elevation in metres.
     */
    public void setElevation(double elevation) {
        this.elevation = elevation;
        hasElevation = true;
    }

    /**
     * Get the elevation.
     *
     * @return the elevation in metres.
     */
    public double getElevation() {
        if (hasElevation) {
            return elevation;
        }
        throw new IllegalStateException();
    }

    /**
     * Returns true if an elevation has been assigned to this Address, false
     * otherwise.
     */
    public boolean hasElevation() {
        return hasElevation;
    }

    /**
     * Format the address.
     *
     * @return the formatted address.
     */
    protected String format() {
        Bundle extras = getExtras();
        String formatted = (extras == null) ? null : extras.getString(KEY_FORMATTED);
        if (formatted != null)
            return formatted;

        StringBuilder buf = new StringBuilder();
        String feature = getFeatureName();
        String premises = getPremises();
        String thoroughfare = getThoroughfare();
        int addressLinesCount = getMaxAddressLineIndex() + 1;
        String address;
        String subloc = getSubLocality();
        String locality = getLocality();
        String subadmin = getSubAdminArea();
        String admin = getAdminArea();
        String country = getCountryName();

        if (!isEmpty(feature)) {
            if (buf.length() > 0)
                buf.append(ADDRESS_SEPARATOR);
            buf.append(feature);
        }
        if (!isEmpty(premises)
            && !premises.equals(feature)) {
            if (buf.length() > 0)
                buf.append(ADDRESS_SEPARATOR);
            buf.append(premises);
        }
        if (!isEmpty(thoroughfare)
            && !thoroughfare.equals(feature)
            && !thoroughfare.equals(premises)) {
            if (buf.length() > 0)
                buf.append(ADDRESS_SEPARATOR);
            buf.append(thoroughfare);
        }
        if (addressLinesCount >= 0) {
            for (int i = 0; i < addressLinesCount; i++) {
                address = getAddressLine(i);
                if (!isEmpty(address)
                    && ((thoroughfare == null) || !address.contains(thoroughfare))
                    && ((premises == null) || !address.contains(premises))
                    && ((subloc == null) || !address.contains(subloc))
                    && ((locality == null) || !address.contains(locality))
                    && ((subadmin == null) || !address.contains(subadmin))
                    && ((admin == null) || !address.contains(admin))
                    && ((country == null) || !address.contains(country))) {
                    if (buf.length() > 0)
                        buf.append(ADDRESS_SEPARATOR);
                    buf.append(address);
                }
            }
        }
        if (!isEmpty(subloc)
            && !subloc.equals(thoroughfare)
            && !subloc.equals(feature)) {
            if (buf.length() > 0)
                buf.append(ADDRESS_SEPARATOR);
            buf.append(subloc);
        }
        if (!isEmpty(locality)
            && !locality.equals(subloc)
            && !locality.equals(feature)) {
            if (buf.length() > 0)
                buf.append(ADDRESS_SEPARATOR);
            buf.append(locality);
        }
        if (!isEmpty(subadmin)
            && !subadmin.equals(locality)
            && !subadmin.equals(subloc)
            && !subadmin.equals(feature)) {
            if (buf.length() > 0)
                buf.append(ADDRESS_SEPARATOR);
            buf.append(subadmin);
        }
        if (!isEmpty(admin)
            && !admin.equals(subadmin)
            && !admin.equals(locality)
            && !admin.equals(subloc)
            && !admin.equals(feature)) {
            if (buf.length() > 0)
                buf.append(ADDRESS_SEPARATOR);
            buf.append(admin);
        }
        if (!isEmpty(country) && !country.equals(feature)) {
            if (buf.length() > 0)
                buf.append(ADDRESS_SEPARATOR);
            buf.append(country);
        }

        if (buf.length() == 0) {
            Locale locale = getLocale();
            return locale.getDisplayCountry(locale);
        }

        return buf.toString();
    }

    /**
     * Compare two addresses by latitude and longitude only.
     *
     * @param a1 the first address.
     * @param a2 the second address.
     * @return the comparison as per {@link Comparable}.
     */
    public static int compare(Address a1, Address a2) {
        if (a1 == a2) {
            return 0;
        }
        if (a1 == null) {
            return -1;
        }
        if (a2 == null) {
            return 1;
        }

        double lat1 = a1.getLatitude();
        double lat2 = a2.getLatitude();
        double latD = lat1 - lat2;
        if (latD >= EPSILON)
            return 1;
        if (latD <= -EPSILON)
            return -1;

        double lng1 = a1.getLongitude();
        double lng2 = a2.getLongitude();
        double lngD = lng1 - lng2;
        if (lngD >= EPSILON)
            return 1;
        if (lngD <= -EPSILON)
            return -1;

        // Don't need to compare elevation.
        return 0;
    }

    @Override
    public int compareTo(ZmanimAddress that) {
        int c = compare(this, that);
        if (c != 0)
            return c;

        String format1 = this.getFormatted();
        String format2 = that.getFormatted();
        c = format1.compareToIgnoreCase(format2);
        if (c != 0)
            return c;

        // Long.compare since API 19+.
        long id1 = this.getId();
        long id2 = that.getId();
        return (id1 < id2) ? -1 : (id1 == id2 ? 0 : 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ZmanimAddress) {
            return compareTo((ZmanimAddress) obj) == 0;
        }
        return super.equals(obj);
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeLong(id);
        parcel.writeDouble(elevation);
        parcel.writeString(formatted);
        parcel.writeInt(favorite ? 1 : 0);
    }

    public static final Parcelable.Creator<ZmanimAddress> CREATOR = new Parcelable.Creator<ZmanimAddress>() {
        @Override
        public ZmanimAddress createFromParcel(Parcel source) {
            Address a = Address.CREATOR.createFromParcel(source);
            ZmanimAddress za = new ZmanimAddress(a);
            za.id = source.readLong();
            za.elevation = source.readDouble();
            za.formatted = source.readString();
            za.favorite = source.readInt() != 0;
            return za;
        }

        @Override
        public ZmanimAddress[] newArray(int size) {
            return new ZmanimAddress[size];
        }
    };

    public void setCountryCode(String countryCode) {
        if (ISO639_PALESTINE.equals(countryCode)) {
            countryCode = ISO639_ISRAEL;
            Locale locale = new Locale(getLocale().getLanguage(), countryCode);
            setCountryName(locale.getDisplayCountry(locale));
        }
        super.setCountryCode(countryCode);
    }

    @Override
    public void setCountryName(String countryName) {
        if (getCountryName() != null)
            return;
        super.setCountryName(countryName);
    }
}
