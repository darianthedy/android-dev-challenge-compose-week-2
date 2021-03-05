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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainViewModel(private val timer: Timer) : ViewModel() {

    private val _changeFocusToHours = MutableLiveData(false)
    val changeFocusToHours: LiveData<Boolean> = _changeFocusToHours

    private val _hoursText = MutableLiveData("")
    val hoursText: LiveData<String> = _hoursText

    private val _changeFocusToMinutes = MutableLiveData(false)
    val changeFocusToMinutes: LiveData<Boolean> = _changeFocusToMinutes

    private val _minutesText = MutableLiveData("")
    val minutesText: LiveData<String> = _minutesText

    private val _changeFocusToSeconds = MutableLiveData(false)
    val changeFocusToSeconds: LiveData<Boolean> = _changeFocusToSeconds

    private val _secondsText = MutableLiveData("")
    val secondsText: LiveData<String> = _secondsText

    private val _progress = MutableLiveData(0f)
    val progress: LiveData<Float> = _progress

    private val _userTimerInput = MutableLiveData("")

    private val _isPauseStopVisible = MutableLiveData(false)
    val isPauseStopVisible: LiveData<Boolean> = _isPauseStopVisible

    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = _isRunning

    private val _remainingTime = MutableLiveData(0f)
    val remainingTime: LiveData<Float> = _remainingTime

    private var currentProgress = 0f
    private var countingJob: Job? = null

    fun onViewCreated() {
        this._changeFocusToHours.value = false
        this._changeFocusToMinutes.value = false
        this._changeFocusToSeconds.value = false
    }

    fun onInputHours(hoursText: String) {
        if (hoursText.length <= 2)
            this._hoursText.value = hoursText

        if (hoursText.length >= 2)
            this._changeFocusToMinutes.value = true
    }

    fun onInputMinutes(minutesText: String) {
        if (minutesText.length <= 2)
            this._minutesText.value = minutesText

        if (minutesText.isEmpty())
            this._changeFocusToHours.value = true

        if (minutesText.length >= 2)
            this._changeFocusToSeconds.value = true
    }

    fun onInputSeconds(secondsText: String) {
        if (secondsText.length <= 2)
            this._secondsText.value = secondsText

        if (secondsText.isEmpty())
            this._changeFocusToMinutes.value = true
    }

    fun onTimerButtonPressed() {
        _isPauseStopVisible.value = true

        if (_isRunning.value == false) {
            _isRunning.value = true

            if (currentProgress == 0f) {
                _progress.value = 0f
            }

            val userTimerHours = _hoursText.value?.replace("[\\D]", "") ?: "0"
            val userTimerMinutes = _minutesText.value?.replace("[\\D]", "") ?: "0"
            val userTimerSeconds = _secondsText.value?.replace("[\\D]", "") ?: "0"
            val timerInSeconds =
                ((userTimerHours.toFloatOrNull() ?: 0f) * 60 * 60) +
                    ((userTimerMinutes.toFloatOrNull() ?: 0f) * 60) +
                    (userTimerSeconds.toFloatOrNull() ?: 0f)

            val remaining = timerInSeconds - ((_progress.value ?: 0f) * timerInSeconds)

            countingJob = viewModelScope.launch {
                timer.executeTimer(remaining * 1_000, getTimerUpdates(timerInSeconds))

                if (_progress.value ?: 0f >= 1f) {
                    _isPauseStopVisible.value = false
                    currentProgress = 0f
                    countingJob?.cancel()
                    _isRunning.value = false
                }
            }
        } else {
            currentProgress = _progress.value ?: 0f
            countingJob?.cancel()
            _isRunning.value = false
        }
    }

    private fun getTimerUpdates(timerInSeconds: Float) = object : TimerUpdates {
        override fun onProgressUpdated(timeElapsedSinceStart: Long) {
            _progress.value = currentProgress + ((timeElapsedSinceStart / timerInSeconds) / 1_000)
            _remainingTime.value = (timerInSeconds - ((_progress.value ?: 0f) * timerInSeconds)) * 1_000
        }
    }

    fun onStopButtonPressed() {
        _progress.value = 0f
        _remainingTime.value = 0f
        _isPauseStopVisible.value = false
        currentProgress = 0f
        countingJob?.cancel()
        _isRunning.value = false
    }
}
