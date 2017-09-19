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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import static android.text.format.DateUtils.YEAR_IN_MILLIS;
import static java.lang.System.currentTimeMillis;
import static net.sf.times.location.AddressColumns.TIMESTAMP;

/**
 * A helper class to manage database creation and version management for
 * addresses and elevations.
 *
 * @author Moshe Waisberg
 */
public class AddressOpenHelper extends SQLiteOpenHelper {

    /** Database name for times. */
    private static final String DB_NAME = "times";
    /** Database version. */
    private static final int DB_VERSION = 4;
    /** Database table for addresses. */
    public static final String TABLE_ADDRESSES = "addresses";
    /** Database table for elevations. */
    public static final String TABLE_ELEVATIONS = "elevations";
    /** Database table for cities. */
    public static final String TABLE_CITIES = "cities";

    /**
     * Constructs a new helper.
     *
     * @param context
     *         the context.
     */
    public AddressOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(TABLE_ADDRESSES).append('(');
        sql.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
        sql.append(AddressColumns.LOCATION_LATITUDE).append(" DOUBLE NOT NULL,");
        sql.append(AddressColumns.LOCATION_LONGITUDE).append(" DOUBLE NOT NULL,");
        sql.append(AddressColumns.LATITUDE).append(" DOUBLE NOT NULL,");
        sql.append(AddressColumns.LONGITUDE).append(" DOUBLE NOT NULL,");
        sql.append(AddressColumns.ADDRESS).append(" TEXT NOT NULL,");
        sql.append(AddressColumns.LANGUAGE).append(" TEXT,");
        sql.append(TIMESTAMP).append(" INTEGER NOT NULL,");
        sql.append(AddressColumns.FAVORITE).append(" INTEGER NOT NULL");
        sql.append(");");
        db.execSQL(sql.toString());

        sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(TABLE_ELEVATIONS).append('(');
        sql.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
        sql.append(ElevationColumns.LATITUDE).append(" DOUBLE NOT NULL,");
        sql.append(ElevationColumns.LONGITUDE).append(" DOUBLE NOT NULL,");
        sql.append(ElevationColumns.ELEVATION).append(" DOUBLE NOT NULL,");
        sql.append(ElevationColumns.TIMESTAMP).append(" INTEGER NOT NULL");
        sql.append(");");
        db.execSQL(sql.toString());

        sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(TABLE_CITIES).append('(');
        sql.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY,");
        sql.append(CitiesColumns.TIMESTAMP).append(" INTEGER NOT NULL,");
        sql.append(CitiesColumns.FAVORITE).append(" INTEGER NOT NULL");
        sql.append(");");
        db.execSQL(sql.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + TABLE_ADDRESSES + ";");
        if (oldVersion >= 3) {
            db.execSQL("DROP TABLE " + TABLE_ELEVATIONS + ";");
            if (oldVersion >= 4) {
                db.execSQL("DROP TABLE " + TABLE_CITIES + ";");
            }
        }
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        // Delete stale records older than 1 year.
        String whereClause = "(" + TIMESTAMP + " < " + (currentTimeMillis() - YEAR_IN_MILLIS) + ")";
        db.delete(TABLE_ADDRESSES, whereClause, null);
    }
}
