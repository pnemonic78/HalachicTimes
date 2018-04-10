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

import com.github.text.method.RangeInputFilter;

/**
 * Seconds input filter.
 *
 * @author Moshe Waisberg
 */
public class SecondsInputFilter extends RangeInputFilter {

    public static final double SECONDS_MIN = 0;
    public static final double SECONDS_MAX = 59;

    public SecondsInputFilter() {
        super(false, true, SECONDS_MIN, SECONDS_MAX);
    }
}
