package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY registrationDate DESC")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE assetTag = :tag LIMIT 1")
    suspend fun getDeviceByAssetTag(tag: String): DeviceEntity?

    @Query("SELECT COUNT(*) FROM devices WHERE assetTag = :tag")
    suspend fun countByAssetTag(tag: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(devices: List<DeviceEntity>)

    @Update
    suspend fun updateDevice(device: DeviceEntity)

    @Query("DELETE FROM devices WHERE assetTag = :tag")
    suspend fun deleteDeviceByAssetTag(tag: String)

    @Query("DELETE FROM devices")
    suspend fun deleteAllDevices()
}

@Dao
interface WipeOperationDao {
    @Query("SELECT * FROM wipe_operations ORDER BY startTime DESC")
    fun getAllOperations(): Flow<List<WipeOperationEntity>>

    @Query("SELECT * FROM wipe_operations WHERE assetTag = :tag ORDER BY startTime DESC")
    fun getOperationsForDevice(tag: String): Flow<List<WipeOperationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(op: WipeOperationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ops: List<WipeOperationEntity>)

    @Query("DELETE FROM wipe_operations")
    suspend fun deleteAllOperations()
}

@Dao
interface CertificateDao {
    @Query("SELECT * FROM certificates ORDER BY issuedTimestamp DESC")
    fun getAllCertificates(): Flow<List<CertificateEntity>>

    @Query("SELECT * FROM certificates WHERE certificateId = :id LIMIT 1")
    suspend fun getCertificateById(id: String): CertificateEntity?

    @Query("SELECT * FROM certificates WHERE assetTag = :tag LIMIT 1")
    suspend fun getCertificateByAssetTag(tag: String): CertificateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(cert: CertificateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(certs: List<CertificateEntity>)

    @Query("DELETE FROM certificates WHERE certificateId = :id")
    suspend fun deleteCertificate(id: String)

    @Query("DELETE FROM certificates")
    suspend fun deleteAllCertificates()
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE assetTag = :tag ORDER BY timestamp DESC")
    fun getLogsForDevice(tag: String): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<AuditLogEntity>)

    @Query("DELETE FROM audit_logs")
    suspend fun deleteAllLogs()
}
