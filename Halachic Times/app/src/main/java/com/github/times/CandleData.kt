package com.github.times

import com.github.times.ZmanimDays.NO_HOLIDAY

data class CandleData(
    val holidayToday: Int = NO_HOLIDAY,
    val countToday: Int = CANDLES_NONE,
    val holidayTomorrow: Int = NO_HOLIDAY,
    val countTomorrow: Int = CANDLES_NONE,
    val whenCandles: Int = BEFORE_SUNSET,
    val candlesOffset: Int = 0,
    val omerToday: Int = 0,
    val omerTomorrow: Int = 0
) {
    companion object {

        /**
         * No candles to light.
         */
        const val CANDLES_NONE = 0

        /**
         * Number of candles to light for Shabbath.
         */
        const val CANDLES_SHABBATH = 2

        /**
         * Number of candles to light for a festival.
         */
        const val CANDLES_FESTIVAL = 2

        /**
         * Number of candles to light for Yom Kippurim.
         */
        const val CANDLES_YOM_KIPPUR = 1

        /**
         * Flag indicating lighting times before sunset.
         */
        const val BEFORE_SUNSET = 0

        /**
         * Flag indicating lighting times at sunset.
         */
        const val AT_SUNSET = 1

        /**
         * Flag indicating lighting times at twilight.
         */
        const val AT_TWILIGHT = 2

        /**
         * Flag indicating lighting times after nightfall.
         */
        const val AT_NIGHT = 3

        /**
         * Flag indicating lighting times after Shabbath.
         */
        const val MOTZE_SHABBATH = 4
    }
}