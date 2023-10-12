/*
 * Copyright 2016, Moshe Waisberg
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
package com.github.times.location.provider

import android.content.ContentResolver.CURSOR_DIR_BASE_TYPE
import android.content.ContentResolver.CURSOR_ITEM_BASE_TYPE
import android.content.Context
import android.net.Uri
import android.provider.BaseColumns

/**
 * Location provider contract.
 *
 * @author Moshe Waisberg
 */
object LocationContract {
    /** The authority for the addresses provider  */
    @JvmStatic
    fun AUTHORITY(context: Context): String {
        return context.packageName + ".locations"
    }

    /** A content:// style uri to the authority for the addresses provider  */
    @JvmStatic
    fun AUTHORITY_URI(context: Context): Uri {
        return Uri.parse("content://" + AUTHORITY(context))
    }

    /**
     * Address table columns.
     *
     * @author Moshe Waisberg
     */
    interface AddressColumns : BaseColumns {
        companion object {
            const val _ID = BaseColumns._ID

            /**
             * The location's latitude column.
             *
             * Type: DOUBLE
             */
            const val LOCATION_LATITUDE = "loc_latitude"

            /**
             * The location's longitude column.
             *
             * Type: DOUBLE
             */
            const val LOCATION_LONGITUDE = "loc_longitude"

            /**
             * The latitude column.
             *
             * Type: DOUBLE
             */
            const val LATITUDE = "latitude"

            /**
             * The longitude column.
             *
             * Type: DOUBLE
             */
            const val LONGITUDE = "longitude"

            /**
             * The formatted name column.
             *
             * Type: TEXT
             */
            const val ADDRESS = "address"

            /**
             * The language column.
             *
             * Type: TEXT
             */
            const val LANGUAGE = "language"

            /**
             * The timestamp column.
             *
             * Type: LONG
             */
            const val TIMESTAMP = "timestamp"

            /**
             * Is favourite address?
             * <P>Type: INTEGER (boolean)</P>
             */
            const val FAVORITE = "favorite"
        }
    }

    /**
     * Contains the addresses.
     */
    object Addresses : AddressColumns {
        /** Table name for addresses.  */
        const val ADDRESS = "address"

        /**
         * The content:// style URI for this table.  Requests to this URI can be
         * performed on the UI thread because they are always unblocking.
         */
        @JvmStatic
        fun CONTENT_URI(context: Context): Uri {
            return Uri.withAppendedPath(AUTHORITY_URI(context), ADDRESS)
        }

        /**
         * The MIME-type of [.CONTENT_URI] providing a directory of contact directories.
         */
        const val CONTENT_TYPE = "$CURSOR_DIR_BASE_TYPE/com.github.times.location.address"

        /**
         * The MIME-type of a [.CONTENT_URI] item.
         */
        const val CONTENT_ITEM_TYPE = "$CURSOR_ITEM_BASE_TYPE/com.github.times.location.address"
    }

    /**
     * Elevation table columns.
     *
     * @author Moshe Waisberg
     */
    interface ElevationColumns : BaseColumns {
        companion object {
            const val _ID = BaseColumns._ID

            /**
             * The latitude.
             *
             * Type: DOUBLE
             */
            const val LATITUDE = "latitude"

            /**
             * The longitude.
             *
             * Type: DOUBLE
             */
            const val LONGITUDE = "longitude"

            /**
             * The elevation / altitude.
             *
             * Type: DOUBLE
             */
            const val ELEVATION = "elevation"

            /**
             * The timestamp.
             *
             * Type: LONG
             */
            const val TIMESTAMP = "timestamp"
        }
    }

    /**
     * Contains the elevations.
     */
    object Elevations : ElevationColumns {
        /** Table name for elevations.  */
        const val ELEVATION = "elevation"

        /**
         * The content:// style URI for this table.  Requests to this URI can be
         * performed on the UI thread because they are always unblocking.
         */
        @JvmStatic
        fun CONTENT_URI(context: Context): Uri {
            return Uri.withAppendedPath(AUTHORITY_URI(context), ELEVATION)
        }

        /**
         * The MIME-type of [.CONTENT_URI] providing a directory of contact directories.
         */
        const val CONTENT_TYPE = "$CURSOR_DIR_BASE_TYPE/com.github.times.location.elevation"

        /**
         * The MIME-type of a [.CONTENT_URI] item.
         */
        const val CONTENT_ITEM_TYPE = "$CURSOR_ITEM_BASE_TYPE/com.github.times.location.elevation"
    }

    /**
     * City table columns.
     *
     * @author Moshe Waisberg
     */
    interface CityColumns : BaseColumns {
        companion object {
            const val _ID = BaseColumns._ID

            /**
             * The timestamp.
             *
             * Type: LONG
             */
            const val TIMESTAMP = "timestamp"

            /**
             * Is favourite city?
             * <P>Type: INTEGER (boolean)</P>
             */
            const val FAVORITE = "favorite"
        }
    }

    /**
     * Contains the cities.
     */
    object Cities : CityColumns {
        /** Table name for cities.  */
        const val CITY = "city"

        /**
         * The content:// style URI for this table.  Requests to this URI can be
         * performed on the UI thread because they are always unblocking.
         */
        @JvmStatic
        fun CONTENT_URI(context: Context): Uri {
            return Uri.withAppendedPath(AUTHORITY_URI(context), CITY)
        }

        /**
         * The MIME-type of [.CONTENT_URI] providing a directory of contact directories.
         */
        const val CONTENT_TYPE = "$CURSOR_DIR_BASE_TYPE/com.github.times.location.city"

        /**
         * The MIME-type of a [.CONTENT_URI] item.
         */
        const val CONTENT_ITEM_TYPE = "$CURSOR_ITEM_BASE_TYPE/com.github.times.location.city"
    }
}