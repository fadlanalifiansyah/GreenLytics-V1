package com.example.greenlytics.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class DashboardViewModel : ViewModel() {

    // Tambahan : LiveData untuk menyimpan nama pengguna
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            // Mengambil nama depan saja agar tampilan lebih rapi
            val fullName = it.displayName ?: "Pengguna"
            val firstName = fullName.split(" ").firstOrNull() ?: "Pengguna"
            _userName.value = firstName
        }
    }
}