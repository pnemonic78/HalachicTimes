package com.github.geonames

import com.github.math.times
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.LineString
import java.awt.Point
import java.awt.Polygon
import java.awt.geom.Point2D

fun Geometry.close(): Geometry {
    val coords = coordinates
    if (coords.isEmpty()) return this
    val first = coords[0]
    val last = coords[coords.lastIndex]
    if (first == last) return this
    val coordsNew = coords + first
    return factory.createPolygon(coordsNew)
}

fun com.vividsolutions.jts.geom.Point.toPoint2D(): Point2D {
    return Point2D.Double(x, y)
}

fun Geometry.toPolygon(scale: Double = 1.0): Polygon {
    val poly = Polygon()
    coordinates.forEach { coordinate ->
        poly.addPoint((coordinate.x * scale).toInt(), (coordinate.y * scale).toInt())
    }
    return poly
}

operator fun Geometry.times(value: Double): Geometry {
    val coords = coordinates
    val coordsNew = Array(coords.size) { i -> coords[i] * value }
    val seq = factory.coordinateSequenceFactory.create(coordsNew)
    if (this is LineString) {
        return factory.createLineString(seq)
    }
    if (this is com.vividsolutions.jts.geom.Point) {
        return factory.createPoint(seq)
    }
    return factory.createPolygon(seq)
}

operator fun Coordinate.times(value: Double): Coordinate {
    return Coordinate(x * value, y * value, z * value)
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

fun Polygon.addPoint(x: Double, y: Double) {
    addPoint(x.toInt(), y.toInt())
}
