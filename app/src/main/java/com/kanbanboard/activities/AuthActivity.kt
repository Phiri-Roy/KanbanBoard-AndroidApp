package com.kanbanboard.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.kanbanboard.R
import com.kanbanboard.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Primary action button (Login/Signup)
        binding.btnPrimaryAction.setOnClickListener {
            if (validateInputs()) {
                showLoading(true)
                if (isLoginMode) {
                    handleLogin()
                } else {
                    handleSignup()
                }
            }
        }

        // Toggle between login and signup
        binding.btnAuthToggle.setOnClickListener {
            toggleAuthMode()
        }

        // Forgot password
        binding.btnForgotPassword.setOnClickListener {
            handleForgotPassword()
        }
    }

    private fun validateInputs(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            return false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return false
        }

        if (!isLoginMode) {
            val name = binding.etName.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (name.isEmpty()) {
                binding.tilName.error = "Name is required"
                return false
            }

            if (password != confirmPassword) {
                binding.tilConfirmPassword.error = "Passwords do not match"
                return false
            }
        }

        // Clear any previous errors
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilName?.error = null
        binding.tilConfirmPassword?.error = null

        return true
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    // Sign in success
                    navigateToMain()
                } else {
                    // If sign in fails, display a message to the user
                    Toast.makeText(this, getString(R.string.msg_error_login),
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun handleSignup() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val name = binding.etName.text.toString().trim()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign up success, create user document in Firestore
                    val user = auth.currentUser
                    if (user != null) {
                        createUserDocument(user, name)
                    } else {
                        showLoading(false)
                        Toast.makeText(this, getString(R.string.msg_error_signup),
                            Toast.LENGTH_SHORT).show()
                    }
                } else {
                    showLoading(false)
                    // If sign up fails, display a message to the user
                    Toast.makeText(this, getString(R.string.msg_error_signup),
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createUserDocument(user: FirebaseUser, name: String) {
        val userMap = hashMapOf(
            "name" to name,
            "email" to user.email,
            "darkMode" to false
        )

        db.collection("users").document(user.uid)
            .set(userMap)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, getString(R.string.msg_signup_success),
                    Toast.LENGTH_SHORT).show()
                navigateToMain()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, getString(R.string.msg_error_signup),
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleForgotPassword() {
        val email = binding.etEmail.text.toString().trim()
        
        if (email.isEmpty()) {
            binding.tilEmail.error = "Please enter your email"
            return
        }

        showLoading(true)
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to send reset email",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun toggleAuthMode() {
        isLoginMode = !isLoginMode
        
        // Update UI elements
        binding.apply {
            // Update title and button text
            tvAuthTitle.text = getString(if (isLoginMode) R.string.text_login else R.string.text_signup)
            btnPrimaryAction.text = getString(if (isLoginMode) R.string.btn_login else R.string.btn_signup)
            
            // Toggle visibility of signup-specific fields
            tilName.visibility = if (isLoginMode) View.GONE else View.VISIBLE
            tilConfirmPassword.visibility = if (isLoginMode) View.GONE else View.VISIBLE
            
            // Toggle forgot password button visibility
            btnForgotPassword.visibility = if (isLoginMode) View.VISIBLE else View.GONE
            
            // Update toggle prompt and button
            tvAuthTogglePrompt.text = getString(if (isLoginMode) R.string.text_no_account else R.string.text_have_account)
            btnAuthToggle.text = getString(if (isLoginMode) R.string.text_signup else R.string.text_login)
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnPrimaryAction.isEnabled = !show
        binding.btnAuthToggle.isEnabled = !show
        binding.btnForgotPassword.isEnabled = !show
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
