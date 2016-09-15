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
package net.sf.text.method;

import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;

/**
 * Number range input filter.
 *
 * @author Moshe Waisberg
 */
public class RangeInputFilter extends DigitsKeyListener {

    protected final boolean sign;
    protected final boolean decimal;
    protected final double minimum;
    protected final double maximum;

    /**
     * Creates a new range filter with only non-negative integer values.
     *
     * @param minimum
     *         the minimum value.
     * @param maximum
     *         the maximum value.
     */
    public RangeInputFilter(int minimum, int maximum) {
        this(false, minimum, maximum);
    }

    /**
     * Creates a new range filter with only integer values.
     *
     * @param sign
     *         accepts the minus sign (only at the beginning)?
     * @param minimum
     *         the minimum value.
     * @param maximum
     *         the maximum value.
     */
    public RangeInputFilter(boolean sign, int minimum, int maximum) {
        this(sign, false, minimum, maximum);
    }

    /**
     * Creates a new range filter with only numerical values.
     *
     * @param sign
     *         accepts the minus sign (only at the beginning)?
     * @param decimal
     *         accepts the decimal point?
     * @param minimum
     *         the minimum value.
     * @param maximum
     *         the maximum value.
     */
    public RangeInputFilter(boolean sign, boolean decimal, double minimum, double maximum) {
        super(sign, decimal);
        if (!decimal) {
            minimum = Math.rint(minimum);
            maximum = Math.rint(maximum);
        }
        this.sign = sign;
        this.decimal = decimal;
        this.minimum = sign ? minimum : Math.max(0, minimum);
        this.maximum = sign ? maximum : Math.max(0, maximum);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        CharSequence out = super.filter(source, start, end, dest, dstart, dend);

        out = (out != null) ? out : source.subSequence(start, end);
        String s = dest.toString();
        s = s.substring(0, dstart) + out + s.substring(dend);
        if (!TextUtils.isEmpty(s)) {
            if (sign) {
                if ("-".equals(s) || "+".equals(s)) {
                    return out;
                }
                if (decimal && (".".equals(s) || "-.".equals(s) || "+.".equals(s))) {
                    return out;
                }
            } else if (decimal && (".".equals(s))) {
                return out;
            }

            double latitude = Double.parseDouble(s);
            if ((latitude < minimum) || (latitude > maximum)) {
                return "";
            }
        }

        return out;
    }
}
