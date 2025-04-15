package com.kanbanboard

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class KanbanApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Configure Firestore
        setupFirestore()
        
        // Create notification channels
        createNotificationChannels()
        
        // Load theme preference
        loadThemePreference()
    }

    private fun setupFirestore() {
        // Enable Firestore offline persistence
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create task reminders channel
            val taskChannel = NotificationChannel(
                getString(R.string.notification_channel_name),
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_description)
                enableLights(true)
                enableVibration(true)
            }

            // Register the channel with the system
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(taskChannel)
        }
    }

    private fun loadThemePreference() {
        // Get saved theme preference from SharedPreferences
        val sharedPrefs = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isDarkMode = sharedPrefs.getBoolean("dark_mode", false)
        
        // Set the theme
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    companion object {
        fun saveThemePreference(context: Context, isDarkMode: Boolean) {
            context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("dark_mode", isDarkMode)
                .apply()
        }
    }
}
