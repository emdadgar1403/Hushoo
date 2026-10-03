package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PurpleNeural
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
fun NeuralTasksScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("پالایش حریم خصوصی (PII)", "خلاصه‌سازی متن", "پردازش تنسور تصویر")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDark900)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SlateCard,
            contentColor = EmeraldPrimary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) EmeraldLight else TextSecondary
                        )
                    },
                    modifier = Modifier.testTag("task_tab_$index")
                )
            }
        }

        when (selectedTab) {
            0 -> PiiScrubberSection(viewModel)
            1 -> DocumentAnalyzerSection(viewModel)
            2 -> VisionTensorSection(viewModel)
        }
    }
}

@Composable
fun PiiScrubberSection(viewModel: MainViewModel) {
    val redactionResult by viewModel.redactionResult.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    val samplePiiText = "قرارداد محرمانه: اطلاعات آقای رضایی با کد ملی ۰۰۱۲۳۴۵۶۷۸ و شماره تماس ۰۹۱۲۳۴۵۶۷۸۹ جهت واریز به کارت ۶۰۳۷۹۹۱۸۱۲۳۴۵۶۷۸ و ایمیل info@secure-corp.ir ثبت گردید."
    var rawInput by remember { mutableStateOf(samplePiiText) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "سپر محلی حریم خصوصی (Offline Data Scrubber)",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "پالایش خودکار کدهای ملی، کارت‌های شتاب، شماره تماس‌ها و ایمیل بدون ارسال حتی ۱ بایت به اینترنت.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "متن ورودی حاوی اطلاعات حساس:",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = rawInput,
                onValueChange = { rawInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pii_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = SlateBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SlateCard,
                    unfocusedContainerColor = SlateCard
                ),
                maxLines = 6
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.scrubText(rawInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("scrub_data_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "پالایش و ایمن‌سازی محلی",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = {
                        rawInput = samplePiiText
                        viewModel.scrubText(samplePiiText)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SlateDark800),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SlateBorder)
                ) {
                    Text(text = "متن نمونه", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }

        // Output Result
        redactionResult?.let { result ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("redaction_result_card"),
                    colors = CardDefaults.cardColors(containerColor = SlateDark800),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "نتیجه پاک‌سازی امن (محافظت شده):",
                                color = EmeraldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Surface(
                                color = EmeraldPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${result.detectedPiiCount} مورد حساس محافظت شد",
                                    color = EmeraldLight,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result.sanitizedText,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { clipboardManager.setText(AnnotatedString(result.sanitizedText)) },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateCard),
                                border = BorderStroke(1.dp, SlateBorder),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("کپی متن امن شده", color = TextPrimary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentAnalyzerSection(viewModel: MainViewModel) {
    val analysisResult by viewModel.textAnalysisResult.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzingText.collectAsStateWithLifecycle()

    val sampleDoc = "هوش مصنوعی لوکال (On-Device AI) انقلابی در پردازش امن داده‌هاست. در این معماری، وزن‌های شبکه عصبی به صورت کوانتایز شده درون حافظه موقت رم تلفن همراه قرار می‌گیرند و کلیه محاسبات ضرب ماتریسی بدون نیاز به اتصال اینترنت یا سرورهای خارجی توسط پردازنده‌های کم‌مصرف انجام می‌شود. این رویکرد خطر نشت اطلاعات را به صفر رسانده و باتری گوشی را با کاهش توان محاسباتی تا ۷۰ درصد نجات می‌دهد."
    var docInput by remember { mutableStateOf(sampleDoc) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyanAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "تحلیلگر و خلاصه‌ساز آفلاین متن",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "استخراج نکات کلیدی، تحلیل حس و فشرده‌سازی متن کاملاً روی دستگاه",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = docInput,
                onValueChange = { docInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("doc_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = SlateBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SlateCard,
                    unfocusedContainerColor = SlateCard
                ),
                maxLines = 6
            )
        }

        item {
            Button(
                onClick = { viewModel.analyzeText(docInput) },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analyze_doc_button")
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black)
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تحلیل و خلاصه‌سازی هوشمند",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        analysisResult?.let { result ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("doc_analysis_result"),
                    colors = CardDefaults.cardColors(containerColor = SlateDark800),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SlateBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "خلاصه عصبی تولید شده:",
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "کاهش حجم: ${result.compressionRatio}",
                                color = EmeraldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = result.summary,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "تحلیل حس متن: ${result.sentiment}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "کلمات کلیدی اصلی:",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            result.topKeywords.forEach { kw ->
                                Surface(
                                    color = SlateCard,
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.5.dp, SlateBorder)
                                ) {
                                    Text(
                                        text = kw,
                                        color = EmeraldLight,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VisionTensorSection(viewModel: MainViewModel) {
    val visionResult by viewModel.visionResult.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzingVision.collectAsStateWithLifecycle()

    // Create a local sample bitmap for offline tensor analysis
    val sampleBitmap = remember {
        val bmp = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint().apply {
            color = android.graphics.Color.rgb(16, 185, 129)
        }
        canvas.drawRect(0f, 0f, 224f, 112f, paint)
        paint.color = android.graphics.Color.rgb(6, 182, 212)
        canvas.drawCircle(112f, 112f, 60f, paint)
        bmp
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PurpleNeural.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = PurpleNeural,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "تحلیلگر تنسورهای تصویری (Vision Tensor Core)",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "استخراج ویژگی‌های تصویری و هیستوگرام رنگ محلی بدون خروج تصویر از دستگاه",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = { viewModel.analyzeImage(sampleBitmap) },
                colors = ButtonDefaults.buttonColors(containerColor = PurpleNeural),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analyze_vision_button")
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                } else {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "اجرای خط‌لوله تنسور تصویری روی پردازنده گرافیکی محلی",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        visionResult?.let { res ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("vision_tensor_result"),
                    colors = CardDefaults.cardColors(containerColor = SlateDark800),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, PurpleNeural.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "مشخصات تنسور ورودی:",
                            color = PurpleNeural,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "ابعاد تنسور: ${res.dimensions}", color = TextPrimary, fontSize = 12.sp)
                        Text(text = "کانال ورودی: ${res.tensorChannelFormat}", color = TextSecondary, fontSize = 12.sp)
                        Text(text = "میانگین روشنایی (Luma): ${res.averageLuminance} / 255", color = TextSecondary, fontSize = 12.sp)
                        Text(text = "تم رنگی غالب: ${res.dominantTone}", color = EmeraldLight, fontSize = 12.sp)
                        Text(text = "سطح پیچیدگی بافت: ${res.complexity}", color = CyanAccent, fontSize = 12.sp)
                        Text(text = "زمان اجرای خط‌لوله: ${res.estimatedInferenceTimeMs} میلی‌ثانیه", color = TextMuted, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
