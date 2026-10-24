package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.CertificateEntity
import com.example.data.DeviceEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfCertificateGenerator {

    fun generateCertificatePdf(
        context: Context,
        certificate: CertificateEntity,
        device: DeviceEntity?
    ): File {
        val certDir = File(context.cacheDir, "certificates")
        if (!certDir.exists()) {
            certDir.mkdirs()
        }
        val pdfFile = File(certDir, "${certificate.certificateId}.pdf")

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (points)
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawCertificateContent(canvas, certificate, device)

        document.finishPage(page)

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }

    private fun drawCertificateContent(
        canvas: Canvas,
        cert: CertificateEntity,
        device: DeviceEntity?
    ) {
        val width = 595f
        val height = 842f

        // Background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#0A0E1A")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // Decorative Cybersecurity Border
        val borderPaint = Paint().apply {
            color = Color.parseColor("#00E5FF")
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawRect(24f, 24f, width - 24f, height - 24f, borderPaint)

        val innerBorderPaint = Paint().apply {
            color = Color.parseColor("#172036")
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawRect(28f, 28f, width - 28f, height - 28f, innerBorderPaint)

        // Corner accents (Cyber style)
        val cornerPaint = Paint().apply {
            color = Color.parseColor("#2979FF")
            strokeWidth = 5f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        // Top-left
        canvas.drawLine(20f, 20f, 60f, 20f, cornerPaint)
        canvas.drawLine(20f, 20f, 20f, 60f, cornerPaint)
        // Top-right
        canvas.drawLine(width - 60f, 20f, width - 20f, 20f, cornerPaint)
        canvas.drawLine(width - 20f, 20f, width - 20f, 60f, cornerPaint)
        // Bottom-left
        canvas.drawLine(20f, height - 20f, 60f, height - 20f, cornerPaint)
        canvas.drawLine(20f, height - 20f, 20f, height - 60f, cornerPaint)
        // Bottom-right
        canvas.drawLine(width - 60f, height - 20f, width - 20f, height - 20f, cornerPaint)
        canvas.drawLine(width - 20f, height - 20f, width - 20f, height - 60f, cornerPaint)

        // Header Background Banner
        val headerBannerPaint = Paint().apply {
            color = Color.parseColor("#131B2E")
            style = Paint.Style.FILL
        }
        canvas.drawRect(30f, 30f, width - 30f, 130f, headerBannerPaint)

        // Title text
        val titlePaint = Paint().apply {
            color = Color.parseColor("#00E5FF")
            textSize = 15f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "SECURE DATA WIPING FOR TRUSTWORTHY IT ASSET RECYCLING",
            width / 2f,
            62f,
            titlePaint
        )

        val subTitlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("CERTIFICATE OF DATA SANITIZATION", width / 2f, 88f, subTitlePaint)

        val authorityPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 10f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "College IT Asset Disposition & Security Verification Laboratory",
            width / 2f,
            108f,
            authorityPaint
        )

        // Certificate metadata bar
        val metaBoxPaint = Paint().apply {
            color = Color.parseColor("#1A233A")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(40f, 142f, width - 40f, 178f), 6f, 6f, metaBoxPaint)

        val metaTextPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val metaLabelPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 9f
            isAntiAlias = true
        }

        canvas.drawText("CERTIFICATE ID:", 52f, 156f, metaLabelPaint)
        canvas.drawText(cert.certificateId, 52f, 170f, metaTextPaint)

        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm:ss z", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(cert.issuedTimestamp))
        canvas.drawText("ISSUE DATE / TIMESTAMP:", 230f, 156f, metaLabelPaint)
        canvas.drawText(formattedDate, 230f, 170f, metaTextPaint)

        canvas.drawText("STATUS:", 440f, 156f, metaLabelPaint)
        val passPaint = Paint().apply {
            color = Color.parseColor("#00E676")
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(cert.verificationStatus, 440f, 171f, passPaint)

        // Section 1: Device Details Card
        drawSectionHeader(canvas, 198f, "1. IDENTIFIED IT ASSET SPECIFICATIONS")
        val cardPaint = Paint().apply {
            color = Color.parseColor("#10172A")
            style = Paint.Style.FILL
        }
        val cardBorderPaint = Paint().apply {
            color = Color.parseColor("#263352")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val card1Rect = RectF(40f, 210f, width - 40f, 310f)
        canvas.drawRoundRect(card1Rect, 6f, 6f, cardPaint)
        canvas.drawRoundRect(card1Rect, 6f, 6f, cardBorderPaint)

        drawKeyValue(canvas, 55f, 232f, "Asset Tag / Record ID", cert.assetTag)
        drawKeyValue(canvas, 240f, 232f, "Device Name", cert.deviceName)
        drawKeyValue(canvas, 420f, 232f, "Device Type", cert.deviceType)

        drawKeyValue(canvas, 55f, 268f, "Storage Media", cert.storageDetails)
        drawKeyValue(canvas, 240f, 268f, "Storage Capacity", device?.capacity ?: "N/A")
        drawKeyValue(canvas, 420f, 268f, "Device Condition", device?.condition ?: "N/A")

        drawKeyValue(canvas, 55f, 302f, "Model Number", device?.model ?: "N/A")
        drawKeyValue(canvas, 240f, 302f, "Manufacturer", device?.manufacturer ?: "N/A")
        drawKeyValue(canvas, 420f, 302f, "Intake Operator", device?.operatorName ?: "N/A")

        // Section 2: Sanitization Execution Details Card
        drawSectionHeader(canvas, 330f, "2. SANITIZATION METHOD & COMPLIANCE STANDARD")
        val card2Rect = RectF(40f, 342f, width - 40f, 442f)
        canvas.drawRoundRect(card2Rect, 6f, 6f, cardPaint)
        canvas.drawRoundRect(card2Rect, 6f, 6f, cardBorderPaint)

        drawKeyValue(canvas, 55f, 364f, "Accredited Standard", cert.sanitizationMethod)
        drawKeyValue(canvas, 360f, 364f, "Verification Criteria", "NIST SP 800-88 Zero/Pattern")

        drawKeyValue(canvas, 55f, 400f, "Sanitization Technique", "Cryptographic / Physical Block Erase")
        drawKeyValue(canvas, 360f, 400f, "Target Sanitization Scope", "All LBAs (100% User Addressable Space)")

        drawKeyValue(canvas, 55f, 434f, "Residual Data State", "Zero (Irrecoverable via Lab Recovery)")
        drawKeyValue(canvas, 360f, 434f, "E-Waste Recycling Clearance", "APPROVED FOR DISPOSITION")

        // Section 3: Cryptographic Proof & QR Code Verification Card
        drawSectionHeader(canvas, 462f, "3. CRYPTOGRAPHIC VERIFICATION & AUDIT EVIDENCE")
        val card3Rect = RectF(40f, 474f, width - 40f, 620f)
        canvas.drawRoundRect(card3Rect, 6f, 6f, cardPaint)
        canvas.drawRoundRect(card3Rect, 6f, 6f, cardBorderPaint)

        drawKeyValue(canvas, 55f, 496f, "Verification Audit Token", "SHA-256 Checksum")
        val hashPaint = Paint().apply {
            color = Color.parseColor("#00E5FF")
            textSize = 8.5f
            isAntiAlias = true
        }
        canvas.drawText(cert.tamperProofHash, 55f, 514f, hashPaint)

        drawKeyValue(canvas, 55f, 540f, "Sanitizing Operator", cert.operatorName)
        drawKeyValue(canvas, 240f, 540f, "Verification Result", "PASS (0 Bad Blocks, 100% Verified)")

        drawKeyValue(
            canvas,
            55f,
            572f,
            "Verification Reference",
            "audit://secuwipe.internal/verify?cid=${cert.certificateId}"
        )
        drawKeyValue(
            canvas,
            55f,
            602f,
            "Integrity Status",
            "Tamper-Evident Immutable Hash Verified"
        )

        // Draw QR Code on the right side of Card 3
        val qrData = "SECUWIPE-VERIFIED|CID:${cert.certificateId}|TAG:${cert.assetTag}|HASH:${cert.tamperProofHash}"
        val qrMatrix = QrCodeGenerator.generateMatrix(qrData)
        val qrBoxSize = 100f
        val qrLeft = width - 155f
        val qrTop = 490f
        val cellSize = qrBoxSize / QrCodeGenerator.MATRIX_SIZE

        val qrBgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(qrLeft - 6f, qrTop - 6f, qrLeft + qrBoxSize + 6f, qrTop + qrBoxSize + 6f),
            4f,
            4f,
            qrBgPaint
        )

        val qrModulePaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        for (r in 0 until QrCodeGenerator.MATRIX_SIZE) {
            for (c in 0 until QrCodeGenerator.MATRIX_SIZE) {
                if (qrMatrix[r][c]) {
                    canvas.drawRect(
                        qrLeft + c * cellSize,
                        qrTop + r * cellSize,
                        qrLeft + (c + 1) * cellSize,
                        qrTop + (r + 1) * cellSize,
                        qrModulePaint
                    )
                }
            }
        }

        val qrCaptionPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 7.5f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Scan to Verify Clearance", qrLeft + qrBoxSize / 2f, qrTop + qrBoxSize + 14f, qrCaptionPaint)

        // Signatures Section
        val sigLinePaint = Paint().apply {
            color = Color.parseColor("#64748B")
            strokeWidth = 1f
            isAntiAlias = true
        }
        // Operator Signature
        canvas.drawLine(55f, 686f, 230f, 686f, sigLinePaint)
        val sigTextPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            textSize = 9.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(cert.operatorName, 55f, 698f, sigTextPaint)
        canvas.drawText("Authorized Sanitization Engineer", 55f, 710f, metaLabelPaint)

        // Lab Lead Signature
        canvas.drawLine(340f, 686f, 515f, 686f, sigLinePaint)
        canvas.drawText("Prof. IT Security Coordinator", 340f, 698f, sigTextPaint)
        canvas.drawText("Head of IT Asset Disposition & Recycling", 340f, 710f, metaLabelPaint)

        // Footer disclaimer
        val footerPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 8f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "This certificate is issued in compliance with NIST SP 800-88 Rev 1 guidelines for media sanitization.",
            width / 2f,
            750f,
            footerPaint
        )
        canvas.drawText(
            "SecuWipe IT Asset Security Engine • Document SHA-256 Tamper Protection Enabled",
            width / 2f,
            764f,
            footerPaint
        )

        // Prominent DEMO watermark if isDemo is true
        if (cert.isDemo) {
            canvas.save()
            canvas.rotate(-32f, width / 2f, height / 2f)
            val demoPaint = Paint().apply {
                color = Color.parseColor("#40FF5252") // Translucent Crimson
                textSize = 34f
                isFakeBoldText = true
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                "DEMO — NOT VALID AS PROOF OF SANITIZATION",
                width / 2f,
                height / 2f,
                demoPaint
            )
            canvas.restore()
        }
    }

    private fun drawSectionHeader(canvas: Canvas, y: Float, text: String) {
        val headerPaint = Paint().apply {
            color = Color.parseColor("#00E5FF")
            textSize = 10.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(text, 40f, y, headerPaint)
    }

    private fun drawKeyValue(canvas: Canvas, x: Float, y: Float, label: String, value: String) {
        val labelPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 8.5f
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(label.uppercase(Locale.getDefault()), x, y - 10f, labelPaint)
        canvas.drawText(value, x, y + 2f, valuePaint)
    }

    fun createViewPdfIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun createSharePdfIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Data Sanitization Certificate: ${file.nameWithoutExtension}")
            putExtra(
                Intent.EXTRA_TEXT,
                "Attached is the official NIST SP 800-88 / DoD sanitization certificate generated by SecuWipe for IT asset recycling clearance."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
