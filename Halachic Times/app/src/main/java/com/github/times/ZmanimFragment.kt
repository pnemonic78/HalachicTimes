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

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ScrollView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.github.times.ZmanViewHolder.Companion.TAG_ITEM
import com.github.times.ZmanimDays.getName
import com.github.times.databinding.DateGroupBinding
import com.github.times.databinding.DateGroupLightBinding
import com.github.times.databinding.DividerBinding
import com.github.times.databinding.TimesListBinding
import com.github.times.location.ZmanimLocations
import com.github.times.preference.SimpleZmanimPreferences
import com.github.times.preference.ZmanimPreferences
import com.github.view.ViewUtils.applyMaxWidth
import com.kosherjava.zmanim.hebrewcalendar.JewishDate
import java.util.Calendar

/**
 * Shows a list of halachic times (*zmanim*) for prayers.
 *
 * @author Moshe Waisberg
 */
open class ZmanimFragment<VH : ZmanViewHolder, A : ZmanimAdapter<VH>, P : ZmanimPopulater<A>> :
    Fragment() {

    private var context: Context? = null
    private var onClickListener: OnZmanItemClickListener? = null
    private var _binding: TimesListBinding? = null

    /**
     * The scroller.
     */
    private var scrollView: ScrollView? = null

    /**
     * The list.
     */
    protected var list: ViewGroup? = null

    /**
     * Provider for locations.
     */
    protected var locations: ZmanimLocations? = null

    /**
     * The preferences.
     */
    protected val preferences: ZmanimPreferences by lazy {
        val context = contextImpl
        if (context is ZmanimActivity) {
            context.preferences
        } else {
            SimpleZmanimPreferences(context)
        }
    }

    /**
     * The master item selected id.
     */
    private var highlightItemId = 0

    /**
     * The master item selected row.
     */
    private var highlightRow: View? = null

    /**
     * The master item background that is not selected.
     */
    private var unhighlightBackground: Drawable? = null
    private var paddingLeft = 0
    private var paddingTop = 0
    private var paddingRight = 0
    private var paddingBottom = 0

    /**
     * The adapter populater.
     */
    protected val populater: P by lazy { createPopulater(contextImpl) }

    /**
     * The adapter.
     */
    var adapter: A? = null
        private set

    private val contextImpl: Context get() = context ?: requireContext()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        this.context = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = contextImpl
        val app = context.applicationContext as ZmanimApplication
        locations = app.locations
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = TimesListBinding.inflate(inflater, container, false)
        _binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = _binding!!
        scrollView = binding.root
        list = binding.list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Create a new times adapter.
     *
     * @param context the context.
     * @return the adapter.
     */
    protected open fun createAdapter(context: Context): A? {
        return ZmanimAdapter<VH>(context, preferences, onClickListener) as A
    }

    /**
     * Create a new times populater.
     *
     * @param context the context.
     * @return the populater.
     */
    protected open fun createPopulater(context: Context): P {
        return ZmanimPopulater<A>(context, preferences) as P
    }

    /**
     * Populate the list with times.
     *
     * @param date the date.
     */
    open fun populateTimes(date: Calendar): A? {
        // Called before attached to activity?
        if (!isAdded) {
            return null
        }
        val locations = locations ?: return null
        val gloc = locations.geoLocation ?: return null
        // Have we been destroyed?
        val populater: P = populater
        populater.setCalendar(date)
        populater.setGeoLocation(gloc)
        populater.isInIsrael = locations.isInIsrael
        val adapter = createAdapter(contextImpl) ?: return null
        populater.populate(adapter, false)
        this.adapter = adapter
        val list = list ?: return adapter
        bindViews(list, adapter)
        return adapter
    }

    /**
     * Bind the times to a list.
     *
     * @param list    the list.
     * @param adapter the list adapter.
     */
    private fun bindViews(list: ViewGroup, adapter: A) {
        val context = list.context
        list.removeAllViews()
        // Ignore potential "IllegalArgumentException".
        val jcal = adapter.jewishCalendar ?: return
        var jewishDate: JewishDate? = jcal
        var dateHebrew = adapter.formatDate(context, jcal)
        val count = adapter.itemCount
        var position = 0
        var item: ZmanimItem?
        var viewHolder: ZmanViewHolder
        val timeViews = arrayOfNulls<View>(count)
        var jewishDatePrevious: JewishDate? = null
        if (count > 0) {
            item = adapter.getItem(position)
            if (item != null) {
                if (item.titleId == R.string.hour) {
                    viewHolder = adapter.onCreateViewHolder(list, 0)
                    timeViews[position] = viewHolder.time
                    bindView(list, viewHolder, item)
                    position++
                    if (position < count) {
                        item = adapter.getItem(position)
                    }
                }
                jewishDatePrevious = item?.jewishDate
            }
            bindViewGrouping(list, dateHebrew)
            val holidayToday = adapter.holidayToday
            val candlesToday = adapter.candlesTodayCount
            val holidayName = getName(context, holidayToday, candlesToday)
            bindViewGrouping(list, holidayName)

            // Sefirat HaOmer?
            val omerToday = adapter.dayOfOmerToday
            if (omerToday >= 1) {
                val omerLabel = adapter.formatOmer(context, omerToday)
                bindViewGrouping(list, omerLabel)
            }
            while (position < count) {
                item = adapter.getItem(position)
                if (item == null) {
                    position++
                    continue
                }
                jewishDate = item.jewishDate

                // Start of the next Hebrew day.
                if (jewishDate != null && (jewishDatePrevious == null || jewishDatePrevious != jewishDate)) {
                    jewishDatePrevious = jewishDate
                    jcal.setJewishDate(
                        jewishDate.jewishYear,
                        jewishDate.jewishMonth,
                        jewishDate.jewishDayOfMonth
                    )
                    dateHebrew = adapter.formatDate(context, jewishDate)
                    bindViewGrouping(list, dateHebrew)
                    val holidayTomorrow = adapter.holidayTomorrow
                    val candlesTomorrow = adapter.candlesCount
                    val holidayTomorrowName = getName(context, holidayTomorrow, candlesTomorrow)
                    bindViewGrouping(list, holidayTomorrowName)

                    // Sefirat HaOmer?
                    val omerTomorrow = adapter.dayOfOmerTomorrow
                    if (omerTomorrow >= 1) {
                        val omerLabel = adapter.formatOmer(context, omerTomorrow)
                        bindViewGrouping(list, omerLabel)
                    }
                }
                viewHolder = adapter.onCreateViewHolder(list, 0)
                timeViews[position] = viewHolder.time
                bindView(list, viewHolder, item)
                position++
            }
        }
        list.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                list.viewTreeObserver.removeOnGlobalLayoutListener(this)
                // Make all time texts same width.
                applyMaxWidth(timeViews)
            }
        })
        highlight(highlightItemId)
    }

    /**
     * Bind the time to a list.
     *
     * @param list       the list.
     * @param viewHolder the view holder.
     * @param item       the item.
     */
    private fun bindView(list: ViewGroup, viewHolder: ZmanViewHolder, item: ZmanimItem) {
        viewHolder.bind(item)
        bindDivider(list)
        list.addView(viewHolder.itemView)
    }

    private fun bindDivider(list: ViewGroup) {
        val inflater = LayoutInflater.from(list.context)
        DividerBinding.inflate(inflater, list, true)
    }

    /**
     * Bind the date group header to a list.
     *
     * @param list  the list.
     * @param label the formatted Hebrew date label.
     */
    private fun bindViewGrouping(list: ViewGroup, label: CharSequence?) {
        if (label.isNullOrEmpty()) return
        if (list.childCount > 0) {
            bindDivider(list)
        }
        val inflater = LayoutInflater.from(list.context)
        if (preferences.isDarkTheme) {
            val binding = DateGroupBinding.inflate(inflater, list, false)
            binding.title.text = label
            val row: View = binding.root
            list.addView(row)
        } else {
            val binding = DateGroupLightBinding.inflate(inflater, list, false)
            binding.title.text = label
            val row: View = binding.root
            list.addView(row)
        }
    }

    fun setOnClickListener(listener: OnZmanItemClickListener?) {
        onClickListener = listener
    }

    /**
     * The master item background that is selected.
     */
    private val selectedBackground: Drawable by lazy {
        ResourcesCompat.getDrawable(
            contextImpl.resources,
            R.drawable.list_selected,
            null
        )!!
    }

    /**
     * Mark the selected row as unselected.
     */
    fun unhighlight() {
        unhighlight(highlightRow)
    }

    /**
     * Mark the row as unselected.
     *
     * @param view the row view.
     */
    private fun unhighlight(view: View?) {
        highlightItemId = 0
        highlightRow = null
        var bg = unhighlightBackground
        if (view == null || bg == null) return

        // Workaround for Samsung ICS bug where the highlight lingers.
        if (bg is StateListDrawable) bg = bg.getConstantState()!!.newDrawable()
        view.background = bg
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        unhighlightBackground = null
    }

    /**
     * Mark the row as selected.
     *
     * @param itemId the row id.
     */
    fun highlight(itemId: Int) {
        highlightItemId = itemId
        highlightRow = null
        if (itemId == 0) {
            return
        }
        // Find the view that matches the item id (the view that was clicked).
        val list = list ?: return
        var view: View? = null
        var child: View
        var item: ZmanimItem?
        val count = list.childCount
        for (i in 0 until count) {
            child = list.getChildAt(i)
            // Maybe row divider?
            item = child.getTag(TAG_ITEM) as ZmanimItem? ?: continue
            if (item.titleId == itemId) {
                view = child
                break
            }
        }
        if (view == null) return
        unhighlightBackground = view.background
        paddingLeft = view.paddingLeft
        paddingTop = view.paddingTop
        paddingRight = view.paddingRight
        paddingBottom = view.paddingBottom
        view.background = selectedBackground
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        highlightRow = view
        // Scroll to the row
        scrollView?.requestChildFocus(list, view)
    }

    /**
     * Set the view's visibility.
     *
     * @param visibility the visibility.
     * @see View.VISIBLE
     * @see View.INVISIBLE
     * @see View.GONE
     */
    fun setVisibility(visibility: Int) {
        view?.visibility = visibility
    }
}