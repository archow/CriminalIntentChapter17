package com.example.criminalintentchapter17.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.criminalintentchapter17.Crime
import com.example.criminalintentchapter17.CrimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CrimeListViewModel : ViewModel(){
    private val crimeRepo = CrimeRepository.get()
    private val _crimes: MutableStateFlow<List<Crime>> = MutableStateFlow(emptyList())
    val crimes = _crimes.asStateFlow()

    init {
        viewModelScope.launch {
            crimeRepo.getCrimes().collect {
                _crimes.value = it
            }
        }
    }

    suspend fun addCrime(crime: Crime) {
        crimeRepo.addCrime(crime)
    }
}