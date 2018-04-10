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
package com.github.times.location.text;

import net.sf.text.method.RangeInputFilter;
import com.github.times.location.ZmanimLocation;

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
