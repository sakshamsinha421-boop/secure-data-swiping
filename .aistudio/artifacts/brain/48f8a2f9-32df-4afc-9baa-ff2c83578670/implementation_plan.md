# SecuWipe — Secure Data Wiping for Trustworthy IT Asset Recycling

A modern, cybersecurity-themed Android application and tablet-ready dashboard engineered for college IT mini-project demonstrations. It streamlines the lifecycle of retired IT assets: registering enterprise storage devices, executing NIST SP 800-88 Rev 1 and DoD 5220.22-M compliant sanitization routines with real-time sector wipe visualization, validating tamper-evident verification evidence, maintaining a persistent Room SQLite audit trail, and generating official PDF sanitization certificates with embedded verification QR codes.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural parameters have been aligned with your preferences:
> - **Platform & Form Factor**: Native Android application with adaptive layout supporting both vertical handheld phones and widescreen/tablet presentation modes (Navigation Rail + Master-Detail views).
> - **Sanitization Standards**: Prominently highlights **NIST SP 800-88 Rev 1** (Clear, Purge, Cryptographic Erase) and **DoD 5220.22-M** (3-Pass Zero/Complement/Random Overwrite).
> - **College Demo Highlight**: Complete end-to-end asset lifecycle from hardware intake and sanitization simulation to downloadable, professional PDF sanitization certificates.
> - **Demo Mode Safeguards**: Prominent "DEMO — NOT VALID AS PROOF OF SANITIZATION" badges on generated demo certificates and simulated hex inspectors, clearly separating educational demonstrations from destructive real-world commands.

---

### 1. Overview & Core Concept

- **What It Does**: SecuWipe provides a complete IT Asset Disposition (ITAD) sanitization system. College evaluators and asset managers can register retired storage devices (HDDs, SSDs, laptops, desktops, USB drives), select accredited wiping profiles based on storage geometry, monitor animated real-time wiping with simulated block verification and throughput metrics, perform multi-point post-wipe verification, and export signed PDF certificates ready for recycling clearance.
- **Target Audience / Persona**: IT asset managers, cybersecurity audit students, hardware recyclers, and academic evaluators assessing data sanitization compliance before e-waste processing.
- **Key Value**: Guarantees zero residual data leakage risk through verifiable compliance trails, preventing premature recycling of unverified assets while providing an intuitive, polished visual demonstration.

---

### 2. User Experience & Visual Design

#### Visual Identity & Theme
- **Aesthetic Direction**: High-tech cybersecurity command center. Deep obsidian/navy backdrop with luminous cyan and cobalt accents, complemented by emerald verification badges and high-contrast telemetry text.
- **Color Palette Tokens**:
  - `Background`: Deep Midnight Navy (`#0A0E1A`)
  - `Surface`: Slate Surface Layer (`#131B2E`) and Elevated Container (`#1C263D`)
  - `Primary / Accent`: Electric Cyan (`#00E5FF`) & Cyber Blue (`#2979FF`)
  - `Pass / Safe`: Emerald Neon (`#00E676`)
  - `Hold / In Progress`: Amber Warning (`#FFB300`)
  - `Fail / Alert`: Crimson Security (`#FF5252`)
  - `Text Hierarchy`: Ultra-bright white headlines (`#F0F4FC`), silver body text (`#94A3B8`), subtle muted captions (`#64748B`).
- **Typography & Rhythm**:
  - Monospace telemetry labels and block hashes paired with clean, rounded sans-serif headings.
  - High density of data cards, pill tags, and structured tables optimized for quick scanning during live presentations.

#### Key User Flows & Modules
1. **Executive Dashboard**:
   - Hero metric cards: Total Registered Assets, Sanitized Devices, Pending/Failed Verification, and Certified Ready-for-Recycling.
   - Interactive SVG/Canvas Donut and Bar distribution charts showing breakdown by sanitization method and compliance status.
   - Recent audit log feeds and quick-action shortcuts (Register Device, Quick Wipe, View Certificates).
2. **Device Registration**:
   - Form inputs: Asset Tag / Serial No. (with automatic format validation and duplicate prevention), Device Name, Category (Laptop, Desktop, Enterprise HDD, NVMe SSD, USB Flash, Other), Capacity (GB/TB), Manufacturer, Model, Current Condition (Working, Degraded, Scrapped), Operator Name, and Intake Notes.
   - Auto-generates unique SHA-256 asset hash ID and stores into persistent Room SQLite database.
3. **Sanitization Wiping Engine**:
   - Device selector displaying hardware capabilities and recommended sanitization standard based on media type (e.g. Purge/Cryptographic Erase for NVMe SSDs, Multi-pass Overwrite for HDDs).
   - Standard selector: NIST SP 800-88 Clear (Single-pass Zero fill), NIST SP 800-88 Purge (Block Erase), DoD 5220.22-M (Pass 1: 0x00, Pass 2: 0xFF, Pass 3: Pseudo-random + Verify), Cryptographic Erase (Crypto key zeroing).
   - Live visualizer: Real-time animated memory/sector block grid (showing Unallocated, Writing, Overwritten, Verified), elapsed timer, data throughput (MB/s), current block LBA address, and scrolling live terminal logs.
   - Dual Mode: "Demo Mode" with simulated high-speed sector wiping and educational explanations, plus real-storage safety checks.
4. **Verification & Audit Inspection**:
   - Post-sanitization automated sector sampling and checksum calculation (SHA-256 verification hash).
   - Status categorization: **PASS** (100% sectors validated zero/pattern), **FAIL** (bad blocks or residual signature found), **HOLD / REVIEW** (incomplete wipe or operator intervention needed).
   - Digital sign-off with operator credential stamping.
5. **PDF Certificate Generator**:
   - Direct on-device rendering to a professional, ISO-style "Certificate of Sanitization & E-Waste Recycling Clearance".
   - Encodes project title, unique certificate UUID, asset tag, storage serial, wiping protocol, operator signature, timestamp, and verification hash.
   - Dynamic 2D QR Code drawn directly onto the PDF canvas that encodes the cryptographic verification summary.
   - Native Android intent to View, Open, or Share the generated PDF file via standard viewers.
6. **Device History & Audit Trail**:
   - Searchable, filterable ledger with instant filtering by asset type, status, or date.
   - Detail inspect sheet showing full history: Intake -> Sanitization -> Verification -> Certificate Generation.
   - One-click regeneration and redownload of issued certificates.
7. **Settings & Mini-Project Demo Toolkit**:
   - One-tap "Load College Demo Dataset" (preloads realistic sample laptops, servers, and drives with different statuses).
   - Interactive NIST SP 800-88 Rev 1 compliance cheat-sheet.
   - Reset database button with safety confirmation.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Native Android Compose with Adaptive Navigation**:
  - *Chosen Approach*: Jetpack Compose UI using a responsive `NavigationSuiteScaffold` pattern (Navigation Rail for tablets/landscape and Navigation Bar for compact phones).
  - *Why*: Delivers a presentation-ready experience that fits tablet projection screens for college juries while working smoothly on mobile emulators.
  - *Alternatives Considered*: Fixed mobile-only layout (looked cramped for dense audit tables) or web-view wrapper (inferior offline performance and lacks native PDF generation).
- **Decision 2: Room Database (Local SQLite) for Persistent State**:
  - *Chosen Approach*: Official Android Room ORM with Kotlin Coroutines and reactive `Flow` observation for all assets, logs, and certificates.
  - *Why*: Device records, historical logs, and certificates persist across app restarts without requiring cloud connectivity or external network setup.
- **Decision 3: Native Android Graphics `PdfDocument` for Certificate Generation**:
  - *Chosen Approach*: Standard Android `android.graphics.pdf.PdfDocument` combined with a built-in QR Code matrix generator.
  - *Why*: Zero third-party dependency vulnerabilities, lightning-fast offline PDF generation, crisp vector rendering, and direct compatibility with Android's system print/view intents.
- **Decision 4: Simulated High-Fidelity Wiping Engine with Safety Guards**:
  - *Chosen Approach*: Realistic coroutine-driven sector visualizer that streams realistic LBA blocks, simulated pattern writes, and entropy verification logs, clearly branded with a Demo Mode badge.
  - *Why*: Directly satisfies academic presentation needs without risking accidental destruction of real host storage or requiring root privileges.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
│                                                                        │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐  ┌──────────┐ │
│  │   Dashboard   │  │ Device Intake │  │ Wiping Engine │  │ Verify   │ │
│  │   (Metrics,   │  │ (Asset Tag,   │  │ (NIST/DoD,    │  │ (SHA-256,│ │
│  │    Charts)    │  │  Specs, Form) │  │  Live Blocks) │  │  Audit)  │ │
│  └───────┬───────┘  └───────┬───────┘  └───────┬───────┘  └────┬─────┘ │
│          │                  │                  │               │       │
│          └──────────────────┼──────────────────┴───────────────┘       │
│                             ▼                                          │
│                 SecuWipeViewModel (StateFlow)                          │
│          (Coordinates Navigation, Wiping Simulation, PDF)              │
└─────────────────────────────┬──────────────────────────────────────────┘
                              │
                              ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        Data & Logic Layer                              │
│                                                                        │
│   ┌───────────────────────────┐      ┌─────────────────────────────┐   │
│   │    WipingEngineService    │      │    PdfCertificateBuilder    │   │
│   │  - NIST 800-88 Algorithms │      │  - Android PdfDocument      │   │
│   │  - Real-time Sector Flow  │      │  - QR Code Matrix Generator │   │
│   │  - Checksum Verification  │      │  - FileProvider Sharing     │   │
│   └─────────────┬─────────────┘      └──────────────┬──────────────┘   │
│                 │                                   │                  │
│                 ▼                                   ▼                  │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │                   AssetRepository                              │   │
│   │        - Reactive Flow queries for Devices and Logs            │   │
│   │        - Audit Trail recorder & status state-machine           │   │
│   └─────────────────────────────┬──────────────────────────────────┘   │
│                                 │                                      │
│                                 ▼                                      │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │                     Room SQLite Database                       │   │
│   │  - DeviceEntity (Asset tags, specs, condition, status)         │   │
│   │  - WipeOperationEntity (Standard, passes, timestamps, speed)   │   │
│   │  - CertificateEntity (UUID, issue date, hash, pdf path)        │   │
│   │  - AuditLogEntity (Sequential immutable event history)         │   │
│   └────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

#### Room Entities & State Flow
- **`DeviceEntity`**:
  - `id`: Auto-generated Long primary key.
  - `assetTag`: Unique string identifier (e.g., `ASSET-2026-0814`).
  - `name`: Display name (e.g., "ThinkPad T14 Gen 2").
  - `deviceType`: Enum (`LAPTOP`, `DESKTOP`, `ENTERPRISE_HDD`, `NVME_SSD`, `USB_DRIVE`, `OTHER`).
  - `storageCapacity`: String (e.g., "512 GB").
  - `manufacturer`: String (e.g., "Samsung PM981").
  - `operatorName`: String.
  - `condition`: Enum (`WORKING`, `DEGRADED`, `OBSOLETE`).
  - `status`: Enum (`REGISTERED`, `WIPING`, `WIPED`, `VERIFIED_PASS`, `VERIFIED_FAIL`, `HOLD_REVIEW`, `RECYCLED`).
  - `registeredTimestamp`: Long.
  - `notes`: String.
- **`WipeOperationEntity`**:
  - `operationId`: String UUID.
  - `deviceAssetTag`: Foreign reference.
  - `standard`: Enum (`NIST_800_88_CLEAR`, `NIST_800_88_PURGE`, `NIST_800_88_CRYPTO`, `DOD_5220_22_M`).
  - `passesCompleted`: Int.
  - `totalPasses`: Int.
  - `startTime`: Long, `endTime`: Long.
  - `throughputMBps`: Double.
  - `verificationHash`: String (SHA-256).
  - `isDemo`: Boolean.
  - `status`: Enum (`PASS`, `FAIL`, `HOLD`).
- **`CertificateEntity`**:
  - `certificateId`: String (e.g., `CERT-WIPE-2026-9921`).
  - `assetTag`: String.
  - `issuedAt`: Long.
  - `pdfFilePath`: String.
  - `verificationQrData`: String.
  - `sanitizationStandard`: String.
  - `tamperProofHash`: String.
- **`AuditLogEntity`**:
  - `logId`: Long.
  - `assetTag`: String.
  - `action`: String (e.g., "DEVICE_REGISTERED", "WIPE_STARTED", "PASS_VERIFIED", "CERTIFICATE_GENERATED").
  - `timestamp`: Long.
  - `details`: String.

---

### Planned Execution Strategy
1. **Core Data & Database Layer**:
   - Implement Room Entities, DAOs (`DeviceDao`, `WipeDao`, `AuditDao`), and Database singleton with pre-seeded sample data.
   - Implement `AssetRepository` coordinating asynchronous operations.
2. **Sanitization Engine & Simulation**:
   - Build `WipingEngine` emitting real-time block states, sector progress, data rates, and cryptographic verification logs.
3. **PDF Certificate & QR Generator**:
   - Implement `CertificateGenerator` creating vector-crisp PDF documents using Android `PdfDocument`, complete with QR code matrix rendering and system sharing via `FileProvider`.
4. **Jetpack Compose UI Screens**:
   - Build cybersecurity dark theme tokens in `Theme.kt` and `Color.kt`.
   - Implement adaptive navigation layout (Navigation Rail for tablets / widescreen, Navigation Bar for phones).
   - Implement `DashboardScreen` with statistics cards, visual distribution charts, and quick actions.
   - Implement `DeviceRegistrationScreen` with robust validation and duplicate detection.
   - Implement `DataWipingScreen` with interactive live sector grid visualizer, standard chooser, and safe confirmation dialogs.
   - Implement `VerificationScreen` with SHA-256 hash checks and criteria validation.
   - Implement `CertificatesScreen` with preview cards and one-tap PDF view/share.
   - Implement `DeviceHistoryScreen` with full search, filters, and timeline inspection.
   - Implement `SettingsScreen` with NIST SP 800-88 compliance guide and demo data reset.
5. **Verification & Testing**:
   - Verify complete compilation via `compile_applet`.
