package com.aura.reader.util

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.ui.graphics.vector.ImageVector
import com.aura.reader.R
import com.aura.reader.ui.theme.Strings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AmbientSoundType(
    val id: String,
    @RawRes val soundRes: Int?,
    val icon: ImageVector
) {
    OFF("off", null, Icons.Rounded.VolumeOff),
    RAIN("rain", R.raw.ambient_rain, Icons.Rounded.WaterDrop),
    FIREPLACE("fireplace", R.raw.ambient_fire, Icons.Rounded.LocalFireDepartment),
    FOREST("forest", R.raw.ambient_forest, Icons.Rounded.Forest),
    OCEAN("ocean", R.raw.ambient_ocean, Icons.Rounded.Waves);

    fun getTitle(strings: Strings): String = when (this) {
        OFF -> strings.ambientSoundOff
        RAIN -> strings.ambientSoundRain
        FIREPLACE -> strings.ambientSoundFire
        FOREST -> strings.ambientSoundForest
        OCEAN -> strings.ambientSoundOcean
    }

    companion object {
        fun fromId(id: String): AmbientSoundType = entries.firstOrNull { it.id == id } ?: OFF
    }
}

object AmbientSoundPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private val _currentSound = MutableStateFlow(AmbientSoundType.OFF)
    val currentSound: StateFlow<AmbientSoundType> = _currentSound.asStateFlow()

    private val _volume = MutableStateFlow(0.70f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    fun setSound(context: Context, sound: AmbientSoundType) {
        if (_currentSound.value == sound && sound != AmbientSoundType.OFF) return
        stop()
        _currentSound.value = sound
        if (sound == AmbientSoundType.OFF || sound.soundRes == null) return

        try {
            val mp = MediaPlayer.create(context.applicationContext, sound.soundRes) ?: return
            mp.isLooping = true
            val v = _volume.value
            mp.setVolume(v, v)
            mp.start()
            mediaPlayer = mp
        } catch (_: Exception) {
            _currentSound.value = AmbientSoundType.OFF
        }
    }

    fun setVolume(vol: Float) {
        val v = vol.coerceIn(0f, 1f)
        _volume.value = v
        mediaPlayer?.let { mp ->
            try {
                mp.setVolume(v, v)
            } catch (_: Exception) {}
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (_: Exception) {}
    }

    fun resume() {
        try {
            if (mediaPlayer != null && !mediaPlayer!!.isPlaying && _currentSound.value != AmbientSoundType.OFF) {
                mediaPlayer?.start()
            }
        } catch (_: Exception) {}
    }

    fun stop() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
        _currentSound.value = AmbientSoundType.OFF
    }
}
