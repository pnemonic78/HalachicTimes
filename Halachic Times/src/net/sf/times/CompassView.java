/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/MPL-1.1.html
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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.View;

/**
 * Compass view.
 * 
 * @author Moshe
 */
public class CompassView extends View {

	private static final int PADDING = 10;
	private static final int CIRCLE_THICKNESS = 2;

	private static final double PI_2 = Math.PI / 2;

	private float mNorth;
	private float mSouth;
	private float mEast;
	private float mWest;
	private float mHoliest;

	private Paint mPaintCircle;
	private Paint mPaintNorth;
	private Paint mPaintSouth;
	private Paint mPaintEast;
	private Paint mPaintWest;
	private Paint mPaintHoliest;

	/**
	 * Constructs a new compass view.
	 * 
	 * @param context
	 *            the context.
	 */
	public CompassView(Context context) {
		super(context);
	}

	/**
	 * Constructs a new compass view.
	 * 
	 * @param context
	 *            the context.
	 * @param attrs
	 *            the attributes.
	 */
	public CompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Constructs a new compass view.
	 * 
	 * @param context
	 *            the context.
	 * @param attrs
	 *            the attributes.
	 * @param defStyle
	 *            the default style.
	 */
	public CompassView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		final int w = getWidth();
		final int h = getHeight();
		final int w2 = w / 2;
		final int h2 = h / 2;
		final int r = Math.min(w2, h2) - PADDING - CIRCLE_THICKNESS;

		if (mPaintCircle == null) {
			mPaintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaintCircle.setStyle(Paint.Style.STROKE);
			mPaintCircle.setStrokeWidth(CIRCLE_THICKNESS);
			mPaintCircle.setColor(Color.WHITE);
		}
		canvas.drawCircle(w2, h2, r, mPaintCircle);

		if (mPaintNorth == null) {
			mPaintNorth = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaintNorth.setStyle(Paint.Style.STROKE);
			mPaintNorth.setStrokeWidth(4);
			mPaintNorth.setColor(Color.RED);
		}
		float radiansNorth = -mNorth;
		canvas.drawLine(w2, h2, w2 + (r * FloatMath.sin(radiansNorth)), h2 - (r * FloatMath.cos(radiansNorth)), mPaintNorth);

		if (mPaintSouth == null) {
			mPaintSouth = new Paint(mPaintNorth);
			mPaintSouth.setStrokeWidth(1);
			mPaintSouth.setColor(Color.YELLOW);
		}
		float radiansSouth = -mSouth;
		canvas.drawLine(w2, h2, w2 + (r * FloatMath.sin(radiansSouth)), h2 - (r * FloatMath.cos(radiansSouth)), mPaintSouth);

		if (mPaintEast == null) {
			mPaintEast = new Paint(mPaintSouth);
			mPaintEast.setColor(Color.GRAY);
		}
		float radiansEast = -mEast;
		canvas.drawLine(w2, h2, w2 + (r * FloatMath.sin(radiansEast)), h2 - (r * FloatMath.cos(radiansEast)), mPaintEast);

		if (mPaintWest == null) {
			mPaintWest = new Paint(mPaintEast);
			mPaintWest.setColor(Color.GRAY);
		}
		float radiansWest = -mWest;
		canvas.drawLine(w2, h2, w2 + (r * FloatMath.sin(radiansWest)), h2 - (r * FloatMath.cos(radiansWest)), mPaintWest);

		if (mPaintHoliest == null) {
			mPaintHoliest = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaintHoliest.setStyle(Paint.Style.STROKE);
			mPaintHoliest.setStrokeWidth(4);
			mPaintHoliest.setColor(Color.BLUE);
		}
		float radiansHoliest = radiansNorth + (float) Math.toRadians(mHoliest);
		canvas.drawLine(w2, h2, w2 + (r * FloatMath.sin(radiansHoliest)), h2 - (r * FloatMath.cos(radiansHoliest)), mPaintHoliest);
	}

	/**
	 * Set the bearing to magnetic North pole.
	 * 
	 * @param bearing
	 *            the bearing in radians.
	 */
	public void setNorth(float bearing) {
		mNorth = bearing;
		mSouth = (float) (bearing + Math.PI);
		mEast = (float) (bearing - PI_2);
		mWest = (float) (bearing + PI_2);
		invalidate();
	}

	/**
	 * Set the bearing to the Holy of Holies.
	 * 
	 * @param bearing
	 *            the bearing in degrees.
	 */
	public void setHoliest(float bearing) {
		mHoliest = bearing;
		invalidate();
	}
}
