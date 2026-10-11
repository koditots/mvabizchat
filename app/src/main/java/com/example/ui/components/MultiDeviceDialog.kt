package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LinkedDevice
import com.example.ui.theme.*

@Composable
fun MultiDeviceDialog(
    devices: List<LinkedDevice>,
    onDismiss: () -> Unit,
    onLinkDevice: (name: String, type: String) -> Unit,
    onUnlinkDevice: (String) -> Unit
) {
    var showScanSim by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PurpleSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("multi_device_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Linked Devices",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Multi-Device Cloud Sync",
                            fontSize = 12.sp,
                            color = PurpleAccent
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Info banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PurpleSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = null,
                            tint = PurpleAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Use MVA Business Chat on Web, Desktop, and Tablets without keeping your primary phone online.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(devices) { dev ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PurpleSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (dev.isCurrent) PurplePrimary else PurplePrimaryDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when {
                                            dev.deviceType.contains("Android", true) -> Icons.Default.PhoneAndroid
                                            dev.deviceType.contains("Web", true) -> Icons.Default.Laptop
                                            else -> Icons.Default.Computer
                                        },
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dev.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${dev.lastActive} • ${dev.location}",
                                        fontSize = 11.sp,
                                        color = if (dev.isCurrent) PurpleE2EEGreen else TextMuted
                                    )
                                }

                                if (!dev.isCurrent) {
                                    IconButton(onClick = { onUnlinkDevice(dev.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Unlink",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Current",
                                        fontSize = 11.sp,
                                        color = PurpleAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onLinkDevice("iPad Pro • Safari Web", "iPadOS • Web Browser")
                        showScanSim = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("link_device_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Link New Device (QR Scanner)", fontWeight = FontWeight.Bold, color = Color.White)
                }

                if (showScanSim) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✓ Device successfully paired and cloud keys synchronized!",
                        fontSize = 11.sp,
                        color = PurpleE2EEGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
