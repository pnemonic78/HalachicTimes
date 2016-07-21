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
package net.sf.times.compass.bahai;

/**
 * Show the direction in which to pray.
 * Points to to the tomb of Bahá'u'lláh in Bahjí, Israel.
 *
 * @author Moshe Waisberg
 */
public class CompassFragment extends net.sf.times.compass.CompassFragment {

    /** Latitude of the Bahaullah's tomb, according to wikipedia. */
    private static final double HOLIEST_LATITUDE = 32.943333;
    /** Longitude of the Bahaullah's tomb, according to wikipedia. */
    private static final double HOLIEST_LONGITUDE = 35.092222;
    /** Elevation of the Bahaullah's tomb, according to Google. */
    private static final double HOLIEST_ELEVATION = 22;

    public CompassFragment() {
        setHoliest(HOLIEST_LATITUDE, HOLIEST_LONGITUDE, HOLIEST_ELEVATION);
    }
}
