package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeviceEntity
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.StatusFail
import com.example.ui.theme.StatusHold
import com.example.ui.theme.StatusPass
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SanitizationStatusDonutChart(
    devices: List<DeviceEntity>,
    modifier: Modifier = Modifier
) {
    val total = devices.size.coerceAtLeast(1)
    val passCount = devices.count { it.status == "VERIFIED_PASS" }
    val wipedCount = devices.count { it.status == "WIPED" || it.status == "WIPING" }
    val pendingCount = devices.count { it.status == "REGISTERED" }
    val holdCount = devices.count { it.status == "HOLD_REVIEW" }
    val failCount = devices.count { it.status == "VERIFIED_FAIL" }

    val slices = listOf(
        ChartSlice("Verified Pass", passCount, StatusPass),
        ChartSlice("Wiped / In Progress", wipedCount, CyberCyan),
        ChartSlice("Pending Wipe", pendingCount, CyberBlue),
        ChartSlice("Hold / Review", holdCount, StatusHold),
        ChartSlice("Failed", failCount, StatusFail)
    ).filter { it.value > 0 }

    CyberCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SANITIZATION COMPLIANCE BREAKDOWN",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Donut Chart on Canvas
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(130.dp)) {
                        val strokeWidth = 22f
                        val arcSize = size.width - strokeWidth
                        var startAngle = -90f

                        if (slices.isEmpty()) {
                            drawArc(
                                color = Color(0xFF263352),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
                            )
                        } else {
                            slices.forEach { slice ->
                                val sweep = (slice.value.toFloat() / total) * 360f
                                drawArc(
                                    color = slice.color,
                                    startAngle = startAngle,
                                    sweepAngle = sweep - 2f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                    topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                                    size = Size(arcSize, arcSize)
                                )
                                startAngle += sweep
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${devices.size}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "TOTAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        )
                    }
                }

                // Legend
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    slices.forEach { slice ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(slice.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${slice.label}: ",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = "${slice.value} (${(slice.value * 100 / total)}%)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceTypeDistributionBar(
    devices: List<DeviceEntity>,
    modifier: Modifier = Modifier
) {
    val total = devices.size.coerceAtLeast(1)
    val laptops = devices.count { it.deviceType.contains("LAPTOP", ignoreCase = true) }
    val desktops = devices.count { it.deviceType.contains("DESKTOP", ignoreCase = true) }
    val hdds = devices.count { it.deviceType.contains("HDD", ignoreCase = true) }
    val ssds = devices.count { it.deviceType.contains("SSD", ignoreCase = true) }
    val usbs = devices.count { it.deviceType.contains("USB", ignoreCase = true) }

    val categories = listOf(
        Pair("Laptop", laptops to CyberCyan),
        Pair("Desktop", desktops to CyberBlue),
        Pair("HDD", hdds to CyberPurple),
        Pair("SSD", ssds to StatusPass),
        Pair("USB", usbs to StatusHold)
    )

    CyberCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ASSET INVENTORY BY STORAGE TYPE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Multi-segment progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFF1E293B))
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    categories.forEach { (_, data) ->
                        val count = data.first
                        val color = data.second
                        if (count > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(count.toFloat())
                                    .height(14.dp)
                                    .background(color)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                categories.forEach { (name, data) ->
                    val count = data.first
                    val color = data.second
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$name ($count)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

private data class ChartSlice(val label: String, val value: Int, val color: Color)
