package com.gaitvision.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Patient::class, Video::class, GaitScore::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // abstract fun patientDao(): PatientDao
    // abstract fun videoDao(): VideoDao
    // abstract fun gaitScoreDao(): GaitScoreDao
}
