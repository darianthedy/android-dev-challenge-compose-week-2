package com.example.androiddevchallenge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainViewModel(private val timer: Timer): ViewModel() {

    private val _progress = MutableLiveData(0f)
    val progress: LiveData<Float> = _progress

    private val _userTimerInput = MutableLiveData("")
    val userTimerInput: LiveData<String> = _userTimerInput

    private val _isPauseStopVisible = MutableLiveData(false)
    val isPauseStopVisible: LiveData<Boolean> = _isPauseStopVisible

    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = _isRunning

    private val _remainingTime = MutableLiveData(0f)
    val remainingTime: LiveData<Float> = _remainingTime

    private var currentProgress = 0f
    private var countingJob: Job? = null

    fun onUserTimerChanged(input: String) {
        _userTimerInput.value = input
    }

    fun onTimerButtonPressed() {
        _isPauseStopVisible.value = true

        if (_isRunning.value == false) {
            _isRunning.value = true
            if (currentProgress == 0f) {
                _progress.value = 0f
            }

            val userTimerString = _userTimerInput.value?.replace("[\\D]", "") ?: "0"
            val userTimer = (userTimerString.toFloatOrNull() ?: 0f)
            val remaining = userTimer - ((_progress.value ?: 0f) * userTimer)
            countingJob = viewModelScope.launch {
                timer.executeTimer(remaining * 1_000, getTimerUpdates(userTimer))

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

    private fun getTimerUpdates(userTimer: Float) = object : TimerUpdates {
        override fun onProgressUpdated(timeElapsedSinceStart: Long) {
            _progress.value = currentProgress + ((timeElapsedSinceStart / userTimer) / 1_000)
            _remainingTime.value = (userTimer - ((_progress.value ?: 0f) * userTimer)) * 1_000
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