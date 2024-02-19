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
package com.github.geonames.test;

import com.github.geonames.Countries;
import com.github.geonames.CountryInfo;
import com.github.geonames.CountryRegion;
import com.github.geonames.GeoShape;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

public class CountryCanvas extends JComponent {

    private static final int RATIO = 50000;

    private static final int BORDER_VERTICES = 16;

    private int[] mainVertices;
    private int[] centre;
    private Polygon poly;
    private Polygon border;
    private int ratio = RATIO;
    private int ratioNeg = -RATIO;
    private int tX, tY;
    private int[] specific;

    public CountryCanvas() {
        super();
        setPreferredSize(new Dimension(2500, 1500));
    }

    public void setRegion(CountryRegion region) {
        ratio = RATIO;
        tX = 0;
        tY = 0;
        switch (region.getCountryCode()) {
            case "AE":
                ratio /= 40;
                tX = -3700;
                tY = 2300;
                break;
            case "AF":
                tX = -1100;
                tY = 900;
                break;
            case "BW":
                tX = -250;
                tY = -300;
                // Dikholola near Brits.
                specific = new int[]{27746222, -25411172};
                break;
            case "IL":
                tX = -450;
                tY = 850;
                break;
            case "US":
                tX = 3400;
                tY = 1500;
                break;
            case "ZA":
                ratio /= 25;
                tX = -800;
                tY = -1000;
                // Dikholola near Brits.
                specific = new int[]{27746222, -25411172};
                break;
        }

        ratioNeg = -ratio;
        centre = findCentre(region);
        centre[0] /= ratio;
        centre[1] /= ratioNeg;
        mainVertices = region.findMainVertices(BORDER_VERTICES);

        poly = new Polygon();
        for (int i = 0; i < region.npoints; i++) {
            poly.addPoint(region.xpoints[i] / ratio, region.ypoints[i] / ratioNeg);
        }
        border = new Polygon();
        for (int i : mainVertices) {
            if (i >= 0) {
                border.addPoint(region.xpoints[i] / ratio, region.ypoints[i] / ratioNeg);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        g.translate(tX, tY);

        g.setColor(Color.DARK_GRAY);
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).draw(poly);
        } else {
            g.drawPolygon(poly);
        }

        Rectangle rect = poly.getBounds();
        g.setColor(Color.YELLOW);
        g.drawRect(rect.x, rect.y, rect.width, rect.height);

        int cx = centre[0];
        int cy = centre[1];
        g.setColor(Color.RED);
        g.drawOval(cx - 2, cy - 2, 5, 5);

        final double sweepAngle = (2f * Math.PI) / BORDER_VERTICES;
        double angleStart = -(sweepAngle * 0.5f);
        double angleEnd;
        int x2, y2;
        int r = 2500;

        for (int v = 0; v < BORDER_VERTICES; v++) {
            x2 = cx + (int) (r * Math.cos(angleStart));
            y2 = cy + (int) (r * Math.sin(angleStart));
            g.setColor(Color.CYAN);
            g.drawLine(cx, cy, x2, y2);

            angleEnd = angleStart + sweepAngle;
            x2 = cx + (int) (r * Math.cos(angleEnd));
            y2 = cy + (int) (r * Math.sin(angleEnd));
            g.setColor(Color.MAGENTA);
            g.drawLine(cx, cy, x2, y2);

            angleStart += sweepAngle;
        }

        g.setColor(Color.BLUE);
        for (int i : mainVertices) {
            if (i >= 0)
                g.drawOval(poly.xpoints[i] - 2, poly.ypoints[i] - 2, 5, 5);
        }
        g.setColor(Color.ORANGE);
        g.drawPolygon(border);

        if (specific != null) {
            g.setColor(Color.RED);
            g.drawOval((specific[0] / ratio) - 5, (specific[1] / ratioNeg) - 5, 10, 10);
        }

        g.translate(-tX, -tY);
    }

    /**
     * Find the centre of gravity.
     *
     * @return the middle-x at index {@code 0}, the middle-y at index {@code 1}.
     */
    public int[] findCentre(CountryRegion region) {
        int[] centre = new int[2];

        int n = region.npoints;
        long tx = 0;
        long ty = 0;
        for (int i = 0; i < n; i++) {
            tx += region.xpoints[i];
            ty += region.ypoints[i];
        }
        centre[0] = (int) (tx / n);
        centre[1] = (int) (ty / n);

        return centre;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Country code required");
            System.exit(0);
            return;
        }
        String code = args[0];

        populateFromShapes(code, new File("GeoNames/res/countryInfo.txt"), new File("GeoNames/res/shapes_simplified_low.txt"));
    }

    public static void populateFromShapes(String code, File countryInfoFile, File shapesFile) throws IOException {
        Countries countries = new Countries();
        Collection<CountryInfo> names = countries.loadInfo(countryInfoFile);
        Collection<GeoShape> shapes = countries.loadShapes(shapesFile);
        Collection<CountryRegion> regions = countries.toRegions(names, shapes);
        CountryCanvas canvas = null;

        for (CountryRegion region : regions) {
            if (code.equals(region.getCountryCode())) {
                canvas = new CountryCanvas();
                canvas.setRegion(region);
                break;
            }
        }

        JFrame window = new JFrame();
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setBounds(0, 0, 1000, 1000);
        window.getContentPane().add(new JScrollPane(canvas));
        window.setVisible(true);
    }
}
