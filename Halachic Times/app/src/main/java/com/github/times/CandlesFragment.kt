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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.github.times.ZmanimDays.SHABBATH
import com.github.times.databinding.CandlesChannukaBinding
import com.github.times.databinding.CandlesFragmentBinding
import com.github.times.databinding.CandlesKippurimBinding
import com.github.times.databinding.CandlesShabbatBinding
import com.github.times.location.ZmanimLocations
import com.github.times.preference.SimpleZmanimPreferences
import com.github.times.preference.ZmanimPreferences
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import java.util.Calendar

/**
 * Shows candle images.
 *
 * @author Moshe Waisberg
 */
class CandlesFragment : Fragment() {
    /**
     * Provider for locations.
     */
    private val locations: ZmanimLocations
        get() {
            val app = requireContext().applicationContext as ZmanimApplication
            return app.locations
        }

    /**
     * The preferences.
     */
    private val preferences: ZmanimPreferences by lazy {
        val context = requireContext()
        if (context is ZmanimActivity) {
            context.preferences
        } else {
            SimpleZmanimPreferences(context)
        }
    }

    private var _binding: CandlesFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = CandlesFragmentBinding.inflate(inflater, container, false)
        _binding = binding
        return binding.root
    }

    private fun createPopulater(context: Context): CandlesPopulater {
        return CandlesPopulater(context, preferences)
    }

    fun populateTimes(date: Calendar) {
        // Called before attached to activity?
        if (!isAdded) return
        val binding = _binding ?: return
        val locations = locations
        val gloc = locations.geoLocation ?: return
        // Have we been destroyed?
        val context = binding.root.context
        val populater = createPopulater(context).apply {
            setCalendar(date)
            setGeoLocation(gloc)
            isInIsrael = locations.isInIsrael
        }
        val candles = populater.populateCandles(preferences) ?: return
        val animate = preferences.isCandlesAnimated
        bindViews(binding, candles, animate)
    }

    private fun bindViews(binding: CandlesFragmentBinding, candles: CandleData, animate: Boolean) {
        val container: ViewGroup = binding.root
        container.removeAllViews()
        val context = container.context
        val inflater = LayoutInflater.from(context)

        val holiday = getCandlesHoliday(candles)
        val candlesCount = candles.countTomorrow
        var candleViews: Array<CandleView>? = null
        when (holiday) {
            JewishCalendar.EREV_YOM_KIPPUR, JewishCalendar.YOM_KIPPUR -> {
                val bindingKippur = CandlesKippurimBinding.inflate(inflater, container, true)
                candleViews = arrayOf(bindingKippur.candle1)
            }

            JewishCalendar.CHANUKAH -> {
                val bindingChannuka = CandlesChannukaBinding.inflate(inflater, container, true)
                candleViews = arrayOf(
                    bindingChannuka.candle1,
                    bindingChannuka.candle2,
                    bindingChannuka.candle3,
                    bindingChannuka.candle4,
                    bindingChannuka.candle5,
                    bindingChannuka.candle6,
                    bindingChannuka.candle7,
                    bindingChannuka.candle8,
                    bindingChannuka.candleShamash
                )

                // Only show relevant candles.
                run {
                    var i = 0
                    while (i < candlesCount) {
                        candleViews!![i].visibility = View.VISIBLE
                        i++
                    }
                }
                var i = candlesCount
                while (i < 8) {
                    candleViews[i].visibility = View.INVISIBLE
                    i++
                }
            }

            else -> if (candlesCount > 0) {
                val bindingShabbat = CandlesShabbatBinding.inflate(inflater, container, true)
                candleViews = arrayOf(
                    bindingShabbat.candle1,
                    bindingShabbat.candle2
                )
            }
        }
        setFlickers(candleViews, animate)
    }

    private fun setFlickers(candles: Array<CandleView>?, enabled: Boolean) {
        if (candles == null) return
        for (candle in candles) {
            candle.flicker(enabled)
        }
    }

    /**
     * Set the view's visibility.
     *
     * @param visibility the visibility.
     */
    fun setVisibility(visibility: Boolean) {
        val view = view
        if (view != null) {
            view.isVisible = visibility
        }
    }

    val candlesCount: Int
        get() {
            val root = view as? ViewGroup ?: return 0
            return root.childCount
        }

    /**
     * Get the occasion for lighting candles.
     *
     * @return the candles holiday.
     */
    fun getCandlesHoliday(candles: CandleData): Int {
        val at = candles.whenCandles
        val holidayToday = candles.holidayToday
        val holidayTomorrow = candles.holidayTomorrow
        return if (at == CandleData.BEFORE_SUNSET) (if (holidayTomorrow == SHABBATH) holidayTomorrow else holidayToday) else holidayTomorrow
    }
}