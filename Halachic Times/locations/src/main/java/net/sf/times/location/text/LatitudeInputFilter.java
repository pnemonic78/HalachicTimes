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

import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;

import net.sf.times.location.ZmanimLocation;

/**
 * Latitude input filter.
 *
 * @author Moshe Waisberg
 */
public class LatitudeInputFilter extends DigitsKeyListener {

    protected static final double LATITUDE_MIN = ZmanimLocation.LATITUDE_MIN;
    protected static final double LATITUDE_MAX = ZmanimLocation.LATITUDE_MAX;

    public LatitudeInputFilter() {
        super(true, true);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        CharSequence out = super.filter(source, start, end, dest, dstart, dend);

        out = (out != null) ? out : source.subSequence(start, end);
        String s = dest.toString();
        s = s.substring(0, dstart) + out + s.substring(dend);
        if (!TextUtils.isEmpty(s)) {
            if ("-".equals(s) || "+".equals(s) || ".".equals(s) || "-.".equals(s) || "+.".equals(s)) {
                return out;
            }
            double latitude = Double.parseDouble(s);
            if ((latitude < LATITUDE_MIN) || (latitude > LATITUDE_MAX)) {
                return "";
            }
        }

        return out;
    }
}
