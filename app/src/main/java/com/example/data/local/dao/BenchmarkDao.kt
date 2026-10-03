package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.BenchmarkResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BenchmarkDao {
    @Query("SELECT * FROM benchmark_results ORDER BY timestamp DESC")
    fun getAllBenchmarks(): Flow<List<BenchmarkResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBenchmark(result: BenchmarkResultEntity): Long

    @Query("DELETE FROM benchmark_results")
    suspend fun deleteAllBenchmarks()
}
