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

import static com.github.view.ViewUtils.applyMaxWidth;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.github.times.databinding.DateGroupBinding;
import com.github.times.databinding.DateGroupLightBinding;
import com.github.times.databinding.DividerBinding;
import com.github.times.databinding.TimesListBinding;
import com.github.times.location.ZmanimLocations;
import com.github.times.preference.SimpleZmanimPreferences;
import com.github.times.preference.ZmanimPreferences;
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;
import com.kosherjava.zmanim.hebrewcalendar.JewishDate;
import com.kosherjava.zmanim.util.GeoLocation;

import java.util.Calendar;

/**
 * Shows a list of halachic times (<em>zmanim</em>) for prayers.
 *
 * @author Moshe Waisberg
 */
public class ZmanimFragment<VH extends ZmanViewHolder, A extends ZmanimAdapter<VH>, P extends ZmanimPopulater<VH, A>> extends Fragment {

    private Context context;
    private OnZmanItemClickListener onClickListener;
    private TimesListBinding _binding;
    /**
     * The scroller.
     */
    private ScrollView scrollView;
    /**
     * The list.
     */
    protected ViewGroup list;
    /**
     * Provider for locations.
     */
    protected ZmanimLocations locations;
    /**
     * The preferences.
     */
    protected ZmanimPreferences preferences;
    /**
     * The master item selected id.
     */
    private int highlightItemId;
    /**
     * The master item selected row.
     */
    private View highlightRow;
    /**
     * The master item background that is selected.
     */
    private Drawable highlightBackground;
    /**
     * The master item background that is not selected.
     */
    private Drawable unhighlightBackground;
    private int paddingLeft;
    private int paddingTop;
    private int paddingRight;
    private int paddingBottom;
    /**
     * The adapter populater.
     */
    private P populater;
    /**
     * The adapter.
     */
    private A adapter;

    @NonNull
    protected Context getContextImpl() {
        if (context == null) {
            context = requireContext();
        }
        return context;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = getContextImpl();
        if (context instanceof ZmanimActivity) {
            preferences = ((ZmanimActivity) context).getZmanimPreferences();
        } else {
            preferences = new SimpleZmanimPreferences(context);
        }
        ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
        locations = app.getLocations();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TimesListBinding binding = TimesListBinding.inflate(inflater, container, false);
        _binding = binding;
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TimesListBinding binding = _binding;
        this.scrollView = binding.getRoot();
        this.list = binding.list;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

    /**
     * Create a new times adapter.
     *
     * @param context the context.
     * @return the adapter.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    protected A createAdapter(@NonNull Context context) {
        return (A) new ZmanimAdapter(context, preferences, onClickListener);
    }

    /**
     * Get the times adapter.
     *
     * @return the populated adapter - {@code null} otherwise.
     */
    @Nullable
    protected A getAdapter() {
        return this.adapter;
    }

    /**
     * Create a new times populater.
     *
     * @param context the context.
     * @return the populater.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    protected P createPopulater(@NonNull Context context) {
        return (P) new ZmanimPopulater<VH, A>(context, preferences);
    }

    /**
     * Get the times populater.
     *
     * @return the populater.
     */
    protected P getPopulater() {
        P populater = this.populater;
        if (populater == null) {
            populater = createPopulater(getContextImpl());
            this.populater = populater;
        }
        return populater;
    }

    /**
     * Populate the list with times.
     *
     * @param date the date.
     */
    public A populateTimes(@NonNull Calendar date) {
        // Called before attached to activity?
        if (!isAdded()) {
            return null;
        }
        ZmanimLocations locations = this.locations;
        if (locations == null)
            return null;
        GeoLocation gloc = locations.getGeoLocation();
        // Have we been destroyed?
        if (gloc == null)
            return null;

        P populater = getPopulater();
        if (populater != null) {
            populater.setCalendar(date);
            populater.setGeoLocation(gloc);
            populater.setInIsrael(locations.isInIsrael());
        }

        A adapter = createAdapter(getContextImpl());
        if ((populater != null) && (adapter != null)) {
            populater.populate(adapter, false);
        }
        this.adapter = adapter;

        ViewGroup list = this.list;
        if (list == null)
            return adapter;
        bindViews(list, adapter);
        return adapter;
    }

    /**
     * Bind the times to a list.
     *
     * @param list    the list.
     * @param adapter the list adapter.
     */
    protected void bindViews(@NonNull final ViewGroup list, @NonNull final A adapter) {
        if (list == null)
            return;
        if (adapter == null)
            return;

        list.removeAllViews();

        final Context context = getContextImpl();

        final JewishCalendar jcal = adapter.getJewishCalendar();
        if (jcal == null) {
            // Ignore potential "IllegalArgumentException".
            return;
        }
        JewishDate jewishDate = jcal;
        CharSequence dateHebrew = adapter.formatDate(context, jewishDate);

        final int count = adapter.getItemCount();
        int position = 0;
        ZmanimItem item;
        ZmanViewHolder viewHolder;
        final View[] timeViews = new View[count];
        JewishDate jewishDatePrevious = null;

        if (count > 0) {
            item = adapter.getItem(position);
            if (item != null) {
                if (item.titleId == R.string.hour) {
                    viewHolder = adapter.onCreateViewHolder(list, 0);
                    timeViews[position] = viewHolder.getTime();
                    bindView(list, viewHolder, item);
                    position++;
                    if (position < count) {
                        item = adapter.getItem(position);
                    }
                }
                jewishDatePrevious = item.jewishDate;
            }

            bindViewGrouping(list, dateHebrew);

            int holidayToday = adapter.getHolidayToday();
            int candlesToday = adapter.getCandlesTodayCount();
            CharSequence holidayName = ZmanimDays.getName(context, holidayToday, candlesToday);
            bindViewGrouping(list, holidayName);

            // Sefirat HaOmer?
            final int omerToday = adapter.getDayOfOmerToday();
            if (omerToday >= 1) {
                CharSequence omerLabel = adapter.formatOmer(context, omerToday);
                bindViewGrouping(list, omerLabel);
            }

            for (; position < count; position++) {
                item = adapter.getItem(position);
                if (item == null) {
                    continue;
                }
                jewishDate = item.jewishDate;

                // Start of the next Hebrew day.
                if ((jewishDate != null) && ((jewishDatePrevious == null) || !jewishDatePrevious.equals(jewishDate))) {
                    jewishDatePrevious = jewishDate;
                    jcal.setJewishDate(jewishDate.getJewishYear(), jewishDate.getJewishMonth(), jewishDate.getJewishDayOfMonth());

                    dateHebrew = adapter.formatDate(context, jewishDate);
                    bindViewGrouping(list, dateHebrew);

                    int holidayTomorrow = adapter.getHolidayTomorrow();
                    int candlesTomorrow = adapter.getCandlesCount();
                    CharSequence holidayTomorrowName = ZmanimDays.getName(context, holidayTomorrow, candlesTomorrow);
                    bindViewGrouping(list, holidayTomorrowName);

                    // Sefirat HaOmer?
                    final int omerTomorrow = adapter.getDayOfOmerTomorrow();
                    if (omerTomorrow >= 1) {
                        CharSequence omerLabel = adapter.formatOmer(context, omerTomorrow);
                        bindViewGrouping(list, omerLabel);
                    }
                }

                viewHolder = adapter.onCreateViewHolder(list, 0);
                timeViews[position] = viewHolder.getTime();
                bindView(list, viewHolder, item);
            }
        }

        list.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                list.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                // Make all time texts same width.
                try {
                    applyMaxWidth(timeViews);
                } catch (NullPointerException e) {
                    throw new NullPointerException("null object reference on " + jcal);
                }
            }
        });

        highlight(highlightItemId);
    }

    /**
     * Bind the time to a list.
     *
     * @param list       the list.
     * @param viewHolder the view holder.
     * @param item       the item.
     */
    protected void bindView(ViewGroup list, ZmanViewHolder viewHolder, ZmanimItem item) {
        if (viewHolder == null) {
            throw new IllegalArgumentException("holder required");
        }
        viewHolder.bind(item);
        bindDivider(list);
        list.addView(viewHolder.itemView);
    }

    protected void bindDivider(ViewGroup list) {
        LayoutInflater inflater = LayoutInflater.from(list.getContext());
        DividerBinding.inflate(inflater, list, true);
    }

    /**
     * Bind the date group header to a list.
     *
     * @param list  the list.
     * @param label the formatted Hebrew date label.
     */
    protected void bindViewGrouping(ViewGroup list, CharSequence label) {
        if (TextUtils.isEmpty(label)) return;
        if (list.getChildCount() > 0) {
            bindDivider(list);
        }
        LayoutInflater inflater = LayoutInflater.from(list.getContext());
        if (preferences.isDarkTheme()) {
            DateGroupBinding binding = DateGroupBinding.inflate(inflater, list, false);
            binding.title.setText(label);
            View row = binding.getRoot();
            list.addView(row);
        } else {
            DateGroupLightBinding binding = DateGroupLightBinding.inflate(inflater, list, false);
            binding.title.setText(label);
            View row = binding.getRoot();
            list.addView(row);
        }
    }

    public void setOnClickListener(OnZmanItemClickListener listener) {
        this.onClickListener = listener;
    }

    /**
     * Get the background for the selected item.
     *
     * @return the background.
     */
    private Drawable getSelectedBackground() {
        Drawable highlightBackground = this.highlightBackground;
        if (highlightBackground == null) {
            highlightBackground = ResourcesCompat.getDrawable(getContextImpl().getResources(), R.drawable.list_selected, null);
            this.highlightBackground = highlightBackground;
        }
        return highlightBackground;
    }

    /**
     * Mark the selected row as unselected.
     */
    public void unhighlight() {
        unhighlight(highlightRow);
    }

    /**
     * Mark the row as unselected.
     *
     * @param view the row view.
     */
    private void unhighlight(View view) {
        highlightItemId = 0;
        highlightRow = null;

        Drawable bg = unhighlightBackground;
        if ((view == null) || (bg == null))
            return;

        // Workaround for Samsung ICS bug where the highlight lingers.
        if (bg instanceof StateListDrawable)
            bg = bg.getConstantState().newDrawable();
        view.setBackground(bg);
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        unhighlightBackground = null;
    }

    /**
     * Mark the row as selected.
     *
     * @param itemId the row id.
     */
    public void highlight(int itemId) {
        highlightItemId = itemId;
        highlightRow = null;
        if (itemId == 0) {
            return;
        }
        // Find the view that matches the item id (the view that was clicked).
        final ViewGroup list = this.list;
        if (list == null)
            return;

        View view = null;
        View child;
        ZmanimItem item;
        final int count = list.getChildCount();
        for (int i = 0; i < count; i++) {
            child = list.getChildAt(i);
            item = (ZmanimItem) child.getTag(R.id.time);
            // Maybe row divider?
            if (item == null)
                continue;
            if (item.titleId == itemId) {
                view = child;
                break;
            }
        }
        if (view == null)
            return;

        unhighlightBackground = view.getBackground();
        paddingLeft = view.getPaddingLeft();
        paddingTop = view.getPaddingTop();
        paddingRight = view.getPaddingRight();
        paddingBottom = view.getPaddingBottom();
        view.setBackground(getSelectedBackground());
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        highlightRow = view;
        // Scroll to the row
        if (scrollView != null) {
            scrollView.requestChildFocus(list, view);
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
}
