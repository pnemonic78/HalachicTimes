/*
 * Copyright 2008 Marc Wick, geonames.org
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
 *
 */
package org.geonames

import com.github.times.location.geonames.json.DateSerializer
import java.util.Date
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * gmtOffset and dstOffset are computed on the server with the
 * [java.util.TimeZone] and included in the web service as not all
 * geonames users are using java.
 *
 * @author marc
 */
@Serializable
class Timezone {
    /**
     * the timezoneId (example : "Pacific/Honolulu")
     * <p>
     * see also [java.util.TimeZone] and
     * http://www.twinsun.com/tz/tz-link.htm
     *
     * @return the timezoneId
     */
    @SerialName("timezoneId")
    var timezoneId: String? = null

    /**
     * @return the countryCode
     */
    @SerialName("countryCode")
    var countryCode: String? = null

    /**
     * @return the time
     */
    @SerialName("time")
    @Serializable(with = DateSerializer::class)
    var time: Date? = null

    /**
     * @return the sunrise
     */
    @SerialName("sunrise")
    @Serializable(with = DateSerializer::class)
    var sunrise: Date? = null

    /**
     * @return the sunset
     */
    @SerialName("sunset")
    @Serializable(with = DateSerializer::class)
    var sunset: Date? = null

    /**
     * the gmtOffset as of first of January of current year
     *
     * @return the gmtOffset
     */
    @get:Deprecated("")
    @Deprecated("")
    var gmtOffset = 0.0

    /**
     * the dstOffset as of first of July of current year
     *
     * @return the dstOffset
     */
    @get:Deprecated("")
    @Deprecated("")
    var dstOffset = 0.0
}
