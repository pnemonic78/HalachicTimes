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
package net.sf.geonames;

import com.vividsolutions.jts.geom.Geometry;

import java.awt.Shape;

/**
 * Geonames shape.
 *
 * @author moshe.w
 */
public class GeoShape {

    private long geoNameId;
    private String geoJSON;
    private Geometry geometry;
    private Shape shape;

    public String getGeoJSON() {
        return geoJSON;
    }

    public void setGeoJSON(String geoJSON) {
        this.geoJSON = geoJSON;
        setGeometry(null);
    }

    public long getGeoNameId() {
        return geoNameId;
    }

    public void setGeoNameId(long geoNameId) {
        this.geoNameId = geoNameId;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
        setShape(null);
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }
}
