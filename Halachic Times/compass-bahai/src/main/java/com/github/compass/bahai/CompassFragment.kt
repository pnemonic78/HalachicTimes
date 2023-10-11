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
package com.github.compass.bahai;

/**
 * Show the direction in which to pray.
 * Points to to the tomb of Bahá'u'lláh in Bahjí, Israel.
 *
 * @author Moshe Waisberg
 */
public class CompassFragment extends com.github.times.compass.CompassFragment {

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
