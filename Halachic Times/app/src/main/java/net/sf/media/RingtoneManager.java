package net.sf.media;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Ringtone manager.
 */
public class RingtoneManager extends android.media.RingtoneManager {

    private static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    private static final String[] INTERNAL_COLUMNS = new String[]{
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
            "\"" + MediaStore.Audio.Media.INTERNAL_CONTENT_URI + "\"",
            MediaStore.Audio.Media.TITLE_KEY
    };

    private Context context;
    private Cursor cursor;
    private boolean includeExternal = true;

    /**
     * If a column (item from this list) exists in the Cursor, its value must
     * be true (value of 1) for the row to be returned.
     */
    private final List<String> filterColumns = new ArrayList<String>();

    public RingtoneManager(Context context) {
        super(context);
        this.context = context;
        setIncludeExternal((Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) || (context.checkCallingOrSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED));
    }

    @Override
    public void setType(int type) {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        super.setType(type);
        setFilterColumnsList(type);
    }

    @Override
    public Cursor getCursor() {
        if (cursor == null) {
            if (includeExternal) {
                cursor = super.getCursor();
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
    public boolean getIncludeExternal() {
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
}
