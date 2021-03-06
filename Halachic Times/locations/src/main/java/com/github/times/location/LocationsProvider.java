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
package com.github.times.location;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

import java.util.Collection;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;

import timber.log.Timber;

import static android.content.Intent.ACTION_TIMEZONE_CHANGED;
import static android.text.format.DateUtils.HOUR_IN_MILLIS;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;

/**
 * Locations provider.
 *
 * @author Moshe Waisberg
 */
public class LocationsProvider implements ZmanimLocationListener, LocationFormatter {

    private static final String TAG = "LocationProvider";

    public static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    /**
     * The maximum time interval between location updates, in milliseconds.
     */
    private static final long UPDATE_INTERVAL_MAX = 6 * HOUR_IN_MILLIS;
    /**
     * The time interval between requesting location updates, in milliseconds.
     */
    private static final long UPDATE_INTERVAL_START = 30 * SECOND_IN_MILLIS;
    /**
     * The duration to receive updates, in milliseconds.<br>
     * Should be enough time to get a sufficiently accurate location.
     */
    private static final long UPDATE_DURATION = MINUTE_IN_MILLIS;
    /**
     * The minimum time interval between location updates, in milliseconds.
     */
    private static final long UPDATE_TIME = SECOND_IN_MILLIS;
    /**
     * The minimum distance between location updates, in metres.
     */
    private static final int UPDATE_DISTANCE = 10;

    /**
     * Time zone ID for Jerusalem.
     */
    private static final String TZ_JERUSALEM = "Asia/Jerusalem";
    /**
     * Time zone ID for Israeli Standard Time.
     */
    private static final String TZ_IST = "IST";
    /**
     * Time zone ID for Israeli Daylight Time.
     */
    private static final String TZ_IDT = "IDT";
    /**
     * Time zone ID for Jerusalem Standard Time.
     */
    private static final String TZ_JST = "JST";
    /**
     * Time zone ID for Beirut (patch for Israeli law of DST 2013).
     */
    private static final String TZ_BEIRUT = "Asia/Beirut";
    /**
     * The offset in milliseconds from UTC of Israeli time zone's standard time.
     */
    private static final int TZ_OFFSET_ISRAEL = (int) (2 * HOUR_IN_MILLIS);
    /**
     * Israeli time zone offset with daylight savings time.
     */
    private static final int TZ_OFFSET_DST_ISRAEL = (int) (TZ_OFFSET_ISRAEL + HOUR_IN_MILLIS);

    /**
     * Northern-most latitude for Israel.
     */
    private static final double ISRAEL_NORTH = 33.289212;
    /**
     * Southern-most latitude for Israel.
     */
    private static final double ISRAEL_SOUTH = 29.489218;
    /**
     * Eastern-most longitude for Israel.
     */
    private static final double ISRAEL_EAST = 35.891876;
    /**
     * Western-most longitude for Israel.
     */
    private static final double ISRAEL_WEST = 34.215317;

    /**
     * Start seeking locations.
     */
    private static final int WHAT_START = 0;
    /**
     * Stop seeking locations.
     */
    private static final int WHAT_STOP = 1;
    /**
     * Location has changed.
     */
    private static final int WHAT_CHANGED = 2;
    /**
     * Found an elevation.
     */
    private static final int WHAT_ELEVATION = 3;
    /**
     * Found an address.
     */
    private static final int WHAT_ADDRESS = 4;

    /**
     * If the current location is older than 1 second, then it is stale.
     */
    private static final long LOCATION_EXPIRATION = SECOND_IN_MILLIS;

    protected static final double LATITUDE_MIN = ZmanimLocation.LATITUDE_MIN;
    protected static final double LATITUDE_MAX = ZmanimLocation.LATITUDE_MAX;
    protected static final double LONGITUDE_MIN = ZmanimLocation.LONGITUDE_MIN;
    protected static final double LONGITUDE_MAX = ZmanimLocation.LONGITUDE_MAX;

    /**
     * The context.
     */
    private final Context context;
    /**
     * The owner location listeners.
     */
    private final Collection<ZmanimLocationListener> locationListeners = new CopyOnWriteArrayList<>();
    /**
     * Service provider for locations.
     */
    private final LocationManager locationManager;
    /**
     * The location.
     */
    private Location location;
    /**
     * The preferences.
     */
    private final LocationPreferences preferences;
    /**
     * The list of countries.
     */
    private CountriesGeocoder countriesGeocoder;
    /**
     * The time zone.
     */
    private TimeZone timeZone;
    /**
     * The handler thread.
     */
    private final HandlerThread handlerThread;
    /**
     * The handler.
     */
    private final Handler handler;
    /**
     * The next time to start update locations.
     */
    private long startTaskDelay = UPDATE_INTERVAL_START;
    /**
     * The next time to stop update locations.
     */
    private final long stopTaskDelay = UPDATE_DURATION;
    /**
     * The location is externally set?
     */
    private boolean manualLocation;
    /**
     * The location formatter.
     */
    private final LocationFormatter formatterHelper;

    /**
     * Constructs a new provider.
     *
     * @param context the context.
     */
    public LocationsProvider(Context context) {
        Context app = context.getApplicationContext();
        if (app != null) {
            context = app;
        }
        this.context = context;
        preferences = new SimpleLocationPreferences(context);
        countriesGeocoder = new CountriesGeocoder(context);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        timeZone = TimeZone.getDefault();
        formatterHelper = createLocationFormatter(context);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ADDRESS);
        filter.addAction(ACTION_ELEVATION);
        filter.addAction(ACTION_TIMEZONE_CHANGED);
        context.registerReceiver(broadcastReceiver, filter);

        handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new UpdatesHandler(handlerThread.getLooper());
    }

    /**
     * Register a location listener to receive location notifications.
     *
     * @param listener the listener.
     */
    private void addLocationListener(ZmanimLocationListener listener) {
        if (!locationListeners.contains(listener) && (listener != this)) {
            locationListeners.add(listener);
        }
    }

    /**
     * Unregister a location listener to stop receiving location notifications.
     *
     * @param listener the listener.
     */
    private void removeLocationListener(ZmanimLocationListener listener) {
        locationListeners.remove(listener);
    }

    @Override
    public void onLocationChanged(Location location) {
        Timber.v("onLocationChanged %s", location);
        onLocationChanged(location, true, true);
    }

    private void onLocationChanged(final Location location, boolean findAddress, boolean findElevation) {
        if (!isValid(location)) {
            return;
        }
        final Location locationOld = this.location;
        if (ZmanimLocation.compareAll(location, locationOld) == 0) {
            return;
        }

        Location locationNew = location;
        boolean keepLocation = true;
        if ((locationOld != null) && (ZmanimLocation.compare(locationOld, location) != 0)) {
            // Ignore old locations.
            if (locationOld.getTime() + LOCATION_EXPIRATION > location.getTime()) {
                keepLocation = false;
            }
            // Ignore manual locations.
            if (manualLocation) {
                // But does the new location have an elevation?
                if (location.hasAltitude() && !locationOld.hasAltitude()) {
                    double distance = ZmanimLocation.distanceBetween(locationOld, location);
                    if (distance <= GeocoderBase.SAME_CITY) {
                        locationOld.setAltitude(location.getAltitude());
                    }
                }
                locationNew = locationOld;
            }
        }

        if (keepLocation) {
            this.location = locationNew;
            preferences.putLocation(locationNew);
        }

        notifyLocationChanged(locationNew);

        if (findElevation && !locationNew.hasAltitude()) {
            findElevation(locationNew);
        } else if (findAddress) {
            findAddress(locationNew);
        }
    }

    private void notifyLocationChanged(Location location) {
        for (ZmanimLocationListener listener : locationListeners) {
            listener.onLocationChanged(location);
        }
        broadcastLocationChanged(location);
    }

    private void broadcastLocationChanged(Location location) {
        Intent intent = new Intent(ACTION_LOCATION_CHANGED);
        intent.setPackage(context.getPackageName());
        intent.putExtra(EXTRA_LOCATION, location);
        context.sendBroadcast(intent);
    }

    @Override
    public void onProviderDisabled(String provider) {
        for (ZmanimLocationListener listener : locationListeners) {
            listener.onProviderDisabled(provider);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        for (ZmanimLocationListener listener : locationListeners) {
            listener.onProviderEnabled(provider);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        for (ZmanimLocationListener listener : locationListeners) {
            listener.onStatusChanged(provider, status, extras);
        }
    }

    @Override
    public void onAddressChanged(Location location, ZmanimAddress address) {
        for (ZmanimLocationListener listener : locationListeners) {
            listener.onAddressChanged(location, address);
        }
    }

    @Override
    public void onElevationChanged(Location location) {
        onLocationChanged(location, true, false);
    }

    public static boolean hasNoLocationPermission(Context context) {
        return (ContextCompat.checkSelfPermission(context, PERMISSIONS[0]) != PackageManager.PERMISSION_GRANTED)
            && (ContextCompat.checkSelfPermission(context, PERMISSIONS[1]) != PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Get a location from GPS.
     *
     * @return the location - {@code null} otherwise.
     */
    public Location getLocationGPS() {
        LocationManager locationManager = this.locationManager;
        if ((locationManager == null) || hasNoLocationPermission(context)) {
            return null;
        }

        try {
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (IllegalArgumentException | SecurityException | NullPointerException e) {
            Timber.e(e, "GPS: %s", e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * Get a location from the GSM network.
     *
     * @return the location - {@code null} otherwise.
     */
    public Location getLocationNetwork() {
        LocationManager locationManager = this.locationManager;
        if ((locationManager == null) || hasNoLocationPermission(context)) {
            return null;
        }

        try {
            return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (IllegalArgumentException | SecurityException | NullPointerException e) {
            Timber.e(e, "Network: %s", e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * Get a passive location from other application's GPS.
     *
     * @return the location - {@code null} otherwise.
     */
    public Location getLocationPassive() {
        LocationManager locationManager = this.locationManager;
        if ((locationManager == null) || hasNoLocationPermission(context)) {
            return null;
        }

        try {
            return locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        } catch (IllegalArgumentException | SecurityException | NullPointerException e) {
            Timber.e(e, "Passive: %s", e.getLocalizedMessage());
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
     * @param timeZone the time zone.
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
        return preferences.getLocation();
    }

    /**
     * Get the best location.
     *
     * @return the location - {@code null} otherwise.
     */
    public Location getLocation() {
        Location location = this.location;
        if (isValid(location))
            return location;
        location = getLocationGPS();
        if (isValid(location))
            return location;
        location = getLocationNetwork();
        if (isValid(location))
            return location;
        location = getLocationPassive();
        if (isValid(location))
            return location;
        location = getLocationSaved();
        if (isValid(location))
            return location;
        location = getLocationTZ();
        return location;
    }

    /**
     * Is the location valid?
     *
     * @param location the location to check.
     * @return {@code false} if location is invalid.
     */
    public boolean isValid(Location location) {
        return ZmanimLocation.isValid(location);
    }

    /**
     * Stop listening.
     *
     * @param listener the listener who wants to stop listening.
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
     * @param listener the listener who wants to resume listening.
     */
    public void start(ZmanimLocationListener listener) {
        if (listener == null) {
            Timber.w("start with listener null");
            return;
        }
        if (!handlerThread.isAlive()) {
            Timber.w("start with dead handler");
            return;
        }
        addLocationListener(listener);

        // Give the listener our latest known location, and address.
        Location location = getLocation();
        if (this.location == null) {
            handler.obtainMessage(WHAT_CHANGED, location).sendToTarget();
        } else if (location != null) {
            listener.onLocationChanged(location);
        }

        startTaskDelay = UPDATE_INTERVAL_START;
        sendEmptyMessage(WHAT_START);
    }

    /**
     * Is the location in Israel?<br>
     * Used to determine if user is in diaspora for 2-day festivals.
     *
     * @param location the location.
     * @param timeZone the time zone.
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
     * @param timeZone the time zone.
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

    @Override
    public CharSequence formatBearing(double azimuth) {
        return formatterHelper.formatBearing(azimuth);
    }

    @Override
    public CharSequence formatBearingDecimal(double azimuth) {
        return formatterHelper.formatBearingDecimal(azimuth);
    }

    @Override
    public CharSequence formatBearingSexagesimal(double azimuth) {
        return formatterHelper.formatBearingSexagesimal(azimuth);
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
     * @param location the location - {@code null} to request the current location.
     */
    public void setLocation(Location location) {
        this.location = null;
        manualLocation = location != null;
        onLocationChanged(location);
        if (location == null) {
            sendEmptyMessage(WHAT_START);
        }
    }

    private void requestUpdates() {
        LocationManager locationManager = this.locationManager;
        if ((locationManager == null) || hasNoLocationPermission(context)) {
            return;
        }

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(true);
        criteria.setCostAllowed(true);

        String provider = locationManager.getBestProvider(criteria, true);
        if (provider == null) {
            Timber.w("No location provider");
            return;
        }

        try {
            locationManager.requestLocationUpdates(provider, UPDATE_TIME, UPDATE_DISTANCE, this);
        } catch (IllegalArgumentException | SecurityException | NullPointerException e) {
            Timber.e(e, "request updates: %s", e.getMessage());
        }

        // Let the updates run for only a small while to save battery.
        sendEmptyMessageDelayed(WHAT_STOP, stopTaskDelay);
        startTaskDelay = Math.min(UPDATE_INTERVAL_MAX, startTaskDelay << 1);
    }

    private void removeUpdates() {
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(this);
            } catch (Exception e) {
                Timber.e(e, "remove updates: %s", e.getMessage());
            }
        }

        if (hasActiveListeners()) {
            sendEmptyMessageDelayed(WHAT_START, startTaskDelay);
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

        handlerThread.quit();
        handlerThread.interrupt();
    }

    private class UpdatesHandler extends Handler {

        UpdatesHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Location location = null;
            ZmanimAddress address = null;

            switch (msg.what) {
                case WHAT_START:
                    requestUpdates();
                    break;
                case WHAT_STOP:
                    removeUpdates();
                    break;
                case WHAT_CHANGED:
                    location = (Location) msg.obj;
                    onLocationChanged(location);
                    break;
                case WHAT_ADDRESS:
                    if (msg.obj != null) {
                        if (msg.obj instanceof ZmanimAddress) {
                            address = (ZmanimAddress) msg.obj;
                            location = address.getExtras().getParcelable(EXTRA_LOCATION);
                        } else if (msg.obj instanceof Location) {
                            location = (Location) msg.obj;
                        }
                        onAddressChanged(location, address);
                    }
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
        Intent findAddress = new Intent(ACTION_ADDRESS);
        findAddress.putExtra(EXTRA_LOCATION, location);
        findAddress.putExtra(EXTRA_PERSIST, persist);
        AddressService.enqueueWork(context, findAddress);
    }

    public void findElevation(Location location) {
        Intent findElevation = new Intent(ACTION_ELEVATION);
        findElevation.putExtra(EXTRA_LOCATION, location);
        AddressService.enqueueWork(context, findElevation);
    }

    /**
     * Create a location formatter helper.
     *
     * @param context the context.
     * @return the formatter.
     */
    protected LocationFormatter createLocationFormatter(Context context) {
        return new SimpleLocationFormatter(context, preferences);
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
        return true;
    }

    /**
     * Get the location preferences.
     *
     * @return the preferences.
     */
    public LocationPreferences getLocationPreferences() {
        return preferences;
    }

    /**
     * The receiver for addresses and date/time settings.
     */
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            String intentPackage = intent.getPackage();
            Location location = null;
            ZmanimAddress address = null;
            Bundle intentExtras = intent.getExtras();

            switch (action) {
                case ACTION_ADDRESS:
                    if (TextUtils.isEmpty(intentPackage) || !intentPackage.equals(context.getPackageName())) {
                        return;
                    }
                    if (intentExtras != null) {
                        location = intentExtras.getParcelable(EXTRA_LOCATION);
                        address = intentExtras.getParcelable(EXTRA_ADDRESS);
                    }
                    if (address != null) {
                        Bundle extras = address.getExtras();
                        if (extras == null) {
                            extras = new Bundle();
                        }
                        extras.putParcelable(EXTRA_LOCATION, location);
                        address.setExtras(extras);
                        if (handler != null) {//In case we receive broadcast before provider is constructed.
                            handler.obtainMessage(WHAT_ADDRESS, address).sendToTarget();
                        }
                    } else if (handler != null) {//In case we receive broadcast before provider is constructed.
                        handler.obtainMessage(WHAT_ADDRESS, location).sendToTarget();
                    }
                    break;
                case ACTION_ELEVATION:
                    if (TextUtils.isEmpty(intentPackage) || !intentPackage.equals(context.getPackageName())) {
                        return;
                    }
                    if (intentExtras != null) {
                        location = intentExtras.getParcelable(EXTRA_LOCATION);
                    }
                    if (handler != null) {//In case we receive broadcast before provider is constructed.
                        handler.obtainMessage(WHAT_ELEVATION, location).sendToTarget();
                    }
                    break;
                case ACTION_TIMEZONE_CHANGED:
                    timeZone = TimeZone.getDefault();
                    break;
            }
        }
    };

    private boolean sendEmptyMessage(int what) {
        if (handlerThread.isAlive()) {
            return handler.sendEmptyMessage(what);
        }
        return false;
    }

    private boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        if (handlerThread.isAlive()) {
            return handler.sendEmptyMessageDelayed(what, delayMillis);
        }
        return false;
    }
}
