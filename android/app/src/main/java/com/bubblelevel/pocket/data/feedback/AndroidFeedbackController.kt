package com.bubblelevel.pocket.data.feedback

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import com.bubblelevel.pocket.domain.feedback.FeedbackController

class AndroidFeedbackController(context: Context) : FeedbackController {
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var tone: ToneGenerator? = null
    override fun levelTone() {
        if (audio.getStreamVolume(AudioManager.STREAM_MUSIC) == 0 || audio.ringerMode == AudioManager.RINGER_MODE_SILENT) return
        runCatching {
            val player = tone ?: ToneGenerator(AudioManager.STREAM_MUSIC, 18).also { tone = it }
            player.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
        }
    }
    override fun release() {
        tone?.release()
        tone = null
    }
}
