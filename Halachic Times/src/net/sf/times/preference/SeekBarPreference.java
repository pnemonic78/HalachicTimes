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
package net.sf.times.preference;

import java.util.Timer;
import java.util.TimerTask;

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

/**
 * SeekBar preference.
 * 
 * @author Moshe Waisberg
 */
public class SeekBarPreference extends Preference implements OnSeekBarChangeListener {

	/** Android namespace. */
	private static final String NS_ANDROID = "http://schemas.android.com/apk/res/android";
	/** Delay in milliseconds to wait for user to finish changing the seek bar. */
	private static final long PERSIST_DELAY = 650;

	private final Context mContext;
	private SeekBar mSeekBar;
	private int mProgress;
	private int mMax = 100;
	private Timer mTimer;
	private PersistTask mTask;
	private Toast mToast;

	/**
	 * Creates a new seek bar preference.
	 * 
	 * @param context
	 *            the context.
	 */
	public SeekBarPreference(Context context) {
		super(context);
		mContext = context;
	}

	/**
	 * Creates a new seek bar preference.
	 * 
	 * @param context
	 *            the context.
	 * @param attrs
	 *            the attributes.
	 */
	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mMax = attrs.getAttributeIntValue(NS_ANDROID, "max", 100);
	}

	/**
	 * Creates a new seek bar preference.
	 * 
	 * @param context
	 *            the context.
	 * @param attrs
	 *            the attributes.
	 * @param defStyle
	 *            the default style.
	 */
	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		mMax = attrs.getAttributeIntValue(NS_ANDROID, "max", 100);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		View view = super.onCreateView(parent);

		TextView title = (TextView) view.findViewById(android.R.id.title);
		RelativeLayout host = (RelativeLayout) title.getParent();

		mSeekBar = new SeekBar(getContext());
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		lp.alignWithParent = true;
		lp.addRule(RelativeLayout.BELOW, android.R.id.summary);
		host.addView(mSeekBar, lp);

		return view;
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);

		final int max = mMax;
		final int progress = mProgress;
		if (max != mSeekBar.getMax())
			mSeekBar.setMax(max);
		mSeekBar.setOnSeekBarChangeListener(this);
		if (progress != mSeekBar.getProgress())
			mSeekBar.setProgress(progress);
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, 0);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		setProgress(restoreValue ? getPersistedInt(mProgress) : (Integer) defaultValue);
	}

	/**
	 * Set the progress state and saves it to the {@link SharedPreferences}.
	 * 
	 * @param progress
	 *            the progress.
	 */
	public void setProgress(int progress) {
		if (mSeekBar == null) {
			// Save this for when the seek bar is created.
			mProgress = progress;
		} else {
			// Calls onProgressChanged -> persistProgress
			mSeekBar.setProgress(progress);
		}
	}

	public int getProgress() {
		return mProgress;
	}

	/**
	 * Set the range of the progress bar to {@code 0}...{@code max}.
	 * 
	 * @param max
	 *            the upper range of this progress bar.
	 */
	public void setMax(int max) {
		mMax = max;
		if (mSeekBar != null)
			mSeekBar.setMax(max);
	}

	public int getMax() {
		return mSeekBar.getMax();
	}

	/**
	 * Set the progress state and saves it to the {@link SharedPreferences}.
	 * 
	 * @param progress
	 *            the progress.
	 */
	protected void persistProgress(int progress) {
		mProgress = progress;
		// Postpone persisting until user finished dragging.
		if (mTask != null)
			mTask.cancel();
		mTask = new PersistTask(progress);
		if (mTimer == null)
			mTimer = new Timer();
		mTimer.schedule(mTask, PERSIST_DELAY);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (mSeekBar == seekBar) {
			if (mProgress != progress) {
				// FIXME print the progress on the bar instead of toasting.
				if (mToast == null)
					mToast = Toast.makeText(mContext, String.valueOf(progress), Toast.LENGTH_SHORT);
				else {
					mToast.setText(String.valueOf(progress));
					mToast.show();
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

		private final int mProgress;

		/**
		 * Constructs a new task.
		 * 
		 * @param progress
		 *            the progress to save.
		 */
		public PersistTask(int progress) {
			super();
			mProgress = progress;
		}

		@Override
		public void run() {
			if (callChangeListener(mProgress)) {
				persistInt(mProgress);
			}
		}
	}
}
