package com.example.greenlytics.ui.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    private val _navigateToWelcome = MutableLiveData<Boolean>()
    val navigateToWelcome: LiveData<Boolean> get() = _navigateToWelcome

    fun startSplashScreenTimer() {
        viewModelScope.launch {
            delay(3000) // Menunggu 3 detik
            _navigateToWelcome.value = true
        }
    }
}