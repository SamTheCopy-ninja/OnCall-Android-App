package com.example.oncall.models

// Data class for setting daily work goals

data class HourGoals(
    val id: String? = null,
    val firebaseID: String? = null,
    val dayOfWeek: Map<String, String>? = null,
    val workMin: String? = null,
    val workMax: String? = null
)
