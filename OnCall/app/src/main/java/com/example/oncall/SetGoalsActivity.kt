package com.example.oncall

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.oncall.adapter.MatchedEntryAdapter
import com.example.oncall.models.HourGoals
import com.example.oncall.models.MatchedEntry
import com.example.oncall.models.TimesheetEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Certain sections of code in this class have been adapted from this source:
// Author: Mkr Developer
// Source: https://www.youtube.com/watch?v=KiJy5Oi4rRo&list=PLEGrY4uRTu5ls7Mq7h6RcdKGFdQVqy0KZ

class SetGoalsActivity : AppCompatActivity(), MatchedEntryAdapter.OnItemClickListener {
    // Declare variables
    private lateinit var checkMon : CheckBox
    private lateinit var checkTue : CheckBox
    private lateinit var checkWed : CheckBox
    private lateinit var checkThurs : CheckBox
    private lateinit var checkFri : CheckBox
    private lateinit var checkSat : CheckBox
    private lateinit var checkSun : CheckBox
    private lateinit var hoursMin : EditText
    private lateinit var hoursMax : EditText
    private lateinit var hoursSave : Button

    private lateinit var viewHome : ImageButton
    private lateinit var viewCat : ImageButton
    private lateinit var viewGoals : ImageButton
    private lateinit var viewTimesheet : ImageButton

    private lateinit var matchedEntriesRecyclerView: RecyclerView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_set_goals)

        // Initialize the views
        checkMon = findViewById(R.id.monday_checkbox)
        checkTue = findViewById(R.id.tuesday_checkbox)
        checkWed = findViewById(R.id.wednesday_checkbox)
        checkThurs = findViewById(R.id.thursday_checkbox)
        checkFri = findViewById(R.id.friday_checkbox)
        checkSat = findViewById(R.id.saturday_checkbox)
        checkSun = findViewById(R.id.sunday_checkbox)
        hoursMin = findViewById(R.id.goalMin)
        hoursMax = findViewById(R.id.goalMax)
        hoursSave = findViewById(R.id.saveHours)

        viewHome = findViewById(R.id.homeNav)
        viewCat = findViewById(R.id.catNav)
        viewGoals = findViewById(R.id.goalsNav)
        viewTimesheet = findViewById(R.id.timesheetNav)


        // Initialize the RecyclerView
        matchedEntriesRecyclerView = findViewById(R.id.matched_entries_recycler_view)

        // Get the list of MatchedEntry objects from Firebase
        val matchedEntries: List<MatchedEntry> = getMatchedEntriesFromDataSource()

        // Create an instance of the adapter
        val adapter = MatchedEntryAdapter(matchedEntries)
        adapter.setOnItemClickListener(this)

        // Set the adapter on the RecyclerView
        matchedEntriesRecyclerView.adapter = adapter

        // Set a layout manager
        matchedEntriesRecyclerView.layoutManager = LinearLayoutManager(this)


        // Initialising Firebase auth object
        auth = FirebaseAuth.getInstance()
        // Create table for goals set
        database = FirebaseDatabase.getInstance().getReference("WorkGoals")

        hoursSave.setOnClickListener{
            dailyWorkGoal()
        }

        // Makeshift nav bar --
        viewGoals.setOnClickListener {
            val intent = Intent(this, SetGoalsActivity::class.java)
            startActivity(intent)
            finish()
        }

        viewHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        viewCat.setOnClickListener {
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
            finish()
        }

        viewTimesheet.setOnClickListener {
            val intent = Intent(this, TimesheetActivity::class.java)
            startActivity(intent)
            finish()
        }
        // -- Makeshift nav bar


    }

    // Function to create goals
    private fun dailyWorkGoal(){
        // Get the current user ID
        val userId = auth.currentUser?.uid

        // Get the selected days of the week
        val daysOfWeek = getDaysOfWeek()

        // Get the minimum and maximum work hours
        val minHours = hoursMin.text.toString().trim()
        val maxHours = hoursMax.text.toString().trim()

        // Unique ID for each goal created
        val hoursId = database.push().key!!
        val hourDetails = HourGoals(hoursId, userId, daysOfWeek, minHours, maxHours)

        database.child(hoursId).setValue(hourDetails)
            .addOnCompleteListener{
                Toast.makeText(this, "Daily Goal Hours Saved", Toast.LENGTH_LONG).show()
                hoursMin.text.clear()
                hoursMax.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Hours not saved ${it.message} ", Toast.LENGTH_SHORT).show()
                hoursMin.text.clear()
                hoursMax.text.clear()
            }


    }

    // Helper function to get the day of the week based on the calender
    private fun getDaysOfWeek(): Map<String, String> {
        val daysOfWeek = mutableMapOf<String, String>()
        val calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)

        if (checkMon.isChecked) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            val mondayDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)
            daysOfWeek["Monday"] = mondayDate
        }

        if (checkTue.isChecked) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
            val tuesdayDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)
            daysOfWeek["Tuesday"] = tuesdayDate
        }

        if (checkWed.isChecked) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
            val wednesdayDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)
            daysOfWeek["Wednesday"] = wednesdayDate
        }

        if (checkThurs.isChecked) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
            val thursdayDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)
            daysOfWeek["Thursday"] = thursdayDate
        }

        if (checkFri.isChecked) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
            val fridayDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)
            daysOfWeek["Friday"] = fridayDate
        }

        if (checkSat.isChecked) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
            val saturdayDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)
            daysOfWeek["Saturday"] = saturdayDate
        }

        if (checkSun.isChecked) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            val sundayDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)
            daysOfWeek["Sunday"] = sundayDate
        }

        return daysOfWeek
    }

    // Function to delete set goals from database
    override fun onItemClick(matchedEntry: MatchedEntry) {
        val workGoalsRef = FirebaseDatabase.getInstance().getReference("WorkGoals")
        val currentUserId = auth.currentUser?.uid ?: return

        workGoalsRef.orderByChild("firebaseID").equalTo(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (childSnapshot in dataSnapshot.children) {
                        val hourGoals = childSnapshot.getValue(HourGoals::class.java)
                        if (hourGoals?.dayOfWeek?.containsValue(matchedEntry.date) == true) {
                            childSnapshot.ref.removeValue()
                                .addOnSuccessListener {
                                    refreshAdapter() // Call the refreshAdapter method after successful deletion
                                }
                                .addOnFailureListener { exception ->
                                    showErrorMessage("Failed to delete entry: ${exception.message}")
                                }
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showErrorMessage("Failed to fetch data: ${error.message}")
                }
            })
    }

    // Function to handle errors caused by deleting entries
    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Helper Function to refresh the view after an entry is deleted
    private fun refreshAdapter() {
        val matchedEntries: List<MatchedEntry> = getMatchedEntriesFromDataSource()
        val adapter = matchedEntriesRecyclerView.adapter as MatchedEntryAdapter
        adapter.updateData(matchedEntries)
    }


    // Recycler view for work goals
    private fun getMatchedEntriesFromDataSource(): List<MatchedEntry> {
        val matchedEntries = mutableListOf<MatchedEntry>()

        // Fetch data from Firebase
        val workGoalsRef = FirebaseDatabase.getInstance().getReference("WorkGoals")
        val timesheetRef = FirebaseDatabase.getInstance().getReference("Timesheet")

        // Fetch the WorkGoals data
        workGoalsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val workGoalsData = dataSnapshot.children.mapNotNull { it.getValue(HourGoals::class.java) }
                // Process the WorkGoals data
                processWorkGoalsData(workGoalsData)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any error
               fetchError()
            }
        })

        // Get the Timesheet data
        timesheetRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val timesheetData = dataSnapshot.children.mapNotNull { it.getValue(TimesheetEntry::class.java) }
                // Process the Timesheet data
                processTimesheetData(timesheetData)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
                fetchError()
            }
        })

        return matchedEntries
    }

    // Function to calculate the ACTUAL hours based on the timesheet entry dates
    private fun calculateHoursWorked(startTime: String?, endTime: String?): Double {
        if (startTime == null || endTime == null) {
            return 0.0 // Return 0.0 if startTime or endTime is null
        }

        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val startTimeObj = timeFormatter.parse(startTime)
        val endTimeObj = timeFormatter.parse(endTime)

        if (startTimeObj == null || endTimeObj == null) {
            return 0.0
        }

        // Calculate the hours
        val diffInMillis = endTimeObj.time - startTimeObj.time
        val diffInHours = diffInMillis / (1000.0 * 60.0 * 60.0)

        return diffInHours
    }


    // Function to check the Firebase timesheet entries and match them based on date
    private fun processWorkGoalsData(workGoalsData: List<HourGoals>) {
        val currentUserId = auth.currentUser?.uid ?: return
        val matchedEntries = mutableMapOf<String, MatchedEntry>() // List for the returned data

        workGoalsData.forEach { workGoal ->
            if (workGoal.firebaseID == currentUserId) {
                workGoal.dayOfWeek?.forEach { (day, date) ->
                    val matchedTimesheetEntries = timesheetData.filter { timesheetEntry ->
                        timesheetEntry.firebaseID == currentUserId && timesheetEntry.startDate == date
                    }

                    var totalHoursWorked = 0.0

                    matchedTimesheetEntries.forEach { timesheetEntry ->
                        val hoursWorked =
                            timesheetEntry.startTime?.let { timesheetEntry.endTime?.let { it1 ->
                                calculateHoursWorked(it,
                                    it1
                                )
                            } }
                        if (hoursWorked != null) {
                            totalHoursWorked += hoursWorked
                        }
                        //totalHoursWorked += hoursWorked
                    }

                    val existingEntry = matchedEntries[date]
                    if (existingEntry == null) {
                        val newEntry = MatchedEntry(date, workGoal.workMin ?: "", workGoal.workMax ?: "", totalHoursWorked)
                        matchedEntries[date] = newEntry
                    } else {
                        existingEntry.hoursWorked = totalHoursWorked
                    }
                }
            }
        }

        // Update the RecyclerView adapter with the new matched entries
        val adapter = matchedEntriesRecyclerView.adapter as MatchedEntryAdapter
        adapter.updateData(matchedEntries.values.toList())
    }

    private fun fetchError(){
        Toast.makeText(this, "No Entries to match", Toast.LENGTH_LONG).show()
    }

    private fun processTimesheetData(timesheetData: List<TimesheetEntry>) {
        this.timesheetData = timesheetData
    }

    private var timesheetData: List<TimesheetEntry> = emptyList()



}