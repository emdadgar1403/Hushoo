package com.example

import com.example.data.engine.OnDeviceNeuralEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testPiiScrubberRedactsNationalIdAndCardsLocally() {
        val engine = OnDeviceNeuralEngine()
        val text = "کد ملی: 0012345678 و کارت: 6037991812345678"
        val result = engine.scrubSensitiveDataLocally(text)

        assertTrue(result.detectedPiiCount >= 2)
        assertTrue(result.sanitizedText.contains("کد ملی محفوظ شد"))
        assertTrue(result.sanitizedText.contains("شماره کارت بانکی امن شد"))
    }

    @Test
    fun testOfflineModelsListNotEmpty() {
        val engine = OnDeviceNeuralEngine()
        assertTrue(engine.availableModels.isNotEmpty())
        assertEquals("NeuroLlama-1B Mobile", engine.getActiveModel().name)
    }
}
