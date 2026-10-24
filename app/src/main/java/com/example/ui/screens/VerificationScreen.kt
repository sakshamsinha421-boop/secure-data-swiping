package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.DeviceEntity
import com.example.data.VerificationStatus
import com.example.ui.AppDestination
import com.example.ui.SecuWipeViewModel
import com.example.ui.components.CyberCard
import com.example.ui.components.DemoModeBanner
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderSubtle
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.StatusFail
import com.example.ui.theme.StatusHold
import com.example.ui.theme.StatusPass
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VerificationScreen(
    viewModel: SecuWipeViewModel,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedVerifyDeviceTag.collectAsStateWithLifecycle()

    val currentDevice = devices.find { it.assetTag == selectedTag } ?: devices.firstOrNull { it.status == "WIPED" } ?: devices.firstOrNull()

    var auditorNotes by remember { mutableStateOf("") }
    var auditorName by remember { mutableStateOf("Lead Security Evaluator") }
    var selectedStatus by remember(currentDevice) {
        mutableStateOf(
            when (currentDevice?.verificationStatus) {
                "PASS" -> VerificationStatus.PASS
                "FAIL" -> VerificationStatus.FAIL
                "HOLD_REVIEW" -> VerificationStatus.HOLD_REVIEW
                else -> VerificationStatus.PASS
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            SectionHeader(
                title = "Sanitization Audit & Verification",
                subtitle = "Validate residual data evidence and sign off on clearance criteria"
            )
        }

        item {
            DemoModeBanner()
        }

        // Device Selection Pill Row
        item {
            CyberCard {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SELECT ASSET FOR COMPLIANCE VERIFICATION:",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        devices.take(4).forEach { dev ->
                            val isSelected = dev.assetTag == currentDevice?.assetTag
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) CyberCyan.copy(alpha = 0.2f) else CyberSurface,
                                border = BorderStroke(1.dp, if (isSelected) CyberCyan else CyberBorderSubtle),
                                modifier = Modifier
                                    .clickable { viewModel.selectDeviceForVerification(dev.assetTag) }
                                    .testTag("verify_select_${dev.assetTag}")
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Text(
                                        text = dev.assetTag,
                                        color = if (isSelected) CyberCyan else TextPrimary,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = dev.deviceType,
                                        color = TextMuted,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (currentDevice != null) {
            // Target Asset Specifications
            item {
                CyberCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = currentDevice.assetTag,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = CyberCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                Text(
                                    text = currentDevice.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                            StatusBadge(status = currentDevice.status)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${currentDevice.deviceType} • ${currentDevice.capacity} • ${currentDevice.manufacturer} (${currentDevice.model})",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Text(
                            text = "Condition: ${currentDevice.condition} • Operator: ${currentDevice.operatorName}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                        )
                    }
                }
            }

            // Cryptographic Evidence Checklist
            item {
                CyberCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "INSPECTION CRITERIA & EVIDENCE LOG",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        EvidenceRow(
                            title = "Overwriting Protocol Parity",
                            value = if (currentDevice.lastWipeMethod.isNotBlank()) currentDevice.lastWipeMethod else "NIST SP 800-88 Purge",
                            status = "VERIFIED",
                            statusColor = StatusPass
                        )
                        EvidenceRow(
                            title = "Residual Data Sector Sampling",
                            value = "10% Random LBA Sampling: 0x00 Zero-Entropy Verified",
                            status = "PASS",
                            statusColor = StatusPass
                        )
                        EvidenceRow(
                            title = "Uncorrectable Bad Blocks",
                            value = if (currentDevice.status == "HOLD_REVIEW") "4 Bad Sectors Detected (Review Needed)" else "0 Bad Sectors Detected",
                            status = if (currentDevice.status == "HOLD_REVIEW") "WARNING" else "PASS",
                            statusColor = if (currentDevice.status == "HOLD_REVIEW") StatusHold else StatusPass
                        )
                        EvidenceRow(
                            title = "Cryptographic Media Key State",
                            value = "Hardware Encryption Key Invalidated / Re-keyed",
                            status = "VERIFIED",
                            statusColor = StatusPass
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tamper-proof hash readout
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF070B14),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "TAMPER-PROOF VERIFICATION HASH (SHA-256):",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)
                                )
                                Text(
                                    text = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                                    color = CyberCyan,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Decision Selector
            item {
                CyberCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "AUDITOR VERIFICATION DECISION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val decisions = listOf(
                            Triple(VerificationStatus.PASS, "PASS — Cleared for Recycling", StatusPass),
                            Triple(VerificationStatus.HOLD_REVIEW, "HOLD / REVIEW — Manual Inspection", StatusHold),
                            Triple(VerificationStatus.FAIL, "FAIL — Sanitization Incomplete", StatusFail)
                        )

                        decisions.forEach { (status, desc, color) ->
                            val isSelected = selectedStatus == status
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) color.copy(alpha = 0.15f) else CyberSurface,
                                border = BorderStroke(1.dp, if (isSelected) color else CyberBorderSubtle),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { selectedStatus = status }
                                    .testTag("decision_${status.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (status) {
                                            VerificationStatus.PASS -> Icons.Default.CheckCircle
                                            VerificationStatus.HOLD_REVIEW -> Icons.Default.Warning
                                            else -> Icons.Default.Error
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) color else TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = desc,
                                        color = if (isSelected) color else TextPrimary,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = auditorName,
                            onValueChange = { auditorName = it },
                            label = { Text("Lead Auditor Name") },
                            modifier = Modifier.fillMaxWidth().testTag("input_auditor_name"),
                            shape = RoundedCornerShape(10.dp),
                            colors = outlinedTextFieldColors(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = auditorNotes,
                            onValueChange = { auditorNotes = it },
                            label = { Text("Audit Notes / Justification") },
                            placeholder = { Text("e.g. 100% sector verification passed with NIST compliance.", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth().testTag("input_auditor_notes"),
                            shape = RoundedCornerShape(10.dp),
                            colors = outlinedTextFieldColors(),
                            maxLines = 3
                        )
                    }
                }
            }

            // Action Buttons
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            viewModel.applyVerificationDecision(
                                assetTag = currentDevice.assetTag,
                                status = selectedStatus,
                                notes = auditorNotes,
                                operator = auditorName
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_save_verification"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, tint = CyberSurface)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVE VERIFICATION AUDIT", color = CyberSurface, fontWeight = FontWeight.Bold)
                    }

                    if (selectedStatus == VerificationStatus.PASS) {
                        Button(
                            onClick = {
                                viewModel.generateCertificateForDevice(context, currentDevice) { file, intent ->
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        onNavigate(AppDestination.CERTIFICATES)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_generate_certificate_direct"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusPass)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = CyberSurface)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AUTHORIZE & GENERATE PDF CERTIFICATE", color = CyberSurface, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EvidenceRow(
    title: String,
    value: String,
    status: String,
    statusColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
            Text(text = value, color = TextSecondary, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
        }
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = statusColor.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
        ) {
            Text(
                text = status,
                color = statusColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp)
            )
        }
    }
}
