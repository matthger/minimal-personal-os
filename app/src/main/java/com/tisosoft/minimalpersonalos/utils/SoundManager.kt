package com.tisosoft.minimalpersonalos.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.tisosoft.minimalpersonalos.R

class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private val clickSoundId: Int
    private val errorSoundId: Int

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        clickSoundId = soundPool.load(context, R.raw.click_sound, 1)
        errorSoundId = soundPool.load(context, R.raw.error_sound, 1)
    }

    fun playClick(volume: Float = 1f) {
        soundPool.play(clickSoundId, volume, volume, 0, 0, 1f)
    }

    fun playError(volume: Float = 1f) {
        soundPool.play(errorSoundId, volume, volume, 0, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}
