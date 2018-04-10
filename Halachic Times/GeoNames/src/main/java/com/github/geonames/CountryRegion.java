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
package com.github.geonames;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

import org.geotools.geojson.geom.GeometryJSON;

import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.github.geonames.CountryInfo.*;

/**
 * Country region.
 *
 * @author Moshe Waisberg
 */
public class CountryRegion extends Polygon {

    /** Factor to convert coordinate value to a fixed-point integer. */
    public static final double FACTOR_TO_INT = 1e+5;
    /**
     * Factor to convert coordinate value to a fixed-point integer for city
     * limits.
     */
    private static final double CITY_BOUNDARY = 1e+4;

    private final String countryCode;

    /**
     * Constructs a new region.
     */
    public CountryRegion(String countryCode) {
        super();
        if (ISO639_PALESTINE.equals(countryCode)) {
            countryCode = ISO639_ISRAEL;
        }
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Add a location.
     *
     * @param latitude
     *         the latitude.
     * @param longitude
     *         the longitude.
     */
    public void addLocation(double latitude, double longitude) {
        int x = (int) (longitude * FACTOR_TO_INT);
        int y = (int) (latitude * FACTOR_TO_INT);
        addPoint(x, y);
    }

    /**
     * Add a set of locations.
     *
     * @param polygon
     *         the polygon with locations.
     */
    public void addPolygon(Polygon polygon) {
        for (int i = 0; i < polygon.npoints; i++) {
            addPoint(polygon.xpoints[i], polygon.ypoints[i]);
        }
    }

    /**
     * Find the main vertices that represent the border.
     *
     * @param vertexCount
     *         the number of vertices.
     * @return an array of indexes.
     */
    public int[] findMainVertices(final int vertexCount) {
        int[] indexes = new int[vertexCount];
        int r = 0;
        int n = npoints;

        // Find centre.
        long tx = 0;
        long ty = 0;
        for (int i = 0; i < n; i++) {
            tx += xpoints[i];
            ty += ypoints[i];
        }
        double cx = ((double) tx) / n;
        double cy = ((double) ty) / n;

        final double sweepAngle = (2f * Math.PI) / vertexCount;
        double angleStart = -(sweepAngle / 2f);
        double angleEnd;
        double x, y, a, d;
        int farIndex;
        double farDist;

        for (int v = 0; v < vertexCount; v++) {
            angleEnd = angleStart + sweepAngle;
            farDist = Double.MIN_VALUE;
            farIndex = -1;

            for (int i = 0; i < n; i++) {
                x = xpoints[i];
                y = ypoints[i];
                a = Math.atan2(y - cy, x - cx) + Math.PI;
                if ((angleStart <= a) && (a < angleEnd)) {
                    d = Point2D.distanceSq(cx, cy, x, y);
                    if (farDist < d) {
                        farDist = d;
                        farIndex = i;
                    }
                }
            }

            if (farIndex >= 0)
                indexes[r++] = farIndex;

            angleStart += sweepAngle;
        }

        if (r < vertexCount) {
            switch (r) {
                case 0:
                    addPoint((int) (xpoints[0] - CITY_BOUNDARY), (int) (ypoints[0] - CITY_BOUNDARY));
                    addPoint((int) (xpoints[0] + CITY_BOUNDARY), (int) (ypoints[0] + CITY_BOUNDARY));
                case 1:
                    addPoint((int) (xpoints[1] - CITY_BOUNDARY), (int) (ypoints[1] - CITY_BOUNDARY));
                    addPoint((int) (xpoints[1] + CITY_BOUNDARY), (int) (ypoints[1] + CITY_BOUNDARY));
                    return findMainVertices(vertexCount);
            }

            for (int i = r; i < vertexCount; i++)
                indexes[i] = -1;
        }

        return indexes;
    }

    public static CountryRegion toRegion(String countryCode, GeoShape geoShape) throws IOException {
        CountryRegion region = new CountryRegion(countryCode);

        GeometryJSON json = new GeometryJSON();
        Geometry geometry = json.read(geoShape.getGeoJSON());
        geoShape.setGeometry(geometry);

        addGeometry(region, geometry);

        return region;
    }

    private static void addGeometry(CountryRegion region, Geometry geometry) {
        Coordinate[] coordinates = geometry.getCoordinates();
        final int length = coordinates.length;
        Coordinate coordinate;

        for (int i = 0; i < length; i++) {
            coordinate = coordinates[i];
            region.addLocation(coordinate.y, coordinate.x);
        }
    }

    public static List<CountryRegion> toRegions(String countryCode, GeoShape geoShape) throws IOException {
        List<CountryRegion> regions = new ArrayList<>();
        CountryRegion region;

        GeometryJSON json = new GeometryJSON();
        Geometry geometry = json.read(geoShape.getGeoJSON());
        geoShape.setGeometry(geometry);

        if (geometry instanceof com.vividsolutions.jts.geom.Polygon) {
            region = new CountryRegion(countryCode);
            addGeometry(region, geometry);
            regions.add(region);
        } else if (geometry instanceof GeometryCollection) {
            GeometryCollection gc = (GeometryCollection) geometry;
            int count = gc.getNumGeometries();
            for (int i = 0; i < count; i++) {
                region = new CountryRegion(countryCode);
                addGeometry(region, gc.getGeometryN(i));
                regions.add(region);
            }
        }

        return regions;
    }
}
