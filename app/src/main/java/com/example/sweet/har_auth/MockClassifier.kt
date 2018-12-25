package com.example.sweet.har_auth

import android.content.res.AssetManager
import org.tensorflow.lite.Interpreter

class MockClassifier: Classifier {
    data class Recognition(
        var id: String = "", // Recognitionした結果のラベル
        var title: String = "",
        var confidence: Float = 0F // 信頼性のスコア A sortable score for how good the recognition is relative to others. Higher should be better.
    )

   companion object {
        fun create(
            assetManager: AssetManager?,
            modelPath: String,
            labelPath: String,
            inputSize: Int
        ) : Classifier {
            val classifier = TensorPersonClassifier()
            classifier.interpreter = null
            classifier.labelList = emptyList()
            classifier.inputSize = inputSize

            return classifier
        }
    }

    override fun recognizePerson(sensor: SensorQueue): List<Classifier.Recognition> {
        return listOf<Classifier.Recognition>(Classifier.Recognition("-1", "本人(sample)", 0.5F))
    }

    override fun close() {
        /*
        interpreter!!.close()
        interpreter = null
        */
    }
}
