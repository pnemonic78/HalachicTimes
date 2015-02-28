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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Compass view.
 *
 * @author Moshe Waisberg
 */
public class CompassView extends View {

	private static final int PADDING = 0;
	private static final float CIRCLE_THICKNESS = 3;
	private static final float HOLIEST_THICKNESS = 6;
	private static final float NORTH_THICKNESS = 4;
	private static final float SOUTH_THICKNESS = 2;
	private static final float LABEL_THICKNESS = 1;
	private static final float LABEL2_THICKNESS = 1;
	private static final float PIVOT_RADIUS = HOLIEST_THICKNESS;

	private float mNorth;
	private float mHoliest;

	private Paint mPaintCircle;
	private Paint mPaintFrame;
	private Paint mPaintNorth;
	private Paint mPaintEast;
	private Paint mPaintSouth;
	private Paint mPaintWest;
	private Paint mPaintNE;
	private Paint mPaintHoliest;
	private Paint mPaintFill;
	private Paint mPaintPivot;
	private final RectF mRectFill = new RectF();
	private float mWidth2;
	private float mHeight2;
	private float mRadius;

	private String mLabelNorth;
	private String mLabelEast;
	private String mLabelSouth;
	private String mLabelWest;

	/**
	 * Constructs a new compass view.
	 *
	 * @param context
	 * 		the context.
	 */
	public CompassView(Context context) {
		super(context);
		init(context);
	}

	/**
	 * Constructs a new compass view.
	 *
	 * @param context
	 * 		the context.
	 * @param attrs
	 * 		the attributes.
	 */
	public CompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * Constructs a new compass view.
	 *
	 * @param context
	 * 		the context.
	 * @param attrs
	 * 		the attributes.
	 * @param defStyle
	 * 		the default style.
	 */
	public CompassView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	/** Initialise. */
	private void init(Context context) {
		Resources res = getResources();

		mLabelNorth = context.getString(R.string.north);
		mLabelEast = context.getString(R.string.east);
		mLabelSouth = context.getString(R.string.south);
		mLabelWest = context.getString(R.string.west);

		mPaintCircle = new Paint(Paint.DITHER_FLAG);
		mPaintCircle.setStyle(Paint.Style.FILL);
		mPaintCircle.setColor(res.getColor(R.color.compass));

		mPaintFrame = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintFrame.setStyle(Paint.Style.STROKE);
		mPaintFrame.setStrokeWidth(CIRCLE_THICKNESS);
		mPaintFrame.setColor(res.getColor(R.color.compass_frame));

		mPaintPivot = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintPivot.setColor(mPaintFrame.getColor());

		mPaintNorth = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		mPaintNorth.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaintNorth.setStrokeWidth(NORTH_THICKNESS);
		mPaintNorth.setTextSize(18);
		mPaintNorth.setTextAlign(Align.CENTER);
		mPaintNorth.setColor(res.getColor(R.color.compass_north));

		mPaintSouth = new TextPaint(mPaintNorth);
		mPaintSouth.setStrokeWidth(SOUTH_THICKNESS);
		mPaintSouth.setColor(res.getColor(R.color.compass_south));

		mPaintEast = new TextPaint(mPaintSouth);
		mPaintEast.setStrokeWidth(LABEL_THICKNESS);
		mPaintEast.setColor(res.getColor(R.color.compass_label));

		mPaintWest = new TextPaint(mPaintEast);

		mPaintNE = new Paint(mPaintEast);
		mPaintNE.setStrokeWidth(LABEL2_THICKNESS);
		mPaintNE.setColor(res.getColor(R.color.compass_label2));

		mPaintHoliest = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintHoliest.setStyle(Paint.Style.STROKE);
		mPaintHoliest.setStrokeWidth(HOLIEST_THICKNESS);
		mPaintHoliest.setColor(res.getColor(R.color.compass_arrow));

		mPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintFill.setColor(res.getColor(R.color.compass_arrow_bg));
		mPaintFill.setStyle(Paint.Style.STROKE);
		mPaintFill.setStrokeCap(Paint.Cap.BUTT);

		setAzimuth(0f);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		final float w2 = mWidth2;
		final float h2 = mHeight2;
		final float r = mRadius;
		final float r5 = r / 2f;
		final float r7 = (r * 7f) / 10f;
		final float r8 = (r * 8f) / 10f;
		final float r9 = (r * 9f) / 10f;
		final float h2r5 = h2 - r5;
		final float h2r7 = h2 - r7;
		final float h2r8 = h2 - r8;
		final float h2r9 = h2 - r9;

		canvas.drawCircle(w2, h2, r, mPaintCircle);

		float sweepAngle = mNorth + mHoliest;
		if (sweepAngle > 180f)
			sweepAngle -= 360f;
		canvas.drawArc(mRectFill, -90f, sweepAngle, false, mPaintFill);

		canvas.rotate(mNorth, w2, h2);
		canvas.drawLine(w2, h2r7, w2, h2r9, mPaintNorth);
		canvas.drawText(mLabelNorth, w2, h2r5, mPaintNorth);

		canvas.rotate(45, w2, h2);
		canvas.drawLine(w2, h2r8, w2, h2r9, mPaintNE);

		canvas.rotate(45, w2, h2);
		canvas.drawLine(w2, h2r7, w2, h2r9, mPaintEast);
		canvas.drawText(mLabelEast, w2, h2r5, mPaintEast);

		canvas.rotate(45, w2, h2);
		canvas.drawLine(w2, h2r8, w2, h2r9, mPaintNE);

		canvas.rotate(45, w2, h2);
		canvas.drawLine(w2, h2r7, w2, h2r9, mPaintSouth);
		canvas.drawText(mLabelSouth, w2, h2r5, mPaintSouth);

		canvas.rotate(45, w2, h2);
		canvas.drawLine(w2, h2r8, w2, h2r9, mPaintNE);

		canvas.rotate(45, w2, h2);
		canvas.drawLine(w2, h2r7, w2, h2r9, mPaintWest);
		canvas.drawText(mLabelWest, w2, h2r5, mPaintWest);

		canvas.rotate(45, w2, h2);
		canvas.drawLine(w2, h2r8, w2, h2r9, mPaintNE);

		canvas.rotate(45 + mHoliest, w2, h2);
		canvas.drawLine(w2, h2, w2, h2 - r, mPaintHoliest);

		canvas.drawCircle(w2, h2, r, mPaintFrame);
		canvas.drawCircle(w2, h2, PIVOT_RADIUS, mPaintPivot);
	}

	/**
	 * Set the azimuth to magnetic North pole.
	 *
	 * @param bearing
	 * 		the bearing in radians.
	 */
	public void setAzimuth(float bearing) {
		mNorth = (float) Math.toDegrees(-bearing);
		invalidate();
	}

	/**
	 * Set the bearing to the Holy of Holies.
	 *
	 * @param bearing
	 * 		the bearing in degrees.
	 */
	public void setHoliest(float bearing) {
		mHoliest = bearing;
		invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		final int w2 = w / 2;
		final int h2 = h / 2;
		final float r = Math.max(0, Math.min(w2, h2) - PADDING - CIRCLE_THICKNESS);
		final float r5 = r / 2f;

		mWidth2 = w2;
		mHeight2 = h2;
		mRadius = r;

		if (r > 0) {
			Resources res = getResources();
			RadialGradient gradientCircle = new RadialGradient(w2, h2, r * 3, res.getColor(R.color.compass), res.getColor(R.color.compass_gradient), Shader.TileMode.CLAMP);
			mPaintCircle.setShader(gradientCircle);
		}

		final float sizeDirections = r / 5f;
		mPaintNorth.setTextSize(sizeDirections);
		mPaintEast.setTextSize(sizeDirections);
		mPaintSouth.setTextSize(sizeDirections);
		mPaintWest.setTextSize(sizeDirections);

		mRectFill.left = w2 - r5;
		mRectFill.top = h2 - r5;
		mRectFill.right = w2 + r5;
		mRectFill.bottom = h2 + r5;
		mPaintFill.setStrokeWidth(r);
	}
}
