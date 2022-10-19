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

import static android.os.Build.VERSION;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY;
import static android.text.format.DateUtils.FORMAT_SHOW_YEAR;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static android.text.format.DateUtils.formatDateTime;
import static com.github.times.ZmanimItem.NEVER;
import static com.github.util.LocaleUtils.isLocaleRTL;
import static java.lang.System.currentTimeMillis;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;

import com.github.app.LocaleCallbacks;
import com.github.app.LocaleHelper;
import com.github.app.SimpleThemeCallbacks;
import com.github.app.ThemeCallbacks;
import com.github.app.TodayDatePickerDialog;
import com.github.content.ContextResourcesWrapper;
import com.github.times.compass.CompassActivity;
import com.github.times.content.res.ZmanimResources;
import com.github.times.location.LocatedActivity;
import com.github.times.location.LocationActivity;
import com.github.times.preference.SimpleZmanimPreferences;
import com.github.times.preference.ZmanimPreferenceActivity;
import com.github.times.preference.ZmanimPreferences;
import com.github.times.remind.ZmanimReminder;
import com.github.times.remind.ZmanimReminderService;
import com.github.view.animation.ConstraintLayoutWeightAnimation;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Calendar;

/**
 * Shows a list of halachic times (<em>zmanim</em>) for prayers.
 *
 * @author Moshe Waisberg
 */
public class ZmanimActivity extends LocatedActivity<ZmanimPreferences> implements
    OnDateSetListener,
    View.OnClickListener,
    OnGestureListener,
    GestureDetector.OnDoubleTapListener,
    Animation.AnimationListener {

    /**
     * The date parameter.
     */
    public static final String EXTRA_DATE = "date";
    /**
     * The time parameter.
     */
    public static final String EXTRA_TIME = "time";
    /**
     * The details list parameter.
     */
    private static final String PARAMETER_DETAILS = "details";

    private static final int WHAT_TOGGLE_DETAILS = 0;
    private static final int WHAT_COMPASS = 1;
    private static final int WHAT_DATE = 2;
    private static final int WHAT_LOCATION = 3;
    private static final int WHAT_SETTINGS = 4;
    private static final int WHAT_TODAY = 5;
    private static final int WHAT_CANCEL_REMINDERS = 6;
    private static final int WHAT_UPDATE_REMINDERS = 7;

    private static final int CHILD_MAIN = 0;
    private static final int CHILD_DETAILS = 1;

    private static final int CHILD_DETAILS_LIST = 0;
    private static final int CHILD_DETAILS_CANDLES = 1;

    private static final long DATE_MIN = -62167359300528L;// (-1970 + 1) * DAY_IN_MILLIS * 365.25

    /**
     * The date.
     */
    private final Calendar calendar = Calendar.getInstance();
    /**
     * The location header Gregorian date.
     */
    private TextView headerGregorianDate;
    /**
     * The button to navigate to yesterday.
     */
    private View buttonYesterday;
    /**
     * The button to navigate to tomorrow.
     */
    private View buttonTomorrow;
    /**
     * The preferences.
     */
    private ZmanimPreferences preferences;
    /**
     * The date picker.
     */
    private DatePickerDialog datePicker;
    /**
     * The master fragment.
     */
    private ZmanimFragment<ZmanimAdapter, ZmanimPopulater<ZmanimAdapter>> masterFragment;
    /**
     * The details fragment switcher.
     */
    private ViewSwitcher detailsFragmentSwitcher;
    /**
     * The details fragment.
     */
    private ZmanimDetailsFragment<ZmanimDetailsAdapter, ZmanimDetailsPopulater<ZmanimDetailsAdapter>> detailsListFragment;
    /**
     * The candles fragment.
     */
    private CandlesFragment candlesFragment;
    /**
     * Is master fragment switched with details fragment?
     */
    private ViewSwitcher viewSwitcher;
    /**
     * The master item selected id.
     */
    private int selectedId;
    /**
     * The gesture detector.
     */
    private GestureDetector gestureDetector;
    /**
     * Is locale RTL?
     */
    private boolean localeRTL;
    /**
     * Slide left-to-right animation.
     */
    private Animation slideLeftToRight;
    /**
     * Slide right-to-left animation.
     */
    private Animation slideRightToLeft;
    /**
     * Grow details animation.
     */
    private Animation detailsGrow;
    /**
     * Shrink details animation.
     */
    private Animation detailsShrink;
    /**
     * Hide navigation bar animation.
     */
    private Animation hideNavigation;
    /**
     * Show navigation bar animation.
     */
    private Animation showNavigation;
    private final ActivityHandler handler = new ActivityHandler(this);
    private LocaleCallbacks<ZmanimPreferences> localeCallbacks;

    /**
     * The handler.
     */
    private static class ActivityHandler extends Handler {

        private final WeakReference<ZmanimActivity> activityWeakReference;

        public ActivityHandler(ZmanimActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NotNull Message msg) {
            final ZmanimActivity activity = activityWeakReference.get();
            if (activity == null) {
                return;
            }
            Context context = activity;

            switch (msg.what) {
                case WHAT_TOGGLE_DETAILS:
                    activity.toggleDetails(msg.arg1);
                    break;
                case WHAT_COMPASS:
                    activity.startActivity(new Intent(context, CompassActivity.class));
                    break;
                case WHAT_DATE:
                    final Calendar calendar = activity.calendar;
                    final int year = calendar.get(Calendar.YEAR);
                    final int month = calendar.get(Calendar.MONTH);
                    final int day = calendar.get(Calendar.DAY_OF_MONTH);
                    if (activity.datePicker == null) {
                        Resources res = context.getResources();
                        res = new ZmanimResources(res.getAssets(), res.getDisplayMetrics(), res.getConfiguration());
                        context = new ContextResourcesWrapper(context, res);
                        activity.datePicker = new TodayDatePickerDialog(context, activity, year, month, day);
                    } else {
                        activity.datePicker.updateDate(year, month, day);
                    }
                    activity.datePicker.show();
                    break;
                case WHAT_LOCATION:
                    activity.startLocations();
                    break;
                case WHAT_SETTINGS:
                    activity.startActivity(new Intent(context, ZmanimPreferenceActivity.class));
                    break;
                case WHAT_TODAY:
                    activity.setDate(currentTimeMillis());
                    activity.populateFragments(activity.calendar);
                    break;
                case WHAT_CANCEL_REMINDERS:
                    activity.cancelReminders();
                    break;
                case WHAT_UPDATE_REMINDERS:
                    activity.updateReminders();
                    break;
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        this.localeCallbacks = new LocaleHelper<>(newBase);
        Context context = localeCallbacks.attachBaseContext(newBase);
        super.attachBaseContext(context);

        applyOverrideConfiguration(context.getResources().getConfiguration());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        initLocation();
        if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            initNotificationPermissions();
        }
        handleIntent(getIntent());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        localeCallbacks.onCreate(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    protected void handleIntent(Intent intent) {
        long date = intent.getLongExtra(EXTRA_DATE, NEVER);
        if (date == NEVER) {
            date = intent.getLongExtra(EXTRA_TIME, NEVER);
            if (date == NEVER) {
                date = currentTimeMillis();
            }
        }
        setDate(date);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(EXTRA_DATE, calendar.getTimeInMillis());
        outState.putInt(PARAMETER_DETAILS, selectedId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        setDate(savedInstanceState.getLong(EXTRA_DATE));
        selectedId = savedInstanceState.getInt(PARAMETER_DETAILS, selectedId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.sendEmptyMessage(WHAT_CANCEL_REMINDERS);
        int itemId = selectedId;
        if (itemId != 0) {
            // We need to wait for the list rows to get their default
            // backgrounds before we can highlight any row.
            Message msg = handler.obtainMessage(WHAT_TOGGLE_DETAILS, itemId, 0);
            handler.sendMessageDelayed(msg, SECOND_IN_MILLIS);
        }
    }

    @Override
    protected void onPause() {
        int itemId = selectedId;
        if (itemId != 0) {
            hideDetails();
            selectedId = itemId;
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        handler.sendEmptyMessage(WHAT_UPDATE_REMINDERS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        handler.removeMessages(WHAT_TOGGLE_DETAILS);
        handler.removeMessages(WHAT_COMPASS);
        handler.removeMessages(WHAT_DATE);
        handler.removeMessages(WHAT_LOCATION);
        handler.removeMessages(WHAT_SETTINGS);
        handler.removeMessages(WHAT_TODAY);
    }

    /**
     * Initialise.
     */
    @SuppressWarnings({"unchecked", "InflateParams"})
    private void init() {
        final Context context = this;

        setContentView(R.layout.times);
        View view = getWindow().getDecorView();

        gestureDetector = new GestureDetector(context, this, handler);
        gestureDetector.setIsLongpressEnabled(false);

        FragmentManager fragmentManager = getSupportFragmentManager();
        masterFragment = (ZmanimFragment<ZmanimAdapter, ZmanimPopulater<ZmanimAdapter>>) fragmentManager.findFragmentById(R.id.list_fragment);
        masterFragment.setOnClickListener(this);
        detailsFragmentSwitcher = view.findViewById(R.id.details_fragment);
        detailsListFragment = (ZmanimDetailsFragment<ZmanimDetailsAdapter, ZmanimDetailsPopulater<ZmanimDetailsAdapter>>) fragmentManager.findFragmentById(R.id.details_list_fragment);
        candlesFragment = (CandlesFragment) fragmentManager.findFragmentById(R.id.candles_fragment);

        viewSwitcher = view.findViewById(R.id.frame_fragments);
        if (viewSwitcher != null) {
            Animation inAnim = AnimationUtils.makeInAnimation(context, false);
            viewSwitcher.setInAnimation(inAnim);
            Animation outAnim = AnimationUtils.makeOutAnimation(context, true);
            viewSwitcher.setOutAnimation(outAnim);
        } else {
            ConstraintLayout.LayoutParams detailsFragmentSwitcherLayoutParams = (ConstraintLayout.LayoutParams) detailsFragmentSwitcher.getLayoutParams();
            float detailsWeight = detailsFragmentSwitcherLayoutParams.horizontalWeight;
            long detailsAnimTime = context.getResources().getInteger(android.R.integer.config_mediumAnimTime);
            detailsGrow = new ConstraintLayoutWeightAnimation(detailsFragmentSwitcher, 0f, detailsWeight);
            detailsGrow.setDuration(detailsAnimTime);
            detailsShrink = new ConstraintLayoutWeightAnimation(detailsFragmentSwitcher, detailsWeight, 0f);
            detailsShrink.setDuration(detailsAnimTime);
        }

        View header = view.findViewById(R.id.header);
        header.setOnClickListener(this);
        headerGregorianDate = header.findViewById(R.id.date_gregorian);
        headerLocation = header.findViewById(R.id.coordinates);
        headerAddress = header.findViewById(R.id.address);

        buttonYesterday = header.findViewById(R.id.nav_yesterday);
        buttonYesterday.setOnClickListener(this);
        buttonTomorrow = header.findViewById(R.id.nav_tomorrow);
        buttonTomorrow.setOnClickListener(this);

        slideRightToLeft = AnimationUtils.loadAnimation(context, R.anim.slide_right_to_left);
        slideLeftToRight = AnimationUtils.loadAnimation(context, R.anim.slide_left_to_right);

        hideNavigation = AnimationUtils.loadAnimation(context, R.anim.hide_nav);
        hideNavigation.setAnimationListener(this);
        showNavigation = AnimationUtils.loadAnimation(context, R.anim.show_nav);
        showNavigation.setAnimationListener(this);
    }

    /**
     * Initialise the location providers.
     */
    private void initLocation() {
        localeRTL = isLocaleRTL(this);
    }

    /**
     * Set the date for the list.
     *
     * @param date the date, in milliseconds.
     */
    private void setDate(long date) {
        if (date <= DATE_MIN) {
            // Bad Gregorian dates cause problems for the Jewish dates.
            return;
        }
        calendar.setTimeZone(getTimeZone());
        calendar.setTimeInMillis(date);

        showDate();
        scheduleNextDay();
    }

    /**
     * Set the date for the list.
     *
     * @param year        the year.
     * @param monthOfYear the month of the year.
     * @param dayOfMonth  the day of the month.
     */
    private void setDate(int year, int monthOfYear, int dayOfMonth) {
        if ((year <= 1) || (monthOfYear < 0) || (dayOfMonth <= 0)) {
            // Bad Gregorian dates cause problems for the Jewish dates.
            return;
        }
        calendar.setTimeZone(getTimeZone());
        calendar.setTimeInMillis(currentTimeMillis());
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        showDate();
        scheduleNextDay();
    }

    private void showDate() {
        TextView textGregorian = this.headerGregorianDate;
        // Have we been destroyed?
        if (textGregorian == null)
            return;
        CharSequence dateGregorian = formatDateTime(this, calendar.getTimeInMillis(), FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR | FORMAT_SHOW_WEEKDAY);
        textGregorian.setText(dateGregorian);
    }

    @Override
    protected Runnable createUpdateLocationRunnable(Location location) {
        return new Runnable() {
            @Override
            public void run() {
                bindHeader(location);
                populateFragments(calendar);
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.zmanim, menu);

        PackageManager pm = getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS))
            menu.removeItem(R.id.menu_compass);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_compass:
                handler.sendEmptyMessage(WHAT_COMPASS);
                return true;
            case R.id.menu_date:
                handler.sendEmptyMessage(WHAT_DATE);
                return true;
            case R.id.menu_location:
                handler.sendEmptyMessage(WHAT_LOCATION);
                return true;
            case R.id.menu_settings:
                handler.sendEmptyMessage(WHAT_SETTINGS);
                return true;
            case R.id.menu_today:
                handler.sendEmptyMessage(WHAT_TODAY);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        setDate(year, monthOfYear, dayOfMonth);
        populateFragments(calendar);
    }

    @Override
    protected Runnable createBindHeaderRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                bindHeader();
            }
        };
    }

    /**
     * Show/hide the details list.
     *
     * @param view the master row view that was clicked.
     */
    protected void toggleDetails(View view) {
        ZmanimItem item = (ZmanimItem) view.getTag(R.id.time);
        if (item == null) {
            item = (ZmanimItem) view.getTag();
            if (item == null)
                return;
        }
        toggleDetails(item.titleId);
    }

    /**
     * Show/hide the details list.
     *
     * @param itemId the master item id.
     */
    protected void toggleDetails(int itemId) {
        if (!hasDetails(itemId))
            return;

        if (viewSwitcher != null) {
            masterFragment.unhighlight();
            if (itemId == R.string.candles) {
                candlesFragment.populateTimes(calendar);
                detailsFragmentSwitcher.setDisplayedChild(CHILD_DETAILS_CANDLES);
            } else {
                detailsListFragment.populateTimes(calendar, itemId);
                detailsFragmentSwitcher.setDisplayedChild(CHILD_DETAILS_LIST);
            }
            masterFragment.highlight(itemId);
            if (isDetailsShowing())
                hideDetails();
            else
                showDetails();
            selectedId = itemId;
        } else if ((selectedId == itemId) && isDetailsShowing()) {
            hideDetails();
            masterFragment.unhighlight();
        } else {
            masterFragment.unhighlight();
            if (itemId == R.string.candles) {
                detailsFragmentSwitcher.setDisplayedChild(CHILD_DETAILS_CANDLES);
            } else {
                detailsListFragment.populateTimes(calendar, itemId);
                detailsFragmentSwitcher.setDisplayedChild(CHILD_DETAILS_LIST);
            }
            masterFragment.highlight(itemId);
            if (!isDetailsShowing())
                showDetails();
            selectedId = itemId;
        }
    }

    @Override
    public void onBackPressed() {
        if (masterFragment != null) {
            masterFragment.unhighlight();

            if (isDetailsShowing()) {
                hideDetails();
                return;
            }
        }

        finishAfterTransition();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.header:
                toggleNavigationView();
                break;
            case R.id.nav_yesterday:
                navigateYesterday();
                break;
            case R.id.nav_tomorrow:
                navigateTomorrow();
                break;
            default:
                toggleDetails(view);
                break;
        }
    }

    private void toggleNavigationView() {
        Animation anim = buttonYesterday.getAnimation();
        if ((anim == null) || anim.hasEnded()) {
            if (buttonYesterday.getVisibility() == View.VISIBLE) {
                buttonYesterday.startAnimation(hideNavigation);
            } else {
                buttonYesterday.startAnimation(showNavigation);
            }
        }
        anim = buttonTomorrow.getAnimation();
        if ((anim == null) || anim.hasEnded()) {
            if (buttonTomorrow.getVisibility() == View.VISIBLE) {
                buttonTomorrow.startAnimation(hideNavigation);
            } else {
                buttonTomorrow.startAnimation(showNavigation);
            }
        }
    }

    protected void hideDetails() {
        if (viewSwitcher != null) {
            viewSwitcher.setDisplayedChild(CHILD_MAIN);

            // Not enough to hide the details switcher = must also hide its
            // children otherwise visible when sliding dates.
            if (detailsListFragment != null)
                detailsListFragment.setVisibility(View.INVISIBLE);
            if (candlesFragment != null)
                candlesFragment.setVisibility(View.INVISIBLE);
        } else if (detailsShrink != null) {
            detailsFragmentSwitcher.startAnimation(detailsShrink);
        }
        selectedId = 0;
    }

    protected void showDetails() {
        if (viewSwitcher != null) {
            viewSwitcher.setDisplayedChild(CHILD_DETAILS);
        } else if (detailsGrow != null) {
            detailsFragmentSwitcher.startAnimation(detailsGrow);
        }
    }

    protected boolean isDetailsShowing() {
        if ((detailsFragmentSwitcher == null) || (detailsFragmentSwitcher.getVisibility() != View.VISIBLE))
            return false;
        if (viewSwitcher == null) {
            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) detailsFragmentSwitcher.getLayoutParams();
            return (lp.horizontalWeight > 0);
        }
        return (viewSwitcher.getDisplayedChild() == CHILD_DETAILS);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent event) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent event) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // Go to date? Also don't go to date accidentally while scrolling vertically.
        if (Math.abs(velocityX) > Math.abs(velocityY) * 2) {
            if (velocityX < 0) {
                if (localeRTL) {
                    navigateYesterday();
                } else {
                    navigateTomorrow();
                }
            } else if (localeRTL) {
                navigateTomorrow();
            } else {
                navigateYesterday();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event))
            return true;
        return super.onTouchEvent(event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        localeRTL = isLocaleRTL(newConfig);
    }

    /**
     * Slide the view in from right to left.
     *
     * @param view the view to animate.
     */
    protected void slideLeft(View view) {
        view.startAnimation(slideRightToLeft);
    }

    /**
     * Slide the view in from left to right.
     *
     * @param view the view to animate.
     */
    protected void slideRight(View view) {
        view.startAnimation(slideLeftToRight);
    }

    private void populateFragments(@NonNull Calendar date) {
        masterFragment.populateTimes(date);
        detailsListFragment.populateTimes(date);
        candlesFragment.populateTimes(date);

        if (!isValidDetailsShowing()) {
            masterFragment.unhighlight();
            hideDetails();
        }
    }

    /**
     * Is the details list populated for a valid date?<br>
     * For example, candle lighting for a Friday or Erev Chag.
     *
     * @return {@code true} if valid.
     */
    private boolean isValidDetailsShowing() {
        if (!isDetailsShowing())
            return true;

        if (candlesFragment.isVisible()) {
            CandlesAdapter candlesAdapter = candlesFragment.getAdapter();
            if (candlesAdapter.getCandlesCount() == 0)
                return false;
            return true;
        }

        if (detailsListFragment.isVisible()) {
            int masterId = detailsListFragment.getMasterId();
            ZmanimAdapter masterAdapter = masterFragment.getAdapter();
            int count = masterAdapter.getCount();
            ZmanimItem item;
            for (int i = 0; i < count; i++) {
                item = masterAdapter.getItem(i);
                if ((item != null) && (item.titleId == masterId)) {
                    return true;
                }
            }
            return false;
        }

        return false;
    }

    private void navigateYesterday() {
        Calendar calendar = this.calendar;
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        if (localeRTL) {
            slideLeft(masterFragment.getView());
            slideLeft(detailsFragmentSwitcher);
        } else {
            slideRight(masterFragment.getView());
            slideRight(detailsFragmentSwitcher);
        }

        setDate(calendar.getTimeInMillis());
        populateFragments(calendar);
    }

    private void navigateTomorrow() {
        Calendar calendar = this.calendar;
        calendar.add(Calendar.DAY_OF_MONTH, +1);

        if (localeRTL) {
            slideRight(masterFragment.getView());
            slideRight(detailsFragmentSwitcher);
        } else {
            slideLeft(masterFragment.getView());
            slideLeft(detailsFragmentSwitcher);
        }

        setDate(calendar.getTimeInMillis());
        populateFragments(calendar);
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        final View buttonYesterday = this.buttonYesterday;
        if (buttonYesterday != null) {
            if (animation == hideNavigation) {
                buttonYesterday.setVisibility(View.INVISIBLE);
                buttonYesterday.setEnabled(false);
            } else if (animation == showNavigation) {
                buttonYesterday.setVisibility(View.VISIBLE);
                buttonYesterday.setEnabled(true);
            }
        }
        final View buttonTomorrow = this.buttonTomorrow;
        if (buttonTomorrow != null) {
            if (animation == hideNavigation) {
                buttonTomorrow.setVisibility(View.INVISIBLE);
                buttonTomorrow.setEnabled(false);
            } else if (animation == showNavigation) {
                buttonTomorrow.setVisibility(View.VISIBLE);
                buttonTomorrow.setEnabled(true);
            }
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    /**
     * Is the item a master with detail times?
     *
     * @param itemId the item id.
     * @return {@code true} if the item has details.
     */
    protected boolean hasDetails(int itemId) {
        return (itemId != 0) && (itemId != R.string.fast_begins) && (itemId != R.string.fast_ends);
    }

    private void updateReminders() {
        final Context context = this;
        Intent intent = new Intent(ZmanimReminder.ACTION_UPDATE);
        ZmanimReminderService.enqueueWork(context, intent);
    }

    private void cancelReminders() {
        final Context context = this;
        Intent intent = new Intent(ZmanimReminder.ACTION_CANCEL);
        ZmanimReminderService.enqueueWork(context, intent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected Class<? extends Activity> getLocationActivityClass() {
        return LocationActivity.class;
    }

    /**
     * Refresh the list at midnight for the next civil day.
     */
    private void scheduleNextDay() {
        Calendar today = Calendar.getInstance();
        if (!isToday(calendar, today)) {
            return;
        }
        long now = today.getTimeInMillis();

        Calendar tomorrow = today;
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 0);
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);
        tomorrow.set(Calendar.MILLISECOND, 0);

        handler.sendEmptyMessageDelayed(WHAT_TODAY, tomorrow.getTimeInMillis() - now);
    }

    private boolean isToday(Calendar when, Calendar today) {
        int whenYear = when.get(Calendar.YEAR);
        int whenMonth = when.get(Calendar.MONTH);
        int whenDay = when.get(Calendar.DAY_OF_MONTH);

        int todayYear = today.get(Calendar.YEAR);
        int todayMonth = today.get(Calendar.MONTH);
        int todayDay = today.get(Calendar.DAY_OF_MONTH);

        return (whenYear == todayYear) && (whenMonth == todayMonth) && (whenDay == todayDay);
    }

    public ZmanimPreferences getZmanimPreferences() {
        ZmanimPreferences preferences = this.preferences;
        if (preferences == null) {
            preferences = new SimpleZmanimPreferences(this);
            this.preferences = preferences;
        }
        return preferences;
    }

    @Override
    protected ThemeCallbacks<ZmanimPreferences> createThemeCallbacks(Context context) {
        return new SimpleThemeCallbacks<>(context, getZmanimPreferences());
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    private void initNotificationPermissions() {
        ZmanimReminder.checkNotificationPermissions(this);
    }
}
