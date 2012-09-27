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
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.text.TextPaint;
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
	private float mEast;
	private float mSouth;
	private float mWest;
	private float mHoliest;

	private Paint mPaintCircle;
	private Paint mPaintNorth;
	private Paint mPaintEast;
	private Paint mPaintSouth;
	private Paint mPaintWest;
	private Paint mPaintHoliest;
	private Paint mPaintFill;
	private final RectF mRectFill = new RectF();

	private String mLabelNorth;
	private String mLabelEast;
	private String mLabelSouth;
	private String mLabelWest;

	/**
	 * Constructs a new compass view.
	 * 
	 * @param context
	 *            the context.
	 */
	public CompassView(Context context) {
		super(context);
		init(context);
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
		init(context);
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
		init(context);
	}

	/** Initialise. */
	private void init(Context context) {
		mLabelNorth = context.getString(R.string.north);
		mLabelEast = context.getString(R.string.east);
		mLabelSouth = context.getString(R.string.south);
		mLabelWest = context.getString(R.string.west);

		mPaintCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintCircle.setStyle(Paint.Style.STROKE);
		mPaintCircle.setStrokeWidth(CIRCLE_THICKNESS);
		mPaintCircle.setColor(Color.WHITE);

		mPaintNorth = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		mPaintNorth.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaintNorth.setStrokeWidth(4);
		mPaintNorth.setTextSize(18);
		mPaintNorth.setTextAlign(Align.CENTER);
		mPaintNorth.setColor(Color.RED);

		mPaintSouth = new TextPaint(mPaintNorth);
		mPaintSouth.setStrokeWidth(1);
		mPaintSouth.setColor(Color.YELLOW);

		mPaintEast = new TextPaint(mPaintSouth);
		mPaintEast.setColor(Color.WHITE);

		mPaintWest = new TextPaint(mPaintEast);

		mPaintHoliest = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintHoliest.setStyle(Paint.Style.STROKE);
		mPaintHoliest.setStrokeWidth(6);
		mPaintHoliest.setColor(Color.BLUE);

		mPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintFill.setColor(Color.GRAY);
		mPaintFill.setStyle(Paint.Style.STROKE);
		mPaintFill.setStrokeCap(Paint.Cap.BUTT);

		setNorth(0f);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		final int w = getWidth();
		final int h = getHeight();
		final int w2 = w / 2;
		final int h2 = h / 2;
		final float r = Math.min(w2, h2) - PADDING - CIRCLE_THICKNESS;
		final float r2 = r / 2f;
		final float r34 = (r * 3f) / 4f;
		final float sizeDirections = r / 5f;
		final float sizeDirections2 = sizeDirections / 2f;
		final float degreesNorth = (float) Math.toDegrees(mNorth);

		mRectFill.left = w2 - r2;
		mRectFill.top = h2 - r2;
		mRectFill.right = w2 + r2;
		mRectFill.bottom = h2 + r2;
		mPaintFill.setStrokeWidth(r);
		float sweepAngle = mHoliest - degreesNorth;
		if (sweepAngle > 180f)
			sweepAngle = sweepAngle - 360f;
		canvas.drawArc(mRectFill, -90f, sweepAngle, false, mPaintFill);

		mPaintNorth.setTextSize(sizeDirections);
		float radiansNorth = -mNorth;
		float sinNorth = FloatMath.sin(radiansNorth);
		float cosNorth = FloatMath.cos(radiansNorth);
		canvas.drawLine(w2 + (r34 * sinNorth), h2 - (r34 * cosNorth), w2 + (r * sinNorth), h2 - (r * cosNorth), mPaintNorth);
		canvas.drawText(mLabelNorth, w2 + (r2 * sinNorth), h2 - (r2 * cosNorth), mPaintNorth);

		mPaintEast.setTextSize(sizeDirections);
		float radiansEast = -mEast;
		float sinEast = FloatMath.sin(radiansEast);
		float cosEast = FloatMath.cos(radiansEast);
		canvas.drawLine(w2 + (r34 * sinEast), h2 - (r34 * cosEast), w2 + (r * sinEast), h2 - (r * cosEast), mPaintEast);
		canvas.drawText(mLabelEast, w2 + (r2 * sinEast), h2 + sizeDirections2 - (r2 * cosEast), mPaintEast);

		mPaintSouth.setTextSize(sizeDirections);
		float radiansSouth = -mSouth;
		float sinSouth = FloatMath.sin(radiansSouth);
		float cosSouth = FloatMath.cos(radiansSouth);
		canvas.drawLine(w2 + (r34 * sinSouth), h2 - (r34 * cosSouth), w2 + (r * sinSouth), h2 - (r * cosSouth), mPaintSouth);
		canvas.drawText(mLabelSouth, w2 + (r2 * sinSouth), h2 - (r2 * cosSouth), mPaintSouth);

		mPaintWest.setTextSize(sizeDirections);
		float radiansWest = -mWest;
		float sinWest = FloatMath.sin(radiansWest);
		float cosWest = FloatMath.cos(radiansWest);
		canvas.drawLine(w2 + (r34 * sinWest), h2 - (r34 * cosWest), w2 + (r * sinWest), h2 - (r * cosWest), mPaintWest);
		canvas.drawText(mLabelWest, w2 + (r2 * sinWest), h2 + sizeDirections2 - (r2 * cosWest), mPaintWest);

		float radiansHoliest = radiansNorth + (float) Math.toRadians(mHoliest);
		float sinHoliest = FloatMath.sin(radiansHoliest);
		float cosHoliest = FloatMath.cos(radiansHoliest);
		canvas.drawLine(w2, h2, w2 + (r * sinHoliest), h2 - (r * cosHoliest), mPaintHoliest);

		canvas.drawCircle(w2, h2, r, mPaintCircle);
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
