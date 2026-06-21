package com.example.segotlo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class FixtureItem(
    val title: String, 
    val details: String, 
    val status: String, 
    val score: String? = null,
    val isResult: Boolean = false
)

class FixtureAdapter(private val items: List<FixtureItem>) :
    RecyclerView.Adapter<FixtureAdapter.FixtureViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FixtureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fixture, parent, false)
        return FixtureViewHolder(view)
    }

    override fun onBindViewHolder(holder: FixtureViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvDetails.text = item.details
        holder.tvStatus.text = item.status
        
        if (item.isResult && item.score != null) {
            holder.tvScore.text = item.score
            holder.tvScore.visibility = View.VISIBLE
            holder.tvStatus.text = "FT" // Standard for results
        } else {
            holder.tvScore.visibility = View.GONE
            holder.tvStatus.text = item.status
        }
    }

    override fun getItemCount(): Int = items.size

    class FixtureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_match_title)
        val tvDetails: TextView = itemView.findViewById(R.id.tv_match_details)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_match_status)
        val tvScore: TextView = itemView.findViewById(R.id.tv_match_score)
    }
}