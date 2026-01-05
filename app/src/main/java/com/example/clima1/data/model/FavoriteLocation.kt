package com.example.clima1.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_locations")
data class FavoriteLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val timestamp: Long = System.currentTimeMillis()
)

