package org.geonames

import java.util.Date
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.geonames.json.DefaultDateSerializer

@Serializable
data class TimeZone(
    /**
     * The timezoneId (example : "Pacific/Honolulu")
     * <p>
     * see also [java.util.TimeZone] and
     * http://www.twinsun.com/tz/tz-link.htm
     *
     * @return the timezoneId
     */
    @SerialName("timeZoneId")
    var id: String,

    /**
     * @return the countryCode
     */
    @SerialName("countryCode")
    var countryCode: String? = null,

    /**
     * @return the time
     */
    @SerialName("time")
    @Serializable(with = DefaultDateSerializer::class)
    var time: Date? = null,

    /**
     * @return the sunrise
     */
    @SerialName("sunrise")
    @Serializable(with = DefaultDateSerializer::class)
    var sunrise: Date? = null,

    /**
     * @return the sunset
     */
    @SerialName("sunset")
    @Serializable(with = DefaultDateSerializer::class)
    var sunset: Date? = null,

    /**
     * The gmtOffset as of first of January of current year
     *
     * @return the gmtOffset
     */
    @SerialName("gmtOffset")
    @get:Deprecated("")
    @Deprecated("")
    var gmtOffset: Double = 0.0,

    /**
     * The dstOffset as of first of July of current year
     *
     * @return the dstOffset
     */
    @SerialName("dstOffset")
    @get:Deprecated("")
    @Deprecated("")
    var dstOffset: Double = 0.0
) {
    val timeZone: java.util.TimeZone get() = java.util.TimeZone.getTimeZone(id)
}
