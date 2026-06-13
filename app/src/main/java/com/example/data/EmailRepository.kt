package com.example.data

import kotlinx.coroutines.flow.Flow

class EmailRepository(private val emailDao: EmailDao) {
    val allEmails: Flow<List<GeneratedEmail>> = emailDao.getAllEmails()

    fun searchEmails(query: String): Flow<List<GeneratedEmail>> {
        return emailDao.searchEmails(query)
    }

    suspend fun insertEmail(email: GeneratedEmail) {
        emailDao.insertEmail(email)
    }

    suspend fun deleteEmailById(id: Int) {
        emailDao.deleteEmailById(id)
    }

    suspend fun deleteAll() {
        emailDao.deleteAll()
    }
}
