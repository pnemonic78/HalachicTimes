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
package com.github.geonames.ui

import com.github.geonames.Countries
import com.github.geonames.CountryGeometry
import com.github.geonames.CountryGeometry.Companion.VERTICES_COUNT
import com.github.geonames.CountryRegion
import com.github.geonames.CountryRegion.Companion.toFixedPoint
import com.github.geonames.dump.NameShapesLow
import com.github.geonames.dump.PathCountryInfo
import com.github.geonames.dump.PathShapesLow
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Point
import java.awt.Polygon
import java.awt.Rectangle
import java.awt.Toolkit
import java.io.File
import java.io.IOException
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.WindowConstants
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.system.exitProcess

class CountryCanvas() : JComponent() {
    private var tX = 0
    private var tY = 0
    private var specific: Point? = null
    private val geometries = mutableListOf<CountryGeometry>()
    private var envelope: Rectangle? = null

    constructor(region: CountryRegion) : this() {
        setRegion(region)
    }

    fun setRegion(region: CountryRegion) {
        var ratioX = FIXEDINT_TO_DOUBLE
        var ratioY = -ratioX
        var specific: Point? = null

        when (region.countryCode) {
            "BW" -> {
                // Dikholola near Brits.
                specific = Point(
                    (toFixedPoint(27.746222) * ratioX).toInt(),
                    (toFixedPoint(-25.411172) * ratioY).toInt()
                )
            }

            "IL" -> {
                ratioX *= 15
                ratioY *= 15
            }

            "US" -> {
                ratioX *= 1
                ratioY *= 1
            }

            "ZA" -> {
                ratioX *= 5
                ratioY *= 5
                // Dikholola near Brits.
                specific = Point(
                    (toFixedPoint(27.746222) * ratioX).toInt(),
                    (toFixedPoint(-25.411172) * ratioY).toInt()
                )
            }
        }

        geometries.clear()
        geometries += region.geometries.map {
            it.scale(ratioX, ratioY)
        }

        val mainIndex = region.maxAreaIndex
        val mainGeometry = geometries[mainIndex]
        val envelope = mainGeometry.envelope
        var envelopeAll = envelope
        geometries.forEach { geo ->
            envelopeAll = Rectangle(
                min(envelopeAll.x, geo.envelope.x),
                min(envelopeAll.y, geo.envelope.y),
                max(envelopeAll.width, geo.envelope.width),
                max(envelopeAll.height, geo.envelope.height)
            )
        }

        this.tX = 20 - envelopeAll.x
        this.tY = 20 - envelopeAll.y
        this.envelope = envelopeAll
        this.specific = specific
    }

    override fun paint(g: Graphics) {
        g.translate(tX, tY)
        envelope?.let { paintEnvelope(g, it) }
        geometries.forEachIndexed { index, geo ->
            paintGeometry(g, geo, index)
        }
        specific?.let { paintSpecific(g, it) }
        g.translate(-tX, -tY)
    }

    private fun paintBoundary(g: Graphics, poly: Polygon, index: Int) {
        g.color = boundaryColors[index % boundaryColors.size]
        g.drawPolygon(poly)
    }

    private fun paintMain(g: Graphics, poly: Polygon) {
        g.color = Color.magenta
        g.drawPolygon(poly)
    }

    private fun paintVertices(g: Graphics, poly: Polygon) {
        g.color = Color.blue
        paintBoundary(g, poly, 0)
        val npoints = poly.npoints
        for (i in 0 until npoints) {
            g.drawOval(poly.xpoints[i] - 5, poly.ypoints[i] - 5, 10, 10)
        }
    }

    private fun paintEnvelope(g: Graphics, rect: Rectangle) {
        g.color = Color.yellow
        g.drawRect(rect.x, rect.y, rect.width, rect.height)
    }

    private fun paintCentroid(g: Graphics, cx: Int, cy: Int) {
        g.color = Color.blue
        g.drawOval(cx - 5, cy - 5, 10, 10)
    }

    private fun paintRays(g: Graphics, cx: Int, cy: Int) {
        val sweepAngle = (2 * PI) / VERTICES_COUNT
        var angleStart = 0.0
        var x2: Int
        var y2: Int
        val r = CANVAS_SIZE

        g.color = Color.cyan
        for (v in 0 until VERTICES_COUNT) {
            x2 = cx + (r * cos(angleStart)).toInt()
            y2 = cy + (r * sin(angleStart)).toInt()
            g.drawLine(cx, cy, x2, y2)
            angleStart += sweepAngle
        }
    }

    private fun paintSpecific(g: Graphics, specific: Point) {
        g.color = Color.red
        g.drawOval(specific.x - 5, specific.y - 5, 10, 10)
    }

    private fun paintGeometry(g: Graphics, countryGeometry: CountryGeometry, index: Int) {
        val boundary = countryGeometry.boundary
        val cx = countryGeometry.centroid.x
        val cy = countryGeometry.centroid.y

//        paintRays(g, cx, cy)
        paintCentroid(g, cx, cy)
        paintBoundary(g, boundary, index)
        paintEnvelope(g, countryGeometry.envelope)
        paintMain(g, boundary)
        paintVertices(g, countryGeometry.pathVertices)
    }

    fun showFrame() {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val canvas = this
        canvas.preferredSize = Dimension(CANVAS_SIZE, CANVAS_SIZE)
        JFrame().apply {
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            setBounds(0, 0, screenSize.width, screenSize.height)
            contentPane.add(JScrollPane(canvas))
            isVisible = true
        }
    }

    companion object {
        private const val FIXEDINT_TO_DOUBLE = 10 / CountryRegion.FACTOR_TO_INT
        private const val CANVAS_SIZE = 2500

        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            var countryInfoPath = PathCountryInfo
            var shapesPath = PathShapesLow
            if (args.isEmpty()) {
                println("Country code required")
                exitProcess(1)
            }
            val countryCode = args[0]
            if (args.size > 1) {
                countryInfoPath = args[1]
                if (args.size > 2) {
                    shapesPath = args[2]
                }
            }
            val canvas =
                getCanvas(countryCode, File(countryInfoPath), File(shapesPath), NameShapesLow)
            if (canvas == null) {
                System.err.println("No country region!")
                return
            }
            canvas.showFrame()
        }

        @Throws(IOException::class)
        private fun getCanvas(
            countryCode: String,
            countryInfoFile: File,
            shapesFile: File,
            shapesZipName: String? = null
        ): CountryCanvas? {
            val countries = Countries()
            val names = countries.loadInfo(countryInfoFile).filter { it.iso == countryCode }
            if (names.isEmpty()) return null
            val shapes = countries.loadShapes(shapesFile, shapesZipName)
            val regions = countries.toRegions(names, shapes)
            return regions.firstOrNull()?.let { CountryCanvas(it) }
        }

        private val boundaryColors = arrayOf(
            Color.black,
            Color.blue,
            Color.gray,
            Color.green,
            Color.darkGray,
            Color.cyan,
            Color.lightGray,
            Color.orange,
            Color.yellow,
            Color.magenta,
            Color.red
        )
    }
}
