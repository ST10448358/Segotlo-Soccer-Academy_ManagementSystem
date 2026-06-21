package com.example.segotlo

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var contentFrame: FrameLayout
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_dashboard)

        db = FirebaseFirestore.getInstance()
        contentFrame = findViewById(R.id.admin_content_frame)
        val navView = findViewById<NavigationView>(R.id.admin_nav_view)
        val btnMenu = findViewById<ImageButton>(R.id.btn_admin_menu)
        val notificationDot = findViewById<View>(R.id.view_notification_dot)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.admin_nav_dashboard -> loadHome()
                R.id.admin_nav_registrations -> {
                    menuItem.actionView = null
                    notificationDot.visibility = View.GONE
                    loadRegistrations()
                }
                R.id.admin_nav_manage_users -> loadManageUsers()
                R.id.admin_nav_teams_players -> loadTeamsPlayers()
                R.id.admin_nav_create_schedule -> loadCreateSchedule()
                R.id.admin_nav_match_fixtures -> loadMatchFixtures()
                R.id.admin_nav_announcements -> loadAnnouncements()
                R.id.admin_nav_send_announcement -> loadSendAnnouncement()
                R.id.admin_nav_messages -> {
                    menuItem.actionView = null
                    notificationDot.visibility = View.GONE
                    loadMessages()
                }
                R.id.admin_nav_logout -> logout()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Show badge count for messages
        val msgMenuItem = navView.menu.findItem(R.id.admin_nav_messages)
        msgMenuItem.actionView = createBadge("3")

        // Show badge count for registrations
        val regPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val hasReg = regPrefs.getString("fullName", null) != null && !regPrefs.getBoolean("isApproved", false) && !regPrefs.getBoolean("isRejected", false)
        if (hasReg) {
            val regMenuItem = navView.menu.findItem(R.id.admin_nav_registrations)
            regMenuItem.actionView = createBadge("1")
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

    private fun createBadge(count: String): TextView {
        return TextView(this).apply {
            text = count
            setTextColor(Color.BLACK)
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            val size = (20 * resources.displayMetrics.density).toInt()
            layoutParams = ViewGroup.LayoutParams(size, size)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#FFEA00")) // Yellow
            }
        }
    }

    private fun loadManageUsers() {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_admin_manage_users, contentFrame, false)
        contentFrame.removeAllViews()
        contentFrame.addView(view)

        val rv = view.findViewById<RecyclerView>(R.id.rv_manage_users)
        rv.layoutManager = LinearLayoutManager(this)

        val tabLayout = view.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tab_manage_users)
        
        fun refreshList() {
            when (tabLayout.selectedTabPosition) {
                0 -> loadApprovedPlayersList(rv) // Players
                1 -> loadCoaches(rv)         // Coaches
                2 -> loadParents(rv)         // Parents
            }
        }

        tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                refreshList()
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })

        val btnAdd = view.findViewById<Button>(R.id.btn_add_new_user)
        btnAdd.setOnClickListener {
            when (tabLayout.selectedTabPosition) {
                1 -> showAddCoachDialog { refreshList() }
                0 -> showAddPlayerDialog { refreshList() }
                2 -> showAddParentDialog { refreshList() }
            }
        }

        refreshList()
    }

    private fun showAddParentDialog(onAdded: () -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_coach, null)
        val dialog = android.app.AlertDialog.Builder(this).setView(dialogView).create()
        
        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
        val etChildName = dialogView.findViewById<EditText>(R.id.et_child_name)
        val btnSave = dialogView.findViewById<Button>(R.id.btn_save_coach)

        tvTitle?.text = "Add New Parent"
        etChildName?.visibility = View.VISIBLE
        btnSave?.text = "SAVE PARENT"
        
        btnSave.setOnClickListener {
            val name = dialogView.findViewById<EditText>(R.id.et_coach_name).text.toString()
            val user = dialogView.findViewById<EditText>(R.id.et_coach_username).text.toString()
            val pass = dialogView.findViewById<EditText>(R.id.et_coach_password).text.toString()
            val child = etChildName?.text.toString()

            if (name.isNotEmpty() && user.isNotEmpty() && pass.isNotEmpty() && child.isNotEmpty()) {
                val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                val parents = prefs.getString("manual_parent_list", "") ?: ""
                // Format: username:password:name:childName
                val newParent = "$user:$pass:$name:$child"
                val updated = if (parents.isEmpty()) newParent else "$parents##$newParent"
                prefs.edit { putString("manual_parent_list", updated) }
                
                Toast.makeText(this, "Parent $name added!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                onAdded()
            } else {
                Toast.makeText(this, "Please fill in all fields including child's name", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun showAddPlayerDialog(onAdded: () -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_coach, null)
        val dialog = android.app.AlertDialog.Builder(this).setView(dialogView).create()
        
        dialogView.findViewById<TextView>(R.id.tv_dialog_title)?.text = "Add New Player"
        
        dialogView.findViewById<Button>(R.id.btn_save_coach).setOnClickListener {
            val name = dialogView.findViewById<EditText>(R.id.et_coach_name).text.toString()
            val user = dialogView.findViewById<EditText>(R.id.et_coach_username).text.toString()
            val pass = dialogView.findViewById<EditText>(R.id.et_coach_password).text.toString()

            if (name.isNotEmpty() && user.isNotEmpty() && pass.isNotEmpty()) {
                val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                val players = prefs.getString("manual_player_list", "") ?: ""
                val updated = if (players.isEmpty()) "$user:$pass:$name" else "$players##$user:$pass:$name"
                prefs.edit { putString("manual_player_list", updated) }
                
                Toast.makeText(this, "Player $name added!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                onAdded()
            }
        }
        dialog.show()
    }

    private fun showAddCoachDialog(onAdded: () -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_coach, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btn_save_coach).setOnClickListener {
            val name = dialogView.findViewById<EditText>(R.id.et_coach_name).text.toString()
            val user = dialogView.findViewById<EditText>(R.id.et_coach_username).text.toString()
            val pass = dialogView.findViewById<EditText>(R.id.et_coach_password).text.toString()

            if (user.isNotEmpty() && pass.isNotEmpty()) {
                val prefs = getSharedPreferences("StaffData", MODE_PRIVATE)
                val coaches = prefs.getString("coach_list", "") ?: ""
                val updated = if (coaches.isEmpty()) "$user:$pass:$name" else "$coaches##$user:$pass:$name"
                prefs.edit { putString("coach_list", updated) }
                
                Toast.makeText(this, "Coach $name added!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                onAdded()
            }
        }
        dialog.show()
    }

    private fun loadCoaches(rv: RecyclerView) {
        val prefs = getSharedPreferences("StaffData", MODE_PRIVATE)
        val coachList = prefs.getString("coach_list", "")
        val items = mutableListOf<NotificationItem>()
        
        if (!coachList.isNullOrEmpty()) {
            coachList.split("##").forEach {
                val parts = it.split(":")
                if (parts.size >= 3) {
                    items.add(NotificationItem(parts[2], "Username: ${parts[0]}"))
                }
            }
        }
        
        if (items.isEmpty()) {
            items.add(NotificationItem("Daniel Molokwane", "Head Coach"))
        }
        
        rv.adapter = PlayerProfileAdapter(items)
    }

    private fun loadParents(rv: RecyclerView) {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val manualParents = prefs.getString("manual_parent_list", "")
        val items = mutableListOf<NotificationItem>()
        
        if (!manualParents.isNullOrEmpty()) {
            manualParents.split("##").forEach {
                val parts = it.split(":")
                if (parts.size >= 4) {
                    items.add(NotificationItem(parts[2], "Parent of ${parts[3]}"))
                } else if (parts.size >= 3) {
                    items.add(NotificationItem(parts[2], "Parent Account"))
                }
            }
        }

        items.add(NotificationItem("Mary Molefe", "Parent of Thabo"))
        items.add(NotificationItem("John Dlamini", "Parent of Lerato"))
        
        rv.adapter = PlayerProfileAdapter(items)
    }

    private fun loadApprovedPlayersList(rv: RecyclerView) {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val manualPlayers = prefs.getString("manual_player_list", "")
        
        val players = mutableListOf<NotificationItem>()
        
        if (!manualPlayers.isNullOrEmpty()) {
            manualPlayers.split("##").forEach {
                val parts = it.split(":")
                if (parts.size >= 3) {
                    players.add(NotificationItem(parts[2], "U15 • Striker"))
                }
            }
        }

        val registeredName = prefs.getString("fullName", null)
        val isApproved = prefs.getBoolean("isApproved", false)
        if (isApproved && registeredName != null) {
            players.add(NotificationItem(registeredName, "U15 • Striker"))
        }
        
        players.add(NotificationItem("Karabo M.", "U13"))
        players.add(NotificationItem("Lesedi P.", "U11"))
        
        rv.adapter = PlayerProfileAdapter(players)
    }

    private fun loadHome() {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_admin_home, contentFrame, false)
        contentFrame.removeAllViews()
        contentFrame.addView(view)

        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = prefs.getString("username", "Admin")
        view.findViewById<TextView>(R.id.tv_admin_welcome_greeting)?.text = "Welcome, $username 👋"

        // Setup Card Navigation with yellow highlights on click
        val cardReg = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_stat_registrations)
        val cardAnn = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_stat_announcements)
        val cardSched = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_stat_schedule)
        val cardFix = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_stat_fixtures)
        val cardLatestAnn = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.card_latest_announcement)

        fun highlightAndRun(card: com.google.android.material.card.MaterialCardView, action: () -> Unit) {
            card.setOnClickListener {
                // Apply a yellow highlight effect temporarily
                card.strokeWidth = 4
                card.postDelayed({ 
                    card.strokeWidth = 0
                    action() 
                }, 150)
            }
        }

        highlightAndRun(cardReg) { loadRegistrations() }
        highlightAndRun(cardAnn) { loadAnnouncements() }
        highlightAndRun(cardSched) { loadCreateSchedule() }
        highlightAndRun(cardFix) { loadMatchFixtures() }
        highlightAndRun(cardLatestAnn) { loadAnnouncements() }

        // Dynamic Counts from Firestore
        db.collection("users").get().addOnSuccessListener { documents ->
            var approvedPlayerCount = 0
            for (doc in documents) {
                if (doc.getString("role") == "Player" && doc.getBoolean("isApproved") == true) {
                    approvedPlayerCount++
                }
            }
            // Base count + Firestore count
            val totalReg = 16 + approvedPlayerCount
            view.findViewById<TextView>(R.id.tv_stat_reg_count)?.text = totalReg.toString()
        }

        val academyPrefs = getSharedPreferences("AcademyData", MODE_PRIVATE)
        val savedAnn = academyPrefs.getString("announcements", "") ?: ""
        val annList = if (savedAnn.isEmpty()) emptyList<String>() else savedAnn.split("##")
        view.findViewById<TextView>(R.id.tv_stat_announcement_count)?.text = (3 + annList.size).toString()

        // Display Latest Announcement
        if (annList.isNotEmpty()) {
            val latest = annList[0].split("|")
            if (latest.size >= 3) {
                view.findViewById<TextView>(R.id.tv_home_latest_announcement_title)?.text = latest[0]
                view.findViewById<TextView>(R.id.tv_home_latest_announcement_desc)?.text = latest[2]
            }
        }
    }

    private fun loadMessages() {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_admin_messages, contentFrame, false)
        contentFrame.removeAllViews()
        contentFrame.addView(view)

        val rv = view.findViewById<RecyclerView>(R.id.rv_admin_messages)
        rv.layoutManager = LinearLayoutManager(this)

        val adminPrefs = getSharedPreferences("AdminPrefs", MODE_PRIVATE)
        
        // Load messages from SharedPreferences
        val savedMessages = adminPrefs.getString("saved_messages", "") ?: ""
        val messages = mutableListOf<MessageItem>()
        
        if (savedMessages.isNotEmpty()) {
            savedMessages.split("###").forEach {
                val parts = it.split("||")
                if (parts.size >= 3) {
                    messages.add(MessageItem(parts[0], parts[1], parts[2]))
                }
            }
        }

        // Add initial mock messages if list is empty
        if (messages.isEmpty()) {
            messages.addAll(listOf(
                MessageItem("leseso", "lesedi@gmail.com", "i want to join the club"),
                MessageItem("John Doe", "john@example.com", "When is the next trial?"),
                MessageItem("Sarah Smith", "sarah@gmail.com", "Interested in U13 registration.")
            ))
            // Save them so they persist
            val toSave = messages.joinToString("###") { "${it.name}||${it.email}||${it.message}" }
            adminPrefs.edit { putString("saved_messages", toSave) }
        }

        rv.adapter = MessageAdapter(messages) { item ->
            showFullMessageDialog(item)
        }
    }

    private fun showFullMessageDialog(item: MessageItem) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_coach, null)
        val dialog = android.app.AlertDialog.Builder(this).setView(dialogView).create()
        
        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
        val etEmail = dialogView.findViewById<EditText>(R.id.et_coach_name)
        val etMessage = dialogView.findViewById<EditText>(R.id.et_coach_username)
        val etReply = dialogView.findViewById<EditText>(R.id.et_coach_password)
        val btnAction = dialogView.findViewById<Button>(R.id.btn_save_coach)

        tvTitle?.text = "Message from ${item.name}"
        
        etEmail.setText(item.email)
        etEmail.isEnabled = false
        
        etMessage.setText(item.message)
        etMessage.isEnabled = false
        etMessage.minLines = 3
        etMessage.gravity = Gravity.TOP

        etReply.visibility = View.VISIBLE
        etReply.hint = "Type your reply here..."
        etReply.inputType = android.text.InputType.TYPE_CLASS_TEXT
        etReply.setText("")
        
        btnAction.text = "REPLY"
        
        var isReplyMode = true

        btnAction.setOnClickListener {
            if (isReplyMode) {
                val replyText = etReply.text.toString().trim()
                if (replyText.isNotEmpty()) {
                    Toast.makeText(this, "Message sent to ${item.email}", Toast.LENGTH_LONG).show()
                    
                    // Switch UI to "Close" choice
                    tvTitle?.text = "Reply Sent"
                    etReply.visibility = View.GONE
                    btnAction.text = "CLOSE"
                    isReplyMode = false
                } else {
                    Toast.makeText(this, "Please enter a reply", Toast.LENGTH_SHORT).show()
                }
            } else {
                dialog.dismiss()
            }
        }
        
        dialog.show()
    }

    private fun loadRegistrations() {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_admin_registrations, contentFrame, false)
        contentFrame.removeAllViews()
        contentFrame.addView(view)

        val pendingContainer = view.findViewById<LinearLayout>(R.id.layout_pending_list)
        val pendingParentContainer = view.findViewById<LinearLayout>(R.id.layout_pending_parents_list)
        val approvedContainer = view.findViewById<LinearLayout>(R.id.layout_approved_list)
        val rejectedContainer = view.findViewById<LinearLayout>(R.id.layout_rejected_list)
        
        val tvPendingHeader = view.findViewById<TextView>(R.id.tv_pending_header)
        val tvPendingParentsHeader = view.findViewById<TextView>(R.id.tv_pending_parents_header)
        val tvApprovedHeader = view.findViewById<TextView>(R.id.tv_approved_header)
        val tvRejectedHeader = view.findViewById<TextView>(R.id.tv_rejected_header)

        var pendingCount = 0
        var pendingParentCount = 0
        var approvedCount = 0
        var rejectedCount = 0

        // Fetch from Firestore
        db.collection("users").get().addOnSuccessListener { documents ->
            pendingContainer.removeAllViews()
            pendingParentContainer.removeAllViews()
            approvedContainer.removeAllViews()
            rejectedContainer.removeAllViews()

            for (doc in documents) {
                val name = doc.getString("name") ?: "Unknown"
                val email = doc.getString("email") ?: ""
                val role = doc.getString("role") ?: "Player"
                val isApproved = doc.getBoolean("isApproved") ?: false
                val isRejected = doc.getBoolean("isRejected") ?: false
                val uid = doc.id

                if (!isApproved && !isRejected) {
                    val itemView = LayoutInflater.from(this).inflate(R.layout.item_admin_pending, pendingContainer, false)
                    itemView.findViewById<TextView>(R.id.tv_pending_name).text = name
                    
                    if (role == "Player") {
                        pendingCount++
                        itemView.findViewById<TextView>(R.id.tv_pending_details).text = "Role: Player • Email: $email"
                        pendingContainer.addView(itemView)
                    } else {
                        pendingParentCount++
                        tvPendingParentsHeader.visibility = View.VISIBLE
                        itemView.findViewById<TextView>(R.id.tv_pending_details).text = "Role: Parent • Email: $email"
                        pendingParentContainer.addView(itemView)
                    }
                    
                    itemView.findViewById<Button>(R.id.btn_item_approve).setOnClickListener {
                        db.collection("users").document(uid).update("isApproved", true, "isRejected", false)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Approved: $name", Toast.LENGTH_SHORT).show()
                                loadRegistrations()
                            }
                    }
                    
                    itemView.findViewById<Button>(R.id.btn_item_reject).setOnClickListener {
                        db.collection("users").document(uid).update("isApproved", false, "isRejected", true)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Rejected: $name", Toast.LENGTH_SHORT).show()
                                loadRegistrations()
                            }
                    }
                } else if (isApproved) {
                    approvedCount++
                    addApprovedPlayerCard(approvedContainer, name, "Role: $role • $email")
                } else if (isRejected) {
                    rejectedCount++
                    addRejectedPlayerText(rejectedContainer, name, "Role: $role", "REJECTED")
                }
            }

            // Mock Approved Players (Keep them for UI fullness if desired)
            val mockApproved = listOf("Thabo Molefe", "Lerato Dlamini", "Sipho Nkosi")
            mockApproved.forEach { mName ->
                approvedCount++
                addApprovedPlayerCard(approvedContainer, mName, "Age 15 • Striker")
            }

            tvPendingHeader.text = "Pending Applications ($pendingCount)"
            tvPendingParentsHeader.text = "Pending Parents / Guardians ($pendingParentCount)"
            tvApprovedHeader.text = "Approved Players ($approvedCount)"
            tvRejectedHeader.text = "Rejected ($rejectedCount)"
        }
    }

    private fun addApprovedPlayerCard(container: LinearLayout, name: String, details: String) {
        val card = LayoutInflater.from(this).inflate(R.layout.item_player_profile_card, container, false)
        card.findViewById<TextView>(R.id.tv_profile_card_name).text = name
        card.findViewById<TextView>(R.id.tv_profile_card_details).text = details
        container.addView(card)
    }

    private fun addRejectedPlayerText(container: LinearLayout, name: String, details: String, reason: String) {
        val itemView = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 24, 0, 24)
            gravity = Gravity.CENTER_VERTICAL
        }

        val leftLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            orientation = LinearLayout.VERTICAL
        }

        val tvName = TextView(this).apply {
            text = name
            setTextColor(Color.WHITE)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
        }

        val tvDetails = TextView(this).apply {
            text = details
            setTextColor(Color.parseColor("#888888"))
            textSize = 12f
        }

        leftLayout.addView(tvName)
        leftLayout.addView(tvDetails)

        val tvReason = TextView(this).apply {
            text = reason.uppercase()
            setTextColor(Color.parseColor("#FF2D7A"))
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(12, 8, 12, 8)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1A000000")) // Subtle dark bg
                cornerRadius = 20f
                setStroke(1, Color.parseColor("#FF2D7A"))
            }
        }

        itemView.addView(leftLayout)
        itemView.addView(tvReason)
        container.addView(itemView)
    }

    private fun loadManagePlayers() {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_coach_player_profiles, contentFrame, false)
        view.findViewById<TextView>(R.id.tv_coach_players_title)?.text = "MANAGE PLAYERS"
        contentFrame.removeAllViews()
        contentFrame.addView(view)

        val rv = view.findViewById<RecyclerView>(R.id.rv_coach_all_players)
        rv.layoutManager = GridLayoutManager(this, 2)
        
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isApproved = prefs.getBoolean("isApproved", false)
        val registeredName = prefs.getString("fullName", "Thabo Molefe")
        val registeredPos = prefs.getString("position", "Striker")

        val players = mutableListOf<NotificationItem>()
        if (isApproved) {
            players.add(NotificationItem(registeredName!!, "Age 15 • $registeredPos"))
        }
        players.add(NotificationItem("Thabo Molefe", "Age 15 • Striker"))
        players.add(NotificationItem("Lerato Dlamini", "Age 14 • Midfielder"))
        players.add(NotificationItem("Sipho Nkosi", "Age 16 • Goalkeeper"))
        players.add(NotificationItem("Amanda Khumalo", "Age 13 • Defender"))
        
        rv.adapter = PlayerProfileAdapter(players)
    }

    private fun loadTeamsPlayers() {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_admin_teams_players, contentFrame, false)
        contentFrame.removeAllViews()
        contentFrame.addView(view)

        val rv = view.findViewById<RecyclerView>(R.id.rv_teams_players)
        rv.layoutManager = LinearLayoutManager(this)
        
        // Show current teams (e.g. from design: Segotlo U13, U11, U9)
        val teams = listOf(
            NotificationItem("Segotlo U13", "18 Players"),
            NotificationItem("Segotlo U11", "16 Players"),
            NotificationItem("Segotlo U9", "14 Players"),
            NotificationItem("Girls Team U15", "15 Players")
        )
        rv.adapter = PlayerProfileAdapter(teams)

        val btnAdd = view.findViewById<Button>(R.id.btn_add_new_team)
        btnAdd.setOnClickListener {
            showAddTeamDialog()
        }
    }

    private fun showAddTeamDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_team, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinner_add_team_age)
        val ages = arrayOf("U11", "U12", "U13", "U14", "U15", "U16", "U18")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ages).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btn_save_match).setOnClickListener {
            val opponent = dialogView.findViewById<EditText>(R.id.et_opponent_name).text.toString()
            val age = spinner.selectedItem.toString()
            val date = dialogView.findViewById<EditText>(R.id.et_match_date).text.toString()

            if (opponent.isNotEmpty() && date.isNotEmpty()) {
                val prefs = getSharedPreferences("AcademyData", MODE_PRIVATE)
                val fixtures = prefs.getString("fixtures", "") ?: ""
                val newMatch = "Segotlo $age vs $opponent|$date • 10:00 • Academy Grounds|UPCOMING"
                val updated = if (fixtures.isEmpty()) newMatch else "$newMatch##$fixtures"
                prefs.edit { putString("fixtures", updated) }
                
                Toast.makeText(this, "Match added!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                loadMatchFixtures()
            }
        }
        dialog.show()
    }

    private fun loadCreateSchedule() {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_coach_create_schedule, contentFrame, false)
        contentFrame.removeAllViews()
        contentFrame.addView(view)
        setupScheduleForm(view)
    }

    private fun setupScheduleForm(view: View) {
        val etDate = view.findViewById<EditText>(R.id.et_session_date)
        val spinnerDay = view.findViewById<Spinner>(R.id.spinner_session_day)
        val spinnerGroup = view.findViewById<Spinner>(R.id.spinner_session_group)
        val btnAdd = view.findViewById<Button>(R.id.btn_add_to_calendar)
        val etTitle = view.findViewById<EditText>(R.id.et_session_title)

        etDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, yearSelected, monthOfYear, dayOfMonth ->
                etDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", monthOfYear + 1, dayOfMonth, yearSelected))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        val days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        spinnerDay.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, days).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val groups = arrayOf("All Groups", "U13", "U15", "U17", "Goalkeepers", "All")
        spinnerGroup.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, groups).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        btnAdd.setOnClickListener {
            val title = etTitle.text.toString()
            val date = etDate.text.toString()
            val group = spinnerGroup.selectedItem.toString()

            if (title.isNotEmpty() && date.isNotEmpty()) {
                val academyPrefs = getSharedPreferences("AcademyData", MODE_PRIVATE)
                val currentAnnouncements = academyPrefs.getString("announcements", "") ?: ""
                val newEntry = "New Training Schedule|$date|A new training session '$title' has been scheduled for $group."
                val updatedAnnouncements = if (currentAnnouncements.isEmpty()) newEntry else "$newEntry##$currentAnnouncements"
                
                academyPrefs.edit { 
                    putString("announcements", updatedAnnouncements)
                    putBoolean("new_training_available", true)
                }

                Toast.makeText(this, "Schedule added successfully", Toast.LENGTH_SHORT).show()
                loadAnnouncements()
            } else {
                Toast.makeText(this, "Please fill in title and date", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMatchFixtures() {
        val view = LayoutInflater.from(this).inflate(R.layout.activity_fixtures, contentFrame, false)
        contentFrame.removeAllViews()
        contentFrame.addView(view)
        
        view.findViewById<View>(R.id.btn_back).visibility = View.GONE

        val rvUpcoming = view.findViewById<RecyclerView>(R.id.rv_fixtures_upcoming)
        val rvResults = view.findViewById<RecyclerView>(R.id.rv_fixtures_results)
        
        rvUpcoming.layoutManager = LinearLayoutManager(this)
        rvResults.layoutManager = LinearLayoutManager(this)

        val upcoming = mutableListOf<FixtureItem>()
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
            FixtureItem("Segotlo U14 vs coner", "17 June 2026 • 10:00 • Academy Grounds", "UPCOMING"),
            FixtureItem("Segotlo U11 vs chiefs", "16 June 2026 • 10:00 • Academy Grounds", "UPCOMING"),
            FixtureItem("Segotlo U13 vs Madikwe Stars", "24 May 2025 • 10:00 AM • Carletonville Stadium", "UPCOMING"),
            FixtureItem("Segotlo U17 vs Tamposstad FC", "15 Jun 2026 • 13:00 • Carletonville Stadium", "UPCOMING")
        ))

        val results = listOf(
            FixtureItem("Segotlo U15 vs Pella Warriors", "28 May 2026 • Academy Grounds", "FT", "3 - 1", true),
            FixtureItem("Segotlo All-Stars vs North West Select", "10 May 2026 • Tamposstad Grounds", "FT", "2 - 2", true)
        )

        rvUpcoming.adapter = FixtureAdapter(upcoming)
        rvResults.adapter = FixtureAdapter(results)
    }

    private fun loadAnnouncements() {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_coach_announcements_list, contentFrame, false)
        contentFrame.removeAllViews()
        contentFrame.addView(view)

        val rv = view.findViewById<RecyclerView>(R.id.rv_coach_all_announcements)
        rv.layoutManager = LinearLayoutManager(this)
        
        val announcements = mutableListOf<AnnouncementItem>()
        
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
            AnnouncementItem("Segotlo Cup 2026", "27 May 2026", "Registration open for all players."),
            AnnouncementItem("School Holiday Update", "24 May 2026", "Updated training slots for June."),
            AnnouncementItem("Career Guidance Exhibition", "20 May 2026", "Hosting a guidance exhibition on 20 June.")
        ))
        rv.adapter = AnnouncementAdapter(announcements)
    }

    private fun loadSendAnnouncement() {
        val view = LayoutInflater.from(this).inflate(R.layout.layout_admin_send_announcement, contentFrame, false)
        contentFrame.removeAllViews()
        contentFrame.addView(view)

        val etTitle = view.findViewById<EditText>(R.id.et_announcement_title)
        val etMessage = view.findViewById<EditText>(R.id.et_announcement_message)
        val spinnerAudience = view.findViewById<Spinner>(R.id.spinner_target_audience)
        val btnSend = view.findViewById<Button>(R.id.btn_send_announcement)

        val audiences = arrayOf("All Members", "Players Only", "Parents Only", "Coaches Only")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, audiences)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAudience.adapter = adapter

        btnSend.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val message = etMessage.text.toString().trim()
            val audience = spinnerAudience.selectedItem.toString()

            if (title.isNotEmpty() && message.isNotEmpty()) {
                val calendar = Calendar.getInstance()
                val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val currentDate = dateFormat.format(calendar.time)

                val academyPrefs = getSharedPreferences("AcademyData", MODE_PRIVATE)
                val currentAnnouncements = academyPrefs.getString("announcements", "") ?: ""
                
                // Format: Title|Date|Description
                val newEntry = "$title|$currentDate|$message ($audience)"
                val updatedAnnouncements = if (currentAnnouncements.isEmpty()) newEntry else "$newEntry##$currentAnnouncements"
                
                academyPrefs.edit { putString("announcements", updatedAnnouncements) }

                Toast.makeText(this, "Announcement Sent to $audience", Toast.LENGTH_SHORT).show()
                loadAnnouncements()
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
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