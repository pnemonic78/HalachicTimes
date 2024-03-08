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
import com.github.geonames.CountryRegion
import com.github.geonames.CountryRegion.Companion.VERTICES_COUNT
import com.github.geonames.CountryRegion.Companion.toFixedPoint
import com.github.geonames.dump.NameShapesLow
import com.github.geonames.dump.PathCountryInfo
import com.github.geonames.dump.PathShapesLow
import com.github.lang.times
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
import kotlin.math.sin
import kotlin.system.exitProcess

class CountryCanvas() : JComponent() {
    private var mainVertices: IntArray = intArrayOf()
    private var centroid: Point = Point()
    private var boundary: Polygon? = null
    private var boundaryMain: Polygon? = null
    private var envelope: Rectangle? = null
    private var tX = 0
    private var tY = 0
    private var specific: Point? = null
    private val boundaries = mutableListOf<Polygon>()

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
                ratioX *= 150
                ratioY *= 150
            }

            "US" -> {
                ratioX *= 10
                ratioY *= 10
            }

            "ZA" -> {
                ratioX *= 50
                ratioY *= 50
                // Dikholola near Brits.
                specific = Point(
                    (toFixedPoint(27.746222) * ratioX).toInt(),
                    (toFixedPoint(-25.411172) * ratioY).toInt()
                )
            }
        }
        val mainIndex = region.maxAreaIndex
        val boundaryMain = region.boundaries[mainIndex]
        val boundary = Polygon(
            boundaryMain.xpoints * ratioX,
            boundaryMain.ypoints * ratioY,
            boundaryMain.npoints
        )
        val envelope = boundary.bounds
        val centroid = region.centroids[mainIndex]
        val mainVertices = region.findMainVertices(boundaryMain, VERTICES_COUNT)
        val borderMain = Polygon()
        for (i in mainVertices) {
            if (i < 0) continue
            borderMain.addPoint(
                (boundaryMain.xpoints[i] * ratioX).toInt(),
                (boundaryMain.ypoints[i] * ratioY).toInt()
            )
        }
        boundaries.clear()
        region.boundaries.forEach { b ->
            boundaries.add(
                Polygon(
                    b.xpoints * ratioX,
                    b.ypoints * ratioY,
                    b.npoints
                )
            )
        }

        this.tX = 20 - envelope.x
        this.tY = 20 - envelope.y
        this.centroid = centroid.let {
            Point((it.x * ratioX).toInt(), (it.y * ratioY).toInt())
        }
        this.mainVertices = mainVertices
        this.boundary = boundary
        this.boundaryMain = borderMain
        this.envelope = envelope
        this.specific = specific
    }

    override fun paint(g: Graphics) {
        val boundary = boundary ?: return
        val boundaryMain = boundaryMain ?: return
        val cx = centroid.x
        val cy = centroid.y

        g.translate(tX, tY)
        paintRays(g, cx, cy)
        paintCentroid(g, cx, cy)
        boundaries.forEachIndexed { index, polygon ->
            paintBoundary(g, polygon, index)
        }
        envelope?.let { paintEnvelope(g, it) }
        paintMain(g, boundaryMain)
        paintMainVertices(g, boundary)
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

    private fun paintMainVertices(g: Graphics, poly: Polygon) {
        g.color = Color.blue
        for (i in mainVertices) {
            if (i < 0) continue
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
        private const val FIXEDINT_TO_DOUBLE = 1.0 / CountryRegion.FACTOR_TO_INT
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
