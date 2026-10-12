package com.example.data.call

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class CallHardwareManager(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var toneGenerator: ToneGenerator? = null
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var previousAudioMode = AudioManager.MODE_NORMAL
    private var previousSpeakerState = false
    private var previousMicrophoneMuteState = false

    fun onCallStarted(isSpeakerOn: Boolean) {
        try {
            audioManager?.let { am ->
                previousAudioMode = am.mode
                previousSpeakerState = am.isSpeakerphoneOn
                previousMicrophoneMuteState = am.isMicrophoneMute

                am.mode = AudioManager.MODE_IN_COMMUNICATION
                am.isSpeakerphoneOn = isSpeakerOn
                am.isMicrophoneMute = false
            }
        } catch (e: Exception) {
            Log.e("CallHardwareManager", "Error configuring audio hardware", e)
        }
    }

    fun startOutgoingRingbackTone() {
        stopTone()
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 70)
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 3500)
        } catch (e: Exception) {
            Log.e("CallHardwareManager", "Error playing ringback tone", e)
        }
    }

    fun startVibrationFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(200)
            }
        } catch (e: Exception) {
            Log.e("CallHardwareManager", "Error vibrating", e)
        }
    }

    fun stopTone() {
        try {
            toneGenerator?.stopTone()
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            Log.e("CallHardwareManager", "Error stopping tone", e)
        }
    }

    fun setMuted(muted: Boolean) {
        try {
            audioManager?.isMicrophoneMute = muted
        } catch (e: Exception) {
            Log.e("CallHardwareManager", "Error setting mic mute", e)
        }
    }

    fun setSpeakerOn(speakerOn: Boolean) {
        try {
            audioManager?.isSpeakerphoneOn = speakerOn
        } catch (e: Exception) {
            Log.e("CallHardwareManager", "Error setting speakerphone", e)
        }
    }

    fun onCallEnded() {
        stopTone()
        try {
            audioManager?.let { am ->
                am.mode = AudioManager.MODE_NORMAL
                am.isSpeakerphoneOn = previousSpeakerState
                am.isMicrophoneMute = previousMicrophoneMuteState
            }
        } catch (e: Exception) {
            Log.e("CallHardwareManager", "Error restoring audio mode", e)
        }
    }
}
