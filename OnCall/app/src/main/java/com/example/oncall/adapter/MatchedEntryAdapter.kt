package com.example.oncall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.oncall.R
import com.example.oncall.models.MatchedEntry

// Adapter class to display entries when a user sets work goals for the week

// Certain sections of code in this class have been adapted from this source:
// Author: Mkr Developer
// Source: https://www.youtube.com/watch?v=KiJy5Oi4rRo&list=PLEGrY4uRTu5ls7Mq7h6RcdKGFdQVqy0KZ

class MatchedEntryAdapter(private var matchedEntries: List<MatchedEntry>) :
    RecyclerView.Adapter<MatchedEntryAdapter.ViewHolder>() {

        // Interface for deleting goals
        interface OnItemClickListener {
            fun onItemClick(matchedEntry: MatchedEntry)
        }

        private var listener: OnItemClickListener? = null

        fun setOnItemClickListener(listener: OnItemClickListener) {
            this.listener = listener
        }


    fun updateData(newData: List<MatchedEntry>) {
        val diffResult = DiffUtil.calculateDiff(MatchedEntryDiffCallback(matchedEntries, newData))
        matchedEntries = newData
        diffResult.dispatchUpdatesTo(this)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_matched_entry, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val matchedEntry = matchedEntries[position]
        holder.bind(matchedEntry)
    }

    override fun getItemCount(): Int {
        return matchedEntries.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private val dateTextView: TextView = itemView.findViewById(R.id.date_text_view)
        private val minHoursTextView: TextView = itemView.findViewById(R.id.min_hours_text_view)
        private val maxHoursTextView: TextView = itemView.findViewById(R.id.max_hours_text_view)
       // private val hoursWorkedTextView: TextView = itemView.findViewById(R.id.hours_worked_text_view)

        fun bind(matchedEntry: MatchedEntry) {
            dateTextView.text = matchedEntry.date
            minHoursTextView.text = "Min hours set: ${matchedEntry.minHoursSet}"
            maxHoursTextView.text = "Max hours set: ${matchedEntry.maxHoursSet}"
          //  hoursWorkedTextView.text = "Actual hours worked: ${matchedEntry.hoursWorked}"
        }


        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener?.onItemClick(matchedEntries[position])
            }
        }

    }
}


// Helper class to create a new list if goals are added for a different date
class MatchedEntryDiffCallback(
    private val oldList: List<MatchedEntry>,
    private val newList: List<MatchedEntry>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].date == newList[newItemPosition].date
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem.minHoursSet == newItem.minHoursSet &&
                oldItem.maxHoursSet == newItem.maxHoursSet &&
                oldItem.hoursWorked == newItem.hoursWorked
    }
}