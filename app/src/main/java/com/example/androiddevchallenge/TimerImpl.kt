/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

            while (progress <= 1f) {
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
