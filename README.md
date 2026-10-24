# SecuWipe — Secure Data Wiping for Trustworthy IT Asset Recycling

**College IT Mini Project / Academic Capstone Prototype**  
**Title:** Secure Data Wiping for Trustworthy IT Asset Recycling  
**Domain:** Cybersecurity, IT Asset Disposition (ITAD), Hardware Sanitization, Environmental E-Waste Compliance  

---

## 1. Project Overview

**SecuWipe** is a modern, cybersecurity-themed Android application and tablet-ready dashboard developed for academic IT presentations and IT asset recycling operations. It addresses the critical vulnerability of residual sensitive data leaking from retired laptops, desktops, enterprise SAS drives, NVMe SSDs, and USB flash memory before entering the recycling lifecycle.

The application implements accredited sanitization profiles, dynamic live sector wiping visualizations, cryptographic post-wipe verification, persistent local SQLite (Room ORM) audit trails, and automated, downloadable PDF sanitization certificates equipped with 2D verification QR codes.

---

## 2. Key Features & Modules

### 1. Cybersecurity Executive Dashboard
- **Key Metrics:** Total Registered Devices, Successfully Sanitized Assets, Pending/Failed Verification Reviews, and Assets Certified Ready for Recycling.
- **Graphical Visualizations:** 
  - Dynamic Canvas Donut Chart for Sanitization Compliance Breakdown.
  - Multi-segment Asset Inventory Bar by Storage Technology (Laptop, Desktop, HDD, NVMe SSD, USB).
- **Recent Asset Ledger:** Real-time search and filtering with status indicators.

### 2. Device Intake & Registration
- Detailed intake form capturing Asset Tag / Barcode, Device Name, Media Category, Storage Capacity, Manufacturer, Model, Physical Condition, Operator Name, and Date.
- Prevents duplicate asset IDs and ensures validation before persisting to SQLite.

### 3. Data Sanitization Wiping Engine
- Selection of accredited media sanitization standards:
  - **NIST SP 800-88 Rev 1 Clear:** Single-pass logical zero overwrite (0x00).
  - **NIST SP 800-88 Rev 1 Purge:** Hardware-level ATA/NVMe block erase rendering data recovery infeasible even using laboratory techniques.
  - **NIST SP 800-88 Cryptographic Erase (CE):** Instant invalidation of internal Media Encryption Keys (MEK) for Self-Encrypting Drives (SED).
  - **DoD 5220.22-M (3-Pass):** Pass 1 (0x00), Pass 2 (0xFF), Pass 3 (Pseudo-Random) with sector verification.
- **Live Visualizer:** 64-block storage sector allocation grid (Pending, Writing, Written, Verified), real-time LBA sector pointer, and throughput metrics (MB/s).
- **Academic Demo Mode:** High-fidelity simulation mode for presentation safety, prominently labeled with badges to prevent confusion with destructive commands.
- **Safety Safeguards:** Confirmation dialogs preventing accidental wiping without explicit operator approval.

### 4. Post-Sanitization Verification
- Automated random sector sampling for zero-entropy verification.
- Tamper-proof SHA-256 cryptographic token generation.
- Tri-state auditor sign-off: **PASS** (Cleared for Recycling), **HOLD / REVIEW** (Manual inspection needed), and **FAIL** (Bad sectors or incomplete wipe).

### 5. PDF Sanitization Certificate Generator
- Generates official, downloadable, ISO-standard **Certificate of Data Sanitization**.
- Encodes Project Title, Certificate UUID, Asset Tag, Storage Specs, Sanitization Standard, Operator Signature, and Cryptographic Hash.
- Embedded 2D QR Code drawn directly onto the PDF canvas for instant verification token checking.
- Seamless Android sharing and viewing via `FileProvider`.
- Prominent educational watermark when generated in Demo Mode: `DEMO — NOT VALID AS PROOF OF SANITIZATION`.

### 6. Device History & Audit Trail
- Filter assets by type (Laptop, Desktop, HDD, SSD, USB) and status (Registered, Wiped, Verified, Hold).
- Expandable timeline of events for every asset (Intake -> Wiped -> Verified -> Certified).

---

## 3. Technology Stack

- **Platform:** Native Android (minSdk 24, targetSdk 36)
- **UI Framework:** Jetpack Compose with Material Design 3 (M3)
- **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Local Database:** SQLite via Android Room ORM with Kotlin Symbol Processing (KSP)
- **Concurrency:** Kotlin Coroutines & reactive `Flow`
- **PDF Engine:** Native Android `android.graphics.pdf.PdfDocument` + Custom 2D QR Code Matrix
- **Testing:** Local JVM Robolectric Testing

---

## 4. Database Schema

The SQLite database (`secuwipe_database`) contains four primary relational tables:

1. **`devices`**:
   - `id`: INTEGER PRIMARY KEY AUTOINCREMENT
   - `assetTag`: TEXT UNIQUE (e.g. `CLG-IT-2026-001`)
   - `name`: TEXT
   - `deviceType`: TEXT
   - `capacity`: TEXT
   - `manufacturer`: TEXT
   - `model`: TEXT
   - `condition`: TEXT
   - `operatorName`: TEXT
   - `registrationDate`: INTEGER (Epoch milliseconds)
   - `status`: TEXT (`REGISTERED`, `WIPING`, `WIPED`, `VERIFIED_PASS`, `HOLD_REVIEW`)
   - `notes`: TEXT
   - `lastWipeMethod`: TEXT
   - `lastWipeTimestamp`: INTEGER
   - `verificationStatus`: TEXT
   - `certificateId`: TEXT

2. **`wipe_operations`**:
   - `id`: INTEGER PRIMARY KEY AUTOINCREMENT
   - `operationId`: TEXT
   - `assetTag`: TEXT
   - `methodCode`: TEXT
   - `methodTitle`: TEXT
   - `standardName`: TEXT
   - `passesConfigured`: INTEGER
   - `passesCompleted`: INTEGER
   - `startTime`: INTEGER
   - `endTime`: INTEGER
   - `throughputMBps`: REAL
   - `verificationHash`: TEXT
   - `isDemo`: INTEGER (Boolean)
   - `resultStatus`: TEXT
   - `auditLog`: TEXT

3. **`certificates`**:
   - `certificateId`: TEXT PRIMARY KEY
   - `assetTag`: TEXT
   - `deviceName`: TEXT
   - `deviceType`: TEXT
   - `storageDetails`: TEXT
   - `sanitizationMethod`: TEXT
   - `operatorName`: TEXT
   - `issuedTimestamp`: INTEGER
   - `verificationStatus`: TEXT
   - `tamperProofHash`: TEXT
   - `isDemo`: INTEGER
   - `pdfFilePath`: TEXT

4. **`audit_logs`**:
   - `id`: INTEGER PRIMARY KEY AUTOINCREMENT
   - `assetTag`: TEXT
   - `action`: TEXT
   - `operator`: TEXT
   - `timestamp`: INTEGER
   - `details`: TEXT

---

## 5. Local Setup & Execution Instructions

1. Clone or open the project folder in **Android Studio Hedgehog / Ladybug** or run via Gradle CLI.
2. Ensure JDK 17+ and Android SDK Platform 34+ are installed.
3. Build the project:
   ```bash
   gradle assembleDebug
   ```
4. Run local tests:
   ```bash
   gradle :app:testDebugUnitTest
   ```
5. Install and launch on an Android emulator or connected device:
   ```bash
   gradle installDebug
   ```

---

## 6. Demonstration Workflow for College Project Evaluation

1. **Dashboard Overview:** Examine the cybersecurity command center, current compliance statistics, and storage type breakdown.
2. **Device Intake:** Navigate to **Register Device**, enter a new asset (e.g., `CLG-LAB-2026-09`, `ThinkPad X1 Carbon`, `NVMe SSD`, `512 GB`, Condition: `WORKING`).
3. **Data Sanitization:** Select the newly registered device in **Data Wiping**, choose **NIST SP 800-88 Purge (Block Erase)** or **DoD 5220.22-M (3-Pass)**, review the safety confirmation dialog, and click **Execute Protocol**.
4. **Live Monitoring:** Observe real-time sector block state transitions (Pending -> Writing -> Overwritten -> Verified), throughput metrics, and streaming terminal logs.
5. **Post-Wipe Verification:** Inspect the SHA-256 integrity token, review bad block diagnostics, and sign off with the Auditor name.
6. **PDF Certificate Generation:** Export the signed certificate to inspect the generated A4 layout, dynamic QR verification token, and academic demonstration watermark.
