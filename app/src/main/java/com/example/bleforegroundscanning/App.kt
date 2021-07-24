package com.example.bleforegroundscanning

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat

val prefs: Prefs by lazy {
    App.prefs!!
}

class App : Application() {

    companion object {
        const val serviceChannelId: String = "service-channel-id"
        const val serviceChannelName: String = "foreground-service-name"
        var prefs: Prefs? = null
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
        prefs = Prefs(applicationContext)

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(serviceChannelId, serviceChannelName, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC; //will be shown in lock screen
            notificationChannel.enableVibration(true)
            notificationChannel.enableLights(true)
            notificationChannel.setShowBadge(true)
            val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            notificationChannel.setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    audioAttributes
            )
            NotificationManagerCompat.from(this).createNotificationChannel(notificationChannel)
        }
    }
}