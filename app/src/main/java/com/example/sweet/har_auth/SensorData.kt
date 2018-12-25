package com.example.sweet.har_auth

import kotlin.math.pow
import kotlin.math.sqrt

class SensorData(x: Float, y: Float, z: Float) {
    val x: Float = x
    val y: Float = y
    val z: Float = z
    val mag: Float = sqrt(x.pow(x) + y.pow(2) + z.pow(2))

    companion object {
        fun create(x: Float, y: Float, z: Float): SensorData {
            return SensorData(x, y, z)
        }
    }

    override fun toString(): String {
        return "%3.4f %3.4f %3.4f %3.4f\n".format(this.x, this.y, this.z, this.mag)
    }
}