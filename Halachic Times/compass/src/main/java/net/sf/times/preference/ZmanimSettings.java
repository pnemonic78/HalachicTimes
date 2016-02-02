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
package net.sf.times.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import net.sf.times.compass.R;

/**
 * Application settings.
 *
 * @author Moshe Waisberg
 */
public class ZmanimSettings {

    /** Preference name for the latitude. */
    private static final String KEY_LATITUDE = "latitude";
    /** Preference name for the longitude. */
    private static final String KEY_LONGITUDE = "longitude";
    /** Preference key for the elevation / altitude. */
    private static final String KEY_ELEVATION = "altitude";
    /** Preference name for the location provider. */
    private static final String KEY_PROVIDER = "provider";
    /** Preference name for the location time. */
    private static final String KEY_TIME = "time";
    /** Preference name for the co-ordinates visibility. */
    public static final String KEY_COORDS = "coords.visible";
    /** Preference name for the co-ordinates format. */
    public static final String KEY_COORDS_FORMAT = "coords.format";
    /** Preference name for showing summaries. */
    public static final String KEY_SUMMARIES = "summaries.visible";
    /** Preference name for the theme. */
    public static final String KEY_THEME = "theme";

    /** Format the coordinates in decimal notation. */
    public static String FORMAT_DECIMAL;
    /** Format the coordinates in sexagesimal notation. */
    public static String FORMAT_SEXIGESIMAL;

    /** Dark theme. */
    public static String LIST_THEME_DARK;
    /** Light theme. */
    public static String LIST_THEME_LIGHT;

    private Context context;
    private final SharedPreferences preferences;

    /**
     * Constructs a new settings.
     *
     * @param context
     *         the context.
     */
    public ZmanimSettings(Context context) {
        Context app = context.getApplicationContext();
        if (app != null)
            context = app;
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Get the data.
     *
     * @return the shared preferences.
     */
    public SharedPreferences getData() {
        return preferences;
    }

    /**
     * Get the editor to modify the preferences data.
     *
     * @return the editor.
     */
    public Editor edit() {
        return preferences.edit();
    }

    /**
     * Get the location.
     *
     * @return the location - {@code null} otherwise.
     */
    public Location getLocation() {
        if (!preferences.contains(KEY_LATITUDE))
            return null;
        if (!preferences.contains(KEY_LONGITUDE))
            return null;
        double latitude;
        double longitude;
        double elevation;
        try {
            latitude = Double.parseDouble(preferences.getString(KEY_LATITUDE, "0"));
            longitude = Double.parseDouble(preferences.getString(KEY_LONGITUDE, "0"));
            elevation = Double.parseDouble(preferences.getString(KEY_ELEVATION, "0"));
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return null;
        }
        String provider = preferences.getString(KEY_PROVIDER, "");
        Location location = new Location(provider);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAltitude(elevation);
        location.setTime(preferences.getLong(KEY_TIME, 0L));
        return location;
    }

    /**
     * Set the location.
     *
     * @return the location.
     */
    public void putLocation(Location location) {
        Editor editor = preferences.edit();
        editor.putString(KEY_PROVIDER, location.getProvider());
        editor.putString(KEY_LATITUDE, Double.toString(location.getLatitude()));
        editor.putString(KEY_LONGITUDE, Double.toString(location.getLongitude()));
        editor.putString(KEY_ELEVATION, Double.toString(location.hasAltitude() ? location.getAltitude() : 0));
        editor.putLong(KEY_TIME, location.getTime());
        editor.commit();
    }

    /**
     * Are coordinates visible?
     *
     * @return {@code true} to show coordinates.
     */
    public boolean isCoordinates() {
        return preferences.getBoolean(KEY_COORDS, context.getResources().getBoolean(R.bool.coords_visible_defaultValue));
    }

    /**
     * Get the notation of latitude and longitude.
     *
     * @return the format.
     */
    public String getCoordinatesFormat() {
        return preferences.getString(KEY_COORDS_FORMAT, context.getString(R.string.coords_format_defaultValue));
    }

    /**
     * Are summaries visible?
     *
     * @return {@code true} to show summaries.
     */
    public boolean isSummaries() {
        return preferences.getBoolean(KEY_SUMMARIES, context.getResources().getBoolean(R.bool.summaries_visible_defaultValue));
    }

    /**
     * Get the application theme.
     *
     * @return the theme resource id.
     */
    public int getTheme() {
        String value = preferences.getString(KEY_THEME, context.getString(R.string.theme_defaultValue));
        if (LIST_THEME_LIGHT.equals(value)) {
            return R.style.Theme_Compass_Light;
        }
        return R.style.Theme_Compass_Dark;
    }

    /**
     * Initialize. Should be called only once when application created.
     *
     * @param context
     *         the context.
     */
    public static void init(Context context) {
        FORMAT_DECIMAL = context.getString(R.string.coords_format_value_decimal);
        FORMAT_SEXIGESIMAL = context.getString(R.string.coords_format_value_sexagesimal);

        LIST_THEME_DARK = context.getString(R.string.theme_value_dark);
        LIST_THEME_LIGHT = context.getString(R.string.theme_value_light);
    }
}
