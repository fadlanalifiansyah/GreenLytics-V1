package com.example.greenlytics.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Untuk viewModelScope
import kotlinx.coroutines.delay         // Untuk delay
import kotlinx.coroutines.launch        // Untuk launch

class AuthViewModel : ViewModel() {
    private val _shouldNavigate = MutableLiveData<Boolean>()
    val shouldNavigate: LiveData<Boolean> get() = _shouldNavigate

    init {
        // Logika timer dipisah di sini (tidak di Fragment)
        viewModelScope.launch {
            delay(3000)
            _shouldNavigate.value = true
        }
    }
    fun onNavigated() {
        _shouldNavigate.value = false
    }
}