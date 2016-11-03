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
package net.sf.preference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.media.Ringtone;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;

import net.sf.lib.R;
import net.sf.media.RingtoneManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Preference} that allows the user to choose a ringtone from those on the device.
 * The chosen ringtone's URI will be persisted as a string.
 *
 * @author Moshe Waisberg
 */
public class RingtonePreference extends DialogPreference {

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

    private static final int[] ATTRIBUTES = {android.R.attr.ringtoneType, android.R.attr.showDefault, android.R.attr.showSilent};

    private static final String DEFAULT_PATH = RingtoneManager.DEFAULT_PATH;
    private static final Uri DEFAULT_URI = null;

    private static final String SILENT_PATH = RingtoneManager.SILENT_PATH;
    private static final Uri SILENT_URI = RingtoneManager.SILENT_URI;

    private static final int POS_UNKNOWN = -1;

    private int ringtoneType;
    private boolean showDefault;
    private boolean showSilent;
    private List<CharSequence> entries;
    private List<Uri> entryValues;
    private Uri value;
    private RingtoneManager ringtoneManager;
    private Ringtone ringtoneSample;

    /** The position in the list of the 'Silent' item. */
    private int silentPos = POS_UNKNOWN;

    /** The position in the list of the 'Default' item. */
    private int defaultRingtonePos = POS_UNKNOWN;

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

        final TypedArray a = context.obtainStyledAttributes(attrs, ATTRIBUTES, defStyleAttr, defStyleRes);
        int ringtoneType = a.getInt(0, RingtoneManager.TYPE_RINGTONE);
        boolean showDefault = a.getBoolean(1, true);
        boolean showSilent = a.getBoolean(2, true);
        a.recycle();

        ringtoneManager = new RingtoneManager(context);
        setRingtoneType(ringtoneType);
        setShowDefault(showDefault);
        setShowSilent(showSilent);
    }

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
                ringtoneManager = new RingtoneManager(context);
                entries = null;
                entryValues = null;
            }
            ringtoneManager.setType(type);

            defaultRingtoneUri = RingtoneManager.getDefaultUri(type);
            defaultRingtone = RingtoneManager.getRingtone(context, defaultRingtoneUri);
        }
        ringtoneType = type;

        // The volume keys will control the stream that we are choosing a ringtone for
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.setVolumeControlStream(ringtoneManager.inferStreamType());
        }
    }

    /**
     * Get the ringtone type.
     *
     * @return the type. One of {@link RingtoneManager#TYPE_ALARM} or {@link RingtoneManager#TYPE_NOTIFICATION} or {@link RingtoneManager#TYPE_RINGTONE}, or {@link RingtoneManager#TYPE_ALL}.
     */
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
     * <p/>
     * By default, this saves the ringtone URI to the persistent storage as a
     * string.
     *
     * @param ringtoneUri
     *         The chosen ringtone's {@link Uri}. Can be {@code null}.
     */
    protected void onSaveRingtone(Uri ringtoneUri) {
        persistString(ringtoneUri != null ? ringtoneUri.toString() : SILENT_PATH);
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
        final String uriString = getValue();
        if (uriString == DEFAULT_PATH) {
            return defaultRingtoneUri;
        }
        return TextUtils.isEmpty(uriString) ? SILENT_URI : Uri.parse(uriString);
    }

    @Override
    protected String onGetDefaultValue(TypedArray a, int index) {
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
            if ((value == DEFAULT_URI) || value.equals(defaultRingtoneUri)) {
                return defaultRingtonePos;
            }
            if (SILENT_URI.equals(value)) {
                return silentPos;
            }

            Uri entryValue;
            for (int i = entryValues.size() - 1; i >= 0; i--) {
                entryValue = entryValues.get(i);
                if (value.equals(entryValue)) {
                    return i;
                }
            }
        }
        return POS_UNKNOWN;
    }

    private int getValueIndex() {
        return findIndexOfValue(onRestoreRingtone());
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        List<CharSequence> entries = getEntries();
        CharSequence[] items = entries.toArray(new CharSequence[entries.size()]);
        builder.setSingleChoiceItems(items, getValueIndex(), this);
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

        if (positiveResult) {
            Uri uri = value;
            if (callChangeListener(uri != null ? uri.toString() : SILENT_PATH)) {
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
            entries = new ArrayList<>();
            entryValues = new ArrayList<>();

            if (showDefault) {
                String uriPath = ringtoneManager.filterInternal(defaultRingtoneUri);
                if (uriPath != null) {
                    defaultRingtonePos = entryValues.size();
                    entries.add(ringtoneManager.getDefaultTitle());
                    entryValues.add(defaultRingtoneUri);
                } else {
                    defaultRingtonePos = -1;
                }
            } else {
                defaultRingtonePos = -1;
            }
            if (showSilent) {
                silentPos = entryValues.size();
                entries.add(ringtoneManager.getSilentTitle());
                entryValues.add(SILENT_URI);
            } else {
                silentPos = -1;
            }

            Cursor cursor = ringtoneManager.getCursor();
            if ((cursor != null) && cursor.moveToFirst()) {
                Uri uri;
                do {
                    uri = Uri.parse(cursor.getString(URI_COLUMN_INDEX));
                    entries.add(cursor.getString(TITLE_COLUMN_INDEX));
                    entryValues.add(ContentUris.withAppendedId(uri, cursor.getLong(ID_COLUMN_INDEX)));
                } while (cursor.moveToNext());
                cursor.close();
            }
        }
        return entries;
    }

    private void playRingtone(int position, int delay) {
        value = getRingtoneUri(position);

        if (ringtoneSample != null) {
            ringtoneSample.stop();
        }

        Ringtone ringtone;
        if (position == silentPos) {
            ringtone = null;
        } else if (position == defaultRingtonePos) {
            ringtone = defaultRingtone;
        } else {
            ringtone = RingtoneManager.getRingtone(getContext(), getRingtoneUri(position));
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

    /**
     * Returns the value of the key.
     *
     * @return The value of the key.
     */
    public String getValue() {
        return ringtoneManager.filterInternal(getPersistedString(DEFAULT_PATH));
    }

    /**
     * Get the ringtone title.
     */
    public CharSequence getRingtoneTitle() {
        return getRingtoneTitle(getValue());
    }

    /**
     * Get the ringtone title.
     *
     * @param uriString
     *         the Uri path.
     * @return the title.
     */
    public CharSequence getRingtoneTitle(String uriString) {
        if (uriString == DEFAULT_PATH) {
            return ringtoneManager.getDefaultTitle();
        }
        if (uriString.equals(SILENT_PATH)) {
            return ringtoneManager.getSilentTitle();
        }
        Uri uri = Uri.parse(uriString);
        int index = findIndexOfValue(uri);
        if (index > POS_UNKNOWN) {
            return entries.get(index);
        }

        Context context = getContext();
        Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
        if (ringtone != null) {
            return ringtone.getTitle(context);
        }
        return null;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        final SavedState myState = new SavedState(superState);
        myState.value = this.value;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !(state instanceof SavedState)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        this.value = myState.value;
    }

    private static class SavedState extends BaseSavedState {
        Uri value;

        public SavedState(Parcel source) {
            super(source);
            value = Uri.CREATOR.createFromParcel(source);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            Uri.writeToParcel(dest, value);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
