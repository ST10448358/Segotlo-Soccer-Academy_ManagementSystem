package com.example.segotlo

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AnnouncementsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_announcements)
        
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        val rvAnnouncements = findViewById<RecyclerView>(R.id.rv_announcements)
        rvAnnouncements.layoutManager = LinearLayoutManager(this)

        val announcements = mutableListOf<AnnouncementItem>()
        
        // Load custom announcements from staff
        val academyPrefs = getSharedPreferences("AcademyData", MODE_PRIVATE)
        val savedAnnouncements = academyPrefs.getString("announcements", "")
        if (!savedAnnouncements.isNullOrEmpty()) {
            savedAnnouncements.split("##").forEach { entry ->
                val parts = entry.split("|")
                if (parts.size == 3) {
                    announcements.add(AnnouncementItem(parts[0], parts[1], parts[2]))
                }
            }
        }

        announcements.addAll(listOf(
            AnnouncementItem("Segotlo Cup 2026", "27 May 2026", "Registration for the annual Segotlo Cup is now open! All age groups are invited to participate."),
            AnnouncementItem("Holiday Schedule", "24 May 2026", "Please note the updated training schedule for the upcoming winter holidays."),
            AnnouncementItem("Match Results: U15", "20 May 2026", "Congratulations to our U15 team for their 3-1 victory against Pella Warriors!")
        ))

        rvAnnouncements.adapter = AnnouncementAdapter(announcements)
    }
}