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

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Location that is partially stored in the local database.
 *
 * @author Moshe Waisberg
 */
public class ZmanimLocation extends Location {

    /**
     * Minimum valid latitude.
     */
    public static final double LATITUDE_MIN = -90;
    /**
     * Maximum valid latitude.
     */
    public static final double LATITUDE_MAX = 90;
    /**
     * Minimum valid longitude.
     */
    public static final double LONGITUDE_MIN = -180;
    /**
     * Maximum valid longitude.
     */
    public static final double LONGITUDE_MAX = 180;

    /**
     * Lowest possible natural elevation on the surface of the earth.
     */
    public static final double ELEVATION_MIN = -500;
    /**
     * Highest possible natural elevation from the surface of the earth.
     */
    public static final double ELEVATION_MAX = 100_000;

    /**
     * Double subtraction error.
     */
    private static final double EPSILON = 1e-6;

    private static final double RADIANS_180 = Math.PI;
    private static final double RADIANS_360 = RADIANS_180 * 2;
    private static final double RADIANS_45 = RADIANS_180 / 4;

    private long id;

    /**
     * Constructs a new location.
     *
     * @param provider the name of the provider that generated this location.
     */
    public ZmanimLocation(String provider) {
        super(provider);
    }

    /**
     * Construct a new location that is copied from an existing one.
     *
     * @param location the source location.
     */
    public ZmanimLocation(Location location) {
        super(location);
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
     * Returns the approximate initial bearing in degrees East of true
     * North when traveling along the loxodrome path between this
     * location and the given location. The constant bearing path is defined
     * using the Rhumb line.
     *
     * @param dest the destination location
     * @return the initial bearing in degrees
     */
    public float angleTo(Location dest) {
        return angleTo(dest.getLatitude(), dest.getLongitude());
    }

    /**
     * Returns the approximate initial bearing in degrees East of true
     * North when traveling along the loxodrome path between this
     * location and the given location. The constant bearing path is defined
     * using the Rhumb line.
     *
     * @param latitude  the destination latitude, in degrees.
     * @param longitude the destination longitude, in degrees.
     * @return the bearing in degrees.
     */
    public float angleTo(double latitude, double longitude) {
        return (float) computeRhumbBearing(this.getLatitude(), this.getLongitude(), latitude, longitude);
    }

    /**
     * Returns the approximate initial bearing in degrees East of true
     * North when traveling along the loxodrome path between this
     * location and the given location. The constant bearing path is defined
     * using the Rhumb line.
     *
     * @param location    the initial location.
     * @param destination the destination location.
     * @return the bearing in degrees.
     */
    public static float angleTo(Location location, Location destination) {
        return (float) computeRhumbBearing(location.getLatitude(), location.getLongitude(), destination.getLatitude(), destination.getLongitude());
    }

    /**
     * Computes the azimuth angle (clockwise from North) of a Rhumb line (a line of constant heading) between two
     * locations.
     * This method uses a spherical model, not elliptical.
     *
     * @param latitude1  the starting latitude, in degrees.
     * @param longitude1 the starting longitude, in degrees.
     * @param latitude2  the destination longitude, in degrees.
     * @param longitude2 the destination latitude, in degrees.
     * @return teh bearing in degrees.
     */
    private static double computeRhumbBearing(double latitude1, double longitude1, double latitude2, double longitude2) {
        double lat1 = Math.toRadians(latitude1);
        double lng1 = Math.toRadians(longitude1);
        double lat2 = Math.toRadians(latitude2);
        double lng2 = Math.toRadians(longitude2);

        double phi1 = Math.tan(RADIANS_45 + (lat1 / 2));
        double phi2 = Math.tan(RADIANS_45 + (lat2 / 2));
        double dPhi = Math.log(phi2 / phi1);
        double dLon = lng2 - lng1;

        // if dLon over 180° take shorter Rhumb line across the anti-meridian:
        if (Math.abs(dLon) > RADIANS_180)
            dLon = dLon > 0 ? -(RADIANS_360 - dLon) : (RADIANS_360 + dLon);

        double azimuth = Math.atan2(dLon, dPhi);
        if (azimuth < 0) {
            azimuth += RADIANS_360;
        }

        return Math.toDegrees(azimuth);
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeLong(id);
    }

    public static final Parcelable.Creator<ZmanimLocation> CREATOR = new Parcelable.Creator<ZmanimLocation>() {
        @Override
        public ZmanimLocation createFromParcel(Parcel source) {
            Location l = Location.CREATOR.createFromParcel(source);
            ZmanimLocation zl = new ZmanimLocation(l);
            zl.id = source.readLong();
            return zl;
        }

        @Override
        public ZmanimLocation[] newArray(int size) {
            return new ZmanimLocation[size];
        }
    };

    /**
     * Compare two locations by latitude and longitude only.
     *
     * @param l1 the first location.
     * @param l2 the second location.
     * @return the comparison as per {@link Comparable}.
     */
    public static int compare(Location l1, Location l2) {
        if (l1 == l2) {
            return 0;
        }
        if (l1 == null) {
            return -1;
        }
        if (l2 == null) {
            return 1;
        }

        double lat1 = l1.getLatitude();
        double lat2 = l2.getLatitude();
        double latD = lat1 - lat2;
        if (latD >= EPSILON) {
            return 1;
        }
        if (latD <= -EPSILON) {
            return -1;
        }

        double lng1 = l1.getLongitude();
        double lng2 = l2.getLongitude();
        double lngD = lng1 - lng2;
        if (lngD >= EPSILON) {
            return 1;
        }
        if (lngD <= -EPSILON) {
            return -1;
        }

        return 0;
    }

    /**
     * Compare two locations by latitude and then longitude, and then altitude, and then time.
     *
     * @param l1 the first location.
     * @param l2 the second location.
     * @return the comparison as per {@link Comparable}.
     */
    public static int compareAll(Location l1, Location l2) {
        int c = compare(l1, l2);
        if (c != 0) {
            return c;
        }

        double ele1 = l1.hasAltitude() ? l1.getAltitude() : 0;
        double ele2 = l2.hasAltitude() ? l2.getAltitude() : 0;
        double eleD = ele1 - ele2;
        if (eleD >= EPSILON) {
            return 1;
        }
        if (eleD <= -EPSILON) {
            return -1;
        }

        long t1 = l1.getTime();
        long t2 = l2.getTime();
        return (t1 > t2) ? 1 : (t1 < t2 ? -1 : 0);
    }

    public static double toDecimal(int degrees, int minutes, double seconds) {
        return degrees + (minutes / 60.0) + (seconds / 3600.0);
    }

    /**
     * Is the location valid?
     *
     * @param location the location to check.
     * @return {@code false} if location is invalid.
     */
    public static boolean isValid(Location location) {
        if (location == null)
            return false;

        final double latitude = location.getLatitude();
        if ((latitude < LATITUDE_MIN) || (latitude > LATITUDE_MAX))
            return false;

        final double longitude = location.getLongitude();
        if ((longitude < LONGITUDE_MIN) || (longitude > LONGITUDE_MAX))
            return false;

        final double elevation = location.getAltitude();
        if ((elevation < ELEVATION_MIN) || (elevation > ELEVATION_MAX))
            return false;

        return true;
    }

    public static double distanceBetween(Location startLocation, Location endLocation) {
        float[] distances = new float[1];
        distanceBetween(startLocation, endLocation, distances);
        return distances[0];
    }

    public static void distanceBetween(Location startLocation, Location endLocation, float[] distances) {
        distanceBetween(startLocation.getLatitude(), startLocation.getLongitude(),
            endLocation.getLatitude(), endLocation.getLongitude(), distances);
    }
}
