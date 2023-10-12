/*
 * Copyright 2023, Moshe Waisberg
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
package com.github.compass.jewish

import com.github.times.compass.HolyCompassFragment

/**
 * Show the direction in which to pray.
 * Points to the Holy of Holies in Jerusalem in Israel.
 *
 * @author Moshe Waisberg
 */
class JewishCompassFragment : HolyCompassFragment() {
    init {
        setHoliest(HOLIEST_LATITUDE, HOLIEST_LONGITUDE, HOLIEST_ELEVATION)
    }

    companion object {
        /**
         * Latitude of the Holy of Holies.
         */
        private const val HOLIEST_LATITUDE = 31.778

        /**
         * Longitude of the Holy of Holies.
         */
        private const val HOLIEST_LONGITUDE = 35.2353

        /**
         * Elevation of the Holy of Holies, according to Google.
         */
        private const val HOLIEST_ELEVATION = 744.5184937
    }
}