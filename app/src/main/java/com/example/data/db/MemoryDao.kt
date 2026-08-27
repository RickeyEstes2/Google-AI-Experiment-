package com.example.data.db

import androidx.room.*
import com.example.data.model.MemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {

    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    fun getAllMemoriesFlow(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE isStarred = 1 ORDER BY timestamp DESC")
    fun getStarredMemoriesFlow(): Flow<List<MemoryEntity>>

    @Query("""
        SELECT * FROM memories 
        WHERE text LIKE '%' || :query || '%' 
           OR title LIKE '%' || :query || '%' 
           OR appName LIKE '%' || :query || '%' 
           OR tags LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun searchMemoriesFlow(query: String): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE appName = :appName ORDER BY timestamp DESC")
    fun getMemoriesByAppFlow(appName: String): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE appCategory = :category ORDER BY timestamp DESC")
    fun getMemoriesByCategoryFlow(category: String): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE id = :id")
    suspend fun getMemoryById(id: Long): MemoryEntity?

    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    suspend fun getAllMemoriesList(): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE timestamp >= :fromTime AND timestamp <= :toTime ORDER BY timestamp DESC")
    suspend fun getMemoriesBetween(fromTime: Long, toTime: Long): List<MemoryEntity>

    @Query("SELECT DISTINCT appName FROM memories WHERE appName != '' ORDER BY appName ASC")
    fun getDistinctAppNamesFlow(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM memories")
    fun getMemoriesCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memories: List<MemoryEntity>)

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("DELETE FROM memories")
    suspend fun clearAllMemories()

    @Query("SELECT * FROM memories ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMemories(limit: Int): List<MemoryEntity>
}
