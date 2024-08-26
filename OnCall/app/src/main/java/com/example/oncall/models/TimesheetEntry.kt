package com.example.oncall.models

import android.net.Uri

// Data class for timesheet entries

data class TimesheetEntry(
    val id: String? = null,
    val firebaseID : String? = null,
    val taskName: String? = null,
    val taskLocation: String? = null,
    val hourlyRate: String? = null,
    val selectedCategory : String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val taskNotes: String? = null,
    val photoUpload: String? = null,
    val user: String? = null,
    val isCompleted: Boolean = false,
    val displayAsCompleted: Boolean = false
)
