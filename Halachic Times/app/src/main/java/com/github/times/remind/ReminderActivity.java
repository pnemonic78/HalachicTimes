package com.github.times.remind;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;

import com.github.app.LocaleCallbacks;
import com.github.app.LocaleHelper;
import com.github.app.SimpleThemeCallbacks;
import com.github.app.ThemeCallbacks;
import com.github.times.R;
import com.github.times.preference.SimpleZmanimPreferences;
import com.github.times.preference.ZmanimPreferences;

/**
 * Shows a reminder alarm for a (<em>zman</em>).
 *
 * @author Moshe Waisberg
 */
public class ReminderActivity<P extends ZmanimPreferences> extends Activity implements
        ThemeCallbacks<P> {

    private LocaleCallbacks<P> localeCallbacks;
    private ThemeCallbacks<P> themeCallbacks;
    /** The preferences. */
    private P preferences;

    @Override
    protected void attachBaseContext(Context newBase) {
        this.localeCallbacks = new LocaleHelper<>(newBase);
        Context context = localeCallbacks.attachBaseContext(newBase);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreate();

        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        setContentView(R.layout.reminder);

        handleIntent(getIntent());
    }

    @Override
    public void onCreate() {
        localeCallbacks.onCreate(this);
        getThemeCallbacks().onCreate();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    protected void handleIntent(Intent intent) {
        //TODO implement me!
    }

    @Override
    public P getThemePreferences() {
        return getThemeCallbacks().getThemePreferences();
    }

    protected ThemeCallbacks<P> getThemeCallbacks() {
        if (themeCallbacks == null) {
            themeCallbacks = createThemeCallbacks(this);
        }
        return themeCallbacks;
    }

    protected ThemeCallbacks<P> createThemeCallbacks(ContextWrapper context) {
        return new SimpleThemeCallbacks<>(context, getZmanimPreferences());
    }

    public P getZmanimPreferences() {
        if (preferences == null) {
            preferences = (P) new SimpleZmanimPreferences(this);
        }
        return preferences;
    }
}
