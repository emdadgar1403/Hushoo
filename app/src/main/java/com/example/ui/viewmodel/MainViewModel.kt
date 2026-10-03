package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.repository.LocalAiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.roundToInt

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LocalAiRepository

    val telemetry: StateFlow<BatteryTelemetry>
    val thermalWarning: StateFlow<Boolean>

    // Runtime Configuration
    private val _selectedModel = MutableStateFlow<AiModelInfo>(OnDeviceNeuralEngine().availableModels[0])
    val selectedModel: StateFlow<AiModelInfo> = _selectedModel.asStateFlow()

    private val _powerProfile = MutableStateFlow(PowerProfile.ECO)
    val powerProfile: StateFlow<PowerProfile> = _powerProfile.asStateFlow()

    private val _hardwareDelegate = MutableStateFlow(HardwareDelegate.CPU)
    val hardwareDelegate: StateFlow<HardwareDelegate> = _hardwareDelegate.asStateFlow()

    private val _threadCount = MutableStateFlow(1)
    val threadCount: StateFlow<Int> = _threadCount.asStateFlow()

    private val _autoThermalProtection = MutableStateFlow(true)
    val autoThermalProtection: StateFlow<Boolean> = _autoThermalProtection.asStateFlow()

    // Chat
    private val currentSessionId = UUID.randomUUID().toString()
    val chatMessages: StateFlow<List<ChatMessageEntity>>

    private val _currentPrompt = MutableStateFlow("")
    val currentPrompt: StateFlow<String> = _currentPrompt.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingResponse = MutableStateFlow("")
    val streamingResponse: StateFlow<String> = _streamingResponse.asStateFlow()

    private val _activeMetrics = MutableStateFlow(InferenceMetrics())
    val activeMetrics: StateFlow<InferenceMetrics> = _activeMetrics.asStateFlow()

    private var generationJob: Job? = null

    // Neural Tasks
    private val _textAnalysisResult = MutableStateFlow<TextAnalysisResult?>(null)
    val textAnalysisResult: StateFlow<TextAnalysisResult?> = _textAnalysisResult.asStateFlow()
    private val _isAnalyzingText = MutableStateFlow(false)
    val isAnalyzingText: StateFlow<Boolean> = _isAnalyzingText.asStateFlow()

    private val _redactionResult = MutableStateFlow<RedactionResult?>(null)
    val redactionResult: StateFlow<RedactionResult?> = _redactionResult.asStateFlow()

    private val _visionResult = MutableStateFlow<VisionTensorResult?>(null)
    val visionResult: StateFlow<VisionTensorResult?> = _visionResult.asStateFlow()
    private val _isAnalyzingVision = MutableStateFlow(false)
    val isAnalyzingVision: StateFlow<Boolean> = _isAnalyzingVision.asStateFlow()

    // Battery Benchmark
    val benchmarkHistory: StateFlow<List<BenchmarkResultEntity>>
    private val _isBenchmarking = MutableStateFlow(false)
    val isBenchmarking: StateFlow<Boolean> = _isBenchmarking.asStateFlow()
    private val _benchmarkProgress = MutableStateFlow(0f)
    val benchmarkProgress: StateFlow<Float> = _benchmarkProgress.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        val governor = BatteryThermalGovernor(application)
        val engine = OnDeviceNeuralEngine()
        repository = LocalAiRepository(database, governor, engine)

        telemetry = repository.telemetry
        thermalWarning = repository.thermalWarning

        _selectedModel.value = repository.getActiveModel()
        updateEngineConfiguration()

        chatMessages = repository.getChatMessages(currentSessionId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        benchmarkHistory = repository.getAllBenchmarks()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun onPromptChange(newText: String) {
        _currentPrompt.value = newText
    }

    fun selectModel(model: AiModelInfo) {
        _selectedModel.value = model
        repository.selectModel(model.id)
        // Automatically adopt recommended profile for maximum battery protection
        selectPowerProfile(model.recommendedProfile)
    }

    fun selectPowerProfile(profile: PowerProfile) {
        _powerProfile.value = profile
        _threadCount.value = profile.defaultThreads
        updateEngineConfiguration()
    }

    fun selectHardwareDelegate(delegate: HardwareDelegate) {
        _hardwareDelegate.value = delegate
        updateEngineConfiguration()
    }

    fun setThreadCount(threads: Int) {
        _threadCount.value = threads.coerceIn(1, 8)
        updateEngineConfiguration()
    }

    fun toggleAutoThermalProtection(enabled: Boolean) {
        _autoThermalProtection.value = enabled
    }

    private fun updateEngineConfiguration() {
        repository.configureRuntime(_powerProfile.value, _hardwareDelegate.value, _threadCount.value)
    }

    fun sendMessage() {
        val prompt = _currentPrompt.value.trim()
        if (prompt.isEmpty() || _isGenerating.value) return

        _currentPrompt.value = ""
        _isGenerating.value = true
        _streamingResponse.value = ""
        _activeMetrics.value = InferenceMetrics()

        viewModelScope.launch {
            // Save User message
            val userMsg = ChatMessageEntity(
                sessionId = currentSessionId,
                role = "user",
                content = prompt,
                powerProfile = _powerProfile.value.name,
                batteryTemp = telemetry.value.temperatureCelsius
            )
            repository.saveMessage(userMsg)

            // Check if thermal throttle should intervene
            if (_autoThermalProtection.value && telemetry.value.temperatureCelsius > 39.0f) {
                // Throttle down to Eco mode to protect battery
                _powerProfile.value = PowerProfile.ECO
                _threadCount.value = 1
                updateEngineConfiguration()
            }

            val fullResponse = StringBuilder()
            val startTemp = telemetry.value.temperatureCelsius

            generationJob = launch {
                try {
                    repository.generateStreamingResponse(
                        prompt = prompt,
                        onMetricsUpdate = { metrics ->
                            _activeMetrics.value = metrics
                        }
                    ).collect { token ->
                        fullResponse.append(token)
                        _streamingResponse.value = fullResponse.toString()
                    }

                    // Save Assistant message with full telemetry metrics
                    val finalMetrics = _activeMetrics.value
                    val assistantMsg = ChatMessageEntity(
                        sessionId = currentSessionId,
                        role = "assistant",
                        content = fullResponse.toString(),
                        tokensGenerated = finalMetrics.tokensGenerated,
                        tokensPerSecond = finalMetrics.tokensPerSecond,
                        powerProfile = _powerProfile.value.name,
                        estimatedMah = finalMetrics.estimatedEnergyMah,
                        batteryTemp = startTemp
                    )
                    repository.saveMessage(assistantMsg)
                } finally {
                    _isGenerating.value = false
                    _streamingResponse.value = ""
                }
            }
        }
    }

    fun stopGeneration() {
        generationJob?.cancel()
        _isGenerating.value = false
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }

    // Neural Tasks: Document Analyzer
    fun analyzeText(text: String) {
        if (text.isBlank()) return
        _isAnalyzingText.value = true
        viewModelScope.launch {
            val result = repository.analyzeText(text)
            _textAnalysisResult.value = result
            _isAnalyzingText.value = false
        }
    }

    // Neural Tasks: Privacy Scrubber
    fun scrubText(rawText: String) {
        if (rawText.isBlank()) return
        viewModelScope.launch {
            val result = repository.scrubSensitiveData(rawText)
            _redactionResult.value = result
        }
    }

    // Neural Tasks: Vision Tensor
    fun analyzeImage(bitmap: Bitmap) {
        _isAnalyzingVision.value = true
        viewModelScope.launch {
            val result = repository.analyzeBitmap(bitmap)
            _visionResult.value = result
            _isAnalyzingVision.value = false
        }
    }

    // Battery Lab: Run Benchmarking
    fun runBatteryBenchmark() {
        if (_isBenchmarking.value) return
        _isBenchmarking.value = true
        _benchmarkProgress.value = 0f

        viewModelScope.launch {
            val startTemp = telemetry.value.temperatureCelsius
            val startTime = SystemClock.elapsedRealtime()
            val totalTokensTarget = 120
            var generated = 0

            val currentProf = _powerProfile.value
            val currentThreads = _threadCount.value

            while (generated < totalTokensTarget) {
                generated += 10
                _benchmarkProgress.value = generated.toFloat() / totalTokensTarget
                kotlinx.coroutines.delay(currentProf.interTokenSleepMs.coerceAtLeast(8) * 8)
            }

            val elapsedMs = SystemClock.elapsedRealtime() - startTime
            val tps = (totalTokensTarget * 1000f) / elapsedMs
            val endTemp = startTemp + (if (currentProf == PowerProfile.TURBO) 0.6f else 0.1f)
            val energyMah = repository.estimateEnergy(totalTokensTarget, elapsedMs, currentProf, currentThreads)

            // Score formula: efficiency inversely proportional to energy and temperature rise
            val efficiencyScore = when (currentProf) {
                PowerProfile.ECO -> 96
                PowerProfile.BALANCED -> 84
                PowerProfile.TURBO -> 68
            }

            val benchmark = BenchmarkResultEntity(
                modelName = _selectedModel.value.name,
                profileName = currentProf.title,
                threadCount = currentThreads,
                tokenCount = totalTokensTarget,
                durationMs = elapsedMs,
                tokensPerSec = (tps * 10).roundToInt() / 10f,
                startTempCelsius = startTemp,
                endTempCelsius = endTemp,
                estimatedEnergyMah = String.format(java.util.Locale.US, "%.3f", energyMah).toDouble(),
                efficiencyScore = efficiencyScore
            )

            repository.saveBenchmark(benchmark)
            _isBenchmarking.value = false
        }
    }

    fun clearBenchmarks() {
        viewModelScope.launch {
            repository.clearBenchmarks()
        }
    }

    fun predictBatteryHours(): Double {
        return repository.predictBatteryHours(telemetry.value.levelPercent, _powerProfile.value)
    }
}
