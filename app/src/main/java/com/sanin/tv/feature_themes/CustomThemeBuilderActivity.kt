package com.sanin.tv.feature_themes

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sanin.tv.snackString

/**
 * Feature 4: Custom Themes Builder
 * Full-screen activity for creating and managing custom color themes.
 * Color fields accept standard 6-digit hex values (#RRGGBB).
 */
class CustomThemeBuilderActivity : AppCompatActivity() {
    private var editingTheme: CustomTheme? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themeId = intent.getStringExtra("themeId")
        editingTheme = themeId?.let { id ->
            CustomThemeManager.getAllThemes().find { it.id == id }
        }

        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, statusBarHeight(), 48, 64)
        }
        scroll.addView(root)
        setContentView(scroll)

        root.addView(TextView(this).apply {
            text = if (editingTheme != null) "Edit Theme" else "Create Theme"
            textSize = 22f
        })

        val nameField = EditText(this).apply {
            hint = "Theme name"
            editingTheme?.let { setText(it.name) }
        }
        root.addView(nameField)

        val colorFields = mutableMapOf<String, EditText>()
        val colorDefs = listOf(
            "primary" to (editingTheme?.primaryColor ?: "#FF6200EE"),
            "secondary" to (editingTheme?.secondaryColor ?: "#FF03DAC5"),
            "background" to (editingTheme?.backgroundColor ?: "#FF121212"),
            "surface" to (editingTheme?.surfaceColor ?: "#FF1E1E1E"),
            "accent" to (editingTheme?.accentColor ?: "#FFBB86FC"),
            "onPrimary" to (editingTheme?.onPrimaryColor ?: "#FFFFFFFF"),
            "onBackground" to (editingTheme?.onBackgroundColor ?: "#FFFFFFFF"),
        )
        colorDefs.forEach { (label, default) ->
            root.addView(TextView(this).apply { text = label; textSize = 13f; setPadding(0, 16, 0, 0) })
            val field = EditText(this).apply {
                hint = "#AARRGGBB"
                setText(default)
            }
            colorFields[label] = field
            root.addView(field)

            val preview = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(48.dp(), 48.dp())
                setBackgroundColor(CustomThemeManager.parseColor(default))
            }
            root.addView(preview)
            field.addTextChangedListener(object : android.text.TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
    val hex = s.toString().trim()
                    if (hex.length == 7 || hex.length == 9) {
                        preview.setBackgroundColor(CustomThemeManager.parseColor(hex))
                    }
                }
            })
        }

        val saveBtn = Button(this).apply {
            text = "Save Theme"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.topMargin = 24 }
        }
        root.addView(saveBtn)

        val exportBtn = Button(this).apply {
            text = "Export Theme"
            isEnabled = editingTheme != null
        }
        root.addView(exportBtn)

        if (editingTheme == null) {
            root.addView(TextView(this).apply {
                text = "─── Presets ───"
                textSize = 14f
                setPadding(0, 32, 0, 8)
            })
            CustomTheme.PRESETS.forEach { preset ->
                val btn = Button(this).apply {
                    text = "Use "${preset.name}""
                    setOnClickListener {
                        nameField.setText(preset.name)
                        colorFields["primary"]?.setText(preset.primaryColor)
                        colorFields["secondary"]?.setText(preset.secondaryColor)
                        colorFields["background"]?.setText(preset.backgroundColor)
                        colorFields["surface"]?.setText(preset.surfaceColor)
                        colorFields["accent"]?.setText(preset.accentColor)
                    }
                }
                root.addView(btn)
            }
        }

        saveBtn.setOnClickListener {
    val name = nameField.text.toString().trim().ifEmpty { "My Theme" }
            val theme = CustomTheme(
                id = editingTheme?.id ?: java.util.UUID.randomUUID().toString(),
                name = name,
                primaryColor = colorFields["primary"]?.text.toString().trim(),
                secondaryColor = colorFields["secondary"]?.text.toString().trim(),
                backgroundColor = colorFields["background"]?.text.toString().trim(),
                surfaceColor = colorFields["surface"]?.text.toString().trim(),
                accentColor = colorFields["accent"]?.text.toString().trim(),
                onPrimaryColor = colorFields["onPrimary"]?.text.toString().trim(),
                onBackgroundColor = colorFields["onBackground"]?.text.toString().trim(),
            )
            CustomThemeManager.saveTheme(theme)
            snackString("Theme \"${theme.name}\" saved!")
            finish()
        }

        exportBtn.setOnClickListener {
            editingTheme?.let { t ->
                val json = CustomThemeManager.exportTheme(t)
                android.content.ClipboardManager::class.java.let {
    val cm = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("theme", json))
                    snackString("Theme JSON copied to clipboard!")
                }
            }
        }
    }

    private fun statusBarHeight(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    private fun Int.dp() = (this * resources.displayMetrics.density).toInt()
}
