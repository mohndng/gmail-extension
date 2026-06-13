package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EmailDao {
    @Query("SELECT * FROM generated_emails ORDER BY timestamp DESC")
    fun getAllEmails(): Flow<List<GeneratedEmail>>

    @Query("SELECT * FROM generated_emails WHERE generatedEmail LIKE '%' || :query || '%' OR tag LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchEmails(query: String): Flow<List<GeneratedEmail>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmail(email: GeneratedEmail)

    @Query("DELETE FROM generated_emails WHERE id = :id")
    suspend fun deleteEmailById(id: Int)

    @Query("DELETE FROM generated_emails")
    suspend fun deleteAll()
}
