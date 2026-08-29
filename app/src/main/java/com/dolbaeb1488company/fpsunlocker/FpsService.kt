package com.dolbaeb1488company.fpsunlocker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dolbaeb1488company.fpsunlocker.model.OriginSettingsConstants

class FpsService : Service() {

    private val CHANNEL_ID = "fps_unlocker_channel"
    private val NOTIFICATION_ID = 101

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == OriginSettingsConstants.ACTION_TOGGLE_FPS) {
                toggleFps()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val filter = IntentFilter(OriginSettingsConstants.ACTION_TOGGLE_FPS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(receiver, filter)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(NOTIFICATION_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
                } else {
                    startForeground(NOTIFICATION_ID, buildNotification())
                }
            } else {
                startForeground(NOTIFICATION_ID, buildNotification())
            }
        } catch (e: Exception) {
            Log.e("FpsService", "Error starting foreground service: ${e.message}", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        updateNotification()
        return START_STICKY
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
            Log.w("FpsService", "Receiver error on destroy: ${e.message}")
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun toggleFps() {
        try {
            val currentValue = Settings.System.getString(contentResolver, OriginSettingsConstants.SETTING_FPS_INTERPOLATION)
            val isChecked = currentValue != OriginSettingsConstants.VALUE_144_FORCE
            val newValue = if (isChecked) OriginSettingsConstants.VALUE_144_FORCE else OriginSettingsConstants.VALUE_STOCK
            Settings.System.putString(contentResolver, OriginSettingsConstants.SETTING_FPS_INTERPOLATION, newValue)

            // Notify UI if running
            sendBroadcast(
                Intent(OriginSettingsConstants.ACTION_UPDATE_UI)
                    .putExtra("isChecked", isChecked)
                    .setPackage(packageName)
            )

            // Update notification
            updateNotification()
        } catch (e: Exception) {
            Log.e("FpsService", "Error toggling FPS: ${e.message}", e)
        }
    }

    private fun updateNotification() {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, buildNotification())
        } catch (e: Exception) {
            Log.e("FpsService", "Error updating notification", e)
        }
    }

    private fun buildNotification(): Notification {
        val currentValue = Settings.System.getString(contentResolver, OriginSettingsConstants.SETTING_FPS_INTERPOLATION)
        val isEnabled = currentValue == OriginSettingsConstants.VALUE_144_FORCE
        val statusText = if (isEnabled) getString(R.string.on) else getString(R.string.off)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val toggleIntent = Intent(OriginSettingsConstants.ACTION_TOGGLE_FPS).apply {
            setPackage(packageName)
        }
        val togglePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            toggleIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val actionText = if (isEnabled) getString(R.string.turn_off) else getString(R.string.turn_on)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content, statusText))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_manage, actionText, togglePendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "FPS Unlocker Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Quick toggle and status notification for OriginOS 144 FPS"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
