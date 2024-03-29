package com.github.math

import com.github.geonames.CountryRegion.Companion.toFixedPoint
import java.awt.Point
import java.awt.Polygon
import java.awt.geom.Line2D
import java.awt.geom.Point2D
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PolygonTests {
    @Test
    fun rectangle_contains() {
        val xpoints = intArrayOf(0, 0, 10, 10)
        val ypoints = intArrayOf(0, 10, 10, 0)
        val poly = Polygon(xpoints, ypoints, 4)
        val pointAbove = Point(5, 15)
        val pointBelow = Point(5, -5)
        val pointLeft = Point(-5, 5)
        val pointRight = Point(15, 5)
        val pointInside = Point(5, 5)
        val pointAbove1 = Point(0, 11)
        val pointInside1 = Point(1, 1)
//        val pointOnTop = Point(5, 10)
//        val pointOnRight = Point(10, 5)

        assertFalse("above", poly.inside(pointAbove))
        assertFalse("above1", poly.inside(pointAbove1))
        assertFalse("below", poly.inside(pointBelow))
        assertFalse("left", poly.inside(pointLeft))
        assertFalse("right", poly.inside(pointRight))
        assertTrue("inside", poly.inside(pointInside))
        assertTrue("inside1", poly.inside(pointInside1))
        //FIXME assertTrue("top", poly.inside(pointOnTop))
        //FIXME assertTrue("right", poly.inside(pointOnRight))
    }

    @Test
    fun china_not_contains_delhi() {
        val xpoints = intArrayOf(
            11233700,
            10013100,
            8112399,
            7876800,
            7449500,
            7445600,
            7380100,
            7353800,
            7350000,
            7393700,
            7988200,
            8303100,
            8689600,
            12232200,
            12326500,
            12351400,
            12360000,
            12515700,
            12561500,
            13476700,
            13477000,
            13391100,
            12191100,
            11986000,
            11235400,
            11233700
        )
        val ypoints = intArrayOf(
            1682500,
            2150700,
            3002000,
            3132600,
            3706500,
            3714300,
            3861399,
            3927800,
            3938200,
            4003300,
            4491100,
            4722300,
            4913500,
            5349900,
            5356100,
            5355600,
            5354900,
            5320200,
            5307700,
            4837100,
            4771500,
            4626300,
            2845100,
            2547500,
            1684199,
            1682500
        )
        val vertices = intArrayOf(5, 1, 0, 22, 19, 18, 11, 8)
        val point = Point2D.Double(toFixedPoint(77.23149), toFixedPoint(28.65195))

        val poly = Polygon(xpoints, ypoints, 21)
        assertFalse("China (convex hull)", poly.inside(point))

        val sizeV = vertices.size
        val xpointsV = IntArray(sizeV) { i -> xpoints[vertices[i]] }
        val ypointsV = IntArray(sizeV) { i -> ypoints[vertices[i]] }
        val region = Polygon(xpointsV, ypointsV, sizeV)
        assertFalse("China (country region)", region.contains(point))
    }

    @Test
    fun india_contains_delhi() {
        val xpoints = intArrayOf(
            9383400,
            7304800,
            7301900,
            7217100,
            6816300,
            6816800,
            6951300,
            6958700,
            7375000,
            7395300,
            7676000,
            7684800,
            7812099,
            9609200,
            9636800,
            9639700,
            9740100,
            9741500,
            9394700,
            9390200,
            9383400
        )
        val ypoints = intArrayOf(
            675800,
            826700,
            827200,
            1081700,
            2373900,
            2377100,
            2701000,
            2718300,
            3434400,
            3467100,
            3566299,
            3567199,
            3547999,
            2946000,
            2928300,
            2925300,
            2819200,
            2801500,
            695600,
            681300,
            675800
        )
        val vertices = intArrayOf(3, 2, 0, 16, 10, 9, 7, 5)
        val point = Point2D.Double(toFixedPoint(77.23149), toFixedPoint(28.65195))

        val poly = Polygon(xpoints, ypoints, 21)
        assertTrue("Delhi (convex hull)", poly.inside(point))

        val sizeV = vertices.size
        val xpointsV = IntArray(sizeV) { i -> xpoints[vertices[i]] }
        val ypointsV = IntArray(sizeV) { i -> ypoints[vertices[i]] }
        val region = Polygon(xpointsV, ypointsV, sizeV)
        assertTrue("Delhi (country region)", region.contains(point))
    }

    @Test
    fun israel_contains_jerusalem() {
        val xpoints = intArrayOf(
            3426400, 3522600, 3547600, 3555899, 3557400, 3540300, 3522399
        )
        val ypoints = intArrayOf(
            3122800, 3137300, 3149200, 3176200, 3221200, 3250200, 3255200
        )
        val vertices = intArrayOf(0, 1, 2, 3, 4, 5, 6)
        val point = Point2D.Double(toFixedPoint(35.2353), toFixedPoint(31.778))

        val poly = Polygon(xpoints, ypoints, 7)
        assertTrue("Israel (convex hull)", poly.inside(point))

        val sizeV = vertices.size
        val xpointsV = IntArray(sizeV) { i -> xpoints[vertices[i]] }
        val ypointsV = IntArray(sizeV) { i -> ypoints[vertices[i]] }
        val region = Polygon(xpointsV, ypointsV, sizeV)
        assertTrue("Israel (country region)", region.contains(point))
    }

    /**
     * Is the point inside a convex hull?
     * Does the lines segment p-centroid intersect any path line segments?
     */
    private fun Polygon.inside(point: Point2D): Boolean {
        val npoints = this.npoints
        val xpoints = this.xpoints
        val ypoints = this.ypoints
        val centre = centre(xpoints, ypoints, npoints)
        val segment = Line2D.Double(centre.x, centre.y, point.x, point.y)

        var j = npoints - 1
        for (i in 0 until npoints) {
            if (segment.intersectsLine(
                    xpoints[j].toDouble(),
                    ypoints[j].toDouble(),
                    xpoints[i].toDouble(),
                    ypoints[i].toDouble()
                )
            ) {
                return false
            }
            j = i
        }

        return true
    }

    private fun centre(xpoints: IntArray, ypoints: IntArray, npoints: Int): Point2D {
        var x = 0.0
        var y = 0.0
        for (i in 0 until npoints) {
            x += xpoints[i]
            y += ypoints[i]
        }
        return Point2D.Double(x / npoints, y / npoints)
    }
}
