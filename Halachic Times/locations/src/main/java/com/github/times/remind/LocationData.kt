package com.github.times.remind

import android.location.Location
import android.os.Build

object LocationData {
    private val DATA_LOCATION = Location::class.java.name
    private val DATA_KEY_SUFFIX = "/${DATA_LOCATION}"
    private val DATA_PREFIX = "${DATA_KEY_SUFFIX}."
    private val DATA_LATITUDE = DATA_PREFIX + "Latitude"
    private val DATA_LONGITUDE = DATA_PREFIX + "Longitude"
    private val DATA_PROVIDER = DATA_PREFIX + "Provider"
    private val DATA_TIME = DATA_PREFIX + "Time"
    private val DATA_ELAPSED_REALTIME_NANOS = DATA_PREFIX + "ElapsedRealtimeNanos"
    private val DATA_ELAPSED_REALTIME_UNCERTAINTY_NANOS =
        DATA_PREFIX + "ElapsedRealtimeUncertaintyNanos"
    private val DATA_ALTITUDE = DATA_PREFIX + "Altitude"
    private val DATA_SPEED = DATA_PREFIX + "Speed"
    private val DATA_BEARING = DATA_PREFIX + "Bearing"
    private val DATA_ACCURACY = DATA_PREFIX + "Accuracy"
    private val DATA_VERTICAL_ACCURACY_METERS = DATA_PREFIX + "VerticalAccuracyMeters"
    private val DATA_SPEED_ACCURACY_METERSPERSECOND = DATA_PREFIX + "SpeedAccuracyMetersPerSecond"
    private val DATA_BEARING_ACCURACY_DEGREES = DATA_PREFIX + "BearingAccuracyDegrees"

    fun writeToData(data: androidx.work.Data.Builder, key: String, location: Location) {
        data.putString(key + DATA_KEY_SUFFIX, key)
        data.putString(key + DATA_PROVIDER, location.provider)
        data.putLong(key + DATA_TIME, location.time)
        data.putLong(key + DATA_ELAPSED_REALTIME_NANOS, location.elapsedRealtimeNanos)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            data.putDouble(
                key + DATA_ELAPSED_REALTIME_UNCERTAINTY_NANOS,
                location.elapsedRealtimeUncertaintyNanos
            )
        }
        data.putDouble(key + DATA_LATITUDE, location.latitude)
        data.putDouble(key + DATA_LONGITUDE, location.longitude)
        data.putDouble(key + DATA_ALTITUDE, location.altitude)
        data.putFloat(key + DATA_SPEED, location.speed)
        data.putFloat(key + DATA_BEARING, location.bearing)
        data.putFloat(key + DATA_ACCURACY, location.accuracy)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            data.putFloat(key + DATA_VERTICAL_ACCURACY_METERS, location.verticalAccuracyMeters)
            data.putFloat(
                key + DATA_SPEED_ACCURACY_METERSPERSECOND,
                location.speedAccuracyMetersPerSecond
            )
            data.putFloat(key + DATA_BEARING_ACCURACY_DEGREES, location.bearingAccuracyDegrees)
        }
    }

    fun readFromData(data: androidx.work.Data, dataKey: String, keysToRemove: MutableCollection<String>): Location? {
        if (!dataKey.endsWith(DATA_KEY_SUFFIX)) return null
        val value = data.getString(dataKey)
        val key = getKey(dataKey)
        if (key.isNullOrEmpty() || key != value) return null
        keysToRemove.add(dataKey)

        val provider = data.getString(key + DATA_PROVIDER)
        val location = Location(provider)
        location.time = data.getLong(key + DATA_TIME, 0L)
        location.elapsedRealtimeNanos = data.getLong(key + DATA_ELAPSED_REALTIME_NANOS, 0L)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            location.elapsedRealtimeUncertaintyNanos = data.getDouble(
                key + DATA_ELAPSED_REALTIME_UNCERTAINTY_NANOS,
                Double.NaN
            )
        }
        location.latitude = data.getDouble(key + DATA_LATITUDE, Double.NaN)
        location.longitude =
            data.getDouble(key + DATA_LONGITUDE, Double.NaN)
        location.altitude =
            data.getDouble(key + DATA_ALTITUDE, Double.NaN)
        location.speed = data.getFloat(key + DATA_SPEED, Float.NaN)
        location.bearing = data.getFloat(key + DATA_BEARING, Float.NaN)
        location.accuracy = data.getFloat(key + DATA_ACCURACY, Float.NaN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            location.verticalAccuracyMeters =
                data.getFloat(key + DATA_VERTICAL_ACCURACY_METERS, Float.NaN)
            location.speedAccuracyMetersPerSecond = data.getFloat(
                key + DATA_SPEED_ACCURACY_METERSPERSECOND,
                Float.NaN
            )
            location.bearingAccuracyDegrees =
                data.getFloat(key + DATA_BEARING_ACCURACY_DEGREES, Float.NaN)
        }
        keysToRemove.add(key + DATA_ACCURACY)
        keysToRemove.add(key + DATA_ALTITUDE)
        keysToRemove.add(key + DATA_BEARING)
        keysToRemove.add(key + DATA_BEARING_ACCURACY_DEGREES)
        keysToRemove.add(key + DATA_ELAPSED_REALTIME_NANOS)
        keysToRemove.add(key + DATA_ELAPSED_REALTIME_UNCERTAINTY_NANOS)
        keysToRemove.add(key + DATA_LATITUDE)
        keysToRemove.add(key + DATA_LONGITUDE)
        keysToRemove.add(key + DATA_PROVIDER)
        keysToRemove.add(key + DATA_SPEED)
        keysToRemove.add(key + DATA_SPEED_ACCURACY_METERSPERSECOND)
        keysToRemove.add(key + DATA_TIME)
        keysToRemove.add(key + DATA_VERTICAL_ACCURACY_METERS)
        return location
    }

    fun getKey(key: String?): String? {
        if (key.isNullOrEmpty()) return null
        val index = key.indexOf(DATA_KEY_SUFFIX)
        return if (index > 0) key.substring(0, index) else null
    }
}