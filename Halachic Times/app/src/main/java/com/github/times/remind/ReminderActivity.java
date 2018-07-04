package com.github.times.remind;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import com.github.app.LocaleCallbacks;
import com.github.app.LocaleHelper;
import com.github.app.SimpleThemeCallbacks;
import com.github.app.ThemeCallbacks;
import com.github.times.R;
import com.github.times.preference.SimpleZmanimPreferences;
import com.github.times.preference.ZmanimPreferences;
import com.github.util.LocaleUtils;
import com.github.util.LogUtils;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static com.github.util.TimeUtils.roundUp;

/**
 * Shows a reminder alarm for a (<em>zman</em>).
 *
 * @author Moshe Waisberg
 */
public class ReminderActivity<P extends ZmanimPreferences> extends Activity implements
        ThemeCallbacks<P>, View.OnClickListener {

    private static final String TAG = "ReminderActivity";

    /**
     * Extras name for the reminder id.
     */
    public static final String EXTRA_REMINDER_ID = "reminder_id";
    /**
     * Extras name for the reminder title.
     */
    public static final String EXTRA_REMINDER_TITLE = "reminder_title";
    /**
     * Extras name for the reminder time.
     */
    public static final String EXTRA_REMINDER_TIME = "reminder_time";

    private LocaleCallbacks<P> localeCallbacks;
    private ThemeCallbacks<P> themeCallbacks;
    /**
     * The preferences.
     */
    private P preferences;
    private SimpleDateFormat dateFormat;
    private Format timeFormat;
    private long timeFormatGranularity;

    private TextView timeView;
    private TextView titleView;
    private View dismissView;

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
        timeView = findViewById(R.id.time);
        titleView = findViewById(android.R.id.title);
        dismissView = findViewById(R.id.reminder_dismiss);
        dismissView.setOnClickListener(this);

        final Context context = this;
        final Locale locale = LocaleUtils.getDefaultLocale(context);
        final P settings = getZmanimPreferences();
        boolean time24 = DateFormat.is24HourFormat(context);

        if (settings.isSeconds()) {
            String pattern = context.getString(time24 ? R.string.twenty_four_hour_time_format : R.string.twelve_hour_time_format);
            this.timeFormat = new SimpleDateFormat(pattern, locale);
            this.timeFormatGranularity = SECOND_IN_MILLIS;
        } else {
            this.timeFormat = DateFormat.getTimeFormat(context);
            this.timeFormatGranularity = MINUTE_IN_MILLIS;
        }

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

    protected void handleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            long when = extras.getLong(EXTRA_REMINDER_TIME, 0L);

            if (when > 0L) {
                int id = extras.getInt(EXTRA_REMINDER_ID);
                CharSequence contentTitle = extras.getCharSequence(EXTRA_REMINDER_TITLE);
                if ((id != 0) && (contentTitle == null)) {
                    contentTitle = getString(id);
                }
                ZmanimReminderItem reminderItem = new ZmanimReminderItem(id, contentTitle, null, when);
                notifyNow(reminderItem);
            } else {
                finish();
            }
        }
    }

    /**
     * Notify now.
     *
     * @param item the reminder item.
     */
    public void notifyNow(ZmanimReminderItem item) {
        notifyNow(getZmanimPreferences(), item);
    }

    /**
     * Notify now.
     *
     * @param settings the preferences.
     * @param item     the reminder item.
     */
    public void notifyNow(P settings, ZmanimReminderItem item) {
        LogUtils.i(TAG, "remind now [" + item.title + "] for [" + formatDateTime(item.time) + "]");

        String timeLabel = timeFormat.format(roundUp(item.time, timeFormatGranularity));

        timeView.setText(timeLabel);
        titleView.setText(item.title);

        startNoise();
    }

    /**
     * Format the date and time with seconds.<br>
     * The pattern is "{@code yyyy-MM-dd HH:mm:ss.SSS}"
     *
     * @param time the time to format.
     * @return the formatted time.
     */
    private String formatDateTime(Date time) {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        }
        return dateFormat.format(time);
    }

    /**
     * Format the date and time with seconds.
     *
     * @param time the time to format.
     * @return the formatted time.
     * @see #formatDateTime(Date)
     */
    private String formatDateTime(long time) {
        return formatDateTime(new Date(time));
    }

    @Override
    public void onClick(View view) {
        if (view == dismissView) {
            dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        // User must explicitly cancel the reminder.
    }

    /**
     * Dismiss the reminder.
     */
    public void dismiss() {
        stopNoise();
        cancelNotification();
        setResult(RESULT_CANCELED);
        finish();
    }

    private void cancelNotification() {
        //TODO implement me!
    }

    private void startNoise() {
        playSound();
        vibrate();
    }

    private void stopNoise() {
        stopSound();
        stopVibrate();
    }

    private void playSound() {
        //TODO play a repeating sound for "alarm"
        //TODO play a singular sound for "notification"
    }

    private void stopSound() {
        //TODO implement me!
    }

    private void vibrate() {
        //TODO implement me!
    }

    private void stopVibrate() {
        //TODO implement me!
    }
}
