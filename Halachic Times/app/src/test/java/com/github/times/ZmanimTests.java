package com.github.times;

import net.sourceforge.zmanim.ComplexZmanimCalendar;
import net.sourceforge.zmanim.util.GeoLocation;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ZmanimTests {

    @Test
    public void sofZmanKidushLevana_Jerusalem() {
        // Jerusalem, Israel, 2019-May
        TimeZone tz = TimeZone.getTimeZone("Asia/Jerusalem");
        Calendar cal = Calendar.getInstance(tz);
        cal.set(2019, Calendar.MAY, 1);
        GeoLocation location = new GeoLocation("test", 31.76904, 35.21633, tz);
        ComplexZmanimCalendar zcal = new ComplexZmanimCalendar(location);
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

}
