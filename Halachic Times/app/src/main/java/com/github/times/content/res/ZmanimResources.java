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
package com.github.times.content.res;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Zmanim resources to patch buggy strings.
 */
public class ZmanimResources extends Resources {

    private int item_is_selected;

    /**
     * Create a new Resources object on top of an existing set of assets in an AssetManager.
     *
     * @param assets  Previously created AssetManager.
     * @param metrics Current display metrics to consider when selecting/computing resource values.
     * @param config  Desired device configuration to consider when selecting/computing resource values (optional).
     */
    public ZmanimResources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
        super(assets, metrics, config);

        item_is_selected = getIdentifier("item_is_selected", "string", "android");
    }

    @Override
    public String getString(int id) throws NotFoundException {
        String s = super.getString(id);

        // Patch for mistranslated Samsung Lollipop devices.
        if (id == item_is_selected) {
            s = s.replace("%d", "%s");
            s = s.replace("%1$d", "%1$s");
        }

        return s;
    }
}
