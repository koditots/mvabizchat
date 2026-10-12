package com.example.ui.screens.auth

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun BusinessSetupScreen(
    userEmail: String,
    initialName: String,
    onBusinessCreated: (
        businessName: String,
        name: String,
        department: String,
        role: UserRole,
        phone: String,
        statusMessage: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var businessName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf(initialName.ifBlank { userEmail.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() } }) }
    var department by remember { mutableStateOf("Executive Management") }
    var role by remember { mutableStateOf(UserRole.OWNER) }
    var phone by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("Enterprise Active • Encrypted Workspace 🛡️") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val departments = listOf(
        "Executive Management",
        "Operations & SecOps",
        "Client Engineering & Product",
        "Finance & Commercial Strategy",
        "Global Logistics & Supply Chain",
        "Legal & Enterprise Compliance"
    )

    var isDepartmentMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Background glow
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-50).dp)
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
            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = PurplePrimaryDark,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PurpleNeonGlow),
                modifier = Modifier.size(76.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = "Business Setup",
                        tint = PurpleNeonGlow,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Establish Business Profile",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Signed in as: $userEmail",
                fontSize = 12.sp,
                color = PurpleAccent
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Fill in your enterprise and company details by hand to establish your executive workspace.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PurpleSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Business / Company Name
                    Text(text = "Business / Company Name *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it; errorMessage = null },
                        placeholder = { Text("e.g. Mervers Logistics, Apex Cloud Inc.", fontSize = 12.sp, color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.CorporateFare, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth().testTag("business_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleNeonGlow,
                            unfocusedBorderColor = PurpleBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Owner / Founder Full Name
                    Text(text = "Your Full Name / Founder Title *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it; errorMessage = null },
                        placeholder = { Text("Your full legal name", fontSize = 12.sp, color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth().testTag("owner_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleNeonGlow,
                            unfocusedBorderColor = PurpleBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Department Selection
                    Text(text = "Department / Division *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = department,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { isDepartmentMenuExpanded = !isDepartmentMenuExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Department", tint = PurpleAccent)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isDepartmentMenuExpanded = true }
                                .testTag("business_dept_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurpleNeonGlow,
                                unfocusedBorderColor = PurpleBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        DropdownMenu(
                            expanded = isDepartmentMenuExpanded,
                            onDismissRequest = { isDepartmentMenuExpanded = false }
                        ) {
                            departments.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text(dept, fontSize = 13.sp) },
                                    onClick = {
                                        department = dept
                                        isDepartmentMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Business Role Selection
                    Text(text = "Role in Business *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(UserRole.OWNER, UserRole.ADMIN, UserRole.MEMBER).forEach { r ->
                            val isSelected = role == r
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) PurplePrimary else PurpleSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PurpleNeonGlow else PurpleBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { role = r }
                            ) {
                                Text(
                                    text = r.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextMuted,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Phone Number
                    Text(text = "Business Phone / Hotline (Optional)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = { Text("+1 555-0199", fontSize = 12.sp, color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(18.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleNeonGlow,
                            unfocusedBorderColor = PurpleBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mission / Status Message
                    Text(text = "Enterprise Status Message", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = statusMessage,
                        onValueChange = { statusMessage = it },
                        placeholder = { Text("Available for business deliberations", fontSize = 12.sp, color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleNeonGlow,
                            unfocusedBorderColor = PurpleBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

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

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (businessName.isBlank()) {
                                errorMessage = "Please enter your Business / Company name."
                                return@Button
                            }
                            if (ownerName.isBlank()) {
                                errorMessage = "Please enter your name."
                                return@Button
                            }
                            onBusinessCreated(
                                businessName.trim(),
                                ownerName.trim(),
                                department,
                                role,
                                phone.trim(),
                                statusMessage.trim()
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("create_business_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Establish Business Workspace", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
