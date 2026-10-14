package com.example.util

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper

/**
 * AudioBeepSuppressor
 *
 * Implements targeted, session-level stream muting to eliminate repeated beep sounds
 * during voice dictation without silencing phone ringtones, notifications, or calls.
 *
 * Features:
 * 1. Targeted Stream Muting: Only STREAM_MUSIC and STREAM_SYSTEM are muted.
 *    STREAM_RING, STREAM_NOTIFICATION, and STREAM_VOICE_CALL remain completely untouched.
 * 2. Session-Level Lifecycle: Muted once when dictation begins, kept muted through intermediate
 *    pauses and restarts, and unmuted with a 400ms delay on stop to swallow the shutdown tone.
 * 3. Crash Safety Protection: Persists muting state in SharedPreferences before adjusting volumes,
 *    and restores original volumes immediately on Application startup if a crash or kill occurred.
 */
class AudioBeepSuppressor(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private val safetyPrefs = context.getSharedPreferences(PREFS_AUDIO_SAFETY, Context.MODE_PRIVATE)

    @Volatile
    private var isSessionSuppressed = false
    private var savedMusicVol: Int = -1
    private var savedSystemVol: Int = -1
    private var pendingUnmuteRunnable: Runnable? = null

    @Synchronized
    fun suppressForSession() {
        val am = audioManager ?: return

        // Cancel any pending un-mute delayed runnable if a new session starts
        pendingUnmuteRunnable?.let { mainHandler.removeCallbacks(it) }
        pendingUnmuteRunnable = null

        if (isSessionSuppressed) return

        try {
            // 1. Save current volumes before starting dictation
            savedMusicVol = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            savedSystemVol = am.getStreamVolume(AudioManager.STREAM_SYSTEM)

            // Crash Safety: Write record immediately before adjusting volumes
            safetyPrefs.edit()
                .putBoolean(KEY_MUTED_BY_APP, true)
                .putInt(KEY_SAVED_MUSIC_VOL, savedMusicVol)
                .putInt(KEY_SAVED_SYSTEM_VOL, savedSystemVol)
                .commit()

            // 2. Mute selectively with flags = 0 to avoid showing Android volume slider HUD
            // Ring and notification streams remain completely untouched
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
            am.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0)

            isSessionSuppressed = true
            DiagnosticsLog.log(
                DiagnosticsLog.Category.SYSTEM,
                "Continuous beep suppression engaged (STREAM_MUSIC & STREAM_SYSTEM muted, alerts intact)"
            )
        } catch (e: Exception) {
            DiagnosticsLog.log(DiagnosticsLog.Category.SYSTEM, "Beep suppression setup error: ${e.message}")
        }
    }

    @Synchronized
    fun restoreSessionVolumes(delayed: Boolean = true) {
        val am = audioManager ?: return
        if (!isSessionSuppressed && savedMusicVol < 0 && savedSystemVol < 0) return

        pendingUnmuteRunnable?.let { mainHandler.removeCallbacks(it) }

        val unmuteAction = Runnable {
            synchronized(this@AudioBeepSuppressor) {
                try {
                    // Unmute and restore STREAM_MUSIC
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
                    if (savedMusicVol >= 0) {
                        try { am.setStreamVolume(AudioManager.STREAM_MUSIC, savedMusicVol, 0) } catch (ignored: Exception) {}
                    }

                    // Unmute and restore STREAM_SYSTEM
                    am.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0)
                    if (savedSystemVol >= 0) {
                        try { am.setStreamVolume(AudioManager.STREAM_SYSTEM, savedSystemVol, 0) } catch (ignored: Exception) {}
                    }

                    // Clear crash safety record
                    safetyPrefs.edit().clear().commit()
                    DiagnosticsLog.log(DiagnosticsLog.Category.SYSTEM, "Session audio streams unmuted and restored")
                } catch (e: Exception) {
                    DiagnosticsLog.log(DiagnosticsLog.Category.SYSTEM, "Error restoring audio volumes: ${e.message}")
                } finally {
                    isSessionSuppressed = false
                    savedMusicVol = -1
                    savedSystemVol = -1
                    pendingUnmuteRunnable = null
                }
            }
        }

        if (delayed) {
            // Google Speech Services emits a subtle shutdown/cancellation tone right after
            // stopListening() or cancel(). The 400 ms delay ensures this final sound plays into
            // the muted stream before system audio is restored.
            pendingUnmuteRunnable = unmuteAction
            mainHandler.postDelayed(unmuteAction, UNMUTE_DELAY_MS)
        } else {
            unmuteAction.run()
        }
    }

    @Synchronized
    fun muteBeforeStart() {
        if (!isSessionSuppressed) {
            suppressForSession()
        }
    }

    @Synchronized
    fun restoreAfterStart() {
        // Intermediate restarts within an active session must remain muted
        if (isSessionSuppressed) return
        restoreSessionVolumes(delayed = false)
    }

    @Synchronized
    fun release() {
        restoreSessionVolumes(delayed = false)
    }

    companion object {
        private const val PREFS_AUDIO_SAFETY = "kotha_audio_safety_prefs"
        private const val KEY_MUTED_BY_APP = "muted_by_app"
        private const val KEY_SAVED_MUSIC_VOL = "saved_music_vol"
        private const val KEY_SAVED_SYSTEM_VOL = "saved_system_vol"
        private const val UNMUTE_DELAY_MS = 400L

        /**
         * Crash Safety Protection:
         * If the user kills the app or an unexpected crash occurs during voice typing
         * while streams were muted, restores original volumes immediately on startup.
         */
        fun restoreCrashSafetyIfNeeded(context: Context) {
            try {
                val prefs = context.getSharedPreferences(PREFS_AUDIO_SAFETY, Context.MODE_PRIVATE)
                if (prefs.getBoolean(KEY_MUTED_BY_APP, false)) {
                    val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                    if (am != null) {
                        val savedMusic = prefs.getInt(KEY_SAVED_MUSIC_VOL, -1)
                        val savedSystem = prefs.getInt(KEY_SAVED_SYSTEM_VOL, -1)

                        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
                        if (savedMusic >= 0) {
                            try { am.setStreamVolume(AudioManager.STREAM_MUSIC, savedMusic, 0) } catch (ignored: Exception) {}
                        }

                        am.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0)
                        if (savedSystem >= 0) {
                            try { am.setStreamVolume(AudioManager.STREAM_SYSTEM, savedSystem, 0) } catch (ignored: Exception) {}
                        }
                    }
                    prefs.edit().clear().commit()
                    DiagnosticsLog.log(DiagnosticsLog.Category.SYSTEM, "Crash safety: restored audio volumes from previous interrupted session")
                }
            } catch (e: Exception) {
                DiagnosticsLog.log(DiagnosticsLog.Category.SYSTEM, "Crash safety check error: ${e.message}")
            }
        }
    }
}
