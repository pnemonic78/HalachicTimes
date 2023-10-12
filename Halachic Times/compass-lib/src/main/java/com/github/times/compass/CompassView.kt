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
package com.github.times.compass

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.github.times.compass.lib.R
import com.github.times.compass.preference.CompassPreferences
import com.github.times.compass.preference.SimpleCompassPreferences
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Compass view.
 *
 * @author Moshe Waisberg
 */
class CompassView : View {
    private var north = 0f
    private var holiest = Float.NaN
    private var northToHoliest = 0f
    private var colorFace = Color.TRANSPARENT
    private var colorGradient = Color.TRANSPARENT
    private var colorFrame = Color.TRANSPARENT
    private var colorFrameHighlight = Color.TRANSPARENT
    private var colorFrameShadow = Color.TRANSPARENT
    private var colorLabel = Color.TRANSPARENT
    private var colorLabelDark = Color.TRANSPARENT
    private var colorLabel2 = Color.TRANSPARENT
    private var colorNorth = Color.TRANSPARENT
    private var colorNorthDark = Color.TRANSPARENT
    private var colorSouth = Color.TRANSPARENT
    private var colorSouthDark = Color.TRANSPARENT
    private var colorEast = Color.TRANSPARENT
    private var colorEastDark = Color.TRANSPARENT
    private var colorWest = Color.TRANSPARENT
    private var colorWestDark = Color.TRANSPARENT
    private var colorTarget = Color.TRANSPARENT
    private var colorArrowBg = Color.TRANSPARENT
    private var colorPivot = Color.TRANSPARENT
    private var colorPivotDark = Color.TRANSPARENT
    private val paintCircle = Paint(Paint.DITHER_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val paintFrame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val paintFrameOuter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val paintFrameInner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val paintNorth = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        textAlign = Align.CENTER
    }
    private val paintEast = TextPaint(paintNorth)
    private val paintSouth = TextPaint(paintNorth)
    private val paintWest = TextPaint(paintNorth)
    private val paintNE = Paint(paintEast)
    private val paintHoliest = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
    }
    private val paintFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }
    private val paintPivot = Paint(Paint.DITHER_FLAG).apply {
        style = Paint.Style.FILL
    }
    private var paintTick = Paint(paintNE)
    private val paintShadowTemplate = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val paintShadow = arrayOf(
        Paint(paintShadowTemplate),
        Paint(paintShadowTemplate),
        Paint(paintShadowTemplate)
    )
    private var paintShadowHoliest = Paint(paintShadowTemplate)
    private val rectFill = RectF()
    private var widthHalf = 0f
    private var heightHalf = 0f
    private var radius = 0f
    private var radiusPivot = 0f
    private val pathArrowHoliest = Path()
    private val pathArrowBig = Path()
    private val pathArrowSmall = Path()
    private val pathShadowBig = arrayOfNulls<Path>(SHADOWS_SIZE)
    private val pathShadowSmall = arrayOfNulls<Path>(SHADOWS_SIZE)
    private val pathShadowHoliest = Path()
    private val rectFrameOuter = RectF()
    private val rectFrameInner = RectF()
    private var labelNorth: String = ""
    private var labelEast: String = ""
    private var labelSouth: String = ""
    private var labelWest: String = ""

    var ticks = true

    /**
     * Constructs a new compass view.
     *
     * @param context the context.
     */
    constructor(context: Context) : this(context, null)

    /**
     * Constructs a new compass view.
     *
     * @param context the context.
     * @param attrs   the attributes.
     */
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    /**
     * Constructs a new compass view.
     *
     * @param context  the context.
     * @param attrs    the attributes.
     * @param defStyle the default style.
     */
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    init {
        init(context)
    }

    /** Initialise.  */
    private fun init(context: Context) {
        val res = context.resources
        val prefs: CompassPreferences = if (context is BaseCompassActivity) {
            context.compassPreferences
        } else {
            SimpleCompassPreferences(context)
        }
        val a = context.obtainStyledAttributes(prefs.compassTheme, R.styleable.CompassView)
        colorFace = a.getColor(R.styleable.CompassView_compassColorFace, colorFace)
        colorGradient = a.getColor(R.styleable.CompassView_compassColorGradient, colorGradient)
        colorFrame = a.getColor(R.styleable.CompassView_compassColorFrame, colorFrame)
        colorFrameHighlight =
            a.getColor(R.styleable.CompassView_compassColorFrameHighlight, colorFrameHighlight)
        colorFrameShadow =
            a.getColor(R.styleable.CompassView_compassColorFrameShadow, colorFrameShadow)
        colorLabel = a.getColor(R.styleable.CompassView_compassColorLabel, colorLabel)
        colorLabelDark = a.getColor(R.styleable.CompassView_compassColorLabelDark, colorLabelDark)
        colorLabel2 = a.getColor(R.styleable.CompassView_compassColorLabel2, colorLabel2)
        colorNorth = a.getColor(R.styleable.CompassView_compassColorNorth, colorNorth)
        colorNorthDark = a.getColor(R.styleable.CompassView_compassColorNorthDark, colorNorthDark)
        colorSouth = a.getColor(R.styleable.CompassView_compassColorSouth, colorSouth)
        colorSouthDark = a.getColor(R.styleable.CompassView_compassColorSouthDark, colorSouthDark)
        colorEast = a.getColor(R.styleable.CompassView_compassColorEast, colorEast)
        colorEastDark = a.getColor(R.styleable.CompassView_compassColorEastDark, colorEastDark)
        colorWest = a.getColor(R.styleable.CompassView_compassColorWest, colorWest)
        colorWestDark = a.getColor(R.styleable.CompassView_compassColorWestDark, colorWestDark)
        colorTarget = a.getColor(R.styleable.CompassView_compassColorTarget, colorTarget)
        colorArrowBg = a.getColor(R.styleable.CompassView_compassColorArrowBg, colorArrowBg)
        colorPivot = a.getColor(R.styleable.CompassView_compassColorPivot, colorPivot)
        colorPivotDark = a.getColor(R.styleable.CompassView_compassColorPivotDark, colorPivotDark)
        a.recycle()

        labelNorth = context.getString(com.github.times.location.R.string.north)
        labelEast = context.getString(com.github.times.location.R.string.east)
        labelSouth = context.getString(com.github.times.location.R.string.south)
        labelWest = context.getString(com.github.times.location.R.string.west)

        paintCircle.apply {
            color = colorFace
        }
        paintFrame.apply {
            strokeWidth = res.getDimension(R.dimen.circle_thickness)
            color = colorFrame
        }
        paintFrameOuter.apply {
            strokeWidth = res.getDimension(R.dimen.circle_bevel_thickness)
            color = colorFrame
        }
        paintFrameInner.apply {
            strokeWidth = res.getDimension(R.dimen.circle_bevel_thickness)
            color = colorFrame
        }
        paintPivot.apply {
            color = colorPivot
        }
        paintNorth.apply {
            strokeWidth = res.getDimension(R.dimen.north_thickness)
            color = colorNorth
        }
        paintSouth.apply {
            strokeWidth = res.getDimension(R.dimen.south_thickness)
            color = colorSouth
        }
        paintEast.apply {
            strokeWidth = res.getDimension(R.dimen.label_thickness)
            color = colorEast
        }
        paintWest.apply {
            strokeWidth = res.getDimension(R.dimen.label_thickness)
            color = colorWest
        }
        paintNE.apply {
            strokeWidth = res.getDimension(R.dimen.label2_thickness)
            color = colorLabel2
        }
        paintTick.apply {
            strokeWidth = res.getDimension(R.dimen.label2_thickness)
            color = colorLabel2
        }
        paintHoliest.apply {
            strokeWidth = res.getDimension(R.dimen.holiest_thickness)
            color = colorTarget
        }
        paintFill.apply {
            color = colorArrowBg
        }
        paintShadow[0].apply {
            strokeWidth = res.getDimension(R.dimen.shadow_thickness)
            color = ResourcesCompat.getColor(res, R.color.compass_shadow_1, null)
        }
        paintShadow[1].apply {
            strokeWidth = res.getDimension(R.dimen.shadow_thickness)
            color = ResourcesCompat.getColor(res, R.color.compass_shadow_2, null)
        }
        paintShadow[2].apply {
            strokeWidth = res.getDimension(R.dimen.shadow_thickness)
            color = ResourcesCompat.getColor(res, R.color.compass_shadow_3, null)
        }
        paintShadowHoliest.apply {
            strokeWidth = res.getDimension(R.dimen.holiest_thickness) * 2f
            color = ResourcesCompat.getColor(res, R.color.compass_shadow_2, null)
        }

        setAzimuth(0f)
        setHoliest(0f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w2 = widthHalf
        val h2 = heightHalf
        val r = radius
        val r7 = r * 0.7f
        val r8 = r * 0.8f
        val r9 = r * 0.9f
        val h2r7 = h2 - r7
        val h2r8 = h2 - r8
        val h2r9 = h2 - r9
        canvas.drawCircle(w2, h2, r, paintCircle)
        canvas.drawCircle(w2, h2, r, paintFrame)
        canvas.drawArc(rectFrameOuter, 0f, 360f, false, paintFrameOuter)
        canvas.drawArc(rectFrameInner, 0f, 360f, false, paintFrameInner)
        canvas.rotate(north, w2, h2)

        // Shadow for North.
        for (i in 0 until SHADOWS_SIZE) {
            canvas.drawPath(pathShadowBig[i]!!, paintShadow[i])
        }
        // Shadow for North-East.
        canvas.rotate(45f, w2, h2)
        for (i in 0 until SHADOWS_SIZE) {
            canvas.drawPath(pathShadowSmall[i]!!, paintShadow[i])
        }
        // Shadow for East.
        canvas.rotate(45f, w2, h2)
        for (i in 0 until SHADOWS_SIZE) {
            canvas.drawPath(pathShadowBig[i]!!, paintShadow[i])
        }
        // Shadow for South-East.
        canvas.rotate(45f, w2, h2)
        for (i in 0 until SHADOWS_SIZE) {
            canvas.drawPath(pathShadowSmall[i]!!, paintShadow[i])
        }
        // Shadow for South.
        canvas.rotate(45f, w2, h2)
        for (i in 0 until SHADOWS_SIZE) {
            canvas.drawPath(pathShadowBig[i]!!, paintShadow[i])
        }
        // Shadow for South-West.
        canvas.rotate(45f, w2, h2)
        for (i in 0 until SHADOWS_SIZE) {
            canvas.drawPath(pathShadowSmall[i]!!, paintShadow[i])
        }
        // Shadow for West.
        canvas.rotate(45f, w2, h2)
        for (i in 0 until SHADOWS_SIZE) {
            canvas.drawPath(pathShadowBig[i]!!, paintShadow[i])
        }
        // Shadow for North-West.
        canvas.rotate(45f, w2, h2)
        for (i in 0 until SHADOWS_SIZE) {
            canvas.drawPath(pathShadowSmall[i]!!, paintShadow[i])
        }
        canvas.rotate(45f, w2, h2)

        // Arrow and label for North.
        canvas.drawPath(pathArrowBig, paintNorth)
        canvas.drawText(labelNorth, w2, h2r7, paintNorth)

        // Arrow for North-East.
        canvas.rotate(45f, w2, h2)
        canvas.drawPath(pathArrowSmall, paintNE)

        // Arrow and label for East.
        canvas.rotate(45f, w2, h2)
        canvas.drawPath(pathArrowBig, paintEast)
        canvas.drawText(labelEast, w2, h2r7, paintEast)

        // Arrow for South-East.
        canvas.rotate(45f, w2, h2)
        canvas.drawPath(pathArrowSmall, paintNE)

        // Arrow and label for South.
        canvas.rotate(45f, w2, h2)
        canvas.drawPath(pathArrowBig, paintSouth)
        canvas.drawText(labelSouth, w2, h2r7, paintSouth)

        // Arrow for South-West.
        canvas.rotate(45f, w2, h2)
        canvas.drawPath(pathArrowSmall, paintNE)

        // Arrow and label for West.
        canvas.rotate(45f, w2, h2)
        canvas.drawPath(pathArrowBig, paintWest)
        canvas.drawText(labelWest, w2, h2r7, paintWest)

        // Arrow for North-West.
        canvas.rotate(45f, w2, h2)
        canvas.drawPath(pathArrowSmall, paintNE)
        if (holiest.isNaN()) {
            canvas.rotate(45f, w2, h2)
        } else {
            // Samsung has bug with tiny arcs.
            if (abs(northToHoliest) >= 0.1) {
                canvas.drawArc(rectFill, 315 - north, northToHoliest, false, paintFill)
            }
            canvas.rotate(45 + holiest, w2, h2)
            canvas.drawLine(w2, h2, w2, h2r9, paintShadowHoliest)
            canvas.drawPath(pathShadowHoliest, paintShadowHoliest)
            canvas.drawLine(w2, h2, w2, h2r9, paintHoliest)
            canvas.drawPath(pathArrowHoliest, paintHoliest)
        }
        canvas.drawCircle(w2, h2, radiusPivot, paintPivot)
        if (ticks) {
            var i = 10
            while (i < 360) {
                canvas.rotate(10f, w2, h2)
                when (i) {
                    90, 180, 270 -> {
                        i += 10
                        continue
                    }
                }
                canvas.drawLine(w2, h2r8, w2, h2r9, paintTick)
                i += 10
            }
        }
    }

    /**
     * Set the azimuth to true North pole.
     *
     * @param azimuth the azimuth in degrees.
     */
    fun setAzimuth(azimuth: Float) {
        north = azimuth
        setNorthToHoliest()
    }

    /**
     * Set the bearing to some holy place.
     *
     * @param bearing the bearing in degrees.
     */
    fun setHoliest(bearing: Float) {
        holiest = bearing
        setNorthToHoliest()
    }

    private fun setNorthToHoliest() {
        northToHoliest = north + holiest
        if (northToHoliest > 180f) {
            northToHoliest -= 360f
        } else if (northToHoliest < -180f) {
            northToHoliest += 360f
        }
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val res = resources
        val w2 = w / 2
        val h2 = h / 2
        val boundary =
            res.getDimension(R.dimen.padding) + res.getDimension(R.dimen.circle_thickness)
        val r = max(0f, min(w2, h2) - boundary * 2f)
        val r75 = r * 0.075f
        val r1 = r * 0.1f
        val r15 = r * 0.15f
        val r2 = r * 0.2f
        val r4 = r * 0.4f
        val r5 = r * 0.5f
        val r6 = r * 0.6f
        val r85 = r * 0.85f
        val r9 = r * 0.9f
        val h2r6 = h2 - r6
        val h2r85 = h2 - r85
        val h2r9 = h2 - r9
        widthHalf = w2.toFloat()
        heightHalf = h2.toFloat()
        radius = r
        radiusPivot = r15
        if (r > 0) {
            paintCircle.shader = RadialGradient(
                w2.toFloat(),
                h2.toFloat(),
                r * 3,
                colorFace,
                colorGradient,
                Shader.TileMode.CLAMP
            )
            paintPivot.shader = RadialGradient(
                w2.toFloat(),
                h2.toFloat(),
                radiusPivot,
                colorPivot,
                colorPivotDark,
                Shader.TileMode.CLAMP
            )
        }
        paintNorth.shader = LinearGradient(
            w2.toFloat(),
            h2.toFloat(),
            w2.toFloat(),
            0f,
            colorNorth,
            colorNorthDark,
            Shader.TileMode.CLAMP
        )
        paintNorth.textSize = r2
        paintEast.shader = LinearGradient(
            w2.toFloat(),
            h2.toFloat(),
            w2.toFloat(),
            0f,
            colorEast,
            colorEastDark,
            Shader.TileMode.CLAMP
        )
        paintEast.textSize = r2
        paintSouth.shader = LinearGradient(
            w2.toFloat(),
            h2.toFloat(),
            w2.toFloat(),
            0f,
            colorSouth,
            colorSouthDark,
            Shader.TileMode.CLAMP
        )
        paintSouth.textSize = r2
        paintWest.shader = LinearGradient(
            w2.toFloat(),
            h2.toFloat(),
            w2.toFloat(),
            0f,
            colorWest,
            colorWestDark,
            Shader.TileMode.CLAMP
        )
        paintWest.textSize = r2
        val frameThickness = paintFrame.strokeWidth
        val frameInnerThickness = paintFrameInner.strokeWidth
        rectFill.set(w2 - r5, h2 - r5, w2 + r5, h2 + r5)
        paintFill.strokeWidth = r - frameThickness - frameInnerThickness
        val frameThicknessHalf = frameThickness / 2f
        rectFrameOuter.set(
            w2 - r - frameThicknessHalf,
            h2 - r - frameThicknessHalf,
            w2 + r + frameThicknessHalf,
            h2 + r + frameThicknessHalf
        )
        rectFrameInner.set(
            w2 - r + frameThicknessHalf,
            h2 - r + frameThicknessHalf,
            w2 + r - frameThicknessHalf,
            h2 + r - frameThicknessHalf
        )
        val colorFrame = this.colorFrame
        val colorFrameHighlight = this.colorFrameHighlight
        val colorFrameShadow = this.colorFrameShadow
        val colorsFrameOuter = intArrayOf(
            colorFrameShadow,
            colorFrameShadow,
            colorFrame,
            colorFrame,
            colorFrameHighlight,
            colorFrameHighlight,
            colorFrameHighlight,
            colorFrame,
            colorFrameShadow
        )
        val gradientFrameOuter =
            SweepGradient(w2.toFloat(), h2.toFloat(), colorsFrameOuter, positionsFrameOuter)
        paintFrameOuter.shader = gradientFrameOuter
        val colorsFrameInner = intArrayOf(
            colorFrameHighlight,
            colorFrameHighlight,
            colorFrame,
            colorFrame,
            colorFrameShadow,
            colorFrameShadow,
            colorFrameShadow,
            colorFrame,
            colorFrameHighlight
        )
        val gradientFrameInner =
            SweepGradient(w2.toFloat(), h2.toFloat(), colorsFrameInner, positionsFrameOuter)
        paintFrameInner.shader = gradientFrameInner
        pathArrowBig.reset()
        pathArrowBig.moveTo(w2 - r75, h2.toFloat())
        pathArrowBig.lineTo(w2 + r75, h2.toFloat())
        pathArrowBig.lineTo(w2.toFloat(), h2 - r6)
        pathArrowBig.close()
        pathArrowSmall.reset()
        pathArrowSmall.moveTo(w2 - r75, h2.toFloat())
        pathArrowSmall.lineTo(w2 + r75, h2.toFloat())
        pathArrowSmall.lineTo(w2.toFloat(), h2 - r4)
        pathArrowSmall.close()
        pathArrowHoliest.reset()
        pathArrowHoliest.moveTo(w2.toFloat(), h2r9)
        pathArrowHoliest.lineTo(w2 - r1, h2r6)
        pathArrowHoliest.quadTo(w2.toFloat(), h2r85, w2 + r1, h2r6)
        pathArrowHoliest.close()
        val shadowThickness = res.getDimension(R.dimen.shadow_thickness)
        var pathShadow: Path?
        var shadowOffset = shadowThickness
        var i = 0
        while (i < SHADOWS_SIZE) {
            pathShadow = pathShadowBig[i]
            if (pathShadow == null) {
                pathShadow = Path()
                pathShadowBig[i] = pathShadow
            }
            pathShadow.reset()
            pathShadow.moveTo(w2 - r75 - shadowOffset, h2 + shadowOffset)
            pathShadow.lineTo(w2 + r75 + shadowOffset, h2 + shadowOffset)
            pathShadow.lineTo(w2.toFloat(), h2 - r6 - shadowOffset - shadowOffset)
            pathShadow.close()
            pathShadow = pathShadowSmall[i]
            if (pathShadow == null) {
                pathShadow = Path()
                pathShadowSmall[i] = pathShadow
            }
            pathShadow.reset()
            pathShadow.moveTo(w2 - r75 - shadowOffset, h2 + shadowOffset)
            pathShadow.lineTo(w2 + r75 + shadowOffset, h2 + shadowOffset)
            pathShadow.lineTo(w2.toFloat(), h2 - r4 - shadowOffset - shadowOffset)
            pathShadow.close()
            i++
            shadowOffset += shadowThickness
        }
        pathShadowHoliest.reset()
        pathShadowHoliest.set(pathArrowHoliest)
        pathShadowHoliest.close()
    }

    companion object {
        private const val SHADOWS_SIZE = 3
        private val positionsFrameOuter =
            floatArrayOf(0f, 0.125f, 0.250f, 0.375f, 0.500f, 0.625f, 0.750f, 0.875f, 1f)
    }
}