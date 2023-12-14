package com.example.criminalintentchapter17.database

import androidx.room.TypeConverter
import java.util.Date

class CrimeTypeConverters {
    @TypeConverter
    fun fromDate(date: Date): Long = date.time

    @TypeConverter
    fun toDate(millisSinceEpoch: Long): Date = Date(millisSinceEpoch)
}