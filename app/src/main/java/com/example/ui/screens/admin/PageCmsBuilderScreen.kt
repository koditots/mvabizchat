package com.example.ui.screens.admin

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DynamicPageSection
import com.example.data.model.DynamicSectionItem
import com.example.data.model.SectionType
import com.example.data.repository.AppRepository
import com.example.ui.theme.*

@Composable
fun PageCmsBuilderScreen(
    repository: AppRepository,
    onClose: () -> Unit
) {
    val sections by repository.dynamicSections.collectAsState()
    var editingSection by remember { mutableStateOf<DynamicPageSection?>(null) }
    var showAddSectionDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .testTag("page_cms_builder_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Column {
                    Text(
                        text = "Page CMS & Section Builder",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Drag, drop & customize all page sections",
                        fontSize = 11.sp,
                        color = PurpleAccent
                    )
                }
            }

            IconButton(
                onClick = { repository.resetSectionsToDefault() },
                modifier = Modifier.testTag("reset_cms_button")
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = "Reset", tint = TextMuted)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Info Banner
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = PurpleSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DashboardCustomize, contentDescription = null, tint = PurpleNeonGlow, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Manage texts, images, background colors, videos, products, and jobs. Use handles to reorder sections. Changes render on Home immediately.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Add Section Palette FAB / Button
        Button(
            onClick = { showAddSectionDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("add_new_section_button"),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Section, Product or Job to Page", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Reorderable Sections List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(sections) { index, section ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PurpleSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (section.isVisible) PurplePrimary else PurpleBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cms_section_card_${section.id}")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Drag / Reorder arrows
                            Column {
                                IconButton(
                                    onClick = { if (index > 0) repository.moveSection(index, index - 1) },
                                    enabled = index > 0,
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Move Up",
                                        tint = if (index > 0) PurpleAccent else TextMuted
                                    )
                                }
                                IconButton(
                                    onClick = { if (index < sections.size - 1) repository.moveSection(index, index + 1) },
                                    enabled = index < sections.size - 1,
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Move Down",
                                        tint = if (index < sections.size - 1) PurpleAccent else TextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = PurplePrimaryDark
                                    ) {
                                        Text(
                                            text = section.type.name.replace("_", " "),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PurpleNeonGlow,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "#${index + 1}",
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = section.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = section.subtitle.ifEmpty { "${section.items.size} item(s)" },
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            // Visibility Toggle
                            Switch(
                                checked = section.isVisible,
                                onCheckedChange = { repository.toggleSectionVisibility(section.id) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PurplePrimary
                                ),
                                modifier = Modifier.testTag("toggle_visibility_${section.id}")
                            )

                            // Edit Button
                            IconButton(
                                onClick = { editingSection = section },
                                modifier = Modifier.testTag("edit_section_${section.id}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PurpleAccent)
                            }

                            // Delete Button
                            IconButton(onClick = { repository.deleteSection(section.id) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Section Dialog
    if (editingSection != null) {
        EditSectionDialog(
            section = editingSection!!,
            onDismiss = { editingSection = null },
            onSave = { updated ->
                repository.updateSection(updated)
                editingSection = null
            }
        )
    }

    // Add Section Dialog / Component Palette
    if (showAddSectionDialog) {
        AddSectionPaletteDialog(
            onDismiss = { showAddSectionDialog = false },
            onAddSection = { newSec ->
                repository.addSection(newSec)
                showAddSectionDialog = false
            }
        )
    }
}

@Composable
fun EditSectionDialog(
    section: DynamicPageSection,
    onDismiss: () -> Unit,
    onSave: (DynamicPageSection) -> Unit
) {
    var title by remember { mutableStateOf(section.title) }
    var subtitle by remember { mutableStateOf(section.subtitle) }
    var description by remember { mutableStateOf(section.description) }
    var bgStart by remember { mutableStateOf(section.bgGradientStart) }
    var bgEnd by remember { mutableStateOf(section.bgGradientEnd) }
    var mediaUrl by remember { mutableStateOf(section.mediaUrl) }
    var buttonText by remember { mutableStateOf(section.buttonText) }
    var buttonColor by remember { mutableStateOf(section.buttonColorHex) }
    var buttonLink by remember { mutableStateOf(section.buttonLink) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PurpleSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("edit_section_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Customize ${section.type.name.replace("_", " ")}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Section Title", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth().testTag("edit_section_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Subtitle / Category", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Background Gradient Colors (Hex):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PurpleAccent)
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = bgStart,
                        onValueChange = { bgStart = it },
                        label = { Text("Gradient Start") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = bgEnd,
                        onValueChange = { bgEnd = it },
                        label = { Text("Gradient End") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = mediaUrl,
                    onValueChange = { mediaUrl = it },
                    label = { Text("Media / Video / Image URL", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Action Button Configuration:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PurpleAccent)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = buttonText,
                    onValueChange = { buttonText = it },
                    label = { Text("Button Text") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = buttonColor,
                        onValueChange = { buttonColor = it },
                        label = { Text("Button Color Hex") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = buttonLink,
                        onValueChange = { buttonLink = it },
                        label = { Text("Button Link URL") },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onSave(
                            section.copy(
                                title = title,
                                subtitle = subtitle,
                                description = description,
                                bgGradientStart = bgStart,
                                bgGradientEnd = bgEnd,
                                mediaUrl = mediaUrl,
                                buttonText = buttonText,
                                buttonColorHex = buttonColor,
                                buttonLink = buttonLink
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_section_changes_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Save & Apply Changes", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AddSectionPaletteDialog(
    onDismiss: () -> Unit,
    onAddSection: (DynamicPageSection) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PurpleSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("add_section_palette_dialog")
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
                        Text(text = "Component Palette", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                        Text(text = "Choose section to add to page", fontSize = 11.sp, color = PurpleAccent)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val presets = listOf(
                    Triple(
                        "Product Showcase",
                        "Showcase hardware, tools or services",
                        SectionType.PRODUCT_SHOWCASE
                    ),
                    Triple(
                        "Careers & Open Roles",
                        "Job postings with application buttons",
                        SectionType.JOB_CAREERS
                    ),
                    Triple(
                        "Featured Video Section",
                        "Video player card with play button",
                        SectionType.FEATURED_VIDEO
                    ),
                    Triple(
                        "Hero Banner Section",
                        "Hero banner with image and CTA",
                        SectionType.HERO_BANNER
                    ),
                    Triple(
                        "Announcement Alert",
                        "Broadcast or company alert card",
                        SectionType.ANNOUNCEMENT_ALERT
                    )
                )

                presets.forEach { (name, desc, type) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PurpleSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable {
                                val newId = "sec_${System.currentTimeMillis()}"
                                val newSection = when (type) {
                                    SectionType.PRODUCT_SHOWCASE -> DynamicPageSection(
                                        id = newId,
                                        type = type,
                                        title = "New Enterprise Solutions",
                                        subtitle = "Deployable Modules",
                                        items = listOf(
                                            DynamicSectionItem("item_1", "Quantum Gateway", "$249.00", "$249.00", "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400", "https://mva.chat/p1", "Inquire")
                                        )
                                    )
                                    SectionType.JOB_CAREERS -> DynamicPageSection(
                                        id = newId,
                                        type = type,
                                        title = "Engineering Openings",
                                        subtitle = "Careers",
                                        items = listOf(
                                            DynamicSectionItem("job_1", "Systems Architect", "Core Engine", "Remote • Full-Time", "", "https://mva.chat/apply", "Apply")
                                        )
                                    )
                                    SectionType.FEATURED_VIDEO -> DynamicPageSection(
                                        id = newId,
                                        type = type,
                                        title = "Product Overview Video",
                                        subtitle = "Watch Online",
                                        mediaUrl = "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=800",
                                        isVideo = true,
                                        buttonText = "Watch Video"
                                    )
                                    SectionType.HERO_BANNER -> DynamicPageSection(
                                        id = newId,
                                        type = type,
                                        title = "High-Security Workspace",
                                        subtitle = "Enterprise Suite",
                                        buttonText = "Get Started"
                                    )
                                    else -> DynamicPageSection(
                                        id = newId,
                                        type = type,
                                        title = "Company Broadcast",
                                        description = "Critical company announcement will appear here."
                                    )
                                }
                                onAddSection(newSection)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text(text = desc, fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
