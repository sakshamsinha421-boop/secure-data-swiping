package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DeviceEntity::class,
        WipeOperationEntity::class,
        CertificateEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SecuWipeDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun wipeOperationDao(): WipeOperationDao
    abstract fun certificateDao(): CertificateDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: SecuWipeDatabase? = null

        fun getDatabase(context: Context): SecuWipeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SecuWipeDatabase::class.java,
                    "secuwipe_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
