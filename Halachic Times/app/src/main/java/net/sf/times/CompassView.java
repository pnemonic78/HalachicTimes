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
import android.graphics.Path;
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
	private static final float HOLIEST_THICKNESS = 4;
	private static final float NORTH_THICKNESS = 2;
	private static final float SOUTH_THICKNESS = 2;
	private static final float LABEL_THICKNESS = 1;
	private static final float LABEL2_THICKNESS = 1;
	private static final float PIVOT_RADIUS = 10;

	private float north;
	private float holiest;

	private Paint paintCircle;
	private Paint paintFrame;
	private Paint paintNorth;
	private Paint paintEast;
	private Paint paintSouth;
	private Paint paintWest;
	private Paint paintNE;
	private Paint paintHoliest;
	private Paint paintFill;
	private Paint paintPivot;
	private final RectF rectFill = new RectF();
	private float widthHalf;
	private float heightHalf;
	private float radius;
	private final Path pathArrowHoliest = new Path();
	private float density;

	private String labelNorth;
	private String labelEast;
	private String labelSouth;
	private String labelWest;

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

		density = res.getDisplayMetrics().density;

		labelNorth = context.getString(R.string.north);
		labelEast = context.getString(R.string.east);
		labelSouth = context.getString(R.string.south);
		labelWest = context.getString(R.string.west);

		paintCircle = new Paint(Paint.DITHER_FLAG);
		paintCircle.setStyle(Paint.Style.FILL);
		paintCircle.setColor(res.getColor(R.color.compass));

		paintFrame = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintFrame.setStyle(Paint.Style.STROKE);
		paintFrame.setStrokeWidth(CIRCLE_THICKNESS * density);
		paintFrame.setColor(res.getColor(R.color.compass_frame));

		paintPivot = new Paint(Paint.DITHER_FLAG);
		paintPivot.setStyle(Paint.Style.FILL);
		paintPivot.setColor(res.getColor(R.color.compass_pivot));

		paintNorth = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		paintNorth.setStyle(Paint.Style.FILL_AND_STROKE);
		paintNorth.setStrokeWidth(NORTH_THICKNESS * density);
		paintNorth.setTextSize(18);
		paintNorth.setTextAlign(Align.CENTER);
		paintNorth.setColor(res.getColor(R.color.compass_north));

		paintSouth = new TextPaint(paintNorth);
		paintSouth.setStrokeWidth(SOUTH_THICKNESS * density);
		paintSouth.setColor(res.getColor(R.color.compass_south));

		paintEast = new TextPaint(paintSouth);
		paintEast.setStrokeWidth(LABEL_THICKNESS * density);
		paintEast.setColor(res.getColor(R.color.compass_label));

		paintWest = new TextPaint(paintEast);

		paintNE = new Paint(paintEast);
		paintNE.setStrokeWidth(LABEL2_THICKNESS * density);
		paintNE.setColor(res.getColor(R.color.compass_label2));

		paintHoliest = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintHoliest.setStyle(Paint.Style.FILL_AND_STROKE);
		paintHoliest.setStrokeWidth(HOLIEST_THICKNESS * density);
		paintHoliest.setColor(res.getColor(R.color.compass_arrow));

		paintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintFill.setColor(res.getColor(R.color.compass_arrow_bg));
		paintFill.setStyle(Paint.Style.STROKE);
		paintFill.setStrokeCap(Paint.Cap.BUTT);

		setAzimuth(0f);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		final float w2 = widthHalf;
		final float h2 = heightHalf;
		final float r = radius;
		final float r1 = r * 0.1f;
		final float r2 = r * 0.2f;
		final float r5 = r * 0.5f;
		final float r7 = r * 0.7f;
		final float r8 = r * 0.8f;
		final float r9 = r * 0.9f;
		final float h2r5 = h2 - r5;
		final float h2r7 = h2 - r7;
		final float h2r8 = h2 - r8;
		final float h2r9 = h2 - r9;

		canvas.drawCircle(w2, h2, r, paintCircle);

		float sweepAngle = north + holiest;
		if (sweepAngle > 180f)
			sweepAngle -= 360f;
		canvas.drawArc(rectFill, -90f, sweepAngle, false, paintFill);

		canvas.rotate(north, w2, h2);
		canvas.drawLine(w2, h2r7, w2, h2r9, paintNorth);
		canvas.drawText(labelNorth, w2, h2r5, paintNorth);

		canvas.rotate(45, w2, h2);
		canvas.drawLine(w2, h2r8, w2, h2r9, paintNE);

		canvas.rotate(45, w2, h2);
		canvas.drawLine(w2, h2r7, w2, h2r9, paintEast);
		canvas.drawText(labelEast, w2, h2r5, paintEast);

		canvas.rotate(45, w2, h2);
		canvas.drawLine(w2, h2r8, w2, h2r9, paintNE);

		canvas.rotate(45, w2, h2);
		canvas.drawLine(w2, h2r7, w2, h2r9, paintSouth);
		canvas.drawText(labelSouth, w2, h2r5, paintSouth);

		canvas.rotate(45, w2, h2);
		canvas.drawLine(w2, h2r8, w2, h2r9, paintNE);

		canvas.rotate(45, w2, h2);
		canvas.drawLine(w2, h2r7, w2, h2r9, paintWest);
		canvas.drawText(labelWest, w2, h2r5, paintWest);

		canvas.rotate(45, w2, h2);
		canvas.drawLine(w2, h2r8, w2, h2r9, paintNE);

		canvas.rotate(45 + holiest, w2, h2);
		canvas.drawLine(w2, h2, w2, h2 - r, paintHoliest);
		pathArrowHoliest.reset();
		pathArrowHoliest.moveTo(w2, h2 - r);
		pathArrowHoliest.quadTo(w2, h2r9, w2 - r1, h2r7);
		pathArrowHoliest.quadTo(w2, h2r9, w2 + r1, h2r7);
		pathArrowHoliest.quadTo(w2, h2r9, w2, h2 - r);
		pathArrowHoliest.close();
		canvas.drawPath(pathArrowHoliest, paintHoliest);

		canvas.drawCircle(w2, h2, r, paintFrame);

		canvas.drawCircle(w2, h2, PIVOT_RADIUS * density, paintPivot);
	}

	/**
	 * Set the azimuth to magnetic North pole.
	 *
	 * @param bearing
	 * 		the bearing in radians.
	 */
	public void setAzimuth(float bearing) {
		north = (float) Math.toDegrees(-bearing);
		invalidate();
	}

	/**
	 * Set the bearing to the Holy of Holies.
	 *
	 * @param bearing
	 * 		the bearing in degrees.
	 */
	public void setHoliest(float bearing) {
		holiest = bearing;
		invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		final int w2 = w / 2;
		final int h2 = h / 2;
		final float r = Math.max(0, Math.min(w2, h2) - ((PADDING - CIRCLE_THICKNESS) * density));
		final float r2 = r * 0.2f;
		final float r5 = r * 0.5f;

		widthHalf = w2;
		heightHalf = h2;
		radius = r;

		if (r > 0) {
			Resources res = getResources();
			RadialGradient gradientCircle = new RadialGradient(w2, h2, r * 3, res.getColor(R.color.compass), res.getColor(R.color.compass_gradient), Shader.TileMode.CLAMP);
			paintCircle.setShader(gradientCircle);

			RadialGradient gradientPivot = new RadialGradient(w2, h2, PIVOT_RADIUS * density, res.getColor(R.color.compass_pivot), res.getColor(R.color.compass_frame), Shader.TileMode.CLAMP);
			paintPivot.setShader(gradientPivot);
		}

		final float sizeDirections = r2;
		paintNorth.setTextSize(sizeDirections);
		paintEast.setTextSize(sizeDirections);
		paintSouth.setTextSize(sizeDirections);
		paintWest.setTextSize(sizeDirections);

		rectFill.left = w2 - r5;
		rectFill.top = h2 - r5;
		rectFill.right = w2 + r5;
		rectFill.bottom = h2 + r5;
		paintFill.setStrokeWidth(r);
	}
}
