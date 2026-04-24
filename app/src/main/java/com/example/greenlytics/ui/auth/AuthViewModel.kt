package com.example.greenlytics.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.greenlytics.data.repository.AuthRepository
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository: AuthRepository = AuthRepository.getInstance()

    private val _loginResult = MutableLiveData<Result<FirebaseUser?>>()
    val loginResult: LiveData<Result<FirebaseUser?>> = _loginResult

    fun loginWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            val result = repository.signInWithGoogle(credential)
            _loginResult.postValue(result)
        }
    }
}