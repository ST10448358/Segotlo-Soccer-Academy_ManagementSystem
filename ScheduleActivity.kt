package com.example.segotlo

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScheduleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_schedule)
        
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        val rvSchedule = findViewById<RecyclerView>(R.id.rv_schedule)
        rvSchedule.layoutManager = LinearLayoutManager(this)

        val schedule = listOf(
            ScheduleItem("Tue, 02 Jun 2026", "Morning Fitness Training", "07:00 - 08:30 • Academy Grounds"),
            ScheduleItem("Thu, 04 Jun 2026", "Tactical Drills Session", "15:00 - 17:00 • Main Pitch"),
            ScheduleItem("Sat, 07 Jun 2026", "Friendly Match Prep", "09:00 - 11:00 • Training Ground B"),
            ScheduleItem("Wed, 10 Jun 2026", "Homework Assistance", "14:00 - 16:00 • Community Hall")
        )

        rvSchedule.adapter = ScheduleAdapter(schedule)
    }
}