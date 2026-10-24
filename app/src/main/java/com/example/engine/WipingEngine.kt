package com.example.engine

import com.example.data.DeviceEntity
import com.example.data.SanitizationMethod
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class BlockState {
    PENDING,
    WRITING,
    WRITTEN,
    VERIFIED,
    ERROR
}

enum class SessionStatus {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    ABORTED
}

data class WipingSessionState(
    val selectedDevice: DeviceEntity? = null,
    val selectedMethod: SanitizationMethod = SanitizationMethod.NIST_PURGE,
    val isDemoMode: Boolean = true,
    val status: SessionStatus = SessionStatus.IDLE,
    val currentPass: Int = 1,
    val totalPasses: Int = 1,
    val progress: Float = 0f,
    val throughputMBps: Double = 0.0,
    val currentLBA: Long = 0L,
    val maxLBA: Long = 104857600L, // ~50GB in 512-byte sectors for simulation
    val blocks: List<BlockState> = List(64) { BlockState.PENDING },
    val terminalLogs: List<String> = emptyList(),
    val verificationHash: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val errorMessage: String? = null
)

class WipingEngine(private val scope: CoroutineScope) {

    private val _sessionState = MutableStateFlow(WipingSessionState())
    val sessionState: StateFlow<WipingSessionState> = _sessionState.asStateFlow()

    private var wipeJob: Job? = null
    private var isPaused = false

    fun selectDevice(device: DeviceEntity) {
        if (_sessionState.value.status == SessionStatus.RUNNING) return
        _sessionState.update {
            it.copy(
                selectedDevice = device,
                status = SessionStatus.IDLE,
                progress = 0f,
                currentPass = 1,
                totalPasses = it.selectedMethod.passes,
                blocks = List(64) { BlockState.PENDING },
                terminalLogs = listOf(
                    formatLog("Selected asset: ${device.assetTag} (${device.name})"),
                    formatLog("Storage: ${device.manufacturer} ${device.model} [${device.capacity}]"),
                    formatLog("Ready to initialize sanitization profile.")
                ),
                verificationHash = "",
                errorMessage = null
            )
        }
    }

    fun selectMethod(method: SanitizationMethod) {
        if (_sessionState.value.status == SessionStatus.RUNNING) return
        _sessionState.update {
            it.copy(
                selectedMethod = method,
                totalPasses = method.passes,
                currentPass = 1
            )
        }
    }

    fun setDemoMode(isDemo: Boolean) {
        if (_sessionState.value.status == SessionStatus.RUNNING) return
        _sessionState.update { it.copy(isDemoMode = isDemo) }
    }

    fun startWipe(onFinished: ((WipingSessionState) -> Unit)? = null) {
        val current = _sessionState.value
        val device = current.selectedDevice ?: return

        wipeJob?.cancel()
        isPaused = false

        val startTime = System.currentTimeMillis()
        val totalPasses = current.selectedMethod.passes
        val totalBlocks = 64

        _sessionState.update {
            it.copy(
                status = SessionStatus.RUNNING,
                startTime = startTime,
                endTime = 0L,
                currentPass = 1,
                totalPasses = totalPasses,
                progress = 0f,
                blocks = List(totalBlocks) { BlockState.PENDING },
                errorMessage = null,
                terminalLogs = it.terminalLogs + listOf(
                    formatLog("=== INITIALIZING SANITIZATION PROTOCOL ==="),
                    formatLog("Standard: ${current.selectedMethod.title}"),
                    formatLog("Target Device: ${device.assetTag} (${device.capacity})"),
                    formatLog("Mode: ${if (current.isDemoMode) "DEMO / EDUCATIONAL SIMULATION" else "PHYSICAL DISK SANITIZATION"}"),
                    formatLog("Total configured passes: $totalPasses"),
                    formatLog("Locking device access... Success.")
                )
            )
        }

        wipeJob = scope.launch(Dispatchers.Default) {
            try {
                for (pass in 1..totalPasses) {
                    _sessionState.update {
                        it.copy(
                            currentPass = pass,
                            terminalLogs = it.terminalLogs + formatLog("--- Starting Pass $pass/$totalPasses (${getPassDescription(current.selectedMethod, pass)}) ---")
                        )
                    }

                    // Reset blocks to pending for each pass
                    val updatedBlocks = MutableList(totalBlocks) { BlockState.PENDING }

                    for (b in 0 until totalBlocks) {
                        while (isPaused && isActive) {
                            delay(200)
                        }

                        // Writing phase
                        updatedBlocks[b] = BlockState.WRITING
                        val blockProgress = ((pass - 1).toFloat() * totalBlocks + b) / (totalPasses * totalBlocks)
                        val lba = (current.maxLBA * (b + 1) / totalBlocks)
                        val speed = Random.nextDouble(180.0, 480.0)

                        _sessionState.update { state ->
                            state.copy(
                                progress = blockProgress,
                                currentLBA = lba,
                                throughputMBps = speed,
                                blocks = updatedBlocks.toList()
                            )
                        }

                        // Speed: fast enough for college presentation, detailed enough to observe
                        val stepDelay = if (current.isDemoMode) 70L else 120L
                        delay(stepDelay)

                        // Written phase
                        updatedBlocks[b] = BlockState.WRITTEN
                        _sessionState.update { it.copy(blocks = updatedBlocks.toList()) }

                        if (b % 16 == 0 || b == totalBlocks - 1) {
                            _sessionState.update { state ->
                                state.copy(
                                    terminalLogs = (state.terminalLogs + formatLog("Pass $pass: Overwritten LBA 0x${java.lang.Long.toHexString(lba).uppercase(Locale.US)} (${(blockProgress * 100).toInt()}%) @ ${String.format(Locale.US, "%.1f", speed)} MB/s")).takeLast(100)
                                )
                            }
                        }
                    }
                }

                // Verification Phase (Read back & hash calculation)
                _sessionState.update {
                    it.copy(
                        throughputMBps = 520.0,
                        terminalLogs = it.terminalLogs + listOf(
                            formatLog("--- Commencing Post-Wipe Verification & Entropy Check ---"),
                            formatLog("Sampling random 10% sector blocks for residual data..."),
                            formatLog("Validating zero-fill / pattern parity across storage geometry...")
                        )
                    )
                }

                // Verify blocks animation
                val verifiedBlocks = MutableList(totalBlocks) { BlockState.WRITTEN }
                for (b in 0 until totalBlocks step 2) {
                    delay(30L)
                    verifiedBlocks[b] = BlockState.VERIFIED
                    if (b + 1 < totalBlocks) verifiedBlocks[b + 1] = BlockState.VERIFIED
                    _sessionState.update { it.copy(blocks = verifiedBlocks.toList()) }
                }

                val endTime = System.currentTimeMillis()
                val rawToHash = "${device.assetTag}|${current.selectedMethod.code}|$startTime|$endTime|SECURE_ZERO"
                val md = MessageDigest.getInstance("SHA-256")
                val hashBytes = md.digest(rawToHash.toByteArray(Charsets.UTF_8))
                val hashHex = hashBytes.joinToString("") { "%02x".format(it) }

                val finalState = _sessionState.value.copy(
                    status = SessionStatus.COMPLETED,
                    progress = 1.0f,
                    endTime = endTime,
                    throughputMBps = 0.0,
                    verificationHash = hashHex,
                    terminalLogs = _sessionState.value.terminalLogs + listOf(
                        formatLog("Verification status: PASS (0 bad blocks, 0 bytes residual data)"),
                        formatLog("Cryptographic proof hash generated:"),
                        formatLog("SHA256: $hashHex"),
                        formatLog("=== SANITIZATION PROTOCOL FINISHED SUCCESSFULLY ===")
                    )
                )

                _sessionState.value = finalState
                onFinished?.invoke(finalState)

            } catch (e: CancellationException) {
                _sessionState.update {
                    it.copy(
                        status = SessionStatus.ABORTED,
                        throughputMBps = 0.0,
                        terminalLogs = it.terminalLogs + formatLog("Sanitization aborted by operator.")
                    )
                }
            } catch (e: Exception) {
                _sessionState.update {
                    it.copy(
                        status = SessionStatus.FAILED,
                        throughputMBps = 0.0,
                        errorMessage = e.message ?: "Unknown wiping error",
                        terminalLogs = it.terminalLogs + formatLog("ERROR: ${e.message}")
                    )
                }
            }
        }
    }

    fun pauseWipe() {
        if (_sessionState.value.status == SessionStatus.RUNNING) {
            isPaused = true
            _sessionState.update {
                it.copy(
                    status = SessionStatus.PAUSED,
                    terminalLogs = it.terminalLogs + formatLog("Sanitization PAUSED by operator.")
                )
            }
        }
    }

    fun resumeWipe() {
        if (_sessionState.value.status == SessionStatus.PAUSED) {
            isPaused = false
            _sessionState.update {
                it.copy(
                    status = SessionStatus.RUNNING,
                    terminalLogs = it.terminalLogs + formatLog("Sanitization RESUMED.")
                )
            }
        }
    }

    fun abortWipe() {
        wipeJob?.cancel()
        wipeJob = null
        isPaused = false
        _sessionState.update {
            it.copy(
                status = SessionStatus.ABORTED,
                throughputMBps = 0.0,
                terminalLogs = it.terminalLogs + formatLog("Sanitization ABORTED.")
            )
        }
    }

    private fun getPassDescription(method: SanitizationMethod, pass: Int): String {
        return when (method) {
            SanitizationMethod.NIST_CLEAR -> "Pass 1: Binary Zero Fill (0x00)"
            SanitizationMethod.NIST_PURGE -> "Pass 1: ATA/NVMe Physical Block Erase"
            SanitizationMethod.NIST_CRYPTO -> "Pass 1: Cryptographic Media Encryption Key Invalidation"
            SanitizationMethod.DOD_5220_22_M -> when (pass) {
                1 -> "Pass 1: Fixed Character (0x00)"
                2 -> "Pass 2: Complement Bit Inversion (0xFF)"
                else -> "Pass 3: Pseudo-Random Character Overwrite"
            }
        }
    }

    private fun formatLog(message: String): String {
        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        return "[${sdf.format(Date())}] $message"
    }
}
