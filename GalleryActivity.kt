package com.example.segotlo

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gallery)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }

        val rvGallery = findViewById<RecyclerView>(R.id.rv_gallery)
        rvGallery.layoutManager = GridLayoutManager(this, 2)

        val images = listOf(
            R.drawable.img1,
            R.drawable.img2,
            R.drawable.img3,
            R.drawable.img4,
            R.drawable.img7,
            R.drawable.img8,
            R.drawable.img9,
            R.drawable.img11
        )

        rvGallery.adapter = GalleryAdapter(images)
    }
}