package net.sf.preference;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;

import net.sf.times.R;

/**
 * A {@link Preference} that allows the user to choose a ringtone from those on the device.
 * The chosen ringtone's URI will be persisted as a string.
 *
 * @author Moshe Waisberg
 */
public class RingtonePreference extends DialogPreference {

    private static final int[] ATTRS = {android.R.attr.ringtoneType, android.R.attr.showDefault, android.R.attr.showSilent};

    private int ringtoneType;
    private boolean showDefault;
    private boolean showSilent;
    private Cursor entries;
    private int clickedDialogEntryIndex = -1;
    private RingtoneManager ringtoneManager;
    private Ringtone ringtone;

    public RingtonePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs, ATTRS, defStyleAttr, defStyleRes);
        ringtoneType = a.getInt(0, RingtoneManager.TYPE_RINGTONE);
        showDefault = a.getBoolean(1, true);
        showSilent = a.getBoolean(2, true);
        a.recycle();

        ringtoneManager = new RingtoneManager(context);
        ringtoneManager.setType(ringtoneType);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RingtonePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RingtonePreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.ringtonePreferenceStyle);
    }

    public void setRingtoneType(int type) {
        ringtoneManager.setType(type);
        ringtoneType = type;
    }

    public int getRingtoneType() {
        return ringtoneType;
    }

    /**
     * Returns whether to a show an item for the default sound/ringtone.
     *
     * @return Whether to show an item for the default sound/ringtone.
     */
    public boolean getShowDefault() {
        return showDefault;
    }

    /**
     * Sets whether to show an item for the default sound/ringtone. The default
     * to use will be deduced from the sound type(s) being shown.
     *
     * @param showDefault
     *         Whether to show the default or not.
     * @see RingtoneManager#EXTRA_RINGTONE_SHOW_DEFAULT
     */
    public void setShowDefault(boolean showDefault) {
        this.showDefault = showDefault;
    }

    /**
     * Returns whether to a show an item for 'Silent'.
     *
     * @return Whether to show an item for 'Silent'.
     */
    public boolean getShowSilent() {
        return showSilent;
    }

    /**
     * Sets whether to show an item for 'Silent'.
     *
     * @param showSilent
     *         Whether to show 'Silent'.
     * @see RingtoneManager#EXTRA_RINGTONE_SHOW_SILENT
     */
    public void setShowSilent(boolean showSilent) {
        this.showSilent = showSilent;
    }

    /**
     * Called when a ringtone is chosen.
     * <p>
     * By default, this saves the ringtone URI to the persistent storage as a
     * string.
     *
     * @param ringtoneUri
     *         The chosen ringtone's {@link Uri}. Can be null.
     */
    protected void onSaveRingtone(Uri ringtoneUri) {
        persistString(ringtoneUri != null ? ringtoneUri.toString() : "");
    }

    /**
     * Called when the chooser is about to be shown and the current ringtone
     * should be marked. Can return null to not mark any ringtone.
     * <p>
     * By default, this restores the previous ringtone URI from the persistent
     * storage.
     *
     * @return The ringtone to be marked as the current ringtone.
     */
    protected Uri onRestoreRingtone() {
        final String uriString = getPersistedString(null);
        return !TextUtils.isEmpty(uriString) ? Uri.parse(uriString) : null;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValueObj) {
        String defaultValue = (String) defaultValueObj;

        /*
         * This method is normally to make sure the internal state and UI
         * matches either the persisted value or the default value. Since we
         * don't show the current value in the UI (until the dialog is opened)
         * and we don't keep local state, if we are restoring the persisted
         * value we don't need to do anything.
         */
        if (restorePersistedValue) {
            return;
        }

        // If we are setting to the default value, we should persist it.
        if (!TextUtils.isEmpty(defaultValue)) {
            onSaveRingtone(Uri.parse(defaultValue));
        }
    }

    private int getValueIndex() {
        return -1;//TODO implement me!
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        if (entries == null) {
            entries = getCursor();
        }
        if (showDefault) {
            //TODO add to list
        }
        if (showSilent) {
            //TODO add to list
        }

        clickedDialogEntryIndex = getValueIndex();
        builder.setSingleChoiceItems(entries, clickedDialogEntryIndex, MediaStore.Audio.Media.TITLE, this);
        builder.setPositiveButton(R.string.ok, this);
        builder.setNegativeButton(R.string.cancel, this);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);

        if (which >= 0) {
            clickedDialogEntryIndex = which;
            ringtone = ringtoneManager.getRingtone(which);
            if (ringtone.isPlaying()) {
                ringtone.stop();
            }
            ringtone.play();
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult && clickedDialogEntryIndex >= 0) {
            Uri uri = ringtoneManager.getRingtoneUri(clickedDialogEntryIndex);
            if (callChangeListener(uri != null ? uri.toString() : "")) {
                onSaveRingtone(uri);
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (ringtone != null) {
            ringtone.stop();
        }
    }

    private Cursor getCursor() {
        return ringtoneManager.getCursor();
    }
}
