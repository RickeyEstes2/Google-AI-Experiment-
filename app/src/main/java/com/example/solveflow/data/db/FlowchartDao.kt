package com.example.solveflow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FlowchartDao {

    @Query("SELECT * FROM flowcharts ORDER BY updatedAt DESC")
    fun getAllFlowchartsFlow(): Flow<List<FlowchartEntity>>

    @Query("SELECT * FROM flowcharts WHERE id = :id LIMIT 1")
    suspend fun getFlowchartById(id: String): FlowchartEntity?

    @Query("SELECT COUNT(*) FROM flowcharts")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FlowchartEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<FlowchartEntity>)

    @Update
    suspend fun update(entity: FlowchartEntity)

    @Query("DELETE FROM flowcharts WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM flowcharts WHERE isTemplate = 0")
    suspend fun deleteAllCustom()

    // Diagnostic runs
    @Query("SELECT * FROM diagnostic_runs ORDER BY completedAt DESC")
    fun getAllDiagnosticRunsFlow(): Flow<List<DiagnosticRunEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: DiagnosticRunEntity)

    @Query("DELETE FROM diagnostic_runs WHERE id = :id")
    suspend fun deleteRunById(id: String)
}
