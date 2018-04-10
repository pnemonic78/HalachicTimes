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
package com.github.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.github.lib.R;

public class SeekBarDialogPreference extends DialogPreference implements OnSeekBarChangeListener {

    private static final int[] ATTRIBUTES = {android.R.attr.max};

    /**
     * Seek bar in the dialog.
     */
    private SeekBar seekBar;
    /**
     * Summary view in the dialog.
     */
    private TextView summaryView;
    private int progress;
    private int max;
    private boolean progressSet;
    private final Resources resources;
    private int summaryFormat;
    private Object[] summaryArgs;

    public SeekBarDialogPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        this.resources = context.getResources();

        final TypedArray a = context.obtainStyledAttributes(attrs, ATTRIBUTES, defStyleAttr, defStyleRes);
        this.max = a.getInt(0, 100);
        a.recycle();

        if (getDialogLayoutResource() == 0) {
            setDialogLayoutResource(R.layout.preference_seekbar);
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        SeekBar seekBar = view.findViewById(android.R.id.edit);
        this.summaryView = view.findViewById(android.R.id.summary);
        this.seekBar = seekBar;

        int progress = getProgress();

        seekBar.setMax(getMax());
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setEnabled(isEnabled());
        if (progress == seekBar.getProgress()) {
            onProgressChanged(seekBar, progress, false);
        } else {
            seekBar.setProgress(progress);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            int progress = seekBar.getProgress();
            if (callChangeListener(progress)) {
                setProgress(progress);
            }
        }
    }

    @Override
    protected Integer onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setProgress(restoreValue ? getPersistedInt(progress) : (Integer) defaultValue);
    }

    /**
     * Gets the progress from the {@link SharedPreferences}.
     *
     * @return the progress.
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Set the range of the progress bar to {@code 0}...{@code max}.
     *
     * @param max the upper range of this progress bar.
     */
    public void setMax(int max) {
        this.max = max;
        if (seekBar != null)
            seekBar.setMax(max);
    }

    /**
     * Get the maximum progress.
     *
     * @return the upper range of this progress bar.
     */
    public int getMax() {
        return max;
    }

    /**
     * Saves the progress to the {@link SharedPreferences}.
     *
     * @param progress the progress.
     */
    public void setProgress(int progress) {
        // Always persist/notify the first time; don't assume the field's
        // default value.
        final boolean changed = this.progress != progress;
        if (changed || !progressSet) {
            this.progress = progress;
            progressSet = true;
            persistInt(progress);
            if (changed) {
                notifyDependencyChange(shouldDisableDependents());
                notifyChanged();
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if ((this.seekBar == seekBar) && (summaryView != null)) {
            summaryArgs[0] = progress;
            updateSummary();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    /**
     * Set the summary format.
     *
     * @param pluralId the plural id for quantity.
     * @param args     the plural arguments.
     */
    public void setSummaryFormat(int pluralId, Object... args) {
        summaryFormat = pluralId;
        if (args != null) {
            int length = args.length;
            summaryArgs = new Object[1 + length];
            System.arraycopy(args, 0, summaryArgs, 1, length);
        } else {
            summaryArgs = new Object[1];
        }
        summaryArgs[0] = getProgress();
        updateSummary();
    }

    private void updateSummary() {
        CharSequence summary;
        if (summaryFormat != 0) {
            summary = resources.getQuantityString(summaryFormat, progress, summaryArgs);
        } else {
            summary = summaryArgs[0].toString();
        }
        setSummary(summary);
        if (summaryView != null) {
            summaryView.setText(summary);
        }
    }
}
