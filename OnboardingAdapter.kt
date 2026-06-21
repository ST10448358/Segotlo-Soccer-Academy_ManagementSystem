package com.example.segotlo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class OnboardingAdapter(
    private val slideLayouts: List<Int>
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return slideLayouts[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        val context = holder.itemView.context
        
        if (getItemViewType(position) == R.layout.onboarding_contact) {
            val btnSend = holder.itemView.findViewById<Button>(R.id.btn_onboarding_send_msg)
            val etName = holder.itemView.findViewById<EditText>(R.id.et_onboarding_name)
            val etEmail = holder.itemView.findViewById<EditText>(R.id.et_onboarding_email)
            val etMsg = holder.itemView.findViewById<EditText>(R.id.et_onboarding_msg)

            btnSend?.setOnClickListener {
                val name = etName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val msg = etMsg.text.toString().trim()

                if (name.isNotEmpty() && email.isNotEmpty() && msg.isNotEmpty()) {
                    // Save message for Admin
                    val prefs = context.getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE)
                    prefs.edit().apply {
                        putString("lastMsgName", name)
                        putString("lastMsgEmail", email)
                        putString("lastMsgContent", msg)
                        apply()
                    }
                    Toast.makeText(context, "Message sent to Admin!", Toast.LENGTH_SHORT).show()
                    etName.setText("")
                    etEmail.setText("")
                    etMsg.setText("")
                } else {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        if (getItemViewType(position) == R.layout.item_onboarding) {
            holder.itemView.findViewById<ImageView>(R.id.onboarding_image)?.setImageResource(R.drawable.img11)
        }
    }

    override fun getItemCount(): Int = slideLayouts.size

    class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}