package com.github.times;

import android.content.Context;

import androidx.annotation.StringRes;

import java.util.HashMap;
import java.util.Map;

import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.CHANUKAH;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.CHOL_HAMOED_PESACH;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.CHOL_HAMOED_SUCCOS;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.FAST_OF_ESTHER;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.FAST_OF_GEDALYAH;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.HOSHANA_RABBA;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.PESACH;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.PESACH_SHENI;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.PURIM;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.PURIM_KATAN;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.ROSH_CHODESH;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.ROSH_HASHANA;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.SEVENTEEN_OF_TAMMUZ;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.SHAVUOS;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.SHEMINI_ATZERES;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.SHUSHAN_PURIM;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.SIMCHAS_TORAH;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.SUCCOS;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.TENTH_OF_TEVES;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.TISHA_BEAV;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.TU_BEAV;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.TU_BESHVAT;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.YOM_HAATZMAUT;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.YOM_HASHOAH;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.YOM_HAZIKARON;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.YOM_KIPPUR;
import static net.sourceforge.zmanim.hebrewcalendar.JewishCalendar.YOM_YERUSHALAYIM;

public class ZmanimDays {

    /**
     * Holiday id for Shabbath.
     */
    public static final int SHABBATH = 100;

    private static final Map<Integer, Integer> NAMES = new HashMap();

    static {
        NAMES.put(PESACH, R.string.day_pesach);
        NAMES.put(CHOL_HAMOED_PESACH, R.string.day_chol_hamoed_pesach);
        NAMES.put(PESACH_SHENI, R.string.day_pesach_sheni);
        NAMES.put(SHAVUOS, R.string.day_shavuos);
        NAMES.put(SEVENTEEN_OF_TAMMUZ, R.string.day_seventeen_of_tammuz);
        NAMES.put(TISHA_BEAV, R.string.day_tisha_beav);
        NAMES.put(TU_BEAV, R.string.day_tu_beav);
        NAMES.put(ROSH_HASHANA, R.string.day_rosh_hashana);
        NAMES.put(FAST_OF_GEDALYAH, R.string.day_fast_of_gedalyah);
        NAMES.put(YOM_KIPPUR, R.string.day_yom_kippur);
        NAMES.put(SUCCOS, R.string.day_succos);
        NAMES.put(CHOL_HAMOED_SUCCOS, R.string.day_chol_hamoed_succos);
        NAMES.put(HOSHANA_RABBA, R.string.day_hoshana_rabba);
        NAMES.put(SHEMINI_ATZERES, R.string.day_shemini_atzeres);
        NAMES.put(SIMCHAS_TORAH, R.string.day_simchas_torah);
        NAMES.put(CHANUKAH, R.string.day_chanukah);
        NAMES.put(TENTH_OF_TEVES, R.string.day_tenth_of_teves);
        NAMES.put(TU_BESHVAT, R.string.day_tu_beshvat);
        NAMES.put(FAST_OF_ESTHER, R.string.day_fast_of_esther);
        NAMES.put(PURIM, R.string.day_purim);
        NAMES.put(SHUSHAN_PURIM, R.string.day_shushan_purim);
        NAMES.put(PURIM_KATAN, R.string.day_purim_katan);
        NAMES.put(ROSH_CHODESH, R.string.day_rosh_chodesh);
        NAMES.put(YOM_HASHOAH, R.string.day_yom_hashoah);
        NAMES.put(YOM_HAZIKARON, R.string.day_yom_hazikaron);
        NAMES.put(YOM_HAATZMAUT, R.string.day_yom_haatzmaut);
        NAMES.put(YOM_YERUSHALAYIM, R.string.day_yom_yerushalayim);
        NAMES.put(SHABBATH, R.string.day_shabbath);
    }

    @StringRes
    public static int getNameId(int day) {
        if (day < 0) return 0;
        Integer nameId = NAMES.get(day);
        return (nameId == null) ? 0 : nameId;
    }

    public static CharSequence getName(Context context, int day) {
        int nameId = getNameId(day);
        return (nameId == 0) ? null : context.getText(nameId);
    }
}
