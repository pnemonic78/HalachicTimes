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
package com.github.times.location.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.DatabaseUtils
import android.net.Uri
import android.provider.BaseColumns
import com.github.times.location.provider.LocationContract.Elevations

/**
 * Location content provider.<br></br>
 * Fetches addresses, cities, and elevations from the database.
 *
 * @author Moshe Waisberg
 */
class LocationContentProvider : ContentProvider() {
    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    private var openHelper: LocationOpenHelper? = null

    override fun onCreate(): Boolean {
        val context = context
        val authority = LocationContract.AUTHORITY(context!!)
        uriMatcher.addURI(authority, LocationContract.Addresses.ADDRESS, CODE_ADDRESSES)
        uriMatcher.addURI(authority, LocationContract.Addresses.ADDRESS + "/#", CODE_ADDRESS_ID)
        uriMatcher.addURI(authority, LocationContract.Cities.CITY, CODE_CITIES)
        uriMatcher.addURI(authority, LocationContract.Cities.CITY + "/#", CODE_CITY_ID)
        uriMatcher.addURI(authority, Elevations.ELEVATION, CODE_ELEVATIONS)
        uriMatcher.addURI(authority, Elevations.ELEVATION + "/#", CODE_ELEVATION_ID)
        openHelper = LocationOpenHelper(context)
        return true
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            CODE_ADDRESSES -> LocationContract.Addresses.CONTENT_TYPE
            CODE_ADDRESS_ID -> LocationContract.Addresses.CONTENT_ITEM_TYPE
            CODE_CITIES -> LocationContract.Cities.CONTENT_TYPE
            CODE_CITY_ID -> LocationContract.Cities.CONTENT_ITEM_TYPE
            CODE_ELEVATIONS -> Elevations.CONTENT_TYPE
            CODE_ELEVATION_ID -> Elevations.CONTENT_ITEM_TYPE
            else -> null
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        var selectionCriteria = selection
        val context = context ?: return null
        val db = openHelper?.readableDatabase ?: return null
        val cursor: Cursor
        val id: String?
        when (uriMatcher.match(uri)) {
            CODE_ADDRESSES -> cursor = db.query(
                LocationOpenHelper.TABLE_ADDRESSES,
                projection,
                selectionCriteria,
                selectionArgs,
                null,
                null,
                sortOrder
            )

            CODE_ADDRESS_ID -> {
                id = uri.lastPathSegment
                selectionCriteria = DatabaseUtils.concatenateWhere(selectionCriteria, BaseColumns._ID + "=" + id)
                cursor = db.query(
                    LocationOpenHelper.TABLE_ADDRESSES,
                    projection,
                    selectionCriteria,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
                )
            }

            CODE_CITIES -> cursor = db.query(
                LocationOpenHelper.TABLE_CITIES,
                projection,
                selectionCriteria,
                selectionArgs,
                null,
                null,
                sortOrder
            )

            CODE_CITY_ID -> {
                id = uri.lastPathSegment
                selectionCriteria = DatabaseUtils.concatenateWhere(selectionCriteria, BaseColumns._ID + "=" + id)
                cursor = db.query(
                    LocationOpenHelper.TABLE_CITIES,
                    projection,
                    selectionCriteria,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
                )
            }

            CODE_ELEVATIONS -> cursor = db.query(
                LocationOpenHelper.TABLE_ELEVATIONS,
                projection,
                selectionCriteria,
                selectionArgs,
                null,
                null,
                sortOrder
            )

            CODE_ELEVATION_ID -> {
                id = uri.lastPathSegment
                selectionCriteria = DatabaseUtils.concatenateWhere(selectionCriteria, BaseColumns._ID + "=" + id)
                cursor = db.query(
                    LocationOpenHelper.TABLE_ELEVATIONS,
                    projection,
                    selectionCriteria,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
                )
            }

            else -> throw UnsupportedOperationException("Unknown uri: $uri")
        }
        cursor.setNotificationUri(context.contentResolver, uri)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val context = context ?: return null
        val db = openHelper?.writableDatabase ?: return null
        val result: Uri?
        val id: Long

        when (uriMatcher.match(uri)) {
            CODE_ADDRESSES,
            CODE_ADDRESS_ID -> {
                id = db.insert(LocationOpenHelper.TABLE_ADDRESSES, null, values)
                result =
                    ContentUris.withAppendedId(LocationContract.Addresses.CONTENT_URI(context), id)
            }

            CODE_CITIES,
            CODE_CITY_ID -> {
                id = db.insert(LocationOpenHelper.TABLE_CITIES, null, values)
                result =
                    ContentUris.withAppendedId(LocationContract.Cities.CONTENT_URI(context), id)
            }

            CODE_ELEVATIONS,
            CODE_ELEVATION_ID -> {
                id = db.insert(LocationOpenHelper.TABLE_ELEVATIONS, null, values)
                result = ContentUris.withAppendedId(Elevations.CONTENT_URI(context), id)
            }

            else -> throw UnsupportedOperationException("Unknown uri: $uri")
        }
        context.contentResolver.notifyChange(uri, null)
        return result
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        var selectionCriteria = selection
        val context = context ?: return 0
        val db = openHelper?.writableDatabase ?: return 0
        val result: Int
        val id: String?

        when (uriMatcher.match(uri)) {
            CODE_ADDRESSES -> result =
                db.delete(LocationOpenHelper.TABLE_ADDRESSES, selectionCriteria, selectionArgs)

            CODE_ADDRESS_ID -> {
                id = uri.lastPathSegment
                selectionCriteria = DatabaseUtils.concatenateWhere(selectionCriteria, BaseColumns._ID + "=" + id)
                result = db.delete(LocationOpenHelper.TABLE_ADDRESSES, selectionCriteria, selectionArgs)
            }

            CODE_CITIES -> result =
                db.delete(LocationOpenHelper.TABLE_CITIES, selectionCriteria, selectionArgs)

            CODE_CITY_ID -> {
                id = uri.lastPathSegment
                selectionCriteria = DatabaseUtils.concatenateWhere(selectionCriteria, BaseColumns._ID + "=" + id)
                result = db.delete(LocationOpenHelper.TABLE_CITIES, selectionCriteria, selectionArgs)
            }

            CODE_ELEVATIONS -> result =
                db.delete(LocationOpenHelper.TABLE_ELEVATIONS, selectionCriteria, selectionArgs)

            CODE_ELEVATION_ID -> {
                id = uri.lastPathSegment
                selectionCriteria = DatabaseUtils.concatenateWhere(selectionCriteria, BaseColumns._ID + "=" + id)
                result = db.delete(LocationOpenHelper.TABLE_ELEVATIONS, selectionCriteria, selectionArgs)
            }

            else -> throw UnsupportedOperationException("Unknown uri: $uri")
        }
        if (result > 0) {
            context.contentResolver.notifyChange(uri, null)
        }
        return result
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        var selectionCriteria = selection
        val context = context ?: return 0
        val db = openHelper?.writableDatabase ?: return 0
        val result: Int
        val id: String?

        when (uriMatcher.match(uri)) {
            CODE_ADDRESSES -> result =
                db.update(LocationOpenHelper.TABLE_ADDRESSES, values, selectionCriteria, selectionArgs)

            CODE_ADDRESS_ID -> {
                id = uri.lastPathSegment
                selectionCriteria = DatabaseUtils.concatenateWhere(selectionCriteria, BaseColumns._ID + "=" + id)
                result =
                    db.update(LocationOpenHelper.TABLE_ADDRESSES, values, selectionCriteria, selectionArgs)
            }

            CODE_CITIES -> result =
                db.update(LocationOpenHelper.TABLE_CITIES, values, selectionCriteria, selectionArgs)

            CODE_CITY_ID -> {
                id = uri.lastPathSegment
                selectionCriteria = DatabaseUtils.concatenateWhere(selectionCriteria, BaseColumns._ID + "=" + id)
                result =
                    db.update(LocationOpenHelper.TABLE_CITIES, values, selectionCriteria, selectionArgs)
            }

            CODE_ELEVATIONS -> result =
                db.update(LocationOpenHelper.TABLE_ELEVATIONS, values, selectionCriteria, selectionArgs)

            CODE_ELEVATION_ID -> {
                id = uri.lastPathSegment
                selectionCriteria = DatabaseUtils.concatenateWhere(selectionCriteria, BaseColumns._ID + "=" + id)
                result =
                    db.update(LocationOpenHelper.TABLE_ELEVATIONS, values, selectionCriteria, selectionArgs)
            }

            else -> throw UnsupportedOperationException("Unknown uri: $uri")
        }
        if (result > 0) {
            context.contentResolver.notifyChange(uri, null)
        }
        return result
    }

    override fun shutdown() {
        openHelper!!.close()
        super.shutdown()
    }

    companion object {
        private const val CODE_ADDRESSES = 100
        private const val CODE_ADDRESS_ID = 101
        private const val CODE_CITIES = 200
        private const val CODE_CITY_ID = 201
        private const val CODE_ELEVATIONS = 300
        private const val CODE_ELEVATION_ID = 301
    }
}