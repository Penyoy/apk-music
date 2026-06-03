package com.sonicplayer.util

import android.os.CountDownTimer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SleepTimerManager {
    private var timer: CountDownTimer? = null
    
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive
    
    private val _remainingMillis = MutableStateFlow(0L)
    val remainingMillis: StateFlow<Long> = _remainingMillis
    
    private val _remainingMinutes = MutableStateFlow(0)
    val remainingMinutes: StateFlow<Int> = _remainingMinutes

    fun startTimer(minutes: Int, onFinish: () -> Unit) {
        cancelTimer()
        
        val millis = minutes * 60 * 1000L
        _remainingMillis.value = millis
        _remainingMinutes.value = minutes
        _isActive.value = true
        
        timer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingMillis.value = millisUntilFinished
                _remainingMinutes.value = (millisUntilFinished / 1000 / 60).toInt()
            }

            override fun onFinish() {
                _isActive.value = false
                _remainingMillis.value = 0
                _remainingMinutes.value = 0
                onFinish()
            }
        }.start()
    }

    fun cancelTimer() {
        timer?.cancel()
        timer = null
        _isActive.value = false
        _remainingMillis.value = 0
        _remainingMinutes.value = 0
    }

    fun getRemainingTimeText(): String {
        val minutes = _remainingMillis.value / 1000 / 60
        val seconds = (_remainingMillis.value / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
