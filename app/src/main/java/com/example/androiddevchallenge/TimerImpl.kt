package com.example.androiddevchallenge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface Timer {
    suspend fun executeTimer(duration: Float, timerUpdates: TimerUpdates)
}

interface TimerUpdates {
    fun onProgressUpdated(timeElapsedSinceStart: Long)
}

class TimerImpl : Timer {

    private var progress = 0f

    override suspend fun executeTimer(duration: Float, timerUpdates: TimerUpdates) {
        progress = 0f

        withContext(Dispatchers.Default) {
            val startTime = System.currentTimeMillis()

            while(progress <= 1f) {
                val timeElapsedSinceStart = System.currentTimeMillis() - startTime
                val progressDone = timeElapsedSinceStart / duration

                withContext(Dispatchers.Main) {
                    progress = progressDone
                    timerUpdates.onProgressUpdated(timeElapsedSinceStart)
                }
            }
        }
    }
}