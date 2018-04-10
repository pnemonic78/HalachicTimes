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
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * SeekBar preference.
 *
 * @author Moshe Waisberg
 */
public class SeekBarPreference extends Preference implements OnSeekBarChangeListener {

    private static final int[] ATTRIBUTES = {android.R.attr.max};

    /** Delay in milliseconds to wait for user to finish changing the seek bar. */
    private static final long PERSIST_DELAY = 650;

    private SeekBar seekBar;
    private int progress;
    private int max;
    private Timer timer;
    private PersistTask task;
    private Toast toast;

    public SeekBarPreference(Context context) {
        super(context);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.preferenceStyle);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    /**
     * Creates a new seek bar preference.
     *
     * @param context      the context.
     * @param attrs        the attributes.
     * @param defStyleAttr the default attribute style.
     * @param defStyleRes  the default resource style.
     */
    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs, ATTRIBUTES, defStyleAttr, defStyleRes);
        this.max = a.getInt(0, 100);
        a.recycle();
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);

        TextView title = view.findViewById(android.R.id.title);
        RelativeLayout host = (RelativeLayout) title.getParent();

        seekBar = new SeekBar(getContext());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.alignWithParent = true;
        lp.addRule(RelativeLayout.BELOW, android.R.id.summary);
        host.addView(seekBar, lp);

        return view;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        int progress = getProgress();

        seekBar.setMax(max);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setEnabled(isEnabled());
        if (progress == seekBar.getProgress()) {
            onProgressChanged(seekBar, progress, false);
        } else {
            seekBar.setProgress(progress);
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
     * Set the progress state and saves it to the {@link SharedPreferences}.
     *
     * @param progress the progress.
     */
    public void setProgress(int progress) {
        if (seekBar == null) {
            // Save this for when the seek bar is created.
            this.progress = progress;
        } else {
            // Calls onProgressChanged -> persistProgress
            seekBar.setProgress(progress);
        }
    }

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

    public int getMax() {
        return seekBar.getMax();
    }

    /**
     * Set the progress state and saves it to the {@link SharedPreferences}.
     *
     * @param progress the progress.
     */
    protected void persistProgress(int progress) {
        this.progress = progress;
        // Postpone persisting until user finished dragging.
        if (task != null)
            task.cancel();
        task = new PersistTask(progress);
        if (timer == null)
            timer = new Timer();
        timer.schedule(task, PERSIST_DELAY);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (this.seekBar == seekBar) {
            if (this.progress != progress) {
                // FIXME print the progress on the bar instead of toasting.
                if (toast == null)
                    toast = Toast.makeText(getContext(), String.valueOf(progress), Toast.LENGTH_SHORT);
                else {
                    toast.setText(String.valueOf(progress));
                    toast.show();
                }
                persistProgress(progress);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    /**
     * Timed task to persist the preference.
     *
     * @author Moshe
     */
    private class PersistTask extends TimerTask {

        private final int progress;

        /**
         * Constructs a new task.
         *
         * @param progress the progress to save.
         */
        public PersistTask(int progress) {
            this.progress = progress;
        }

        @Override
        public void run() {
            if (callChangeListener(progress)) {
                persistInt(progress);
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (seekBar != null) {
            seekBar.setEnabled(isEnabled());
        }
    }
}
