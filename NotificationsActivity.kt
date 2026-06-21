package com.example.segotlo

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotificationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notifications)
        
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val fullName = prefs.getString("fullName", "Thabo")
        val firstName = fullName?.split(" ")?.get(0) ?: "User"

        val rvNotifications = findViewById<RecyclerView>(R.id.rv_notifications)
        rvNotifications.layoutManager = LinearLayoutManager(this)

        val notifications = listOf(
            NotificationItem("Game Tomorrow!", "Hello $firstName, you have a match against Madikwe Stars tomorrow. Please confirm if you are available."),
            NotificationItem("Training Update", "Morning fitness training is moved to Field B."),
            NotificationItem("Profile Approved", "Welcome to the academy! Your registration has been approved.")
        )

        rvNotifications.adapter = NotificationAdapter(notifications)
    }
}