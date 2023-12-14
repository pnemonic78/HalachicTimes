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
package com.github.times.remind

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.text.style.RelativeSizeSpan
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker
import com.github.app.LocaleCallbacks
import com.github.app.LocaleHelper
import com.github.app.SimpleThemeCallbacks
import com.github.app.ThemeCallbacks
import com.github.lib.R
import com.github.text.style.TypefaceSpan
import com.github.times.BuildConfig
import com.github.times.ZmanimHelper.formatDateTime
import com.github.times.databinding.AlarmActivityBinding
import com.github.times.preference.RingtonePreference
import com.github.times.preference.SimpleZmanimPreferences
import com.github.times.preference.ZmanimPreferences
import com.github.times.remind.ZmanimReminderItem.Companion.from
import com.github.util.LocaleUtils.getDefaultLocale
import com.github.util.TimeUtils.roundUp
import java.text.Format
import java.text.SimpleDateFormat
import timber.log.Timber

/**
 * Shows a reminder alarm for a (*zman*).
 *
 * @author Moshe Waisberg
 */
class AlarmActivity<P : ZmanimPreferences> : AppCompatActivity(), ThemeCallbacks<P> {

    private lateinit var localeCallbacks: LocaleCallbacks<P>
    private val themeCallbacks: ThemeCallbacks<P> by lazy { createThemeCallbacks(this) }

    @Suppress("UNCHECKED_CAST")
    private val preferences: P by lazy { SimpleZmanimPreferences(this) as P }
    private lateinit var timeFormat: Format
    private var timeFormatGranularity: Long = 0
    private var binding: AlarmActivityBinding? = null
    private val handler = Handler(Looper.getMainLooper())
    private var silenceRunnable: Runnable? = null

    override fun attachBaseContext(newBase: Context) {
        localeCallbacks = LocaleHelper(newBase)
        val context = localeCallbacks.attachBaseContext(newBase)
        super.attachBaseContext(context)
        applyOverrideConfiguration(context.resources.configuration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        onPreCreate()
        super.onCreate(savedInstanceState)

        // Turn on the screen.
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val binding = AlarmActivityBinding.inflate(layoutInflater)
        this.binding = binding
        setContentView(binding.root)

        binding.reminderDismiss.setOnClickListener { dismiss(true) }
        val context: Context = this
        val locale = getDefaultLocale(context)
        val prefs = preferences
        if (prefs.isSeconds) {
            val time24 = DateFormat.is24HourFormat(context)
            val pattern = if (time24) {
                context.getString(R.string.twenty_four_hour_time_format)
            } else {
                context.getString(R.string.twelve_hour_time_format)
            }
            timeFormat = SimpleDateFormat(pattern, locale)
            timeFormatGranularity = DateUtils.SECOND_IN_MILLIS
        } else {
            timeFormat = DateFormat.getTimeFormat(context)
            timeFormatGranularity = DateUtils.MINUTE_IN_MILLIS
        }
        handleIntent(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions()
        }
    }

    override fun onPreCreate() {
        localeCallbacks.onPreCreate(this)
        themeCallbacks.onPreCreate()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        dismiss(isFinishing)
    }

    override val themePreferences: P
        get() = themeCallbacks.themePreferences

    private fun createThemeCallbacks(context: Context): ThemeCallbacks<P> {
        return SimpleThemeCallbacks(context, preferences)
    }

    private fun handleIntent(intent: Intent) {
        val context: Context = this
        val extras = intent.extras ?: return
        val item = from(context, extras)
        if (item.isNullOrEmpty()) {
            close()
        } else {
            notifyNow(item)
            if (extras.containsKey(EXTRA_SILENCE_TIME)) {
                val silenceAt = extras.getLong(EXTRA_SILENCE_TIME)
                silenceFuture(silenceAt)
            }
        }
    }

    /**
     * Notify now.
     *
     * @param item the reminder item.
     */
    private fun notifyNow(item: ZmanimReminderItem) {
        Timber.i("remind now [" + item.title + "] for [" + formatDateTime(item.time) + "]")
        if (item.isEmpty) {
            close()
            return
        }
        val binding = binding ?: return
        val timeLabel: CharSequence = timeFormat.format(roundUp(item.time, timeFormatGranularity))
        val spans = SpannableStringBuilder.valueOf(timeLabel)
        val indexMinutes = timeLabel.indexOf(':')
        if (indexMinutes >= 0) {
            // Regular "sans-serif" is like bold for "sans-serif-thin".
            spans.setSpan(
                TypefaceSpan(Typeface.SANS_SERIF),
                0,
                indexMinutes,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
            val indexSeconds = timeLabel.indexOf(':', indexMinutes + 1)
            if (indexSeconds > indexMinutes) {
                spans.setSpan(
                    RelativeSizeSpan(0.5f),
                    indexSeconds,
                    timeLabel.length,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
        }
        binding.time.text = spans
        binding.title.text = item.title
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // User must explicitly cancel the reminder.
        setResult(RESULT_CANCELED)
    }

    /**
     * Dismiss the reminder.
     *
     * @param finish is the activity finishing?
     */
    fun dismiss(finish: Boolean) {
        val silenceRunnable = silenceRunnable
        if (silenceRunnable != null) {
            handler.removeCallbacks(silenceRunnable)
        }
        if (finish) {
            stopService()
            setResult(RESULT_OK)
            close()
        }
    }

    private fun close() {
        finish()
    }

    /**
     * Set timer to silence the alert.
     *
     * @param triggerAt when to silence.
     */
    private fun silenceFuture(triggerAt: Long) {
        Timber.i("silence future at [%s] %d", formatDateTime(triggerAt), triggerAt)
        var silenceRunnable = silenceRunnable
        if (silenceRunnable == null) {
            silenceRunnable = Runnable { stopLock() }
            this.silenceRunnable = silenceRunnable
        }
        val now = System.currentTimeMillis()
        val delayMillis = triggerAt - now
        handler.postDelayed(silenceRunnable, delayMillis)
    }

    private fun stopLock() {
        // Allow the screen to sleep.
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun stopService(): Boolean {
        val context: Context = this
        val intent = Intent(context, ZmanimReminderService::class.java)
        return stopService(intent)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun checkPermissions() {
        val context: Context = this
        if (PermissionChecker.checkCallingOrSelfPermission(
                context,
                RingtonePreference.PERMISSION_RINGTONE
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(RingtonePreference.PERMISSION_RINGTONE), REQUEST_PERMISSIONS)
        }
    }

    companion object {
        /**
         * Extras name to silence to alarm.
         */
        const val EXTRA_SILENCE_TIME = BuildConfig.APPLICATION_ID + ".SILENCE_TIME"

        private const val REQUEST_PERMISSIONS = 0xA1A7 // ALARM
    }
}