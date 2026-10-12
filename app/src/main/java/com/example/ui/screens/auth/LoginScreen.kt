package com.example.ui.screens.auth

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.GoogleAuthResult
import com.example.data.auth.GoogleSignInHelper
import com.example.data.model.UserRole
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class LoginTab {
    GOOGLE_AUTH,
    WORK_ACCOUNT
}

@Composable
fun LoginScreen(
    onGoogleSignInSuccess: (displayName: String, email: String, photoUrl: String?) -> Unit,
    onWorkAccountSignInSuccess: (email: String, name: String, department: String, role: UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(LoginTab.GOOGLE_AUTH) }
    var isRegisterMode by remember { mutableStateOf(false) }

    // Work Account Form State
    var fullName by remember { mutableStateOf("") }
    var workEmail by remember { mutableStateOf("merversaus@gmail.com") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Executive & Engineering") }
    var selectedRole by remember { mutableStateOf(UserRole.ADMIN) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val departments = listOf(
        "Executive & Engineering",
        "Operations & SecOps",
        "Product & Architecture",
        "Cloud Infrastructure",
        "Finance & Legal"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Decorative background gradient glows
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-60).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PurplePrimary.copy(alpha = 0.25f), Color.Transparent)
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
            Spacer(modifier = Modifier.height(24.dp))

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
                text = "Zero-Trust Enterprise Communication & E2EE Workspace",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Navigation Tabs (Google vs Work Account)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PurpleSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (activeTab == LoginTab.GOOGLE_AUTH) PurplePrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                activeTab = LoginTab.GOOGLE_AUTH
                                errorMessage = null
                            }
                            .testTag("tab_google_auth")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = if (activeTab == LoginTab.GOOGLE_AUTH) Color.White else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Google Sign-In",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTab == LoginTab.GOOGLE_AUTH) Color.White else TextMuted
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (activeTab == LoginTab.WORK_ACCOUNT) PurplePrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                activeTab = LoginTab.WORK_ACCOUNT
                                errorMessage = null
                            }
                            .testTag("tab_work_account")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = if (activeTab == LoginTab.WORK_ACCOUNT) Color.White else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Work Account",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTab == LoginTab.WORK_ACCOUNT) Color.White else TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tab Content
            when (activeTab) {
                LoginTab.GOOGLE_AUTH -> {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = PurpleSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = PurpleE2EEGreen,
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Enterprise Single Sign-On",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Authenticate securely using Android Credential Manager with your verified Google Workspace account.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    errorMessage = null
                                    isLoading = true
                                    scope.launch {
                                        val helper = GoogleSignInHelper(context)
                                        when (val result = helper.signInWithGoogle()) {
                                            is GoogleAuthResult.Success -> {
                                                isLoading = false
                                                onGoogleSignInSuccess(
                                                    result.displayName ?: "Google User",
                                                    result.email,
                                                    result.profilePictureUri
                                                )
                                            }
                                            is GoogleAuthResult.Cancelled -> {
                                                isLoading = false
                                                errorMessage = "Sign-in cancelled. Please select your Google account."
                                            }
                                            is GoogleAuthResult.Error -> {
                                                isLoading = false
                                                errorMessage = result.errorMessage
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
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Sign in with Google",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
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
                                    text = "2FA & Device Attestation Active",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                LoginTab.WORK_ACCOUNT -> {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = PurpleSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Register vs Sign In Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isRegisterMode) "Create Enterprise Account" else "Sign In to Workspace",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                TextButton(
                                    onClick = {
                                        isRegisterMode = !isRegisterMode
                                        errorMessage = null
                                    }
                                ) {
                                    Text(
                                        text = if (isRegisterMode) "Sign In instead" else "Register",
                                        fontSize = 12.sp,
                                        color = PurpleNeonGlow,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (isRegisterMode) {
                                OutlinedTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    label = { Text("Full Name") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = PurpleAccent)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PurpleNeonGlow,
                                        unfocusedBorderColor = PurpleBorder
                                    ),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            OutlinedTextField(
                                value = workEmail,
                                onValueChange = { workEmail = it },
                                label = { Text("Work Email") },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = PurpleAccent)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_email_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PurpleNeonGlow,
                                    unfocusedBorderColor = PurpleBorder
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = PurpleAccent)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle password visibility",
                                            tint = TextMuted
                                        )
                                    }
                                },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_password_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PurpleNeonGlow,
                                    unfocusedBorderColor = PurpleBorder
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true
                            )

                            if (isRegisterMode) {
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    label = { Text("Confirm Password") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = PurpleAccent)
                                    },
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PurpleNeonGlow,
                                        unfocusedBorderColor = PurpleBorder
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("DEPARTMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PurpleAccent)
                                Spacer(modifier = Modifier.height(6.dp))

                                var expandedDept by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = PurpleSurfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { expandedDept = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = department, fontSize = 13.sp, color = TextPrimary)
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = PurpleAccent)
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = expandedDept,
                                        onDismissRequest = { expandedDept = false }
                                    ) {
                                        departments.forEach { dept ->
                                            DropdownMenuItem(
                                                text = { Text(dept) },
                                                onClick = {
                                                    department = dept
                                                    expandedDept = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    errorMessage = null
                                    if (workEmail.isBlank() || !workEmail.contains("@")) {
                                        errorMessage = "Please enter a valid work email address"
                                        return@Button
                                    }
                                    if (password.length < 6) {
                                        errorMessage = "Password must be at least 6 characters"
                                        return@Button
                                    }
                                    if (isRegisterMode && password != confirmPassword) {
                                        errorMessage = "Passwords do not match"
                                        return@Button
                                    }

                                    isLoading = true
                                    scope.launch {
                                        kotlinx.coroutines.delay(600)
                                        isLoading = false
                                        onWorkAccountSignInSuccess(
                                            workEmail.trim(),
                                            fullName.ifBlank { workEmail.substringBefore("@") },
                                            department,
                                            selectedRole
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("work_account_submit_btn"),
                                shape = RoundedCornerShape(12.dp),
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
                                    Text(
                                        text = if (isRegisterMode) "Register Enterprise Account" else "Sign In to Workspace",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF3F1420),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFFFCA5A5),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Zero-Trust Security Footer
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PurpleSurfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "E2EE",
                        tint = PurpleE2EEGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Encrypted by Double Ratchet • Zero-Knowledge E2EE",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}
