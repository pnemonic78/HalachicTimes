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
package com.github.times.remind;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static com.github.times.ZmanimItem.NEVER;
import static com.github.util.TimeUtils.roundUp;
import static java.lang.System.currentTimeMillis;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.github.app.LocaleCallbacks;
import com.github.app.LocaleHelper;
import com.github.app.SimpleThemeCallbacks;
import com.github.app.ThemeCallbacks;
import com.github.text.style.TypefaceSpan;
import com.github.times.BuildConfig;
import com.github.times.ZmanimHelper;
import com.github.times.databinding.AlarmActivityBinding;
import com.github.times.preference.SimpleZmanimPreferences;
import com.github.times.preference.ZmanimPreferences;
import com.github.util.LocaleUtils;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

/**
 * Shows a reminder alarm for a (<em>zman</em>).
 *
 * @author Moshe Waisberg
 */
public class AlarmActivity<P extends ZmanimPreferences> extends AppCompatActivity implements
    ThemeCallbacks<P> {

    /**
     * Extras name to silence to alarm.
     */
    public static final String EXTRA_SILENCE_TIME = BuildConfig.APPLICATION_ID + ".SILENCE_TIME";

    private LocaleCallbacks<P> localeCallbacks;
    private ThemeCallbacks<P> themeCallbacks;
    /**
     * The preferences.
     */
    private P preferences;
    private Format timeFormat;
    private long timeFormatGranularity;

    private AlarmActivityBinding binding;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable silenceRunnable;

    @Override
    protected void attachBaseContext(Context newBase) {
        this.localeCallbacks = new LocaleHelper<>(newBase);
        Context context = localeCallbacks.attachBaseContext(newBase);
        super.attachBaseContext(context);

        applyOverrideConfiguration(context.getResources().getConfiguration());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        onPreCreate();
        super.onCreate(savedInstanceState);

        final Window win = getWindow();
        // Turn on the screen.
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        AlarmActivityBinding binding = AlarmActivityBinding.inflate(getLayoutInflater());
        this.binding = binding;
        setContentView(binding.getRoot());

        binding.reminderDismiss.setOnClickListener(v -> dismiss(true));

        final Context context = this;
        final Locale locale = LocaleUtils.getDefaultLocale(context);
        final P prefs = getZmanimPreferences();
        boolean time24 = DateFormat.is24HourFormat(context);

        if (prefs.isSeconds()) {
            String pattern = time24 ? context.getString(com.github.lib.R.string.twenty_four_hour_time_format)
                : context.getString(com.github.lib.R.string.twelve_hour_time_format);
            this.timeFormat = new SimpleDateFormat(pattern, locale);
            this.timeFormatGranularity = SECOND_IN_MILLIS;
        } else {
            this.timeFormat = DateFormat.getTimeFormat(context);
            this.timeFormatGranularity = MINUTE_IN_MILLIS;
        }

        handleIntent(getIntent());
    }

    @Override
    public void onPreCreate() {
        getThemeCallbacks().onPreCreate();
        localeCallbacks.onPreCreate(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismiss(isFinishing());
    }

    @Override
    public P getThemePreferences() {
        return getThemeCallbacks().getThemePreferences();
    }

    protected ThemeCallbacks<P> getThemeCallbacks() {
        ThemeCallbacks<P> themeCallbacks = this.themeCallbacks;
        if (themeCallbacks == null) {
            themeCallbacks = createThemeCallbacks(this);
            this.themeCallbacks = themeCallbacks;
        }
        return themeCallbacks;
    }

    protected ThemeCallbacks<P> createThemeCallbacks(Context context) {
        return new SimpleThemeCallbacks<>(context, getZmanimPreferences());
    }

    public P getZmanimPreferences() {
        P preferences = this.preferences;
        if (preferences == null) {
            final Context context = this;
            preferences = (P) new SimpleZmanimPreferences(context);
            this.preferences = preferences;
        }
        return preferences;
    }

    protected void handleIntent(Intent intent) {
        final Context context = this;
        Bundle extras = intent.getExtras();
        ZmanimReminderItem item = ZmanimReminderItem.from(context, extras);
        if (item != null) {
            if (item.isEmpty()) {
                close();
            } else {
                notifyNow(item);
            }

            if (extras.containsKey(EXTRA_SILENCE_TIME)) {
                long triggerAt = extras.getLong(EXTRA_SILENCE_TIME);
                silenceFuture(triggerAt);
            }
        } else {
            close();
        }
    }

    /**
     * Notify now.
     *
     * @param item the reminder item.
     */
    public void notifyNow(ZmanimReminderItem item) {
        Timber.i("remind now [" + item.title + "] for [" + formatDateTime(item.time) + "]");
        if (item.isEmpty()) {
            close();
            return;
        }

        CharSequence timeLabel = timeFormat.format(roundUp(item.time, timeFormatGranularity));
        SpannableStringBuilder spans = SpannableStringBuilder.valueOf(timeLabel);
        int indexMinutes = TextUtils.indexOf(timeLabel, ':');
        if (indexMinutes >= 0) {
            // Regular "sans-serif" is like bold for "sans-serif-thin".
            spans.setSpan(new TypefaceSpan(Typeface.SANS_SERIF), 0, indexMinutes, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            int indexSeconds = TextUtils.indexOf(timeLabel, ':', indexMinutes + 1);
            if (indexSeconds > indexMinutes) {
                spans.setSpan(new RelativeSizeSpan(0.5f), indexSeconds, timeLabel.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }

        binding.time.setText(spans);
        binding.title.setText(item.title);
    }

    /**
     * Format the date and time with seconds.
     *
     * @param time the time to format.
     * @return the formatted time.
     */
    private String formatDateTime(long time) {
        if (time == NEVER) {
            return "NEVER";
        }
        return ZmanimHelper.formatDateTime(new Date(time));
    }

    @Override
    public void onBackPressed() {
        // User must explicitly cancel the reminder.
        setResult(RESULT_CANCELED);
    }

//    @Override
//    @TargetApi(Build.VERSION_CODES.M)
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == AlarmKlaxon.REQUEST_PERMISSIONS) {
//            service.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//    }

    /**
     * Dismiss the reminder.
     *
     * @param finish is the activity finishing?
     */
    public void dismiss(boolean finish) {
        final Runnable silenceRunnable = this.silenceRunnable;
        if (silenceRunnable != null) {
            handler.removeCallbacks(silenceRunnable);
        }
        if (finish) {
            stopService();
            setResult(RESULT_OK);
            close();
        }
    }

    private void close() {
        finish();
    }

    /**
     * Set timer to silence the alert.
     *
     * @param triggerAt when to silence.
     */
    private void silenceFuture(long triggerAt) {
        Timber.i("silence future at [" + formatDateTime(triggerAt) + "]");

        Runnable silenceRunnable = this.silenceRunnable;
        if (silenceRunnable == null) {
            silenceRunnable = new Runnable() {
                @Override
                public void run() {
                    stopLock();
                }
            };
            this.silenceRunnable = silenceRunnable;
        }
        final long now = currentTimeMillis();
        long delayMillis = triggerAt - now;
        handler.postDelayed(silenceRunnable, delayMillis);
    }

    private void stopLock() {
        final Window win = this.getWindow();
        // Allow the screen to sleep.
        win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private boolean stopService() {
        final Context context = this;
        Intent intent = new Intent(context, ZmanimReminderService.class);
        return stopService(intent);
    }
}
