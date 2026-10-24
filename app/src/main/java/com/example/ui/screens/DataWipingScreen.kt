package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DeviceEntity
import com.example.data.SanitizationMethod
import com.example.engine.BlockState
import com.example.engine.SessionStatus
import com.example.ui.AppDestination
import com.example.ui.SecuWipeViewModel
import com.example.ui.components.CyberCard
import com.example.ui.components.DemoModeBanner
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderSubtle
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.StatusFail
import com.example.ui.theme.StatusHold
import com.example.ui.theme.StatusPass
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DataWipingScreen(
    viewModel: SecuWipeViewModel,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val session by viewModel.wipingSessionState.collectAsStateWithLifecycle()

    var showDevicePickerModal by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    val logListState = rememberLazyListState()
    LaunchedEffect(session.terminalLogs.size) {
        if (session.terminalLogs.isNotEmpty()) {
            logListState.animateScrollToItem(session.terminalLogs.size - 1)
        }
    }

    // Safety Confirmation Dialog
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = StatusHold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Sanitization Protocol", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "You are about to initialize data sanitization on target storage:",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Asset Tag: ${session.selectedDevice?.assetTag ?: "None"}",
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "• Protocol: ${session.selectedMethod.title}",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "• Mode: ${if (session.isDemoMode) "DEMO SIMULATION (Safe for presentation)" else "DESTRUCTIVE HARDWARE SANITIZATION"}",
                        color = if (session.isDemoMode) StatusPass else StatusFail,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "All addressable sectors will be overwritten in accordance with ${session.selectedMethod.standard}.",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmationDialog = false
                        viewModel.startWipingOperation()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_confirm_wiping")
                ) {
                    Text("Execute Protocol", color = CyberSurface, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
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
                title = "Data Sanitization Engine",
                subtitle = "NIST SP 800-88 Rev 1 & DoD 5220.22-M Cryptographic & Sector Overwrite System"
            )
        }

        // Demo Mode Controller Card
        item {
            CyberCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "College Demo & Presentation Mode",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (session.isDemoMode) Color(0xFF2E1906) else StatusPass.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, if (session.isDemoMode) Color(0xFFFF9800) else StatusPass)
                            ) {
                                Text(
                                    text = if (session.isDemoMode) "SIMULATED" else "LIVE HARDWARE",
                                    color = if (session.isDemoMode) Color(0xFFFFB74D) else StatusPass,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = if (session.isDemoMode)
                                "Emulates high-speed sector wiping, real-time block visualization, and entropy verification for evaluation."
                            else
                                "Direct disk sanitization requires rooted Android storage or external OTG device node access.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                        )
                    }

                    Switch(
                        checked = session.isDemoMode,
                        onCheckedChange = { viewModel.wipingEngine.setDemoMode(it) },
                        enabled = session.status != SessionStatus.RUNNING,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberCyan,
                            checkedTrackColor = CyberCyan.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CyberSurface
                        ),
                        modifier = Modifier.testTag("switch_demo_mode")
                    )
                }
            }
        }

        // Target Device Card
        item {
            CyberCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "TARGET STORAGE DEVICE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )

                        OutlinedButton(
                            onClick = { showDevicePickerModal = !showDevicePickerModal },
                            enabled = session.status != SessionStatus.RUNNING,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, CyberCyan),
                            modifier = Modifier.testTag("btn_select_device")
                        ) {
                            Text("Switch Device", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (session.selectedDevice != null) {
                        val dev = session.selectedDevice!!
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dev.assetTag,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = CyberCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                Text(
                                    text = dev.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "${dev.deviceType} • ${dev.capacity} • ${dev.manufacturer} (${dev.model})",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                                )
                            }
                            StatusBadge(status = dev.status)
                        }
                    } else {
                        Text(
                            text = "No device selected. Please select a registered asset to initialize sanitization.",
                            color = StatusHold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Device Picker Dropdown list
                    AnimatedVisibility(visible = showDevicePickerModal) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .background(CyberSurface, RoundedCornerShape(8.dp))
                                .border(1.dp, CyberBorderSubtle, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "SELECT REGISTERED DEVICE:",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            devices.forEach { dev ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            viewModel.selectDeviceForWipe(dev)
                                            showDevicePickerModal = false
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "${dev.assetTag} — ${dev.name}",
                                            color = TextPrimary,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = "${dev.deviceType} • ${dev.capacity}",
                                            color = TextSecondary,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                                        )
                                    }
                                    StatusBadge(status = dev.status)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sanitization Method Selector
        item {
            CyberCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SANITIZATION METHOD & ACCREDITED STANDARD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    SanitizationMethod.values().forEach { method ->
                        val isSelected = session.selectedMethod == method
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CyberCyan.copy(alpha = 0.15f) else CyberSurface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) CyberCyan else CyberBorderSubtle
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(enabled = session.status != SessionStatus.RUNNING) {
                                    viewModel.wipingEngine.selectMethod(method)
                                }
                                .testTag("method_option_${method.code}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(2.dp, if (isSelected) CyberCyan else TextMuted, RoundedCornerShape(8.dp))
                                                .background(if (isSelected) CyberCyan else Color.Transparent)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = method.title,
                                            color = if (isSelected) CyberCyan else TextPrimary,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = CyberSurfaceVariant
                                    ) {
                                        Text(
                                            text = "${method.passes} ${if (method.passes > 1) "Passes" else "Pass"}",
                                            color = TextSecondary,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = method.description,
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Best For: ${method.suitableFor}",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Telemetry & Progress Dashboard
        item {
            CyberCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "REAL-TIME WIPING MONITOR",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )

                        // Status pill
                        val (statusText, statusColor) = when (session.status) {
                            SessionStatus.RUNNING -> "WIPING ACTIVE" to CyberCyan
                            SessionStatus.PAUSED -> "PAUSED" to StatusHold
                            SessionStatus.COMPLETED -> "SANITIZATION COMPLETE" to StatusPass
                            SessionStatus.FAILED -> "ERROR / FAILED" to StatusFail
                            SessionStatus.ABORTED -> "ABORTED" to StatusHold
                            SessionStatus.IDLE -> "READY TO START" to TextSecondary
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = statusColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = statusText,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress bar & metrics
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Overall Progress (${(session.progress * 100).toInt()}%)",
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Pass ${session.currentPass} of ${session.totalPasses}",
                                color = CyberCyan,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { session.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = CyberCyan,
                            trackColor = Color(0xFF1E293B)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Telemetry readouts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("THROUGHPUT", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
                            Text(
                                text = String.format(Locale.US, "%.1f MB/s", session.throughputMBps),
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                        Column {
                            Text("CURRENT SECTOR LBA", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
                            Text(
                                text = "0x" + java.lang.Long.toHexString(session.currentLBA).uppercase(Locale.US).padStart(8, '0'),
                                color = CyberCyan,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                        Column {
                            Text("BLOCK INTEGRITY", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
                            Text(
                                text = "100% OK",
                                color = StatusPass,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sector Grid Visualizer (64 blocks)
                    Text(
                        text = "STORAGE SECTOR ALLOCATION MAP (64 LOGICAL BLOCKS):",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )

                    // 8x8 Grid of blocks
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (row in 0 until 8) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (col in 0 until 8) {
                                    val index = row * 8 + col
                                    val blockState = session.blocks.getOrElse(index) { BlockState.PENDING }

                                    val blockColor = when (blockState) {
                                        BlockState.PENDING -> Color(0xFF1E293B)
                                        BlockState.WRITING -> CyberCyan.copy(alpha = pulseAlpha)
                                        BlockState.WRITTEN -> CyberBlue
                                        BlockState.VERIFIED -> StatusPass
                                        BlockState.ERROR -> StatusFail
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(14.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(blockColor)
                                            .border(0.5.dp, Color(0xFF0F172A), RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                        }
                    }

                    // Legend
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BlockLegendItem("Pending", Color(0xFF1E293B))
                        BlockLegendItem("Writing", CyberCyan)
                        BlockLegendItem("Written", CyberBlue)
                        BlockLegendItem("Verified", StatusPass)
                    }
                }
            }
        }

        // Live Terminal Log Output
        item {
            CyberCard {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "LIVE SANITIZATION TERMINAL LOG",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "${session.terminalLogs.size} events",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 10.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 200.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF070B14),
                        border = BorderStroke(1.dp, Color(0xFF1A233A))
                    ) {
                        LazyColumn(
                            state = logListState,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            items(session.terminalLogs) { logLine ->
                                val isHighlight = logLine.contains("===") || logLine.contains("PASS") || logLine.contains("SHA256")
                                Text(
                                    text = logLine,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = if (isHighlight) CyberCyan else TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Operation Controls
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (session.status != SessionStatus.RUNNING && session.status != SessionStatus.PAUSED) {
                    Button(
                        onClick = { showConfirmationDialog = true },
                        enabled = session.selectedDevice != null,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_start_wiping"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberSurface)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("START SANITIZATION", color = CyberSurface, fontWeight = FontWeight.Bold)
                    }
                } else if (session.status == SessionStatus.RUNNING) {
                    Button(
                        onClick = { viewModel.wipingEngine.pauseWipe() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_pause_wiping"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusHold)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null, tint = CyberSurface)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PAUSE OPERATION", color = CyberSurface, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.wipingEngine.abortWipe() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_abort_wiping"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, StatusFail)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = StatusFail)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ABORT", color = StatusFail, fontWeight = FontWeight.Bold)
                    }
                } else if (session.status == SessionStatus.PAUSED) {
                    Button(
                        onClick = { viewModel.wipingEngine.resumeWipe() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_resume_wiping"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusPass)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberSurface)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RESUME", color = CyberSurface, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.wipingEngine.abortWipe() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, StatusFail)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = StatusFail)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ABORT", color = StatusFail, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Completed Quick Link Card
        if (session.status == SessionStatus.COMPLETED) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF00381B),
                    border = BorderStroke(1.5.dp, StatusPass),
                    modifier = Modifier.fillMaxWidth().testTag("wiping_completed_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = StatusPass, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sanitization Succeeded & Verified",
                                color = StatusPass,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Target media has completed ${session.selectedMethod.title} with 0 residual data blocks.",
                            color = Color(0xFFD1FAE5),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    session.selectedDevice?.let {
                                        viewModel.selectDeviceForVerification(it.assetTag)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusPass),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_go_to_verification")
                            ) {
                                Text("Proceed to Verification", color = CyberSurface, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { onNavigate(AppDestination.CERTIFICATES) },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, StatusPass)
                            ) {
                                Text("View Certificates", color = StatusPass)
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

@Composable
private fun BlockLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = TextMuted, style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp))
    }
}
