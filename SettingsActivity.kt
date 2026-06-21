package com.example.segotlo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var ivProfile: ImageView

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            ivProfile.setImageURI(imageUri)
            // In a real app, you'd save this URI to persistent storage
            Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        ivProfile = findViewById(R.id.iv_settings_profile)
        val btnChangePic = findViewById<Button>(R.id.btn_change_pic)

        btnChangePic.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImage.launch(intent)
        }

        // Theme selection clicks
        findViewById<android.view.View>(R.id.theme_yellow).setOnClickListener { updateTheme("#FFEA00") }
        findViewById<android.view.View>(R.id.theme_blue).setOnClickListener { updateTheme("#00BFFF") }
        findViewById<android.view.View>(R.id.theme_green).setOnClickListener { updateTheme("#32CD32") }
        findViewById<android.view.View>(R.id.theme_red).setOnClickListener { updateTheme("#FF4500") }
    }

    private fun updateTheme(colorHex: String) {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        prefs.edit().putString("themeColor", colorHex).apply()
        Toast.makeText(this, "Theme color updated! Restart app to apply.", Toast.LENGTH_SHORT).show()
    }
}