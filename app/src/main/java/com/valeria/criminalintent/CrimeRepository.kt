package com.valeria.criminalintent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.valeria.criminalintent.database.CrimeDatabase
import com.valeria.criminalintent.database.migration_1_2
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executors



private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context){

//2//
    private val database : CrimeDatabase = Room.databaseBuilder(//Создает конкретную реализацию абстрактного класса
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_1_2).build()

    private val crimeDao = database.crimeDao()
    private val executor = Executors.newSingleThreadExecutor() // вставка и обновлн с пом исполнителя; добавл св-во исп для хран ссылки, выполн функции встаки и обновл с пом исполнителя

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()
    fun getCrime(id:UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    fun updateCrime(crime: Crime){
        executor.execute{
            crimeDao.updateCrime(crime)
        }
    }

    fun addCrime(crime: Crime){
        executor.execute{
            crimeDao.addCrime(crime)
        }
    }

    fun deleteCrime(crime: Crime){
        executor.execute {
            crimeDao.deleteCrime(crime)
        }
    }

 //1//
    companion object{
        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context){
            if (INSTANCE==null){
                INSTANCE = CrimeRepository(context)
            }
        }

        fun get(): CrimeRepository{
            return INSTANCE?:
            throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}