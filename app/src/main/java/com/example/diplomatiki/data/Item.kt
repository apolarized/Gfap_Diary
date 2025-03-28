package com.example.diplomatiki.data

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Entity data class represents a single row in the database.
 */
@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val gfapval: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val comment: String = ""
){
    fun formatedPrice(): String {
        // value formatting logic
        return String.format("%.2f", gfapval)
    }

    fun formattedTimestamp(): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    fun hasComment(): Boolean {
        return comment.isNotBlank()
    }
}