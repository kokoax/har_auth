package com.example.sweet.har_auth

import android.content.res.AssetManager
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Float
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

class TensorPersonClassifier: Classifier {
    var interpreter: Interpreter? = null
    var labelList: List<String> = emptyList()
    var inputSize: Int = 0
    companion object {
        private val MAX_RESULTS = 3
        private val BATCH_SIZE = 1
        private val CHANNEL_SIZE = 3
        private val THRESHOLD = 0.1f

        fun create(
            assetManager: AssetManager,
            modelPath: String,
            labelPath: String,
            inputSize: Int
        ) : Classifier {
            val classifier = TensorPersonClassifier()
            classifier.interpreter = Interpreter(classifier.loadModelFile(assetManager, modelPath))
            classifier.labelList = classifier.loadLabelList(assetManager, labelPath)
            classifier.inputSize = inputSize

            return classifier
        }
    }

    override fun recognizePerson(sensor: SensorQueue): List<Classifier.Recognition> {
        val byteBuffer = convertFloatToByteBuffer(sensor)
        val result = Array(1) { FloatArray(labelList.size) }
        interpreter!!.run(byteBuffer, result)
        return getSortedResult(result)
    }

    /*
    override fun recognizeImage(bitmap: Bitmap) : List<Classifier.Recognition> {
        val byteBuffer = convertBitmapToByteBuffer(bitmap)
        val result = Array(1) { ByteArray(labelList!!.size) }
        interpreter!!.run(byteBuffer, result)
        return getSortedResult(result)
    }
    */

    override fun close() {
        interpreter!!.close()
        interpreter = null
    }

    @Throws(IOException::class)
    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    @Throws(IOException::class)
    private fun loadLabelList(assetManager: AssetManager, labelPath: String): List<String> {
        val labelList = ArrayList<String>()
        val reader = BufferedReader(InputStreamReader(assetManager.open(labelPath)))
        while (true) {
            val line = reader.readLine() ?: break
            labelList.add(line)
        }
        reader.close()
        return labelList
    }


    private fun convertFloatToByteBuffer(sensor: SensorQueue): ByteBuffer {
        val bytePerChannel = 4 // for float
        val byteBuffer = ByteBuffer.allocateDirect(BATCH_SIZE * inputSize * CHANNEL_SIZE * bytePerChannel)
        byteBuffer.order(ByteOrder.nativeOrder())
        val sorted = sensor.sorted()
        for (i in 0 until inputSize) {
            println(i)
            byteBuffer.putFloat(sorted[i].x)
            byteBuffer.putFloat(sorted[i].y)
            byteBuffer.putFloat(sorted[i].z)
            byteBuffer.putFloat(sorted[i].mag)
        }
        return byteBuffer
    }

    private fun getSortedResult(labelProbArray: Array<FloatArray>): List<Classifier.Recognition> {

        val pq = PriorityQueue(
            MAX_RESULTS,
            Comparator<Classifier.Recognition> { (_, _, confidence1), (_, _, confidence2) -> Float.compare(confidence1, confidence2) })

        for (i in labelList!!.indices) {
            // val confidence = (labelProbArray[0][i].toInt() and 0xff) / 255.0f
            val confidence = (labelProbArray[0][i] * 100) / 127.0F
            if (confidence > THRESHOLD) {
                pq.add(Classifier.Recognition("" + i,
                    if (labelList!!.size > i) labelList!![i] else "Unknown",
                    confidence))
            }
        }

        val recognitions = ArrayList<Classifier.Recognition>()
        val recognitionsSize = Math.min(pq.size, MAX_RESULTS)
        for (i in 0 until recognitionsSize) {
            recognitions.add(pq.poll())
        }

        return recognitions
    }
}