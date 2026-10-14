package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.settings.SettingsRepository

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }

        DiagnosticsLog.log(
            DiagnosticsLog.Category.SYSTEM,
            "BootReceiver triggered with action: $action"
        )

        val settingsRepo = SettingsRepository(context)
        val keeper = AccessibilityKeeper(context, settingsRepo)
        keeper.attemptRepairIfNeeded(source = "BootReceiver")

        if (settingsRepo.cachedSettings.showBootNotification) {
            postBootNotification(context)
        }
    }

    private fun postBootNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val channelId = "kotha_boot_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            101,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_boot_title))
            .setContentText(context.getString(R.string.notification_boot_text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        nm.notify(1001, notification)
    }
}
