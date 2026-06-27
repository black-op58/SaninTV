package com.sanin.tv.feature_search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.RangeSlider

/**
 * Feature 6: Advanced Search Filters — Bottom Sheet UI
 * Provides year, season, score range, source, status, format, and on-list filters.
 */
class AdvancedSearchFilterBottomSheet : BottomSheetDialogFragment() {
    var onFiltersApplied: ((AdvancedSearchFilter) -> Unit)? = null
    private var currentFilter = AdvancedSearchFilter.EMPTY

    companion object {
    fun newInstance(current: AdvancedSearchFilter = AdvancedSearchFilter.EMPTY): AdvancedSearchFilterBottomSheet {
    return AdvancedSearchFilterBottomSheet().apply {
                currentFilter = current
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val ctx = requireContext()
        val scroll = ScrollView(ctx)
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 48)
        }
        scroll.addView(root)

        root.addView(TextView(ctx).apply {
            text = "Advanced Filters"
            textSize = 20f
        })

        // Year spinner
        root.addView(TextView(ctx).apply { text = "Season Year"; textSize = 13f; setPadding(0, 16, 0, 0) })
        val yearLabels = listOf("Any") + AdvancedSearchFilter.YEARS.map { it.toString() }
        val yearSpinner = Spinner(ctx).also {
            it.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, yearLabels)
            val current = currentFilter.seasonYear
            it.setSelection(if (current == null) 0 else yearLabels.indexOf(current.toString()).takeIf { i -> i >= 0 } ?: 0)
        }
        root.addView(yearSpinner)

        // Season spinner
        root.addView(TextView(ctx).apply { text = "Season"; textSize = 13f; setPadding(0, 16, 0, 0) })
        val seasonLabels = listOf("Any") + AdvancedSearchFilter.SEASONS
        val seasonSpinner = Spinner(ctx).also {
            it.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, seasonLabels)
            it.setSelection(if (currentFilter.season == null) 0 else seasonLabels.indexOf(currentFilter.season).coerceAtLeast(0))
        }
        root.addView(seasonSpinner)

        // Score range (RangeSlider requires Material library)
        root.addView(TextView(ctx).apply { text = "Score Range (0–100)"; textSize = 13f; setPadding(0, 16, 0, 0) })
        val scoreLabel = TextView(ctx).apply {
            text = "${currentFilter.minScore ?: 0} – ${currentFilter.maxScore ?: 100}"
        }
        root.addView(scoreLabel)

        // Source spinner
        root.addView(TextView(ctx).apply { text = "Source"; textSize = 13f; setPadding(0, 16, 0, 0) })
        val sourceLabels = listOf("Any") + AdvancedSearchFilter.SOURCES
        val sourceSpinner = Spinner(ctx).also {
            it.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, sourceLabels)
            it.setSelection(if (currentFilter.source == null) 0 else sourceLabels.indexOf(currentFilter.source).coerceAtLeast(0))
        }
        root.addView(sourceSpinner)

        // Status spinner
        root.addView(TextView(ctx).apply { text = "Airing Status"; textSize = 13f; setPadding(0, 16, 0, 0) })
        val statusLabels = listOf("Any") + AdvancedSearchFilter.STATUSES
        val statusSpinner = Spinner(ctx).also {
            it.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, statusLabels)
            it.setSelection(if (currentFilter.status == null) 0 else statusLabels.indexOf(currentFilter.status).coerceAtLeast(0))
        }
        root.addView(statusSpinner)

        // Format spinner
        root.addView(TextView(ctx).apply { text = "Format"; textSize = 13f; setPadding(0, 16, 0, 0) })
        val formatLabels = listOf("Any") + AdvancedSearchFilter.FORMATS
        val formatSpinner = Spinner(ctx).also {
            it.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, formatLabels)
            it.setSelection(if (currentFilter.format == null) 0 else formatLabels.indexOf(currentFilter.format).coerceAtLeast(0))
        }
        root.addView(formatSpinner)

        // On my list checkbox
        val onListCheck = CheckBox(ctx).apply {
            text = "On My List Only"
            isChecked = currentFilter.onMyList
            setPadding(0, 16, 0, 0)
        }
        root.addView(onListCheck)

        // Action buttons
        val btnRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 24, 0, 0)
        }
        root.addView(btnRow)

        val resetBtn = Button(ctx).apply {
            text = "Reset"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        btnRow.addView(resetBtn)

        val applyBtn = Button(ctx).apply {
            text = "Apply"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        btnRow.addView(applyBtn)

        fun buildFilter(): AdvancedSearchFilter {
    return AdvancedSearchFilter(
                seasonYear = yearSpinner.selectedItem.toString().toIntOrNull(),
                season = seasonSpinner.selectedItem.toString().takeIf { it != "Any" },
                source = sourceSpinner.selectedItem.toString().takeIf { it != "Any" },
                status = statusSpinner.selectedItem.toString().takeIf { it != "Any" },
                format = formatSpinner.selectedItem.toString().takeIf { it != "Any" },
                onMyList = onListCheck.isChecked
            )
        }

        applyBtn.setOnClickListener {
            onFiltersApplied?.invoke(buildFilter())
            dismiss()
        }

        resetBtn.setOnClickListener {
            onFiltersApplied?.invoke(AdvancedSearchFilter.EMPTY)
            dismiss()
        }

        return scroll
    }
}
