/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 *
 * http://sourceforge.net/projects/halachictimes
 *
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 *
 */
package net.sf.media;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;

import net.sf.lib.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Ringtone manager that can ignore external media when not permitted.
 */
public class RingtoneManager extends android.media.RingtoneManager {

    /** Invalid {@link Uri} path that means 'default'. */
    public static final String DEFAULT_PATH = null;

    /** Empty {@link Uri} that means 'silent'. */
    public static final Uri SILENT_URI = Uri.EMPTY;
    /** Empty {@link Uri} path that means 'silent'. */
    public static final String SILENT_PATH = SILENT_URI.toString();

    private static final String INTERNAL_PATH = MediaStore.Audio.Media.INTERNAL_CONTENT_URI.toString();
    private static final String EXTERNAL_PATH = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString();

    private static final String[] INTERNAL_COLUMNS = new String[]{
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
            "\"" + MediaStore.Audio.Media.INTERNAL_CONTENT_URI + "\"",
            MediaStore.Audio.Media.TITLE_KEY
    };

    private static final String[] SETTINGS_COLUMNS = new String[]{
            Settings.NameValueTable._ID, Settings.NameValueTable.NAME, Settings.NameValueTable.VALUE
    };

    private Context context;
    private Cursor cursor;
    private int type;
    private boolean includeExternal = true;

    /**
     * If a column (item from this list) exists in the Cursor, its value must
     * be true (value of 1) for the row to be returned.
     */
    private final List<String> filterColumns = new ArrayList<String>();

    public RingtoneManager(Context context) {
        super(context);
        this.context = context;
        setIncludeExternal((Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
                || (context.checkCallingOrSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED));
    }

    @Override
    public void setType(int type) {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        super.setType(type);
        this.type = type;
        setFilterColumnsList(type);
    }

    @Override
    public Cursor getCursor() {
        if ((cursor != null) && cursor.isClosed()) {
            cursor = null;
        }
        if (cursor == null) {
            if (includeExternal) {
                try {
                    cursor = super.getCursor();
                } catch (SecurityException e) {
                    includeExternal = false;
                    cursor = getInternalRingtones();
                }
            } else {
                cursor = getInternalRingtones();
            }
        }
        return cursor;
    }

    /**
     * Include external media?
     *
     * @param include
     *         whether to include.
     */
    public void setIncludeExternal(boolean include) {
        this.includeExternal = include;
    }

    /**
     * Is external media included?
     *
     * @return is included?
     */
    public boolean isIncludeExternal() {
        return includeExternal;
    }

    private Cursor getInternalRingtones() {
        return query(
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI, INTERNAL_COLUMNS,
                constructBooleanTrueWhereClause(filterColumns),
                null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
    }

    private void setFilterColumnsList(int type) {
        List<String> columns = filterColumns;
        // Constructor not finished.
        if (columns == null) {
            return;
        }
        columns.clear();

        if ((type & TYPE_RINGTONE) != 0) {
            columns.add(MediaStore.Audio.AudioColumns.IS_RINGTONE);
        }

        if ((type & TYPE_NOTIFICATION) != 0) {
            columns.add(MediaStore.Audio.AudioColumns.IS_NOTIFICATION);
        }

        if ((type & TYPE_ALARM) != 0) {
            columns.add(MediaStore.Audio.AudioColumns.IS_ALARM);
        }
    }

    /**
     * Constructs a where clause that consists of at least one column being 1
     * (true). This is used to find all matching sounds for the given sound
     * types (ringtone, notifications, etc.)
     *
     * @param columns
     *         The columns that must be true.
     * @return The where clause.
     */
    private static String constructBooleanTrueWhereClause(List<String> columns) {

        if (columns == null) return null;

        StringBuilder sb = new StringBuilder();
        sb.append('(');

        for (int i = columns.size() - 1; i >= 0; i--) {
            sb.append(columns.get(i)).append("=1 or ");
        }

        if (columns.size() > 0) {
            // Remove last ' or '
            sb.setLength(sb.length() - 4);
        }

        sb.append(')');

        return sb.toString();
    }

    private Cursor query(Uri uri,
                         String[] projection,
                         String selection,
                         String[] selectionArgs,
                         String sortOrder) {
        return context.getContentResolver().query(uri, projection, selection, selectionArgs,
                sortOrder);
    }

    public String filterInternal(String uriString) {
        if (!TextUtils.isEmpty(uriString)) {
            if (uriString.startsWith(INTERNAL_PATH)) {
                // Is definitely internal.
                return uriString;
            }

            if (uriString.startsWith(EXTERNAL_PATH)) {
                // Try a 'default' tone.
                uriString = getDefaultUri(type).toString();
            }

            ContentResolver resolver = context.getContentResolver();
            Uri uri = Uri.parse(uriString);
            Cursor cursor = resolver.query(uri, SETTINGS_COLUMNS, null, null, null);
            if ((cursor != null) && cursor.moveToFirst()) {
                String uriValue = cursor.getString(URI_COLUMN_INDEX);
                cursor.close();

                if (uriValue == null) {
                    return null;
                }
                if (uriValue.startsWith(INTERNAL_PATH)) {
                    // Is definitely internal.
                    return uriString;
                }
                if (uriValue.startsWith(EXTERNAL_PATH)) {
                    // 'Default' tone is definitely external.
                    return SILENT_PATH;
                }
            }
        }
        return uriString;
    }

    public String filterInternal(Uri uri) {
        return filterInternal(uri != null ? uri.toString() : null);
    }

    /**
     * Get the 'default' tone title.
     *
     * @return the title.
     */
    public String getDefaultTitle() {
        if (type == RingtoneManager.TYPE_NOTIFICATION) {
            return context.getString(R.string.notification_sound_default);
        }
        if (type == RingtoneManager.TYPE_ALARM) {
            return context.getString(R.string.alarm_sound_default);
        }
        return context.getString(R.string.ringtone_default);
    }

    /**
     * Get the 'silent' tone title.
     *
     * @return the title.
     */
    public String getSilentTitle() {
        return context.getString(R.string.ringtone_silent);
    }
}
