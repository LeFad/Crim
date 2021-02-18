package com.valeria.criminalintent

import androidx.lifecycle.ViewModel

class CrimeListViewModel:ViewModel() {
//2//
    private val crimeRepository = CrimeRepository.get()
    val crimeListLiveData = crimeRepository.getCrimes()

    fun addCrime(crime: Crime){
        crimeRepository.addCrime(crime)
    }

    fun deleteCrime(crime: Crime){
        crimeRepository.deleteCrime(crime)
    }

//1//
//    val crimes = mutableListOf<Crime>()
//
//    init {
//        for (i in 0 until 100){
//            val crime = Crime()
//            crime.title = "Crime #$i"
//            crime.isSolved = i % 2 == 0
//            crimes +=crime
//        }
//    }
}