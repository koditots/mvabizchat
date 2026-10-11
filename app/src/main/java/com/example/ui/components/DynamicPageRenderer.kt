package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DynamicPageSection
import com.example.data.model.DynamicSectionItem
import com.example.data.model.SectionType
import com.example.ui.theme.*

@Composable
fun DynamicPageRenderer(
    sections: List<DynamicPageSection>,
    onItemClick: (DynamicSectionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val visibleSections = sections.filter { it.isVisible }.sortedBy { it.sortOrder }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        visibleSections.forEach { section ->
            when (section.type) {
                SectionType.HERO_BANNER -> {
                    HeroBannerSection(section = section, onButtonClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(section.buttonLink))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    })
                }
                SectionType.PRODUCT_SHOWCASE -> {
                    ProductShowcaseSection(section = section, onItemClick = onItemClick)
                }
                SectionType.JOB_CAREERS -> {
                    JobCareersSection(section = section, onItemClick = onItemClick)
                }
                SectionType.FEATURED_VIDEO -> {
                    FeaturedVideoSection(section = section, onWatchClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(section.buttonLink))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    })
                }
                SectionType.ANNOUNCEMENT_ALERT -> {
                    AnnouncementSection(section = section)
                }
                SectionType.QUICK_ACTIONS_GRID, SectionType.SECURITY_E2EE_BADGE -> {
                    HeroBannerSection(section = section, onButtonClick = {})
                }
            }
        }
    }
}

@Composable
fun HeroBannerSection(
    section: DynamicPageSection,
    onButtonClick: () -> Unit
) {
    val bgStart = parseHex(section.bgGradientStart, Color(0xFF3B0764))
    val bgEnd = parseHex(section.bgGradientEnd, Color(0xFF090410))
    val btnColor = parseHex(section.buttonColorHex, PurplePrimary)

    Surface(
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dynamic_hero_section_${section.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(bgStart, bgEnd)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                if (section.subtitle.isNotEmpty()) {
                    Text(
                        text = section.subtitle.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurpleAccent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = section.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                if (section.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = section.description,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }

                if (section.mediaUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = section.mediaUrl,
                        contentDescription = section.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onButtonClick,
                    colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("hero_button_${section.id}")
                ) {
                    Text(
                        text = section.buttonText,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowOutward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ProductShowcaseSection(
    section: DynamicPageSection,
    onItemClick: (DynamicSectionItem) -> Unit
) {
    val bgStart = parseHex(section.bgGradientStart, Color(0xFF240046))
    val bgEnd = parseHex(section.bgGradientEnd, Color(0xFF140A24))
    val btnColor = parseHex(section.buttonColorHex, PurpleAccent)

    Surface(
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dynamic_product_section_${section.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(bgStart, bgEnd)))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = section.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (section.subtitle.isNotEmpty()) {
                        Text(text = section.subtitle, fontSize = 11.sp, color = PurpleAccent)
                    }
                }
                Icon(Icons.Default.Inventory2, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(section.items) { item ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = PurpleSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                        modifier = Modifier
                            .width(180.dp)
                            .clickable { onItemClick(item) }
                            .testTag("product_card_${item.id}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (item.imageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = item.imageUrl,
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Text(
                                text = item.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.subtitle,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.priceOrTag,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = PurpleNeonGlow
                                )
                                FilledTonalButton(
                                    onClick = { onItemClick(item) },
                                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = btnColor),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(item.buttonText, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JobCareersSection(
    section: DynamicPageSection,
    onItemClick: (DynamicSectionItem) -> Unit
) {
    val bgStart = parseHex(section.bgGradientStart, Color(0xFF1E1038))
    val bgEnd = parseHex(section.bgGradientEnd, Color(0xFF090410))

    Surface(
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dynamic_job_section_${section.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(bgStart, bgEnd)))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = section.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    if (section.subtitle.isNotEmpty()) {
                        Text(text = section.subtitle, fontSize = 11.sp, color = PurpleAccent)
                    }
                }
                Icon(Icons.Default.WorkOutline, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                section.items.forEach { job ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PurpleSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(job) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PurplePrimaryDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Engineering, contentDescription = null, tint = PurpleNeonGlow, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = job.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text(text = "${job.subtitle} • ${job.priceOrTag}", fontSize = 11.sp, color = TextSecondary)
                            }
                            Button(
                                onClick = { onItemClick(job) },
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(job.buttonText, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturedVideoSection(
    section: DynamicPageSection,
    onWatchClick: () -> Unit
) {
    val bgStart = parseHex(section.bgGradientStart, Color(0xFF2D124D))
    val bgEnd = parseHex(section.bgGradientEnd, Color(0xFF10002B))

    Surface(
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dynamic_video_section_${section.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(bgStart, bgEnd)))
                .padding(16.dp)
        ) {
            Text(text = section.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = section.subtitle, fontSize = 11.sp, color = PurpleAccent)

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onWatchClick),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = section.mediaUrl,
                    contentDescription = section.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                )
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(PurplePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }

            if (section.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = section.description, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
            }
        }
    }
}

@Composable
fun AnnouncementSection(section: DynamicPageSection) {
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
            Icon(Icons.Default.Campaign, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = section.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                Text(text = section.description, fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}

fun parseHex(hex: String, fallback: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}
