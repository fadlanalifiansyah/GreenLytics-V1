package com.example.greenlytics.ui.tips

import androidx.lifecycle.*
import com.example.greenlytics.data.model.TipsModel
import com.example.greenlytics.data.repository.EmissionRepo
import com.example.greenlytics.utils.TipsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TipsViewModel @Inject constructor(private val repository: EmissionRepo) : ViewModel() {

    private val _tipsList = MutableLiveData<List<TipsModel>>()
    val tipsList: LiveData<List<TipsModel>> = _tipsList

    private val _potentialSaving = MutableLiveData<String>()
    val potentialSaving: LiveData<String> = _potentialSaving

    fun loadContent() {
        viewModelScope.launch {
            val emissions = repository.getAllEmissions()
            _tipsList.postValue(TipsProvider.getRelevantTips(emissions))

            val total = emissions.sumOf { it.emisiKarbon }
            val saving = String.format("%.1f", total * 0.15) // Potensi hemat 15%[cite: 1]
            _potentialSaving.postValue("$saving kg CO₂")
        }
    }
}