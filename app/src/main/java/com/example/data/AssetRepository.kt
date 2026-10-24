package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AssetRepository(
    private val db: SecuWipeDatabase
) {
    private val deviceDao = db.deviceDao()
    private val wipeDao = db.wipeOperationDao()
    private val certDao = db.certificateDao()
    private val auditDao = db.auditLogDao()

    val allDevices: Flow<List<DeviceEntity>> = deviceDao.getAllDevices()
    val allOperations: Flow<List<WipeOperationEntity>> = wipeDao.getAllOperations()
    val allCertificates: Flow<List<CertificateEntity>> = certDao.getAllCertificates()
    val allAuditLogs: Flow<List<AuditLogEntity>> = auditDao.getAllAuditLogs()

    suspend fun getDeviceByAssetTag(tag: String): DeviceEntity? = withContext(Dispatchers.IO) {
        deviceDao.getDeviceByAssetTag(tag)
    }

    suspend fun registerDevice(device: DeviceEntity): Result<Long> = withContext(Dispatchers.IO) {
        val existingCount = deviceDao.countByAssetTag(device.assetTag.trim())
        if (existingCount > 0) {
            return@withContext Result.failure(IllegalArgumentException("Asset Tag '${device.assetTag}' is already registered."))
        }
        val id = deviceDao.insertDevice(device)
        auditDao.insertLog(
            AuditLogEntity(
                assetTag = device.assetTag,
                action = "DEVICE_REGISTERED",
                operator = device.operatorName,
                timestamp = System.currentTimeMillis(),
                details = "Registered '${device.name}' (${device.deviceType}, ${device.capacity}) into disposition ledger."
            )
        )
        Result.success(id)
    }

    suspend fun updateDevice(device: DeviceEntity) = withContext(Dispatchers.IO) {
        deviceDao.updateDevice(device)
    }

    suspend fun deleteDevice(tag: String) = withContext(Dispatchers.IO) {
        deviceDao.deleteDeviceByAssetTag(tag)
        auditDao.insertLog(
            AuditLogEntity(
                assetTag = tag,
                action = "DEVICE_DELETED",
                operator = "System / User",
                timestamp = System.currentTimeMillis(),
                details = "Asset record $tag deleted from database."
            )
        )
    }

    suspend fun recordWipeOperation(operation: WipeOperationEntity) = withContext(Dispatchers.IO) {
        wipeDao.insertOperation(operation)
        val dev = deviceDao.getDeviceByAssetTag(operation.assetTag)
        if (dev != null) {
            val updatedStatus = if (operation.resultStatus == "PASS") "WIPED" else "HOLD_REVIEW"
            val verificationStatus = if (operation.resultStatus == "PASS") "PASS" else "HOLD_REVIEW"
            deviceDao.updateDevice(
                dev.copy(
                    status = updatedStatus,
                    lastWipeMethod = operation.methodCode,
                    lastWipeTimestamp = operation.endTime,
                    verificationStatus = verificationStatus
                )
            )
        }
        auditDao.insertLog(
            AuditLogEntity(
                assetTag = operation.assetTag,
                action = "WIPE_COMPLETED_${operation.resultStatus}",
                operator = "System Wiping Engine",
                timestamp = operation.endTime,
                details = "Completed ${operation.methodTitle} (${operation.passesCompleted}/${operation.passesConfigured} passes). Status: ${operation.resultStatus}."
            )
        )
    }

    suspend fun recordCertificate(cert: CertificateEntity) = withContext(Dispatchers.IO) {
        certDao.insertCertificate(cert)
        val dev = deviceDao.getDeviceByAssetTag(cert.assetTag)
        if (dev != null) {
            deviceDao.updateDevice(
                dev.copy(
                    certificateId = cert.certificateId,
                    status = "VERIFIED_PASS",
                    verificationStatus = "PASS"
                )
            )
        }
        auditDao.insertLog(
            AuditLogEntity(
                assetTag = cert.assetTag,
                action = "CERTIFICATE_ISSUED",
                operator = cert.operatorName,
                timestamp = cert.issuedTimestamp,
                details = "Sanitization certificate ${cert.certificateId} generated. Certified ready for recycling."
            )
        )
    }

    suspend fun addAuditLog(assetTag: String, action: String, operator: String, details: String) =
        withContext(Dispatchers.IO) {
            auditDao.insertLog(
                AuditLogEntity(
                    assetTag = assetTag,
                    action = action,
                    operator = operator,
                    timestamp = System.currentTimeMillis(),
                    details = details
                )
            )
        }

    suspend fun seedSampleDataIfEmpty() = withContext(Dispatchers.IO) {
        val count = deviceDao.countByAssetTag("CLG-IT-2026-001")
        if (count == 0) {
            deviceDao.insertAll(SampleData.sampleDevices)
            certDao.insertAll(SampleData.sampleCertificates)
            auditDao.insertAll(SampleData.sampleAuditLogs)
        }
    }

    suspend fun resetToSampleData() = withContext(Dispatchers.IO) {
        deviceDao.deleteAllDevices()
        wipeDao.deleteAllOperations()
        certDao.deleteAllCertificates()
        auditDao.deleteAllLogs()

        deviceDao.insertAll(SampleData.sampleDevices)
        certDao.insertAll(SampleData.sampleCertificates)
        auditDao.insertAll(SampleData.sampleAuditLogs)
    }
}
