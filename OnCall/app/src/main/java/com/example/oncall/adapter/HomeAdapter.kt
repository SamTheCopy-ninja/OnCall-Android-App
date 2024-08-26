package com.example.oncall.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.oncall.HomeActivity
import com.example.oncall.R
import com.example.oncall.models.TimesheetEntry
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso



// Adapter for recycler view used to display timesheet entries on the Home page

// Certain sections of code in this class have been adapted from this source:
// Author: Mkr Developer
// Source: https://www.youtube.com/watch?v=KiJy5Oi4rRo&list=PLEGrY4uRTu5ls7Mq7h6RcdKGFdQVqy0KZ

class HomeAdapter(
    private val context: Context,
    private var homeItems: List<TimesheetEntry>,
    private val onItemClick: (TimesheetEntry) -> Unit
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    // function to filter results when a user searches for entries by date
    fun updateData(newEntries: List<TimesheetEntry>) {
        homeItems = newEntries
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskDate: TextView = itemView.findViewById(R.id.taskDate)
        val taskTime: TextView = itemView.findViewById(R.id.taskTime)
        val taskName: TextView = itemView.findViewById(R.id.taskName)
        val taskTag: TextView = itemView.findViewById(R.id.taskTag)
        val taskImg : ImageView = itemView.findViewById(R.id.taskImg)
        val taskLocation: TextView = itemView.findViewById(R.id.taskLocation)
        val taskBudget: TextView = itemView.findViewById(R.id.taskBudget)
        val taskDesc: TextView = itemView.findViewById(R.id.taskDesc)


        init {
            itemView.setOnClickListener {
                onItemClick(homeItems[adapterPosition])
            }

            itemView.setOnLongClickListener {
                val activity = context as? HomeActivity
                val entry = homeItems[adapterPosition]
                activity?.showMarkCompletedConfirmationDialog(entry)
                true
            }

        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.entries, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = homeItems[position]
        holder.taskDate.text = currentItem.startDate
        holder.taskTime.text = "at: ${currentItem.startTime}"
        holder.taskName.text = currentItem.taskName
        holder.taskTag.text = "Tag: ${currentItem.selectedCategory}"
        holder.taskDesc.text = "Notes: ${currentItem.taskNotes}"
        holder.taskBudget.text = "for R${currentItem.hourlyRate}/hour"
        holder.taskLocation.text = "at: ${currentItem.taskLocation}"



        // Load the image using Picasso
        if (currentItem.photoUpload != null)
        {
            Picasso.get().load(currentItem.photoUpload).into(holder.taskImg)
        }
        else
        {
            // Set a default image if the user did not add one
            holder.taskImg.setImageResource(R.drawable.baseline_add_a_photo)
        }

        // Check if the entry should be displayed as completed
        if (currentItem.displayAsCompleted) {
            // Cross out the text or change the color

            holder.taskName.paintFlags = holder.taskName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.taskName.setTextColor(Color.GRAY)

            holder.taskTime.paintFlags = holder.taskTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.taskTime.setTextColor(Color.GRAY)

            holder.taskDate.paintFlags = holder.taskDate.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.taskDate.setTextColor(Color.GRAY)

            holder.taskTag.paintFlags = holder.taskTag.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.taskTag.setTextColor(Color.GRAY)

            holder.taskDesc.paintFlags = holder.taskTag.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.taskDesc.setTextColor(Color.GRAY)

            holder.taskBudget.paintFlags = holder.taskTag.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.taskBudget.setTextColor(Color.GRAY)

            holder.taskLocation.paintFlags = holder.taskTag.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.taskLocation.setTextColor(Color.GRAY)
        } else {
            // Reset the text appearance for non-completed entries
            holder.taskName.paintFlags = holder.taskName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.taskName.setTextColor(Color.BLACK)
            holder.taskTime.setTextColor(Color.BLACK)
            holder.taskDate.setTextColor(Color.BLACK)
            holder.taskTag.setTextColor(Color.BLACK)
            holder.taskDesc.setTextColor(Color.BLACK)
            holder.taskBudget.setTextColor(Color.BLACK)
            holder.taskLocation.setTextColor(Color.BLACK)
        }

    }

    override fun getItemCount(): Int {
        return homeItems.size
    }
}