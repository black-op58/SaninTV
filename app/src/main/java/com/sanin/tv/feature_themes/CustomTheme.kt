package com.sanin.tv.feature_themes

import java.io.Serializable

/**
 * Feature 4: Custom Themes Builder
 * Represents a user-designed color theme. Colors stored as ARGB hex strings e.g. "#FF1A1A2E".
 */
data class CustomTheme(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val primaryColor: String = "#FF6200EE",
    val secondaryColor: String = "#FF03DAC5",
    val backgroundColor: String = "#FF121212",
    val surfaceColor: String = "#FF1E1E1E",
    val onPrimaryColor: String = "#FFFFFFFF",
    val onBackgroundColor: String = "#FFFFFFFF",
    val accentColor: String = "#FFBB86FC",
    val createdAt: Long = System.currentTimeMillis()
) : Serializable {
    companion object {
    private const val serialVersionUID = 1L

        val PRESETS = listOf(
            CustomTheme(name = "Midnight Purple", primaryColor = "#FF6200EE", accentColor = "#FFBB86FC", backgroundColor = "#FF0D0D0D"),
            CustomTheme(name = "Ocean Blue", primaryColor = "#FF1565C0", accentColor = "#FF82B1FF", backgroundColor = "#FF0A0A1A"),
            CustomTheme(name = "Rose Gold", primaryColor = "#FFE91E63", accentColor = "#FFFFAB91", backgroundColor = "#FF1A0A0A"),
            CustomTheme(name = "Forest Green", primaryColor = "#FF2E7D32", accentColor = "#FFA5D6A7", backgroundColor = "#FF0A1A0A"),
        )
    }
}
