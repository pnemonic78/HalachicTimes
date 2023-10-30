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
package com.github.times;

import static com.github.times.ZmanimDays.SHABBATH;
import static com.github.times.ZmanimPopulater.BEFORE_SUNSET;
import static com.kosherjava.zmanim.hebrewcalendar.JewishCalendar.CHANUKAH;
import static com.kosherjava.zmanim.hebrewcalendar.JewishCalendar.EREV_YOM_KIPPUR;
import static com.kosherjava.zmanim.hebrewcalendar.JewishCalendar.YOM_KIPPUR;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.times.databinding.CandlesChannukaBinding;
import com.github.times.databinding.CandlesFragmentBinding;
import com.github.times.databinding.CandlesKippurimBinding;
import com.github.times.databinding.CandlesShabbatBinding;
import com.github.times.location.ZmanimLocations;
import com.github.times.preference.SimpleZmanimPreferences;
import com.github.times.preference.ZmanimPreferences;
import com.kosherjava.zmanim.util.GeoLocation;

import java.util.Calendar;

/**
 * Shows candle images.
 *
 * @author Moshe Waisberg
 */
public class CandlesFragment extends Fragment {

    /**
     * Provider for locations.
     */
    private ZmanimLocations locations;
    /**
     * The preferences.
     */
    private ZmanimPreferences preferences;

    private CandlesFragmentBinding _binding = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = requireContext();
        if (context instanceof ZmanimActivity) {
            preferences = ((ZmanimActivity) context).getZmanimPreferences();
        } else {
            preferences = new SimpleZmanimPreferences(context);
        }
        ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
        locations = app.getLocations();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        CandlesFragmentBinding binding = CandlesFragmentBinding.inflate(inflater, container, false);
        _binding = binding;
        return binding.getRoot();
    }

    @NonNull
    private CandlesPopulater createPopulater(@NonNull Context context) {
        return new CandlesPopulater(context, preferences);
    }

    public void populateTimes(@NonNull Calendar date) {
        // Called before attached to activity?
        if (!isAdded()) return;
        CandlesFragmentBinding binding = _binding;
        if (binding == null) return;

        ZmanimLocations locations = this.locations;
        if (locations == null) return;
        GeoLocation gloc = locations.getGeoLocation();
        // Have we been destroyed?
        if (gloc == null) return;
        Context context = binding.getRoot().getContext();

        CandlesPopulater populater = createPopulater(context);
        populater.setCalendar(date);
        populater.setGeoLocation(gloc);
        populater.setInIsrael(locations.isInIsrael());

        final CandleData candles = populater.populateCandles(preferences);

        final boolean animate = preferences.isCandlesAnimated();
        bindViews(binding, candles, animate);
    }

    private void bindViews(@NonNull CandlesFragmentBinding binding, @NonNull CandleData candles, boolean animate) {
        ViewGroup container = binding.getRoot();
        container.removeAllViews();

        final int holiday = getCandlesHoliday(candles);
        final int candlesCount = candles.countTomorrow;
        CandleView[] candleViews = null;

        Context context = container.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (holiday) {
            case EREV_YOM_KIPPUR:
            case YOM_KIPPUR:
                CandlesKippurimBinding bindingKippur = CandlesKippurimBinding.inflate(inflater, container, true);

                candleViews = new CandleView[1];
                candleViews[0] = bindingKippur.candle1;
                break;
            case CHANUKAH:
                CandlesChannukaBinding bindingChannuka = CandlesChannukaBinding.inflate(inflater, container, true);

                candleViews = new CandleView[9];
                candleViews[0] = bindingChannuka.candle1;
                candleViews[1] = bindingChannuka.candle2;
                candleViews[2] = bindingChannuka.candle3;
                candleViews[3] = bindingChannuka.candle4;
                candleViews[4] = bindingChannuka.candle5;
                candleViews[5] = bindingChannuka.candle6;
                candleViews[6] = bindingChannuka.candle7;
                candleViews[7] = bindingChannuka.candle8;
                candleViews[8] = bindingChannuka.candleShamash;

                // Only show relevant candles.
                for (int i = 0; i < candlesCount; i++) {
                    candleViews[i].setVisibility(View.VISIBLE);
                }
                for (int i = candlesCount; i < 8; i++) {
                    candleViews[i].setVisibility(View.INVISIBLE);
                }
                break;
            default:
                if (candlesCount > 0) {
                    CandlesShabbatBinding bindingShabbat = CandlesShabbatBinding.inflate(inflater, container, true);

                    candleViews = new CandleView[2];
                    candleViews[0] = bindingShabbat.candle1;
                    candleViews[1] = bindingShabbat.candle2;
                }
                break;
        }

        setFlickers(candleViews, animate);
    }

    private void setFlickers(@Nullable CandleView[] candles, boolean enabled) {
        if (candles == null)
            return;
        for (CandleView candle : candles) {
            if (candle == null)
                continue;
            candle.flicker(enabled);
        }
    }

    /**
     * Set the view's visibility.
     *
     * @param visibility the visibility.
     * @see View#VISIBLE
     * @see View#INVISIBLE
     * @see View#GONE
     */
    public void setVisibility(int visibility) {
        View view = getView();
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    public int getCandlesCount() {
        ViewGroup root = (ViewGroup) getView();
        if (root == null) return 0;
        return root.getChildCount();
    }

    /**
     * Get the occasion for lighting candles.
     *
     * @return the candles holiday.
     */
    public int getCandlesHoliday(@NonNull CandleData candles) {
        final int when = candles.whenCandles;
        final int holidayToday = candles.holidayToday;
        final int holidayTomorrow = candles.holidayTomorrow;
        return (when == BEFORE_SUNSET) ? ((holidayTomorrow == SHABBATH) ? holidayTomorrow : holidayToday) : holidayTomorrow;
    }
}