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
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Location content provider.<br>
 * Fetches addresses, cities, and elevations from the database.
 *
 * @author Moshe Waisberg
 */
public class LocationContentProvider extends ContentProvider {

    private static final int CODE_ADDRESSES = 100;
    private static final int CODE_ADDRESS_ID = 101;
    private static final int CODE_CITIES = 200;
    private static final int CODE_CITY_ID = 201;
    private static final int CODE_ELEVATIONS = 300;
    private static final int CODE_ELEVATION_ID = 301;

    private final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private LocationOpenHelper openHelper;

    @Override
    public boolean onCreate() {
        final Context context = getContext();

        final String authority = LocationContract.AUTHORITY;
        uriMatcher.addURI(authority, LocationContract.Address.ADDRESS, CODE_ADDRESSES);
        uriMatcher.addURI(authority, LocationContract.Address.ADDRESS + "/#", CODE_ADDRESS_ID);
        uriMatcher.addURI(authority, LocationContract.City.CITY, CODE_CITIES);
        uriMatcher.addURI(authority, LocationContract.City.CITY + "/#", CODE_CITY_ID);
        uriMatcher.addURI(authority, LocationContract.Elevation.ELEVATION, CODE_ELEVATIONS);
        uriMatcher.addURI(authority, LocationContract.Elevation.ELEVATION + "/#", CODE_ELEVATION_ID);

        openHelper = new LocationOpenHelper(context);

        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case CODE_ADDRESSES:
                return LocationContract.Address.CONTENT_TYPE;
            case CODE_ADDRESS_ID:
                return LocationContract.Address.CONTENT_ITEM_TYPE;
            case CODE_CITIES:
                return LocationContract.City.CONTENT_TYPE;
            case CODE_CITY_ID:
                return LocationContract.City.CONTENT_ITEM_TYPE;
            case CODE_ELEVATIONS:
                return LocationContract.Elevation.CONTENT_TYPE;
            case CODE_ELEVATION_ID:
                return LocationContract.Elevation.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor;
        String id;

        switch (uriMatcher.match(uri)) {
            case CODE_ADDRESSES:
                cursor = db.query(LocationOpenHelper.TABLE_ADDRESSES, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_ADDRESS_ID:
                id = uri.getLastPathSegment();
                selection = DatabaseUtils.concatenateWhere(selection, BaseColumns._ID + "=" + id);
                cursor = db.query(LocationOpenHelper.TABLE_ADDRESSES, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_CITIES:
                cursor = db.query(LocationOpenHelper.TABLE_CITIES, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_CITY_ID:
                id = uri.getLastPathSegment();
                selection = DatabaseUtils.concatenateWhere(selection, BaseColumns._ID + "=" + id);
                cursor = db.query(LocationOpenHelper.TABLE_CITIES, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_ELEVATIONS:
                cursor = db.query(LocationOpenHelper.TABLE_ELEVATIONS, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_ELEVATION_ID:
                id = uri.getLastPathSegment();
                selection = DatabaseUtils.concatenateWhere(selection, BaseColumns._ID + "=" + id);
                cursor = db.query(LocationOpenHelper.TABLE_ELEVATIONS, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri result = null;

        if (result != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return result;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int result = 0;

        if (result > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return result;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int result = 0;

        if (result > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return result;
    }

    @Override
    public void shutdown() {
        openHelper.close();
        super.shutdown();
    }
}
