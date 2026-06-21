package com.example.segotlo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private var selectedRole: String? = null
    private var isPasswordVisible = false

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etEmail = findViewById<EditText>(R.id.et_username) // Using et_username as email field
        val etPassword = findViewById<EditText>(R.id.et_password)
        val ivPasswordToggle = findViewById<ImageView>(R.id.iv_password_toggle)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.tv_forgot_password).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        findViewById<TextView>(R.id.tv_register).setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        ivPasswordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivPasswordToggle.setImageResource(R.drawable.ic_eye)
                ivPasswordToggle.setColorFilter(getColor(R.color.academy_yellow))
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivPasswordToggle.setImageResource(R.drawable.ic_eye_off)
                ivPasswordToggle.setColorFilter(Color.parseColor("#555555"))
            }
            etPassword.setSelection(etPassword.text.length)
        }

        findViewById<Button>(R.id.btn_login).setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (selectedRole == null) {
                Toast.makeText(this, "Please choose a role", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                Toast.makeText(this, "Please fill in the email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Please fill in the password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Standard credentials for Admin/Coach for quick testing (as in your previous code)
            if (selectedRole == "Admin" && ((email == "admin" && password == "admin") || (email == "Lesedi" && password == "1234"))) {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
                return@setOnClickListener
            }

            if (selectedRole == "Coach" && (email == "coach" && password == "coach")) {
                startActivity(Intent(this, CoachDashboardActivity::class.java))
                finish()
                return@setOnClickListener
            }

            // Firebase Login
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid
                    if (uid != null) {
                        checkUserInFirestore(uid)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        setupRoleSelection()
    }

    private fun checkUserInFirestore(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    val isApproved = document.getBoolean("isApproved") ?: false

                    if (role == selectedRole) {
                        if (selectedRole == "Player" && !isApproved) {
                            Toast.makeText(this, "Account pending approval by admin", Toast.LENGTH_LONG).show()
                            auth.signOut()
                        } else {
                            // Valid login and approved (if player)
                            navigateToDashboard(role)
                        }
                    } else {
                        Toast.makeText(this, "Role mismatch: Found $role but expected $selectedRole", Toast.LENGTH_LONG).show()
                        auth.signOut()
                    }
                } else {
                    Toast.makeText(this, "User data not found in Firestore", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToDashboard(role: String?) {
        val intent = when (role) {
            "Admin" -> Intent(this, AdminDashboardActivity::class.java)
            "Coach" -> Intent(this, CoachDashboardActivity::class.java)
            "Player" -> Intent(this, PlayerDashboardActivity::class.java)
            "Parent" -> Intent(this, ParentDashboardActivity::class.java)
            else -> null
        }
        if (intent != null) {
            startActivity(intent)
            finish()
        }
    }

    private fun setupRoleSelection() {
        val player = findViewById<MaterialCardView>(R.id.role_player)
        val parent = findViewById<MaterialCardView>(R.id.role_parent)
        val coach = findViewById<MaterialCardView>(R.id.role_coach)
        val admin = findViewById<MaterialCardView>(R.id.role_admin)

        val roles = mapOf(
            player to "Player",
            parent to "Parent",
            coach to "Coach",
            admin to "Admin"
        )

        roles.keys.forEach { roleCard ->
            roleCard.setOnClickListener {
                roles.keys.forEach { r -> 
                    r.strokeWidth = 0 
                }
                roleCard.strokeWidth = 4
                selectedRole = roles[roleCard]
            }
        }
    }
}