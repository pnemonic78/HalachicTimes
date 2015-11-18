package net.sf.preference;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
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

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Preference} that allows the user to choose a ringtone from those on the device.
 * The chosen ringtone's URI will be persisted as a string.
 *
 * @author Moshe Waisberg
 */
public class RingtonePreference extends DialogPreference {

    private static final int[] ATTRS = {android.R.attr.ringtoneType, android.R.attr.showDefault, android.R.attr.showSilent};

    /**
     * The column index (in the cursor} for the row ID.
     */
    public static final int ID_COLUMN_INDEX = 0;
    /**
     * The column index (in the cursor} for the title.
     */
    private static final int TITLE_COLUMN_INDEX = 1;
    /**
     * The column index (in the cursor} for the media provider's URI.
     */
    public static final int URI_COLUMN_INDEX = 2;

    private int ringtoneType;
    private boolean showDefault;
    private boolean showSilent;
    private boolean showExternal;
    private List<CharSequence> entries;
    private List<Uri> entryValues;
    private int clickedDialogEntryIndex;
    private RingtoneManager ringtoneManager;
    private Ringtone ringtoneSample;

    /** The position in the list of the 'Silent' item. */
    private int silentPos = -1;

    /** The position in the list of the 'Default' item. */
    private int defaultRingtonePos = -1;

    /** The Uri to play when the 'Default' item is clicked. */
    private Uri defaultRingtoneUri;

    /**
     * A Ringtone for the default ringtone. In most cases, the RingtoneManager
     * will stop the previous ringtone. However, the RingtoneManager doesn't
     * manage the default ringtone for us, so we should stop this one manually.
     */
    private Ringtone defaultRingtone;

    public RingtonePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs, ATTRS, defStyleAttr, defStyleRes);
        int ringtoneType = a.getInt(0, RingtoneManager.TYPE_RINGTONE);
        boolean showDefault = a.getBoolean(1, true);
        boolean showSilent = a.getBoolean(2, true);
        a.recycle();

        ringtoneManager = new RingtoneManager(context);
        setRingtoneType(ringtoneType);
        setShowDefault(showDefault);
        setShowSilent(showSilent);
        setShowExternal(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RingtonePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RingtonePreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.ringtonePreferenceStyle);
    }

    public void setRingtoneType(int type) {
        Context context = getContext();

        if (type != ringtoneType) {
            if (entries != null) {
                ringtoneManager.getCursor().close();
                ringtoneManager = new RingtoneManager(context);
                entries = null;
            }
            ringtoneManager.setType(type);

            defaultRingtoneUri = RingtoneManager.getDefaultUri(type);
            defaultRingtone = RingtoneManager.getRingtone(context, defaultRingtoneUri);
        }
        ringtoneType = type;

        // The volume keys will control the stream that we are choosing a ringtone for
        Activity activity = (Activity) context;
        activity.setVolumeControlStream(ringtoneManager.inferStreamType());
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
     * Returns whether to a show an item for 'Silent'.
     *
     * @return Whether to show an item for 'Silent'.
     */
    public boolean getShowExternal() {
        return showExternal;
    }

    /**
     * Sets whether to show external media.
     *
     * @param showExternal
     *         Whether to show externals.
     */
    public void setShowExternal(boolean showExternal) {
        this.showExternal = showExternal;
    }

    /**
     * Called when a ringtone is chosen.
     * <p/>
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
     * <p/>
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

    /**
     * Returns the index of the given value (in the entry values array).
     *
     * @param value
     *         The value whose index should be returned.
     * @return The index of the value, or -1 if not found.
     */
    public int findIndexOfValue(Uri value) {
        if (entryValues != null) {
            Uri entryValue;
            for (int i = entryValues.size() - 1; i >= 0; i--) {
                entryValue = entryValues.get(i);
                if ((value == entryValue) || value.equals(entryValue)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int getValueIndex() {
        return findIndexOfValue(onRestoreRingtone());
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        List<CharSequence> entries = getEntries();
        CharSequence[] items = entries.toArray(new CharSequence[entries.size()]);
        clickedDialogEntryIndex = getValueIndex();
        builder.setSingleChoiceItems(items, clickedDialogEntryIndex, this);
        builder.setPositiveButton(R.string.ok, this);
        builder.setNegativeButton(R.string.cancel, this);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);

        if (which >= 0) {
            // Play clip
            playRingtone(which, 0);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult && clickedDialogEntryIndex >= 0) {
            Uri uri = getRingtoneUri(clickedDialogEntryIndex);
            if (callChangeListener(uri != null ? uri.toString() : "")) {
                onSaveRingtone(uri);
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        stopAnyPlayingRingtone();
    }

    private List<CharSequence> getEntries() {
        if (entries == null) {
            Cursor cursor = ringtoneManager.getCursor();
            int count = cursor.getCount();

            if (showDefault) {
                count++;
            }
            if (showSilent) {
                count++;
            }
            entries = new ArrayList<>(count);
            entryValues = new ArrayList<>(count);

            if (showDefault) {
                defaultRingtonePos = entryValues.size();
                entries.add(getContext().getString(R.string.ringtone_default));
                entryValues.add(defaultRingtoneUri);
            } else {
                defaultRingtonePos = -1;
            }
            if (showSilent) {
                silentPos = entryValues.size();
                entries.add(getContext().getString(R.string.ringtone_silent));
                entryValues.add(null);
            } else {
                silentPos = -1;
            }
            if (cursor.moveToFirst()) {
                Uri uri;
                do {
                    uri = Uri.parse(cursor.getString(URI_COLUMN_INDEX));
                    if (showExternal || MediaStore.Audio.Media.INTERNAL_CONTENT_URI.equals(uri)) {
                        entries.add(cursor.getString(TITLE_COLUMN_INDEX));
                        entryValues.add(ContentUris.withAppendedId(uri, cursor.getLong(ID_COLUMN_INDEX)));
                    }
                } while (cursor.moveToNext());
            }
        }
        return entries;
    }

    private void playRingtone(int position, int delay) {
        clickedDialogEntryIndex = position;

        if (ringtoneSample != null) {
            ringtoneSample.stop();
        }

        Ringtone ringtone;
        if (position == silentPos) {
            ringtone = null;
        } else if (position == defaultRingtonePos) {
            ringtone = defaultRingtone;
        } else {
            ringtone = ringtoneManager.getRingtone(getContext(), getRingtoneUri(position));
        }

        if (ringtone != null) {
            ringtone.play();
        }
        ringtoneSample = ringtone;
    }

    private void stopAnyPlayingRingtone() {
        if (ringtoneSample != null) {
            ringtoneSample.stop();
        }
        if (ringtoneManager != null) {
            ringtoneManager.stopPreviousRingtone();
        }
    }

    private Uri getRingtoneUri(int position) {
        return entryValues.get(position);
    }
}
