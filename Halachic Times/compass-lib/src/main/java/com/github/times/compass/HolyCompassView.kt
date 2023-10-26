package com.github.times.compass

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
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
class HolyCompassView : CompassView {
    private var holiest = 0f
    private var northToHoliest = 0f
    private var colorTarget = Color.TRANSPARENT
    private val paintHoliest = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
    }
    private var paintShadowHoliest = Paint(paintShadowTemplate)
    private val pathShadowHoliest = Path()
    private val pathArrowHoliest = Path()

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
    @SuppressLint("CustomViewStyleable")
    private fun init(context: Context) {
        ticks = false

        val prefs: CompassPreferences = if (context is BaseCompassActivity<*>) {
            context.compassPreferences
        } else {
            SimpleCompassPreferences(context)
        }
        val a = context.obtainStyledAttributes(prefs.compassTheme, R.styleable.CompassView)
        init(context, a)
        a.recycle()

        setHoliest(0f)
    }

    private fun init(context: Context, a: TypedArray) {
        val res = context.resources

        colorTarget = a.getColor(R.styleable.CompassView_compassColorTarget, colorTarget)
        paintHoliest.apply {
            strokeWidth = res.getDimension(R.dimen.holiest_thickness)
            color = colorTarget
        }
        paintShadowHoliest.apply {
            strokeWidth = res.getDimension(R.dimen.holiest_thickness) * 2f
            color = ResourcesCompat.getColor(res, R.color.compass_shadow_2, null)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w2 = widthHalf
        val h2 = heightHalf
        val r = radius
        val r9 = r * 0.9f
        val h2r9 = h2 - r9

        // Samsung has bug with tiny arcs.
        if (abs(northToHoliest) >= 0.1) {
            canvas.drawArc(rectFill, 270 - north, northToHoliest, false, paintFill)
        }
        canvas.rotate(holiest, w2, h2)
        canvas.drawLine(w2, h2, w2, h2r9, paintShadowHoliest)
        canvas.drawPath(pathShadowHoliest, paintShadowHoliest)
        canvas.drawLine(w2, h2, w2, h2r9, paintHoliest)
        canvas.drawPath(pathArrowHoliest, paintHoliest)

        canvas.drawCircle(w2, h2, radiusPivot, paintPivot)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val res = resources
        val boundary =
            res.getDimension(R.dimen.padding) + res.getDimension(R.dimen.circle_thickness)
        val w2 = w / 2f
        val h2 = h / 2f
        val r = max(0f, min(w2, h2) - boundary * 2f)
        val r1 = r * 0.1f
        val r6 = r * 0.6f
        val r85 = r * 0.85f
        val r9 = r * 0.9f
        val h2r6 = h2 - r6
        val h2r85 = h2 - r85
        val h2r9 = h2 - r9

        pathArrowHoliest.reset()
        pathArrowHoliest.moveTo(w2, h2r9)
        pathArrowHoliest.lineTo(w2 - r1, h2r6)
        pathArrowHoliest.quadTo(w2, h2r85, w2 + r1, h2r6)
        pathArrowHoliest.close()

        pathShadowHoliest.reset()
        pathShadowHoliest.set(pathArrowHoliest)
        pathShadowHoliest.close()
    }

    override fun setAzimuth(azimuth: Float) {
        super.setAzimuth(azimuth)
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
}