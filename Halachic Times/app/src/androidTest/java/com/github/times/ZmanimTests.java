package com.github.times;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.times.preference.SimpleZmanimPreferences;
import com.github.times.preference.ZmanimPreferences;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.TimeZone;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class ZmanimTests {

    @Test
    public void molad2019() {
        final Context context = getApplicationContext();
        assertNotNull(context);

        ZmanimPreferences preferences = new SimpleZmanimPreferences(context);
        assertNotNull(preferences);

        ZmanimPopulater populater = new ZmanimPopulater(context, preferences);
        assertNotNull(populater);
        populater.setInIsrael(true);

        assertMolad(populater, 2018, Calendar.DECEMBER, 7, 22, 29);
        assertMolad(populater, 2019, Calendar.JANUARY, 6, 11, 13);
        assertMolad(populater, 2019, Calendar.FEBRUARY, 4, 23, 57);
        assertMolad(populater, 2019, Calendar.MARCH, 6, 12, 41);
        assertMolad(populater, 2019, Calendar.APRIL, 5, 1, 25);
        assertMolad(populater, 2019, Calendar.MAY, 4, 14, 10);
        assertMolad(populater, 2019, Calendar.JUNE, 3, 2, 54);
        assertMolad(populater, 2019, Calendar.JULY, 2, 15, 38);
        assertMolad(populater, 2019, Calendar.AUGUST, 1, 4, 22);
        assertMolad(populater, 2019, Calendar.AUGUST, 30, 17, 6);
        assertMolad(populater, 2019, Calendar.SEPTEMBER, 29, 5, 50);
        assertMolad(populater, 2019, Calendar.OCTOBER, 28, 18, 34);
        assertMolad(populater, 2019, Calendar.NOVEMBER, 27, 7, 18);
        assertMolad(populater, 2019, Calendar.DECEMBER, 26, 20, 2);
    }

    private void assertMolad(ZmanimPopulater populater, int year, int month, int day, int hour, int minute) {
        ZmanimAdapter adapter = new ZmanimAdapter(populater.getContext(), populater.getSettings());
        assertNotNull(adapter);
        assertEquals(0, adapter.getCount());

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        assertNotNull(cal);
        cal.set(year, month, day);
        populater.setCalendar(cal);
        populater.populate(adapter, false);
        assertNotEquals(0, adapter.getCount());
        ZmanimItem item = adapter.getItemById(R.string.molad);
        assertNotNull(item);

        Calendar molad = Calendar.getInstance(cal.getTimeZone());
        assertNotNull(molad);
        molad.setTimeInMillis(item.time);
        assertEquals(year, molad.get(Calendar.YEAR));
        assertEquals(month, molad.get(Calendar.MONTH));
        assertEquals(day, molad.get(Calendar.DAY_OF_MONTH));
        assertEquals(hour, molad.get(Calendar.HOUR_OF_DAY));
        assertEquals(minute, molad.get(Calendar.MINUTE));
    }
}
