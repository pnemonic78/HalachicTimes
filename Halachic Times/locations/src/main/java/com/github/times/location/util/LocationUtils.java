package com.github.times.location.util;

import android.annotation.TargetApi;
import android.location.Location;
import android.os.PersistableBundle;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

/**
 * @author moshe on 2017/09/28.
 */
public class LocationUtils {

    private static final String KEY_PROVIDER = "provider";
    private static final String KEY_TIME = "time";
    private static final String KEY_ELAPSED_REALTIME_NANOS = "elapsed_realtime_nanos";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_ALTITUDE = "altitude";
    private static final String KEY_SPEED = "speed";
    private static final String KEY_BEARING = "bearing";
    private static final String KEY_ACCURACY = "accuracy";
    private static final String KEY_EXTRAS = "extras";

    private LocationUtils() {
    }

    @TargetApi(LOLLIPOP)
    public static void writeParcelable(String key, Location value, android.os.PersistableBundle dest) {
        android.os.PersistableBundle bundle = new android.os.PersistableBundle();
        bundle.putString(KEY_PROVIDER, value.getProvider());
        bundle.putLong(KEY_TIME, value.getTime());
        bundle.putLong(KEY_ELAPSED_REALTIME_NANOS, value.getElapsedRealtimeNanos());
        bundle.putDouble(KEY_LATITUDE, value.getLatitude());
        bundle.putDouble(KEY_LONGITUDE, value.getLongitude());
        if (value.hasAltitude()) {
            bundle.putDouble(KEY_ALTITUDE, value.getAltitude());
        } else {
            bundle.putDouble(KEY_ALTITUDE, Double.NaN);
        }
        if (value.hasSpeed()) {
            bundle.putDouble(KEY_SPEED, value.getSpeed());
        } else {
            bundle.putDouble(KEY_SPEED, Double.NaN);
        }
        bundle.putDouble(KEY_BEARING, value.getBearing());
        if (value.hasAccuracy()) {
            bundle.putDouble(KEY_ACCURACY, value.getAccuracy());
        } else {
            bundle.putDouble(KEY_ACCURACY, Double.NaN);
        }

        dest.putPersistableBundle(key, bundle);
    }

    @TargetApi(LOLLIPOP)
    public static Location readParcelable(PersistableBundle src, String key) {
        android.os.PersistableBundle bundle = src.getPersistableBundle(key);
        if ((bundle == null) || bundle.isEmpty()) {
            return null;
        }

        Location location = new Location(bundle.getString(KEY_PROVIDER));
        location.setTime(bundle.getLong(KEY_TIME));
        location.setElapsedRealtimeNanos(bundle.getLong(KEY_ELAPSED_REALTIME_NANOS));
        location.setLatitude(bundle.getDouble(KEY_LATITUDE));
        location.setLongitude(bundle.getDouble(KEY_LONGITUDE));
        double altitude = bundle.getDouble(KEY_ALTITUDE);
        if (!Double.isNaN(altitude)) {
            location.setAltitude(altitude);
        }
        double speed = bundle.getDouble(KEY_SPEED);
        if (!Double.isNaN(speed)) {
            location.setSpeed((float) speed);
        }
        location.setBearing((float) bundle.getDouble(KEY_BEARING));
        double accuracy = bundle.getDouble(KEY_ACCURACY);
        if (!Double.isNaN(accuracy)) {
            location.setAccuracy((float) accuracy);
        }

        return location;
    }
}
