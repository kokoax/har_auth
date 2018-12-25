package com.example.sweet.har_auth

class SensorQueue(size: Int, q: Array<SensorData>) {
    val size: Int = size
    private var tail: Int = 0
    private var q: Array<SensorData> = q
    companion object {
        private fun initQueue(size: Int): Array<SensorData> {
            return Array(size, {SensorData.create(0F, 0F, 0F) })
        }

        fun create(size: Int): SensorQueue {
            return SensorQueue(size,  SensorQueue.initQueue(size))
        }
    }

    fun all(): Array<SensorData> {
        return this.q
    }

    fun sorted(): Array<SensorData> {
        var newArray = SensorQueue.initQueue(this.size)
        var pos = 0
        (this.tail..this.size-1).forEach {
            newArray[pos++] = this.q[it]
        }
        (0..this.tail-1).forEach {
            newArray[pos++] = this.q[it]
        }
        return newArray
    }

    fun enqueue(x: Float, y: Float, z: Float) {
        this.q[this.tail] = SensorData.create(x, y, z)
        this.tail++
        if(this.tail >= this.size) {
            this.tail = 0
        }
    }
}