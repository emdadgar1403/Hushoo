package com.example.data.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.data.model.BatteryTelemetry
import com.example.data.model.PowerProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

class BatteryThermalGovernor(private val context: Context) {

    private val _telemetry = MutableStateFlow(BatteryTelemetry())
    val telemetry: StateFlow<BatteryTelemetry> = _telemetry.asStateFlow()

    private val _thermalWarning = MutableStateFlow(false)
    val thermalWarning: StateFlow<Boolean> = _thermalWarning.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            intent?.let { updateFromIntent(it) }
        }
    }

    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialIntent = context.registerReceiver(batteryReceiver, filter)
        initialIntent?.let { updateFromIntent(it) }
    }

    private fun updateFromIntent(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)

        val pct = if (level >= 0 && scale > 0) (level * 100) / scale else 85
        val tempCelsius = if (rawTemp > 0) rawTemp / 10.0f else 32.0f
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val healthStr = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "سالم (Good)"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "داغ شدن باتری (Overheat)"
            BatteryManager.BATTERY_HEALTH_DEAD -> "ضعیف (Dead)"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "اضافه ولتاژ (Over Voltage)"
            else -> "عادی (Normal)"
        }

        val statusStr = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "در حال شارژ (Charging)"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "تخلیه شارژ (Discharging)"
            BatteryManager.BATTERY_STATUS_FULL -> "کامل شارژ شده (Full)"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "متصل بدون شارژ (Not Charging)"
            else -> "مستقل (Battery)"
        }

        // Thermal throttle trigger when temp crosses 39.5°C
        val isThrottling = tempCelsius >= 39.5f
        _thermalWarning.value = isThrottling

        _telemetry.value = BatteryTelemetry(
            levelPercent = pct,
            temperatureCelsius = tempCelsius,
            voltageMv = voltage,
            health = healthStr,
            status = statusStr,
            isCharging = isCharging,
            isThermalThrottling = isThrottling,
            currentMahEstimate = calculateBaseCurrent(pct, isCharging, isThrottling)
        )
    }

    private fun calculateBaseCurrent(level: Int, isCharging: Boolean, isThrottling: Boolean): Double {
        if (isCharging) return 0.0
        var base = 280.0
        if (isThrottling) base += 140.0
        return base
    }

    /**
     * Estimates battery energy consumed for an inference session in mAh.
     * Uses power consumption models for mobile ARM big.LITTLE architectures:
     * - ECO (INT4, 1 core): ~150-250mW extra draw (~40-60mA @ 3.85V)
     * - BALANCED (INT8, 2 cores): ~500-750mW extra draw (~130-190mA)
     * - TURBO (FP16, 4 cores): ~1800-2400mW extra draw (~450-620mA)
     */
    fun estimateInferenceEnergyMah(
        tokens: Int,
        durationMs: Long,
        profile: PowerProfile,
        threads: Int
    ): Double {
        val durationHours = max(durationMs, 100L) / 3_600_000.0
        val extraCurrentMa = when (profile) {
            PowerProfile.ECO -> 55.0 * (threads * 0.8)
            PowerProfile.BALANCED -> 160.0 * (threads * 0.7)
            PowerProfile.TURBO -> 480.0 * (threads * 0.6)
        }
        return extraCurrentMa * durationHours
    }

    /**
     * Predicts total continuous inference hours remaining with current battery level & profile
     */
    fun predictRemainingHours(levelPercent: Int, profile: PowerProfile): Double {
        val nominalBatteryCapacityMah = 4500.0
        val remainingCapacityMah = (levelPercent / 100.0) * nominalBatteryCapacityMah
        val baseDrainMa = 120.0
        val inferenceDrainMa = when (profile) {
            PowerProfile.ECO -> 160.0
            PowerProfile.BALANCED -> 380.0
            PowerProfile.TURBO -> 920.0
        }
        val totalDrainMa = baseDrainMa + inferenceDrainMa
        return String.format(java.util.Locale.US, "%.1f", remainingCapacityMah / totalDrainMa).toDouble()
    }
}
