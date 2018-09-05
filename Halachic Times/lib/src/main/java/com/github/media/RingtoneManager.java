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
package com.github.media;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;

import com.github.lib.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION;
import static android.os.Build.VERSION_CODES;

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
    private static final String SETTINGS_PATH = Settings.System.CONTENT_URI.toString();
    private static final String FILE_PATH = ContentResolver.SCHEME_FILE + ":/";

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
        setIncludeExternal((VERSION.SDK_INT < VERSION_CODES.KITKAT)
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

            boolean dangerousUri = false;
            if (uriString.startsWith(EXTERNAL_PATH)) {
                dangerousUri = true;
            } else if (VERSION.SDK_INT >= VERSION_CODES.N) {
                if (uriString.startsWith(FILE_PATH)) {
                    dangerousUri = true;
                } else {
                    File file = new File(uriString);
                    dangerousUri = file.exists() && file.isFile();
                }
            }
            if (dangerousUri) {
                // Try a 'default' tone.
                uriString = getDefaultUri(type).toString();

                if (VERSION.SDK_INT >= VERSION_CODES.N) {
                    if (uriString.startsWith(FILE_PATH)) {
                        return SILENT_PATH;
                    }
                    File file = new File(uriString);
                    if (file.exists() && file.isFile()) {
                        return SILENT_PATH;
                    }
                }
            }

            Uri uri = Uri.parse(uriString);
            Uri uriResolved = resolveUri(context, uri);
            if (uri != uriResolved) {
                if (uriResolved == null) {
                    return DEFAULT_PATH;
                }
                String uriResolvedString = uriResolved.toString();
                if (uriResolvedString.startsWith(INTERNAL_PATH)) {
                    // Is definitely internal.
                    return uriString;
                }
                if (uriResolvedString.startsWith(EXTERNAL_PATH)) {
                    // 'Default' tone is definitely external.
                    return SILENT_PATH;
                }
            }
        }
        return uriString;
    }

    public static Uri resolveUri(Context context, Uri uri) {
        String uriString = uri.toString();
        if (uriString.startsWith(SETTINGS_PATH)) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(uri, SETTINGS_COLUMNS, null, null, null);
            if ((cursor != null) && cursor.moveToFirst()) {
                String path = cursor.getString(URI_COLUMN_INDEX);
                cursor.close();

                if (path == null) {
                    return null;
                }
                return Uri.parse(path);
            }
        }
        return uri;
    }

    public String filterInternal(Uri uri) {
        return filterInternal(uri != null ? uri.toString() : null);
    }

    public String filterInternalMaybe(Uri uri) {
        return filterInternalMaybe(uri != null ? uri.toString() : null);
    }

    public String filterInternalMaybe(String uriString) {
        return isIncludeExternal() ? uriString : filterInternal(uriString);
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
