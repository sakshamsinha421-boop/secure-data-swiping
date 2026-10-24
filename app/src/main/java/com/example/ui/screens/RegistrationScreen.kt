package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DeviceCondition
import com.example.data.DeviceType
import com.example.ui.AppDestination
import com.example.ui.SecuWipeViewModel
import com.example.ui.components.CyberCard
import com.example.ui.components.SectionHeader
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
fun RegistrationScreen(
    viewModel: SecuWipeViewModel,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            SectionHeader(
                title = "IT Asset Intake & Registration",
                subtitle = "Record retired storage hardware into the persistent SQLite disposition database"
            )
        }

        if (formState.successMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF00381B),
                    border = BorderStroke(1.dp, StatusPass),
                    modifier = Modifier.fillMaxWidth().testTag("registration_success_banner")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusPass,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formState.successMessage ?: "",
                                color = StatusPass,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onNavigate(AppDestination.DATA_WIPING) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("btn_proceed_to_wiping")
                            ) {
                                Text("Proceed to Data Wiping", color = CyberSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { onNavigate(AppDestination.DEVICE_HISTORY) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("View in History", color = TextPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            CyberCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. HARDWARE IDENTIFIERS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Asset Tag / ID
                    OutlinedTextField(
                        value = formState.assetTag,
                        onValueChange = { viewModel.onAssetTagChanged(it) },
                        label = { Text("Asset Tag / Record ID *") },
                        placeholder = { Text("e.g. CLG-IT-2026-081", color = TextMuted) },
                        isError = formState.assetTagError != null,
                        supportingText = {
                            if (formState.assetTagError != null) {
                                Text(formState.assetTagError ?: "", color = StatusFail)
                            } else {
                                Text("Unique organization inventory barcode or serial tag", color = TextMuted)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_asset_tag"),
                        shape = RoundedCornerShape(10.dp),
                        colors = outlinedTextFieldColors(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Device Name
                    OutlinedTextField(
                        value = formState.name,
                        onValueChange = { viewModel.onNameChanged(it) },
                        label = { Text("Device Name *") },
                        placeholder = { Text("e.g. Dell Latitude 7490 or Lab 4 Server Disk", color = TextMuted) },
                        isError = formState.nameError != null,
                        supportingText = {
                            if (formState.nameError != null) {
                                Text(formState.nameError ?: "", color = StatusFail)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_device_name"),
                        shape = RoundedCornerShape(10.dp),
                        colors = outlinedTextFieldColors(),
                        singleLine = true
                    )
                }
            }
        }

        // Device Type Selection
        item {
            CyberCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. STORAGE MEDIA CATEGORY *",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DeviceType.values().forEach { type ->
                            val isSelected = formState.deviceType == type
                            val icon = when (type) {
                                DeviceType.LAPTOP -> Icons.Default.Laptop
                                DeviceType.DESKTOP -> Icons.Default.Computer
                                DeviceType.HDD -> Icons.Default.Storage
                                DeviceType.SSD -> Icons.Default.SdCard
                                DeviceType.USB_DRIVE -> Icons.Default.Usb
                                DeviceType.OTHER -> Icons.Default.Devices
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) CyberCyan.copy(alpha = 0.2f) else CyberSurface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) CyberCyan else CyberBorderSubtle
                                ),
                                modifier = Modifier
                                    .clickable { viewModel.onDeviceTypeChanged(type) }
                                    .testTag("chip_device_type_${type.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) CyberCyan else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = type.displayName,
                                        color = if (isSelected) CyberCyan else TextPrimary,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Storage Specs
        item {
            CyberCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. STORAGE SPECIFICATIONS & MANUFACTURER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Capacity presets
                    Text(
                        text = "Storage Capacity *",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val capacities = listOf("128 GB", "256 GB", "512 GB", "1.0 TB", "2.0 TB", "4.0 TB")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        capacities.forEach { cap ->
                            val isSelected = formState.capacity == cap
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) CyberCyan.copy(alpha = 0.2f) else CyberSurface,
                                border = BorderStroke(1.dp, if (isSelected) CyberCyan else CyberBorderSubtle),
                                modifier = Modifier.clickable { viewModel.onCapacityChanged(cap) }
                            ) {
                                Text(
                                    text = cap,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    color = if (isSelected) CyberCyan else TextPrimary,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = formState.capacity,
                        onValueChange = { viewModel.onCapacityChanged(it) },
                        label = { Text("Capacity Custom Input") },
                        isError = formState.capacityError != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_capacity"),
                        shape = RoundedCornerShape(10.dp),
                        colors = outlinedTextFieldColors(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = formState.manufacturer,
                            onValueChange = { viewModel.onManufacturerChanged(it) },
                            label = { Text("Manufacturer") },
                            placeholder = { Text("Samsung, Seagate, WD...", color = TextMuted) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_manufacturer"),
                            shape = RoundedCornerShape(10.dp),
                            colors = outlinedTextFieldColors(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = formState.model,
                            onValueChange = { viewModel.onModelChanged(it) },
                            label = { Text("Model / Serial No.") },
                            placeholder = { Text("e.g. ST2000NM", color = TextMuted) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_model"),
                            shape = RoundedCornerShape(10.dp),
                            colors = outlinedTextFieldColors(),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // Intake Condition & Operator
        item {
            CyberCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "4. HARDWARE CONDITION & CUSTODY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Device Condition", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DeviceCondition.values().forEach { cond ->
                            val isSelected = formState.condition == cond
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) CyberCyan.copy(alpha = 0.2f) else CyberSurface,
                                border = BorderStroke(1.dp, if (isSelected) CyberCyan else CyberBorderSubtle),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.onConditionChanged(cond) }
                            ) {
                                Text(
                                    text = cond.label,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    color = if (isSelected) CyberCyan else TextPrimary,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = formState.operatorName,
                        onValueChange = { viewModel.onOperatorNameChanged(it) },
                        label = { Text("Intake Operator Name *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_operator_name"),
                        shape = RoundedCornerShape(10.dp),
                        colors = outlinedTextFieldColors(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
                    Text(
                        text = "Intake Date: ${sdf.format(Date())} (System Auto-Stamped)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = formState.notes,
                        onValueChange = { viewModel.onNotesChanged(it) },
                        label = { Text("Intake Notes / Dept Reason") },
                        placeholder = { Text("Decommissioned lab machine, disk upgrade, etc.", color = TextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_notes"),
                        shape = RoundedCornerShape(10.dp),
                        colors = outlinedTextFieldColors(),
                        maxLines = 3
                    )
                }
            }
        }

        // Submit Button
        item {
            Button(
                onClick = { viewModel.registerDevice() },
                enabled = !formState.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_submit_registration"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Icon(
                    Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = CyberSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "REGISTER ASSET INTO DATABASE",
                    color = CyberSurface,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyberCyan,
    unfocusedBorderColor = CyberBorderSubtle,
    focusedContainerColor = CyberSurface,
    unfocusedContainerColor = CyberSurface,
    focusedLabelColor = CyberCyan,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)
