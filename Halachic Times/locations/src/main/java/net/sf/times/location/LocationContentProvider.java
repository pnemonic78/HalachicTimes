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
package net.sf.times.location;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Location content provider.<br>
 * Fetches addresses, cities, and elevations from the database.
 *
 * @author Moshe Waisberg
 */
public class LocationContentProvider extends ContentProvider {

    private static final int ADDRESSES = 1;
    private static final int ADDRESS = 2;
    private static final int CITIES = 3;
    private static final int CITY = 4;
    private static final int ELEVATIONS = 5;
    private static final int ELEVATION = 6;

    private final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    @Override
    public boolean onCreate() {
        final String authority = getContext().getPackageName();
        uriMatcher.addURI(authority, "addresses", ADDRESSES);
        uriMatcher.addURI(authority, "address/*", ADDRESS);
        uriMatcher.addURI(authority, "cities", CITIES);
        uriMatcher.addURI(authority, "city/*", CITY);
        uriMatcher.addURI(authority, "elevations", ELEVATIONS);
        uriMatcher.addURI(authority, "elevation/*", ELEVATION);

        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ADDRESSES:
                return LocationContract.Address.CONTENT_TYPE;
            case ADDRESS:
                return LocationContract.Address.CONTENT_ITEM_TYPE;
            case CITIES:
                return LocationContract.City.CONTENT_TYPE;
            case CITY:
                return LocationContract.City.CONTENT_ITEM_TYPE;
            case ELEVATIONS:
                return LocationContract.Elevation.CONTENT_TYPE;
            case ELEVATION:
                return LocationContract.Elevation.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
