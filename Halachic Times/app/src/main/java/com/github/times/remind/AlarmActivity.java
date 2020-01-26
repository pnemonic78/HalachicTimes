package com.github.times.remind;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.app.ActivityUtils;
import com.github.app.LocaleCallbacks;
import com.github.app.LocaleHelper;
import com.github.app.SimpleThemeCallbacks;
import com.github.app.ThemeCallbacks;
import com.github.media.RingtoneManager;
import com.github.text.style.TypefaceSpan;
import com.github.times.BuildConfig;
import com.github.times.R;
import com.github.times.ZmanimHelper;
import com.github.times.preference.SimpleZmanimPreferences;
import com.github.times.preference.ZmanimPreferences;
import com.github.util.LocaleUtils;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.PermissionChecker;
import timber.log.Timber;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static com.github.times.ZmanimItem.NEVER;
import static com.github.util.TimeUtils.roundUp;
import static java.lang.System.currentTimeMillis;

/**
 * Shows a reminder alarm for a (<em>zman</em>).
 *
 * @author Moshe Waisberg
 */
public class AlarmActivity<P extends ZmanimPreferences> extends Activity implements
    ThemeCallbacks<P>,
    View.OnClickListener {

    /**
     * Extras name for the reminder id.
     */
    public static final String EXTRA_REMINDER_ID = ZmanimReminderItem.EXTRA_ID;
    /**
     * Extras name for the reminder title.
     */
    public static final String EXTRA_REMINDER_TITLE = ZmanimReminderItem.EXTRA_TITLE;
    /**
     * Extras name for the reminder time.
     */
    public static final String EXTRA_REMINDER_TIME = ZmanimReminderItem.EXTRA_TIME;
    /**
     * Extras name to silence to alarm.
     */
    public static final String EXTRA_SILENCE_TIME = BuildConfig.APPLICATION_ID + ".SILENCE_TIME";

    private static final int REQUEST_PERMISSIONS = 0x702E; // TONE

    private LocaleCallbacks<P> localeCallbacks;
    private ThemeCallbacks<P> themeCallbacks;
    /**
     * The preferences.
     */
    private P preferences;
    private Format timeFormat;
    private long timeFormatGranularity;
    private MediaPlayer ringtone;

    private TextView timeView;
    private TextView titleView;
    private View dismissView;

    private final Handler handler = new Handler();
    private Runnable silenceRunnable;

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

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        final Window win = getWindow();
        // Turn on the screen.
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.alarm_activity);

        timeView = findViewById(R.id.time);
        titleView = findViewById(android.R.id.title);
        dismissView = findViewById(R.id.reminder_dismiss);
        dismissView.setOnClickListener(this);

        final Context context = this;
        final Locale locale = LocaleUtils.getDefaultLocale(context);
        final P prefs = getZmanimPreferences();
        boolean time24 = DateFormat.is24HourFormat(context);

        if (prefs.isSeconds()) {
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
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismiss();
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

        timeView.setText(spans);
        titleView.setText(item.title);

        startNoise();
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
    public void onClick(View view) {
        if (view == dismissView) {
            dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        // User must explicitly cancel the reminder.
        setResult(RESULT_CANCELED);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            if (ActivityUtils.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE, permissions, grantResults)) {
                final Context context = this;
                playSound(context);
            }
        }
    }

    /**
     * Dismiss the reminder.
     */
    public void dismiss() {
        stopNoise();
        MediaPlayer ringtone = this.ringtone;
        if (ringtone != null) {
            ringtone.release();
            this.ringtone = null;
        }
        if (silenceRunnable != null) {
            handler.removeCallbacks(silenceRunnable);
        }
        setResult(RESULT_OK);
        close();
    }

    private void close() {
        finish();
    }

    private void startNoise() {
        Timber.v("start noise");
        final Context context = this;

        boolean allowSound = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
                allowSound = false;
            }
        }
        if (allowSound) {
            playSound(context);
        }
        vibrate(context, true);
    }

    private void stopNoise() {
        Timber.v("stop noise");
        final Context context = this;
        stopSound();
        vibrate(context, false);

        final Window win = getWindow();
        // Allow the screen to sleep.
        win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void playSound(Context context) {
        MediaPlayer ringtone = getRingtone(context);
        Timber.v("play sound");
        if ((ringtone != null) && !ringtone.isPlaying()) {
            ringtone.start();
        }
    }

    private void stopSound() {
        Timber.v("stop sound");
        MediaPlayer ringtone = this.ringtone;
        if (ringtone != null) {
            try {
                ringtone.stop();
            } catch (IllegalStateException e) {
                Timber.e(e, "error stopping sound: %s", e.getLocalizedMessage());
            }
        }
    }

    private MediaPlayer getRingtone(Context context) {
        MediaPlayer ringtone = this.ringtone;
        if (ringtone == null) {
            final P prefs = getZmanimPreferences();
            Uri prefRingtone = prefs.getReminderRingtone();
            if (prefRingtone != null) {
                Uri uri = RingtoneManager.resolveUri(context, prefRingtone);
                if (uri == null) {
                    return null;
                }
                ringtone = new MediaPlayer();
                try {
                    ringtone.setDataSource(context, uri);

                    int audioStreamType = prefs.getReminderStream();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setLegacyStreamType(audioStreamType)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build();
                        ringtone.setAudioAttributes(audioAttributes);
                    } else {
                        ringtone.setAudioStreamType(audioStreamType);
                    }

                    ringtone.setLooping(audioStreamType == AudioManager.STREAM_ALARM);
                    ringtone.prepare();
                } catch (IOException e) {
                    Timber.e(e, "error preparing ringtone: " + e.getLocalizedMessage() + " for " + prefRingtone + " ~ " + uri);
                    ringtone = null;
                }
                this.ringtone = ringtone;
            }
        }
        return ringtone;
    }

    /**
     * Vibrate the device.
     *
     * @param context the context.
     * @param vibrate {@code true} to start vibrating - {@code false} to stop.
     */
    private void vibrate(Context context, boolean vibrate) {
        Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        if ((vibrator == null) || !vibrator.hasVibrator()) {
            return;
        }
        if (vibrate) {
            vibrator.vibrate(DateUtils.SECOND_IN_MILLIS);
        } else {
            vibrator.cancel();
        }
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
                    stopNoise();
                }
            };
            this.silenceRunnable = silenceRunnable;
        }
        final long now = currentTimeMillis();
        long delayMillis = triggerAt - now;
        handler.postDelayed(silenceRunnable, delayMillis);
    }
}
