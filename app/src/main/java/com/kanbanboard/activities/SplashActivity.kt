package com.kanbanboard.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.kanbanboard.R
import com.kanbanboard.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Add animation to logo and app name
        binding.apply {
            ivLogo.alpha = 0f
            tvAppName.alpha = 0f

            ivLogo.animate().alpha(1f).setDuration(1000).start()
            tvAppName.animate().alpha(1f).setDuration(1000).start()
        }

        // Delay for 2 seconds then check auth state
        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthState()
        }, SPLASH_DELAY)
    }

    private fun checkAuthState() {
        // Check if user is signed in
        val currentUser = auth.currentUser
        val intent = if (currentUser != null) {
            // User is signed in, go to MainActivity
            Intent(this, MainActivity::class.java)
        } else {
            // No user is signed in, go to AuthActivity
            Intent(this, AuthActivity::class.java)
        }
        
        // Clear back stack
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        private const val SPLASH_DELAY = 2000L // 2 seconds
    }
}
