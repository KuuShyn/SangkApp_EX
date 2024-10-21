package com.thesis.sangkapp_ex

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AnonymousActivity : AppCompatActivity() {

    // Firebase Auth instance
    private lateinit var auth: FirebaseAuth

    // Firestore instance
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_anonymous) // Ensure this matches your layout XML file name

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // UI Elements
        val welcomeText = findViewById<TextView>(R.id.welcomeText)  // Link the welcomeText from the XML layout
        val signInButton = findViewById<Button>(R.id.signInButton)  // Link the sign-in button from the XML layout

        // Apply window insets to handle full-screen layout adjustments
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.anonymousLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check if the user is already signed in
        if (auth.currentUser != null) {
            // User is already signed in, show their UID in the welcome text
            "Welcome back, user: ${auth.currentUser?.uid}".also { welcomeText.text = it }
            // Immediately check if the user has a profile
            checkUserProfile(auth.currentUser?.uid)
        } else {
            // If not signed in, display the default welcome message
            "Welcome! Please sign in anonymously.".also { welcomeText.text = it }
        }

        // Set the button click listener to initiate the sign-in process
        signInButton.setOnClickListener {
            // If user is not signed in, trigger anonymous sign-in
            signInAnonymously()
        }
    }

    // Function to handle anonymous sign-in
    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in successful
                    val user = auth.currentUser
                    val userId = user?.uid

                    // Save user UID to SharedPreferences
                    val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("USER_ID", userId)
                    editor.apply()

                    Toast.makeText(this, "Signed in as user: $userId", Toast.LENGTH_SHORT).show()

                    // Check if the user has a profile
                    checkUserProfile(userId)
                } else {
                    // If sign-in fails, display a message to the user
                    Toast.makeText(this, "Sign in failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to check if the user has a profile in Firestore
    private fun checkUserProfile(userId: String?) {
        if (userId != null) {
            // Access Firestore to check if the profile exists
            db.collection("users").document(userId)
                .collection("profile").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Profile exists, navigate to ProfileDetailsFragment
                        navigateToProfileDetails()
                    } else {
                        // Profile does not exist, navigate to ProfileInputFragment
                        navigateToProfileInput()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error checking profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Navigate to ProfileDetailsFragment
    private fun navigateToProfileDetails() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("fragment", "profile_details")
        startActivity(intent)
        finish()
    }

    // Navigate to ProfileInputFragment
    private fun navigateToProfileInput() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("fragment", "profile_input")
        startActivity(intent)
        finish()
    }
}
