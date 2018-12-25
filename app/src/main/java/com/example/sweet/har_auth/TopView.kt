package com.example.sweet.har_auth

import android.content.Context
import android.content.res.AssetManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import kotlin.properties.Delegates
import java.util.concurrent.Executors
import android.os.SystemClock
import java.io.*
import java.nio.channels.FileChannel

class TopView : AppCompatActivity(), SensorEventListener {
    companion object {
        // private val MODEL_PATH = "mobilenet_quant_v1_224.tflite"
        private val MODEL_PATH = "tf_resnet.tflite"
        // private val LABEL_PATH = "labels.txt"
        private val LABEL_PATH = "resnet_labels.txt"
        // private val INPUT_SIZE = 225 // what is this
        private val INPUT_SIZE = 128
    }

    private val executor = Executors.newSingleThreadExecutor()

    private var mManager: SensorManager by Delegates.notNull<SensorManager>()
    private var mSensor: Sensor by Delegates.notNull<Sensor>()
    private lateinit var sensorInfo: TextView
    private lateinit var resultsInfo: TextView
    private lateinit var personPredictionInfo: TextView
    private lateinit var testPredictionInfo: TextView
    private var classifier: Classifier by Delegates.notNull<Classifier>()
    private var sensorQueue: SensorQueue = SensorQueue.create(INPUT_SIZE)
    private var countup = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_view)
        sensorInfo = findViewById(R.id.sensor_text)
        resultsInfo = findViewById(R.id.results_text)
        personPredictionInfo = findViewById(R.id.person_prediction_text)
        testPredictionInfo = findViewById(R.id.test_prediction_text)

        mManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensor = mManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        initTensorModelLoad()

        executor.execute {
            try {
                val testQueue = loadSensorFromCSV("uci_har_1.csv")
                val results = classifier.recognizePerson(testQueue)
                testPredictionInfo.text = results.toString()
            } catch (e: Exception) {
                throw RuntimeException("Error initializing test csv!", e)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        mManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        /*
        val sb = StringBuilder()
        this.sensorQueue.all().forEach {
            sb.append(it.toString())
        }
        */
        sensorInfo.text = "%.4f %.4f %.4f".format(event?.values!![0], event?.values!![1], event?.values!![2])
        this.sensorQueue.enqueue(event?.values!![0], event?.values!![1], event?.values!![2])

        if(countup >= INPUT_SIZE) {
            val startTime = SystemClock.uptimeMillis()
            val results = classifier.recognizePerson(this.sensorQueue)
            val endTime = SystemClock.uptimeMillis()
            resultsInfo.text = results.toString()
            personPredictionInfo.text = "%f sec".format((endTime - startTime).toFloat() / 1000)
            countup = 0
        } else countup++
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    @Throws(IOException::class)
    private fun loadSensorFromCSV(filepath: String): SensorQueue {
        var testSensorQueue: SensorQueue = SensorQueue.create(INPUT_SIZE)

        val inputStreamReader = InputStreamReader(assets.open(filepath))
        inputStreamReader.forEachLine {
            val tmp = it.split(",")
            Log.d("load sensor from csv", tmp[0] + "jdlkajskdjaslkdjalsdjaldjsaldjalskdjsakldjlaksjdklsa")
            testSensorQueue.enqueue(tmp[0].toFloat(), tmp[1].toFloat(), tmp[2].toFloat())
        }
        return testSensorQueue
    }

    private fun initTensorModelLoad() {
        executor.execute {
            try {
                classifier = TensorPersonClassifier.create(
                    assets,
                    MODEL_PATH,
                    LABEL_PATH,
                    INPUT_SIZE)
            } catch (e: Exception) {
                throw RuntimeException("Error initializing TensorFlow!", e)
            }
        }
    }
}
