package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.engine.OnDeviceNeuralEngine
import com.example.data.model.AiModelInfo
import com.example.data.model.HardwareDelegate
import com.example.data.model.PowerProfile
import com.example.ui.components.BatteryTelemetryGauge
import com.example.ui.components.PowerProfileItem
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RedDanger
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateDark800
import com.example.ui.theme.SlateDark900
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun RuntimeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val thermalWarning by viewModel.thermalWarning.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val powerProfile by viewModel.powerProfile.collectAsStateWithLifecycle()
    val hardwareDelegate by viewModel.hardwareDelegate.collectAsStateWithLifecycle()
    val threadCount by viewModel.threadCount.collectAsStateWithLifecycle()
    val autoThermalProtection by viewModel.autoThermalProtection.collectAsStateWithLifecycle()
    val predictedHours = viewModel.predictBatteryHours()

    val availableModels = OnDeviceNeuralEngine().availableModels

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDark900)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner: Futuristic Neural Architecture
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_status_banner"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_ai),
                            contentDescription = "On-Device Neural Processor",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Gradient Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        listOf(androidx.compose.ui.graphics.Color.Transparent, SlateCard)
                                    )
                                )
                        )
                    }

                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "موتور عصبی محلی (On-Device Runtime)",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Surface(
                                color = EmeraldPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WifiOff,
                                        contentDescription = null,
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "۱۰۰٪ آفلاین",
                                        color = EmeraldLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            text = "اجرای مدل هوش مصنوعی مستقیماً بر روی چیپست گوشی، با مهار حرارتی و کنترل دقیق مصرف باتری.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Real-time Battery & Thermal Telemetry Gauge
        item {
            BatteryTelemetryGauge(
                telemetry = telemetry,
                predictedHours = predictedHours
            )
        }

        // Active Thermal Alert Warning if device is hot
        if (thermalWarning) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("thermal_warning_card"),
                    colors = CardDefaults.cardColors(containerColor = RedDanger.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, RedDanger)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = "Warning",
                            tint = RedDanger,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "هشدار افزایش دمای باتری (>39°C)",
                                color = RedDanger,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "تعداد هسته‌ها و گام‌های استنتاج به طور خودکار کاهش یافته است تا از باتری محافظت شود.",
                                color = TextPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Section: Power Profiles (Eco, Balanced, Turbo)
        item {
            Text(
                text = "پروفایل مصرف انرژی و توان پردازشی",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "پروفایل متناسب با نیاز خود را انتخاب کنید تا تخلیه باتری به حداقل برسد.",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        items(PowerProfile.values()) { profile ->
            PowerProfileItem(
                profile = profile,
                isSelected = powerProfile == profile,
                onSelect = { viewModel.selectPowerProfile(profile) }
            )
        }

        // Section: On-Device Model Picker
        item {
            Text(
                text = "انتخاب مدل محلی هوش مصنوعی",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "مدل‌های کوانتایز شده سبک با قابلیت اجرای بدون اینترنت در حافظه دستگاه",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        items(availableModels) { model ->
            val isModelSelected = selectedModel.id == model.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectModel(model) }
                    .testTag("model_item_${model.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = if (isModelSelected) SlateCard else SlateDark800),
                border = BorderStroke(if (isModelSelected) 1.5.dp else 1.dp, if (isModelSelected) CyanAccent else SlateBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isModelSelected) CyanAccent.copy(alpha = 0.2f) else SlateBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeveloperBoard,
                                    contentDescription = null,
                                    tint = if (isModelSelected) CyanAccent else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = model.name,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = model.parameterCount,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            color = SlateDark900,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, SlateBorder)
                        ) {
                            Text(
                                text = "${model.ramRequirementMb} MB RAM",
                                color = EmeraldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = model.descriptionFa,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        model.supportedTasks.take(3).forEach { task ->
                            Surface(
                                color = SlateDark900,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = task,
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Hardware Delegation & Threads
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hardware_governor_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "تنظیمات شتاب‌دهنده سخت‌افزاری",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Hardware Delegate selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HardwareDelegate.values().forEach { delegate ->
                            val isDelSelected = hardwareDelegate == delegate
                            FilterChip(
                                selected = isDelSelected,
                                onClick = { viewModel.selectHardwareDelegate(delegate) },
                                label = {
                                    Text(
                                        text = when (delegate) {
                                            HardwareDelegate.CPU -> "CPU Arm"
                                            HardwareDelegate.GPU_VULKAN -> "GPU Vulkan"
                                            HardwareDelegate.NNAPI -> "NPU (NNAPI)"
                                        },
                                        fontSize = 11.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary.copy(alpha = 0.25f),
                                    selectedLabelColor = EmeraldLight,
                                    containerColor = SlateDark800,
                                    labelColor = TextSecondary
                                ),
                                border = BorderStroke(1.dp, if (isDelSelected) EmeraldPrimary else SlateBorder)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Thread count slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تعداد هسته‌های فعال پردازنده: $threadCount",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (threadCount == 1) "کم‌مصرف‌ترین حالت" else "پردازش سریع‌تر",
                            color = if (threadCount == 1) EmeraldLight else AmberWarning,
                            fontSize = 11.sp
                        )
                    }

                    Slider(
                        value = threadCount.toFloat(),
                        onValueChange = { viewModel.setThreadCount(it.toInt()) },
                        valueRange = 1f..4f,
                        steps = 2,
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldPrimary,
                            activeTrackColor = EmeraldPrimary,
                            inactiveTrackColor = SlateDark800
                        ),
                        modifier = Modifier.testTag("threads_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Thermal Guard Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "محافظ هوشمند حرارت باتری (Auto Thermal Guard)",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "در صورت گرم شدن گوشی (>39°C)، سرعت پردازش را خودکار کم می‌کند تا باتری آسیب نبیند.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = autoThermalProtection,
                            onCheckedChange = { viewModel.toggleAutoThermalProtection(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = EmeraldPrimary.copy(alpha = 0.4f),
                                uncheckedTrackColor = SlateDark800
                            ),
                            modifier = Modifier.testTag("thermal_guard_switch")
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
