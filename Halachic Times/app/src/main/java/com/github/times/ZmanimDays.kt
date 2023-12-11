package com.github.times

import android.content.Context
import androidx.annotation.StringRes
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar

object ZmanimDays {
    /**
     * Holiday none.
     */
    const val NO_HOLIDAY = -1
    /**
     * Holiday id for Shabbath.
     */
    const val SHABBATH = 100

    @StringRes
    val ID_NONE = 0

    private val names = mutableMapOf<Int, Int>()

    init {
        names[JewishCalendar.PESACH] = R.string.day_pesach
        names[JewishCalendar.CHOL_HAMOED_PESACH] = R.string.day_chol_hamoed_pesach
        names[JewishCalendar.PESACH_SHENI] = R.string.day_pesach_sheni
        names[JewishCalendar.SHAVUOS] = R.string.day_shavuos
        names[JewishCalendar.ROSH_HASHANA] = R.string.day_rosh_hashana
        names[JewishCalendar.FAST_OF_GEDALYAH] = R.string.day_fast_of_gedalyah
        names[JewishCalendar.YOM_KIPPUR] = R.string.day_yom_kippur
        names[JewishCalendar.SUCCOS] = R.string.day_succos
        names[JewishCalendar.CHOL_HAMOED_SUCCOS] = R.string.day_chol_hamoed_succos
        names[JewishCalendar.HOSHANA_RABBA] = R.string.day_hoshana_rabba
        names[JewishCalendar.SHEMINI_ATZERES] = R.string.day_shemini_atzeres
        names[JewishCalendar.SIMCHAS_TORAH] = R.string.day_simchas_torah
        names[JewishCalendar.CHANUKAH] = R.string.day_chanukka
        names[JewishCalendar.FAST_OF_ESTHER] = R.string.day_fast_of_esther
        names[JewishCalendar.PURIM] = R.string.day_purim
        names[JewishCalendar.SHUSHAN_PURIM] = R.string.day_shushan_purim
        names[JewishCalendar.PURIM_KATAN] = R.string.day_purim_katan
        names[JewishCalendar.ROSH_CHODESH] = R.string.day_rosh_chodesh
        names[JewishCalendar.YOM_HASHOAH] = R.string.day_yom_hashoah
        names[JewishCalendar.YOM_HAZIKARON] = R.string.day_yom_hazikaron
        names[JewishCalendar.YOM_HAATZMAUT] = R.string.day_yom_haatzmaut
        names[JewishCalendar.YOM_YERUSHALAYIM] = R.string.day_yom_yerushalayim
        names[SHABBATH] = R.string.day_shabbath
    }

    @StringRes
    fun getNameId(day: Int): Int {
        if (day < 0) return ID_NONE
        return names[day] ?: ID_NONE
    }

    fun getName(context: Context, day: Int): CharSequence? {
        return getName(context, day, 0)
    }

    fun getName(context: Context, day: Int, count: Int): CharSequence? {
        val nameId = getNameId(day)
        if (nameId == ID_NONE) return null
        if (count <= 0) return context.getText(nameId)
        return context.getString(nameId, count)
    }
}