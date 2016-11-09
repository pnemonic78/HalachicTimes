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
package net.sf.times.location.text;

import net.sf.text.method.RangeInputFilter;
import net.sf.times.location.ZmanimLocation;

/**
 * Longitude input filter.
 *
 * @author Moshe Waisberg
 */
public class LongitudeInputFilter extends RangeInputFilter {

    public static final double LONGITUDE_MIN = ZmanimLocation.LONGITUDE_MIN;
    public static final double LONGITUDE_MAX = ZmanimLocation.LONGITUDE_MAX;

    public LongitudeInputFilter() {
        this(false);
    }

    public LongitudeInputFilter(boolean sign) {
        super(sign, true, LONGITUDE_MIN, LONGITUDE_MAX);
    }
}