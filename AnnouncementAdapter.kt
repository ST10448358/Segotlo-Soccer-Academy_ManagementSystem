package com.example.segotlo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class AnnouncementItem(val title: String, val date: String, val desc: String)

class AnnouncementAdapter(private val items: List<AnnouncementItem>) :
    RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_announcement, parent, false)
        return AnnouncementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvDate.text = item.date
        holder.tvDesc.text = item.desc
    }

    override fun getItemCount(): Int = items.size

    class AnnouncementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_announcement_title)
        val tvDate: TextView = itemView.findViewById(R.id.tv_announcement_date)
        val tvDesc: TextView = itemView.findViewById(R.id.tv_announcement_desc)
    }
}