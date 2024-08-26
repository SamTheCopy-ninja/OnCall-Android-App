package com.example.oncall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.oncall.R
import com.example.oncall.models.Category

// Adapter for recycler view used to display created

// Certain sections of code in this class have been adapted from this source:
// Author: Mkr Developer
// Source: https://www.youtube.com/watch?v=KiJy5Oi4rRo&list=PLEGrY4uRTu5ls7Mq7h6RcdKGFdQVqy0KZ

class CategoriesAdapter(
    private val categories: MutableList<Category>,
    private val clickListener: OnCategoryClickListener,
    private val onCategoryLongClick: (String) -> Unit
) : RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {

    // Interface to handle click events
    interface OnCategoryClickListener {
        fun onCategoryClick(categoryId: String, categoryName: String)
    }


    interface OnCategoryLongClickListener {
        fun onCategoryLongClick(categoryId: String)
    }

    class CategoryViewHolder(
        itemView: View,
        private val categories: MutableList<Category>,
        private val onCategoryLongClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        val categoryNameTextView: TextView = itemView.findViewById(R.id.tv_category_name)
        val hoursSpentTextView: TextView = itemView.findViewById(R.id.tv_hours_spent)

        init {
            itemView.setOnLongClickListener {
                categories[adapterPosition].id?.let { it1 -> onCategoryLongClick(it1) }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
        return CategoryViewHolder(view, categories, onCategoryLongClick)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryNameTextView.text = category.categoryName
        holder.hoursSpentTextView.text = "Hours Spent: ${category.categoryHours ?: "0"}"

        // Set background color categories if clicked
        val backgroundColor = if (category.isSelected) {
            ContextCompat.getColor(holder.itemView.context, R.color.grey)
        } else {
            ContextCompat.getColor(holder.itemView.context, R.color.amber)
        }
        holder.itemView.setBackgroundColor(backgroundColor)

        // Set click listener
        holder.itemView.setOnClickListener {
            // Deselect previously selected item
            categories.forEach { it.isSelected = false }

            // Select the clicked item
            category.isSelected = true

            // Notify the adapter that the data has changed
            notifyDataSetChanged()

            category.id?.let { it1 -> category.categoryName?.let { it2 ->
                clickListener.onCategoryClick(it1, it2)
            } }
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }
}