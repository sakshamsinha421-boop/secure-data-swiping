package com.example.data

object SampleData {
    val sampleDevices = listOf(
        DeviceEntity(
            assetTag = "CLG-IT-2026-001",
            name = "Dell Latitude 7490",
            deviceType = DeviceType.LAPTOP.name,
            capacity = "512 GB",
            manufacturer = "Samsung NVMe PM981",
            model = "MZVLB512HAJQ",
            condition = DeviceCondition.WORKING.name,
            operatorName = "Prof. R. Sharma (Lab Lead)",
            registrationDate = System.currentTimeMillis() - 86400000L * 4,
            status = "VERIFIED_PASS",
            notes = "Decommissioned from CS Dept Lab 3. Sanitized under NIST SP 800-88 Purge.",
            lastWipeMethod = SanitizationMethod.NIST_PURGE.code,
            lastWipeTimestamp = System.currentTimeMillis() - 86400000L * 3,
            verificationStatus = VerificationStatus.PASS.name,
            certificateId = "CERT-WIPE-2026-8812"
        ),
        DeviceEntity(
            assetTag = "CLG-IT-2026-002",
            name = "HPE ProLiant Server SAS Disk",
            deviceType = DeviceType.HDD.name,
            capacity = "2.0 TB",
            manufacturer = "Seagate Enterprise Constellation",
            model = "ST2000NM0023",
            condition = DeviceCondition.DEGRADED.name,
            operatorName = "A. Verma (SysAdmin)",
            registrationDate = System.currentTimeMillis() - 86400000L * 3,
            status = "VERIFIED_PASS",
            notes = "Server storage rack upgrade. Sanitized with DoD 5220.22-M 3-pass overwrite.",
            lastWipeMethod = SanitizationMethod.DOD_5220_22_M.code,
            lastWipeTimestamp = System.currentTimeMillis() - 86400000L * 2,
            verificationStatus = VerificationStatus.PASS.name,
            certificateId = "CERT-WIPE-2026-9041"
        ),
        DeviceEntity(
            assetTag = "CLG-IT-2026-003",
            name = "MacBook Pro 15 (Retina)",
            deviceType = DeviceType.LAPTOP.name,
            capacity = "1.0 TB",
            manufacturer = "Apple APFS NVMe",
            model = "APPLE SSD SM1024L",
            condition = DeviceCondition.WORKING.name,
            operatorName = "K. Patel (Technician)",
            registrationDate = System.currentTimeMillis() - 86400000L * 2,
            status = "WIPED",
            notes = "Student council loaner laptop. Sanitized via NIST SP 800-88 Clear. Awaiting audit signoff.",
            lastWipeMethod = SanitizationMethod.NIST_CLEAR.code,
            lastWipeTimestamp = System.currentTimeMillis() - 3600000L * 5,
            verificationStatus = VerificationStatus.IN_PROGRESS.name,
            certificateId = ""
        ),
        DeviceEntity(
            assetTag = "CLG-IT-2026-004",
            name = "Kingston IronKey Locker+ 50",
            deviceType = DeviceType.USB_DRIVE.name,
            capacity = "128 GB",
            manufacturer = "Kingston Digital",
            model = "IKLP50-128GB",
            condition = DeviceCondition.WORKING.name,
            operatorName = "S. Sinha (Lead Auditor)",
            registrationDate = System.currentTimeMillis() - 86400000L * 1,
            status = "VERIFIED_PASS",
            notes = "Hardware encrypted secure drive. Sanitized with Cryptographic Erase (CE).",
            lastWipeMethod = SanitizationMethod.NIST_CRYPTO.code,
            lastWipeTimestamp = System.currentTimeMillis() - 3600000L * 2,
            verificationStatus = VerificationStatus.PASS.name,
            certificateId = "CERT-WIPE-2026-9533"
        ),
        DeviceEntity(
            assetTag = "CLG-IT-2026-005",
            name = "Western Digital Blue 1TB HDD",
            deviceType = DeviceType.HDD.name,
            capacity = "1.0 TB",
            manufacturer = "Western Digital",
            model = "WD10EZEX",
            condition = DeviceCondition.DEGRADED.name,
            operatorName = "P. Roy (Audit Staff)",
            registrationDate = System.currentTimeMillis() - 3600000L * 6,
            status = "HOLD_REVIEW",
            notes = "Found 4 uncorrectable bad LBAs during write pass. Needs manual forensic review.",
            lastWipeMethod = SanitizationMethod.DOD_5220_22_M.code,
            lastWipeTimestamp = System.currentTimeMillis() - 3600000L * 1,
            verificationStatus = VerificationStatus.HOLD_REVIEW.name,
            certificateId = ""
        ),
        DeviceEntity(
            assetTag = "CLG-IT-2026-006",
            name = "Lenovo ThinkCentre M90n",
            deviceType = DeviceType.DESKTOP.name,
            capacity = "256 GB",
            manufacturer = "Kioxia NVMe",
            model = "KBG40ZNS256G",
            condition = DeviceCondition.WORKING.name,
            operatorName = "S. Sinha (Lead Auditor)",
            registrationDate = System.currentTimeMillis() - 3600000L * 3,
            status = "REGISTERED",
            notes = "Lab 1 thin client decommissioned. Intake complete, scheduled for sanitization.",
            lastWipeMethod = "",
            lastWipeTimestamp = 0,
            verificationStatus = VerificationStatus.PENDING.name,
            certificateId = ""
        )
    )

    val sampleCertificates = listOf(
        CertificateEntity(
            certificateId = "CERT-WIPE-2026-8812",
            assetTag = "CLG-IT-2026-001",
            deviceName = "Dell Latitude 7490",
            deviceType = "Laptop",
            storageDetails = "Samsung NVMe PM981 (512 GB)",
            sanitizationMethod = "NIST SP 800-88 Rev 1 Purge (Block Erase)",
            operatorName = "Prof. R. Sharma (Lab Lead)",
            issuedTimestamp = System.currentTimeMillis() - 86400000L * 3,
            verificationStatus = "PASS",
            tamperProofHash = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
            isDemo = true,
            pdfFilePath = ""
        ),
        CertificateEntity(
            certificateId = "CERT-WIPE-2026-9041",
            assetTag = "CLG-IT-2026-002",
            deviceName = "HPE ProLiant Server SAS Disk",
            deviceType = "Enterprise HDD",
            storageDetails = "Seagate Enterprise Constellation (2.0 TB)",
            sanitizationMethod = "DoD 5220.22-M (3-Pass Overwrite & Verify)",
            operatorName = "A. Verma (SysAdmin)",
            issuedTimestamp = System.currentTimeMillis() - 86400000L * 2,
            verificationStatus = "PASS",
            tamperProofHash = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8",
            isDemo = true,
            pdfFilePath = ""
        ),
        CertificateEntity(
            certificateId = "CERT-WIPE-2026-9533",
            assetTag = "CLG-IT-2026-004",
            deviceName = "Kingston IronKey Locker+ 50",
            deviceType = "USB Flash Drive",
            storageDetails = "Kingston Digital IKLP50 (128 GB)",
            sanitizationMethod = "NIST SP 800-88 Rev 1 Cryptographic Erase (CE)",
            operatorName = "S. Sinha (Lead Auditor)",
            issuedTimestamp = System.currentTimeMillis() - 3600000L * 2,
            verificationStatus = "PASS",
            tamperProofHash = "4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a",
            isDemo = true,
            pdfFilePath = ""
        )
    )

    val sampleAuditLogs = listOf(
        AuditLogEntity(
            assetTag = "CLG-IT-2026-001",
            action = "CERTIFICATE_ISSUED",
            operator = "Prof. R. Sharma",
            timestamp = System.currentTimeMillis() - 86400000L * 3,
            details = "Sanitization certificate CERT-WIPE-2026-8812 generated and authorized for e-waste recycling."
        ),
        AuditLogEntity(
            assetTag = "CLG-IT-2026-001",
            action = "VERIFICATION_PASS",
            operator = "Prof. R. Sharma",
            timestamp = System.currentTimeMillis() - 86400000L * 3 - 600000,
            details = "100% random sample check passed. SHA-256 entropy check matches zero baseline."
        ),
        AuditLogEntity(
            assetTag = "CLG-IT-2026-002",
            action = "CERTIFICATE_ISSUED",
            operator = "A. Verma",
            timestamp = System.currentTimeMillis() - 86400000L * 2,
            details = "Sanitization certificate CERT-WIPE-2026-9041 generated. DoD 5220.22-M verified."
        ),
        AuditLogEntity(
            assetTag = "CLG-IT-2026-005",
            action = "HOLD_FLAGGED",
            operator = "P. Roy",
            timestamp = System.currentTimeMillis() - 3600000L * 1,
            details = "Sanitization halted due to bad blocks. Device placed on HOLD status for physical degaussing."
        ),
        AuditLogEntity(
            assetTag = "CLG-IT-2026-006",
            action = "DEVICE_REGISTERED",
            operator = "S. Sinha",
            timestamp = System.currentTimeMillis() - 3600000L * 3,
            details = "Device registered into IT Asset Disposition system with condition: WORKING."
        )
    )
}
