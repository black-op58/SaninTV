package com.sanin.tv.home

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.sanin.tv.connections.tmdb.TmdbApi
import com.sanin.tv.databinding.ItemContinueWatchingHeroBinding
import com.sanin.tv.feature_continue.LastScreenshotManager
import com.sanin.tv.loadImage
import com.sanin.tv.media.Media
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.settings.saving.SourceMemoryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Single-item adapter for the hero "Continue Watching" card at the top of Home.
 *
 * Features:
 *  - Banner / optional last-watched screenshot backdrop
 *  - TMDB title logo art (loaded async, fades in — no API key required)
 *  - Cover thumbnail, episode progress, minutes remaining
 *  - Next episode air countdown
 *  - Source-memory indicator badge
 *  - One-tap resume: launches MediaDetailsActivity with autoResume=true
 *  - D-pad focus: subtle scale animation
 */
class ContinueWatchingHeroAdapter(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onResume: (media: Media) -> Unit
) : RecyclerView.Adapter<ContinueWatchingHeroAdapter.HeroViewHolder>() {

    private var media: Media? = null

    fun submitMedia(newMedia: Media?) {
        media = newMedia
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = if (media != null) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeroViewHolder {
        val binding = ItemContinueWatchingHeroBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HeroViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeroViewHolder, position: Int) {
        media?.let { holder.bind(it) }
    }

    inner class HeroViewHolder(
        private val b: ItemContinueWatchingHeroBinding
    ) : RecyclerView.ViewHolder(b.root) {

        // Cancelled on every bind() call to prevent stale logo loads
        // appearing on a recycled ViewHolder showing a different title.
        private var logoJob: Job? = null

        fun bind(m: Media) {
            // Cancel any in-flight logo fetch from the previous bind
            logoJob?.cancel()

            // ── Images ───────────────────────────────────────────────────
            b.heroBanner.loadImage(m.banner ?: m.cover)
            b.heroCover.loadImage(m.cover)

            // Screenshot overlay
            val showScreenshot = PrefManager.getVal<Boolean>(PrefName.ContinueWatchingShowScreenshot)
            if (showScreenshot && LastScreenshotManager.exists(context, m.id)) {
                b.heroScreenshot.isVisible = true
                scope.launch {
                    val bmp: Bitmap? = withContext(Dispatchers.IO) {
                        LastScreenshotManager.load(context, m.id)
                    }
                    if (bmp != null) b.heroScreenshot.setImageBitmap(bmp)
                    else b.heroScreenshot.isVisible = false
                }
            } else {
                b.heroScreenshot.isVisible = false
            }

            // ── TMDB logo (async, fade in) ────────────────────────────────
            // Logo art is fetched server-side — no API key check needed.
            // Falls back to text title if the server is unreachable or has
            // no logo for this title (timeout = 5 s, handled in TmdbApi).
            b.heroLogo.alpha = 0f
            b.heroTitle.alpha = 1f
            b.heroTitle.text = m.userPreferredName

            logoJob = scope.launch {
                val logoUrl = TmdbApi.getLogoUrl(m.id, m.anime != null)
                if (!logoUrl.isNullOrBlank()) {
                    b.heroLogo.loadImage(logoUrl)
                    b.heroLogo.animate().alpha(1f).setDuration(400).start()
                    b.heroTitle.animate().alpha(0f).setDuration(200).start()
                }
            }

            // ── Text ─────────────────────────────────────────────────────
            val progress = m.userProgress ?: 0
            val totalEps = m.anime?.totalEpisodes
            b.heroEpisode.text = if (totalEps != null) "Episode $progress of $totalEps"
                                 else "Episode $progress"

            // ── Minutes remaining ─────────────────────────────────────────
            val epDuration = m.anime?.episodeDuration ?: 24
            val remaining  = if (totalEps != null && totalEps > progress)
                                (totalEps - progress) * epDuration
                             else null
            if (remaining != null && remaining > 0) {
                b.heroMinutesRemaining.isVisible = true
                b.heroMinutesRemaining.text = "~$remaining min remaining"
            } else {
                b.heroMinutesRemaining.isVisible = false
            }

            // ── Next episode air countdown ────────────────────────────────
            val timeUntilAiring = m.timeUntilAiring
            val nextEpNum       = m.anime?.nextAiringEpisode
            if (timeUntilAiring != null && timeUntilAiring > 0 && nextEpNum != null) {
                b.heroNextEp.isVisible = true
                b.heroNextEp.text = "Ep $nextEpNum in ${formatCountdown(timeUntilAiring)}"
            } else {
                b.heroNextEp.isVisible = false
            }

            // ── Progress bar ──────────────────────────────────────────────
            if (totalEps != null && totalEps > 0) {
                b.heroProgressBar.progress = ((progress.toFloat() / totalEps) * 100).toInt()
            } else {
                b.heroProgressBar.progress = 0
            }

            // ── Source memory badge ───────────────────────────────────────
            val expiryHours = PrefManager.getVal<Int>(PrefName.SourceMemoryExpiryHours)
            val hasMemory   = SourceMemoryManager.load(m.id, expiryHours) != null
            b.heroSourceMemory.isVisible = hasMemory

            // ── D-pad / click ─────────────────────────────────────────────
            b.root.setOnClickListener { onResume(m) }
            b.root.setOnFocusChangeListener { v, hasFocus ->
                v.animate()
                    .scaleX(if (hasFocus) 1.03f else 1f)
                    .scaleY(if (hasFocus) 1.03f else 1f)
                    .setDuration(150)
                    .start()
            }
        }
    }

    private fun formatCountdown(seconds: Long): String {
        val days  = TimeUnit.SECONDS.toDays(seconds)
        val hours = TimeUnit.SECONDS.toHours(seconds) % 24
        val mins  = TimeUnit.SECONDS.toMinutes(seconds) % 60
        return when {
            days > 0  -> "${days}d ${hours}h"
            hours > 0 -> "${hours}h ${mins}m"
            else      -> "${mins}m"
        }
    }
}
