package com.sanin.tv.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sanin.tv.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Feature 8: Extension Health Dashboard
 *
 * Displays a list of all installed extension sources.  For each one it shows:
 *   - Source name and media type badge (Anime / Manga / Novel)
 *   - Status indicator: ● green (< 800ms), ● yellow (800–2000ms), ● red (timeout/error)
 *   - Response time in ms
 *   - "Last checked" timestamp
 *
 * Integration — add as a tab or menu item in ExtensionsActivity:
 *
 *   supportFragmentManager.commit {
 *       replace(R.id.fragmentContainer, ExtensionHealthFragment())
 *   }
 *
 * The fragment pings every source the moment it becomes visible, and exposes
 * a "Re-check all" button that re-runs all pings in parallel.
 */
class ExtensionHealthFragment : Fragment() {

    data class SourceHealth(
        val id: String,
        val name: String,
        val baseUrl: String,
        val mediaTypeBadge: String,
        val statusMs: Long = -1L,
        val checkedAt: Long = 0L,
        val error: String? = null
    ) {
        enum class Status { GOOD, SLOW, DOWN, UNCHECKED }

        val status: Status get() = when {
            checkedAt == 0L  -> Status.UNCHECKED
            error != null    -> Status.DOWN
            statusMs < 800   -> Status.GOOD
            statusMs < 2000  -> Status.SLOW
            else             -> Status.DOWN
        }

        val displayLatency: String get() = when {
            checkedAt == 0L -> "—"
            error != null   -> "Error"
            else            -> "${statusMs}ms"
        }

        val displayCheckedAt: String get() = when {
            checkedAt == 0L -> "Not checked yet"
            else            -> "Checked " + SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                .format(Date(checkedAt))
        }
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var recheckButton: TextView
    private val adapter = HealthAdapter()
    private var pingJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 0)
        }

        progressBar = ProgressBar(requireContext()).also { root.addView(it) }

        recheckButton = TextView(requireContext()).apply {
            text = "Re-check all"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            setPadding(48, 24, 48, 24)
            setOnClickListener { pingAllSources() }
        }
        root.addView(recheckButton)

        recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ExtensionHealthFragment.adapter
        }
        root.addView(recyclerView)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSources()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pingJob?.cancel()
    }

    private fun loadSources() {
        val sources = buildSourceList()
        adapter.submitList(sources)
        pingAllSources(sources)
    }

    /**
     * Build the list of sources from installed extensions.
     * Replace the stub entries below with your actual extension registry lookup,
     * e.g. iterating over ExtensionLoader.getAnimeExtensions() etc.
     */
    private fun buildSourceList(): List<SourceHealth> {
        val list = mutableListOf<SourceHealth>()

        try {
            val animeLoader = Class.forName("eu.kanade.tachiyomi.animesource.online.AnimeHttpSource")
            // Replace with your real extension registry — this is a stub
        } catch (_: ClassNotFoundException) { }

        return list.ifEmpty {
            listOf(
                SourceHealth("stub_1", "AnimeKai",   "https://animekai.to",     "ANIME"),
                SourceHealth("stub_2", "AllAnime",   "https://allanime.to",     "ANIME"),
                SourceHealth("stub_3", "MangaDex",   "https://api.mangadex.org","MANGA"),
                SourceHealth("stub_4", "NovelUpdates","https://www.novelupdates.com","NOVEL"),
            )
        }
    }

    private fun pingAllSources(sources: List<SourceHealth> = adapter.currentList) {
        pingJob?.cancel()
        progressBar.visibility = View.VISIBLE

        pingJob = viewLifecycleOwner.lifecycleScope.launch {
            val updated = sources.map { src ->
                async(Dispatchers.IO) {
                    pingSource(src)
                }
            }.awaitAll()

            progressBar.visibility = View.GONE
            adapter.submitList(updated)
        }
    }

    private fun pingSource(src: SourceHealth): SourceHealth {
        return try {
            val start = System.currentTimeMillis()
            val conn = (URL(src.baseUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout    = 5000
                requestMethod  = "HEAD"
                instanceFollowRedirects = true
            }
            conn.responseCode
            val elapsed = System.currentTimeMillis() - start
            conn.disconnect()
            src.copy(statusMs = elapsed, checkedAt = System.currentTimeMillis(), error = null)
        } catch (e: Exception) {
            src.copy(statusMs = -1L, checkedAt = System.currentTimeMillis(), error = e.message)
        }
    }

    private inner class HealthAdapter :
        ListAdapter<SourceHealth, HealthAdapter.VH>(DIFF) {

        inner class VH(val root: LinearLayout) : RecyclerView.ViewHolder(root) {
            val dot:     TextView = root.getChildAt(0) as TextView
            val name:    TextView = root.getChildAt(1) as TextView
            val badge:   TextView = root.getChildAt(2) as TextView
            val latency: TextView = root.getChildAt(3) as TextView
            val time:    TextView = root.getChildAt(4) as TextView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val ctx = parent.context
            val row = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(32, 20, 32, 20)
                addView(TextView(ctx).apply { textSize = 20f })  // dot
                addView(TextView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    setPadding(16, 0, 0, 0)
                })  // name
                addView(TextView(ctx).apply { setPadding(8, 0, 8, 0); textSize = 10f })  // badge
                addView(TextView(ctx).apply { setPadding(8, 0, 8, 0) })                  // latency
                addView(TextView(ctx).apply { textSize = 10f })                          // time
            }
            return VH(row)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val src = getItem(position)
            val ctx = holder.root.context

            val (dotChar, dotColor) = when (src.status) {
                SourceHealth.Status.GOOD      -> "●" to ContextCompat.getColor(ctx, android.R.color.holo_green_dark)
                SourceHealth.Status.SLOW      -> "●" to ContextCompat.getColor(ctx, android.R.color.holo_orange_dark)
                SourceHealth.Status.DOWN      -> "●" to ContextCompat.getColor(ctx, android.R.color.holo_red_dark)
                SourceHealth.Status.UNCHECKED -> "○" to ContextCompat.getColor(ctx, android.R.color.darker_gray)
            }

            holder.dot.text = dotChar
            holder.dot.setTextColor(dotColor)
            holder.name.text = src.name
            holder.badge.text = src.mediaTypeBadge
            holder.latency.text = src.displayLatency
            holder.time.text = src.displayCheckedAt
        }

        private val DIFF = object : DiffUtil.ItemCallback<SourceHealth>() {
            override fun areItemsSame(a: SourceHealth, b: SourceHealth) = a.id == b.id
            override fun areContentsSame(a: SourceHealth, b: SourceHealth) = a == b
        }
    }
}
