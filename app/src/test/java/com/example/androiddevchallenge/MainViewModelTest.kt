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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testCoroutineDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testCoroutineDispatcher.cleanupTestCoroutines()
    }

    private val timer = mockk<Timer>(relaxed = true)
    private val mainViewModel = MainViewModel(timer)

    @Test
    fun `input hours one digit`() {
        mainViewModel.onInputHours("1")

        assertEquals("1", mainViewModel.hoursText.getOrAwaitValue())
        assertEquals(false, mainViewModel.changeFocusToMinutes.getOrAwaitValue())
    }

    @Test
    fun `input hours two digit`() {
        mainViewModel.onInputHours("9")
        mainViewModel.onInputHours("99")

        assertEquals("99", mainViewModel.hoursText.getOrAwaitValue())
        assertEquals(true, mainViewModel.changeFocusToMinutes.getOrAwaitValue())
    }

    @Test
    fun `input hours three digit`() {
        mainViewModel.onInputHours("9")
        mainViewModel.onInputHours("99")
        mainViewModel.onInputHours("999")

        assertEquals("99", mainViewModel.hoursText.getOrAwaitValue())
        assertEquals(true, mainViewModel.changeFocusToMinutes.getOrAwaitValue())
    }

    @Test
    fun `input minutes one digit`() {
        mainViewModel.onInputMinutes("1")

        assertEquals("1", mainViewModel.minutesText.getOrAwaitValue())
        assertEquals(false, mainViewModel.changeFocusToSeconds.getOrAwaitValue())
    }

    @Test
    fun `input minutes two digit`() {
        mainViewModel.onInputMinutes("9")
        mainViewModel.onInputMinutes("99")

        assertEquals("99", mainViewModel.minutesText.getOrAwaitValue())
        assertEquals(true, mainViewModel.changeFocusToSeconds.getOrAwaitValue())
    }

    @Test
    fun `input minutes three digit`() {
        mainViewModel.onInputMinutes("9")
        mainViewModel.onInputMinutes("99")
        mainViewModel.onInputMinutes("999")

        assertEquals("99", mainViewModel.minutesText.getOrAwaitValue())
        assertEquals(true, mainViewModel.changeFocusToSeconds.getOrAwaitValue())
    }

    @Test
    fun `input minutes empty`() {
        mainViewModel.onInputMinutes("")

        assertEquals("", mainViewModel.minutesText.getOrAwaitValue())
        assertEquals(true, mainViewModel.changeFocusToHours.getOrAwaitValue())
    }

    @Test
    fun `input seconds one digit`() {
        mainViewModel.onInputSeconds("1")

        assertEquals("1", mainViewModel.secondsText.getOrAwaitValue())
    }

    @Test
    fun `input seconds two digit`() {
        mainViewModel.onInputSeconds("9")
        mainViewModel.onInputSeconds("99")

        assertEquals("99", mainViewModel.secondsText.getOrAwaitValue())
    }

    @Test
    fun `input seconds three digit`() {
        mainViewModel.onInputSeconds("9")
        mainViewModel.onInputSeconds("99")
        mainViewModel.onInputSeconds("999")

        assertEquals("99", mainViewModel.secondsText.getOrAwaitValue())
    }

    @Test
    fun `input seconds empty`() {
        mainViewModel.onInputSeconds("")

        assertEquals("", mainViewModel.secondsText.getOrAwaitValue())
        assertEquals(true, mainViewModel.changeFocusToMinutes.getOrAwaitValue())
    }

    @Test
    fun `onViewCreated after modifying input time`() {
        mainViewModel.onInputHours("99")
        mainViewModel.onInputMinutes("99")
        mainViewModel.onInputMinutes("")
        mainViewModel.onViewCreated()

        assertEquals(false, mainViewModel.changeFocusToHours.getOrAwaitValue())
        assertEquals(false, mainViewModel.changeFocusToMinutes.getOrAwaitValue())
        assertEquals(false, mainViewModel.changeFocusToSeconds.getOrAwaitValue())
    }

    @Test
    fun startTimer_ImmediatelyFinish() {
        val inputTimerInMs = (99 * 60 * 60 + 99 * 60 + 99) * 1_000
        coEvery {
            timer.executeTimer(inputTimerInMs.toFloat(), any())
        } answers {
            secondArg<TimerUpdates>().onProgressUpdated(inputTimerInMs.toLong())
        }

        mainViewModel.onInputHours("99")
        mainViewModel.onInputMinutes("99")
        mainViewModel.onInputSeconds("99")
        mainViewModel.onTimerButtonPressed()

        assertEquals(1f, mainViewModel.progress.getOrAwaitValue())
        assertEquals(false, mainViewModel.isPauseStopVisible.getOrAwaitValue())
        assertEquals(0f, mainViewModel.remainingTime.getOrAwaitValue())
    }

    @Test
    fun startTimer_UpdatedFrequently() {
        coEvery {
            timer.executeTimer(500_000f, any())
        } answers {
            secondArg<TimerUpdates>().onProgressUpdated(100_000)
            secondArg<TimerUpdates>().onProgressUpdated(200_000)
        }

        mainViewModel.onInputMinutes("8")
        mainViewModel.onInputSeconds("20")
        mainViewModel.onTimerButtonPressed()

        assertEquals(0.4f, mainViewModel.progress.getOrAwaitValue())
        assertEquals(true, mainViewModel.isPauseStopVisible.getOrAwaitValue())
        assertEquals(true, mainViewModel.isRunning.getOrAwaitValue())
        assertEquals(300_000f, mainViewModel.remainingTime.getOrAwaitValue())
    }

    @Test
    fun pauseTimer() {
        coEvery {
            timer.executeTimer(500_000f, any())
        } answers {
            secondArg<TimerUpdates>().onProgressUpdated(100_000)
            secondArg<TimerUpdates>().onProgressUpdated(200_000)
        }

        mainViewModel.onInputMinutes("8")
        mainViewModel.onInputSeconds("20")
        mainViewModel.onTimerButtonPressed() // start
        mainViewModel.onTimerButtonPressed() // pause

        assertEquals(0.4f, mainViewModel.progress.getOrAwaitValue())
        assertEquals(true, mainViewModel.isPauseStopVisible.getOrAwaitValue())
        assertEquals(false, mainViewModel.isRunning.getOrAwaitValue())
        assertEquals(300_000f, mainViewModel.remainingTime.getOrAwaitValue())
    }

    @Test
    fun resumeTimer() {
        coEvery {
            timer.executeTimer(500_000f, any())
        } answers {
            secondArg<TimerUpdates>().onProgressUpdated(100_000)
            secondArg<TimerUpdates>().onProgressUpdated(200_000)
        }

        mainViewModel.onInputMinutes("8")
        mainViewModel.onInputSeconds("20")
        mainViewModel.onTimerButtonPressed() // start
        mainViewModel.onTimerButtonPressed() // pause

        coEvery {
            timer.executeTimer(300_000f, any())
        } answers {
            secondArg<TimerUpdates>().onProgressUpdated(100_000)
            secondArg<TimerUpdates>().onProgressUpdated(200_000)
            secondArg<TimerUpdates>().onProgressUpdated(300_000)
        }
        mainViewModel.onTimerButtonPressed() // resume

        assertEquals(1f, mainViewModel.progress.getOrAwaitValue())
        assertEquals(false, mainViewModel.isPauseStopVisible.getOrAwaitValue())
        assertEquals(0f, mainViewModel.remainingTime.getOrAwaitValue())
    }

    @Test
    fun stopTimer() {
        coEvery {
            timer.executeTimer(500_000f, any())
        } answers {
            secondArg<TimerUpdates>().onProgressUpdated(100_000)
            secondArg<TimerUpdates>().onProgressUpdated(100_000)
        }

        mainViewModel.onInputMinutes("8")
        mainViewModel.onInputSeconds("20")
        mainViewModel.onTimerButtonPressed()
        mainViewModel.onStopButtonPressed()

        assertEquals(0f, mainViewModel.progress.getOrAwaitValue())
        assertEquals(false, mainViewModel.isPauseStopVisible.getOrAwaitValue())
        assertEquals(0f, mainViewModel.remainingTime.getOrAwaitValue())
    }

    @Test
    fun restartTimer() {
        coEvery {
            timer.executeTimer(any(), any())
        } answers {
            secondArg<TimerUpdates>().onProgressUpdated(100_000)
            secondArg<TimerUpdates>().onProgressUpdated(200_000)
        }

        mainViewModel.onInputMinutes("8")
        mainViewModel.onInputSeconds("20")
        mainViewModel.onTimerButtonPressed()
        mainViewModel.onStopButtonPressed()
        mainViewModel.onInputMinutes("3")
        mainViewModel.onInputSeconds("20")
        mainViewModel.onTimerButtonPressed()

        assertEquals(1f, mainViewModel.progress.getOrAwaitValue())
        assertEquals(false, mainViewModel.isPauseStopVisible.getOrAwaitValue())
        assertEquals(0f, mainViewModel.remainingTime.getOrAwaitValue())
    }

    @Test
    fun startAgainAfterFinish() {
        coEvery {
            timer.executeTimer(500_000f, any())
        } answers {
            secondArg<TimerUpdates>().onProgressUpdated(200_000)
            secondArg<TimerUpdates>().onProgressUpdated(500_000)
        }

        mainViewModel.onInputMinutes("8")
        mainViewModel.onInputSeconds("20")
        mainViewModel.onTimerButtonPressed()
        mainViewModel.onInputMinutes("3")
        mainViewModel.onInputSeconds("20")

        coEvery {
            timer.executeTimer(200_000f, any())
        } answers {
            secondArg<TimerUpdates>().onProgressUpdated(100_000)
        }

        mainViewModel.onTimerButtonPressed()

        assertEquals(0.5f, mainViewModel.progress.getOrAwaitValue())
        assertEquals(true, mainViewModel.isPauseStopVisible.getOrAwaitValue())
        assertEquals(true, mainViewModel.isRunning.getOrAwaitValue())
        assertEquals(100_000f, mainViewModel.remainingTime.getOrAwaitValue())
    }
}
