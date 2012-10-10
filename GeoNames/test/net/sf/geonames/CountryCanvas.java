/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.geonames;

import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.File;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;

public class CountryCanvas extends JComponent {

	private static final int RATIO = 50000;

	private int[] main8;
	private int[] centre;
	private Polygon poly;
	private Polygon border;

	public CountryCanvas(CountryRegion region) {
		super();
		setPreferredSize(new Dimension(2500, 1500));

		centre = findCentre(region);
		centre[0] /= RATIO;
		centre[1] /= -RATIO;
		main8 = region.findMainVertices(8);

		poly = new Polygon();
		for (int i = 0; i < region.npoints; i++) {
			poly.addPoint(region.xpoints[i] / RATIO, -region.ypoints[i] / RATIO);
		}
		border = new Polygon();
		for (int i : main8) {
			if (i >= 0)
				border.addPoint(region.xpoints[i] / RATIO, -region.ypoints[i] / RATIO);
		}
		int dX = 0;
		int dY = 0;
		if ("AF".equals(region.getCountryCode())) {
			dX = -1100;
			dY = 900;
		} else if ("IL".equals(region.getCountryCode())) {
			dX = -450;
			dY = 850;
		} else if ("US".equals(region.getCountryCode())) {
			dX = 3400;
			dY = 1500;
		} else if ("ZA".equals(region.getCountryCode())) {
			dX = -250;
			dY = -300;
		}
		poly.translate(dX, dY);
		border.translate(dX, dY);
		centre[0] += dX;
		centre[1] += dY;
	}

	public void paint(Graphics g) {
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
		g.drawOval(cx, cy, 5, 5);

		final double sweepAngle = (2f * Math.PI) / 8;
		double angleStart = -(sweepAngle / 2f);
		double angleEnd;
		int x2, y2;
		int r = 2500;

		for (int v = 0; v < 8; v++) {
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
		for (int i : main8) {
			if (i >= 0)
				g.drawOval(poly.xpoints[i], poly.ypoints[i], 5, 5);
		}
		g.setColor(Color.ORANGE);
		g.drawPolygon(border);
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

	public static void main(String[] args) {
		String path = "res/cities1000.txt";
		String code = args[0];
		File res = new File(path);
		Countries countries = new Countries();
		Collection<GeoName> names;
		Collection<CountryRegion> regions;
		CountryRegion region = null;
		try {
			names = countries.loadNames(res);
			regions = countries.toRegions(names);

			for (CountryRegion r : regions) {
				if (code.equals(r.getCountryCode())) {
					region = r;
					break;
				}
			}
			CountryCanvas canvas = new CountryCanvas(region);
			JDialog window = new JDialog(null, ModalityType.APPLICATION_MODAL);
			window.setBounds(0, 0, 500, 500);
			window.getContentPane().add(new JScrollPane(canvas));
			window.setVisible(true);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
