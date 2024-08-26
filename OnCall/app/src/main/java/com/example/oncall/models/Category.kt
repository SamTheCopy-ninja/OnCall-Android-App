package com.example.oncall.models

// Data class for categories

data class Category(
    val id : String? = null,
    val firebaseID : String? = null,
    var categoryName : String? = null,
    val categoryHours : String? = null,
    var isSelected: Boolean = false
)
