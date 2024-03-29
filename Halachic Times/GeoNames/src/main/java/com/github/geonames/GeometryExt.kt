package com.github.geonames

import com.github.math.sqr
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import java.awt.Rectangle
import java.awt.geom.Rectangle2D

fun Geometry.join(g1: Geometry): Geometry {
    val c0 = this.coordinates
    val l0 = c0.size
    val c1 = g1.coordinates
    val l1 = c1.size
    if (l1 <= 2) return this
    if (l0 <= 2) return g1
    val c = if (l0 <= l1) {
        join(c0, c1)
    } else {
        join(c1, c0)
    }
    return this.factory.createPolygon(c)
}

fun join(c0: Array<Coordinate>, c1: Array<Coordinate>): Array<Coordinate> {
    val l0 = c0.size
    val l1 = c1.size
    assert(l0 <= l1)
    // 1. Find the closest point.
    var minDist = Double.MAX_VALUE
    var minDistIndexC0 = -1
    var minDistIndexC1 = -1
    for (i in 0 until l0) {
        val cI = c0[i]
        for (j in 0 until l1) {
            val cJ = c1[j]
            val d = sqr(cI.x - cJ.x) + sqr(cI.y - cJ.y)
            if (d < minDist) {
                minDist = d
                minDistIndexC0 = i
                minDistIndexC1 = j
            }
        }
    }

    // 2. Delete their edge (line segment).
    // 3. Insert the points with their nearest neighbours.
    val c = ArrayList<Coordinate>(l0 + l1)
    for (i in 0..minDistIndexC0) {
        c.add(c0[i])
    }
    for (i in minDistIndexC1 until l1) {
        c.add(c1[i])
    }
    var i1 = 0
    if (c1[0] == c1[l1 - 1]) i1++
    for (i in i1 until minDistIndexC1) {
        c.add(c1[i])
    }
    for (i in (minDistIndexC0 + 1) until l0) {
        c.add(c0[i])
    }

    return c.toTypedArray()
}

operator fun Rectangle2D.times(value: Double): Rectangle2D {
    val w2 = width * value
    val h2 = height * value
    val dw = w2 - width
    val dh = h2 - height
    return Rectangle2D.Double(x - dw, y - dh, w2, h2)
}

operator fun Rectangle2D.Float.times(value: Float): Rectangle2D {
    val w2 = width * value
    val h2 = height * value
    val dw = w2 - width
    val dh = h2 - height
    return Rectangle2D.Float(x - dw, y - dh, w2, h2)
}

operator fun Rectangle.times(value: Double): Rectangle {
    val w2 = width * value
    val h2 = height * value
    val dw = w2 - width
    val dh = h2 - height
    return Rectangle((x - dw).toInt(), (y - dh).toInt(), w2.toInt(), h2.toInt())
}
