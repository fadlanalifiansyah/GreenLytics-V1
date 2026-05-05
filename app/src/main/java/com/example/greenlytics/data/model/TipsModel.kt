package com.example.greenlytics.data.model

data class TipsModel(
    val id: Int,
    val kategori: String,
    val subKategori: String,
    val judul: String,
    val deskripsi: String,
    val sumber: String,
    val difficulty: String,
    var isCompleted: Boolean = false // Tambahan untuk melacak status tombol
)