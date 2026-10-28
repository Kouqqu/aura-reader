package com.aura.reader.util

import android.content.Context
import android.media.MediaPlayer
import com.aura.reader.R
import com.aura.reader.data.model.HolidaySeason

object HolidaySoundPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun playHolidaySound(context: Context, season: HolidaySeason) {
        if (season == HolidaySeason.HALLOWEEN) {
            playHalloweenSound(context)
        }
    }

    fun playHalloweenSound(context: Context) {
        try {
            stop()
            mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.halloween_sound)?.apply {
                setOnCompletionListener { mp ->
                    try {
                        mp.release()
                    } catch (e: Exception) {}
                    if (mediaPlayer == mp) {
                        mediaPlayer = null
                    }
                }
                start()
            }
        } catch (e: Exception) {
            // Ignore failure on audio initialization
        }
    }

    fun stop() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            // Ignore cleanup failure
        }
    }
}
