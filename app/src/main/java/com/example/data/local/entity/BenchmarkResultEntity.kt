package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "benchmark_results")
data class BenchmarkResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val modelName: String,
    val profileName: String,
    val threadCount: Int,
    val tokenCount: Int,
    val durationMs: Long,
    val tokensPerSec: Float,
    val startTempCelsius: Float,
    val endTempCelsius: Float,
    val estimatedEnergyMah: Double,
    val efficiencyScore: Int // 0 to 100
)
