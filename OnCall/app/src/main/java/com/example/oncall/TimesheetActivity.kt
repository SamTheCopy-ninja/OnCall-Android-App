package com.example.oncall

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.oncall.adapter.CategoriesAdapter
import com.example.oncall.adapter.PreferenceUtils
import com.example.oncall.models.Category
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
import java.text.SimpleDateFormat

import java.util.Calendar
import java.util.Date
import java.util.Locale

// Certain sections of code in this class have been adapted from this source:
// Author: Mkr Developer
// Source: https://www.youtube.com/watch?v=KiJy5Oi4rRo&list=PLEGrY4uRTu5ls7Mq7h6RcdKGFdQVqy0KZ

class TimesheetActivity : AppCompatActivity() {
    // variables
    private lateinit var  taskName: EditText
    private lateinit var  taskLocation: EditText
    private lateinit var  hourlyRate: EditText
    private lateinit var  startTime: TextView
    private lateinit var  endTime: TextView
    private lateinit var  startDate: TextView
    private lateinit var  endDate: TextView
    private lateinit var  recyclerList: RecyclerView
    private lateinit var  taskNotes: EditText
    private lateinit var  photoUpload: ImageView
    private lateinit var  uploadPhoto: Button
    private lateinit var  saveInfo: Button

    private lateinit var homeScreen : ImageButton
    private lateinit var catScreen : ImageButton
    private lateinit var goalScreen : ImageButton
    private lateinit var timesheetScreen : ImageButton

    // Variable to store category user clicked on
    private var selectedCategory: Pair<String, String>? = null

    // Recycler adapter and list for entries
    private lateinit var categoriesAdapter: CategoriesAdapter
    private val categories = mutableListOf<Category>()

    // Variable to store image url
    private var uri : Uri? = null

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database : DatabaseReference
    private lateinit var taskImage: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_timesheet)

        // Initialize the views
        taskName = findViewById(R.id.task_title)
        taskLocation = findViewById(R.id.job_location)
        hourlyRate = findViewById(R.id.hourly_rate)
        startTime = findViewById(R.id.startTime)
        endTime = findViewById(R.id.endTime)
        startDate = findViewById(R.id.startDate)
        endDate = findViewById(R.id.endDate)
        recyclerList = findViewById(R.id.category_list)
        taskNotes = findViewById(R.id.notes)
        photoUpload = findViewById(R.id.photo_preview)
        uploadPhoto = findViewById(R.id.add_photo)
        saveInfo = findViewById(R.id.save_button)

        homeScreen = findViewById(R.id.homePg)
        catScreen = findViewById(R.id.catPg)
        goalScreen = findViewById(R.id.goalPg)
        timesheetScreen = findViewById(R.id.timesheetPg)

        // Initialising Firebase auth object
        auth = FirebaseAuth.getInstance()

        // Storage table for images
        taskImage = FirebaseStorage.getInstance().getReference("Images")


        // Initialize the categories list
        categories.clear()

        // Initialize the CategoriesAdapter
        categoriesAdapter = CategoriesAdapter(categories, object : CategoriesAdapter.OnCategoryClickListener {
            override fun onCategoryClick(categoryId: String, categoryName: String) {
                selectedCategory = Pair(categoryId, categoryName)
            }
        }, ::onCategoryLongClick)


        // Set the adapter to the RecyclerView
        recyclerList.adapter = categoriesAdapter
        recyclerList.layoutManager = LinearLayoutManager(this)


        // Fetch the categories
        fetchCategories()


        // onClickListeners for start and end time
        startTime.setOnClickListener { showTimePickerDialog(startTime) }
        endTime.setOnClickListener { showTimePickerDialog(endTime) }

        startDate.setOnClickListener{showDatePickerDialog(startDate)}
        endDate.setOnClickListener{showDatePickerDialog(endDate)}

        // Allow user to pick image from gallery/wherever they saved the image to
        val pickImg = registerForActivityResult(ActivityResultContracts.GetContent()){
            photoUpload.setImageURI(it)
            if(it != null){
                uri = it
            }
        }

        uploadPhoto.setOnClickListener{
            pickImg.launch("image/*")

        }

        saveInfo.setOnClickListener{
            saveData()
        }

        // Makeshift nav bar --
        goalScreen.setOnClickListener {
            val intent = Intent(this, SetGoalsActivity::class.java)
            startActivity(intent)
            finish()
        }

        homeScreen.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        catScreen.setOnClickListener {
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
            finish()
        }

        timesheetScreen.setOnClickListener {
            val intent = Intent(this, TimesheetActivity::class.java)
            startActivity(intent)
            finish()
        }
        // -- Makeshift nav bar
    }

    private fun onCategoryLongClick(categoryId: String) {
        // No long click functionality required here specifically, in this current build
    }

    // Function to fetch categories from database
    private fun fetchCategories() {
        val currentUserId = auth.currentUser?.uid ?: return

        database = FirebaseDatabase.getInstance().getReference("Category")
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
                    fetchCategories() // Retry the data fetch
                }
            }
        })
    }

    private fun showError(message: String, retry: () -> Unit) {
        Snackbar.make(recyclerList, message, Snackbar.LENGTH_LONG)
            .setAction("Retry") { retry.invoke() }
            .show()
    }

    // Function to allow users to create a timesheet entry
    private fun saveData() {
        database = FirebaseDatabase.getInstance().getReference("Timesheet")

        // Get info
        val workTask = taskName.text.toString()
        val location = taskLocation.text.toString()
        val billing = hourlyRate.text.toString()
        val timeStart = startTime.text.toString()
        val timeEnd = endTime.text.toString()
        val dateStart = startDate.text.toString()
        val dateEnd = endDate.text.toString()
        val notes = taskNotes.text.toString()
        val userId = auth.currentUser?.uid
        val userEmail = auth.currentUser?.email
        val isCompleted: Boolean = false
        val displayAsCompleted: Boolean = false


        if (workTask.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please enter a Task Title and Location", Toast.LENGTH_SHORT).show()
            taskName.text.clear()
            taskLocation.text.clear()
        } else if (startTime.text.equals("Set Start Time") || endTime.text.equals("Set End Time")) {
            Toast.makeText(this, "Please pick Start and End time", Toast.LENGTH_SHORT).show()
        } else if (startDate.text.equals("Set Start Date") || endTime.text.equals("Set End Date")) {
            Toast.makeText(this, "Please pick Start and End time", Toast.LENGTH_SHORT).show()
        } else {
            val timesheetId = database.push().key!!
            val timesheetInfo = TimesheetEntry(
                timesheetId,
                userId,
                workTask,
                location,
                billing,
                selectedCategory?.second,
                timeStart,
                timeEnd,
                dateStart,
                dateEnd,
                notes,
                null, // photoUpload null so user can create timesheet that does not include a photo
                userEmail,
                isCompleted,
                displayAsCompleted
            )

            // Get the current categoryHours and update it
            database.child("Category").child(selectedCategory?.first ?: "").child("categoryHours")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val currentCategoryHours = snapshot.getValue(String::class.java) ?: "0"
                        updateCategoryHours(timeStart, timeEnd, selectedCategory?.first)

                        // After updating categoryHours, set the TimesheetEntry value
                        database.child(timesheetId).setValue(timesheetInfo)
                            .addOnSuccessListener {
                                // If a photo URI is provided, upload the photo and update the database entry
                                uri?.let { photoUri ->
                                    uploadPhotoAndUpdateEntry(timesheetId, photoUri, timesheetInfo)
                                } ?: run {
                                    // No photo URI provided, just show a success message
                                    Toast.makeText(this@TimesheetActivity, "Timesheet Created", Toast.LENGTH_LONG).show()
                                    clearFields()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@TimesheetActivity, "Timesheet Not Created: ${it.message}", Toast.LENGTH_SHORT).show()
                                clearFields()
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                        fetchError()
                    }
                })
        }

    }

    // Database error
    private fun fetchError(){
        Toast.makeText(this, "Could not connect to database", Toast.LENGTH_LONG).show()
    }

    // If user added a photo, add it to the timesheet
    private fun uploadPhotoAndUpdateEntry(timesheetId: String, photoUri: Uri, timesheetInfo: TimesheetEntry) {
        taskImage.child(timesheetId).putFile(photoUri)
            .addOnSuccessListener { task ->
                task.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { url ->
                        val imgLink = url.toString()
                        val updatedTimesheetInfo = timesheetInfo.copy(photoUpload = imgLink)
                        database.child(timesheetId).setValue(updatedTimesheetInfo)
                            .addOnSuccessListener {
                                Toast.makeText(this@TimesheetActivity, "Timesheet Created", Toast.LENGTH_LONG).show()
                                clearFields()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@TimesheetActivity, "Failed to update Timesheet with photo: ${it.message}", Toast.LENGTH_SHORT).show()
                                clearFields()
                            }
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this@TimesheetActivity, "Failed to upload photo: ${it.message}", Toast.LENGTH_SHORT).show()
                clearFields()
            }
    }

    private fun clearFields() {
        taskName.text.clear()
        taskLocation.text.clear()
        hourlyRate.text.clear()
        categories.clear()
        fetchCategories()
    }


    // Function to dynamically update the hours for a category, when a user tags it to their timesheet entry

    private fun updateCategoryHours(
        startTime: String,
        endTime: String,
        categoryId: String?
    ) {
        val startTimeMillis = convertTimeToMillis(startTime)
        val endTimeMillis = convertTimeToMillis(endTime)

        // Calculate the difference in hours
        val hoursDifference = (endTimeMillis - startTimeMillis).toDouble() / (1000 * 60 * 60)

        if (hoursDifference <= 0) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show()
            return
        }

        if (categoryId != null) {
            val database = FirebaseDatabase.getInstance().getReference("Category")
            val categoryRef = database.child(categoryId)

            // Retrieve the current hours from the database
            categoryRef.child("categoryHours").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentHours = snapshot.getValue(String::class.java)?.toDoubleOrNull() ?: 0.0
                    val newCategoryHours = currentHours + hoursDifference

                    // Update the hours in the database
                    categoryRef.child("categoryHours").setValue(newCategoryHours.toString())
                        .addOnSuccessListener {
                            Toast.makeText(this@TimesheetActivity, "Hours Updated", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@TimesheetActivity, "Update Unsuccessful", Toast.LENGTH_SHORT).show()
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@TimesheetActivity, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Invalid category ID", Toast.LENGTH_SHORT).show()
        }
    }



    private fun convertTimeToMillis(timeString: String): Long {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = format.parse(timeString) ?: Date()
        return date.time
    }



// Function to allow users to pick times
    private fun showTimePickerDialog(textView: TextView) {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

    val timePickerDialog = TimePickerDialog(
        this,
        TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
            textView.text = formattedTime
        },
        hour,
        minute,
        false
    )
    timePickerDialog.show()

    }

    // Function to allow users to pick dates
    private fun showDatePickerDialog(textView: TextView) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val formattedDate = String.format(Locale.getDefault(), "%02d-%02d-%d", dayOfMonth, monthOfYear + 1, year)
                textView.text = formattedDate
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

}