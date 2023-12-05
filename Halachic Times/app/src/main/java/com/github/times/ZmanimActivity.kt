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

import android.annotation.TargetApi
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.format.DateUtils
import android.view.GestureDetector
import android.view.GestureDetector.OnDoubleTapListener
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.DatePicker
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.app.LocaleCallbacks
import com.github.app.LocaleHelper
import com.github.app.SimpleThemeCallbacks
import com.github.app.ThemeCallbacks
import com.github.app.TodayDatePickerDialog
import com.github.times.compass.CompassActivity
import com.github.times.databinding.TimesBinding
import com.github.times.location.LocatedActivity
import com.github.times.location.LocationActivity
import com.github.times.preference.SimpleZmanimPreferences
import com.github.times.preference.ZmanimPreferenceActivity
import com.github.times.preference.ZmanimPreferences
import com.github.times.remind.ZmanimReminder
import com.github.times.remind.ZmanimReminder.Companion.checkPermissions
import com.github.times.remind.ZmanimReminderService.Companion.enqueueWork
import com.github.util.LocaleUtils.isLocaleRTL
import com.github.view.animation.ConstraintLayoutWeightAnimation
import java.lang.ref.WeakReference
import java.util.Calendar
import kotlin.math.abs

/**
 * Shows a list of halachic times (*zmanim*) for prayers.
 *
 * @author Moshe Waisberg
 */
class ZmanimActivity : LocatedActivity<ZmanimPreferences>(),
    OnDateSetListener,
    View.OnClickListener,
    GestureDetector.OnGestureListener,
    OnDoubleTapListener,
    AnimationListener,
    OnZmanItemClickListener {
    /**
     * The date.
     */
    private val calendar = Calendar.getInstance()

    /**
     * The location header Gregorian date.
     */
    private lateinit var headerGregorianDate: TextView

    /**
     * The button to navigate to yesterday.
     */
    private lateinit var buttonYesterday: View

    /**
     * The button to navigate to tomorrow.
     */
    private lateinit var buttonTomorrow: View

    /**
     * The date picker.
     */
    private var datePicker: DatePickerDialog? = null

    /**
     * The master fragment.
     */
    private lateinit var masterFragment: ZmanimFragment<ZmanItemViewHolder, ZmanimAdapter<ZmanItemViewHolder>, ZmanimPopulater<ZmanimAdapter<ZmanItemViewHolder>>>

    /**
     * The details fragment switcher.
     */
    private lateinit var detailsFragmentSwitcher: ViewSwitcher

    /**
     * The details fragment.
     */
    private lateinit var detailsListFragment: ZmanimDetailsFragment<ZmanimDetailsAdapter, ZmanimDetailsPopulater<ZmanimDetailsAdapter>>

    /**
     * The candles fragment.
     */
    private lateinit var candlesFragment: CandlesFragment

    /**
     * Is master fragment switched with details fragment?
     */
    private var viewSwitcher: ViewSwitcher? = null

    /**
     * The master item selected id.
     */
    private var selectedId = 0

    /**
     * The gesture detector.
     */
    private lateinit var gestureDetector: GestureDetector

    /**
     * Is locale RTL?
     */
    private var localeRTL = false

    /**
     * Slide left-to-right animation.
     */
    private var slideLeftToRight: Animation? = null

    /**
     * Slide right-to-left animation.
     */
    private var slideRightToLeft: Animation? = null

    /**
     * Grow details animation.
     */
    private var detailsGrow: Animation? = null

    /**
     * Shrink details animation.
     */
    private var detailsShrink: Animation? = null

    /**
     * Hide navigation bar animation.
     */
    private var hideNavigation: Animation? = null

    /**
     * Show navigation bar animation.
     */
    private var showNavigation: Animation? = null
    private val handler = ActivityHandler(this)
    private lateinit var localeCallbacks: LocaleCallbacks<ZmanimPreferences>

    /**
     * The handler.
     */
    private class ActivityHandler(activity: ZmanimActivity) : Handler(Looper.getMainLooper()) {
        private val activityWeakReference = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            val activity = activityWeakReference.get() ?: return
            val context: Context = activity
            when (msg.what) {
                WHAT_TOGGLE_DETAILS -> activity.toggleDetails(msg.arg1)
                WHAT_COMPASS -> activity.startActivity(Intent(context, CompassActivity::class.java))
                WHAT_DATE -> activity.chooseDate()
                WHAT_LOCATION -> activity.startLocations()
                WHAT_SETTINGS -> activity.startActivity(
                    Intent(context, ZmanimPreferenceActivity::class.java)
                )

                WHAT_TODAY -> activity.setDate(System.currentTimeMillis())
                WHAT_CANCEL_REMINDERS -> activity.cancelReminders()
                WHAT_UPDATE_REMINDERS -> activity.updateReminders()
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        localeCallbacks = LocaleHelper(newBase)
        val context = localeCallbacks.attachBaseContext(newBase)
        super.attachBaseContext(context)
        applyOverrideConfiguration(context.resources.configuration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initPermissions()
        }
        handleIntent(intent)
    }

    override fun onPreCreate() {
        super.onPreCreate()
        localeCallbacks.onPreCreate(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        var date = intent.getLongExtra(EXTRA_DATE, ZmanimItem.NEVER)
        if (date == ZmanimItem.NEVER) {
            date = intent.getLongExtra(EXTRA_TIME, ZmanimItem.NEVER)
            if (date == ZmanimItem.NEVER) {
                date = System.currentTimeMillis()
            }
        }
        setDate(date)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(EXTRA_DATE, calendar.timeInMillis)
        outState.putInt(PARAMETER_DETAILS, selectedId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setDate(savedInstanceState.getLong(EXTRA_DATE))
        selectedId = savedInstanceState.getInt(PARAMETER_DETAILS, selectedId)
    }

    override fun onResume() {
        super.onResume()
        handler.sendEmptyMessage(WHAT_CANCEL_REMINDERS)
        val itemId = selectedId
        if (itemId != 0) {
            // We need to wait for the list rows to get their default
            // backgrounds before we can highlight any row.
            val msg = handler.obtainMessage(WHAT_TOGGLE_DETAILS, itemId, 0)
            handler.sendMessageDelayed(msg, DateUtils.SECOND_IN_MILLIS)
        }
    }

    override fun onPause() {
        val itemId = selectedId
        if (itemId != 0) {
            hideDetails()
            selectedId = itemId
        }
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        handler.sendEmptyMessage(WHAT_UPDATE_REMINDERS)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeMessages(WHAT_TOGGLE_DETAILS)
        handler.removeMessages(WHAT_COMPASS)
        handler.removeMessages(WHAT_DATE)
        handler.removeMessages(WHAT_LOCATION)
        handler.removeMessages(WHAT_SETTINGS)
        handler.removeMessages(WHAT_TODAY)
    }

    /**
     * Initialise.
     */
    private fun init() {
        val context: Context = this
        val binding = TimesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        localeRTL = isLocaleRTL(context)
        gestureDetector = GestureDetector(context, this, handler).apply {
            setIsLongpressEnabled(false)
        }
        val fragmentManager = supportFragmentManager
        masterFragment =
            fragmentManager.findFragmentById(R.id.list_fragment) as ZmanimFragment<ZmanItemViewHolder, ZmanimAdapter<ZmanItemViewHolder>, ZmanimPopulater<ZmanimAdapter<ZmanItemViewHolder>>>
        masterFragment.setOnClickListener(this)
        detailsFragmentSwitcher = binding.detailsFragment
        detailsListFragment =
            fragmentManager.findFragmentById(R.id.details_list_fragment) as ZmanimDetailsFragment<ZmanimDetailsAdapter, ZmanimDetailsPopulater<ZmanimDetailsAdapter>>
        candlesFragment = fragmentManager.findFragmentById(R.id.candles_fragment) as CandlesFragment
        viewSwitcher = binding.frameFragments
        if (viewSwitcher != null) {
            val inAnim = AnimationUtils.makeInAnimation(context, false)
            viewSwitcher!!.inAnimation = inAnim
            val outAnim = AnimationUtils.makeOutAnimation(context, true)
            viewSwitcher!!.outAnimation = outAnim
        } else {
            val detailsFragmentSwitcherLayoutParams =
                detailsFragmentSwitcher.layoutParams as ConstraintLayout.LayoutParams
            val detailsWeight = detailsFragmentSwitcherLayoutParams.horizontalWeight
            val detailsAnimTime =
                context.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
            detailsGrow =
                ConstraintLayoutWeightAnimation(detailsFragmentSwitcher, 0f, detailsWeight).apply {
                    duration = detailsAnimTime
                }
            detailsShrink =
                ConstraintLayoutWeightAnimation(detailsFragmentSwitcher, detailsWeight, 0f).apply {
                    duration = detailsAnimTime
                }
        }
        val header = binding.header
        header.root.setOnClickListener(this)
        headerGregorianDate = header.dateGregorian
        headerLocation = header.headerLocation.coordinates
        headerAddress = header.headerLocation.address
        buttonYesterday = header.navYesterday
        buttonYesterday.setOnClickListener(this)
        buttonTomorrow = header.navTomorrow
        buttonTomorrow.setOnClickListener(this)
        slideRightToLeft = AnimationUtils.loadAnimation(
            context,
            com.github.times.common.R.anim.slide_right_to_left
        )
        slideLeftToRight = AnimationUtils.loadAnimation(
            context,
            com.github.times.common.R.anim.slide_left_to_right
        )
        hideNavigation = AnimationUtils.loadAnimation(context, R.anim.hide_nav).apply {
            setAnimationListener(this@ZmanimActivity)
        }
        showNavigation = AnimationUtils.loadAnimation(context, R.anim.show_nav).apply {
            setAnimationListener(this@ZmanimActivity)
        }
    }

    /**
     * Set the date for the list.
     *
     * @param date the date, in milliseconds.
     */
    private fun setDate(date: Long) {
        if (date <= DATE_MIN) {
            // Bad Gregorian dates cause problems for the Jewish dates.
            return
        }
        calendar.apply {
            this.timeZone = timeZone
            this.timeInMillis = date
        }
        showDate()
        scheduleNextDay()
        populateFragments(calendar)
    }

    /**
     * Set the date for the list.
     *
     * @param year        the year.
     * @param monthOfYear the month of the year.
     * @param dayOfMonth  the day of the month.
     */
    private fun setDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        if (year <= 1 || monthOfYear < 0 || dayOfMonth <= 0) {
            // Bad Gregorian dates cause problems for the Jewish dates.
            return
        }
        calendar.apply {
            this.timeZone = timeZone
            this.timeInMillis = System.currentTimeMillis()
            this[Calendar.YEAR] = year
            this[Calendar.MONTH] = monthOfYear
            this[Calendar.DAY_OF_MONTH] = dayOfMonth
        }
        showDate()
        scheduleNextDay()
        populateFragments(calendar)
    }

    private fun showDate() {
        val textGregorian = headerGregorianDate
        // Have we been destroyed?
        val dateGregorian: CharSequence = DateUtils.formatDateTime(
            this,
            calendar.timeInMillis,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_WEEKDAY
        )
        textGregorian.text = dateGregorian
    }

    override fun createUpdateLocationRunnable(location: Location): Runnable {
        return Runnable {
            bindHeader(location)
            populateFragments(calendar)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.zmanim, menu)
        val pm = packageManager
        if (!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS)) menu.removeItem(R.id.menu_compass)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_compass -> {
                handler.sendEmptyMessage(WHAT_COMPASS)
                true
            }

            R.id.menu_date -> {
                handler.sendEmptyMessage(WHAT_DATE)
                true
            }

            R.id.menu_location -> {
                handler.sendEmptyMessage(WHAT_LOCATION)
                true
            }

            R.id.menu_settings -> {
                handler.sendEmptyMessage(WHAT_SETTINGS)
                true
            }

            R.id.menu_today -> {
                handler.sendEmptyMessage(WHAT_TODAY)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun chooseDate() {
        val context: Context = this
        val calendar = calendar
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DAY_OF_MONTH]
        var datePicker = datePicker
        if (datePicker == null) {
            datePicker = TodayDatePickerDialog(context, this, year, month, day)
            this.datePicker = datePicker
        } else {
            datePicker.updateDate(year, month, day)
        }
        datePicker.show()
    }

    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        setDate(year, monthOfYear, dayOfMonth)
    }

    override fun createBindHeaderRunnable(): Runnable {
        return Runnable { bindHeader() }
    }

    override fun onZmanClick(item: ZmanimItem) {
        toggleDetails(item)
    }

    /**
     * Show/hide the details list.
     *
     * @param item the master item that was clicked.
     */
    private fun toggleDetails(item: ZmanimItem) {
        toggleDetails(item.titleId)
    }

    /**
     * Show/hide the details list.
     *
     * @param itemId the master item id.
     */
    private fun toggleDetails(itemId: Int) {
        if (!hasDetails(itemId)) return
        if (viewSwitcher != null) {
            masterFragment.unhighlight()
            if (itemId == R.string.candles) {
                candlesFragment.populateTimes(calendar)
                detailsFragmentSwitcher.displayedChild = CHILD_DETAILS_CANDLES
            } else {
                detailsListFragment.populateTimes(calendar, itemId)
                detailsFragmentSwitcher.displayedChild = CHILD_DETAILS_LIST
            }
            masterFragment.highlight(itemId)
            if (isDetailsShowing) hideDetails() else showDetails()
            selectedId = itemId
        } else if (selectedId == itemId && isDetailsShowing) {
            hideDetails()
            masterFragment.unhighlight()
        } else {
            masterFragment.unhighlight()
            if (itemId == R.string.candles) {
                candlesFragment.populateTimes(calendar)
                detailsFragmentSwitcher.displayedChild = CHILD_DETAILS_CANDLES
            } else {
                detailsListFragment.populateTimes(calendar, itemId)
                detailsFragmentSwitcher.displayedChild = CHILD_DETAILS_LIST
            }
            masterFragment.highlight(itemId)
            if (!isDetailsShowing) showDetails()
            selectedId = itemId
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        masterFragment.unhighlight()
        if (isDetailsShowing) {
            hideDetails()
            return
        }
        finishAfterTransition()
    }

    override fun onClick(view: View) {
        val id = view.id
        if (id == R.id.header) {
            toggleNavigationView()
        } else if (id == R.id.nav_yesterday) {
            navigateYesterday()
        } else if (id == R.id.nav_tomorrow) {
            navigateTomorrow()
        }
    }

    private fun toggleNavigationView() {
        var anim = buttonYesterday.animation
        if (anim == null || anim.hasEnded()) {
            if (buttonYesterday.visibility == View.VISIBLE) {
                buttonYesterday.startAnimation(hideNavigation)
            } else {
                buttonYesterday.startAnimation(showNavigation)
            }
        }
        anim = buttonTomorrow.animation
        if (anim == null || anim.hasEnded()) {
            if (buttonTomorrow.visibility == View.VISIBLE) {
                buttonTomorrow.startAnimation(hideNavigation)
            } else {
                buttonTomorrow.startAnimation(showNavigation)
            }
        }
    }

    protected fun hideDetails() {
        val viewSwitcher = viewSwitcher
        if (viewSwitcher != null) {
            viewSwitcher.displayedChild = CHILD_MAIN

            // Not enough to hide the details switcher = must also hide its
            // children otherwise visible when sliding dates.
            detailsListFragment.setVisibility(View.INVISIBLE)
            candlesFragment.setVisibility(false)
        } else if (detailsShrink != null) {
            detailsFragmentSwitcher.startAnimation(detailsShrink)
        }
        selectedId = 0
    }

    protected fun showDetails() {
        val viewSwitcher = viewSwitcher
        if (viewSwitcher != null) {
            viewSwitcher.displayedChild = CHILD_DETAILS
            detailsListFragment.setVisibility(View.VISIBLE)
            candlesFragment.setVisibility(true)
        } else if (detailsGrow != null) {
            detailsFragmentSwitcher.startAnimation(detailsGrow)
        }
    }

    private val isDetailsShowing: Boolean
        get() {
            if (detailsFragmentSwitcher.visibility != View.VISIBLE) return false
            val viewSwitcher = viewSwitcher
            if (viewSwitcher == null) {
                val lp = detailsFragmentSwitcher.layoutParams as ConstraintLayout.LayoutParams
                return lp.horizontalWeight > 0
            }
            return viewSwitcher.displayedChild == CHILD_DETAILS
        }

    override fun onDown(event: MotionEvent): Boolean = false

    override fun onShowPress(event: MotionEvent) = Unit

    override fun onSingleTapUp(event: MotionEvent): Boolean = false

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean = false

    override fun onLongPress(event: MotionEvent) = Unit

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        // Go to date? Also don't go to date accidentally while scrolling vertically.
        if (abs(velocityX) > abs(velocityY) * 2) {
            if (velocityX < 0) {
                if (localeRTL) {
                    navigateYesterday()
                } else {
                    navigateTomorrow()
                }
            } else if (localeRTL) {
                navigateTomorrow()
            } else {
                navigateYesterday()
            }
            return true
        }
        return false
    }

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean = false

    override fun onDoubleTap(event: MotionEvent): Boolean = false

    override fun onDoubleTapEvent(event: MotionEvent): Boolean = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gestureDetector.onTouchEvent(event)) {
            return true
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        localeRTL = isLocaleRTL(newConfig)
    }

    /**
     * Slide the view in from right to left.
     *
     * @param view the view to animate.
     */
    private fun slideLeft(view: View) {
        view.startAnimation(slideRightToLeft)
    }

    /**
     * Slide the view in from left to right.
     *
     * @param view the view to animate.
     */
    private fun slideRight(view: View) {
        view.startAnimation(slideLeftToRight)
    }

    private fun populateFragments(date: Calendar) {
        masterFragment.populateTimes(date)
        detailsListFragment.populateTimes(date)
        candlesFragment.populateTimes(date)
        if (!isValidDetailsShowing) {
            masterFragment.unhighlight()
            hideDetails()
        }
    }

    /**
     * Is the details list populated for a valid date?<br></br>
     * For example, candle lighting for a Friday or Erev Chag.
     *
     * @return `true` if valid.
     */
    private val isValidDetailsShowing: Boolean
        get() {
            if (!isDetailsShowing) return true
            if (candlesFragment.isVisible) {
                return candlesFragment.candlesCount != 0
            }
            if (detailsListFragment.isVisible) {
                val masterId = detailsListFragment.masterId
                val masterAdapter = masterFragment.adapter
                if (masterAdapter != null) {
                    val count = masterAdapter.itemCount
                    var item: ZmanimItem?
                    for (i in 0 until count) {
                        item = masterAdapter.getItem(i)
                        if (item != null && item.titleId == masterId) {
                            return true
                        }
                    }
                }
                return false
            }
            return false
        }

    private fun navigateYesterday() {
        val masterView = masterFragment.view ?: return
        if (localeRTL) {
            slideLeft(masterView)
            slideLeft(detailsFragmentSwitcher)
        } else {
            slideRight(masterView)
            slideRight(detailsFragmentSwitcher)
        }
        val calendar = calendar
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        setDate(calendar.timeInMillis)
    }

    private fun navigateTomorrow() {
        val masterView = masterFragment.view ?: return
        if (localeRTL) {
            slideRight(masterView)
            slideRight(detailsFragmentSwitcher)
        } else {
            slideLeft(masterView)
            slideLeft(detailsFragmentSwitcher)
        }
        val calendar = calendar
        calendar.add(Calendar.DAY_OF_MONTH, +1)
        setDate(calendar.timeInMillis)
    }

    override fun onAnimationStart(animation: Animation) = Unit

    override fun onAnimationEnd(animation: Animation) {
        if (animation === hideNavigation) {
            buttonYesterday.visibility = View.INVISIBLE
            buttonYesterday.isEnabled = false
        } else if (animation === showNavigation) {
            buttonYesterday.visibility = View.VISIBLE
            buttonYesterday.isEnabled = true
        }
        if (animation === hideNavigation) {
            buttonTomorrow.visibility = View.INVISIBLE
            buttonTomorrow.isEnabled = false
        } else if (animation === showNavigation) {
            buttonTomorrow.visibility = View.VISIBLE
            buttonTomorrow.isEnabled = true
        }
    }

    override fun onAnimationRepeat(animation: Animation) = Unit

    /**
     * Is the item a master with detail times?
     *
     * @param itemId the item id.
     * @return `true` if the item has details.
     */
    private fun hasDetails(itemId: Int): Boolean {
        return itemId != 0
            && itemId != R.string.fast_begins
            && itemId != R.string.fast_ends
            && itemId != R.string.molad
    }

    private fun updateReminders() {
        val context: Context = this
        val intent = Intent(ZmanimReminder.ACTION_UPDATE)
        enqueueWork(context, intent)
    }

    private fun cancelReminders() {
        val context: Context = this
        val intent = Intent(ZmanimReminder.ACTION_CANCEL)
        enqueueWork(context, intent)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (gestureDetector.onTouchEvent(event)) {
            return true
        }
        return super.dispatchTouchEvent(event)
    }

    override val locationActivityClass: Class<out Activity>
        get() = LocationActivity::class.java

    /**
     * Refresh the list at midnight for the next civil day.
     */
    private fun scheduleNextDay() {
        val tomorrow = Calendar.getInstance()
        if (!isToday(calendar, tomorrow)) {
            return
        }
        val now = tomorrow.timeInMillis
        tomorrow.add(Calendar.DAY_OF_MONTH, 1)
        tomorrow[Calendar.HOUR_OF_DAY] = 0
        tomorrow[Calendar.MINUTE] = 0
        tomorrow[Calendar.SECOND] = 0
        tomorrow[Calendar.MILLISECOND] = 0
        handler.sendEmptyMessageDelayed(WHAT_TODAY, tomorrow.timeInMillis - now)
    }

    private fun isToday(time: Calendar, today: Calendar): Boolean {
        val whenYear = time[Calendar.YEAR]
        val whenMonth = time[Calendar.MONTH]
        val whenDay = time[Calendar.DAY_OF_MONTH]
        val todayYear = today[Calendar.YEAR]
        val todayMonth = today[Calendar.MONTH]
        val todayDay = today[Calendar.DAY_OF_MONTH]
        return whenYear == todayYear && whenMonth == todayMonth && whenDay == todayDay
    }

    val preferences: ZmanimPreferences by lazy { SimpleZmanimPreferences(this) }

    override fun createThemeCallbacks(context: Context): ThemeCallbacks<ZmanimPreferences> {
        return SimpleThemeCallbacks(context, preferences)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun initPermissions() {
        checkPermissions(this)
    }

    companion object {
        /**
         * The date parameter.
         */
        const val EXTRA_DATE = "date"

        /**
         * The time parameter.
         */
        const val EXTRA_TIME = "time"

        /**
         * The details list parameter.
         */
        private const val PARAMETER_DETAILS = "details"

        private const val WHAT_TOGGLE_DETAILS = 0
        private const val WHAT_COMPASS = 1
        private const val WHAT_DATE = 2
        private const val WHAT_LOCATION = 3
        private const val WHAT_SETTINGS = 4
        private const val WHAT_TODAY = 5
        private const val WHAT_CANCEL_REMINDERS = 6
        private const val WHAT_UPDATE_REMINDERS = 7

        private const val CHILD_MAIN = 0
        private const val CHILD_DETAILS = 1
        private const val CHILD_DETAILS_LIST = 0
        private const val CHILD_DETAILS_CANDLES = 1

        private const val DATE_MIN = -62167359300528L // (-1970 + 1) * DAY_IN_MILLIS * 365.25
    }
}