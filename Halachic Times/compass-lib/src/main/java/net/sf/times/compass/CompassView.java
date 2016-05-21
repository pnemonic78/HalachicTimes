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
package net.sf.times.compass;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import net.sf.times.compass.lib.BuildConfig;
import net.sf.times.compass.lib.R;

import java.util.Random;

/**
 * Compass view.
 *
 * @author Moshe Waisberg
 */
public class CompassView extends View {

    private static final int SHADOWS = 3;

    private float north;
    private float holiest;
    private float northToHoliest;

    private Paint paintCircle;
    private Paint paintFrame;
    private Paint paintFrameOuter;
    private Paint paintFrameInner;
    private Paint paintNorth;
    private Paint paintEast;
    private Paint paintSouth;
    private Paint paintWest;
    private Paint paintNE;
    private Paint paintHoliest;
    private Paint paintFill;
    private Paint paintPivot;
    private Paint paintArrow;
    private final Paint[] paintShadow = new Paint[SHADOWS];
    private Paint paintShadowHoliest;
    private final RectF rectFill = new RectF();
    private float widthHalf;
    private float heightHalf;
    private float radius;
    private float radiusPivot;
    private final Path pathArrowHoliest = new Path();
    private final Path pathArrowBig = new Path();
    private final Path pathArrowSmall = new Path();
    private final Path[] pathShadowBig = new Path[SHADOWS];
    private final Path[] pathShadowSmall = new Path[SHADOWS];
    private final Path pathShadowHoliest = new Path();
    private final RectF rectFrameOuter = new RectF();
    private final RectF rectFrameInner = new RectF();

    private String labelNorth;
    private String labelEast;
    private String labelSouth;
    private String labelWest;

    /**
     * Constructs a new compass view.
     *
     * @param context
     *         the context.
     */
    public CompassView(Context context) {
        super(context);
        init(context);
    }

    /**
     * Constructs a new compass view.
     *
     * @param context
     *         the context.
     * @param attrs
     *         the attributes.
     */
    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * Constructs a new compass view.
     *
     * @param context
     *         the context.
     * @param attrs
     *         the attributes.
     * @param defStyle
     *         the default style.
     */
    public CompassView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /** Initialise. */
    private void init(Context context) {
        Resources res = getResources();

        labelNorth = context.getString(R.string.north);
        labelEast = context.getString(R.string.east);
        labelSouth = context.getString(R.string.south);
        labelWest = context.getString(R.string.west);

        paintCircle = new Paint(Paint.DITHER_FLAG);
        paintCircle.setStyle(Paint.Style.FILL);
        paintCircle.setColor(res.getColor(R.color.compass));

        paintFrame = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintFrame.setStyle(Paint.Style.STROKE);
        paintFrame.setStrokeWidth(res.getDimension(R.dimen.circle_thickness));
        paintFrame.setColor(res.getColor(R.color.compass_frame));

        paintFrameOuter = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintFrameOuter.setStyle(Paint.Style.STROKE);
        paintFrameOuter.setStrokeWidth(res.getDimension(R.dimen.circle_bevel_thickness));
        paintFrameOuter.setColor(res.getColor(R.color.compass_frame));

        paintFrameInner = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintFrameInner.setStyle(Paint.Style.STROKE);
        paintFrameInner.setStrokeWidth(res.getDimension(R.dimen.circle_bevel_thickness));
        paintFrameInner.setColor(res.getColor(R.color.compass_frame));

        paintPivot = new Paint(Paint.DITHER_FLAG);
        paintPivot.setStyle(Paint.Style.FILL);
        paintPivot.setColor(res.getColor(R.color.compass_pivot));

        paintArrow = new Paint(Paint.DITHER_FLAG);
        paintArrow.setStyle(Paint.Style.FILL);

        paintNorth = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paintNorth.setStyle(Paint.Style.FILL_AND_STROKE);
        paintNorth.setStrokeWidth(res.getDimension(R.dimen.north_thickness));
        paintNorth.setTextSize(res.getDimension(R.dimen.label_size));
        paintNorth.setTextAlign(Align.CENTER);
        paintNorth.setColor(res.getColor(R.color.compass_north));

        paintSouth = new TextPaint(paintNorth);
        paintSouth.setStrokeWidth(res.getDimension(R.dimen.south_thickness));
        paintSouth.setColor(res.getColor(R.color.compass_south));

        paintEast = new TextPaint(paintSouth);
        paintEast.setStrokeWidth(res.getDimension(R.dimen.label_thickness));
        paintEast.setColor(res.getColor(R.color.compass_east));

        paintWest = new TextPaint(paintEast);
        paintEast.setColor(res.getColor(R.color.compass_west));

        paintNE = new Paint(paintEast);
        paintNE.setStrokeWidth(res.getDimension(R.dimen.label2_thickness));
        paintNE.setColor(res.getColor(R.color.compass_label2));

        paintHoliest = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintHoliest.setStyle(Paint.Style.FILL_AND_STROKE);
        paintHoliest.setStrokeWidth(res.getDimension(R.dimen.holiest_thickness));
        paintHoliest.setColor(res.getColor(R.color.compass_holiest));

        paintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintFill.setColor(res.getColor(R.color.compass_arrow_bg));
        paintFill.setStyle(Paint.Style.STROKE);
        paintFill.setStrokeCap(Paint.Cap.BUTT);

        Paint paint;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(res.getDimension(R.dimen.shadow_thickness));
        paint.setColor(res.getColor(R.color.compass_shadow_1));
        paintShadow[0] = new Paint(paint);
        paint.setColor(res.getColor(R.color.compass_shadow_2));
        paintShadow[1] = new Paint(paint);
        paint.setColor(res.getColor(R.color.compass_shadow_3));
        paintShadow[2] = new Paint(paint);

        paintShadowHoliest = new Paint(paintShadow[1]);
        paintShadowHoliest.setStrokeWidth(res.getDimension(R.dimen.holiest_thickness) * 2f);

        setAzimuth(0f);
        setHoliest(0f);
        if (BuildConfig.DEBUG) {
            Random rnd = new Random();
            setAzimuth((float) (Math.PI * 2 * rnd.nextDouble()));
            setHoliest(rnd.nextInt(360));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float w2 = widthHalf;
        final float h2 = heightHalf;
        final float r = radius;
        final float r7 = r * 0.7f;
        final float r9 = r * 0.9f;
        final float h2r7 = h2 - r7;
        final float h2r9 = h2 - r9;

        canvas.drawCircle(w2, h2, r, paintCircle);
        canvas.drawCircle(w2, h2, r, paintFrame);
        canvas.drawArc(rectFrameOuter, 0f, 360f, false, paintFrameOuter);
        canvas.drawArc(rectFrameInner, 0f, 360f, false, paintFrameInner);

        canvas.rotate(north, w2, h2);

        // Shadow for North.
        for (int i = 0; i < SHADOWS; i++) {
            canvas.drawPath(pathShadowBig[i], paintShadow[i]);
        }
        // Shadow for North-East.
        canvas.rotate(45, w2, h2);
        for (int i = 0; i < SHADOWS; i++) {
            canvas.drawPath(pathShadowSmall[i], paintShadow[i]);
        }
        // Shadow for East.
        canvas.rotate(45, w2, h2);
        for (int i = 0; i < SHADOWS; i++) {
            canvas.drawPath(pathShadowBig[i], paintShadow[i]);
        }
        // Shadow for South-East.
        canvas.rotate(45, w2, h2);
        for (int i = 0; i < SHADOWS; i++) {
            canvas.drawPath(pathShadowSmall[i], paintShadow[i]);
        }
        // Shadow for South.
        canvas.rotate(45, w2, h2);
        for (int i = 0; i < SHADOWS; i++) {
            canvas.drawPath(pathShadowBig[i], paintShadow[i]);
        }
        // Shadow for South-West.
        canvas.rotate(45, w2, h2);
        for (int i = 0; i < SHADOWS; i++) {
            canvas.drawPath(pathShadowSmall[i], paintShadow[i]);
        }
        // Shadow for West.
        canvas.rotate(45, w2, h2);
        for (int i = 0; i < SHADOWS; i++) {
            canvas.drawPath(pathShadowBig[i], paintShadow[i]);
        }
        // Shadow for North-West.
        canvas.rotate(45, w2, h2);
        for (int i = 0; i < SHADOWS; i++) {
            canvas.drawPath(pathShadowSmall[i], paintShadow[i]);
        }
        canvas.rotate(45, w2, h2);

        // Arrow and label for North.
        canvas.drawPath(pathArrowBig, paintNorth);
        canvas.drawText(labelNorth, w2, h2r7, paintNorth);

        // Arrow for North-East.
        canvas.rotate(45, w2, h2);
        canvas.drawPath(pathArrowSmall, paintNE);

        // Arrow and label for East.
        canvas.rotate(45, w2, h2);
        canvas.drawPath(pathArrowBig, paintEast);
        canvas.drawText(labelEast, w2, h2r7, paintEast);

        // Arrow for South-East.
        canvas.rotate(45, w2, h2);
        canvas.drawPath(pathArrowSmall, paintNE);

        // Arrow and label for South.
        canvas.rotate(45, w2, h2);
        canvas.drawPath(pathArrowBig, paintSouth);
        canvas.drawText(labelSouth, w2, h2r7, paintSouth);

        // Arrow for South-West.
        canvas.rotate(45, w2, h2);
        canvas.drawPath(pathArrowSmall, paintNE);

        // Arrow and label for West.
        canvas.rotate(45, w2, h2);
        canvas.drawPath(pathArrowBig, paintWest);
        canvas.drawText(labelWest, w2, h2r7, paintWest);

        // Arrow for North-West.
        canvas.rotate(45, w2, h2);
        canvas.drawPath(pathArrowSmall, paintNE);

        canvas.drawArc(rectFill, 315 - north, northToHoliest, false, paintFill);

        canvas.rotate(45 + holiest, w2, h2);
        canvas.drawLine(w2, h2, w2, h2r9, paintShadowHoliest);
        canvas.drawPath(pathShadowHoliest, paintShadowHoliest);
        canvas.drawLine(w2, h2, w2, h2r9, paintHoliest);
        canvas.drawPath(pathArrowHoliest, paintHoliest);

        canvas.drawCircle(w2, h2, radiusPivot, paintPivot);
    }

    /**
     * Set the azimuth to magnetic North pole.
     *
     * @param bearing
     *         the bearing in radians.
     */
    public void setAzimuth(float bearing) {
        north = (float) Math.toDegrees(-bearing);

        northToHoliest = north + holiest;
        if (northToHoliest > 180f)
            northToHoliest -= 360f;
        else if (northToHoliest < -180f)
            northToHoliest += 360f;

        invalidate();
    }

    /**
     * Set the bearing to some holy place.
     *
     * @param bearing
     *         the bearing in degrees.
     */
    public void setHoliest(float bearing) {
        holiest = bearing;

        northToHoliest = north + holiest;
        if (northToHoliest > 180f)
            northToHoliest -= 360f;
        else if (northToHoliest < -180f)
            northToHoliest += 360f;

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Resources res = getResources();

        final int w2 = w / 2;
        final int h2 = h / 2;
        final float boundary = res.getDimension(R.dimen.padding) + res.getDimension(R.dimen.circle_thickness);
        final float r = Math.max(0, Math.min(w2, h2) - (boundary * 2f));
        final float r75 = r * 0.075f;
        final float r1 = r * 0.1f;
        final float r15 = r * 0.15f;
        final float r2 = r * 0.2f;
        final float r4 = r * 0.4f;
        final float r5 = r * 0.5f;
        final float r6 = r * 0.6f;
        final float r85 = r * 0.85f;
        final float r9 = r * 0.9f;
        final float h2r6 = h2 - r6;
        final float h2r85 = h2 - r85;
        final float h2r9 = h2 - r9;

        widthHalf = w2;
        heightHalf = h2;
        radius = r;
        radiusPivot = r15;

        if (r > 0) {
            RadialGradient gradientCircle = new RadialGradient(w2, h2, r * 3, res.getColor(R.color.compass), res.getColor(R.color.compass_gradient), Shader.TileMode.CLAMP);
            paintCircle.setShader(gradientCircle);

            RadialGradient gradientPivot = new RadialGradient(w2, h2, radiusPivot, res.getColor(R.color.compass_pivot), res.getColor(R.color.compass_pivot_dark), Shader.TileMode.CLAMP);
            paintPivot.setShader(gradientPivot);
        }

        LinearGradient gradientNorth = new LinearGradient(w2, h2, w2, 0, res.getColor(R.color.compass_north), res.getColor(R.color.compass_north_dark), Shader.TileMode.CLAMP);
        paintNorth.setShader(gradientNorth);

        LinearGradient gradientEast = new LinearGradient(w2, h2, w2, 0, res.getColor(R.color.compass_east), res.getColor(R.color.compass_east_dark), Shader.TileMode.CLAMP);
        paintEast.setShader(gradientEast);

        LinearGradient gradientSouth = new LinearGradient(w2, h2, w2, 0, res.getColor(R.color.compass_south), res.getColor(R.color.compass_south_dark), Shader.TileMode.CLAMP);
        paintSouth.setShader(gradientSouth);

        LinearGradient gradientWest = new LinearGradient(w2, h2, w2, 0, res.getColor(R.color.compass_west), res.getColor(R.color.compass_west_dark), Shader.TileMode.CLAMP);
        paintWest.setShader(gradientWest);

        final float sizeDirections = r2;
        paintNorth.setTextSize(sizeDirections);
        paintEast.setTextSize(sizeDirections);
        paintSouth.setTextSize(sizeDirections);
        paintWest.setTextSize(sizeDirections);

        float frameThickness = paintFrame.getStrokeWidth();
        float frameInnerThickness = paintFrameInner.getStrokeWidth();
        rectFill.left = w2 - r5;
        rectFill.top = h2 - r5;
        rectFill.right = w2 + r5;
        rectFill.bottom = h2 + r5;
        paintFill.setStrokeWidth(r - frameThickness - frameInnerThickness);

        float frameThicknessHalf = frameThickness / 2f;
        rectFrameOuter.left = w2 - r - frameThicknessHalf;
        rectFrameOuter.top = h2 - r - frameThicknessHalf;
        rectFrameOuter.right = w2 + r + frameThicknessHalf;
        rectFrameOuter.bottom = h2 + r + frameThicknessHalf;

        rectFrameInner.left = w2 - r + frameThicknessHalf;
        rectFrameInner.top = h2 - r + frameThicknessHalf;
        rectFrameInner.right = w2 + r - frameThicknessHalf;
        rectFrameInner.bottom = h2 + r - frameThicknessHalf;

        int colorFrame = res.getColor(R.color.compass_frame);
        int colorFrameHighlight = res.getColor(R.color.compass_frame_highlight);
        int colorFrameShadow = res.getColor(R.color.compass_frame_shadow);

        int[] colorsFrameOuter = {colorFrameShadow, colorFrameShadow, colorFrame, colorFrame, colorFrameHighlight, colorFrameHighlight, colorFrameHighlight, colorFrame, colorFrameShadow};
        float[] positionsFrameOuter = {0f, 0.125f, 0.250f, 0.375f, 0.500f, 0.625f, 0.750f, 0.875f, 1f};
        SweepGradient gradientFrameOuter = new SweepGradient(w2, h2, colorsFrameOuter, positionsFrameOuter);
        paintFrameOuter.setShader(gradientFrameOuter);

        int[] colorsFrameInner = {colorFrameHighlight, colorFrameHighlight, colorFrame, colorFrame, colorFrameShadow, colorFrameShadow, colorFrameShadow, colorFrame, colorFrameHighlight};
        float[] positionsFrameInner = positionsFrameOuter;
        SweepGradient gradientFrameInner = new SweepGradient(w2, h2, colorsFrameInner, positionsFrameInner);
        paintFrameInner.setShader(gradientFrameInner);

        pathArrowBig.reset();
        pathArrowBig.moveTo(w2 - r75, h2);
        pathArrowBig.lineTo(w2 + r75, h2);
        pathArrowBig.lineTo(w2, h2 - r6);
        pathArrowBig.close();

        pathArrowSmall.reset();
        pathArrowSmall.moveTo(w2 - r75, h2);
        pathArrowSmall.lineTo(w2 + r75, h2);
        pathArrowSmall.lineTo(w2, h2 - r4);
        pathArrowSmall.close();

        pathArrowHoliest.reset();
        pathArrowHoliest.moveTo(w2, h2r9);
        pathArrowHoliest.lineTo(w2 - r1, h2r6);
        pathArrowHoliest.quadTo(w2, h2r85, w2 + r1, h2r6);
        pathArrowHoliest.close();

        float shadowThickness = res.getDimension(R.dimen.shadow_thickness);
        Path pathShadow;
        float shadowOffset = shadowThickness;
        for (int i = 0; i < SHADOWS; i++, shadowOffset += shadowThickness) {
            pathShadow = pathShadowBig[i];
            if (pathShadow == null) {
                pathShadow = new Path();
                pathShadowBig[i] = pathShadow;
            }
            pathShadow.reset();
            pathShadow.moveTo(w2 - r75 - shadowOffset, h2 + shadowOffset);
            pathShadow.lineTo(w2 + r75 + shadowOffset, h2 + shadowOffset);
            pathShadow.lineTo(w2, h2 - r6 - shadowOffset - shadowOffset);
            pathShadow.close();

            pathShadow = pathShadowSmall[i];
            if (pathShadow == null) {
                pathShadow = new Path();
                pathShadowSmall[i] = pathShadow;
            }
            pathShadow.reset();
            pathShadow.moveTo(w2 - r75 - shadowOffset, h2 + shadowOffset);
            pathShadow.lineTo(w2 + r75 + shadowOffset, h2 + shadowOffset);
            pathShadow.lineTo(w2, h2 - r4 - shadowOffset - shadowOffset);
            pathShadow.close();
        }

        pathShadowHoliest.reset();
        pathShadowHoliest.set(pathArrowHoliest);
        pathShadowHoliest.close();
    }
}
