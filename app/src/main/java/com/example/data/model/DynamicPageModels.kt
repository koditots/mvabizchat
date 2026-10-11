package com.example.data.model

enum class SectionType {
    HERO_BANNER,
    ANNOUNCEMENT_ALERT,
    PRODUCT_SHOWCASE,
    JOB_CAREERS,
    FEATURED_VIDEO,
    QUICK_ACTIONS_GRID,
    SECURITY_E2EE_BADGE
}

data class DynamicSectionItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val priceOrTag: String = "",
    val imageUrl: String = "",
    val linkUrl: String = "",
    val buttonText: String = "View"
)

data class DynamicPageSection(
    val id: String,
    val type: SectionType,
    val title: String,
    val subtitle: String = "",
    val description: String = "",
    val bgGradientStart: String = "#240046",
    val bgGradientEnd: String = "#090410",
    val mediaUrl: String = "",
    val isVideo: Boolean = false,
    val buttonText: String = "Explore",
    val buttonColorHex: String = "#A855F7",
    val buttonLink: String = "https://mva-business.enterprise.io",
    val isVisible: Boolean = true,
    val sortOrder: Int = 0,
    val items: List<DynamicSectionItem> = emptyList()
)
