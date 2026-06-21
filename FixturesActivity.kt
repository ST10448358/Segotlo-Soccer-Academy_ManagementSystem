package com.example.segotlo

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FixturesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_fixtures)
        
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        val rvUpcoming = findViewById<RecyclerView>(R.id.rv_fixtures_upcoming)
        val rvResults = findViewById<RecyclerView>(R.id.rv_fixtures_results)
        
        rvUpcoming.layoutManager = LinearLayoutManager(this)
        rvResults.layoutManager = LinearLayoutManager(this)

        val upcoming = mutableListOf<FixtureItem>()
        
        // Load custom fixtures
        val academyPrefs = getSharedPreferences("AcademyData", MODE_PRIVATE)
        val savedFixtures = academyPrefs.getString("fixtures", "")
        if (!savedFixtures.isNullOrEmpty()) {
            savedFixtures.split("##").forEach { entry ->
                val parts = entry.split("|")
                if (parts.size == 3) {
                    upcoming.add(FixtureItem(parts[0], parts[1], parts[2]))
                }
            }
        }

        upcoming.addAll(listOf(
            FixtureItem("Segotlo U15 vs Madikwe Stars", "15 Jun 2026 • 10:00 • Carletonville Stadium", "UPCOMING"),
            FixtureItem("Segotlo U17 vs Tamposstad FC", "15 Jun 2026 • 13:00 • Carletonville Stadium", "UPCOMING")
        ))

        val results = listOf(
            FixtureItem("Segotlo U15 vs Pella Warriors", "28 May 2026 • Academy Grounds", "FT", "3 - 1", true),
            FixtureItem("Segotlo All-Stars vs North West Select", "10 May 2026 • Tamposstad Grounds", "FT", "2 - 2", true)
        )

        rvUpcoming.adapter = FixtureAdapter(upcoming)
        rvResults.adapter = FixtureAdapter(results)
    }
}