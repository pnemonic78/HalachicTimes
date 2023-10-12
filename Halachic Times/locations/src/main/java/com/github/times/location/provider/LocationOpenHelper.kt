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

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.format.DateUtils
import com.github.times.location.provider.LocationContract.AddressColumns
import com.github.times.location.provider.LocationContract.CityColumns
import com.github.times.location.provider.LocationContract.ElevationColumns
import java.io.File

/**
 * A helper class to manage database creation and version management for
 * addresses and elevations.
 *
 * @author Moshe Waisberg
 */
class LocationOpenHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val dbFile = File(db.path)
        val folder = dbFile.parent
        val oldFile = File(folder, DB_NAME_TIMES)
        if (oldFile.exists()) {
            SQLiteDatabase.deleteDatabase(oldFile)
        }
        var sql: String = StringBuilder()
            .append("CREATE TABLE ").append(TABLE_ADDRESSES).append('(')
            .append(AddressColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
            .append(AddressColumns.LOCATION_LATITUDE).append(" DOUBLE NOT NULL,")
            .append(AddressColumns.LOCATION_LONGITUDE).append(" DOUBLE NOT NULL,")
            .append(AddressColumns.LATITUDE).append(" DOUBLE NOT NULL,")
            .append(AddressColumns.LONGITUDE).append(" DOUBLE NOT NULL,")
            .append(AddressColumns.ADDRESS).append(" TEXT NOT NULL,")
            .append(AddressColumns.LANGUAGE).append(" TEXT,")
            .append(AddressColumns.TIMESTAMP).append(" INTEGER NOT NULL,")
            .append(AddressColumns.FAVORITE).append(" INTEGER NOT NULL")
            .append(");")
            .toString()
        db.execSQL(sql)
        sql = StringBuilder()
            .append("CREATE TABLE ").append(TABLE_ELEVATIONS).append('(')
            .append(ElevationColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
            .append(ElevationColumns.LATITUDE).append(" DOUBLE NOT NULL,")
            .append(ElevationColumns.LONGITUDE).append(" DOUBLE NOT NULL,")
            .append(ElevationColumns.ELEVATION).append(" DOUBLE NOT NULL,")
            .append(ElevationColumns.TIMESTAMP).append(" INTEGER NOT NULL,")
            .append("UNIQUE (")
            .append(ElevationColumns.LATITUDE)
            .append(", ")
            .append(ElevationColumns.LONGITUDE)
            .append(") ON CONFLICT REPLACE")
            .append(");")
            .toString()
        db.execSQL(sql)
        sql = StringBuilder()
            .append("CREATE TABLE ").append(TABLE_CITIES).append('(')
            .append(CityColumns._ID).append(" INTEGER PRIMARY KEY,")
            .append(CityColumns.TIMESTAMP).append(" INTEGER NOT NULL,")
            .append(CityColumns.FAVORITE).append(" INTEGER NOT NULL")
            .append(");")
            .toString()
        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ADDRESSES;")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CITIES;")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ELEVATIONS;")
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)

        // Delete stale records older than 1 year.
        val olderThanYear = (System.currentTimeMillis() - DateUtils.YEAR_IN_MILLIS).toString()
        db.delete(TABLE_ADDRESSES, "${AddressColumns.TIMESTAMP} < $olderThanYear", null)
        db.delete(TABLE_ELEVATIONS, "${ElevationColumns.TIMESTAMP} < $olderThanYear", null)
    }

    companion object {
        /**
         * Database name for locations.
         */
        private const val DB_NAME = "location"

        /**
         * Database name for times.
         */
        private const val DB_NAME_TIMES = "times"

        /**
         * Database version.
         */
        private const val DB_VERSION = 1

        /**
         * Database table for addresses.
         */
        const val TABLE_ADDRESSES = "addresses"

        /**
         * Database table for elevations.
         */
        const val TABLE_ELEVATIONS = "elevations"

        /**
         * Database table for cities.
         */
        const val TABLE_CITIES = "cities"
    }
}