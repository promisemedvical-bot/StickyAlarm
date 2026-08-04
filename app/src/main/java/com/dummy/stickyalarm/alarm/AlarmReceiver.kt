package com.dummy.stickyalarm.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getLongExtra("noteId", 0L)
        val title = intent.getStringExtra("title") ?: "Reminder"
        val body = intent.getStringExtra("body") ?: ""

        val channelId = "alarm_channel"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId, "Alarms", NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(context, AlarmOverlayActivity::class.java).apply {
            putExtra("noteId", noteId); putExtra("title", title); putExtra("body", body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val fullScreenPI = PendingIntent.getActivity(
            context, noteId.toInt(), fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPI, true)
            .setAutoCancel(true)
            .build()

        nm.notify(noteId.toInt(), notification)
    }
}
