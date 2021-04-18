package com.github.times.remind;

import android.location.Location;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.work.Data;

import java.util.Collection;

public class LocationData {

    private static final String DATA_LOCATION = Location.class.getName();
    private static final String DATA_KEY_SUFFIX = "/" + DATA_LOCATION;
    private static final String DATA_SUFFIX = DATA_KEY_SUFFIX + ".";

    private static final String DATA_LATITUDE = DATA_SUFFIX + "Latitude";
    private static final String DATA_LONGITUDE = DATA_SUFFIX + "Longitude";
    private static final String DATA_PROVIDER = DATA_SUFFIX + "Provider";
    private static final String DATA_TIME = DATA_SUFFIX + "Time";
    private static final String DATA_ELAPSED_REALTIME_NANOS = DATA_SUFFIX + "ElapsedRealtimeNanos";
    private static final String DATA_ELAPSED_REALTIME_UNCERTAINTY_NANOS = DATA_SUFFIX + "ElapsedRealtimeUncertaintyNanos";
    private static final String DATA_ALTITUDE = DATA_SUFFIX + "Altitude";
    private static final String DATA_SPEED = DATA_SUFFIX + "Speed";
    private static final String DATA_BEARING = DATA_SUFFIX + "Bearing";
    private static final String DATA_ACCURACY = DATA_SUFFIX + "Accuracy";
    private static final String DATA_VERTICAL_ACCURACY_METERS = DATA_SUFFIX + "VerticalAccuracyMeters";
    private static final String DATA_SPEED_ACCURACY_METERSPERSECOND = DATA_SUFFIX + "SpeedAccuracyMetersPerSecond";
    private static final String DATA_BEARING_ACCURACY_DEGREES = DATA_SUFFIX + "BearingAccuracyDegrees";

    public static void writeToData(Data.Builder data, String key, Location location) {
        data.putString(key + DATA_KEY_SUFFIX, key);

        data.putString(key + DATA_PROVIDER, location.getProvider());
        data.putLong(key + DATA_TIME, location.getTime());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            data.putLong(key + DATA_ELAPSED_REALTIME_NANOS, location.getElapsedRealtimeNanos());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            data.putDouble(key + DATA_ELAPSED_REALTIME_UNCERTAINTY_NANOS, location.getElapsedRealtimeUncertaintyNanos());
        }
        data.putDouble(key + DATA_LATITUDE, location.getLatitude());
        data.putDouble(key + DATA_LONGITUDE, location.getLongitude());
        data.putDouble(key + DATA_ALTITUDE, location.getAltitude());
        data.putFloat(key + DATA_SPEED, location.getSpeed());
        data.putFloat(key + DATA_BEARING, location.getBearing());
        data.putFloat(key + DATA_ACCURACY, location.getAccuracy());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            data.putFloat(key + DATA_VERTICAL_ACCURACY_METERS, location.getVerticalAccuracyMeters());
            data.putFloat(key + DATA_SPEED_ACCURACY_METERSPERSECOND, location.getSpeedAccuracyMetersPerSecond());
            data.putFloat(key + DATA_BEARING_ACCURACY_DEGREES, location.getBearingAccuracyDegrees());
        }
    }

    @Nullable
    public static Location readFromData(Data data, String key, Collection<String> keysToRemove) {
        if (!key.endsWith(DATA_KEY_SUFFIX)) return null;
        String value = data.getString(key);
        String keyExtra = getKey(key);
        if ((keyExtra == null || !keyExtra.equals(value))) return null;
        keysToRemove.add(key);
        key = keyExtra;

        String provider = data.getString(key + DATA_PROVIDER);
        Location location = new Location(provider);
        location.setTime(data.getLong(key + DATA_TIME, 0L));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            location.setElapsedRealtimeNanos(data.getLong(key + DATA_ELAPSED_REALTIME_NANOS, 0L));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            location.setElapsedRealtimeUncertaintyNanos(data.getDouble(key + DATA_ELAPSED_REALTIME_UNCERTAINTY_NANOS, Double.NaN));
        }
        location.setLatitude(data.getDouble(key + DATA_LATITUDE, Double.NaN));
        location.setLongitude(data.getDouble(key + DATA_LONGITUDE, Double.NaN));
        location.setAltitude(data.getDouble(key + DATA_ALTITUDE, Double.NaN));
        location.setSpeed(data.getFloat(key + DATA_SPEED, Float.NaN));
        location.setBearing(data.getFloat(key + DATA_BEARING, Float.NaN));
        location.setAccuracy(data.getFloat(key + DATA_ACCURACY, Float.NaN));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            location.setVerticalAccuracyMeters(data.getFloat(key + DATA_VERTICAL_ACCURACY_METERS, Float.NaN));
            location.setSpeedAccuracyMetersPerSecond(data.getFloat(key + DATA_SPEED_ACCURACY_METERSPERSECOND, Float.NaN));
            location.setBearingAccuracyDegrees(data.getFloat(key + DATA_BEARING_ACCURACY_DEGREES, Float.NaN));
        }

        keysToRemove.add(key + DATA_ACCURACY);
        keysToRemove.add(key + DATA_ALTITUDE);
        keysToRemove.add(key + DATA_BEARING);
        keysToRemove.add(key + DATA_BEARING_ACCURACY_DEGREES);
        keysToRemove.add(key + DATA_ELAPSED_REALTIME_NANOS);
        keysToRemove.add(key + DATA_ELAPSED_REALTIME_UNCERTAINTY_NANOS);
        keysToRemove.add(key + DATA_LATITUDE);
        keysToRemove.add(key + DATA_LONGITUDE);
        keysToRemove.add(key + DATA_PROVIDER);
        keysToRemove.add(key + DATA_SPEED);
        keysToRemove.add(key + DATA_SPEED_ACCURACY_METERSPERSECOND);
        keysToRemove.add(key + DATA_TIME);
        keysToRemove.add(key + DATA_VERTICAL_ACCURACY_METERS);

        return location;
    }

    @Nullable
    public static String getKey(String key) {
        if (TextUtils.isEmpty(key)) return null;
        int index = key.indexOf(DATA_KEY_SUFFIX);
        if (index > 0) {
            return key.substring(0, index);
        }
        return null;
    }
}
