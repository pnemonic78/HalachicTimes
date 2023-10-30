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
import android.content.res.TypedArray
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
import kotlin.math.max
import kotlin.math.min

/**
 * Compass view.
 *
 * @author Moshe Waisberg
 */
open class CompassView : View {
    protected var north = 0f
        private set
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
    protected val paintFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }
    protected val paintPivot = Paint(Paint.DITHER_FLAG).apply {
        style = Paint.Style.FILL
    }
    private var paintTick = Paint(paintNE)
    protected val paintShadowTemplate = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val paintShadow = arrayOf(
        Paint(paintShadowTemplate),
        Paint(paintShadowTemplate),
        Paint(paintShadowTemplate)
    )
    protected val rectFill = RectF()
    protected var widthHalf = 0f
        private set
    protected var heightHalf = 0f
        private set
    protected var radius = 0f
        private set
    protected var radiusPivot = 0f
        private set
    private val pathArrowBig = Path()
    private val pathArrowSmall = Path()
    private val pathShadowBig = Array(SHADOWS_SIZE) { Path() }
    private val pathShadowSmall = Array(SHADOWS_SIZE) { Path() }
    private val rectFrameOuter = RectF()
    private val rectFrameInner = RectF()
    private var labelNorth: String = ""
    private var labelEast: String = ""
    private var labelSouth: String = ""
    private var labelWest: String = ""

    //TODO add "ticks" to styleable attributes.
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
        val prefs: CompassPreferences = if (context is BaseCompassActivity) {
            context.compassPreferences
        } else {
            SimpleCompassPreferences(context)
        }
        val a = context.obtainStyledAttributes(prefs.compassTheme, R.styleable.CompassView)
        init(context, a)
        a.recycle()

        setAzimuth(0f)
    }

    private fun init(context: Context, a: TypedArray) {
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
        colorArrowBg = a.getColor(R.styleable.CompassView_compassColorArrowBg, colorArrowBg)
        colorPivot = a.getColor(R.styleable.CompassView_compassColorPivot, colorPivot)
        colorPivotDark = a.getColor(R.styleable.CompassView_compassColorPivotDark, colorPivotDark)

        val res = context.resources

        labelNorth = res.getString(com.github.times.location.R.string.north)
        labelEast = res.getString(com.github.times.location.R.string.east)
        labelSouth = res.getString(com.github.times.location.R.string.south)
        labelWest = res.getString(com.github.times.location.R.string.west)

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
            canvas.drawPath(pathShadowBig[i], paintShadow[i])
        }
        // Shadow for North-East.
        canvas.rotate(45f, w2, h2)
        for (i in 0 until SHADOWS_SIZE) {
            canvas.drawPath(pathShadowSmall[i], paintShadow[i])
        }
        // Shadow for East.
        canvas.rotate(45f, w2, h2)
        for (i in 0 until SHADOWS_SIZE) {
            canvas.drawPath(pathShadowBig[i], paintShadow[i])
        }
        // Shadow for South-East.
        canvas.rotate(45f, w2, h2)
        for (i in 0 until SHADOWS_SIZE) {
            canvas.drawPath(pathShadowSmall[i], paintShadow[i])
        }
        // Shadow for South.
        canvas.rotate(45f, w2, h2)
        for (i in 0 until SHADOWS_SIZE) {
            canvas.drawPath(pathShadowBig[i], paintShadow[i])
        }
        // Shadow for South-West.
        canvas.rotate(45f, w2, h2)
        for (i in 0 until SHADOWS_SIZE) {
            canvas.drawPath(pathShadowSmall[i], paintShadow[i])
        }
        // Shadow for West.
        canvas.rotate(45f, w2, h2)
        for (i in 0 until SHADOWS_SIZE) {
            canvas.drawPath(pathShadowBig[i], paintShadow[i])
        }
        // Shadow for North-West.
        canvas.rotate(45f, w2, h2)
        for (i in 0 until SHADOWS_SIZE) {
            canvas.drawPath(pathShadowSmall[i], paintShadow[i])
        }
        canvas.rotate(45f, w2, h2)

        if (ticks) {
            // Ticks from (N to E)
            canvas.rotate(10f, w2, h2)
            for (i in 10 until 90 step 10) {
                canvas.drawLine(w2, h2r8, w2, h2r9, paintTick)
                canvas.rotate(10f, w2, h2)
            }
            // Ticks from (E to S)
            canvas.rotate(10f, w2, h2)
            for (i in 10 until 90 step 10) {
                canvas.drawLine(w2, h2r8, w2, h2r9, paintTick)
                canvas.rotate(10f, w2, h2)
            }
            // Ticks from (S to W)
            canvas.rotate(10f, w2, h2)
            for (i in 10 until 90 step 10) {
                canvas.drawLine(w2, h2r8, w2, h2r9, paintTick)
                canvas.rotate(10f, w2, h2)
            }
            // Ticks from (S to N)
            canvas.rotate(10f, w2, h2)
            for (i in 10 until 90 step 10) {
                canvas.drawLine(w2, h2r8, w2, h2r9, paintTick)
                canvas.rotate(10f, w2, h2)
            }
        }

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

        canvas.rotate(45f, w2, h2)
        canvas.drawCircle(w2, h2, radiusPivot, paintPivot)
    }

    /**
     * Set the azimuth to true North pole.
     *
     * @param azimuth the azimuth in degrees.
     */
    open fun setAzimuth(azimuth: Float) {
        north = -azimuth
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val res = resources
        val w2 = w / 2f
        val h2 = h / 2f
        val boundary =
            res.getDimension(R.dimen.padding) + res.getDimension(R.dimen.circle_thickness)
        val r = max(0f, min(w2, h2) - boundary * 2f)
        val r75 = r * 0.075f
        val r15 = r * 0.15f
        val r2 = r * 0.2f
        val r4 = r * 0.4f
        val r5 = r * 0.5f
        val r6 = r * 0.6f
        widthHalf = w2
        heightHalf = h2
        radius = r
        radiusPivot = r15
        if (r > 0) {
            paintCircle.shader = RadialGradient(
                w2,
                h2,
                r * 3,
                colorFace,
                colorGradient,
                Shader.TileMode.CLAMP
            )
            paintPivot.shader = RadialGradient(
                w2,
                h2,
                radiusPivot,
                colorPivot,
                colorPivotDark,
                Shader.TileMode.CLAMP
            )
        }
        paintNorth.shader = LinearGradient(
            w2,
            h2,
            w2,
            0f,
            colorNorth,
            colorNorthDark,
            Shader.TileMode.CLAMP
        )
        paintNorth.textSize = r2
        paintEast.shader = LinearGradient(
            w2,
            h2,
            w2,
            0f,
            colorEast,
            colorEastDark,
            Shader.TileMode.CLAMP
        )
        paintEast.textSize = r2
        paintSouth.shader = LinearGradient(
            w2,
            h2,
            w2,
            0f,
            colorSouth,
            colorSouthDark,
            Shader.TileMode.CLAMP
        )
        paintSouth.textSize = r2
        paintWest.shader = LinearGradient(
            w2,
            h2,
            w2,
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
        val gradientFrameOuter = SweepGradient(w2, h2, colorsFrameOuter, positionsFrameOuter)
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
        val gradientFrameInner = SweepGradient(w2, h2, colorsFrameInner, positionsFrameOuter)
        paintFrameInner.shader = gradientFrameInner
        pathArrowBig.reset()
        pathArrowBig.moveTo(w2 - r75, h2)
        pathArrowBig.lineTo(w2 + r75, h2)
        pathArrowBig.lineTo(w2, h2 - r6)
        pathArrowBig.close()
        pathArrowSmall.reset()
        pathArrowSmall.moveTo(w2 - r75, h2)
        pathArrowSmall.lineTo(w2 + r75, h2)
        pathArrowSmall.lineTo(w2, h2 - r4)
        pathArrowSmall.close()
        val shadowThickness = res.getDimension(R.dimen.shadow_thickness)
        var pathShadow: Path?
        var shadowOffset = shadowThickness

        for (i in 0 until SHADOWS_SIZE) {
            pathShadow = pathShadowBig[i]
            pathShadow.reset()
            pathShadow.moveTo(w2 - r75 - shadowOffset, h2 + shadowOffset)
            pathShadow.lineTo(w2 + r75 + shadowOffset, h2 + shadowOffset)
            pathShadow.lineTo(w2, h2 - r6 - shadowOffset - shadowOffset)
            pathShadow.close()

            pathShadow = pathShadowSmall[i]
            pathShadow.reset()
            pathShadow.moveTo(w2 - r75 - shadowOffset, h2 + shadowOffset)
            pathShadow.lineTo(w2 + r75 + shadowOffset, h2 + shadowOffset)
            pathShadow.lineTo(w2, h2 - r4 - shadowOffset - shadowOffset)
            pathShadow.close()

            shadowOffset += shadowThickness
        }
    }

    companion object {
        private const val SHADOWS_SIZE = 3
        private val positionsFrameOuter =
            floatArrayOf(0f, 0.125f, 0.250f, 0.375f, 0.500f, 0.625f, 0.750f, 0.875f, 1f)
    }
}