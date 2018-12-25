package com.example.sweet.har_auth

import android.graphics.Bitmap

interface Classifier {
    data class Recognition(
        var id: String = "", // Recognitionした結果のラベル(on the model)
        var title: String = "", // Target Name
        var confidence: Float = 0F // 信頼性のスコア A sortable score for how good the recognition is relative to others. Higher should be better.
    ){
        override fun toString(): String {
            return "Title = $title, Confidence = $confidence)"
        }
    }
    // fun recognizeImage(bitmap: Bitmap): List<Recognition>
    fun recognizePerson(sensor: SensorQueue): List<Recognition>

    fun close()
}