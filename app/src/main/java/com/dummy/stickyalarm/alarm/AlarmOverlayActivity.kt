package com.dummy.stickyalarm.alarm

import android.animation.ObjectAnimator
import android.app.KeyguardManager
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.dummy.stickyalarm.NoteEditorActivity
import com.dummy.stickyalarm.R

class AlarmOverlayActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var noteId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(KeyguardManager::class.java)
            km.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        setContentView(R.layout.activity_alarm_overlay)

        noteId = intent.getLongExtra("noteId", 0L)
        val title = intent.getStringExtra("title") ?: "Reminder"
        val body = intent.getStringExtra("body") ?: ""

        findViewById<android.widget.TextView>(R.id.txtAlarmTitle).text = title
        findViewById<android.widget.TextView>(R.id.txtAlarmBody).text = body

        val root = findViewById<android.widget.LinearLayout>(R.id.alarmRoot)
        ObjectAnimator.ofArgb(root, "backgroundColor", Color.parseColor("#FFAB40"), Color.parseColor("#FFF176")).apply {
            duration = 500
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
            start()
        }

        startAlarmSound()
        startVibration()

        findViewById<android.widget.Button>(R.id.btnDismiss).setOnClickListener {
            stopAlarm(); finish()
        }
        findViewById<android.widget.Button>(R.id.btnSnooze).setOnClickListener {
            AlarmScheduler.snooze(this, noteId, title, body, 5)
            stopAlarm(); finish()
        }
        findViewById<android.widget.Button>(R.id.btnOpenNote).setOnClickListener {
            stopAlarm()
            startActivity(Intent(this, NoteEditorActivity::class.java).apply {
                putExtra("noteId", noteId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            finish()
        }
    }

    private fun startAlarmSound() {
        try {
            val uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmOverlayActivity, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare(); start()
            }
        } catch (_: Exception) { }
    }

    private fun startVibration() {
        vibrator = getSystemService(Vibrator::class.java)
        val pattern = longArrayOf(0, 500, 500)
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopAlarm() {
        mediaPlayer?.stop(); mediaPlayer?.release(); mediaPlayer = null
        vibrator?.cancel()
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }
}
