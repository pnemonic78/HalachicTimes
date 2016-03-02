/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.times;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
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
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import net.sf.app.TodayDatePickerDialog;
import net.sf.content.ContextResourcesWrapper;
import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sf.times.compass.CompassActivity;
import net.sf.times.content.res.ZmanimResources;
import net.sf.times.location.LocationActivity;
import net.sf.times.location.LocationApplication;
import net.sf.times.location.LocationsProvider;
import net.sf.times.location.ZmanimAddress;
import net.sf.times.location.ZmanimLocation;
import net.sf.times.location.ZmanimLocationListener;
import net.sf.times.location.ZmanimLocations;
import net.sf.times.preference.ZmanimPreferenceActivity;
import net.sf.times.preference.ZmanimSettings;
import net.sf.view.animation.LayoutWeightAnimation;

import java.lang.ref.WeakReference;
import java.util.Calendar;

/**
 * Shows a list of halachic times (<em>zmanim</em>) for prayers.
 *
 * @author Moshe Waisberg
 */
public class ZmanimActivity extends Activity implements ZmanimLocationListener, OnDateSetListener, View.OnClickListener, OnGestureListener, GestureDetector.OnDoubleTapListener, Animation.AnimationListener {

    /** The date parameter. */
    public static final String EXTRA_DATE = "date";
    /** The time parameter. */
    public static final String EXTRA_TIME = "time";
    /** The location parameter. */
    public static final String EXTRA_LOCATION = LocationManager.KEY_LOCATION_CHANGED;
    /** The details list parameter. */
    private static final String PARAMETER_DETAILS = "details";

    private static final int WHAT_TOGGLE_DETAILS = 0;
    private static final int WHAT_COMPASS = 1;
    private static final int WHAT_DATE = 2;
    private static final int WHAT_LOCATION = 3;
    private static final int WHAT_SETTINGS = 4;
    private static final int WHAT_TODAY = 5;

    private static final int CHILD_MAIN = 0;
    private static final int CHILD_DETAILS = 1;

    private static final int CHILD_DETAILS_LIST = 0;
    private static final int CHILD_DETAILS_CANDLES = 1;

    /** The date. */
    private final Calendar date = Calendar.getInstance();
    /** The location header. */
    private View header;
    private final Rect headerRect = new Rect();
    /** The navigation bar. */
    private View navigationBar;
    /** Provider for locations. */
    private LocationsProvider locations;
    /** The settings and preferences. */
    protected ZmanimSettings settings;
    /** The date picker. */
    private DatePickerDialog datePicker;
    /** The address location. */
    private Location addressLocation;
    /** The address. */
    private ZmanimAddress address;
    /** Populate the header in UI thread. */
    private Runnable populateHeader;
    /** Update the location in UI thread. */
    private Runnable updateLocation;
    /** The master fragment. */
    private ZmanimFragment<ZmanimAdapter, ZmanimPopulater<ZmanimAdapter>> masterFragment;
    /** The details fragment switcher. */
    private ViewSwitcher detailsFragmentSwitcher;
    /** The details fragment. */
    private ZmanimDetailsFragment detailsListFragment;
    /** The candles fragment. */
    private CandlesFragment candlesFragment;
    /** Is master fragment switched with details fragment? */
    private ViewSwitcher viewSwitcher;
    /** The master item selected id. */
    private int selectedId;
    /** The gesture detector. */
    private GestureDetector gestureDetector;
    /** Is locale RTL? */
    private boolean localeRTL;
    /** Slide left-to-right animation. */
    private Animation slideLeftToRight;
    /** Slide right-to-left animation. */
    private Animation slideRightToLeft;
    /** Grow details animation. */
    private Animation detailsGrow;
    /** Shrink details animation. */
    private Animation detailsShrink;
    /** Hide navigation bar animation. */
    private Animation hideNavigation;
    /** Show navigation bar animation. */
    private Animation showNavigation;
    private final Handler handler;

    /** The handler. */
    private static class ActivityHandler extends Handler {

        private final WeakReference<ZmanimActivity> activityWeakReference;

        public ActivityHandler(ZmanimActivity activity) {
            this.activityWeakReference = new WeakReference<ZmanimActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ZmanimActivity activity = activityWeakReference.get();
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
                    Calendar date = activity.date;
                    final int year = date.get(Calendar.YEAR);
                    final int month = date.get(Calendar.MONTH);
                    final int day = date.get(Calendar.DAY_OF_MONTH);
                    if (activity.datePicker == null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Resources res = context.getResources();
                            res = new ZmanimResources(res.getAssets(), res.getDisplayMetrics(), res.getConfiguration());
                            context = new ContextResourcesWrapper(context, res);
                        }
                        activity.datePicker = new TodayDatePickerDialog(context, activity, year, month, day);
                    } else {
                        activity.datePicker.updateDate(year, month, day);
                    }
                    activity.datePicker.show();
                    break;
                case WHAT_LOCATION:
                    Location loc = activity.locations.getLocation();
                    // Have we been destroyed?
                    if (loc == null)
                        break;

                    Intent intent = new Intent(context, LocationActivity.class);
                    intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, loc);
                    activity.startActivity(intent);
                    break;
                case WHAT_SETTINGS:
                    activity.startActivity(new Intent(context, ZmanimPreferenceActivity.class));
                    break;
                case WHAT_TODAY:
                    activity.setDate(System.currentTimeMillis());
                    activity.populateFragments(activity.date);
                    break;
            }
        }
    }

    /**
     * Creates a new activity.
     */
    public ZmanimActivity() {
        this.handler = new ActivityHandler(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        initLocation();

        Intent intent = getIntent();
        long date = intent.getLongExtra(EXTRA_DATE, 0L);
        if (date == 0L) {
            date = intent.getLongExtra(EXTRA_TIME, 0L);
            if (date == 0L)
                date = System.currentTimeMillis();
        }
        setDate(date);

        Location location = intent.getParcelableExtra(EXTRA_LOCATION);
        if (location != null)
            locations.setLocation(location);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(EXTRA_DATE, date.getTimeInMillis());
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
        locations.start(this);
        cancelReminders();
        int itemId = selectedId;
        if (itemId != 0) {
            // We need to wait for the list rows to get their default
            // backgrounds before we can highlight any row.
            Message msg = handler.obtainMessage(WHAT_TOGGLE_DETAILS, itemId, 0);
            handler.sendMessageDelayed(msg, DateUtils.SECOND_IN_MILLIS);
        }
    }

    @Override
    protected void onPause() {
        int itemId = selectedId;
        if (itemId != 0) {
            hideDetails();
            selectedId = itemId;
        }
        updateReminders();
        super.onPause();
    }

    @Override
    protected void onStop() {
        locations.stop(this);
        super.onStop();
    }

    /** Initialise. */
    @SuppressWarnings({"unchecked", "InflateParams"})
    private void init() {
        Context context = this;
        settings = new ZmanimSettings(context);

        setTheme(settings.getTheme());
        setContentView(R.layout.times);
        View view = getWindow().getDecorView();

        gestureDetector = new GestureDetector(context, this, handler);
        gestureDetector.setIsLongpressEnabled(false);

        FragmentManager fragmentManager = getFragmentManager();
        masterFragment = (ZmanimFragment<ZmanimAdapter, ZmanimPopulater<ZmanimAdapter>>) fragmentManager.findFragmentById(R.id.list_fragment);
        masterFragment.setOnClickListener(this);
        detailsFragmentSwitcher = (ViewSwitcher) view.findViewById(R.id.details_fragment);
        detailsListFragment = (ZmanimDetailsFragment) fragmentManager.findFragmentById(R.id.details_list_fragment);
        candlesFragment = (CandlesFragment) fragmentManager.findFragmentById(R.id.candles_fragment);

        viewSwitcher = (ViewSwitcher) view.findViewById(R.id.frame_fragments);
        if (viewSwitcher != null) {
            Animation inAnim = AnimationUtils.makeInAnimation(context, false);
            inAnim.setDuration(400L);
            viewSwitcher.setInAnimation(inAnim);
            Animation outAnim = AnimationUtils.makeOutAnimation(context, true);
            outAnim.setDuration(400L);
            viewSwitcher.setOutAnimation(outAnim);
        }

        header = view.findViewById(R.id.header);
        navigationBar = header.findViewById(R.id.navigation_bar);

        View iconBack = navigationBar.findViewById(R.id.nav_yesterday);
        iconBack.setOnClickListener(this);
        View iconForward = navigationBar.findViewById(R.id.nav_tomorrow);
        iconForward.setOnClickListener(this);

        slideRightToLeft = AnimationUtils.loadAnimation(context, R.anim.slide_right_to_left);
        slideLeftToRight = AnimationUtils.loadAnimation(context, R.anim.slide_left_to_right);
        detailsGrow = new LayoutWeightAnimation(detailsFragmentSwitcher, 0f, 2f);
        detailsGrow.setDuration(500L);
        detailsShrink = new LayoutWeightAnimation(detailsFragmentSwitcher, 2f, 0f);
        detailsShrink.setDuration(500L);
        hideNavigation = AnimationUtils.loadAnimation(context, R.anim.hide_nav);
        hideNavigation.setAnimationListener(this);
        showNavigation = AnimationUtils.loadAnimation(context, R.anim.show_nav);
        showNavigation.setAnimationListener(this);
    }

    /** Initialise the location providers. */
    private void initLocation() {
        LocationApplication app = (LocationApplication) getApplication();
        locations = app.getLocations();
        localeRTL = ZmanimLocations.isLocaleRTL();
    }

    /**
     * Set the date for the list.
     *
     * @param date
     *         the date.
     */
    private void setDate(long date) {
        this.date.setTimeInMillis(date);
        this.date.setTimeZone(locations.getTimeZone());

        View header = this.header;
        // Have we been destroyed?
        if (header == null)
            return;
        CharSequence dateGregorian = DateUtils.formatDateTime(this, this.date.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                | DateUtils.FORMAT_SHOW_WEEKDAY);
        TextView textGregorian = (TextView) header.findViewById(R.id.date_gregorian);
        textGregorian.setText(dateGregorian);
    }

    /**
     * Set the date for the list.
     *
     * @param year
     *         the year.
     * @param monthOfYear
     *         the month of the year.
     * @param dayOfMonth
     *         the day of the month.
     */
    private void setDate(int year, int monthOfYear, int dayOfMonth) {
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, monthOfYear);
        date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        date.setTimeZone(locations.getTimeZone());

        View header = this.header;
        // Have we been destroyed?
        if (header == null)
            return;
        CharSequence dateGregorian = DateUtils.formatDateTime(this, date.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                | DateUtils.FORMAT_SHOW_WEEKDAY);
        TextView textGregorian = (TextView) header.findViewById(R.id.date_gregorian);
        textGregorian.setText(dateGregorian);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (ZmanimLocation.compareTo(addressLocation, location) != 0) {
            address = null;
        }
        addressLocation = location;
        if (updateLocation == null) {
            updateLocation = new Runnable() {
                @Override
                public void run() {
                    populateHeader();
                    populateFragments(date);
                }
            };
        }
        runOnUiThread(updateLocation);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    /** Populate the header item. */
    private void populateHeader() {
        View header = this.header;
        // Have we been destroyed?
        if (header == null)
            return;
        Location loc = (addressLocation == null) ? locations.getLocation() : addressLocation;
        // Have we been destroyed?
        if (loc == null)
            return;

        final CharSequence coordsText = locations.formatCoordinates(loc);
        final CharSequence locationName = formatAddress();

        // Update the location.
        TextView coordinates = (TextView) header.findViewById(R.id.coordinates);
        coordinates.setText(coordsText);
        coordinates.setVisibility(settings.isCoordinates() ? View.VISIBLE : View.GONE);
        TextView address = (TextView) header.findViewById(R.id.address);
        address.setText(locationName);
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
        populateFragments(date);
    }

    @Override
    public void onAddressChanged(Location location, ZmanimAddress address) {
        addressLocation = location;
        this.address = address;
        if (populateHeader == null) {
            populateHeader = new Runnable() {
                @Override
                public void run() {
                    populateHeader();
                }
            };
        }
        runOnUiThread(populateHeader);
    }

    @Override
    public void onElevationChanged(Location location) {
        onLocationChanged(location);
    }

    /**
     * Format the address for the current location.
     *
     * @return the formatted address.
     */
    private CharSequence formatAddress() {
        if (address != null)
            return address.getFormatted();
        return getString(R.string.location_unknown);
    }

    /**
     * Show/hide the details list.
     *
     * @param item
     *         the master item.
     * @param view
     *         the master row view that was clicked.
     */
    protected void toggleDetails(ZmanimItem item, View view) {
        if (item == null)
            item = (ZmanimItem) view.getTag();
        toggleDetails(item.titleId);
    }

    /**
     * Show/hide the details list.
     *
     * @param itemId
     *         the master item id.
     */
    protected void toggleDetails(int itemId) {
        if (!hasDetails(itemId))
            return;

        if (viewSwitcher != null) {
            if (itemId == R.string.candles) {
                candlesFragment.populateTimes(date);
                detailsFragmentSwitcher.setDisplayedChild(CHILD_DETAILS_CANDLES);
            } else {
                detailsListFragment.populateTimes(date, itemId);
                detailsFragmentSwitcher.setDisplayedChild(CHILD_DETAILS_LIST);
            }
            if (isDetailsShowing())
                hideDetails();
            else
                showDetails();
            selectedId = itemId;
        } else if ((selectedId == itemId) && isDetailsShowing()) {
            hideDetails();
            masterFragment.unhighlight();
        } else {
            if (itemId == R.string.candles) {
                detailsFragmentSwitcher.setDisplayedChild(CHILD_DETAILS_CANDLES);
            } else {
                detailsListFragment.populateTimes(date, itemId);
                detailsFragmentSwitcher.setDisplayedChild(CHILD_DETAILS_LIST);
            }
            masterFragment.unhighlight();
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

        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.header:
            case R.id.navigation_bar:
                Animation anim = navigationBar.getAnimation();
                if ((anim == null) || anim.hasEnded()) {
                    if (navigationBar.getVisibility() == View.VISIBLE) {
                        navigationBar.startAnimation(hideNavigation);
                    } else {
                        navigationBar.startAnimation(showNavigation);
                    }
                }
                break;
            case R.id.nav_yesterday:
                navigateYesterday();
                break;
            case R.id.nav_tomorrow:
                navigateTomorrow();
                break;
            default:
                ZmanimItem item = (ZmanimItem) view.getTag(R.id.time);
                toggleDetails(item, view);
                break;
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
        } else {
            detailsFragmentSwitcher.startAnimation(detailsShrink);
        }
        selectedId = 0;
    }

    protected void showDetails() {
        if (viewSwitcher != null) {
            viewSwitcher.setDisplayedChild(CHILD_DETAILS);
        } else {
            detailsFragmentSwitcher.startAnimation(detailsGrow);
        }
    }

    protected boolean isDetailsShowing() {
        if ((detailsFragmentSwitcher == null) || (detailsFragmentSwitcher.getVisibility() != View.VISIBLE))
            return false;
        if (viewSwitcher == null) {
            LinearLayout.LayoutParams lp = (LayoutParams) detailsFragmentSwitcher.getLayoutParams();
            return (lp.weight > 0);
        }
        return (viewSwitcher.getDisplayedChild() == CHILD_DETAILS);
    }

    /**
     * Search key was pressed.
     */
    @Override
    public boolean onSearchRequested() {
        Location loc = locations.getLocation();
        // Have we been destroyed?
        if (loc == null)
            return super.onSearchRequested();

        ZmanimAddress address = this.address;
        String query = (address != null) ? address.getFormatted().toString() : null;

        Bundle appData = new Bundle();
        appData.putParcelable(LocationManager.KEY_LOCATION_CHANGED, loc);
        startSearch(query, false, appData, false);
        return true;
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
        if ((e1 == null) || (e2 == null))
            return false;
        // Go to date?
        float dX = e2.getX() - e1.getX();
        float dY = e2.getY() - e1.getY();
        if (Math.abs(dX) > Math.abs(dY)) {
            if (dX < 0) {
                if (localeRTL) {
                    navigateYesterday();
                } else {
                    navigateTomorrow();
                }
            } else {
                if (localeRTL) {
                    navigateTomorrow();
                } else {
                    navigateYesterday();
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        View header = this.header;
        if (header != null) {
            int x = (int) e.getX();
            int y = (int) e.getY();
            header.getGlobalVisibleRect(headerRect);
            if (headerRect.contains(x, y)) {
                onClick(header);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
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
        localeRTL = ZmanimLocations.isLocaleRTL();
    }

    /**
     * Slide the view in from right to left.
     *
     * @param view
     *         the view to animate.
     */
    protected void slideLeft(View view) {
        view.startAnimation(slideRightToLeft);
    }

    /**
     * Slide the view in from left to right.
     *
     * @param view
     *         the view to animate.
     */
    protected void slideRight(View view) {
        view.startAnimation(slideLeftToRight);
    }

    private void populateFragments(Calendar date) {
        masterFragment.populateTimes(date);
        detailsListFragment.populateTimes(date);
        candlesFragment.populateTimes(date);

        if (!isValidDetailsShowing())
            hideDetails();
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
                if (item.titleId == masterId)
                    return true;
            }
            return false;
        }

        return false;
    }

    private void navigateYesterday() {
        Calendar date = this.date;
        date.add(Calendar.DATE, -1);

        if (localeRTL) {
            //TODO slideLeft(masterFragment);
            slideLeft(detailsFragmentSwitcher);
        } else {
            //TODO slideRight(masterFragment);
            slideRight(detailsFragmentSwitcher);
        }

        setDate(date.getTimeInMillis());
        populateFragments(date);
    }

    private void navigateTomorrow() {
        Calendar date = this.date;
        date.add(Calendar.DATE, +1);

        if (localeRTL) {
            //TODO slideRight(masterFragment);
            slideRight(detailsFragmentSwitcher);
        } else {
            //TODO slideLeft(masterFragment);
            slideLeft(detailsFragmentSwitcher);
        }

        setDate(date.getTimeInMillis());
        populateFragments(date);
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (navigationBar != null) {
            if (animation == hideNavigation) {
                navigationBar.setVisibility(View.INVISIBLE);
                navigationBar.setEnabled(false);
            } else if (animation == showNavigation) {
                navigationBar.setVisibility(View.VISIBLE);
                navigationBar.setEnabled(true);
            }
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    /**
     * Is the item a master with detail times?
     *
     * @param itemId
     *         the item id.
     * @return {@code true} if the item has details.
     */
    protected boolean hasDetails(int itemId) {
        return (itemId != 0) && (itemId != R.string.fast_begins) && (itemId != R.string.fast_ends);
    }

    private void updateReminders() {
        Context context = this;

        Intent intent = new Intent(context, ZmanimReminder.class);
        intent.setAction(ZmanimReminder.ACTION_UPDATE);
        context.sendBroadcast(intent);
    }

    private void cancelReminders() {
        Context context = this;

        Intent intent = new Intent(context, ZmanimReminder.class);
        intent.setAction(ZmanimReminder.ACTION_CANCEL);
        context.sendBroadcast(intent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (gestureDetector.onTouchEvent(ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }
}
