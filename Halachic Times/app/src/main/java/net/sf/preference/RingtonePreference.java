package net.sf.preference;

import android.annotation.TargetApi;
import android.app.Activity;
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
import android.provider.Settings;
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
    private int clickedDialogEntryIndex;
    private RingtoneManager ringtoneManager;

    /** The position in the list of the 'Silent' item. */
    private int mSilentPos = -1;

    /** The position in the list of the 'Default' item. */
    private int mDefaultRingtonePos = -1;

    /** The position in the list of the last clicked item. */
    private int mClickedPos = -1;

    /** The position in the list of the ringtone to sample. */
    private int mSampleRingtonePos = -1;

    /** The number of static items in the list. */
    private int mStaticItemCount;

    /** The Uri to play when the 'Default' item is clicked. */
    private Uri mUriForDefaultItem;

    /**
     * A Ringtone for the default ringtone. In most cases, the RingtoneManager
     * will stop the previous ringtone. However, the RingtoneManager doesn't
     * manage the default ringtone for us, so we should stop this one manually.
     */
    private Ringtone mDefaultRingtone;

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

        mUriForDefaultItem = Settings.System.DEFAULT_RINGTONE_URI;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RingtonePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RingtonePreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.ringtonePreferenceStyle);
    }

    public void setRingtoneType(int type) {
        if (type != ringtoneType) {
            if (entries != null) {
                entries.close();
                ringtoneManager = new RingtoneManager(getContext());
                entries = null;
            }
            ringtoneManager.setType(type);
        }
        ringtoneType = type;

        // The volume keys will control the stream that we are choosing a ringtone for
        Activity activity = (Activity) getContext();
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
        Uri uri = onRestoreRingtone();
        return ringtoneManager.getRingtonePosition(uri);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        if (entries == null) {
            entries = getCursor();
        }

        clickedDialogEntryIndex = getValueIndex();
        builder.setSingleChoiceItems(entries, clickedDialogEntryIndex, MediaStore.Audio.Media.TITLE, this);
        builder.setPositiveButton(R.string.ok, this);
        builder.setNegativeButton(R.string.cancel, this);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);

        // Save the position of most recently clicked item
        mClickedPos = which;

        if (which >= 0) {
            // Play clip
            playRingtone(which, 0);
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

        stopAnyPlayingRingtone();
    }

    private Cursor getCursor() {
        return ringtoneManager.getCursor();
    }

    private void playRingtone(int position, int delay) {
        mSampleRingtonePos = position;
        clickedDialogEntryIndex = position;

        if (mSampleRingtonePos == mSilentPos) {
            ringtoneManager.stopPreviousRingtone();
            return;
        }

        /*
         * Stop the default ringtone, if it's playing (other ringtones will be
         * stopped by the RingtoneManager when we get another Ringtone from it.
         */
        if (mDefaultRingtone != null && mDefaultRingtone.isPlaying()) {
            mDefaultRingtone.stop();
            mDefaultRingtone = null;
        }

        Ringtone ringtone;
        if (mSampleRingtonePos == mDefaultRingtonePos) {
            if (mDefaultRingtone == null) {
                mDefaultRingtone = RingtoneManager.getRingtone(getContext(), mUriForDefaultItem);
            }
            ringtone = mDefaultRingtone;

            /*
             * Normally the non-static RingtoneManager.getRingtone stops the
             * previous ringtone, but we're getting the default ringtone outside
             * of the RingtoneManager instance, so let's stop the previous
             * ringtone manually.
             */
            ringtoneManager.stopPreviousRingtone();
        } else {
            ringtone = ringtoneManager.getRingtone(getRingtoneManagerPosition(mSampleRingtonePos));
        }

        if (ringtone != null) {
            ringtone.play();
        }
    }

    private void stopAnyPlayingRingtone() {
        if (mDefaultRingtone != null && mDefaultRingtone.isPlaying()) {
            mDefaultRingtone.stop();
        }

        if (ringtoneManager != null) {
            ringtoneManager.stopPreviousRingtone();
        }
    }

    private int getRingtoneManagerPosition(int listPos) {
        return listPos - mStaticItemCount;
    }

}
