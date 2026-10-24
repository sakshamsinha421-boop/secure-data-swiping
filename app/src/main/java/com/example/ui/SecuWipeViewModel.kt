package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AssetRepository
import com.example.data.CertificateEntity
import com.example.data.DeviceCondition
import com.example.data.DeviceEntity
import com.example.data.DeviceType
import com.example.data.SanitizationMethod
import com.example.data.SecuWipeDatabase
import com.example.data.VerificationStatus
import com.example.data.WipeOperationEntity
import com.example.engine.WipingEngine
import com.example.engine.WipingSessionState
import com.example.util.PdfCertificateGenerator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.security.MessageDigest
import java.util.UUID

enum class AppDestination(val title: String, val icon: String) {
    DASHBOARD("Dashboard", "Dashboard"),
    REGISTER_DEVICE("Register Device", "AddCircle"),
    DATA_WIPING("Data Wiping", "DeleteSweep"),
    VERIFICATION("Verification", "FactCheck"),
    CERTIFICATES("Certificates", "Verified"),
    DEVICE_HISTORY("Device History", "History"),
    SETTINGS("Settings & Standards", "Settings")
}

data class RegistrationFormState(
    val assetTag: String = "",
    val name: String = "",
    val deviceType: DeviceType = DeviceType.LAPTOP,
    val capacity: String = "512 GB",
    val manufacturer: String = "",
    val model: String = "",
    val condition: DeviceCondition = DeviceCondition.WORKING,
    val operatorName: String = "Lab Engineer (Admin)",
    val notes: String = "",
    val assetTagError: String? = null,
    val nameError: String? = null,
    val capacityError: String? = null,
    val isSubmitting: Boolean = false,
    val successMessage: String? = null
)

class SecuWipeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SecuWipeDatabase.getDatabase(application)
    private val repository = AssetRepository(db)

    val wipingEngine = WipingEngine(viewModelScope)
    val wipingSessionState: StateFlow<WipingSessionState> = wipingEngine.sessionState

    // Navigation
    private val _currentDestination = MutableStateFlow(AppDestination.DASHBOARD)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    // Database reactive streams
    val devices: StateFlow<List<DeviceEntity>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val operations: StateFlow<List<WipeOperationEntity>> = repository.allOperations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val certificates: StateFlow<List<CertificateEntity>> = repository.allCertificates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<com.example.data.AuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Registration form state
    private val _formState = MutableStateFlow(RegistrationFormState())
    val formState: StateFlow<RegistrationFormState> = _formState.asStateFlow()

    // History Search & Filters
    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _historyTypeFilter = MutableStateFlow("ALL")
    val historyTypeFilter: StateFlow<String> = _historyTypeFilter.asStateFlow()

    private val _historyStatusFilter = MutableStateFlow("ALL")
    val historyStatusFilter: StateFlow<String> = _historyStatusFilter.asStateFlow()

    // Filtered devices for history list
    val filteredDevices: StateFlow<List<DeviceEntity>> = combine(
        devices,
        historySearchQuery,
        historyTypeFilter,
        historyStatusFilter
    ) { list, query, type, status ->
        list.filter { device ->
            val matchesQuery = query.isBlank() ||
                device.assetTag.contains(query, ignoreCase = true) ||
                device.name.contains(query, ignoreCase = true) ||
                device.manufacturer.contains(query, ignoreCase = true) ||
                device.operatorName.contains(query, ignoreCase = true)

            val matchesType = type == "ALL" || device.deviceType == type
            val matchesStatus = status == "ALL" || device.status.equals(status, ignoreCase = true)

            matchesQuery && matchesType && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Verification Selection
    private val _selectedVerifyDeviceTag = MutableStateFlow<String?>(null)
    val selectedVerifyDeviceTag: StateFlow<String?> = _selectedVerifyDeviceTag.asStateFlow()

    // Toast/Alert UI Events
    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent.asSharedFlow()

    init {
        // Pre-seed sample data on first run
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
        }
    }

    fun navigateTo(destination: AppDestination) {
        _currentDestination.value = destination
    }

    // Form handlers
    fun onAssetTagChanged(value: String) {
        _formState.update { it.copy(assetTag = value, assetTagError = null, successMessage = null) }
    }

    fun onNameChanged(value: String) {
        _formState.update { it.copy(name = value, nameError = null) }
    }

    fun onDeviceTypeChanged(value: DeviceType) {
        _formState.update { it.copy(deviceType = value) }
    }

    fun onCapacityChanged(value: String) {
        _formState.update { it.copy(capacity = value, capacityError = null) }
    }

    fun onManufacturerChanged(value: String) {
        _formState.update { it.copy(manufacturer = value) }
    }

    fun onModelChanged(value: String) {
        _formState.update { it.copy(model = value) }
    }

    fun onConditionChanged(value: DeviceCondition) {
        _formState.update { it.copy(condition = value) }
    }

    fun onOperatorNameChanged(value: String) {
        _formState.update { it.copy(operatorName = value) }
    }

    fun onNotesChanged(value: String) {
        _formState.update { it.copy(notes = value) }
    }

    fun registerDevice() {
        val current = _formState.value
        var hasError = false

        if (current.assetTag.isBlank()) {
            _formState.update { it.copy(assetTagError = "Asset Tag / Record ID is required.") }
            hasError = true
        }
        if (current.name.isBlank()) {
            _formState.update { it.copy(nameError = "Device Name is required.") }
            hasError = true
        }
        if (current.capacity.isBlank()) {
            _formState.update { it.copy(capacityError = "Storage capacity is required.") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _formState.update { it.copy(isSubmitting = true) }
            val newDevice = DeviceEntity(
                assetTag = current.assetTag.trim().uppercase(),
                name = current.name.trim(),
                deviceType = current.deviceType.name,
                capacity = current.capacity.trim(),
                manufacturer = current.manufacturer.ifBlank { "Generic" }.trim(),
                model = current.model.ifBlank { "N/A" }.trim(),
                condition = current.condition.name,
                operatorName = current.operatorName.ifBlank { "Operator" }.trim(),
                registrationDate = System.currentTimeMillis(),
                status = "REGISTERED",
                notes = current.notes.trim(),
                verificationStatus = VerificationStatus.PENDING.name
            )

            val result = repository.registerDevice(newDevice)
            if (result.isSuccess) {
                _formState.update {
                    RegistrationFormState(
                        successMessage = "Device ${newDevice.assetTag} registered successfully!"
                    )
                }
                _uiEvent.emit("Device ${newDevice.assetTag} registered successfully.")
            } else {
                _formState.update {
                    it.copy(
                        isSubmitting = false,
                        assetTagError = result.exceptionOrNull()?.message ?: "Failed to register device."
                    )
                }
            }
        }
    }

    // Wiping workflow
    fun selectDeviceForWipe(device: DeviceEntity) {
        wipingEngine.selectDevice(device)
        _currentDestination.value = AppDestination.DATA_WIPING
    }

    fun startWipingOperation() {
        wipingEngine.startWipe { completedState ->
            viewModelScope.launch {
                val dev = completedState.selectedDevice ?: return@launch
                val op = WipeOperationEntity(
                    operationId = "OP-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
                    assetTag = dev.assetTag,
                    methodCode = completedState.selectedMethod.code,
                    methodTitle = completedState.selectedMethod.title,
                    standardName = completedState.selectedMethod.standard,
                    passesConfigured = completedState.totalPasses,
                    passesCompleted = completedState.currentPass,
                    startTime = completedState.startTime,
                    endTime = completedState.endTime,
                    throughputMBps = 320.0,
                    verificationHash = completedState.verificationHash,
                    isDemo = completedState.isDemoMode,
                    resultStatus = "PASS",
                    auditLog = completedState.terminalLogs.joinToString("\n")
                )
                repository.recordWipeOperation(op)
                _uiEvent.emit("Sanitization completed with status PASS.")
            }
        }
    }

    // Verification Workflow
    fun selectDeviceForVerification(tag: String) {
        _selectedVerifyDeviceTag.value = tag
        _currentDestination.value = AppDestination.VERIFICATION
    }

    fun applyVerificationDecision(
        assetTag: String,
        status: VerificationStatus,
        notes: String,
        operator: String
    ) {
        viewModelScope.launch {
            val dev = repository.getDeviceByAssetTag(assetTag) ?: return@launch
            val updatedStatus = when (status) {
                VerificationStatus.PASS -> "VERIFIED_PASS"
                VerificationStatus.FAIL -> "VERIFIED_FAIL"
                VerificationStatus.HOLD_REVIEW -> "HOLD_REVIEW"
                else -> dev.status
            }

            val updated = dev.copy(
                status = updatedStatus,
                verificationStatus = status.name,
                notes = if (notes.isNotBlank()) "${dev.notes}\n[Verification]: $notes" else dev.notes
            )
            repository.updateDevice(updated)
            repository.addAuditLog(
                assetTag = assetTag,
                action = "VERIFICATION_${status.name}",
                operator = operator.ifBlank { "Auditor" },
                details = "Device marked as ${status.label}. Notes: $notes"
            )
            _uiEvent.emit("Device $assetTag verification updated to ${status.label}")
        }
    }

    // PDF Certificate Generation
    fun generateCertificateForDevice(
        context: Context,
        device: DeviceEntity,
        isDemo: Boolean = true,
        onPdfReady: (File, Intent) -> Unit
    ) {
        viewModelScope.launch {
            val certId = "CERT-WIPE-" + (1000 + (Math.random() * 9000).toInt())
            val rawData = "${device.assetTag}|${device.lastWipeMethod}|${System.currentTimeMillis()}|$certId"
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(rawData.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

            val certEntity = CertificateEntity(
                certificateId = certId,
                assetTag = device.assetTag,
                deviceName = device.name,
                deviceType = device.deviceType,
                storageDetails = "${device.manufacturer} ${device.model} (${device.capacity})",
                sanitizationMethod = if (device.lastWipeMethod.isNotBlank()) device.lastWipeMethod else SanitizationMethod.NIST_PURGE.standard,
                operatorName = device.operatorName,
                issuedTimestamp = System.currentTimeMillis(),
                verificationStatus = "PASS",
                tamperProofHash = hash,
                isDemo = isDemo,
                pdfFilePath = ""
            )

            // Generate physical PDF document
            val pdfFile = PdfCertificateGenerator.generateCertificatePdf(context, certEntity, device)
            val updatedCert = certEntity.copy(pdfFilePath = pdfFile.absolutePath)
            repository.recordCertificate(updatedCert)

            val shareIntent = PdfCertificateGenerator.createSharePdfIntent(context, pdfFile)
            onPdfReady(pdfFile, shareIntent)
            _uiEvent.emit("Certificate $certId generated successfully.")
        }
    }

    fun openOrShareCertificate(
        context: Context,
        certificate: CertificateEntity,
        onPdfReady: (File, Intent) -> Unit
    ) {
        viewModelScope.launch {
            val dev = repository.getDeviceByAssetTag(certificate.assetTag)
            val pdfFile = PdfCertificateGenerator.generateCertificatePdf(context, certificate, dev)
            val shareIntent = PdfCertificateGenerator.createSharePdfIntent(context, pdfFile)
            onPdfReady(pdfFile, shareIntent)
        }
    }

    // History search and filters
    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun setHistoryTypeFilter(type: String) {
        _historyTypeFilter.value = type
    }

    fun setHistoryStatusFilter(status: String) {
        _historyStatusFilter.value = status
    }

    // Reset database to initial sample data
    fun resetDatabase() {
        viewModelScope.launch {
            repository.resetToSampleData()
            _uiEvent.emit("Database reset to college demonstration sample data.")
        }
    }
}
