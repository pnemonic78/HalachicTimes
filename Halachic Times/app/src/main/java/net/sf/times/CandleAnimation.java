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
package net.sf.times;

import java.util.Random;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.Handler;
import android.text.format.DateUtils;
import android.widget.ImageView;

/**
 * Flicker animation for 1 candle.
 * 
 * @author Moshe W
 */
public class CandleAnimation implements Runnable {

	private static final int LEVELS = 6;
	private static final long PERIOD = DateUtils.SECOND_IN_MILLIS >> 1;
	private static final int PERIOD_INT = (int) PERIOD;

	private final Handler mHandler;
	private Drawable mCandle;
	/** Randomizer. */
	private final Random mRandom;
	private static Drawable[] mSprites;

	/**
	 * Create a new animation.
	 * 
	 * @param handler
	 *            the timer.
	 * @param view
	 *            the image view.
	 */
	public CandleAnimation(Handler handler, ImageView view) {
		this(handler, view, null);
	}

	/**
	 * Create a new animation.
	 * 
	 * @param handler
	 *            the timer.
	 * @param view
	 *            the image view.
	 * @param random
	 *            the delay randomizer.
	 */
	public CandleAnimation(Handler handler, ImageView view, Random random) {
		mHandler = handler;
		if (view == null)
			throw new IllegalArgumentException("view required");
		mRandom = random;

		// Cache the images to avoid "bitmap size exceeds VM budget".
		if (mSprites == null) {
			mSprites = new Drawable[LEVELS];

			Resources res = view.getResources();
			Options opts = new Options();
			opts.inDither = false;
			Bitmap bmp0 = BitmapFactory.decodeResource(res, R.drawable.candle_0, opts);
			Bitmap bmp1 = BitmapFactory.decodeResource(res, R.drawable.candle_1, opts);
			Bitmap bmp2 = BitmapFactory.decodeResource(res, R.drawable.candle_2, opts);
			Bitmap bmp3 = BitmapFactory.decodeResource(res, R.drawable.candle_3, opts);
			mSprites[0] = new BitmapDrawable(res, bmp0);
			mSprites[1] = new BitmapDrawable(res, bmp1);
			mSprites[2] = new BitmapDrawable(res, bmp2);
			mSprites[3] = new BitmapDrawable(res, bmp3);
			mSprites[4] = mSprites[2];
			mSprites[5] = mSprites[1];
		}

		LevelListDrawable candle = new LevelListDrawable();
		for (int i = 0; i < LEVELS; i++)
			candle.addLevel(0, i, mSprites[i]);
		view.setImageDrawable(candle);
		mCandle = candle;
	}

	@Override
	public void run() {
		int level = mCandle.getLevel();
		level++;
		if (level >= LEVELS)
			level = 0;
		mCandle.setLevel(level);

		if (mRandom == null)
			mHandler.postDelayed(this, PERIOD);
		else
			mHandler.postDelayed(this, mRandom.nextInt(PERIOD_INT));
	}

}
