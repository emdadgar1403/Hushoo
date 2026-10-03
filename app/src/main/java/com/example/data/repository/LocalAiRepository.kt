package com.example.data.repository

import com.example.data.battery.BatteryThermalGovernor
import com.example.data.engine.OnDeviceNeuralEngine
import com.example.data.engine.RedactionResult
import com.example.data.engine.TextAnalysisResult
import com.example.data.engine.VisionTensorResult
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BenchmarkResultEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.model.AiModelInfo
import com.example.data.model.BatteryTelemetry
import com.example.data.model.HardwareDelegate
import com.example.data.model.InferenceMetrics
import com.example.data.model.PowerProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class LocalAiRepository(
    private val database: AppDatabase,
    private val batteryGovernor: BatteryThermalGovernor,
    val neuralEngine: OnDeviceNeuralEngine
) {
    val telemetry: StateFlow<BatteryTelemetry> = batteryGovernor.telemetry
    val thermalWarning: StateFlow<Boolean> = batteryGovernor.thermalWarning

    fun getChatMessages(sessionId: String): Flow<List<ChatMessageEntity>> =
        database.chatDao().getMessagesForSession(sessionId)

    fun getAllBenchmarks(): Flow<List<BenchmarkResultEntity>> =
        database.benchmarkDao().getAllBenchmarks()

    suspend fun saveMessage(message: ChatMessageEntity): Long =
        database.chatDao().insertMessage(message)

    suspend fun saveBenchmark(result: BenchmarkResultEntity): Long =
        database.benchmarkDao().insertBenchmark(result)

    suspend fun clearChatHistory() =
        database.chatDao().deleteAllMessages()

    suspend fun clearBenchmarks() =
        database.benchmarkDao().deleteAllBenchmarks()

    fun selectModel(modelId: String) = neuralEngine.selectModel(modelId)

    fun getActiveModel(): AiModelInfo = neuralEngine.getActiveModel()

    fun configureRuntime(profile: PowerProfile, delegate: HardwareDelegate, threads: Int) {
        neuralEngine.configureRuntime(profile, delegate, threads)
    }

    fun generateStreamingResponse(
        prompt: String,
        temperature: Float = 0.7f,
        onMetricsUpdate: (InferenceMetrics) -> Unit
    ): Flow<String> = neuralEngine.generateStreamingResponse(prompt, temperature, onMetricsUpdate)

    suspend fun analyzeText(text: String): TextAnalysisResult =
        neuralEngine.analyzeTextOffline(text)

    fun scrubSensitiveData(rawText: String): RedactionResult =
        neuralEngine.scrubSensitiveDataLocally(rawText)

    suspend fun analyzeBitmap(bitmap: android.graphics.Bitmap): VisionTensorResult =
        neuralEngine.analyzeBitmapOffline(bitmap)

    fun estimateEnergy(tokens: Int, durationMs: Long, profile: PowerProfile, threads: Int): Double =
        batteryGovernor.estimateInferenceEnergyMah(tokens, durationMs, profile, threads)

    fun predictBatteryHours(levelPercent: Int, profile: PowerProfile): Double =
        batteryGovernor.predictRemainingHours(levelPercent, profile)
}
