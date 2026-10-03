package com.example.data.model

enum class PowerProfile(
    val title: String,
    val titleFa: String,
    val description: String,
    val descriptionFa: String,
    val defaultThreads: Int,
    val interTokenSleepMs: Long,
    val quantization: String,
    val estimatedContinuousHours: Double,
    val batteryImpactRate: String
) {
    ECO(
        title = "Eco Saver",
        titleFa = "حالت فوق‌بهینه (صرفه‌جویی باتری)",
        description = "INT4 quantization, 1 CPU thread, adaptive low-power pacing to preserve battery and keep device cool.",
        descriptionFa = "کوانتایز ۴ بیتی، تک‌هسته کم‌مصرف، وقفه بهینه بین توکن‌ها جهت خنک ماندن پردازنده و کاهش مصرف تا ۷۰٪",
        defaultThreads = 1,
        interTokenSleepMs = 28L,
        quantization = "INT4 (4-Bit)",
        estimatedContinuousHours = 7.8,
        batteryImpactRate = "~1.4 mAh / 1K tokens"
    ),
    BALANCED(
        title = "Balanced",
        titleFa = "حالت متعادل (استاندارد)",
        description = "INT8 quantization, 2-4 CPU threads, active thermal guard. Ideal balance between speed and energy.",
        descriptionFa = "کوانتایز ۸ بیتی، ۲ تا ۴ هسته، محافظ حرارتی خودکار. تعادل عالی بین سرعت پاسخ‌دهی و مصرف شارژ",
        defaultThreads = 2,
        interTokenSleepMs = 12L,
        quantization = "INT8 (8-Bit)",
        estimatedContinuousHours = 4.5,
        batteryImpactRate = "~2.9 mAh / 1K tokens"
    ),
    TURBO(
        title = "Turbo Max",
        titleFa = "حالت توربو (حداکثر سرعت)",
        description = "FP16 precision, maximum CPU/GPU threads, zero pacing for ultra-fast heavy offline computation.",
        descriptionFa = "دقت بالای FP16، تمام هسته‌های پردازشی یا شتاب‌دهنده GPU، پردازش فوری بدون درنگ با توان حداکثری",
        defaultThreads = 4,
        interTokenSleepMs = 0L,
        quantization = "FP16 (16-Bit)",
        estimatedContinuousHours = 2.1,
        batteryImpactRate = "~5.8 mAh / 1K tokens"
    )
}

enum class HardwareDelegate(val label: String, val description: String) {
    CPU("CPU Multi-threading", "Optimized Arm NEON vectorized execution"),
    GPU_VULKAN("GPU / Vulkan Compute", "Parallel tensor compute shader delegation"),
    NNAPI("Android NNAPI", "Direct on-chip NPU / Neural accelerator delegation")
}

data class AiModelInfo(
    val id: String,
    val name: String,
    val parameterCount: String,
    val ramRequirementMb: Int,
    val quantizationType: String,
    val description: String,
    val descriptionFa: String,
    val recommendedProfile: PowerProfile,
    val supportedTasks: List<String>
)

data class BatteryTelemetry(
    val levelPercent: Int = 85,
    val temperatureCelsius: Float = 31.4f,
    val voltageMv: Int = 4120,
    val health: String = "Good",
    val status: String = "Discharging",
    val isCharging: Boolean = false,
    val isThermalThrottling: Boolean = false,
    val currentMahEstimate: Double = 320.0
)

data class InferenceMetrics(
    val tokensGenerated: Int = 0,
    val tokensPerSecond: Float = 0f,
    val elapsedDurationMs: Long = 0L,
    val timeToFirstTokenMs: Long = 0L,
    val estimatedEnergyMah: Double = 0.0,
    val memoryUsageMb: Int = 185
)
