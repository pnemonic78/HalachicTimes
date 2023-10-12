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
package com.github.times.location.provider;

import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;

import static android.content.ContentResolver.CURSOR_DIR_BASE_TYPE;
import static android.content.ContentResolver.CURSOR_ITEM_BASE_TYPE;

/**
 * Location provider contract.
 *
 * @author Moshe Waisberg
 */
public class LocationContract {

    /** The authority for the addresses provider */
    public static String AUTHORITY(Context context) {
        return context.getPackageName() + ".locations";
    }

    /** A content:// style uri to the authority for the addresses provider */
    public static Uri AUTHORITY_URI(Context context) {
        return Uri.parse("content://" + AUTHORITY(context));
    }

    /**
     * Address table columns.
     *
     * @author Moshe Waisberg
     */
    public interface AddressColumns extends BaseColumns {

        /**
         * The location's latitude column.
         * <p>Type: DOUBLE</p>
         */
        String LOCATION_LATITUDE = "loc_latitude";
        /**
         * The location's longitude column.
         * <p>Type: DOUBLE</p>
         */
        String LOCATION_LONGITUDE = "loc_longitude";
        /**
         * The latitude column.
         * <p>Type: DOUBLE</p>
         */
        String LATITUDE = "latitude";
        /**
         * The longitude column.
         * <p>Type: DOUBLE</p>
         */
        String LONGITUDE = "longitude";
        /**
         * The formatted name column.
         * <p>Type: TEXT</p>
         */
        String ADDRESS = "address";
        /**
         * The language column.
         * <p>Type: TEXT</p>
         */
        String LANGUAGE = "language";
        /**
         * The timestamp column.
         * <p>Type: LONG</p>
         */
        String TIMESTAMP = "timestamp";
        /**
         * Is favourite address?
         * <P>Type: INTEGER (boolean)</P>
         */
        String FAVORITE = "favorite";

    }

    /**
     * Contains the addresses.
     */
    public static final class Addresses implements AddressColumns {
        /** Table name for addresses. */
        public static final String ADDRESS = "address";

        /**
         * The content:// style URI for this table.  Requests to this URI can be
         * performed on the UI thread because they are always unblocking.
         */
        public static final Uri CONTENT_URI(Context context) {
            return Uri.withAppendedPath(AUTHORITY_URI(context), ADDRESS);
        }

        /**
         * The MIME-type of {@link #CONTENT_URI} providing a directory of contact directories.
         */
        public static final String CONTENT_TYPE = CURSOR_DIR_BASE_TYPE + "/com.github.times.location.address";

        /**
         * The MIME-type of a {@link #CONTENT_URI} item.
         */
        public static final String CONTENT_ITEM_TYPE = CURSOR_ITEM_BASE_TYPE + "/com.github.times.location.address";
    }

    /**
     * Elevation table columns.
     *
     * @author Moshe Waisberg
     */
    public interface ElevationColumns extends BaseColumns {

        /**
         * The latitude.
         * <p>Type: DOUBLE</p>
         */
        String LATITUDE = "latitude";
        /**
         * The longitude.
         * <p>Type: DOUBLE</p>
         */
        String LONGITUDE = "longitude";
        /**
         * The elevation / altitude.
         * <p>Type: DOUBLE</p>
         */
        String ELEVATION = "elevation";
        /**
         * The timestamp.
         * <p>Type: LONG</p>
         */
        String TIMESTAMP = "timestamp";

    }

    /**
     * Contains the elevations.
     */
    public static final class Elevations implements ElevationColumns {
        /** Table name for elevations. */
        public static final String ELEVATION = "elevation";

        /**
         * The content:// style URI for this table.  Requests to this URI can be
         * performed on the UI thread because they are always unblocking.
         */
        public static final Uri CONTENT_URI(Context context) {
            return Uri.withAppendedPath(AUTHORITY_URI(context), ELEVATION);
        }

        /**
         * The MIME-type of {@link #CONTENT_URI} providing a directory of contact directories.
         */
        public static final String CONTENT_TYPE = CURSOR_DIR_BASE_TYPE + "/com.github.times.location.elevation";

        /**
         * The MIME-type of a {@link #CONTENT_URI} item.
         */
        public static final String CONTENT_ITEM_TYPE = CURSOR_ITEM_BASE_TYPE + "/com.github.times.location.elevation";
    }

    /**
     * City table columns.
     *
     * @author Moshe Waisberg
     */
    public interface CityColumns extends BaseColumns {

        /**
         * The timestamp.
         * <p>Type: LONG</p>
         */
        String TIMESTAMP = "timestamp";
        /**
         * Is favourite city?
         * <P>Type: INTEGER (boolean)</P>
         */
        String FAVORITE = "favorite";

    }

    /**
     * Contains the cities.
     */
    public static final class Cities implements CityColumns {
        /** Table name for cities. */
        public static final String CITY = "city";

        /**
         * The content:// style URI for this table.  Requests to this URI can be
         * performed on the UI thread because they are always unblocking.
         */
        public static final Uri CONTENT_URI(Context context) {
            return Uri.withAppendedPath(AUTHORITY_URI(context), CITY);
        }

        /**
         * The MIME-type of {@link #CONTENT_URI} providing a directory of contact directories.
         */
        public static final String CONTENT_TYPE = CURSOR_DIR_BASE_TYPE + "/com.github.times.location.city";

        /**
         * The MIME-type of a {@link #CONTENT_URI} item.
         */
        public static final String CONTENT_ITEM_TYPE = CURSOR_ITEM_BASE_TYPE + "/com.github.times.location.city";
    }

}
