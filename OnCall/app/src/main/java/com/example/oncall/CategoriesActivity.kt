package com.example.oncall

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton

import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.oncall.adapter.CategoriesAdapter
import com.example.oncall.models.Category
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// Certain sections of code in this class have been adapted from this source:
// Author: Mkr Developer
// Source: https://www.youtube.com/watch?v=KiJy5Oi4rRo&list=PLEGrY4uRTu5ls7Mq7h6RcdKGFdQVqy0KZ

class CategoriesActivity : AppCompatActivity(), CategoriesAdapter.OnCategoryClickListener {
    // declare variables

    private lateinit var category: EditText
    private lateinit var saveBtn: Button
    private lateinit var homeBtn : ImageButton
    private lateinit var catBtn : ImageButton
    private lateinit var goalsBtn : ImageButton
    private lateinit var timesheetBtn : ImageButton
    private lateinit var displayCategories: RecyclerView
    private var categories = mutableListOf<Category>()
    private lateinit var categoriesAdapter : CategoriesAdapter


    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_categories)

        // Initialize the views
        category = findViewById(R.id.category_name)
        saveBtn = findViewById(R.id.save_category)
        displayCategories = findViewById(R.id.displayCategories)
        homeBtn = findViewById(R.id.navToHome)
        catBtn = findViewById(R.id.navToCategory)
        goalsBtn = findViewById(R.id.navToGoals)
        timesheetBtn = findViewById(R.id.navToTimesheet)

        // initialising Firebase auth object
        auth = FirebaseAuth.getInstance()
        // create table
        database = FirebaseDatabase.getInstance().getReference("Category")

        saveBtn.setOnClickListener{
            saveCategory()
        }

        // Makeshift nav bar --
        goalsBtn.setOnClickListener {
            val intent = Intent(this, SetGoalsActivity::class.java)
            startActivity(intent)
            finish()
        }

        homeBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        catBtn.setOnClickListener {
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
            finish()
        }

        timesheetBtn.setOnClickListener {
            val intent = Intent(this, TimesheetActivity::class.java)
            startActivity(intent)
            finish()
        }
        // -- Makeshift nav bar

        // Set up the RecyclerView
        displayCategories.layoutManager = LinearLayoutManager(this)

        categoriesAdapter = CategoriesAdapter(categories, this, ::onCategoryLongClick)
        displayCategories.adapter = categoriesAdapter

        fetchCategories()
    }

    private fun saveCategory() {
       val userCategory = category.text.toString()

        // Associate user with category they create
        val userId = auth.currentUser?.uid
        val hours = "0"

        if(userCategory.isEmpty()){
            Toast.makeText(this, "Enter a Category", Toast.LENGTH_SHORT).show()
            category.text.clear()
        }

        // Create unique ID for each category
        val categoryId = database.push().key!!
        val categoryInfo = Category(categoryId, userId, userCategory, hours)

        // Save category to database
        database.child(categoryId).setValue(categoryInfo)
            .addOnCompleteListener{
                Toast.makeText(this, "Category Saved", Toast.LENGTH_LONG).show()
                category.text.clear()

                // Clear the categories list and fetch the updated data from the database
                categories.clear()
                fetchCategories()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Category not saved ${it.message} ", Toast.LENGTH_SHORT).show()
                category.text.clear()
            }
    }

    // Fetch categories from database
    private fun fetchCategories() {
        val currentUserId = auth.currentUser?.uid ?: return

        categories.clear()
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (categorySnapshot in dataSnapshot.children) {
                    val firebaseID = categorySnapshot.child("firebaseID").value as? String ?: ""

                    // Check if the category belongs to the current user
                    if (firebaseID == currentUserId) {
                        val categoryId = categorySnapshot.key ?: ""
                        val categoryName = categorySnapshot.child("categoryName").value as? String ?: ""
                        val categoryHours = categorySnapshot.child("categoryHours").value as? String ?: ""
                        categories.add(Category(categoryId, firebaseID, categoryName, categoryHours))
                    }
                }
                categoriesAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showError("Error fetching categories: ${databaseError.message}") {
                    fetchCategories()
                }
            }
        })
    }

    private fun showError(message: String, retry: () -> Unit) {
        Snackbar.make(displayCategories, message, Snackbar.LENGTH_LONG)
            .setAction("Retry") { retry.invoke() }
            .show()
    }

    // Function for updating category names
    override fun onCategoryClick(categoryId: String, categoryName: String) {
        // Get the category name based on the category ID
        val category = categories.find { it.id == categoryId }
        val categoryName = category?.categoryName ?: ""

        // Allow the user to enter a new category name
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Update Category Name")

        val input = EditText(this)
        input.setText(categoryName)
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val newCategoryName = input.text.toString().trim()
            if (newCategoryName.isNotEmpty()) {
                updateCategory(categoryId, newCategoryName)
            } else {
                Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    // Update the database with new category name
    private fun updateCategory(categoryId: String, newCategoryName: String) {
        val categoryRef = database.child(categoryId)

        categoryRef.child("categoryName").setValue(newCategoryName)
            .addOnSuccessListener {
                // Update the category
                val categoryToUpdate = categories.find { it.id == categoryId }
                categoryToUpdate?.categoryName = newCategoryName

                // Clear the categories list and fetch the updated data from the database
                categories.clear()
                fetchCategories()

                Toast.makeText(this, "Category updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update category: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    // Function to delete category from list and database
    private fun onCategoryLongClick(categoryId: String) {
        showDeleteConfirmationDialog(categoryId)
    }

    // Confirmation box to delete entry
    private fun showDeleteConfirmationDialog(categoryId: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete this category?")
            .setPositiveButton("Delete") { _, _ ->
                deleteCategory(categoryId)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Delete entry from database
    private fun deleteCategory(categoryId: String) {
        database.child(categoryId).removeValue()
            .addOnSuccessListener {
                // Refresh the categories list
                categories.clear()
                fetchCategories()
                Toast.makeText(this, "Category deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete category: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}