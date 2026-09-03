package com.example.solveflow.data.db

import androidx.room.*
import com.example.solveflow.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LanguageDao {
    @Query("SELECT * FROM languages ORDER BY isCustom ASC, name ASC")
    fun getAllLanguages(): Flow<List<ProgrammingLanguage>>

    @Query("SELECT * FROM languages WHERE id = :id LIMIT 1")
    suspend fun getLanguageById(id: String): ProgrammingLanguage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguage(language: ProgrammingLanguage)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(languages: List<ProgrammingLanguage>)

    @Delete
    suspend fun deleteLanguage(language: ProgrammingLanguage)
}

@Dao
interface SnippetDao {
    @Query("SELECT * FROM code_snippets ORDER BY isFineTunedExemplar DESC, dateAdded DESC")
    fun getAllSnippets(): Flow<List<CodeSnippet>>

    @Query("SELECT * FROM code_snippets")
    suspend fun getAllSnippetsList(): List<CodeSnippet>

    @Query("SELECT * FROM code_snippets WHERE languageId = :languageId ORDER BY dateAdded DESC")
    fun getSnippetsByLanguage(languageId: String): Flow<List<CodeSnippet>>

    @Query("SELECT * FROM code_snippets WHERE id = :id LIMIT 1")
    suspend fun getSnippetById(id: Long): CodeSnippet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: CodeSnippet): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(snippets: List<CodeSnippet>)

    @Update
    suspend fun updateSnippet(snippet: CodeSnippet)

    @Delete
    suspend fun deleteSnippet(snippet: CodeSnippet)
}

@Dao
interface KnowledgeDao {
    @Query("SELECT * FROM knowledge_items ORDER BY dateAdded DESC")
    fun getAllKnowledge(): Flow<List<KnowledgeItem>>

    @Query("SELECT * FROM knowledge_items")
    suspend fun getAllKnowledgeList(): List<KnowledgeItem>

    @Query("SELECT * FROM knowledge_items WHERE languageScope = 'All' OR languageScope = :languageId")
    suspend fun getKnowledgeForLanguage(languageId: String): List<KnowledgeItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledge(item: KnowledgeItem): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<KnowledgeItem>)

    @Delete
    suspend fun deleteKnowledge(item: KnowledgeItem)
}

@Dao
interface GenerationRecordDao {
    @Query("SELECT * FROM generation_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<GenerationRecord>>

    @Query("SELECT * FROM generation_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): GenerationRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GenerationRecord): Long

    @Update
    suspend fun updateRecord(record: GenerationRecord)

    @Delete
    suspend fun deleteRecord(record: GenerationRecord)
}

@Dao
interface RlPolicyDao {
    @Query("SELECT * FROM rl_policy")
    fun getAllPolicyEntries(): Flow<List<RlPolicyEntry>>

    @Query("SELECT * FROM rl_policy WHERE stateKey = :stateKey")
    suspend fun getEntriesForState(stateKey: String): List<RlPolicyEntry>

    @Query("SELECT * FROM rl_policy WHERE stateKey = :stateKey AND strategyName = :strategyName LIMIT 1")
    suspend fun getEntry(stateKey: String, strategyName: String): RlPolicyEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: RlPolicyEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(entries: List<RlPolicyEntry>)

    @Query("DELETE FROM rl_policy")
    suspend fun clearPolicy()
}
