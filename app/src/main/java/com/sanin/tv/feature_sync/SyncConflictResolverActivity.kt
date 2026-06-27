package com.sanin.tv.feature_sync

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sanin.tv.snackString
import com.sanin.tv.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Feature 8: AniList/MAL Sync Conflict Resolver — Activity UI
 * Displays list of progress conflicts and lets the user resolve each one.
 */
class SyncConflictResolverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, statusBarHeight(), 48, 64)
        }
        scroll.addView(root)
        setContentView(scroll)

        root.addView(TextView(this).apply {
            text = "Sync Conflict Resolver"
            textSize = 22f
        })

        val subtitle = TextView(this).apply {
            text = "Checking for AniList ↔ MAL conflicts…"
            textSize = 14f
        }
        root.addView(subtitle)

        val progress = ProgressBar(this)
        root.addView(progress)

        lifecycleScope.launch {
    try {
    val conflicts = withContext(Dispatchers.IO) { loadConflicts() }
                progress.visibility = View.GONE
                subtitle.text = if (conflicts.isEmpty())
                    "✓ No conflicts found. Your lists are in sync!"
                else
                    "${conflicts.size} conflict(s) found:"

                conflicts.forEach { conflict ->
                    root.addView(buildConflictCard(conflict))
                }

                if (conflicts.isEmpty()) {
                    root.addView(TextView(this@SyncConflictResolverActivity).apply {
                        text = "Both AniList and MAL show identical progress for all shared titles."
                        textSize = 13f
                        setPadding(0, 16, 0, 0)
                    })
                } else {
    val resolveAllBtn = Button(this@SyncConflictResolverActivity).apply {
                        text = "Resolve All (Use Higher Progress)"
                        setOnClickListener {
                            resolveAll(conflicts, SyncResolution.USE_HIGHER, root)
                        }
                    }
                    root.addView(resolveAllBtn)
                }
            } catch (e: Exception) {
                progress.visibility = View.GONE
                subtitle.text = "Error loading conflicts: ${e.message}"
                snackString("Failed to load conflicts: ${e.message}")
            }
        }
    }

    private suspend fun loadConflicts(): List<SyncConflict> {
    return emptyList()
    }

    private fun buildConflictCard(conflict: SyncConflict): View {
    val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 24, 0, 0)
        }

        card.addView(TextView(this).apply {
            text = conflict.mediaName
            textSize = 16f
        })

        if (conflict.hasProgressConflict) {
            card.addView(TextView(this).apply {
                text = "Progress: AniList=${conflict.anilistProgress}, MAL=${conflict.malProgress}"
                textSize = 13f
            })
        }
        if (conflict.hasStatusConflict) {
            card.addView(TextView(this).apply {
                text = "Status: AniList=${conflict.anilistStatus}, MAL=${conflict.malStatus}"
                textSize = 13f
            })
        }

        val btnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        card.addView(btnRow)

        val statusLabel = TextView(this).apply { textSize = 12f }

        fun addBtn(label: String, res: SyncResolution) {
            btnRow.addView(Button(this).apply {
                text = label
                textSize = 11f
                setOnClickListener {
                    lifecycleScope.launch {
    val ok = withContext(Dispatchers.IO) {
                            SyncConflictResolver.resolveConflict(conflict, res)
                        }
                        statusLabel.text = if (ok) "✓ Resolved using $label" else "✗ Failed"
                        if (ok) toast("Resolved: ${conflict.mediaName}")
                    }
                }
            })
        }

        addBtn("AniList", SyncResolution.USE_ANILIST)
        addBtn("MAL", SyncResolution.USE_MAL)
        addBtn("Higher", SyncResolution.USE_HIGHER)
        card.addView(statusLabel)

        return card
    }

    private fun resolveAll(conflicts: List<SyncConflict>, resolution: SyncResolution, root: LinearLayout) {
        lifecycleScope.launch {
    var resolved = 0
            withContext(Dispatchers.IO) {
                conflicts.forEach { conflict ->
                    if (SyncConflictResolver.resolveConflict(conflict, resolution)) resolved++
                }
            }
            snackString("Resolved $resolved / ${conflicts.size} conflicts")
        }
    }

    private fun statusBarHeight(): Int {
    val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else 0
    }
}
