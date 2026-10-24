package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CertificateEntity
import com.example.ui.AppDestination
import com.example.ui.SecuWipeViewModel
import com.example.ui.components.CyberCard
import com.example.ui.components.DemoModeBanner
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderSubtle
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.StatusPass
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.PdfCertificateGenerator
import com.example.util.QrCodeGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CertificatesScreen(
    viewModel: SecuWipeViewModel,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val certificates by viewModel.certificates.collectAsStateWithLifecycle()
    val devices by viewModel.devices.collectAsStateWithLifecycle()

    val pendingCertDevices = devices.filter { it.status == "WIPED" && it.certificateId.isBlank() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            SectionHeader(
                title = "Sanitization Certificates Ledger",
                subtitle = "Cryptographically verifiable PDF clearance documents for trustworthy IT asset recycling"
            )
        }

        item {
            DemoModeBanner()
        }

        // Pending issuance alert card if any
        if (pendingCertDevices.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, CyberCyan),
                    modifier = Modifier.fillMaxWidth().testTag("pending_cert_alert")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "${pendingCertDevices.size} sanitized device(s) ready for certification:",
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        pendingCertDevices.forEach { dev ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${dev.assetTag} (${dev.name})",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Button(
                                    onClick = {
                                        viewModel.generateCertificateForDevice(context, dev) { file, intent ->
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Certificate saved to: ${file.name}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusPass),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Generate PDF", color = CyberSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (certificates.isEmpty()) {
            item {
                CyberCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No Certificates Issued Yet", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            "Complete a data wiping operation and audit verification to issue official clearance certificates.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        items(certificates, key = { it.certificateId }) { cert ->
            CertificateItemCard(
                certificate = cert,
                onViewOrShare = {
                    viewModel.openOrShareCertificate(context, cert) { file, intent ->
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Certificate PDF generated: ${file.name}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CertificateItemCard(
    certificate: CertificateEntity,
    onViewOrShare: () -> Unit
) {
    val qrMatrix = remember(certificate.certificateId) {
        QrCodeGenerator.generateMatrix(
            "SECUWIPE-VERIFIED|CID:${certificate.certificateId}|TAG:${certificate.assetTag}|HASH:${certificate.tamperProofHash}"
        )
    }

    CyberCard(modifier = Modifier.testTag("cert_card_${certificate.certificateId}")) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = null,
                        tint = StatusPass,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = certificate.certificateId,
                            color = CyberCyan,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        Text(
                            text = "Issued: ${sdf.format(Date(certificate.issuedTimestamp))}",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF00381B),
                    border = BorderStroke(1.dp, StatusPass)
                ) {
                    Text(
                        text = "PASS • CERTIFIED",
                        color = StatusPass,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body: Specs & QR Code
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ASSET: ${certificate.assetTag} — ${certificate.deviceName}",
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Storage: ${certificate.storageDetails}",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Standard: ${certificate.sanitizationMethod}",
                        color = CyberCyan,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Auditor: ${certificate.operatorName}",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Miniature QR Code preview
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .padding(4.dp)
                ) {
                    QrCanvas(matrix = qrMatrix)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // SHA-256 Hash
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF070B14),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SHA256: ", color = TextMuted, style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold))
                    Text(
                        text = certificate.tamperProofHash.take(36) + "...",
                        color = CyberCyan,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onViewOrShare,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_share_pdf_${certificate.certificateId}"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = CyberSurface, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share / Export PDF", color = CyberSurface, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onViewOrShare,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CyberBorder),
                    modifier = Modifier.testTag("btn_view_pdf_${certificate.certificateId}")
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View", color = CyberCyan, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun QrCanvas(matrix: Array<BooleanArray>) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val size = QrCodeGenerator.MATRIX_SIZE
        val cellW = this.size.width / size
        val cellH = this.size.height / size
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (matrix[r][c]) {
                    drawRect(
                        color = Color.Black,
                        topLeft = androidx.compose.ui.geometry.Offset(c * cellW, r * cellH),
                        size = androidx.compose.ui.geometry.Size(cellW, cellH)
                    )
                }
            }
        }
    }
}
