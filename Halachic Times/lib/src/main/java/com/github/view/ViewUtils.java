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
package com.github.view;

import android.view.View;

/**
 * View utlities.
 *
 * @author Moshe Waisberg
 */
public class ViewUtils {

    private ViewUtils() {
    }

    /**
     * Make all views at least the same width.
     *
     * @param views the array of views.
     */
    public static void applyMaxWidth(View[] views) {
        int maxWidth = 0;
        // First, calculate the maximum widths of the views.
        for (View view : views) {
            maxWidth = Math.max(maxWidth, view.getMeasuredWidth());
        }
        // Then, apply the maximum width to all the views.
        for (View view : views) {
            view.setMinimumWidth(maxWidth);
        }
    }

    /**
     * Make all views at least the same width.
     *
     * @param views the list of views.
     */
    public static void applyMaxWidth(Iterable<View> views) {
        int maxWidth = 0;
        // First, calculate the maximum widths of the views.
        for (View view : views) {
            maxWidth = Math.max(maxWidth, view.getMeasuredWidth());
        }
        // Then, apply the maximum width to all the views.
        for (View view : views) {
            view.setMinimumWidth(maxWidth);
        }
    }
}
