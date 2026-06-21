package com.example.segotlo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var layoutAuthButtons: LinearLayout
    private lateinit var indicators: List<View>

    // Firebase Auth
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.content_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // =========================
        // FIREBASE INITIALISATION
        // =========================
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        auth.createUserWithEmailAndPassword(
            "test@segotlo.com",
            "Password123"
        )
            .addOnSuccessListener { result ->

                val userId = result.user?.uid

                val userMap = hashMapOf(
                    "name" to "Lesedi",
                    "email" to "test@segotlo.com",
                    "role" to "Admin",
                    "uid" to userId
                )

                if (userId != null) {
                    db.collection("users")
                        .document(userId)
                        .set(userMap)
                        .addOnSuccessListener {
                            Log.d("FIREBASE_AUTH", "User saved to Firestore")
                        }
                        .addOnFailureListener {
                            Log.e("FIREBASE_AUTH", it.message.toString())
                        }
                }
            }
            .addOnFailureListener {
                Log.e("FIREBASE_AUTH", it.message.toString())
            }
        // =========================
        // YOUR ONBOARDING CODE
        // =========================

        viewPager = findViewById(R.id.viewPager)
        layoutAuthButtons = findViewById(R.id.layout_auth_buttons)

        indicators = listOf(
            findViewById(R.id.indicator1),
            findViewById(R.id.indicator2)
        )

        val slideLayouts = listOf(
            R.layout.item_onboarding,
            R.layout.onboarding_auth
        )

        val adapter = OnboardingAdapter(slideLayouts)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
                if (position == slideLayouts.size - 1) {
                    layoutAuthButtons.visibility = View.VISIBLE
                } else {
                    layoutAuthButtons.visibility = View.GONE
                }
            }
        })

        findViewById<Button>(R.id.btn_login).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<Button>(R.id.btn_register).setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
    }

    private fun updateIndicators(position: Int) {
        indicators.forEachIndexed { index, view ->
            if (index == position) {
                view.setBackgroundResource(R.drawable.indicator_active)
            } else {
                view.setBackgroundResource(R.drawable.indicator_inactive)
            }
        }
    }
}