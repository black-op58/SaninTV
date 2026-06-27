package com.sanin.tv.feature_themes

import android.graphics.Color
import com.sanin.tv.settings.saving.PrefManager

/**
 * Feature 4: Custom Themes Builder
 * Manages saving, loading, applying, and exporting user-created themes.
 */
object CustomThemeManager {
    private const val KEY_THEMES = "custom_themes_list"
    private const val KEY_ACTIVE_THEME = "custom_theme_active_id"

    @Suppress("UNCHECKED_CAST")
    fun getAllThemes(): List<CustomTheme> {
    return (PrefManager.getNullableCustomVal(KEY_THEMES, null, List::class.java)
            as? List<CustomTheme>) ?: CustomTheme.PRESETS
    }

    fun saveTheme(theme: CustomTheme) {
    val current = getAllThemes().toMutableList()
        val existing = current.indexOfFirst { it.id == theme.id }
        if (existing >= 0) current[existing] = theme else current.add(theme)
        PrefManager.setCustomVal(KEY_THEMES, current)
    }

    fun deleteTheme(themeId: String) {
    val current = getAllThemes().toMutableList()
        current.removeAll { it.id == themeId }
        PrefManager.setCustomVal(KEY_THEMES, current)
        if (getActiveThemeId() == themeId) clearActiveTheme()
    }

    fun setActiveTheme(themeId: String) {
        PrefManager.setVal(KEY_ACTIVE_THEME, themeId)
    }

    fun getActiveThemeId(): String? =
        PrefManager.getNullableCustomVal(KEY_ACTIVE_THEME, null, String::class.java)

    fun getActiveTheme(): CustomTheme? {
    val id = getActiveThemeId() ?: return null
        return getAllThemes().find { it.id == id }
    }

    fun clearActiveTheme() {
        PrefManager.removeVal(KEY_ACTIVE_THEME)
    }

    /** Parse a hex color string safely, returning the default if invalid. */
    fun parseColor(hex: String, default: Int = Color.WHITE): Int = try {
        Color.parseColor(hex)
    } catch (e: IllegalArgumentException) {
        default
    }

    /** Export a theme to a shareable JSON-like string. */
    fun exportTheme(theme: CustomTheme): String {
    return """{"name":"${theme.name}","primary":"${theme.primaryColor}","secondary":"${theme.secondaryColor}","background":"${theme.backgroundColor}","surface":"${theme.surfaceColor}","accent":"${theme.accentColor}"}"""
    }

    /** Import a theme from an exported JSON string. Returns null on parse failure. */
    fun importTheme(json: String): CustomTheme? {
    return try {
    val name = Regex(""""name":"([^"]+)"""").find(json)?.groupValues?.get(1) ?: "Imported Theme"
            val primary = Regex(""""primary":"([^"]+)"""").find(json)?.groupValues?.get(1) ?: "#FF6200EE"
            val secondary = Regex(""""secondary":"([^"]+)"""").find(json)?.groupValues?.get(1) ?: "#FF03DAC5"
            val background = Regex(""""background":"([^"]+)"""").find(json)?.groupValues?.get(1) ?: "#FF121212"
            val surface = Regex(""""surface":"([^"]+)"""").find(json)?.groupValues?.get(1) ?: "#FF1E1E1E"
            val accent = Regex(""""accent":"([^"]+)"""").find(json)?.groupValues?.get(1) ?: "#FFBB86FC"
            CustomTheme(name = name, primaryColor = primary, secondaryColor = secondary,
                backgroundColor = background, surfaceColor = surface, accentColor = accent)
        } catch (e: Exception) {
            null
        }
    }
}
