/*
 * Copyright 2012, Moshe Waisberg
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
package com.github.times

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import java.util.Calendar

/**
 * Shows a list of all opinions for a halachic time (*zman*).
 *
 * @author Moshe Waisberg
 */
class ZmanimDetailsFragment<A : ZmanimDetailsAdapter, P : ZmanimDetailsPopulater<A>> :
    ZmanimFragment<ZmanDetailsViewHolder, A, P>() {

    /**
     * The master id for populating the details.
     */
    var masterId = 0
        private set

    @Suppress("UNCHECKED_CAST")
    override fun createAdapter(context: Context): A? {
        return if (masterId == 0) null else ZmanimDetailsAdapter(context, preferences) as A
    }

    override fun populateTimes(date: Calendar): A? {
        return populateTimes(date, masterId)
    }

    /**
     * Populate the list with detailed times.
     *
     * @param date the date.
     * @param id   the time id.
     */
    fun populateTimes(date: Calendar, id: Int): A? {
        masterId = id
        if (!isAdded) return null
        applyBackground(id)

        val populater = populater ?: return null
        populater.itemId = id
        return super.populateTimes(date)
    }

    private fun applyBackground(id: Int) {
        when (preferences.theme) {
            R.style.Theme_Zmanim_Dark -> setBackgroundColorDark(id, list)
            R.style.Theme_Zmanim_Light -> setBackgroundColorLight(id, list)
            R.style.Theme_Zmanim_DayNight -> {
                val context = list.context
                val nightMode =
                    context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                when (nightMode) {
                    Configuration.UI_MODE_NIGHT_NO -> setBackgroundColorLight(id, list)
                    Configuration.UI_MODE_NIGHT_YES -> setBackgroundColorDark(id, list)
                }
            }

            else -> list.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun setBackgroundColorDark(@StringRes id: Int, list: ViewGroup) {
        val context = list.context
        when (id) {
            R.string.dawn ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.dawn))

            R.string.tallis,
            R.string.tallis_only ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.tallis))

            R.string.sunrise ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.sunrise))

            R.string.shema ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.shema))

            R.string.prayers, R.string.eat_chametz, R.string.burn_chametz ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.prayers))

            R.string.midday ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.midday))

            R.string.earliest_mincha ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.earliest_mincha))

            R.string.mincha ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.mincha))

            R.string.plug_hamincha ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.plug_hamincha))

            R.string.sunset ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.sunset))

            R.string.twilight ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.twilight))

            R.string.nightfall,
            R.string.shabbath_ends,
            R.string.festival_ends ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.nightfall))

            R.string.midnight_guard,
            R.string.midnight,
            R.string.morning_guard ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.midnight))

            else -> list.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun setBackgroundColorLight(@StringRes id: Int, list: ViewGroup) {
        val context = list.context
        when (id) {
            R.string.dawn ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.dawn_light))

            R.string.tallis,
            R.string.tallis_only ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.tallis_light))

            R.string.sunrise ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.sunrise_light))

            R.string.shema ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.shema_light))

            R.string.prayers,
            R.string.eat_chametz,
            R.string.burn_chametz ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.prayers_light))

            R.string.midday ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.midday_light))

            R.string.earliest_mincha ->
                list.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.earliest_mincha_light
                    )
                )

            R.string.mincha ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.mincha_light))

            R.string.plug_hamincha ->
                list.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.plug_hamincha_light
                    )
                )

            R.string.sunset ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.sunset_light))

            R.string.twilight ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.twilight_light))

            R.string.nightfall,
            R.string.shabbath_ends,
            R.string.festival_ends ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.nightfall_light))

            R.string.midnight_guard,
            R.string.midnight,
            R.string.morning_guard ->
                list.setBackgroundColor(ContextCompat.getColor(context, R.color.midnight_light))

            else -> list.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun createPopulater(context: Context): P {
        return ZmanimDetailsPopulater<A>(context, preferences) as P
    }
}