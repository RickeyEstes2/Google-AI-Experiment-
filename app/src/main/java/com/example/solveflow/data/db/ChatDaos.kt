package com.example.solveflow.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat_sessions ORDER BY lastUpdatedAt DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): ChatSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    @Update
    suspend fun updateSession(session: ChatSessionEntity)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): ChatMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Update
    suspend fun updateMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)
}

@Dao
interface LogDao {
    @Query("SELECT * FROM saved_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SavedLogEntity>>

    @Query("SELECT * FROM saved_logs WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: String): SavedLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SavedLogEntity)

    @Query("DELETE FROM saved_logs WHERE id = :id")
    suspend fun deleteLog(id: String)
}

@Dao
interface PdfDao {
    @Query("SELECT * FROM saved_pdf_docs ORDER BY timestamp DESC")
    fun getAllPdfs(): Flow<List<SavedPdfEntity>>

    @Query("SELECT * FROM saved_pdf_docs WHERE id = :id LIMIT 1")
    suspend fun getPdfById(id: String): SavedPdfEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPdf(pdf: SavedPdfEntity)

    @Query("DELETE FROM saved_pdf_docs WHERE id = :id")
    suspend fun deletePdf(id: String)
}

@Dao
interface MathDao {
    @Query("SELECT * FROM saved_math_problems ORDER BY timestamp DESC")
    fun getAllMathProblems(): Flow<List<SavedMathEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMathProblem(problem: SavedMathEntity)

    @Query("DELETE FROM saved_math_problems WHERE id = :id")
    suspend fun deleteMathProblem(id: String)
}
