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
package net.sf.times;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.TextView;

import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sf.times.location.ZmanimLocations;
import net.sf.times.preference.ZmanimPreferences;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;
import net.sourceforge.zmanim.util.GeoLocation;

import java.util.Calendar;

/**
 * Shows a list of halachic times (<em>zmanim</em>) for prayers.
 *
 * @author Moshe Waisberg
 */
public class ZmanimFragment<A extends ZmanimAdapter, P extends ZmanimPopulater<A>> extends Fragment {

    private Context context;
    protected LayoutInflater inflater;
    private OnClickListener onClickListener;
    /** The scroller. */
    private ScrollView scrollView;
    /** The list. */
    protected ViewGroup list;
    /** Provider for locations. */
    protected ZmanimLocations locations;
    /** The settings and preferences. */
    protected ZmanimPreferences settings;
    /** The master item selected id. */
    private int highlightItemId;
    /** The master item selected row. */
    private View highlightRow;
    /** The master item background that is selected. */
    private Drawable highlightBackground;
    /** The master item background that is not selected. */
    private Drawable unhighlightBackground;
    private int paddingLeft;
    private int paddingTop;
    private int paddingRight;
    private int paddingBottom;
    /** The adapter populater. */
    private P populater;
    /** The adapter. */
    private A adapter;

    protected Context getContextImpl() {
        return context;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = getContextImpl();
        settings = new ZmanimPreferences(context);
        ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
        locations = app.getLocations();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        return inflater.inflate(R.layout.times_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.scrollView = (ScrollView) view;
        this.list = view.findViewById(android.R.id.list);
    }

    /**
     * Create a new times adapter.
     *
     * @param context
     *         the context.
     * @return the adapter.
     */
    @SuppressWarnings("unchecked")
    protected A createAdapter(Context context) {
        if (context == null)
            return null;
        return (A) new ZmanimAdapter(context, settings);
    }

    /**
     * Get the times adapter.
     *
     * @return the populated adapter - {@code null} otherwise.
     */
    protected A getAdapter() {
        return this.adapter;
    }

    /**
     * Create a new times populater.
     *
     * @param context
     *         the context.
     * @return the populater.
     */
    @SuppressWarnings("unchecked")
    protected P createPopulater(Context context) {
        if (context == null)
            return null;
        return (P) new ZmanimPopulater<A>(context, settings);
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
     * @param date
     *         the date.
     */
    public A populateTimes(Calendar date) {
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
     * @param list
     *         the list.
     * @param adapter
     *         the list adapter.
     */
    protected void bindViews(final ViewGroup list, A adapter) {
        if (list == null)
            return;
        list.removeAllViews();
        if (adapter == null)
            return;

        final Context context = getContextImpl();
        if (context == null)
            return;

        JewishCalendar jcal = adapter.getJewishCalendar();
        if (jcal == null) {
            // Ignore potential "IllegalArgumentException".
            return;
        }
        CharSequence dateHebrew = adapter.formatDate(context, jcal);

        final int count = adapter.getCount();
        int position = 0;
        ZmanimItem item;
        View row;
        CharSequence groupingText;
        final View[] timeViews = new View[count];
        JewishDate jewishDatePrevious = null;

        if (count > 0) {
            item = adapter.getItem(position);
            if (item != null) {
                if (item.titleId == R.string.hour) {
                    row = adapter.getView(position, null, list);
                    timeViews[position] = row.findViewById(R.id.time);
                    bindView(list, position, row, item);
                    position++;
                    item = adapter.getItem(position);
                }
                jewishDatePrevious = item.jewishDate;
            }

            bindViewGrouping(list, position, dateHebrew);

            for (; position < count; position++) {
                item = adapter.getItem(position);
                if (item == null) {
                    continue;
                }

                // Start of the next Hebrew day.
                if ((jewishDatePrevious == null) || !jewishDatePrevious.equals(item.jewishDate)) {
                    jewishDatePrevious = item.jewishDate;
                    jcal.forward();

                    dateHebrew = adapter.formatDate(context, jcal);
                    groupingText = dateHebrew;

                    // Sefirat HaOmer?
                    int omer = jcal.getDayOfOmer();
                    if (omer >= 1) {
                        CharSequence omerLabel = adapter.formatOmer(context, omer);
                        if (!TextUtils.isEmpty(omerLabel)) {
                            groupingText = TextUtils.concat(groupingText, "\n", omerLabel);
                        }
                    }

                    bindViewGrouping(list, position, groupingText);
                }

                row = adapter.getView(position, null, list);
                timeViews[position] = row.findViewById(R.id.time);
                bindView(list, position, row, item);
            }
        }

        list.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                list.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                // Make all time texts same width.
                int maxWidth = 0;
                for (View view : timeViews) {
                    maxWidth = Math.max(maxWidth, view.getMeasuredWidth());
                }
                for (View view : timeViews) {
                    view.setMinimumWidth(maxWidth);
                }
            }
        });

        highlight(highlightItemId);
    }

    /**
     * Bind the time to a list.
     *
     * @param list
     *         the list.
     * @param position
     *         the position index.
     * @param row
     *         the row view.
     * @param item
     *         the item.
     */
    protected void bindView(ViewGroup list, int position, View row, ZmanimItem item) {
        if (row == null) {
            throw new IllegalArgumentException("row required");
        }
        setOnClickListener(row, item);
        inflater.inflate(R.layout.divider, list);
        list.addView(row);
    }

    /**
     * Bind the date group header to a list.
     *
     * @param list
     *         the list.
     * @param position
     *         the position index.
     * @param label
     *         the formatted Hebrew date label.
     */
    @SuppressLint("InflateParams")
    protected void bindViewGrouping(ViewGroup list, int position, CharSequence label) {
        if (position > 0)
            inflater.inflate(R.layout.divider, list);
        View row = inflater.inflate(R.layout.date_group, list, false);
        TextView text = row.findViewById(android.R.id.title);
        text.setText(label);
        list.addView(row);
    }

    protected void setOnClickListener(View view, ZmanimItem item) {
        final int id = item.titleId;
        boolean clickable = view.isEnabled() && (id != R.string.molad);
        view.setOnClickListener(clickable ? onClickListener : null);
        view.setClickable(clickable);
    }

    public void setOnClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    /**
     * Get the background for the selected item.
     *
     * @return the background.
     */
    private Drawable getSelectedBackground() {
        if (highlightBackground == null) {
            highlightBackground = getContextImpl().getResources().getDrawable(R.drawable.list_selected);
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
     * @param view
     *         the row view.
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            view.setBackgroundDrawable(bg);
        else
            view.setBackground(bg);
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        unhighlightBackground = null;
    }

    /**
     * Mark the row as selected.
     *
     * @param itemId
     *         the row id.
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            view.setBackgroundDrawable(getSelectedBackground());
        else
            view.setBackground(getSelectedBackground());
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        highlightRow = view;
        // Scroll to the row
        scrollView.requestChildFocus(list, view);
    }

    /**
     * Set the view's visibility.
     *
     * @param visibility
     *         the visibility.
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
