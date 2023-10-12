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
package com.github.times.location.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.times.location.provider.LocationContract.AddressColumns;
import com.github.times.location.provider.LocationContract.CityColumns;
import com.github.times.location.provider.LocationContract.ElevationColumns;

import java.io.File;

import static android.text.format.DateUtils.YEAR_IN_MILLIS;
import static java.lang.System.currentTimeMillis;

/**
 * A helper class to manage database creation and version management for
 * addresses and elevations.
 *
 * @author Moshe Waisberg
 */
public class LocationOpenHelper extends SQLiteOpenHelper {

    /**
     * Database name for locations.
     */
    private static final String DB_NAME = "location";
    /**
     * Database name for times.
     */
    private static final String DB_NAME_TIMES = "times";
    /**
     * Database version.
     */
    private static final int DB_VERSION = 1;
    /**
     * Database table for addresses.
     */
    public static final String TABLE_ADDRESSES = "addresses";
    /**
     * Database table for elevations.
     */
    public static final String TABLE_ELEVATIONS = "elevations";
    /**
     * Database table for cities.
     */
    public static final String TABLE_CITIES = "cities";

    /**
     * Constructs a new helper.
     *
     * @param context the context.
     */
    public LocationOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        File dbFile = new File(db.getPath());
        String folder = dbFile.getParent();
        File oldFile = new File(folder, DB_NAME_TIMES);
        if (oldFile.exists()) {
            SQLiteDatabase.deleteDatabase(oldFile);
        }

        String sql = new StringBuilder()
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
                .toString();
        db.execSQL(sql);

        sql = new StringBuilder()
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
                .toString();
        db.execSQL(sql);

        sql = new StringBuilder()
                .append("CREATE TABLE ").append(TABLE_CITIES).append('(')
                .append(CityColumns._ID).append(" INTEGER PRIMARY KEY,")
                .append(CityColumns.TIMESTAMP).append(" INTEGER NOT NULL,")
                .append(CityColumns.FAVORITE).append(" INTEGER NOT NULL")
                .append(");")
                .toString();
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADDRESSES + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CITIES + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ELEVATIONS + ";");

        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        // Delete stale records older than 1 year.
        final String olderThanYear = String.valueOf(currentTimeMillis() - YEAR_IN_MILLIS);
        db.delete(TABLE_ADDRESSES, AddressColumns.TIMESTAMP + " < " + olderThanYear, null);
        db.delete(TABLE_ELEVATIONS, ElevationColumns.TIMESTAMP + " < " + olderThanYear, null);
    }
}
