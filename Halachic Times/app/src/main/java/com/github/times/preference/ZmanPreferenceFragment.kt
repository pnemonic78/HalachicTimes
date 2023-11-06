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
package com.github.times.preference

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.annotation.Keep
import androidx.annotation.XmlRes
import androidx.core.content.PermissionChecker
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.TwoStatePreference
import com.github.lang.isTrue
import com.github.preference.AbstractPreferenceFragment
import com.github.preference.SimplePreferences
import com.github.times.R
import com.github.times.remind.ZmanimReminder
import com.github.times.remind.ZmanimReminderService.Companion.enqueueWork

/**
 * This fragment shows the preferences for a zman screen.
 */
@Keep
open class ZmanPreferenceFragment : AbstractPreferenceFragment() {
    @XmlRes
    private var xmlId = 0
    private var preferenceReminderSunday: Preference? = null
    private var preferenceReminderMonday: Preference? = null
    private var preferenceReminderTuesday: Preference? = null
    private var preferenceReminderWednesday: Preference? = null
    private var preferenceReminderThursday: Preference? = null
    private var preferenceReminderFriday: Preference? = null
    private var preferenceReminderSaturday: Preference? = null

    protected val preferences: ZmanimPreferences by lazy { SimpleZmanimPreferences(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SimpleZmanimPreferences.init(requireContext())
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        val args = requireArguments()
        val opinionKey = args.getString(EXTRA_OPINION)
        val reminderKey = args.getString(EXTRA_REMINDER)
        initOpinion(opinionKey)
        initReminder(reminderKey)
    }

    override val preferencesXml: Int
        get() {
            if (xmlId == 0) {
                val args = requireArguments()
                val xmlName = args.getString(EXTRA_XML)
                val context = requireContext()
                val pkgName = context.packageName
                val res = context.resources
                xmlId = res.getIdentifier(xmlName, "xml", pkgName)
            }
            return xmlId
        }

    private fun initOpinion(opinionKey: String?) {
        if (!opinionKey.isNullOrEmpty()) {
            if (opinionKey.indexOf(';') > 0) {
                val tokens =
                    opinionKey.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (token in tokens) {
                    initOpinionPreference(token)
                }
            } else {
                initOpinionPreference(opinionKey)
            }
        }
    }

    private fun initReminder(reminderKey: String?) {
        var preference: Preference? = initList(reminderKey)
        if (preference == null) {
            preference = initTime(reminderKey)
        }
        if (preference != null) {
            val context = preference.context
            val listener = preference.onPreferenceChangeListener
            preference.setOnPreferenceChangeListener { _: Preference, newValue: Any? ->
                requestReminderPermissions(context)
                remind(context)
                listener?.onPreferenceChange(preference, newValue).isTrue
            }
            initReminderDays(preference)
        }
    }

    private fun initOpinionPreference(key: String?): Preference? {
        var preference: Preference?
        preference = initList(key)
        if (preference != null) {
            val context = preference.context
            preference.setOnPreferenceChangeListener { _: Preference, newValue: Any? ->
                maybeChooseMultipleOpinions(context, newValue)
                true
            }
            return preference
        }

        preference = initRingtone(key)
        if (preference != null) {
            return preference
        }

        preference = initTime(key)
        return preference
    }

    private fun initReminderDays(reminderTime: Preference) {
        val namePrefix = reminderTime.key
        preferenceReminderSunday =
            initReminderDay(namePrefix + ZmanimPreferences.REMINDER_SUNDAY_SUFFIX)
        preferenceReminderMonday =
            initReminderDay(namePrefix + ZmanimPreferences.REMINDER_MONDAY_SUFFIX)
        preferenceReminderTuesday =
            initReminderDay(namePrefix + ZmanimPreferences.REMINDER_TUESDAY_SUFFIX)
        preferenceReminderWednesday =
            initReminderDay(namePrefix + ZmanimPreferences.REMINDER_WEDNESDAY_SUFFIX)
        preferenceReminderThursday =
            initReminderDay(namePrefix + ZmanimPreferences.REMINDER_THURSDAY_SUFFIX)
        preferenceReminderFriday =
            initReminderDay(namePrefix + ZmanimPreferences.REMINDER_FRIDAY_SUFFIX)
        preferenceReminderSaturday =
            initReminderDay(namePrefix + ZmanimPreferences.REMINDER_SATURDAY_SUFFIX)
    }

    private fun initReminderDay(key: String?): Preference? {
        if (key.isNullOrEmpty()) return null
        val preference = findPreference<Preference>(key)
        if (preference != null) {
            preference.onPreferenceChangeListener = this
            return preference
        }
        return null
    }

    override fun onCheckBoxPreferenceChange(
        preference: TwoStatePreference,
        newValue: Any?
    ): Boolean {
        if (preference === preferenceReminderSunday
            || preference === preferenceReminderMonday
            || preference === preferenceReminderTuesday
            || preference === preferenceReminderWednesday
            || preference === preferenceReminderThursday
            || preference === preferenceReminderFriday
            || preference === preferenceReminderSaturday
        ) {
            remind(preference.context)
        }
        return super.onCheckBoxPreferenceChange(preference, newValue)
    }

    /**
     * Run the reminder service.
     * Tries to postpone the reminder until after any preferences have changed.
     */
    private fun remind(context: Context) {
        val intent = Intent(ZmanimReminder.ACTION_UPDATE)
        enqueueWork(context, intent)
    }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        val preferences = this.preferences
        return if (preferences is SimplePreferences) {
            preferences.preferences
        } else {
            PreferenceManager.getDefaultSharedPreferences(context)
        }
    }

    private fun maybeChooseMultipleOpinions(context: Context, newValue: Any?) {
        val opinionBaalHatanya = ZmanimPreferences.Values.OPINION_BAAL_HATANYA
        if (opinionBaalHatanya == newValue) {
            maybeChooseOpinionsBaalHatanya(context)
        }
    }

    private fun maybeChooseOpinionsBaalHatanya(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.opinion_baal_hatanya)
            .setMessage(R.string.opinion_baal_hatanya_all)
            .setCancelable(true)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ -> chooseBaalHatanyaOpinions(context) }
            .show()
    }

    /**
     * Select all relevant preferences to use Baal HaTanya's opinion.
     */
    private fun chooseBaalHatanyaOpinions(context: Context) {
        val opinion = ZmanimPreferences.Values.OPINION_BAAL_HATANYA
        val preferences = getSharedPreferences(context)
        preferences.edit()
            .putString(ZmanimPreferences.KEY_OPINION_HOUR, opinion)
            .putString(ZmanimPreferences.KEY_OPINION_DAWN, opinion)
            .putString(ZmanimPreferences.KEY_OPINION_TALLIS, opinion)
            .putString(ZmanimPreferences.KEY_OPINION_SUNRISE, ZmanimPreferences.Values.OPINION_SEA)
            .putString(ZmanimPreferences.KEY_OPINION_SHEMA, opinion)
            .putString(ZmanimPreferences.KEY_OPINION_TFILA, opinion)
            .putString(ZmanimPreferences.KEY_OPINION_EAT, opinion)
            .putString(ZmanimPreferences.KEY_OPINION_BURN, opinion)
            .putString(ZmanimPreferences.KEY_OPINION_NOON, opinion)
            .putString(ZmanimPreferences.KEY_OPINION_EARLIEST_MINCHA, opinion)
            .putString(ZmanimPreferences.KEY_OPINION_MINCHA, opinion)
            .putString(ZmanimPreferences.KEY_OPINION_PLUG_MINCHA, opinion)
            .putString(ZmanimPreferences.KEY_OPINION_SUNSET, ZmanimPreferences.Values.OPINION_SEA)
            .putString(ZmanimPreferences.KEY_OPINION_NIGHTFALL, opinion)
            .putString(
                ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_AFTER,
                ZmanimPreferences.Values.OPINION_NIGHT
            )
            .putString(
                ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_NIGHTFALL,
                ZmanimPreferences.Values.OPINION_8_5
            )
            .putString(ZmanimPreferences.KEY_OPINION_GUARDS, ZmanimPreferences.Values.OPINION_3)
            .putInt(ZmanimPreferences.KEY_OPINION_CANDLES, 30)
            .putInt(ZmanimPreferences.KEY_OPINION_SHABBATH_ENDS_MINUTES, 0)
            .apply()
    }

    private fun requestReminderPermissions(context: Context) {
        val permissions = mutableListOf<String>()
        if (PermissionChecker.checkCallingOrSelfPermission(
                context,
                RingtonePreference.PERMISSION_RINGTONE
            ) != PermissionChecker.PERMISSION_GRANTED
        ) {
            permissions.add(RingtonePreference.PERMISSION_RINGTONE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!nm.areNotificationsEnabled()) {
                permissions.add(ZmanimReminder.PERMISSION_NOTIFICATIONS)
            }
        }
        if (permissions.size > 0) {
            requestPermissions(permissions.toTypedArray(), REQUEST_PERMISSIONS)
        }
    }

    companion object {
        const val EXTRA_XML = "xml"
        const val EXTRA_OPINION = "opinion"
        const val EXTRA_REMINDER = "reminder"
        private const val REQUEST_PERMISSIONS = 0x702E // TONE
    }
}