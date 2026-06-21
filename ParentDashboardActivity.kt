package com.example.segotlo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ParentDashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var contentFrame: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_parent_dashboard)

        drawerLayout = findViewById(R.id.parent_drawer_layout)
        contentFrame = findViewById(R.id.parent_content_frame)
        val btnSideMenu = findViewById<ImageButton>(R.id.btn_parent_side_menu)
        val navView = findViewById<NavigationView>(R.id.parent_nav_view)
        val notificationDot = findViewById<View>(R.id.parent_notification_dot)

        btnSideMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.parent_nav_dashboard -> loadHome()
                R.id.parent_nav_notifications -> loadNotifications()
                R.id.parent_nav_schedule -> {
                    // Clear new training flag
                    val academyPrefs = getSharedPreferences("AcademyData", MODE_PRIVATE)
                    academyPrefs.edit { putBoolean("new_training_available", false) }
                    notificationDot.visibility = View.GONE
                    loadSchedule()
                }
                R.id.parent_nav_fixtures -> loadFixtures()
                R.id.parent_nav_profile -> loadProfile()
                R.id.parent_nav_announcements -> loadAnnouncements()
                R.id.parent_nav_messages -> loadMessages()
                R.id.parent_nav_logout -> logout()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Check for new training notification
        val academyPrefs = getSharedPreferences("AcademyData", MODE_PRIVATE)
        if (academyPrefs.getBoolean("new_training_available", false)) {
            notificationDot.visibility = View.VISIBLE
        }

        loadHome()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun loadHome() {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_parent_home, contentFrame, false)
        contentFrame.removeAllViews()
        contentFrame.addView(view)

        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val fullName = prefs.getString("fullName", "Parent")
        val username = prefs.getString("username", "Parent")
        val firstName = fullName?.split(" ")?.get(0)?.uppercase() ?: username?.uppercase() ?: "PARENT"
        
        view.findViewById<TextView>(R.id.tv_parent_welcome_greeting).text = "WELCOME, $firstName"

        // Set up click listeners for stats cards
        view.findViewById<View>(R.id.card_parent_sessions).setOnClickListener {
            loadSchedule()
        }
        view.findViewById<View>(R.id.card_parent_fixtures).setOnClickListener {
            loadFixtures()
        }
        view.findViewById<View>(R.id.card_parent_announcements).setOnClickListener {
            loadAnnouncements()
        }

        // Set up click listeners for latest announcement and upcoming training
        view.findViewById<View>(R.id.card_parent_latest_announcement).setOnClickListener {
            loadAnnouncements()
        }
        view.findViewById<View>(R.id.card_parent_upcoming_training).setOnClickListener {
            loadSchedule()
        }
    }

    private fun loadNotifications() {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_parent_alerts, contentFrame, false)
        contentFrame.removeAllViews()
        contentFrame.addView(view)

        // Make notification items clickable
        val items = listOf(
            view.findViewById<LinearLayout>(R.id.notification_item_1),
            view.findViewById<LinearLayout>(R.id.notification_item_2),
            view.findViewById<LinearLayout>(R.id.notification_item_3),
            view.findViewById<LinearLayout>(R.id.notification_item_4)
        )

        items.forEachIndexed { index, item ->
            item?.setOnClickListener {
                showNotificationDialog(index)
            }
        }
    }

    private fun showNotificationDialog(index: Int) {
        val titles = listOf(
            "Registration Approved",
            "Training Schedule Update",
            "Segotlo Cup Registration",
            "Career Guidance Exhibition"
        )
        val details = listOf(
            "Your registration has been approved. Welcome to Segotlo Soccer Academy! You now have full access to the portal features.",
            "A new training schedule has been published for June 2026. Please check the Training Schedule section for your specific age group.",
            "Friendly reminder that registration for the Segotlo Cup 2026 closes on 01 June. Please confirm your child's participation.",
            "We are pleased to confirm the Career Guidance Exhibition scheduled for 20 June 2026. All parents are encouraged to attend."
        )

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_coach, null)
        val dialog = android.app.AlertDialog.Builder(this).setView(dialogView).create()

        dialogView.findViewById<TextView>(R.id.tv_dialog_title)?.text = titles[index]
        val etBody = dialogView.findViewById<android.widget.EditText>(R.id.et_coach_name)
        etBody.setText(details[index])
        etBody.isEnabled = false
        etBody.minLines = 5
        etBody.gravity = Gravity.TOP

        dialogView.findViewById<View>(R.id.et_coach_username).visibility = View.GONE
        dialogView.findViewById<View>(R.id.et_coach_password).visibility = View.GONE
        dialogView.findViewById<View>(R.id.et_child_name).visibility = View.GONE
        
        val btnClose = dialogView.findViewById<android.widget.Button>(R.id.btn_save_coach)
        btnClose.text = "CLOSE"
        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun loadMessages() {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_parent_send_message, contentFrame, false)
        contentFrame.removeAllViews()
        contentFrame.addView(view)

        val spinner = view.findViewById<android.widget.Spinner>(R.id.spinner_message_recipient)
        val recipients = arrayOf("Academy Admin", "Head Coach", "Team Coach")
        spinner.adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, recipients).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val etContent = view.findViewById<android.widget.EditText>(R.id.et_parent_message_content)
        val btnSend = view.findViewById<android.widget.Button>(R.id.btn_parent_send_message)

        btnSend.setOnClickListener {
            val content = etContent.text.toString().trim()
            val recipient = spinner.selectedItem.toString()

            if (content.isNotEmpty()) {
                val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                val senderName = prefs.getString("fullName", "A Parent")
                val senderEmail = prefs.getString("email", "parent@example.com")
                
                // Save message for Admin/Coach to see
                val adminPrefs = getSharedPreferences("AdminPrefs", MODE_PRIVATE)
                val existing = adminPrefs.getString("saved_messages", "") ?: ""
                val newMessage = "$senderName||$senderEmail||$content (To: $recipient)"
                val updated = if (existing.isEmpty()) newMessage else "$existing###$newMessage"
                
                adminPrefs.edit { putString("saved_messages", updated) }

                Toast.makeText(this, "Message sent to $recipient", Toast.LENGTH_LONG).show()
                etContent.setText("")
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle Inbox
        val rv = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_parent_inbox)
        rv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        
        val parentPrefs = getSharedPreferences("ParentPrefs", MODE_PRIVATE)
        val saved = parentPrefs.getString("parent_messages", "") ?: ""
        val items = mutableListOf<MessageItem>()
        if (saved.isNotEmpty()) {
            saved.split("###").forEach {
                val p = it.split("||")
                if (p.size >= 3) items.add(MessageItem(p[0], p[1], p[2]))
            }
        }
        
        if (items.isEmpty()) {
            items.add(MessageItem("Academy Admin", "admin@segotlo.com", "Welcome! Let us know if you need help."))
        }
        
        rv.adapter = MessageAdapter(items) { item ->
            // Parent can also reply or view full
            Toast.makeText(this, "Message from ${item.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSchedule() {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_parent_schedule, contentFrame, false)
        contentFrame.removeAllViews()
        contentFrame.addView(view)

        val container = view.findViewById<LinearLayout>(R.id.layout_parent_schedule_container)
        val academyPrefs = getSharedPreferences("AcademyData", MODE_PRIVATE)
        val savedAnnouncements = academyPrefs.getString("announcements", "") ?: ""
        
        val schedules = mutableListOf<Triple<String, String, String>>() // Title, Date, Desc
        
        if (savedAnnouncements.isNotEmpty()) {
            savedAnnouncements.split("##").forEach { entry ->
                if (entry.contains("New Training Schedule")) {
                    val parts = entry.split("|")
                    if (parts.size == 3) {
                        schedules.add(Triple(parts[2], parts[1], parts[0]))
                    }
                }
            }
        }

        if (schedules.isEmpty()) {
            // Mock data if none saved
            schedules.add(Triple("Morning Fitness Training", "02/06/2026", "U15 & U17"))
            schedules.add(Triple("Tactical Drills Session", "04/06/2026", "ALL GROUPS"))
        }

        schedules.forEach { (title, dateStr, group) ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_training_schedule, container, false)
            
            // Parse date to get Day and Date number
            try {
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val date = sdf.parse(dateStr)
                if (date != null) {
                    itemView.findViewById<TextView>(R.id.tv_item_day).text = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault()).format(date).uppercase()
                    itemView.findViewById<TextView>(R.id.tv_item_date_num).text = java.text.SimpleDateFormat("dd", java.util.Locale.getDefault()).format(date)
                }
            } catch (e: Exception) {
                itemView.findViewById<TextView>(R.id.tv_item_day).text = "DAY"
                itemView.findViewById<TextView>(R.id.tv_item_date_num).text = "??"
            }

            itemView.findViewById<TextView>(R.id.tv_item_title).text = title
            itemView.findViewById<TextView>(R.id.tv_item_details).text = "$dateStr • Academy Grounds"
            itemView.findViewById<TextView>(R.id.tv_item_group).text = group.replace("New Training Schedule", "").trim()
            
            container.addView(itemView)
            
            // Add separator
            val line = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                setBackgroundColor(Color.parseColor("#EEEEEE"))
                val margin = (20 * resources.displayMetrics.density).toInt()
                (layoutParams as LinearLayout.LayoutParams).setMargins(0, margin, 0, margin)
            }
            container.addView(line)
        }
    }

    private fun loadFixtures() {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_parent_fixtures, contentFrame, false)
        contentFrame.removeAllViews()
        contentFrame.addView(view)

        val upcomingList = view.findViewById<LinearLayout>(R.id.layout_parent_upcoming_list)
        val resultsList = view.findViewById<LinearLayout>(R.id.layout_parent_results_list)

        val academyPrefs = getSharedPreferences("AcademyData", MODE_PRIVATE)
        val savedFixtures = academyPrefs.getString("fixtures", "") ?: ""
        
        val fixtures = mutableListOf<FixtureItem>()
        if (savedFixtures.isNotEmpty()) {
            savedFixtures.split("##").forEach { entry ->
                val parts = entry.split("|")
                if (parts.size >= 3) {
                    fixtures.add(FixtureItem(parts[0], parts[1], parts[2]))
                }
            }
        }

        // Add Mock Upcoming if needed
        if (fixtures.none { it.status == "UPCOMING" }) {
            fixtures.add(FixtureItem("Segotlo U15 vs Madikwe Stars", "15 Jun 2026 • 10:00 • Stadium", "UPCOMING"))
            fixtures.add(FixtureItem("Segotlo U17 vs Tamposstad FC", "15 Jun 2026 • 13:00 • Stadium", "UPCOMING"))
        }

        // Add Mock Results
        val results = listOf(
            FixtureItem("Segotlo U15 vs Pella Warriors", "28 May 2026 • Academy Grounds", "FT", "3 - 1", true),
            FixtureItem("Segotlo All-Stars vs North West Select", "10 May 2026 • Tamposstad Grounds", "FT", "2 - 2", true)
        )

        fun addRow(container: LinearLayout, item: FixtureItem, isLast: Boolean) {
            val row = LayoutInflater.from(this).inflate(R.layout.item_fixture_row, container, false)
            row.findViewById<TextView>(R.id.tv_row_title).text = item.title
            row.findViewById<TextView>(R.id.tv_row_details).text = item.details
            
            val statusTv = row.findViewById<TextView>(R.id.tv_row_status)
            statusTv.text = item.status
            
            if (item.isResult) {
                row.findViewById<TextView>(R.id.tv_row_score).apply {
                    visibility = View.VISIBLE
                    text = item.score
                }
                statusTv.setTextColor(Color.parseColor("#00AA00"))
                statusTv.setBackgroundColor(Color.parseColor("#1100AA00"))
            } else {
                statusTv.setTextColor(Color.parseColor("#FFEA00"))
                statusTv.setBackgroundColor(Color.parseColor("#11000000"))
            }

            container.addView(row)
            if (!isLast) {
                val line = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                    setBackgroundColor(Color.parseColor("#EEEEEE"))
                    val margin = (16 * resources.displayMetrics.density).toInt()
                    (layoutParams as LinearLayout.LayoutParams).setMargins(0, margin, 0, margin)
                }
                container.addView(line)
            }
        }

        val upcomingItems = fixtures.filter { it.status == "UPCOMING" }
        upcomingItems.forEachIndexed { i, item -> addRow(upcomingList, item, i == upcomingItems.size - 1) }

        results.forEachIndexed { i, item -> addRow(resultsList, item, i == results.size - 1) }
    }

    private fun loadProfile() {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_parent_profile, contentFrame, false)
        contentFrame.removeAllViews()
        contentFrame.addView(view)

        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val fullName = prefs.getString("fullName", "MAY")
        val username = prefs.getString("username", "may")
        val email = prefs.getString("email", "lesedi@gmail.com")
        
        view.findViewById<TextView>(R.id.tv_parent_profile_name).text = fullName?.uppercase()
        view.findViewById<TextView>(R.id.tv_parent_linked_child).text = "Parent of Karabo"
        
        view.findViewById<TextView>(R.id.tv_linked_full_name).text = fullName?.lowercase()
        view.findViewById<TextView>(R.id.tv_linked_username).text = username?.lowercase()
        view.findViewById<TextView>(R.id.tv_linked_age).text = "15"
        view.findViewById<TextView>(R.id.tv_linked_position).text = "goalkeeper"
        view.findViewById<TextView>(R.id.tv_linked_email).text = email
        
        view.findViewById<TextView>(R.id.tv_linked_status).apply {
            text = "APPROVED"
            setTextColor(Color.parseColor("#00AA00"))
        }

        view.findViewById<View>(R.id.btn_edit_parent_profile).setOnClickListener {
            showEditProfileDialog()
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_coach, null)
        val dialog = android.app.AlertDialog.Builder(this).setView(dialogView).create()
        
        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
        val etName = dialogView.findViewById<android.widget.EditText>(R.id.et_coach_name)
        val etUser = dialogView.findViewById<android.widget.EditText>(R.id.et_coach_username)
        val etPass = dialogView.findViewById<android.widget.EditText>(R.id.et_coach_password)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btn_save_coach)

        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        
        tvTitle?.text = "Edit Profile"
        etName.setText(prefs.getString("fullName", "MAY"))
        etUser.setText(prefs.getString("username", "may"))
        etPass.setText(prefs.getString("password", ""))
        btnSave.text = "UPDATE"
        
        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newUser = etUser.text.toString().trim()
            val newPass = etPass.text.toString().trim()

            if (newName.isNotEmpty() && newUser.isNotEmpty()) {
                prefs.edit {
                    putString("fullName", newName)
                    putString("username", newUser)
                    putString("password", newPass)
                }
                Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                loadProfile()
            }
        }
        dialog.show()
    }

    private fun loadAnnouncements() {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_parent_announcements, contentFrame, false)
        contentFrame.removeAllViews()
        contentFrame.addView(view)

        val container = view.findViewById<LinearLayout>(R.id.layout_parent_announcements_container)
        val academyPrefs = getSharedPreferences("AcademyData", MODE_PRIVATE)
        val savedAnnouncements = academyPrefs.getString("announcements", "") ?: ""
        
        val announcements = mutableListOf<Triple<String, String, String>>() // Title, Date, Desc
        
        if (savedAnnouncements.isNotEmpty()) {
            savedAnnouncements.split("##").forEach { entry ->
                val parts = entry.split("|")
                if (parts.size == 3) {
                    announcements.add(Triple(parts[0], parts[1], parts[2]))
                }
            }
        }

        // Add Mock Data if needed
        if (announcements.isEmpty()) {
            announcements.add(Triple("Segotlo Cup 2026 - Registration Open", "27 May 2026", "All players must register for the Segotlo Cup by 01 June 2026. Contact the admin office to confirm your participation."))
            announcements.add(Triple("School Holiday Schedule Update", "24 May 2026", "Training sessions during the June school holidays will move to morning slots (07:00 - 09:00). Full schedule available on the portal."))
            announcements.add(Triple("Career Guidance Exhibition - 20 June", "20 May 2026", "We are hosting a career guidance exhibition on 20 June. All players, parents and community members are welcome."))
        }

        announcements.forEachIndexed { index, (title, date, desc) ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_announcement_row, container, false)
            itemView.findViewById<TextView>(R.id.tv_ann_title).text = title
            itemView.findViewById<TextView>(R.id.tv_ann_desc).text = desc
            itemView.findViewById<TextView>(R.id.tv_ann_date).text = date
            
            container.addView(itemView)
            
            if (index < announcements.size - 1) {
                val line = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                    setBackgroundColor(Color.parseColor("#EEEEEE"))
                    val margin = (20 * resources.displayMetrics.density).toInt()
                    (layoutParams as LinearLayout.LayoutParams).setMargins(0, margin, 0, margin)
                }
                container.addView(line)
            }
        }
    }

    private fun logout() {
        val intent = Intent(this, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}