package com.github.times.remind

import android.annotation.TargetApi
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Vibrator
import android.text.format.DateUtils
import com.github.app.ActivityUtils
import com.github.media.RingtoneManager
import com.github.times.preference.RingtonePreference.PERMISSION_RINGTONE
import com.github.times.preference.SimpleZmanimPreferences
import com.github.times.preference.ZmanimPreferences
import timber.log.Timber
import java.io.IOException

class AlarmKlaxon(val context: Context, val preferences: ZmanimPreferences) {

    constructor(context: Context) : this(context, SimpleZmanimPreferences(context))

    private var ringtone: MediaPlayer? = null

    @TargetApi(Build.VERSION_CODES.M)
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (ActivityUtils.isPermissionGranted(PERMISSION_RINGTONE, permissions, grantResults)) {
                playSound(context)
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val audioAttributes = AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setLegacyStreamType(audioStreamType)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                        ringtone.setAudioAttributes(audioAttributes)
                    } else {
                        ringtone.setAudioStreamType(audioStreamType)
                    }
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
     * @param vibrate `true` to start vibrating - `false` to stop.
     */
    private fun vibrate(context: Context, vibrate: Boolean) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (!vibrator.hasVibrator()) {
            return
        }
        if (vibrate) {
            vibrator.vibrate(DateUtils.SECOND_IN_MILLIS)
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
    }
}