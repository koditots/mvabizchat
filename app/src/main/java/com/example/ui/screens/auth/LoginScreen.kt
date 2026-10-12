package com.example.ui.screens.auth

import android.accounts.AccountManager
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.GoogleAuthResult
import com.example.data.auth.GoogleSignInHelper
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onGoogleSignInSuccess: (displayName: String, email: String, photoUrl: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var gmailInput by remember { mutableStateOf("merversaus@gmail.com") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val googleHelper = remember { GoogleSignInHelper(context) }

    val accountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val extracted = googleHelper.extractAccountFromIntentResult(result.data)
            val selectedEmail = extracted?.second ?: result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)

            if (!selectedEmail.isNullOrBlank()) {
                val cleanEmail = selectedEmail.trim().lowercase()
                if (!cleanEmail.endsWith("@gmail.com")) {
                    errorMessage = "Registration and sign-in is exclusively for Gmail (@gmail.com) accounts. Selected: $selectedEmail"
                    return@rememberLauncherForActivityResult
                }
                val name = extracted?.first ?: selectedEmail.substringBefore("@")
                    .replace(".", " ")
                    .replace("_", " ")
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                    .ifBlank { "Gmail User" }
                errorMessage = null
                onGoogleSignInSuccess(name, cleanEmail, null)
            } else {
                errorMessage = "No Gmail account was selected from the chooser."
            }
        } else {
            errorMessage = "Account chooser was closed. You can also sign in by typing your Gmail address below."
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Decorative background gradient glows
        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-60).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PurplePrimary.copy(alpha = 0.28f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // App Brand Logo & Name
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = PurplePrimaryDark,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PurpleNeonGlow),
                modifier = Modifier.size(76.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "MVA Logo",
                        tint = PurpleNeonGlow,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "MVA Business Chat",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Zero-Trust Enterprise Communication • Gmail Exclusive",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Gmail Only Notice Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PurpleSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = PurpleE2EEGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Gmail Authentication & Registration",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurpleE2EEGreen
                        )
                        Text(
                            text = "Sign in or register exclusively with your Gmail account. You will set up your business workspace right after.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Auth Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PurpleSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = PurpleNeonGlow,
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Sign In / Register with Gmail",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Select your verified Google account from your device or enter your Gmail address.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. Android Native Account Chooser Button
                    Button(
                        onClick = {
                            errorMessage = null
                            isLoading = true
                            try {
                                val intent = googleHelper.createAccountPickerIntent()
                                accountPickerLauncher.launch(intent)
                            } catch (e: Exception) {
                                scope.launch {
                                    when (val result = googleHelper.signInWithGoogle()) {
                                        is GoogleAuthResult.Success -> {
                                            isLoading = false
                                            val cleanEmail = result.email.trim().lowercase()
                                            if (!cleanEmail.endsWith("@gmail.com")) {
                                                errorMessage = "Registration and sign-in is exclusively for Gmail (@gmail.com) accounts."
                                                return@launch
                                            }
                                            onGoogleSignInSuccess(
                                                result.displayName ?: "Gmail User",
                                                cleanEmail,
                                                result.profilePictureUri
                                            )
                                        }
                                        is GoogleAuthResult.Cancelled -> {
                                            isLoading = false
                                            errorMessage = "Please select your Gmail account or enter it below."
                                        }
                                        is GoogleAuthResult.Error -> {
                                            isLoading = false
                                            errorMessage = result.errorMessage
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_login_submit_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Choose Google / Gmail Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Separator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(modifier = Modifier.weight(1f), color = PurpleBorder)
                        Text(
                            text = "  OR ENTER GMAIL ADDRESS  ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Divider(modifier = Modifier.weight(1f), color = PurpleBorder)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Direct Gmail Address Input
                    OutlinedTextField(
                        value = gmailInput,
                        onValueChange = {
                            gmailInput = it
                            errorMessage = null
                        },
                        label = { Text("Gmail Address (@gmail.com)", fontSize = 12.sp) },
                        placeholder = { Text("yourname@gmail.com", fontSize = 12.sp, color = TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (gmailInput.isNotBlank()) {
                                IconButton(onClick = { gmailInput = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_workspace_email_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleNeonGlow,
                            unfocusedBorderColor = PurpleBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkCardBackground,
                            unfocusedContainerColor = DarkCardBackground
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick suggestion chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PurplePrimaryDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                            modifier = Modifier
                                .clickable {
                                    gmailInput = "merversaus@gmail.com"
                                    errorMessage = null
                                }
                        ) {
                            Text(
                                text = "merversaus@gmail.com",
                                fontSize = 11.sp,
                                color = PurpleNeonGlow,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0x33EF4444),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66EF4444)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 12.sp,
                                color = Color(0xFFFCA5A5),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val cleanEmail = gmailInput.trim().lowercase()
                            if (cleanEmail.isBlank()) {
                                errorMessage = "Please enter your Gmail address."
                                return@Button
                            }
                            if (!cleanEmail.endsWith("@gmail.com")) {
                                errorMessage = "Registration and sign-in is exclusively for Gmail (@gmail.com) accounts."
                                return@Button
                            }
                            errorMessage = null
                            val name = cleanEmail.substringBefore("@")
                                .replace(".", " ")
                                .replace("_", " ")
                                .split(" ")
                                .filter { it.isNotBlank() }
                                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                                .ifBlank { "Gmail User" }
                            onGoogleSignInSuccess(name, cleanEmail, null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("google_direct_email_submit_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B21A8))
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Continue with Gmail", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PurpleE2EEGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Encrypted Workspace • Firestore Cloud Sync",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}
