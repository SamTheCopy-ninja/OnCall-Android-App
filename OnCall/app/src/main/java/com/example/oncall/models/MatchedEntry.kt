package com.example.oncall.models

// Data class for matched entries

data class MatchedEntry(
    val date: String? = null,
    val minHoursSet: String? = null,
    val maxHoursSet: String? = null,
    var hoursWorked: Double? = null
)
