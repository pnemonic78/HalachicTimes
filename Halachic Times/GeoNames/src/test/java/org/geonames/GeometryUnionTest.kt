package org.geonames

import com.github.geonames.join
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import java.awt.Rectangle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeometryUnionTest {
    @Test
    fun contains_disjoint() {
        val r1 = Rectangle(0, 0, 10, 10)
        val r2 = Rectangle(15, 0, 10, 10)
        assertFalse(r1.contains(r2))
        assertFalse(r2.contains(r1))
        assertFalse(r1.intersects(r2))
        assertFalse(r2.intersects(r1))
    }

    @Test
    fun contains_inside() {
        val r1 = Rectangle(0, 0, 10, 10)
        val r2 = Rectangle(2, 2, 5, 5)
        assertTrue(r1.contains(r2))
        assertFalse(r2.contains(r1))
        assertTrue(r1.intersects(r2))
        assertTrue(r2.intersects(r1))
    }

    @Test
    fun contains_overlap_corner1() {
        val r1 = Rectangle(0, 0, 10, 10)
        val r2 = Rectangle(5, 5, 10, 10)
        assertFalse(r1.contains(r2))
        assertFalse(r2.contains(r1))
        assertTrue(r1.intersects(r2))
        assertTrue(r2.intersects(r1))
    }

    @Test
    fun contains_overlap_corner2() {
        val r1 = Rectangle(0, 0, 10, 10)
        val r2 = Rectangle(-5, -5, 10, 10)
        assertFalse(r1.contains(r2))
        assertFalse(r2.contains(r1))
        assertTrue(r1.intersects(r2))
        assertTrue(r2.intersects(r1))
    }

    @Test
    fun contains_overlap_corner3() {
        val r1 = Rectangle(0, 0, 10, 10)
        val r2 = Rectangle(5, -5, 10, 10)
        assertFalse(r1.contains(r2))
        assertFalse(r2.contains(r1))
        assertTrue(r1.intersects(r2))
        assertTrue(r2.intersects(r1))
    }

    @Test
    fun union_two_disjoint_rectangles() {
        val r0_c0 = Coordinate(0.0, 0.0, 0.0)
        val r0_c1 = Coordinate(0.0, 10.0, 0.0)
        val r0_c2 = Coordinate(10.0, 10.0, 0.0)
        val r0_c3 = Coordinate(10.0, 0.0, 0.0)
        val r0 = arrayOf(r0_c0, r0_c1, r0_c2, r0_c3, r0_c0)
        val g0 = GeometryFactory().createPolygon(r0)
        assertNotNull(g0)

        val r1_c0 = Coordinate(15.0, 5.0, 0.0)
        val r1_c1 = Coordinate(15.0, 10.0, 0.0)
        val r1_c2 = Coordinate(20.0, 10.0, 0.0)
        val r1_c3 = Coordinate(20.0, 5.0, 0.0)
        val r1 = arrayOf(r1_c0, r1_c1, r1_c2, r1_c3, r1_c0)
        val g1 = GeometryFactory().createPolygon(r1)
        assertNotNull(g1)

        val u = g0.join(g1)
        assertNotNull(u)
        val c = u.coordinates
        assertNotNull(c)
        assertEquals(6, c.size)
        assertEquals(c[0], r0_c0)
        assertEquals(c[1], r0_c1)
        assertEquals(c[2], r1_c2)
        assertEquals(c[3], r1_c3)
        assertEquals(c[4], r0_c3)
        assertEquals(c[5], r0_c0)
    }

    @Test
    fun union_two_overlapping_rectangles() {
        val r0_c0 = Coordinate(0.0, 0.0, 0.0)
        val r0_c1 = Coordinate(0.0, 10.0, 0.0)
        val r0_c2 = Coordinate(10.0, 10.0, 0.0)
        val r0_c3 = Coordinate(10.0, 0.0, 0.0)
        val r0 = arrayOf(r0_c0, r0_c1, r0_c2, r0_c3, r0_c0)
        val g0 = GeometryFactory().createPolygon(r0)
        assertNotNull(g0)

        val r1_c0 = Coordinate(5.0, 5.0, 0.0)
        val r1_c1 = Coordinate(5.0, 9.0, 0.0)
        val r1_c2 = Coordinate(15.0, 9.0, 0.0)
        val r1_c3 = Coordinate(15.0, 5.0, 0.0)
        val r1 = arrayOf(r1_c0, r1_c1, r1_c2, r1_c3, r1_c0)
        val g1 = GeometryFactory().createPolygon(r1)
        assertNotNull(g1)

        val u = g0.join(g1)
        assertNotNull(u)
        val c = u.coordinates
        assertNotNull(c)
        assertEquals(7, c.size)
        assertEquals(c[0], r0_c0)
        assertEquals(c[1], r0_c1)
        assertEquals(c[2], r0_c2)
        assertEquals(c[3], r1_c2)
        assertEquals(c[4], r1_c3)
        assertEquals(c[5], r0_c3)
        assertEquals(c[6], r0_c0)
    }
}