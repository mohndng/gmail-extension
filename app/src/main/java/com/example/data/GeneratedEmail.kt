package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generated_emails")
data class GeneratedEmail(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalEmail: String,
    val generatedEmail: String,
    val type: String, // "dot", "plus", "combined"
    val tag: String, // E.g., "GitHub Signup", "Stripe Checkout"
    val notes: String = "", // Additional details for QA reference
    val timestamp: Long = System.currentTimeMillis()
)
