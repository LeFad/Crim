package com.valeria.criminalintent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.io.File
import java.util.*

class CrimeDetailViewModel: ViewModel() {

    private val crimeRepository = CrimeRepository.get()
    private var crimeIdLiveData = MutableLiveData<UUID>() // хранит идентификатор отображаемый в дан момент перступления фрагментом КрФр

    var crimeLiveData: LiveData<Crime?> = Transformations.switchMap(crimeIdLiveData) { // проеобраз данных в реал времени "триггер-ответ"
        crimeId -> crimeRepository.getCrime(crimeId)
    }

    fun loadCrime(crimeId: UUID){
        crimeIdLiveData.value = crimeId // какое преступл нужно загрузить
    }

    fun  saveCrime(crime: Crime){
        crimeRepository.updateCrime(crime)
    }

    fun getPhotoFile(crime: Crime):File{
        return crimeRepository.getPhotoFile(crime)
    }
}