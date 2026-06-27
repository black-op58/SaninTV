package com.sanin.tv.home

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sanin.tv.databinding.ItemContinueWatchingBinding
import com.sanin.tv.loadImage
import com.sanin.tv.media.Media
import com.sanin.tv.media.MediaDetailsActivity
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.settings.saving.SourceMemoryManager
import com.sanin.tv.util.DpadHelper.enableDpadNavigation
import java.util.concurrent.TimeUnit

/**
 * Enhanced "Continue Watching" row adapter for AniList-backed [Media] items.
 *
 * Shows per-card:
 *  - Cover image
 *  - Episode progress (Ep N of M)
 *  - Minutes remaining (based on [Media.anime?.episodeDuration] × remaining eps)
 *  - Next episode air countdown (from [Media.timeUntilAiring])
 *  - Episode progress bar
 *  - D-pad focus highlight
 *
 * Tapping navigates to [MediaDetailsActivity] (no auto-resume — the hero card
 * above the row handles one-tap resume).
 */
class AnilistContinueAdapter(
    private val activity: Activity
) : ListAdapter<Media, AnilistContinueAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemContinueWatchingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val b: ItemContinueWatchingBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(m: Media) {
            // ── Cover ─────────────────────────────────────────────────
            b.cwCover.loadImage(m.cover)

            // ── Title ─────────────────────────────────────────────────
            b.cwTitle.text = m.userPreferredName

            // ── Episode label ─────────────────────────────────────────
            val progress = m.userProgress ?: 0
            val totalEps = m.anime?.totalEpisodes
            b.cwEpisode.text = if (totalEps != null) "Ep $progress of $totalEps"
                               else "Ep $progress"

            // ── Minutes remaining ─────────────────────────────────────
            val epDuration = m.anime?.episodeDuration ?: 24
            val remaining  = if (totalEps != null && totalEps > progress)
                                (totalEps - progress) * epDuration
                             else null
            if (remaining != null && remaining > 0) {
                b.cwMinutesRemaining.isVisible = true
                b.cwMinutesRemaining.text = "~$remaining min left"
            } else {
                b.cwMinutesRemaining.isVisible = false
            }

            // ── Next episode air countdown ────────────────────────────
            val timeUntil = m.timeUntilAiring
            val nextEpNum = m.anime?.nextAiringEpisode
            if (timeUntil != null && timeUntil > 0 && nextEpNum != null) {
                b.cwNextEp.isVisible = true
                b.cwNextEp.text = "Ep $nextEpNum in ${formatCountdown(timeUntil)}"
            } else {
                b.cwNextEp.isVisible = false
            }

            // ── Progress bar ──────────────────────────────────────────
            if (totalEps != null && totalEps > 0) {
                b.cwProgressBar.progress = ((progress.toFloat() / totalEps) * 100).toInt()
            } else {
                b.cwProgressBar.progress = 0
            }

            // ── D-pad focus scale ─────────────────────────────────────
            b.root.setOnFocusChangeListener { v, hasFocus ->
                v.animate()
                    .scaleX(if (hasFocus) 1.06f else 1f)
                    .scaleY(if (hasFocus) 1.06f else 1f)
                    .setDuration(120)
                    .start()
            }

            // ── Click → details (no auto-resume; hero card handles that) ──
            b.root.setOnClickListener {
                activity.startActivity(
                    Intent(activity, MediaDetailsActivity::class.java)
                        .putExtra("media", m)
                )
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Media>() {
            override fun areItemsTheSame(a: Media, b: Media) = a.id == b.id
            override fun areContentsTheSame(a: Media, b: Media) =
                a.userProgress == b.userProgress && a.timeUntilAiring == b.timeUntilAiring
        }
    }

    private fun formatCountdown(seconds: Long): String {
        val days  = TimeUnit.SECONDS.toDays(seconds)
        val hours = TimeUnit.SECONDS.toHours(seconds) % 24
        return when {
            days > 0  -> "${days}d ${hours}h"
            hours > 0 -> "${hours}h"
            else      -> "${TimeUnit.SECONDS.toMinutes(seconds)}m"
        }
    }
}
