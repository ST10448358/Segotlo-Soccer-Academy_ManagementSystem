package com.example.segotlo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistrationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nameInput = findViewById<EditText>(R.id.etName)
        val emailInput = findViewById<EditText>(R.id.etEmail)
        val passwordInput = findViewById<EditText>(R.id.etPassword)
        val spinnerRole = findViewById<Spinner>(R.id.spinnerRole)
        val button = findViewById<Button>(R.id.btnCreateAccount)

        // Setup Role Spinner
        val roles = arrayOf("Player", "Parent")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter

        button.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val selectedRole = spinnerRole.selectedItem.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid
                    val userMap = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "role" to selectedRole,
                        "uid" to uid,
                        "isApproved" to (selectedRole != "Player"), // Auto-approve Parents, Player needs admin
                        "isRejected" to false
                    )

                    if (uid != null) {
                        db.collection("users")
                            .document(uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                Log.d("REG", "User saved to Firestore")
                                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                                
                                if (selectedRole == "Player") {
                                    val successIntent = Intent(this, RegistrationSuccessActivity::class.java)
                                    successIntent.putExtra("username", email)
                                    startActivity(successIntent)
                                } else {
                                    startActivity(Intent(this, MainActivity::class.java))
                                }
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Firestore Error: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Auth Error: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}