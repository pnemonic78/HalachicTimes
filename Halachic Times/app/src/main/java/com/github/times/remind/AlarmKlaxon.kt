package com.github.times.remind

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.text.format.DateUtils
import com.github.media.RingtoneManager
import com.github.os.VibratorCompat
import com.github.times.preference.SimpleZmanimPreferences
import com.github.times.preference.ZmanimPreferences
import java.io.IOException
import timber.log.Timber

class AlarmKlaxon(private val context: Context, private val preferences: ZmanimPreferences) {

    constructor(context: Context) : this(context, SimpleZmanimPreferences(context))

    private var ringtone: MediaPlayer? = null
    private val vibrator = VibratorCompat(context)

    private fun startNoise() {
        Timber.v("start noise")
        playSound(context)
        vibrate(true)
    }

    private fun stopNoise() {
        Timber.v("stop noise")
        stopSound()
        vibrate(false)
    }

    private fun playSound(context: Context) {
        val ringtone = getRingtone(context)
        Timber.v("play sound %s", ringtone)
        if ((ringtone != null) && !ringtone.isPlaying) {
            ringtone.start()
        }
    }

    private fun stopSound() {
        Timber.v("stop sound")
        val ringtone: MediaPlayer = this.ringtone ?: return
        try {
            ringtone.stop()
        } catch (e: IllegalStateException) {
            Timber.e(e, "error stopping sound: %s", e.message)
        }
    }

    private fun getRingtone(context: Context): MediaPlayer? {
        var ringtone: MediaPlayer? = this.ringtone
        if (ringtone == null) {
            val prefRingtone: Uri? = preferences.reminderRingtone
            if (prefRingtone != null) {
                val uri = RingtoneManager.resolveUri(context, prefRingtone) ?: return null
                try {
                    val audioStreamType: Int = preferences.reminderStream
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setLegacyStreamType(audioStreamType)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                    ringtone = MediaPlayer().apply {
                        setDataSource(context, uri)
                        setAudioAttributes(audioAttributes)
                        isLooping = audioStreamType == AudioManager.STREAM_ALARM
                        prepare()
                    }
                } catch (e: IOException) {
                    Timber.e(
                        e,
                        "error preparing ringtone: %s for %s ~ %s",
                        e.message,
                        prefRingtone,
                        uri
                    )
                }
                this.ringtone = ringtone
            }
        }
        return ringtone
    }

    /**
     * Vibrate the device.
     *
     * @param isVibrate `true` to start vibrating - `false` to stop.
     */
    private fun vibrate(isVibrate: Boolean) {
        Timber.v("vibrate $isVibrate")
        if (isVibrate) {
            vibrator.vibrate(VIBRATE_DURATION, VibratorCompat.USAGE_ALARM)
        } else {
            vibrator.cancel()
        }
    }

    fun start() {
        Timber.v("start")
        startNoise()
    }

    fun stop() {
        Timber.v("stop")
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