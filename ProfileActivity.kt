package com.example.segotlo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivProfilePic: ImageView
    private lateinit var etUsername: EditText
    private lateinit var etAge: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnEdit: Button
    private var isEditing = false

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            ivProfilePic.setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        ivProfilePic = findViewById(R.id.iv_profile_pic)
        etUsername = findViewById(R.id.et_profile_username)
        etAge = findViewById(R.id.et_profile_age)
        etEmail = findViewById(R.id.et_profile_email)
        etPhone = findViewById(R.id.et_profile_phone)
        btnEdit = findViewById(R.id.btn_edit_profile)

        val tvName = findViewById<TextView>(R.id.tv_profile_name)
        val tvSub = findViewById<TextView>(R.id.tv_profile_sub)
        val tvParent = findViewById<TextView>(R.id.tv_profile_parent)

        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        loadUserData()

        ivProfilePic.setOnClickListener {
            if (isEditing) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                pickImage.launch(intent)
            }
        }

        btnEdit.setOnClickListener {
            if (!isEditing) {
                // Enter Edit Mode
                isEditing = true
                enableEditing(true)
                btnEdit.text = "Save Profile"
            } else {
                // Save Data
                saveUserData()
                isEditing = false
                enableEditing(false)
                btnEdit.text = "Edit Profile"
                Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        findViewById<TextView>(R.id.tv_profile_name).text = prefs.getString("fullName", "Lesesi")
        etUsername.setText(prefs.getString("username", "lee"))
        etAge.setText(prefs.getString("age", "14"))
        etEmail.setText(prefs.getString("email", "lesedi@gmail.com"))
        etPhone.setText(prefs.getString("parentPhone", "0649729721"))
        findViewById<TextView>(R.id.tv_profile_parent).text = "Mary Molefe" // Parent name non-editable as requested
        findViewById<TextView>(R.id.tv_profile_sub).text = "U15 • ${prefs.getString("position", "soccer")}"
    }

    private fun saveUserData() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        prefs.edit().apply {
            putString("username", etUsername.text.toString())
            putString("age", etAge.text.toString())
            putString("email", etEmail.text.toString())
            putString("parentPhone", etPhone.text.toString())
            apply()
        }
    }

    private fun enableEditing(enabled: Boolean) {
        etUsername.isEnabled = enabled
        etAge.isEnabled = enabled
        etEmail.isEnabled = enabled
        etPhone.isEnabled = enabled
        
        if (enabled) {
            etUsername.setBackgroundResource(R.drawable.edit_text_bg)
            etAge.setBackgroundResource(R.drawable.edit_text_bg)
            etEmail.setBackgroundResource(R.drawable.edit_text_bg)
            etPhone.setBackgroundResource(R.drawable.edit_text_bg)
        } else {
            etUsername.setBackgroundResource(android.R.color.transparent)
            etAge.setBackgroundResource(android.R.color.transparent)
            etEmail.setBackgroundResource(android.R.color.transparent)
            etPhone.setBackgroundResource(android.R.color.transparent)
        }
    }
}