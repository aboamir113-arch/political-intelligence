package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "countries")
data class Country(
    @PrimaryKey
    val code: String, // e.g. "SA", "EG", "AE", "US", "IR"
    val nameAr: String,
    val nameEn: String,
    val region: String, // Middle East, North Africa, Gulf, North America, Europe, Asia
    val flagEmoji: String,
    val isStrategicFocus: Boolean = true
)
