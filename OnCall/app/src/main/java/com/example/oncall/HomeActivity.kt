package com.example.oncall


import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.oncall.adapter.HomeAdapter
import com.example.oncall.adapter.PreferenceUtils
import com.example.oncall.models.TimesheetEntry
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Certain sections of code in this class have been adapted from this source:
// Author: Mkr Developer
// Source: https://www.youtube.com/watch?v=KiJy5Oi4rRo&list=PLEGrY4uRTu5ls7Mq7h6RcdKGFdQVqy0KZ

class HomeActivity : AppCompatActivity() {

    // declare variables
    private lateinit var newTimesheet : Button
    private lateinit var goHome : ImageButton
    private lateinit var goCat : ImageButton
    private lateinit var goGoals : ImageButton
    private lateinit var goTimesheet : ImageButton
    private lateinit var itemsToDisplay : RecyclerView
    private var entries = mutableListOf<TimesheetEntry>()
    private lateinit var homeAdapter: HomeAdapter

    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var filterButton: Button
    private lateinit var dataView: Button
    private lateinit var leaderboardBtn: Button
    private lateinit var logoutBtn: Button

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database : DatabaseReference
    private lateinit var taskImage: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Initialize the views
        newTimesheet = findViewById(R.id.timesheetBtn)

        itemsToDisplay = findViewById(R.id.displayHomeList)
        goCat = findViewById(R.id.goToCat)
        goHome = findViewById(R.id.goToHome)
        goGoals = findViewById(R.id.goToGoals)
        goTimesheet = findViewById(R.id.goToTimesheet)

        startDateInput = findViewById(R.id.start_date_input)
        endDateInput = findViewById(R.id.end_date_input)
        filterButton = findViewById(R.id.filter_button)
        dataView = findViewById(R.id.graphPage)
        leaderboardBtn = findViewById(R.id.leaderboard)
        logoutBtn = findViewById(R.id.logout)

        // initialising Firebase auth object
        auth = FirebaseAuth.getInstance()

        // Database storage for uploaded photos
        taskImage = FirebaseStorage.getInstance().getReference("Images")

        // Database table for created timesheet
         database = FirebaseDatabase.getInstance().getReference("Timesheet")

        // Set up the RecyclerView
        itemsToDisplay.layoutManager = LinearLayoutManager(this)

        // Initialize the Adapter
        homeAdapter = HomeAdapter(this, entries) { entry ->
          showDeleteConfirmationDialog(entry)
        }

        itemsToDisplay.adapter = homeAdapter

        newTimesheet.setOnClickListener {
            val intent = Intent(this, TimesheetActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Makeshift nav bar --
        goGoals.setOnClickListener {
            val intent = Intent(this, SetGoalsActivity::class.java)
            startActivity(intent)
            finish()
        }

        goHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        goCat.setOnClickListener {
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
            finish()
        }

        goTimesheet.setOnClickListener {
            val intent = Intent(this, TimesheetActivity::class.java)
            startActivity(intent)
            finish()
        }
        // -- Makeshift nav bar

        
        filterButton.setOnClickListener {
            filterTimeSheetEntries() // Filter timesheet entries displayed based on date
        }

        dataView.setOnClickListener{
            val intent = Intent(this, GraphActivity::class.java)
            startActivity(intent)
            finish()
        }

        leaderboardBtn.setOnClickListener{
            val intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        logoutBtn.setOnClickListener {
            // Sign out the user from Firebase Authentication
            FirebaseAuth.getInstance().signOut()

            // Reset the notification shown flag
            PreferenceUtils.setNotificationShown(this@HomeActivity, false)

            // Send user back to the login screen
            val intent = Intent(this@HomeActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        fetchTimesheetEntries()
    }

    // Function to fetch entries from database
    private fun fetchTimesheetEntries() {
        val currentUserId = auth.currentUser?.uid ?: return

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                entries.clear() // Clear the existing entries before fetching new ones

                for (entrySnapshot in dataSnapshot.children) {
                    val firebaseID = entrySnapshot.child("firebaseID").value as? String ?: ""

                    // Check if the entry belongs to the current user
                    if (firebaseID == currentUserId) {
                        val id = entrySnapshot.key ?: ""
                        val taskName = entrySnapshot.child("taskName").value as? String ?: ""
                        val taskLocation = entrySnapshot.child("taskLocation").value as? String ?: ""
                        val hourlyRate = entrySnapshot.child("hourlyRate").value as? String ?: ""
                        val selectedCategory = entrySnapshot.child("selectedCategory").value as? String ?: ""
                        val startTime = entrySnapshot.child("startTime").value as? String ?: ""
                        val endTime = entrySnapshot.child("endTime").value as? String ?: ""
                        val startDate = entrySnapshot.child("startDate").value as? String ?: ""
                        val endDate = entrySnapshot.child("endDate").value as? String ?: ""
                        val taskNotes = entrySnapshot.child("taskNotes").value as? String ?: ""
                        val user = entrySnapshot.child("user").value as? String ?: ""
                        val isCompleted = entrySnapshot.child("completed").value as? Boolean ?: false
                        val displayAsCompleted = entrySnapshot.child("displayAsCompleted").value as? Boolean ?: false

                        // Check if the photoUpload field exists
                        if (entrySnapshot.hasChild("photoUpload")) {
                            val photoUpload = entrySnapshot.child("photoUpload").value as? String ?: ""
                            entries.add(TimesheetEntry(id, firebaseID, taskName, taskLocation, hourlyRate, selectedCategory, startTime, endTime, startDate, endDate, taskNotes, photoUpload, user, isCompleted, displayAsCompleted))
                        } else {
                            // Display entry without a photo
                            entries.add(TimesheetEntry(id, firebaseID, taskName, taskLocation, hourlyRate, selectedCategory, startTime, endTime, startDate, endDate, taskNotes, null, user, isCompleted, displayAsCompleted))
                        }
                    }
                }

                homeAdapter.notifyDataSetChanged()

                // Display reminder
                // Check if any entry has a start date that matches the current date
                val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                val matchingEntries = entries.filter { entry ->
                    entry.startDate == currentDate
                }

                if (matchingEntries.isNotEmpty() && !PreferenceUtils.isNotificationShown(this@HomeActivity)) {
                    // Show the pop-up notification
                    showPopupNotification()
                    PreferenceUtils.setNotificationShown(this@HomeActivity, true)
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                showError("Error fetching timesheet entries: ${databaseError.message}") { fetchTimesheetEntries() }
            }
        })
    }


// Database error message
    private fun showError(message: String, retry: () -> Unit) {
        Snackbar.make(itemsToDisplay, message, Snackbar.LENGTH_LONG)
            .setAction("Retry") { retry.invoke() }
            .show()
    }

    // Function to search for entries based on date
    private fun filterTimeSheetEntries() {
        val startDate = parseDate(startDateInput.text.toString())
        val endDate = parseDate(endDateInput.text.toString()) // Get user dates

        if (startDate != null && endDate != null) {
            val filteredEntries = entries.filter { entry ->
                val entryDate = entry.startDate?.let { parseDate(it) }
                entryDate != null && entryDate >= startDate && entryDate <= endDate
            }

            homeAdapter.updateData(filteredEntries)
        } else {
            Toast.makeText(this, "Invalid Dates", Toast.LENGTH_LONG).show()
            startDateInput.text.clear()
            endDateInput.text.clear()
        }
    }

    // Format the dates as they appear in the database
    private fun parseDate(dateString: String): Date? {
        val dateFormat = SimpleDateFormat("dd-M-yyyy", Locale.getDefault())
        return try {
            dateFormat.parse(dateString)
        } catch (e: ParseException) {
            null
        }
    }


    // Function to delete entries
    private fun showDeleteConfirmationDialog(entry: TimesheetEntry) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Entry")
            .setMessage("Are you sure you want to delete this entry?")
            .setPositiveButton("Delete") { _, _ ->
                deleteTimesheetEntry(entry)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Delete the entry from the database
    private fun deleteTimesheetEntry(entry: TimesheetEntry) {
        database = FirebaseDatabase.getInstance().getReference("Timesheet")
        val entryId = entry.id
        if (entryId != null) {
            database.child(entryId).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Entry deleted successfully", Toast.LENGTH_SHORT).show()
                    // Remove the deleted entry from the list
                    entries.remove(entry)
                    homeAdapter.notifyDataSetChanged()
                    entries.clear()
                    fetchTimesheetEntries()
                }
                .addOnFailureListener { error ->
                    showError("Error deleting entry: ${error.message}") { deleteTimesheetEntry(entry) }
                }
        }
    }

    // Pop-up to ask the user if they want to mark a Timesheet entry as complete
    fun showMarkCompletedConfirmationDialog(entry: TimesheetEntry) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Mark as Completed")
            .setMessage("Are you sure you want to mark this entry as completed?")
            .setPositiveButton("Mark as Completed") { _, _ ->
                markEntryAsCompleted(entry, homeAdapter)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Function to mark Timesheet entries on the home screen as "completed"
    private fun markEntryAsCompleted(entry: TimesheetEntry, homeAdapter: HomeAdapter) {
        val entryId = entry.id
        if (entryId != null) {
            val entryRef = FirebaseDatabase.getInstance().reference.child("Timesheet").child(entryId)

            // Mark the entry as completed in the database
            val updates = hashMapOf<String, Any>(
                "completed" to true,
                "displayAsCompleted" to true
            )
            entryRef.updateChildren(updates)
                .addOnSuccessListener {
                    // Update the entry in the local list
                    val updatedEntry = entry.copy(isCompleted = true, displayAsCompleted = true)
                    val updatedList = entries.toMutableList().apply {
                        val index = indexOf(entry)
                        if (index != -1) {
                            set(index, updatedEntry)
                        }
                    }

                    // Update the RecyclerView data
                    homeAdapter.updateData(updatedList)
                }
                .addOnFailureListener { error ->
                    showError("Error marking entry as completed: ${error.message}") { markEntryAsCompleted(entry, homeAdapter) }
                }
        }
    }


// Function to display a pop-up if a the user has a Timesheet entry in the database for the present day (Today)
    private fun showPopupNotification() {
        if (!isFinishing) {
            val builder = AlertDialog.Builder(this)
            val titleSpan = SpannableString("Timesheet Entry Reminder").apply {
                setSpan(ForegroundColorSpan(Color.BLACK), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(StyleSpan(Typeface.BOLD), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            val messageSpan = SpannableString("You have at least one timesheet entry scheduled for today.").apply {
                setSpan(ForegroundColorSpan(Color.BLUE), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(RelativeSizeSpan(1.2f), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            builder.setIcon(R.drawable.baseline_add_alert_24)
                .setTitle(titleSpan)
                .setMessage(messageSpan)
                .setPositiveButton("OK", null)
                .show()
        }
    }

}