package com.github.times;

import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.util.GeoLocation;
import net.sourceforge.zmanim.util.SunTimesCalculator;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static net.sourceforge.zmanim.AstronomicalCalendar.GEOMETRIC_ZENITH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ZmanimUnitTests {

    @Test
    public void sofZmanKidushLevana_Jerusalem() {
        // Jerusalem, Israel, 2019-May
        TimeZone tz = TimeZone.getTimeZone("Asia/Jerusalem");
        Calendar cal = Calendar.getInstance(tz);
        cal.set(2019, Calendar.MAY, 1);
        GeoLocation location = new GeoLocation("test", 31.76904, 35.21633, tz);
        ComplexZmanimCalendar zcal = new ComplexZmanimCalendar(location);
        zcal.setUseElevation(true);
        zcal.setCalendar(cal);
        Long date = zcal.getSofZmanKidushLevanaBetweenMoldos();
        assertNull(date);

        cal.set(2019, Calendar.MAY, 19);
        zcal.setCalendar(cal);
        date = zcal.getSofZmanKidushLevanaBetweenMoldos();
        assertNotNull(date);
        assertEquals(1558246265170L, date.longValue());
        cal.setTimeInMillis(date);
        assertEquals(cal.get(Calendar.YEAR), 2019);
        assertEquals(cal.get(Calendar.MONTH), Calendar.MAY);
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), 19);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 9);
        assertEquals(cal.get(Calendar.MINUTE), 11);
        assertEquals(cal.get(Calendar.SECOND), 5);
    }

    @Test
    public void sofZmanKidushLevana_NewJersey() {
        // Teaneck, NJ, USA, 2019-May
        TimeZone tz = TimeZone.getTimeZone("EDT");
        Calendar cal = Calendar.getInstance(tz);
        cal.set(2019, Calendar.MAY, 1);
        GeoLocation location = new GeoLocation("test", 40.896027, -74.0128627, tz);
        ComplexZmanimCalendar zcal = new ComplexZmanimCalendar(location);
        zcal.setUseElevation(true);
        zcal.setCalendar(cal);
        Long date = zcal.getSofZmanKidushLevanaBetweenMoldos();
        assertNull(date);

        cal.set(2019, Calendar.MAY, 19);
        zcal.setCalendar(cal);
        date = zcal.getSofZmanKidushLevanaBetweenMoldos();
        assertNotNull(date);
        assertEquals(1558246265170L, date.longValue());
        cal.setTimeInMillis(date);
        assertEquals(cal.get(Calendar.YEAR), 2019);
        assertEquals(cal.get(Calendar.MONTH), Calendar.MAY);
        assertEquals(cal.get(Calendar.DAY_OF_MONTH), 19);
        assertEquals(cal.get(Calendar.HOUR_OF_DAY), 6);
        assertEquals(cal.get(Calendar.MINUTE), 11);
        assertEquals(cal.get(Calendar.SECOND), 5);
    }


    @Test
    public void dawn_NewYork() {
        // New York, USA, 2021-June
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        Calendar cal = Calendar.getInstance(tz);
        GeoLocation location = new GeoLocation("test", 40.71427, -74.00597, 10.0, tz);
        ComplexZmanimCalendar zcal = new ComplexZmanimCalendar(location);
        zcal.setAstronomicalCalculator(new SunTimesCalculator());
        zcal.setUseElevation(true);

        for (int d = 14; d <= 16; d++) {
            cal.set(2021, Calendar.JUNE, d);
            zcal.setCalendar(cal);
            Long date;
            double offsetZenith;
            for (int i = 20; i <= 26; i++) {
                offsetZenith = GEOMETRIC_ZENITH + i;
                date = zcal.getSunriseOffsetByDegrees(offsetZenith);
                assertNotNull("day=" + d + " zenith=" + offsetZenith, date);
            }
            date = zcal.getAlos26Degrees();
            assertNotNull("day=" + d, date);
        }
    }
}
