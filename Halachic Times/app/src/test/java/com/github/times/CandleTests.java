package com.github.times;

import org.junit.Test;

import static com.github.times.ZmanimPopulater.BEFORE_SUNSET;
import static com.github.times.ZmanimPopulater.CANDLES_MASK;
import static com.github.times.ZmanimPopulater.CANDLES_TODAY_INDEX;
import static com.github.times.ZmanimPopulater.CANDLES_TOMORROW_INDEX;
import static com.github.times.ZmanimPopulater.HOLIDAY_MASK;
import static com.github.times.ZmanimPopulater.HOLIDAY_TODAY_INDEX;
import static com.github.times.ZmanimPopulater.HOLIDAY_TOMORROW_INDEX;
import static com.github.times.ZmanimPopulater.MOTZE_MASK;
import static com.github.times.ZmanimPopulater.MOTZE_INDEX;
import static com.github.times.ZmanimPopulater.OFFSET_MASK;
import static com.github.times.ZmanimPopulater.OFFSET_INDEX;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.CHANUKAH;
import static org.junit.Assert.assertEquals;

public class CandleTests {

    @Test
    public void calculateCandlesTestChanukka7() {
        int holidayToday = CHANUKAH;
        int countToday = 6;
        int holidayTomorrow = CHANUKAH;
        int countTomorrow = 7;
        int when = BEFORE_SUNSET;
        int candlesOffset = 30;

        long candles = ((when & MOTZE_MASK) << MOTZE_INDEX)
                | ((candlesOffset & OFFSET_MASK) << OFFSET_INDEX)
                | ((holidayTomorrow & HOLIDAY_MASK) << HOLIDAY_TOMORROW_INDEX)
                | ((holidayToday & HOLIDAY_MASK) << HOLIDAY_TODAY_INDEX)
                | ((countTomorrow & CANDLES_MASK) << CANDLES_TOMORROW_INDEX)
                | ((countToday & CANDLES_MASK) << CANDLES_TODAY_INDEX);

        final int candlesTodayActual = (int) ((candles >> CANDLES_TODAY_INDEX) & CANDLES_MASK);
        final int candlesTomorrowActual = (int) ((candles >> CANDLES_TOMORROW_INDEX) & CANDLES_MASK);
        final int holidayTodayActual = (byte) ((candles >> HOLIDAY_TODAY_INDEX) & HOLIDAY_MASK);
        final int holidayTomorrowActual = (byte) ((candles >> HOLIDAY_TOMORROW_INDEX) & HOLIDAY_MASK);
        final int candlesOffsetActual = (int) ((candles >> OFFSET_INDEX) & OFFSET_MASK);
        final int candlesWhenActual = (int) ((candles >> MOTZE_INDEX) & MOTZE_MASK);

        assertEquals(countToday, candlesTodayActual);
        assertEquals(countTomorrow, candlesTomorrowActual);
        assertEquals(holidayToday, holidayTodayActual);
        assertEquals(holidayTomorrow, holidayTomorrowActual);
        assertEquals(candlesOffset, candlesOffsetActual);
        assertEquals(when, candlesWhenActual);
    }
}
