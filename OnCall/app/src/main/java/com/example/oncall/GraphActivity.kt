package com.example.oncall

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.oncall.models.HourGoals
import com.example.oncall.models.TimesheetEntry
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.sql.Time
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GraphActivity : AppCompatActivity() {

    // Declare variables
    private lateinit var combinedChart: BarChart
    private lateinit var doubleChart: BarChart
    private lateinit var graphStart: EditText
    private lateinit var graphEnd: EditText
    private lateinit var filterGraph: Button
    private lateinit var goHome: Button
    private val timesheetDataByDate = mutableMapOf<String, Double>()

    // Firebase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_graph)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()


        // Initialize views
        combinedChart = findViewById(R.id.combinedChart)
        doubleChart = findViewById(R.id.doubleChart)
        graphStart = findViewById(R.id.graph_start_date)
        graphEnd = findViewById(R.id.graph_end_date)
        filterGraph = findViewById(R.id.filter_graph)
        goHome = findViewById(R.id.back)

        goHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }


        filterGraph.setOnClickListener {
            filterEntries()
        }

        // Fetch entries from Firebase database
        retrieveTimesheetData()

    }

    // Function to iterate through the Timesheet entries
    // then calculate the TOTAL HOURS worked for each date that has entries
    // This is displayed on the single bar chart
    private fun retrieveTimesheetData() {
        val userId = auth.currentUser?.email
        val databaseRef = FirebaseDatabase.getInstance().getReference("Timesheet")
        val query = databaseRef.orderByChild("user").equalTo(userId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (timesheetSnapshot in snapshot.children) {
                    val timesheetEntry = timesheetSnapshot.getValue(TimesheetEntry::class.java)
                    timesheetEntry?.let {
                        val startTime = it.startTime?.let { it1 -> parseTime(it1) }
                        val endTime = it.endTime?.let { it1 -> parseTime(it1) }
                        val totalHours = calculateTotalHours(startTime, endTime)
                        val date = it.startDate

                        if (date != null) {
                            val existingHours = timesheetDataByDate[date] ?: 0.0
                            timesheetDataByDate[date] = existingHours + totalHours
                        }
                    }
                }

                // Use the time data to populate the bar chart
                populateBarChart(timesheetDataByDate)
            }

            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(this@GraphActivity, "Error retrieving data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Retrieve WorkGoals data to display the MINIMUM and MAXIMUM hours set by the user
        // This is displayed on the double graph
        val userStats = auth.currentUser?.uid
        val workGoalsRef = FirebaseDatabase.getInstance().getReference("WorkGoals")
        workGoalsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allHourGoals = mutableListOf<HourGoals>()
                for (userSnapshot in snapshot.children) {
                    val hourGoals = userSnapshot.getValue(HourGoals::class.java)
                    if (hourGoals?.firebaseID == userStats) {
                        if (hourGoals != null) {
                            allHourGoals.add(hourGoals)
                        }
                    }
                }
                populateDoubleBarChart(allHourGoals)
            }

            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(this@GraphActivity, "Error retrieving WorkGoals data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }

    // Function to parse the times for each Timesheet entry in the database
    // so the data can be user to calculate the hours for the graphs
    private fun parseTime(timeString: String): Time? {
        return try {
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            val time = formatter.parse(timeString)
            Time(time.hours, time.minutes, 0)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    // Helper Function to calculate hours
    private fun calculateTotalHours(startTime: Time?, endTime: Time?): Double {
        if (startTime == null || endTime == null) {
            return 0.0
        }

        val startCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startTime.hours)
            set(Calendar.MINUTE, startTime.minutes)
            set(Calendar.SECOND, startTime.seconds)
        }

        val endCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, endTime.hours)
            set(Calendar.MINUTE, endTime.minutes)
            set(Calendar.SECOND, endTime.seconds)
        }

        val startMillis = startCalendar.timeInMillis
        val endMillis = endCalendar.timeInMillis

        val totalMillis = endMillis - startMillis
        return totalMillis / (1000.0 * 60 * 60) // Convert to hours
    }

    // Function to add the processed data to the charts
    private fun populateBarChart(
        timesheetDataByDate: Map<String, Double>
    ) {
        runOnUiThread {
            // Set up the bar chart
            val barChart = findViewById<BarChart>(R.id.combinedChart)

            // Description label
            barChart.description.text = "Timesheet Hours Worked"
            barChart.description.textSize = 14f
            barChart.description.textColor = Color.BLACK
            barChart.description.setPosition(barChart.width - 10f, 35f)


            // Set up the X-axis
            val xAxisLabels = ArrayList(timesheetDataByDate.keys)
            val xAxis = barChart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f

            // Set up the Y-axis
            val yAxisLeft = barChart.axisLeft
            yAxisLeft.granularity = 1f
            val yAxisRight = barChart.axisRight
            yAxisRight.isEnabled = false // Disable the right Y-axis

            // Add data to the chart
            val barEntries = ArrayList<BarEntry>()
            for ((index, date) in timesheetDataByDate.keys.withIndex()) {
                val totalHours = timesheetDataByDate[date] ?: 0.0
                barEntries.add(BarEntry(index.toFloat(), totalHours.toFloat()))
            }

            val barDataSet = BarDataSet(barEntries, "Total Hours Per Day")
            barDataSet.setDrawValues(true)
            barDataSet.valueTextSize = 12f
            barDataSet.valueTextColor = Color.BLACK


            val barData = BarData(barDataSet)
            barData.barWidth = 0.3f


            barChart.data = barData

            // Refresh the chart
            barChart.invalidate()
        }
    }

    // Function to filter graph based on user dates
    private fun filterEntries() {

        // Get the dates entered by the user
        val startDateString = graphStart.text.toString()
        val endDateString = graphEnd.text.toString()

        val startDate = parseDate(startDateString)
        val endDate = parseDate(endDateString)

        if (startDate != null && endDate != null) {
        // Filter the list
            val filteredData = timesheetDataByDate.filterKeys { date ->
                val currentDate = parseDate(date)
                currentDate!! in startDate..endDate
            }

            // Re-populate the chart
            populateBarChart(filteredData)
        } else {

            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper function to parse the dates for entries
    private fun parseDate(dateString: String): Date? {
        return try {
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateString)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    // Function to populate double bar chart for Min and Max goals
    private fun populateDoubleBarChart(allHourGoals: List<HourGoals>) {
        runOnUiThread {
            val barChart = findViewById<BarChart>(R.id.doubleChart)

            barChart.description.text = "Minimum/Maximum Hours Set"
            barChart.description.textSize = 14f
            barChart.description.textColor = Color.BLACK
            barChart.description.setPosition(barChart.width - 10f, 35f)

            // Set up the X-axis labels with day and date pair
            val xAxisLabels = allHourGoals.flatMap { it.dayOfWeek?.entries ?: emptySet() }
                .map { "${it.key}: ${it.value}" }
                .distinct()

            val xAxis = barChart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f

            // Set up the Y-axis
            val yAxisLeft = barChart.axisLeft
            yAxisLeft.granularity = 1f
            val yAxisRight = barChart.axisRight
            yAxisRight.isEnabled = false

            // Add data to the chart
            val minBarEntries = ArrayList<BarEntry>()
            val maxBarEntries = ArrayList<BarEntry>()
            for ((index, dayAndDate) in xAxisLabels.withIndex()) {
                var minSum = 0f
                var maxSum = 0f
                var count = 0

                for (hourGoals in allHourGoals) {
                    val dayOfWeek = hourGoals.dayOfWeek?.entries?.find { "${it.key}: ${it.value}" == dayAndDate }
                    if (dayOfWeek != null) {
                        minSum += hourGoals.workMin?.toFloatOrNull() ?: 0f
                        maxSum += hourGoals.workMax?.toFloatOrNull() ?: 0f
                        count++
                    }
                }

                val minValue = if (count > 0) minSum / count else 0f
                val maxValue = if (count > 0) maxSum / count else 0f
                minBarEntries.add(BarEntry(index.toFloat() - 0.2f, minValue))
                maxBarEntries.add(BarEntry(index.toFloat() + 0.2f, maxValue))
            }

            val minBarDataSet = BarDataSet(minBarEntries, "Minimum Hours")
            minBarDataSet.color = ContextCompat.getColor(this, R.color.indigo)
            minBarDataSet.setDrawValues(true)
            minBarDataSet.valueTextSize = 14f

            val maxBarDataSet = BarDataSet(maxBarEntries, "Maximum Hours")
            maxBarDataSet.color = ContextCompat.getColor(this, R.color.amber)
            maxBarDataSet.setDrawValues(true)
            maxBarDataSet.valueTextSize = 14f

            val barData = BarData(minBarDataSet, maxBarDataSet)
            barData.barWidth = 0.2f

            barChart.data = barData

            // Refresh the chart
            barChart.invalidate()
        }
    }

}