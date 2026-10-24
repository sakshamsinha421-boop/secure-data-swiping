package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DeviceEntity
import com.example.ui.AppDestination
import com.example.ui.SecuWipeViewModel
import com.example.ui.components.CyberCard
import com.example.ui.components.DemoModeBanner
import com.example.ui.components.DeviceTypeDistributionBar
import com.example.ui.components.MetricStatCard
import com.example.ui.components.SanitizationStatusDonutChart
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberBlue
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
fun DashboardScreen(
    viewModel: SecuWipeViewModel,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val certificates by viewModel.certificates.collectAsStateWithLifecycle()
    var searchFilter by remember { mutableStateOf("") }

    val totalDevices = devices.size
    val sanitizedCount = devices.count { it.status == "VERIFIED_PASS" || it.status == "WIPED" }
    val pendingOrFailed = devices.count { it.status == "REGISTERED" || it.status == "HOLD_REVIEW" || it.status == "VERIFIED_FAIL" }
    val readyForRecycling = devices.count { it.status == "VERIFIED_PASS" && it.certificateId.isNotBlank() }

    val filteredList = remember(devices, searchFilter) {
        if (searchFilter.isBlank()) {
            devices
        } else {
            devices.filter {
                it.assetTag.contains(searchFilter, ignoreCase = true) ||
                    it.name.contains(searchFilter, ignoreCase = true) ||
                    it.manufacturer.contains(searchFilter, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            DemoModeBanner()
        }

        // Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Total Assets",
                        value = "$totalDevices",
                        subtitle = "Registered in disposition",
                        icon = Icons.Default.Devices,
                        accentColor = CyberCyan,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_total_assets",
                        onClick = { onNavigate(AppDestination.DEVICE_HISTORY) }
                    )
                    MetricStatCard(
                        title = "Sanitized",
                        value = "$sanitizedCount",
                        subtitle = "NIST / DoD executed",
                        icon = Icons.Default.CheckCircle,
                        accentColor = StatusPass,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_sanitized_assets",
                        onClick = { onNavigate(AppDestination.DATA_WIPING) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Pending / Review",
                        value = "$pendingOrFailed",
                        subtitle = "Requires audit action",
                        icon = Icons.Default.Warning,
                        accentColor = StatusHold,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_pending_assets",
                        onClick = { onNavigate(AppDestination.VERIFICATION) }
                    )
                    MetricStatCard(
                        title = "Recycle Ready",
                        value = "$readyForRecycling",
                        subtitle = "Certified with PDF",
                        icon = Icons.Default.Recycling,
                        accentColor = CyberBlue,
                        modifier = Modifier.weight(1f),
                        testTag = "metric_recycle_ready",
                        onClick = { onNavigate(AppDestination.CERTIFICATES) }
                    )
                }
            }
        }

        // Quick Actions
        item {
            CyberCard(modifier = Modifier.testTag("quick_actions_panel")) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "DISPOSITION OPERATIONS SHORTCUTS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigate(AppDestination.REGISTER_DEVICE) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_quick_register"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = CyberSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Intake Device", color = CyberSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onNavigate(AppDestination.DATA_WIPING) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_quick_wipe"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Wipe Engine", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { onNavigate(AppDestination.CERTIFICATES) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_quick_certificates"),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
                        ) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Certificates", color = CyberCyan, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Charts
        item {
            SanitizationStatusDonutChart(devices = devices)
        }

        item {
            DeviceTypeDistributionBar(devices = devices)
        }

        // Recent Activity Table
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                SectionHeader(
                    title = "Recent IT Asset Ledger",
                    subtitle = "Live audit trail and disposition status for intake devices"
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchFilter,
                    onValueChange = { searchFilter = it },
                    placeholder = { Text("Search by Asset Tag, Name, Manufacturer...", color = TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = CyberCyan)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_search_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberBorderSubtle,
                        focusedContainerColor = CyberSurface,
                        unfocusedContainerColor = CyberSurface
                    ),
                    singleLine = true
                )
            }
        }

        items(filteredList, key = { it.assetTag }) { device ->
            AssetLedgerItem(
                device = device,
                onWipeClick = { viewModel.selectDeviceForWipe(device) },
                onVerifyClick = { viewModel.selectDeviceForVerification(device.assetTag) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AssetLedgerItem(
    device: DeviceEntity,
    onWipeClick: () -> Unit,
    onVerifyClick: () -> Unit
) {
    CyberCard(modifier = Modifier.testTag("device_item_${device.assetTag}")) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberSurface)
                            .border(1.dp, CyberBorderSubtle, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = device.assetTag,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                StatusBadge(status = device.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${device.deviceType} • ${device.capacity} • ${device.manufacturer}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                )
                val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                Text(
                    text = sdf.format(Date(device.registrationDate)),
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 10.sp)
                )
            }

            if (device.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Notes: ${device.notes}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (device.status == "REGISTERED" || device.status == "HOLD_REVIEW") {
                    OutlinedButton(
                        onClick = onWipeClick,
                        modifier = Modifier.testTag("btn_item_wipe_${device.assetTag}"),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan)
                    ) {
                        Text("Start Sanitization", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (device.status == "WIPED") {
                    Button(
                        onClick = onVerifyClick,
                        modifier = Modifier.testTag("btn_item_verify_${device.assetTag}"),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusPass),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Verify & Certify", color = CyberSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (device.certificateId.isNotBlank()) {
                    Text(
                        text = "Certificate: ${device.certificateId}",
                        color = StatusPass,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}
