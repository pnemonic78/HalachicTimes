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
package com.github.text.style;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Parcel;
import android.text.TextPaint;

/**
 * Changes the typeface family of the text to which the span is attached.
 * <p>
 * This span does not work with {@link android.widget.RemoteViews} because {@link Typeface} is not {@link android.os.Parcelable}.
 * </p>
 */
public class TypefaceSpan extends android.text.style.TypefaceSpan {

    private Typeface typeface;

    /**
     * Create a new span.
     *
     * @param family the font family for this typeface. Examples include "monospace", "serif", and
     *               "sans-serif".
     */
    public TypefaceSpan(String family) {
        super(family);
        typeface = Typeface.create(family, Typeface.NORMAL);
    }

    /**
     * Create a new span.
     *
     * @param tf the typeface.
     */
    public TypefaceSpan(Typeface tf) {
        super("sans-serif");
        typeface = tf;
    }

    /**
     * Create a new span.
     *
     * @param src the parcel with the font family to read.
     */
    public TypefaceSpan(Parcel src) {
        super(src);
    }

    /**
     * Get the typeface.
     *
     * @return the typeface.
     */
    public Typeface getTypeface() {
        return typeface;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        apply(ds, typeface);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        apply(paint, typeface);
    }

    private void apply(Paint paint, Typeface tf) {
        int oldStyle;

        Typeface old = paint.getTypeface();
        if (old == null) {
            oldStyle = Typeface.NORMAL;
        } else {
            oldStyle = old.getStyle();
        }

        tf = Typeface.create(tf, oldStyle);
        int fake = oldStyle & ~tf.getStyle();

        if ((fake & Typeface.BOLD) != 0) {
            paint.setFakeBoldText(true);
        }

        if ((fake & Typeface.ITALIC) != 0) {
            paint.setTextSkewX(-0.25f);
        }

        paint.setTypeface(tf);
    }
}
