package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DeviceType(val displayName: String, val iconName: String) {
    LAPTOP("Laptop", "Laptop"),
    DESKTOP("Desktop", "Computer"),
    HDD("Enterprise HDD", "Storage"),
    SSD("NVMe / SATA SSD", "SdCard"),
    USB_DRIVE("USB Flash Drive", "Usb"),
    OTHER("Other Storage Media", "Devices")
}

enum class SanitizationMethod(
    val code: String,
    val title: String,
    val standard: String,
    val passes: Int,
    val description: String,
    val suitableFor: String
) {
    NIST_CLEAR(
        code = "NIST-CLEAR",
        title = "NIST SP 800-88 Clear",
        standard = "NIST SP 800-88 Rev 1 (Logical Overwrite)",
        passes = 1,
        description = "Overwrites all user-accessible storage locations with logical zeroes (0x00). Prevents recovery using simple keyboard or system utilities.",
        suitableFor = "Standard HDDs, SATA SSDs, USB drives slated for reuse within the organization."
    ),
    NIST_PURGE(
        code = "NIST-PURGE",
        title = "NIST SP 800-88 Purge",
        standard = "NIST SP 800-88 Rev 1 (Block Erase / Secure Erase)",
        passes = 1,
        description = "Executes hardware-level ATA/NVMe Secure Erase and physical block sanitization. Makes target data recovery infeasible even using advanced state-of-the-art laboratory techniques.",
        suitableFor = "Enterprise NVMe SSDs, Flash memory, and magnetic media intended for public surplus or recycling."
    ),
    NIST_CRYPTO(
        code = "NIST-CRYPTO",
        title = "Cryptographic Erase (CE)",
        standard = "NIST SP 800-88 Rev 1 (Cryptographic Erase)",
        passes = 1,
        description = "Cryptographically erases the internal Media Encryption Key (MEK) on hardware-encrypted Self-Encrypting Drives (SED). Instantly renders all ciphertext permanently irrecoverable.",
        suitableFor = "Hardware-encrypted OPAL / SED SSDs and enterprise encrypted server drives."
    ),
    DOD_5220_22_M(
        code = "DOD-5220.22-M",
        title = "DoD 5220.22-M (3-Pass)",
        standard = "National Industrial Security Program (DoD 5220.22-M)",
        passes = 3,
        description = "Three-pass overwriting protocol: Pass 1 writes binary zeroes (0x00), Pass 2 writes complementary binary ones (0xFF), Pass 3 writes a pseudo-random character pattern, followed by sector verification.",
        suitableFor = "Magnetic hard disk drives (HDDs) requiring rigorous multi-pass defense sanitization."
    )
}

enum class DeviceCondition(val label: String) {
    WORKING("Operational / Good"),
    DEGRADED("Degraded / Bad Sectors"),
    OBSOLETE("Obsolete / Decommissioned")
}

enum class VerificationStatus(val label: String) {
    PENDING("Pending Wipe"),
    IN_PROGRESS("In Progress"),
    PASS("PASS — Certified Clean"),
    FAIL("FAIL — Data/Block Error"),
    HOLD_REVIEW("HOLD — Manual Review")
}

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assetTag: String,
    val name: String,
    val deviceType: String,
    val capacity: String,
    val manufacturer: String,
    val model: String,
    val condition: String,
    val operatorName: String,
    val registrationDate: Long,
    val status: String, // e.g. "REGISTERED", "WIPING", "WIPED", "VERIFIED_PASS", "VERIFIED_FAIL", "HOLD_REVIEW", "RECYCLED"
    val notes: String = "",
    val lastWipeMethod: String = "",
    val lastWipeTimestamp: Long = 0,
    val verificationStatus: String = "PENDING",
    val certificateId: String = ""
)

@Entity(tableName = "wipe_operations")
data class WipeOperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operationId: String,
    val assetTag: String,
    val methodCode: String,
    val methodTitle: String,
    val standardName: String,
    val passesConfigured: Int,
    val passesCompleted: Int,
    val startTime: Long,
    val endTime: Long,
    val throughputMBps: Double,
    val verificationHash: String,
    val isDemo: Boolean,
    val resultStatus: String, // "PASS", "FAIL", "HOLD_REVIEW"
    val auditLog: String
)

@Entity(tableName = "certificates")
data class CertificateEntity(
    @PrimaryKey val certificateId: String,
    val assetTag: String,
    val deviceName: String,
    val deviceType: String,
    val storageDetails: String,
    val sanitizationMethod: String,
    val operatorName: String,
    val issuedTimestamp: Long,
    val verificationStatus: String,
    val tamperProofHash: String,
    val isDemo: Boolean,
    val pdfFilePath: String
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assetTag: String,
    val action: String,
    val operator: String,
    val timestamp: Long,
    val details: String
)
