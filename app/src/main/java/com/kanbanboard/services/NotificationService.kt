package com.kanbanboard.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kanbanboard.R
import com.kanbanboard.activities.MainActivity
import com.kanbanboard.firebase.FirebaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationService : FirebaseMessagingService() {

    private val firebaseHelper = FirebaseHelper()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check if message contains a notification payload
        remoteMessage.notification?.let { notification ->
            sendNotification(
                title = notification.title ?: getString(R.string.app_name),
                message = notification.body ?: "",
                data = remoteMessage.data
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Update token in Firestore
        scope.launch {
            try {
                firebaseHelper.updateFCMToken()
            } catch (e: Exception) {
                // Handle token update failure
            }
        }
    }

    private fun sendNotification(
        title: String,
        message: String,
        data: Map<String, String>
    ) {
        // Create intent to open the app when notification is clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = getString(R.string.notification_channel_name)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_description)
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Show the notification
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    companion object {
        // Helper method to schedule task due notifications
        fun scheduleTaskNotification(context: Context, taskId: String, taskTitle: String, dueDate: Long) {
            // Create WorkManager request to schedule notification
            val data = mapOf(
                "taskId" to taskId,
                "taskTitle" to taskTitle,
                "dueDate" to dueDate.toString()
            )

            // Create notification channel
            createNotificationChannel(context)
        }

        private fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = context.getString(R.string.notification_channel_name)
                val descriptionText = context.getString(R.string.notification_channel_description)
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(
                    context.getString(R.string.notification_channel_name),
                    name,
                    importance
                ).apply {
                    description = descriptionText
                    enableLights(true)
                    enableVibration(true)
                }

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
