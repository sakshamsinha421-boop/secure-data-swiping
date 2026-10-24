package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderSubtle
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.StatusFail
import com.example.ui.theme.StatusPass
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeviceHistoryScreen(
    viewModel: SecuWipeViewModel,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredDevices by viewModel.filteredDevices.collectAsStateWithLifecycle()
    val searchQuery by viewModel.historySearchQuery.collectAsStateWithLifecycle()
    val typeFilter by viewModel.historyTypeFilter.collectAsStateWithLifecycle()
    val statusFilter by viewModel.historyStatusFilter.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()

    var expandedAssetTag by remember { mutableStateOf<String?>(null) }
    var deviceToDelete by remember { mutableStateOf<DeviceEntity?>(null) }

    if (deviceToDelete != null) {
        AlertDialog(
            onDismissRequest = { deviceToDelete = null },
            title = { Text("Delete Asset Record", color = StatusFail, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to delete ${deviceToDelete?.assetTag} from the disposition database? This action will be logged in the audit ledger.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val tag = deviceToDelete?.assetTag
                        if (tag != null) {
                            viewModel.resetDatabase() // or delete
                        }
                        deviceToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusFail)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deviceToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CyberSurfaceVariant
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
                title = "IT Asset History & Audit Trail",
                subtitle = "Complete lifecycle tracking, search, and immutable operation records"
            )
        }

        // Search & Filters Card
        item {
            CyberCard {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setHistorySearchQuery(it) },
                        placeholder = { Text("Search by Tag, Name, Manufacturer, Operator...", color = TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = CyberCyan)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("history_search_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = outlinedTextFieldColors(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Filter by Type:", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                    Spacer(modifier = Modifier.height(4.dp))
                    val types = listOf("ALL", "LAPTOP", "DESKTOP", "HDD", "SSD", "USB_DRIVE")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        types.forEach { t ->
                            val isSelected = typeFilter == t
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) CyberCyan.copy(alpha = 0.2f) else CyberSurface,
                                border = BorderStroke(1.dp, if (isSelected) CyberCyan else CyberBorderSubtle),
                                modifier = Modifier.clickable { viewModel.setHistoryTypeFilter(t) }
                            ) {
                                Text(
                                    text = if (t == "ALL") "All Types" else t,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = if (isSelected) CyberCyan else TextPrimary,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Filter by Status:", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                    Spacer(modifier = Modifier.height(4.dp))
                    val statuses = listOf("ALL", "REGISTERED", "WIPED", "VERIFIED_PASS", "HOLD_REVIEW")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        statuses.forEach { s ->
                            val isSelected = statusFilter == s
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) CyberCyan.copy(alpha = 0.2f) else CyberSurface,
                                border = BorderStroke(1.dp, if (isSelected) CyberCyan else CyberBorderSubtle),
                                modifier = Modifier.clickable { viewModel.setHistoryStatusFilter(s) }
                            ) {
                                Text(
                                    text = s.replace("_", " "),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = if (isSelected) CyberCyan else TextPrimary,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (filteredDevices.isEmpty()) {
            item {
                CyberCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No matching asset records found.", color = TextMuted)
                    }
                }
            }
        }

        items(filteredDevices, key = { it.assetTag }) { dev ->
            val isExpanded = expandedAssetTag == dev.assetTag
            val deviceLogs = remember(auditLogs, dev.assetTag) {
                auditLogs.filter { it.assetTag == dev.assetTag }
            }

            CyberCard(modifier = Modifier.testTag("history_item_${dev.assetTag}")) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dev.assetTag,
                                color = CyberCyan,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            Text(
                                text = dev.name,
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        StatusBadge(status = dev.status)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${dev.deviceType} • ${dev.capacity} • ${dev.manufacturer} (${dev.model})",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Expand / Collapse Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedAssetTag = if (isExpanded) null else dev.assetTag
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isExpanded) "Hide Full Audit History" else "View Full Audit Trail & Specs",
                            color = CyberCyan,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
                            Text("Registration Date: ${sdf.format(Date(dev.registrationDate))}", color = TextMuted, fontSize = 10.sp)
                            Text("Assigned Operator: ${dev.operatorName}", color = TextMuted, fontSize = 10.sp)
                            Text("Physical Condition: ${dev.condition}", color = TextMuted, fontSize = 10.sp)
                            if (dev.lastWipeMethod.isNotBlank()) {
                                Text("Last Sanitization: ${dev.lastWipeMethod}", color = CyberCyan, fontSize = 10.sp)
                            }
                            if (dev.certificateId.isNotBlank()) {
                                Text("Certificate ID: ${dev.certificateId}", color = StatusPass, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            if (dev.notes.isNotBlank()) {
                                Text("Notes: ${dev.notes}", color = TextSecondary, fontSize = 10.sp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Audit timeline
                            Text(
                                text = "LIFECYCLE AUDIT TIMELINE (${deviceLogs.size} ENTRIES):",
                                color = TextSecondary,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            deviceLogs.forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .padding(top = 4.dp)
                                            .background(CyberCyan, RoundedCornerShape(3.dp))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "${log.action} • ${sdf.format(Date(log.timestamp))}",
                                            color = TextPrimary,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = log.details,
                                            color = TextMuted,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.selectDeviceForWipe(dev) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = CyberSurface, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sanitize", color = CyberSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.selectDeviceForVerification(dev.assetTag) },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusPass),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Verify", color = CyberSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
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
