package com.github.geonames

import com.github.lang.times
import java.awt.Point
import java.awt.Polygon
import java.awt.geom.Point2D

fun com.vividsolutions.jts.geom.Point.toPoint2D(): Point2D {
    return Point2D.Double(x, y)
}

fun com.vividsolutions.jts.geom.Geometry.toPolygon(scale: Double = 1.0): Polygon {
    val poly = Polygon()
    coordinates.forEach { coordinate ->
        poly.addPoint((coordinate.x * scale).toInt(), (coordinate.y * scale).toInt())
    }
    return poly
}

operator fun Polygon.times(value: Int): Polygon {
    return Polygon(xpoints * value, ypoints * value, npoints)
}

operator fun Point.times(value: Int): Point {
    return Point(x * value, y * value)
}

operator fun Point2D.times(value: Double): Point2D {
    return Point2D.Double(x * value, y * value)
}

fun Point2D.toPoint(): Point {
    return Point(x.toInt(), y.toInt())
}
