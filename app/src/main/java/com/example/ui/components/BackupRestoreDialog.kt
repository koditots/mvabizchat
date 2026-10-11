package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.repository.AppRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun BackupRestoreDialog(
    repository: AppRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val lastBackup by repository.lastCloudBackup.collectAsState()
    val isSyncing by repository.isCloudSyncing.collectAsState()

    var exportedJson by remember { mutableStateOf<String?>(null) }
    var restoreInput by remember { mutableStateOf("") }
    var showRestoreField by remember { mutableStateOf(false) }
    var statusFeedback by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PurpleSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("backup_restore_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Chat Backup & Restore",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Offline & Google Cloud Storage",
                            fontSize = 12.sp,
                            color = PurpleAccent
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Cloud Backup Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PurpleSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = PurpleNeonGlow,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Google Cloud Online Backup",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Backs up encrypted messages and media to your Google Account. Backups are protected by end-to-end encryption keys.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Last Google Backup:", fontSize = 11.sp, color = TextMuted)
                                Text(text = lastBackup, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        repository.performGoogleCloudBackup { success ->
                                            if (success) {
                                                statusFeedback = "Google Cloud backup complete! 🔒"
                                            }
                                        }
                                    }
                                },
                                enabled = !isSyncing,
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("google_backup_button")
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Encrypting...", fontSize = 12.sp)
                                } else {
                                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Back Up", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Offline Local Backup & Export Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PurpleSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SaveAlt,
                                contentDescription = null,
                                tint = PurpleAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Offline Local Export & Restore",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Save your chats and media directly to local device storage as an encrypted payload for maximum air-gapped privacy.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val data = repository.exportLocalEncryptedBackup()
                                    exportedJson = data
                                    clipboardManager.setText(AnnotatedString(data))
                                    statusFeedback = "Exported & copied encrypted payload to clipboard!"
                                },
                                modifier = Modifier.weight(1f).testTag("export_local_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleAccent)
                            ) {
                                Text("Export Local", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showRestoreField = !showRestoreField },
                                modifier = Modifier.weight(1f).testTag("restore_local_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleNeonGlow)
                            ) {
                                Text("Restore Local", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (showRestoreField) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = restoreInput,
                                onValueChange = { restoreInput = it },
                                placeholder = { Text("Paste exported JSON backup...", fontSize = 11.sp, color = TextMuted) },
                                modifier = Modifier.fillMaxWidth().testTag("restore_input_field"),
                                shape = RoundedCornerShape(10.dp),
                                maxLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PurplePrimary,
                                    unfocusedBorderColor = PurpleBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    val ok = repository.restoreLocalBackup(restoreInput)
                                    statusFeedback = if (ok) "Successfully restored local data! ✓" else "Failed to parse backup payload."
                                    if (ok) showRestoreField = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                            ) {
                                Text("Apply Local Restore", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (exportedJson != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Preview of Exported Encrypted Package:",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PurpleDeepBg,
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                Text(
                                    text = exportedJson?.take(300) + "\n... [Full payload stored]",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = PurpleAccent,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }

                if (statusFeedback != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = statusFeedback ?: "",
                        fontSize = 12.sp,
                        color = PurpleE2EEGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
