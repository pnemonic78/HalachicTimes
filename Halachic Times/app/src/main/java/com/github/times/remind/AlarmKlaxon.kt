package com.github.times.remind

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.format.DateUtils
import androidx.annotation.RequiresPermission
import com.github.app.ActivityUtils
import com.github.media.RingtoneManager
import com.github.times.preference.RingtonePreference.PERMISSION_RINGTONE
import com.github.times.preference.SimpleZmanimPreferences
import com.github.times.preference.ZmanimPreferences
import java.io.IOException
import timber.log.Timber

class AlarmKlaxon(val context: Context, val preferences: ZmanimPreferences) {

    constructor(context: Context) : this(context, SimpleZmanimPreferences(context))

    private var ringtone: MediaPlayer? = null

    @TargetApi(Build.VERSION_CODES.M)
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (ActivityUtils.isPermissionGranted(PERMISSION_RINGTONE, permissions, grantResults)) {
                startNoise()
            }
        }
    }

    private fun startNoise() {
        Timber.v("start noise")
        playSound(context)
        vibrate(context, true)
    }

    private fun stopNoise() {
        Timber.v("stop noise")
        stopSound()
        vibrate(context, false)
    }

    private fun playSound(context: Context) {
        val ringtone = getRingtone(context) ?: return
        Timber.v("play sound")
        if (!ringtone.isPlaying) {
            ringtone.start()
        }
    }

    private fun stopSound() {
        Timber.v("stop sound")
        val ringtone: MediaPlayer = this.ringtone ?: return
        try {
            ringtone.stop()
        } catch (e: IllegalStateException) {
            Timber.e(e, "error stopping sound: %s", e.localizedMessage)
        }
    }

    private fun getRingtone(context: Context): MediaPlayer? {
        var ringtone: MediaPlayer? = this.ringtone
        if (ringtone == null) {
            val prefRingtone: Uri? = preferences.reminderRingtone
            if (prefRingtone != null) {
                val uri = RingtoneManager.resolveUri(context, prefRingtone) ?: return null
                ringtone = MediaPlayer()
                try {
                    ringtone.setDataSource(context, uri)
                    val audioStreamType: Int = preferences.reminderStream
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setLegacyStreamType(audioStreamType)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                    ringtone.setAudioAttributes(audioAttributes)
                    ringtone.isLooping = audioStreamType == AudioManager.STREAM_ALARM
                    ringtone.prepare()
                } catch (e: IOException) {
                    Timber.e(
                        e,
                        "error preparing ringtone: %s for %s ~ %s",
                        e.localizedMessage,
                        prefRingtone,
                        uri
                    )
                    ringtone = null
                }
                this.ringtone = ringtone
            }
        }
        return ringtone
    }

    /**
     * Vibrate the device.
     *
     * @param context the context.
     * @param isVibrate `true` to start vibrating - `false` to stop.
     */
    private fun vibrate(context: Context, isVibrate: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            vibrate33(context, isVibrate)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrate26(context, isVibrate)
        } else {
            vibrateLegacy(context, isVibrate)
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrateLegacy(context: Context, isVibrate: Boolean) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (!vibrator.hasVibrator()) {
            return
        }
        if (isVibrate) {
            vibrator.vibrate(VIBRATE_DURATION)
        } else {
            vibrator.cancel()
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibrate26(context: Context, isVibrate: Boolean) {
        val vibrator = context.getSystemService(Vibrator::class.java) ?: return
        if (!vibrator.hasVibrator()) {
            return
        }
        if (isVibrate) {
            val vibe = VibrationEffect.createOneShot(
                VIBRATE_DURATION,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            vibrator.vibrate(vibe)
        } else {
            vibrator.cancel()
        }
    }

    @TargetApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibrate33(context: Context, isVibrate: Boolean) {
        val vibratorManager = context.getSystemService(VibratorManager::class.java) ?: return
        val vibrator = vibratorManager.defaultVibrator
        if (!vibrator.hasVibrator()) {
            return
        }
        if (isVibrate) {
            val vibe = VibrationEffect.createOneShot(
                VIBRATE_DURATION,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            val attributes = VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM)
            vibrator.vibrate(vibe, attributes)
        } else {
            vibrator.cancel()
        }
    }

    fun start() {
        startNoise()
    }

    fun stop() {
        stopNoise()
        val ringtone = ringtone
        if (ringtone != null) {
            ringtone.release()
            this.ringtone = null
        }
    }

    companion object {
        const val REQUEST_PERMISSIONS = 0x702E // TONE
        private const val VIBRATE_DURATION = DateUtils.SECOND_IN_MILLIS
    }
}