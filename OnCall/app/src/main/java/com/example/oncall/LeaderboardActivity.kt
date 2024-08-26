package com.example.oncall

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.oncall.adapter.LeaderboardAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class LeaderboardActivity : AppCompatActivity() {

    // Declare variables
    private lateinit var leaderboardRecyclerView: RecyclerView
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private val leaderboardList = mutableListOf<Pair<String, Int>>()
    private lateinit var returnHomeBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_leaderboard)

        // Initialize the variables
        leaderboardRecyclerView = findViewById(R.id.leaderboard_recycler_view)
        returnHomeBtn = findViewById(R.id.backHomeBtn)
        leaderboardRecyclerView.layoutManager = LinearLayoutManager(this)
        leaderboardAdapter = LeaderboardAdapter(leaderboardList)
        leaderboardRecyclerView.adapter = leaderboardAdapter

        returnHomeBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        fetchLeaderboardData()

    }

    // Function to iterate through all the Timesheet entries
    // and count the number of "Completed" entries every user has
    private fun fetchLeaderboardData() {
        // Database reference for Timesheet entries
        val databaseRef = FirebaseDatabase.getInstance().reference.child("Timesheet")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                leaderboardList.clear()

                val userEmailCounts = mutableMapOf<String, Int>()

                for (entrySnapshot in dataSnapshot.children) {
                    val isCompleted = entrySnapshot.child("completed").value as? Boolean ?: false
                    val user = entrySnapshot.child("user").value as? String ?: continue

                    if (isCompleted) {
                        val currentCount = userEmailCounts[user] ?: 0
                        userEmailCounts[user] = currentCount + 1
                    }
                }
                // After all entries are counted, add them to the local list so the leaderboard has data
                userEmailCounts.forEach { (email, count) ->
                    leaderboardList.add(Pair(email, count))
                }

                // Sort the list
                leaderboardList.sortByDescending { it.second }
                leaderboardAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(this@LeaderboardActivity, "Failed to load leaderboard data. Please try again.", Toast.LENGTH_LONG).show()
            }
        })
    }

}