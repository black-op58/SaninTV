package com.sanin.tv.home

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.sanin.tv.R
import com.sanin.tv.connections.tmdb.TmdbApi
import com.sanin.tv.loadImage
import com.sanin.tv.media.Media
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * ViewPager2 adapter that powers the "Hero Carousel" at the top of the Home tab.
 *
 * Shows the 15 most popular anime of all time.  Each card displays:
 *   • Full-bleed banner image
 *   • Status badge, rating, first two genres
 *   • TMDB logo art (best English logo with aspect ratio > 1.2, or text title
 *     as fallback when no logo is available)
 *   • Synopsis (2-line preview)
 *   • "Watch Now" button
 *
 * Logo art is fetched from the SaninTV proxy — no user API key required.
 *
 * Auto-advances every [AUTO_ADVANCE_INTERVAL_MS] milliseconds.
 * Call [startAutoAdvance] after attaching to a ViewPager2 and
 * [stopAutoAdvance] in onDestroyView / onPause.
 */
class HeroCarouselAdapter(
    private val activity: FragmentActivity,
    val mediaList: List<Media>,
    private val onWatchClicked: (Media) -> Unit
) : RecyclerView.Adapter<HeroCarouselAdapter.CarouselViewHolder>() {

    companion object {
        private const val AUTO_ADVANCE_INTERVAL_MS = 5_000L
    }

    // ── Auto-advance ──────────────────────────────────────────────────────────

    private val handler = Handler(Looper.getMainLooper())
    private var viewPager: ViewPager2? = null
    private val advanceRunnable = object : Runnable {
        override fun run() {
            val vp = viewPager ?: return
            val next = (vp.currentItem + 1) % mediaList.size
            vp.setCurrentItem(next, true)
            handler.postDelayed(this, AUTO_ADVANCE_INTERVAL_MS)
        }
    }

    fun startAutoAdvance(vp: ViewPager2) {
        viewPager = vp
        handler.removeCallbacks(advanceRunnable)
        handler.postDelayed(advanceRunnable, AUTO_ADVANCE_INTERVAL_MS)
    }

    fun stopAutoAdvance() {
        handler.removeCallbacks(advanceRunnable)
        viewPager = null
    }

    // ── PageChangeCallback (pauses auto-advance during user swipe) ────────────

    val pageCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                handler.removeCallbacks(advanceRunnable)
            } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                handler.postDelayed(advanceRunnable, AUTO_ADVANCE_INTERVAL_MS)
            }
        }
    }

    // ── Adapter ───────────────────────────────────────────────────────────────

    override fun getItemCount(): Int = mediaList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hero_carousel, parent, false)
        return CarouselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        holder.bind(mediaList[position])
    }

    inner class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val banner:   ImageView = itemView.findViewById(R.id.carouselBanner)
        private val logo:     ImageView = itemView.findViewById(R.id.carouselLogo)
        private val title:    TextView  = itemView.findViewById(R.id.carouselTitle)
        private val status:   TextView  = itemView.findViewById(R.id.carouselStatus)
        private val rating:   TextView  = itemView.findViewById(R.id.carouselRating)
        private val genres:   TextView  = itemView.findViewById(R.id.carouselGenres)
        private val synopsis: TextView  = itemView.findViewById(R.id.carouselSynopsis)
        private val watchBtn: View      = itemView.findViewById(R.id.carouselWatchButton)

        // Cancelled whenever this ViewHolder is rebound, preventing stale
        // logo loads from appearing on recycled cards.
        private var logoJob: Job? = null

        fun bind(media: Media) {
            // Cancel any in-flight logo fetch from a previous bind
            logoJob?.cancel()

            // Banner image
            banner.loadImage(media.banner ?: media.cover)

            // Reset: show text title while logo loads (or if no logo exists)
            logo.setImageDrawable(null)
            logo.visibility  = View.GONE
            title.text       = media.userPreferredName
            title.visibility = View.VISIBLE

            // Metadata
            status.text  = media.status?.replace("_", " ") ?: ""
            rating.text  = media.meanScore?.let { "★ ${it / 10.0}" } ?: ""
            genres.text  = media.genres.take(2).joinToString(" • ")
            synopsis.text = media.description
                ?.replace(Regex("<.*?>"), "")   // strip HTML tags
                ?.take(200) ?: ""

            // Watch button
            watchBtn.setOnClickListener { onWatchClicked(media) }
            watchBtn.isFocusable = true
            watchBtn.isFocusableInTouchMode = false

            // Logo art — use the Activity's lifecycle scope so the fetch is
            // automatically cancelled when the Activity is destroyed, and
            // cancel the job manually on rebind to stop stale updates.
            logoJob = activity.lifecycleScope.launch {
                val logoUrl = TmdbApi.getLogoUrl(
                    anilistId = media.id,
                    isAnime   = media.anime != null
                )
                if (logoUrl != null) {
                    logo.loadImage(logoUrl)
                    logo.visibility  = View.VISIBLE
                    title.visibility = View.GONE
                } else {
                    logo.visibility  = View.GONE
                    title.visibility = View.VISIBLE
                }
            }
        }
    }
}
