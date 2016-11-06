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
package net.sf.times.location;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import net.sf.util.LocaleUtils;

import java.util.Collection;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Locations provider.
 *
 * @author Moshe Waisberg
 */
public class LocationsProvider implements ZmanimLocationListener, LocationFormatter {

    private static final String TAG = "LocationProvider";

    /** The maximum time interval between location updates, in milliseconds. */
    private static final long UPDATE_TIME_MAX = 6 * DateUtils.HOUR_IN_MILLIS;
    /** The time interval between requesting location updates, in milliseconds. */
    private static final long UPDATE_TIME_START = 30 * DateUtils.SECOND_IN_MILLIS;
    /**
     * The duration to receive updates, in milliseconds.<br>
     * Should be enough time to get a sufficiently accurate location.
     */
    private static final long UPDATE_DURATION = DateUtils.MINUTE_IN_MILLIS;
    /** The minimum time interval between location updates, in milliseconds. */
    private static final long UPDATE_TIME = 5 * DateUtils.SECOND_IN_MILLIS;
    /** The minimum distance between location updates, in metres. */
    private static final int UPDATE_DISTANCE = 100;

    /** Time zone ID for Jerusalem. */
    private static final String TZ_JERUSALEM = "Asia/Jerusalem";
    /** Time zone ID for Israeli Standard Time. */
    private static final String TZ_IST = "IST";
    /** Time zone ID for Israeli Daylight Time. */
    private static final String TZ_IDT = "IDT";
    /** Time zone ID for Jerusalem Standard Time. */
    private static final String TZ_JST = "JST";
    /** Time zone ID for Beirut (patch for Israeli law of DST 2013). */
    private static final String TZ_BEIRUT = "Asia/Beirut";
    /**
     * The offset in milliseconds from UTC of Israeli time zone's standard time.
     */
    private static final int TZ_OFFSET_ISRAEL = (int) (2 * DateUtils.HOUR_IN_MILLIS);
    /** Israeli time zone offset with daylight savings time. */
    private static final int TZ_OFFSET_DST_ISRAEL = (int) (TZ_OFFSET_ISRAEL + DateUtils.HOUR_IN_MILLIS);

    /** Northern-most latitude for Israel. */
    private static final double ISRAEL_NORTH = 33.289212;
    /** Southern-most latitude for Israel. */
    private static final double ISRAEL_SOUTH = 29.489218;
    /** Eastern-most longitude for Israel. */
    private static final double ISRAEL_EAST = 35.891876;
    /** Western-most longitude for Israel. */
    private static final double ISRAEL_WEST = 34.215317;

    /** Start seeking locations. */
    private static final int WHAT_START = 0;
    /** Stop seeking locations. */
    private static final int WHAT_STOP = 1;
    /** Location has changed. */
    private static final int WHAT_CHANGED = 2;
    /** Found an elevation. */
    private static final int WHAT_ELEVATION = 3;
    /** Found an address. */
    private static final int WHAT_ADDRESS = 4;

    /** If the current location is older than 1 second, then it is stale. */
    private static final long LOCATION_EXPIRATION = DateUtils.SECOND_IN_MILLIS;

    protected static final double LATITUDE_MIN = ZmanimLocation.LATITUDE_MIN;
    protected static final double LATITUDE_MAX = ZmanimLocation.LATITUDE_MAX;
    protected static final double LONGITUDE_MIN = ZmanimLocation.LONGITUDE_MIN;
    protected static final double LONGITUDE_MAX = ZmanimLocation.LONGITUDE_MAX;

    /** The context. */
    private final Context context;
    /** The owner location listeners. */
    private final Collection<ZmanimLocationListener> locationListeners = new CopyOnWriteArrayList<>();
    /** Service provider for locations. */
    private final LocationManager locationManager;
    /** The location. */
    private Location location;
    /** The settings and preferences. */
    private LocationPreferences settings;
    /** The list of countries. */
    private CountriesGeocoder countriesGeocoder;
    /** The time zone. */
    private TimeZone timeZone;
    /** The handler thread. */
    private HandlerThread handlerThread;
    /** The handler. */
    private Handler handler;
    /** The next time to start update locations. */
    private long startTaskDelay = UPDATE_TIME_START;
    /** The next time to stop update locations. */
    private final long stopTaskDelay = UPDATE_DURATION;
    /** The location is externally set? */
    private boolean manualLocation;
    /** The location formatter. */
    private final LocationFormatter formatterHelper;

    /**
     * Constructs a new provider.
     *
     * @param context
     *         the context.
     */
    protected LocationsProvider(Context context) {
        Context app = context.getApplicationContext();
        if (app != null)
            context = app;
        this.context = context;
        settings = new LocationPreferences(context);
        countriesGeocoder = new CountriesGeocoder(context);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        timeZone = TimeZone.getDefault();
        formatterHelper = createLocationFormatter(context);

        IntentFilter filter = new IntentFilter(ACTION_ADDRESS);
        context.registerReceiver(broadcastReceiver, filter);
        filter = new IntentFilter(ACTION_ELEVATION);
        context.registerReceiver(broadcastReceiver, filter);
        filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
        context.registerReceiver(broadcastReceiver, filter);

        handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new UpdatesHandler(handlerThread.getLooper());
    }

    /**
     * Register a location listener to receive location notifications.
     *
     * @param listener
     *         the listener.
     */
    private void addLocationListener(ZmanimLocationListener listener) {
        if (!locationListeners.contains(listener) && (listener != this)) {
            locationListeners.add(listener);
        }
    }

    /**
     * Unregister a location listener to stop receiving location notifications.
     *
     * @param listener
     *         the listener.
     */
    private void removeLocationListener(ZmanimLocationListener listener) {
        locationListeners.remove(listener);
    }

    @Override
    public void onLocationChanged(Location location) {
        onLocationChanged(location, true, true);
    }

    private void onLocationChanged(Location location, boolean findAddress, boolean findElevation) {
        if (!isValid(location))
            return;

        boolean keepLocation = true;
        if ((this.location != null) && (ZmanimLocation.compareTo(this.location, location) != 0)) {
            // Ignore old locations.
            if (this.location.getTime() + LOCATION_EXPIRATION > location.getTime()) {
                keepLocation = false;
            }
            // Ignore manual locations.
            if (manualLocation) {
                location = this.location;
                keepLocation = false;
            }
        }

        if (keepLocation) {
            this.location = location;
            settings.putLocation(location);
        }

        for (ZmanimLocationListener listener : locationListeners)
            listener.onLocationChanged(location);

        if (findElevation && !location.hasAltitude())
            findElevation(location);
        else if (findAddress)
            findAddress(location);
    }

    @Override
    public void onProviderDisabled(String provider) {
        for (ZmanimLocationListener listener : locationListeners)
            listener.onProviderDisabled(provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        for (ZmanimLocationListener listener : locationListeners)
            listener.onProviderEnabled(provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        for (ZmanimLocationListener listener : locationListeners)
            listener.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onAddressChanged(Location location, ZmanimAddress address) {
        for (ZmanimLocationListener listener : locationListeners)
            listener.onAddressChanged(location, address);
    }

    @Override
    public void onElevationChanged(Location location) {
        onLocationChanged(location, true, false);
    }

    @Override
    public boolean isPassive() {
        return false;
    }

    /**
     * Get a location from GPS.
     *
     * @return the location - {@code null} otherwise.
     */
    public Location getLocationGPSBase() {
        if (locationManager == null)
            return null;

        try {
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (IllegalArgumentException | SecurityException e) {
            Log.e(TAG, "GPS: " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Get a location from GPS.
     *
     * @return the location - {@code null} otherwise.
     */
    @TargetApi(Build.VERSION_CODES.M)
    public Location getLocationGPS() {
        if (locationManager == null)
            return null;

        if ((context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                && (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            return null;
        }

        try {
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (IllegalArgumentException | SecurityException e) {
            Log.e(TAG, "GPS: " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Get a location from the GSM network.
     *
     * @return the location - {@code null} otherwise.
     */
    public Location getLocationNetworkBase() {
        if (locationManager == null)
            return null;

        try {
            return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (IllegalArgumentException | SecurityException e) {
            Log.e(TAG, "Network: " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Get a location from the GSM network.
     *
     * @return the location - {@code null} otherwise.
     */
    @TargetApi(Build.VERSION_CODES.M)
    public Location getLocationNetwork() {
        if (locationManager == null)
            return null;

        if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        try {
            return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (IllegalArgumentException | SecurityException e) {
            Log.e(TAG, "Network: " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Get a passive location from other application's GPS.
     *
     * @return the location - {@code null} otherwise.
     */
    public Location getLocationPassiveFroyo() {
        if (locationManager == null)
            return null;

        try {
            return locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        } catch (IllegalArgumentException | SecurityException e) {
            Log.e(TAG, "Passive: " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Get a passive location from other application's GPS.
     *
     * @return the location - {@code null} otherwise.
     */
    @TargetApi(Build.VERSION_CODES.M)
    public Location getLocationPassive() {
        if (locationManager == null)
            return null;

        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        try {
            return locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        } catch (IllegalArgumentException | SecurityException e) {
            Log.e(TAG, "Passive: " + e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * Get a location from the time zone.
     *
     * @return the location - {@code null} otherwise.
     */
    public Location getLocationTZ() {
        return getLocationTZ(timeZone);
    }

    /**
     * Get a location from the time zone.
     *
     * @param timeZone
     *         the time zone.
     * @return the location - {@code null} otherwise.
     */
    public Location getLocationTZ(TimeZone timeZone) {
        return countriesGeocoder.findLocation(timeZone);
    }

    /**
     * Get a location from the saved preferences.
     *
     * @return the location - {@code null} otherwise.
     */
    public Location getLocationSaved() {
        return settings.getLocation();
    }

    /**
     * Get the best location.
     *
     * @return the location - {@code null} otherwise.
     */
    public Location getLocation() {
        Location loc = location;
        if (isValid(loc))
            return loc;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            loc = getLocationGPSBase();
            if (isValid(loc))
                return loc;
            loc = getLocationNetworkBase();
            if (isValid(loc))
                return loc;
            loc = getLocationPassiveFroyo();
            if (isValid(loc))
                return loc;
        } else {
            loc = getLocationGPS();
            if (isValid(loc))
                return loc;
            loc = getLocationNetwork();
            if (isValid(loc))
                return loc;
            loc = getLocationPassive();
            if (isValid(loc))
                return loc;
        }
        loc = getLocationSaved();
        if (isValid(loc))
            return loc;
        loc = getLocationTZ();
        return loc;
    }

    /**
     * Is the location valid?
     *
     * @param location
     *         the location to check.
     * @return {@code false} if location is invalid.
     */
    public boolean isValid(Location location) {
        if (location == null)
            return false;
        final double latitude = location.getLatitude();
        if ((latitude < LATITUDE_MIN) || (latitude > LATITUDE_MAX))
            return false;
        final double longitude = location.getLongitude();
        if ((longitude < LONGITUDE_MIN) || (longitude > LONGITUDE_MAX))
            return false;
        return true;
    }

    /**
     * Stop listening.
     *
     * @param listener
     *         the listener who wants to stop listening.
     */
    public void stop(ZmanimLocationListener listener) {
        if (listener != null) {
            removeLocationListener(listener);
        }
        if (!hasActiveListeners()) {
            removeUpdates();
        }
    }

    /**
     * Start or resume listening.
     *
     * @param listener
     *         the listener who wants to resume listening.
     */
    public void start(ZmanimLocationListener listener) {
        if (listener == null) {
            Log.w(TAG, "start with listener null");
            return;
        }
        addLocationListener(listener);

        // Give the listener our latest known location, and address.
        Location location = getLocation();
        handler.obtainMessage(WHAT_CHANGED, location).sendToTarget();

        if (!listener.isPassive()) {
            startTaskDelay = UPDATE_TIME_START;
            handler.sendEmptyMessage(WHAT_START);
        }
    }

    /**
     * Is the location in Israel?<br>
     * Used to determine if user is in diaspora for 2-day festivals.
     *
     * @param location
     *         the location.
     * @param timeZone
     *         the time zone.
     * @return {@code true} if user is in Israel - {@code false} otherwise.
     */
    public boolean isInIsrael(Location location, TimeZone timeZone) {
        if (location == null) {
            if (timeZone == null)
                timeZone = this.timeZone;
            String id = timeZone.getID();
            if (TZ_JERUSALEM.equals(id) || TZ_BEIRUT.equals(id))
                return true;
            // Check offsets because "IST" could be "Ireland ST", "JST" could be
            // "Japan ST".
            int offset = timeZone.getRawOffset() + timeZone.getDSTSavings();
            if ((offset >= TZ_OFFSET_ISRAEL) && (offset <= TZ_OFFSET_DST_ISRAEL)) {
                if (TZ_IDT.equals(id) || TZ_IST.equals(id) || TZ_JST.equals(id))
                    return true;
            }
            return false;
        }

        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        return (latitude <= ISRAEL_NORTH) && (latitude >= ISRAEL_SOUTH) && (longitude >= ISRAEL_WEST) && (longitude <= ISRAEL_EAST);
    }

    /**
     * Is the current location in Israel?<br>
     * Used to determine if user is in diaspora for 2-day festivals.
     *
     * @param timeZone
     *         the time zone.
     * @return {@code true} if user is in Israel - {@code false} otherwise.
     */
    public boolean isInIsrael(TimeZone timeZone) {
        return isInIsrael(getLocation(), timeZone);
    }

    /**
     * Is the current location in Israel?<br>
     * Used to determine if user is in diaspora for 2-day festivals.
     *
     * @return {@code true} if user is in Israel - {@code false} otherwise.
     */
    public boolean isInIsrael() {
        return isInIsrael(timeZone);
    }

    @Override
    public CharSequence formatCoordinates(Location location) {
        return formatterHelper.formatCoordinates(location);
    }

    @Override
    public CharSequence formatCoordinates(Address address) {
        return formatterHelper.formatCoordinates(address);
    }

    @Override
    public CharSequence formatCoordinates(double latitude, double longitude, double elevation) {
        return formatterHelper.formatCoordinates(latitude, longitude, elevation);
    }

    @Override
    public CharSequence formatLatitude(double latitude) {
        return formatterHelper.formatLatitude(latitude);
    }

    @Override
    public CharSequence formatLatitudeDecimal(double latitude) {
        return formatterHelper.formatLatitudeDecimal(latitude);
    }

    @Override
    public CharSequence formatLatitudeSexagesimal(double latitude) {
        return formatterHelper.formatLatitudeSexagesimal(latitude);
    }

    @Override
    public CharSequence formatLongitude(double longitude) {
        return formatterHelper.formatLongitude(longitude);
    }

    @Override
    public CharSequence formatLongitudeDecimal(double longitude) {
        return formatterHelper.formatLongitudeDecimal(longitude);
    }

    @Override
    public CharSequence formatLongitudeSexagesimal(double longitude) {
        return formatterHelper.formatLongitudeSexagesimal(longitude);
    }

    @Override
    public CharSequence formatElevation(double elevation) {
        return formatterHelper.formatElevation(elevation);
    }

    /**
     * Get the time zone.
     *
     * @return the time zone.
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Set the location.
     *
     * @param location
     *         the location.
     */
    public void setLocation(Location location) {
        this.location = null;
        manualLocation = location != null;
        onLocationChanged(location);
    }

    /**
     * Is the default locale right-to-left?
     *
     * @return {@code true} if the locale is either Hebrew or Yiddish.
     */
    public static boolean isLocaleRTL() {
        return LocaleUtils.isLocaleRTL();
    }

    private void requestUpdatesBase() {
        if (locationManager == null)
            return;

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(true);
        criteria.setCostAllowed(true);

        String provider = locationManager.getBestProvider(criteria, true);
        if (provider == null) {
            Log.w(TAG, "No location provider");
            return;
        }
        try {
            locationManager.requestLocationUpdates(provider, UPDATE_TIME, UPDATE_DISTANCE, this);
        } catch (IllegalArgumentException | SecurityException | NullPointerException e) {
            Log.e(TAG, "request updates: " + e.getLocalizedMessage(), e);
        }

        // Let the updates run for only a small while to save battery.
        handler.sendEmptyMessageDelayed(WHAT_STOP, stopTaskDelay);
        startTaskDelay = Math.min(UPDATE_TIME_MAX, startTaskDelay << 1);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestUpdates() {
        if (locationManager == null)
            return;

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(true);
        criteria.setCostAllowed(true);

        String provider = locationManager.getBestProvider(criteria, true);
        if (provider == null) {
            Log.w(TAG, "No location provider");
            return;
        }

        if ((context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            return;
        }

        try {
            locationManager.requestLocationUpdates(provider, UPDATE_TIME, UPDATE_DISTANCE, this);
        } catch (IllegalArgumentException | SecurityException | NullPointerException e) {
            Log.e(TAG, "request updates: " + e.getLocalizedMessage(), e);
        }

        // Let the updates run for only a small while to save battery.
        handler.sendEmptyMessageDelayed(WHAT_STOP, stopTaskDelay);
        startTaskDelay = Math.min(UPDATE_TIME_MAX, startTaskDelay << 1);
    }

    private void removeUpdates() {
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(this);
            } catch (SecurityException e) {
                Log.e(TAG, "remove updates: " + e.getLocalizedMessage(), e);
            }
        }

        if (hasActiveListeners()) {
            handler.sendEmptyMessageDelayed(WHAT_START, startTaskDelay);
        } else {
            handler.removeMessages(WHAT_START);
        }
    }

    /**
     * Quit updating locations.
     */
    public void quit() {
        manualLocation = false;
        locationListeners.clear();
        removeUpdates();
        handler.removeMessages(WHAT_ADDRESS);
        handler.removeMessages(WHAT_CHANGED);
        handler.removeMessages(WHAT_ELEVATION);
        handler.removeMessages(WHAT_START);
        handler.removeMessages(WHAT_STOP);

        context.unregisterReceiver(broadcastReceiver);

        Looper looper = handlerThread.getLooper();
        if (looper != null) {
            looper.quit();
        }
        handlerThread.interrupt();
    }

    private class UpdatesHandler extends Handler {

        public UpdatesHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Location location = null;
            ZmanimAddress address = null;

            switch (msg.what) {
                case WHAT_START:
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        requestUpdatesBase();
                    } else {
                        requestUpdates();
                    }
                    break;
                case WHAT_STOP:
                    removeUpdates();
                    break;
                case WHAT_CHANGED:
                    location = getLocation();
                    onLocationChanged(location);
                    break;
                case WHAT_ADDRESS:
                    if (msg.obj instanceof ZmanimAddress) {
                        address = (ZmanimAddress) msg.obj;
                        if (address != null)
                            location = address.getExtras().getParcelable(EXTRA_LOCATION);
                    } else {
                        location = (Location) msg.obj;
                    }
                    onAddressChanged(location, address);
                    break;
                case WHAT_ELEVATION:
                    location = (Location) msg.obj;
                    onElevationChanged(location);
                    break;
            }
        }
    }

    public void findAddress(Location location) {
        findAddress(location, true);
    }

    public void findAddress(Location location, boolean persist) {
        Intent findAddress = new Intent(context, AddressService.class);
        findAddress.setAction(ACTION_ADDRESS);
        findAddress.putExtra(EXTRA_LOCATION, location);
        findAddress.putExtra(EXTRA_PERSIST, persist);
        context.startService(findAddress);
    }

    public void findElevation(Location location) {
        Intent findElevation = new Intent(context, AddressService.class);
        findElevation.setAction(ACTION_ELEVATION);
        findElevation.putExtra(EXTRA_LOCATION, location);
        context.startService(findElevation);
    }

    /**
     * Create a location formatter helper.
     *
     * @param context
     *         the context.
     * @return the formatter.
     */
    protected LocationFormatter createLocationFormatter(Context context) {
        return new SimpleLocationFormatter(context);
    }

    /**
     * Are any listeners active?
     *
     * @return {@code true} if no listeners are passive.
     */
    private boolean hasActiveListeners() {
        if (locationListeners.isEmpty()) {
            return false;
        }
        for (ZmanimLocationListener listener : locationListeners) {
            if (!listener.isPassive()) {
                return true;
            }
        }
        return false;
    }

    /** The receiver for addresses and date/time settings. */
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            String intentPackage = intent.getPackage();
            Location location = null;

            switch (action) {
                case ACTION_ADDRESS:
                    if (TextUtils.isEmpty(intentPackage) || !intentPackage.equals(context.getPackageName())) {
                        return;
                    }
                    Bundle intentExtras = intent.getExtras();
                    ZmanimAddress address = null;
                    if (intentExtras != null) {
                        location = intentExtras.getParcelable(EXTRA_LOCATION);
                        address = intentExtras.getParcelable(EXTRA_ADDRESS);
                    }
                    if (address != null) {
                        Bundle extras = address.getExtras();
                        if (extras == null) {
                            address.setExtras(new Bundle());
                            extras = address.getExtras();
                        }
                        extras.putParcelable(EXTRA_LOCATION, location);
                        handler.obtainMessage(WHAT_ADDRESS, address).sendToTarget();
                    } else {
                        handler.obtainMessage(WHAT_ADDRESS, location).sendToTarget();
                    }
                    break;
                case ACTION_ELEVATION:
                    if (TextUtils.isEmpty(intentPackage) || !intentPackage.equals(context.getPackageName())) {
                        return;
                    }
                    location = intent.getParcelableExtra(EXTRA_LOCATION);
                    handler.obtainMessage(WHAT_ELEVATION, location).sendToTarget();
                    break;
                case Intent.ACTION_TIMEZONE_CHANGED:
                    timeZone = TimeZone.getDefault();
                    break;
            }
        }
    };

}
