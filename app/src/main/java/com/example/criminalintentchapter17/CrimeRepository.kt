package com.example.criminalintentchapter17

import android.content.Context
import androidx.room.Room
import com.example.criminalintentchapter17.database.CrimeDatabase
import com.example.criminalintentchapter17.database.migration_1_2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(
    context: Context,
    private val coroutineScope: CoroutineScope = GlobalScope
){
    private val crimeDatabase = Room
        .databaseBuilder(context, CrimeDatabase::class.java, DATABASE_NAME)
        .addMigrations(migration_1_2)
        .build()

    fun getCrimes(): Flow<List<Crime>> = crimeDatabase.crimeDao().getCrimes()
    suspend fun getCrime(id: UUID) = crimeDatabase.crimeDao().getCrime(id)
    fun updateCrime(crime: Crime) {
        coroutineScope.launch {
            crimeDatabase.crimeDao().updateCrime(crime)
        }
    }
    suspend fun addCrime(crime: Crime) = crimeDatabase.crimeDao().addCrime(crime)

    companion object {
        private var INSTANCE: CrimeRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE==null) {
                INSTANCE = CrimeRepository(context.applicationContext)
            }
        }
        fun get(): CrimeRepository = INSTANCE ?:
        throw IllegalStateException("CrimeRepository must be initialized.")
    }
}