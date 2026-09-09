package com.gptbot.gptbot.data.repository

import com.gptbot.gptbot.data.db.*
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val chatDao: ChatDao,
    private val logDao: LogDao,
    private val pdfDao: PdfDao,
    private val mathDao: MathDao
) {
    val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()
    val allLogs: Flow<List<SavedLogEntity>> = logDao.getAllLogs()
    val allPdfs: Flow<List<SavedPdfEntity>> = pdfDao.getAllPdfs()
    val allMathProblems: Flow<List<SavedMathEntity>> = mathDao.getAllMathProblems()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun getSessionById(sessionId: String): ChatSessionEntity? =
        chatDao.getSessionById(sessionId)

    suspend fun saveSession(session: ChatSessionEntity) =
        chatDao.insertSession(session)

    suspend fun updateSession(session: ChatSessionEntity) =
        chatDao.updateSession(session)

    suspend fun deleteSession(sessionId: String) =
        chatDao.deleteSession(sessionId)

    suspend fun saveMessage(message: ChatMessageEntity) =
        chatDao.insertMessage(message)

    suspend fun updateMessage(message: ChatMessageEntity) =
        chatDao.updateMessage(message)

    suspend fun getMessageById(messageId: String): ChatMessageEntity? =
        chatDao.getMessageById(messageId)

    suspend fun deleteMessage(messageId: String) =
        chatDao.deleteMessage(messageId)

    // Logs
    suspend fun saveLog(log: SavedLogEntity) = logDao.insertLog(log)
    suspend fun getLogById(id: String): SavedLogEntity? = logDao.getLogById(id)
    suspend fun deleteLog(id: String) = logDao.deleteLog(id)

    // PDF
    suspend fun savePdf(pdf: SavedPdfEntity) = pdfDao.insertPdf(pdf)
    suspend fun getPdfById(id: String): SavedPdfEntity? = pdfDao.getPdfById(id)
    suspend fun deletePdf(id: String) = pdfDao.deletePdf(id)

    // Math
    suspend fun saveMathProblem(problem: SavedMathEntity) = mathDao.insertMathProblem(problem)
    suspend fun deleteMathProblem(id: String) = mathDao.deleteMathProblem(id)
}
