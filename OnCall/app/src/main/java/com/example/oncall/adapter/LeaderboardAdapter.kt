package com.example.oncall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.oncall.R

class LeaderboardAdapter(private val leaderboardList: List<Pair<String, Int>>) :
    RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.leaderboard_item, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val (email, completedEntriesCount) = leaderboardList[position]
        holder.bind(email, completedEntriesCount)
    }

    override fun getItemCount() = leaderboardList.size

    inner class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val emailTextView: TextView = itemView.findViewById(R.id.email_text_view)
        private val completedEntriesTextView: TextView = itemView.findViewById(R.id.completed_entries_text_view)

        fun bind(email: String, completedEntriesCount: Int) {
//            emailTextView.text = email
//            completedEntriesTextView.text = completedEntriesCount.toString()
            emailTextView.text = "User: $email"
            completedEntriesTextView.text = "Entries Completed: $completedEntriesCount"
        }
    }
}