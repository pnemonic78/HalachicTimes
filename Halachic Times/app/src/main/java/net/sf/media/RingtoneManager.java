package net.sf.media;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;

/**
 * Ringtone manager.
 */
public class RingtoneManager extends android.media.RingtoneManager {

    private Cursor cursor;

    public RingtoneManager(Activity activity) {
        super(activity);
    }

    public RingtoneManager(Context context) {
        super(context);
    }

    @Override
    public void setType(int type) {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
        super.setType(type);
    }

    @Override
    public Cursor getCursor() {
        if (cursor == null) {
            cursor = super.getCursor();
        }
        return cursor;
    }
}
