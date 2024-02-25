package com.github.location

class Location(val provider: String) {
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var altitude: Double = 0.0
    var accuracy: Float = 1f
    var time: Long = System.currentTimeMillis()

    companion object {
        /**
         * Minimum valid latitude.
         */
        const val LATITUDE_MIN = -90.0

        /**
         * Maximum valid latitude.
         */
        const val LATITUDE_MAX = 90.0

        /**
         * Minimum valid longitude.
         */
        const val LONGITUDE_MIN = -180.0

        /**
         * Maximum valid longitude.
         */
        const val LONGITUDE_MAX = 180.0

        /**
         * Lowest possible natural elevation on the surface of the earth.
         */
        const val ELEVATION_MIN = -500.0

        /**
         * Highest possible natural elevation from the surface of the earth.
         */
        const val ELEVATION_MAX = 100_000.0
    }
}