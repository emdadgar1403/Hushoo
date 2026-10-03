package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.BenchmarkResultEntity
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
fun BatteryLabScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val isBenchmarking by viewModel.isBenchmarking.collectAsStateWithLifecycle()
    val benchmarkProgress by viewModel.benchmarkProgress.collectAsStateWithLifecycle()
    val history by viewModel.benchmarkHistory.collectAsStateWithLifecycle()
    val powerProfile by viewModel.powerProfile.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDark900)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Battery Lab Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.testTag("battery_lab_header")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BatterySaver,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "آزمایشگاه بهینه‌سازی باتری (Battery Lab)",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "اندازه‌گیری توان مصرفی واقعی پردازنده در تست‌های سنگین",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Benchmark Launcher Button
                    Button(
                        onClick = { viewModel.runBatteryBenchmark() },
                        enabled = !isBenchmarking,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_benchmark_button")
                    ) {
                        if (isBenchmarking) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "در حال شبیه‌سازی بار محاسباتی (${(benchmarkProgress * 100).toInt()}%)...",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "اجرای بنچمارک سنجش مصرف انرژی (${powerProfile.titleFa})",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (isBenchmarking) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { benchmarkProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = CyanAccent,
                            trackColor = SlateDark800
                        )
                    }
                }
            }
        }

        // Comparative Energy Consumption Table
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مقایسه کارایی انرژی بین پروفایل‌های هوش مصنوعی:",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileComparisonRow(
                        title = "حالت فوق‌بهینه (Eco Saver)",
                        quant = "INT4",
                        drain = "۱.۴ mAh / 1K توکن",
                        hours = "۷.۸ ساعت کار مداوم",
                        color = EmeraldPrimary,
                        progress = 0.95f
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileComparisonRow(
                        title = "حالت متعادل (Balanced)",
                        quant = "INT8",
                        drain = "۲.۹ mAh / 1K توکن",
                        hours = "۴.۵ ساعت کار مداوم",
                        color = CyanAccent,
                        progress = 0.65f
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileComparisonRow(
                        title = "حالت توربو (Turbo Max)",
                        quant = "FP16",
                        drain = "۵.۸ mAh / 1K توکن",
                        hours = "۲.۱ ساعت کار مداوم",
                        color = AmberWarning,
                        progress = 0.35f
                    )
                }
            }
        }

        // Benchmark History Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سوابق تست‌های ثبت شده (${history.size})",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (history.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearBenchmarks() },
                        modifier = Modifier.size(24.dp).testTag("clear_benchmarks_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Benchmarks",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        if (history.isEmpty()) {
            item {
                Text(
                    text = "هنوز تستی اجرا نشده است. برای بررسی راندمان باتری، دکمه اجرای بنچمارک را لمس کنید.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(history) { item ->
                BenchmarkItemCard(item)
            }
        }
    }
}

@Composable
fun ProfileComparisonRow(
    title: String,
    quant: String,
    drain: String,
    hours: String,
    color: Color,
    progress: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "$title ($quant)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(text = hours, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = SlateDark800
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "مصرف در هر ۱۰۰۰ توکن:", color = TextMuted, fontSize = 10.sp)
            Text(text = drain, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
fun BenchmarkItemCard(item: BenchmarkResultEntity) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("benchmark_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = SlateDark800),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SlateBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.modelName} • ${item.profileName}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Surface(
                    color = EmeraldPrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "امتیاز راندمان: ${item.efficiencyScore}/100",
                        color = EmeraldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "نرخ: ${item.tokensPerSec} TPS",
                    color = CyanAccent,
                    fontSize = 12.sp
                )
                Text(
                    text = "زمان: ${item.durationMs} ms",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "انرژی: ${item.estimatedEnergyMah} mAh",
                    color = EmeraldLight,
                    fontSize = 12.sp
                )
                Text(
                    text = "تغییر دما: +${String.format(java.util.Locale.US, "%.1f", item.endTempCelsius - item.startTempCelsius)}°C",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
