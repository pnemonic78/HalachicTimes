package com.github.geonames

import com.github.math.times
import com.github.math.toDoubleArray
import com.vividsolutions.jts.geom.Geometry
import java.awt.Point
import java.awt.Polygon
import java.awt.Rectangle
import java.awt.geom.Point2D
import kotlin.math.PI
import kotlin.math.atan2

class CountryGeometry(
    val path: Polygon,
    val boundary: Polygon,
    val centroid: Point,
    val area: Double
) {
    val envelope: Rectangle = boundary.bounds
    val vertices: IntArray
    val pathVertices: Polygon

    init {
        vertices = findMainVertices(boundary, centroid, VERTICES_COUNT)
        pathVertices = setVertices(vertices)
    }

    constructor(geometry: Geometry) : this(
        path = geometry.toPolygon(),
        boundary = geometry.boundary.toPolygon(),
        centroid = (geometry.centroid.toPoint2D()).toPoint(),
        area = geometry.area
    )

    fun scale(scaleX: Double, scaleY: Double): CountryGeometry {
        return CountryGeometry(
            path = Polygon(
                path.xpoints * scaleX,
                path.ypoints * scaleY,
                path.npoints
            ),
            boundary = Polygon(
                boundary.xpoints * scaleX,
                boundary.ypoints * scaleY,
                boundary.npoints
            ),
            centroid = Point(
                (centroid.x * scaleX).toInt(),
                (centroid.y * scaleY).toInt()
            ),
            area = area * scaleX
        )
    }

    /**
     * Find the main vertices that represent the border.
     *
     * @param boundary the boundary with vertices.
     * @param centroid the centroid to rotate about.
     * @param vertexCount the number of vertices.
     * @return an array of indexes.
     */
    fun findMainVertices(boundary: Polygon, centroid: Point2D, vertexCount: Int): IntArray {
        val indexes = IntArray(vertexCount) { -1 }
        val n = boundary.npoints
        val xpoints = boundary.xpoints.toDoubleArray()
        val ypoints = boundary.ypoints.toDoubleArray()
        val cx = centroid.x
        val cy = centroid.y
        val angles = DoubleArray(n)
        var x: Double
        var y: Double

        for (i in 0 until n) {
            x = xpoints[i]
            y = ypoints[i]
            angles[i] = atan2(y - cy, x - cx) + PI
        }

        val sweepAngle = (2 * PI) / vertexCount
        var angleStart = 0.0
        var angleEnd: Double
        var angleRange: OpenEndRange<Double>
        var a: Double
        var d: Double
        var c = 0
        var farIndex: Int
        var farDist: Double

        for (v in 0 until vertexCount) {
            angleEnd = angleStart + sweepAngle
            angleRange = angleStart.rangeUntil(angleEnd)
            farDist = 0.0
            farIndex = -1

            for (i in 0 until n) {
                a = angles[i]
                if (a in angleRange) {
                    x = xpoints[i]
                    y = ypoints[i]
                    d = Point2D.distanceSq(cx, cy, x, y)
                    if (farDist < d) {
                        farDist = d
                        farIndex = i
                    }
                }
            }

            if (farIndex >= 0) indexes[c++] = farIndex
            angleStart += sweepAngle
        }
        if (c < vertexCount) {
            when (c) {
                0 -> {
                    val boundarySmall = Polygon()
                    boundarySmall.addPoint(xpoints[0] - CITY_BOUNDARY, ypoints[0] - CITY_BOUNDARY)
                    boundarySmall.addPoint(xpoints[0] + CITY_BOUNDARY, ypoints[0] + CITY_BOUNDARY)
                    boundarySmall.addPoint(xpoints[1] - CITY_BOUNDARY, ypoints[1] - CITY_BOUNDARY)
                    boundarySmall.addPoint(xpoints[1] + CITY_BOUNDARY, ypoints[1] + CITY_BOUNDARY)
                    return findMainVertices(boundarySmall, centroid, vertexCount)
                }

                1 -> {
                    val boundarySmall = Polygon()
                    boundarySmall.addPoint(xpoints[1] - CITY_BOUNDARY, ypoints[1] - CITY_BOUNDARY)
                    boundarySmall.addPoint(xpoints[1] + CITY_BOUNDARY, ypoints[1] + CITY_BOUNDARY)
                    return findMainVertices(boundarySmall, centroid, vertexCount)
                }
            }
        }

        return indexes.filter { it >= 0 }.toIntArray()
    }

    private fun setVertices(vertices: IntArray): Polygon {
        val p = Polygon()
        for (i in vertices) {
            if (i < 0) continue
            p.addPoint(
                path.xpoints[i],
                path.ypoints[i]
            )
        }
        return p
    }

    companion object {
        /** Factor to convert coordinate value to a fixed-point integer.  */
        internal const val FACTOR_TO_INT = 1e+5

        /** The number of main vertices per region border.  */
        const val VERTICES_COUNT = 16

        /**
         * Factor to convert coordinate value to a fixed-point integer for city
         * limits.
         */
        private const val CITY_BOUNDARY = 1e+4

        fun scale(geometry: Geometry, factor: Double) = CountryGeometry(
            path = geometry.toPolygon(factor),
            boundary = geometry.boundary.toPolygon(factor),
            centroid = (geometry.centroid.toPoint2D() * factor).toPoint(),
            area = geometry.area * factor
        )

        fun toFixedInt(geometry: Geometry) = scale(geometry, FACTOR_TO_INT)
    }
}