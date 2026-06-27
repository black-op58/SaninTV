package com.sanin.tv.media

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.sanin.tv.R
import com.sanin.tv.blurImage
import com.sanin.tv.currActivity
import com.sanin.tv.databinding.ItemMediaCompactBinding
import com.sanin.tv.databinding.ItemMediaLargeBinding
import com.sanin.tv.databinding.ItemMediaPageBinding
import com.sanin.tv.databinding.ItemMediaPageSmallBinding
import com.sanin.tv.loadImage
import com.sanin.tv.setAnimation
import com.sanin.tv.setSafeOnClickListener
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.flaviofaria.kenburnsview.RandomTransitionGenerator
import androidx.lifecycle.lifecycleScope
import com.sanin.tv.connections.tmdb.TmdbApi
import com.sanin.tv.feature_newepisode.NewEpisodeBadgeManager
import kotlinx.coroutines.launch

import java.io.Serializable

class MediaAdaptor(
    var type: Int,
    private val mediaList: MutableList<Media>?,
    private val activity: FragmentActivity,
    private val matchParent: Boolean = false,
    private val viewPager: ViewPager2? = null,
    private val fav: Boolean = false,
    private val isOtherUser: Boolean = false,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val STYLE_COMPACT = 0
        const val STYLE_LARGE = 1
        const val STYLE_PAGE = 2
        const val STYLE_SMALL = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (type) {
            0 -> {
                val b = ItemMediaCompactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                val density = parent.context.resources.displayMetrics.density
                val isPortrait = PrefManager.getVal<Int>(PrefName.CardOrientation) == 1
                val imgW = ((if (isPortrait) 110 else 160) * density).toInt()
                val imgH = ((if (isPortrait) 160 else 90) * density).toInt()
                b.itemCompactImage.layoutParams = b.itemCompactImage.layoutParams.also {
                    it.width = imgW
                    it.height = imgH
                }
                MediaViewHolder(b)
            }
            1 -> MediaLargeViewHolder(
                ItemMediaLargeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            2 -> MediaPageViewHolder(
                ItemMediaPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            3 -> MediaPageSmallViewHolder(
                ItemMediaPageSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> throw IllegalArgumentException()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (type) {

            // ── Type 0: compact landscape card ──────────────────────────────
            0 -> {
                val b = (holder as MediaViewHolder).binding
                setAnimation(activity, b.root)
                val media = mediaList?.getOrNull(position)
                if (media != null) {
                    // Cover art (default) or banner art depending on user pref
                    val isPortrait0 = PrefManager.getVal<Int>(PrefName.CardOrientation) == 1
                    val useBanner = PrefManager.getVal<Int>(PrefName.CardImageType) == 1
                    val imageUrl = if (useBanner) (media.banner ?: media.cover) else media.cover
                    if (!isPortrait0 && !useBanner) {
                        // Landscape + portrait cover: blurred cover fills the frame as background,
                        // sharp cover sits centered on top — no black bars, no cropping, no stretching.
                        b.itemCompactImageBg.visibility = View.VISIBLE
                        blurImage(b.itemCompactImageBg, imageUrl)
                        b.itemCompactImage.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                    } else {
                        // Portrait card or banner image: centerCrop fills the frame cleanly.
                        b.itemCompactImageBg.visibility = View.GONE
                        b.itemCompactImage.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                    }
                    // Apply card roundness pref
                    val density0 = b.root.context.resources.displayMetrics.density
                    val cornerRadius0 = PrefManager.getVal<Int>(PrefName.CardRoundness) * density0
                    b.itemCompactCard.radius = cornerRadius0
                    b.itemCompactImage.loadImage(imageUrl)
                    // Green dot — only when pref enabled + actually airing
                    val isReleasing0 = media.status == currActivity()!!.getString(R.string.status_releasing)
                    b.itemCompactOngoing.isVisible =
                        PrefManager.getVal<Boolean>(PrefName.ShowReleasingIndicator) && isReleasing0
                    b.itemCompactTitle.text = media.userPreferredName
                    b.itemCompactLogoArt.visibility = View.GONE
                    b.itemCompactTitle.visibility = View.VISIBLE
                    val mediaId0 = media.id
                    b.itemCompactLogoArt.tag = mediaId0
                    activity.lifecycleScope.launch {
                        val logo = TmdbApi.getLogoUrl(mediaId0 ?: return@launch, media.anime != null)
                        if (b.itemCompactLogoArt.tag == mediaId0 && logo != null) {
                            b.itemCompactLogoArt.loadImage(logo)
                            b.itemCompactLogoArt.visibility = View.VISIBLE
                            b.itemCompactTitle.visibility = View.GONE
                        }
                    }
                    b.itemCompactScore.text =
                        ((if (media.userScore == 0) (media.meanScore ?: 0) else media.userScore) / 10.0).toString()
                    b.itemCompactScoreBG.background = ContextCompat.getDrawable(
                        b.root.context,
                        if (media.userScore != 0) R.drawable.item_user_score else R.drawable.item_score
                    )
                    // 3-part episode count: [watched primary] | [released white] | [total grey]
                    val watched0 = media.userProgress ?: 0
                    val released0 = if (isReleasing0) media.anime?.nextAiringEpisode
                                    else media.anime?.totalEpisodes
                    val total0 = media.anime?.totalEpisodes
                    b.itemCompactUserProgress.text = "$watched0"
                    b.itemCompactReleased.text = released0?.let { " | $it" } ?: ""
                    b.itemCompactReleased.isVisible = released0 != null
                    val totalText0 = when {
                        total0 != null && total0 != released0 -> " | $total0"
                        total0 == null && isReleasing0 -> " | ?"
                        else -> ""
                    }
                    b.itemCompactTotal.text = totalText0
                    b.itemCompactTotal.isVisible = totalText0.isNotEmpty()
                    // "New Episode" badge — visible when tracking + new eps since last visit (if pref enabled)
                    b.itemCompactNewEpisodeBadge.isVisible =
                        PrefManager.getVal<Boolean>(PrefName.ShowNewEpisodeBadge) &&
                        NewEpisodeBadgeManager.shouldShowBadge(media)
                    // Watch progress bar — shows how far through the anime the user is
                    val watched = media.userProgress ?: 0
                    val total = media.anime?.totalEpisodes ?: media.manga?.totalChapters
                    if (watched > 0 && total != null && total > 0) {
                        b.itemCompactWatchProgress.visibility = View.VISIBLE
                        b.itemCompactWatchProgress.progress = ((watched.toFloat() / total) * 100).toInt()
                    } else {
                        b.itemCompactWatchProgress.visibility = View.GONE
                    }
                    if (media.relation != null) {
                        b.itemCompactRelation.text = "${media.relation}  "
                        b.itemCompactType.visibility = View.VISIBLE
                        if (media.relation!!.contains("\n")) {
                            b.itemCompactRelation.apply {
                                isSingleLine = false
                                maxLines = 2
                                ellipsize = TextUtils.TruncateAt.START
                                includeFontPadding = false
                                setLineSpacing(0f, 0.9f)
                            }
                        }
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    if (position == mediaList!!.size - 2 && viewPager != null) viewPager.post {
                        val size = mediaList.size
                        mediaList.addAll(mediaList)
                        notifyItemRangeInserted(size - 1, mediaList.size)
                    }
                }
            }

            // ── Type 1: large full-width card (library list mode) ────────────
            // Banner background uses cover image (not the banner) per user preference.
            // Logo art is shown below the card, not inside it.
            1 -> {
                val b = (holder as MediaLargeViewHolder).binding
                setAnimation(activity, b.root)
                val media = mediaList?.getOrNull(position)
                if (media != null) {
                    // Respect CardImageType pref on large card too
                    val useBanner1 = PrefManager.getVal<Int>(PrefName.CardImageType) == 1
                    val imageUrl1 = if (useBanner1) (media.banner ?: media.cover) else media.cover
                    // Large card is portrait (108×160dp): cover fills it perfectly; banner (16:9) needs fitCenter.
                    b.itemCompactImage.scaleType = if (useBanner1)
                        android.widget.ImageView.ScaleType.FIT_CENTER
                    else
                        android.widget.ImageView.ScaleType.CENTER_CROP
                    b.itemCompactImage.loadImage(imageUrl1)
                    // Apply card roundness pref
                    val density1 = b.root.context.resources.displayMetrics.density
                    val cornerRadius1 = PrefManager.getVal<Int>(PrefName.CardRoundness) * density1
                    b.itemCompactCard.radius = cornerRadius1
                    // Blurred background always uses cover for the moody backdrop effect
                    blurImage(b.itemCompactBanner, media.cover)
                    // Green dot — only when pref enabled + actually airing
                    val isReleasing1 = media.status == currActivity()!!.getString(R.string.status_releasing)
                    b.itemCompactOngoing.isVisible =
                        PrefManager.getVal<Boolean>(PrefName.ShowReleasingIndicator) && isReleasing1
                    b.itemCompactTitle.text = media.userPreferredName
                    b.itemCompactTitle.visibility = View.VISIBLE
                    b.itemCompactLogoArt.visibility = View.GONE
                    val mediaId1 = media.id
                    b.itemCompactLogoArt.tag = mediaId1
                    activity.lifecycleScope.launch {
                        val logo = TmdbApi.getLogoUrl(mediaId1 ?: return@launch, media.anime != null)
                        if (b.itemCompactLogoArt.tag == mediaId1 && logo != null) {
                            b.itemCompactLogoArt.loadImage(logo)
                            b.itemCompactLogoArt.visibility = View.VISIBLE
                            b.itemCompactTitle.visibility = View.GONE
                        }
                    }
                    b.itemCompactScore.text =
                        ((if (media.userScore == 0) (media.meanScore ?: 0) else media.userScore) / 10.0).toString()
                    b.itemCompactScoreBG.background = ContextCompat.getDrawable(
                        b.root.context,
                        if (media.userScore != 0) R.drawable.item_user_score else R.drawable.item_score
                    )
                    // 3-part episode count: [watched primary] | [released white] | [total grey]
                    val watched1 = media.userProgress ?: 0
                    val totalEps1 = media.anime?.totalEpisodes ?: media.manga?.totalChapters
                    val released1 = if (isReleasing1) media.anime?.nextAiringEpisode else totalEps1
                    b.itemCompactUserProgress.text = "$watched1"
                    b.itemCompactReleased.text = released1?.let { " | $it" } ?: ""
                    b.itemCompactReleased.isVisible = released1 != null
                    val totalText1 = when {
                        totalEps1 != null && totalEps1 != released1 -> " | $totalEps1"
                        totalEps1 == null && isReleasing1 -> " | ?"
                        else -> ""
                    }
                    b.itemCompactTotal.text = totalText1
                    b.itemCompactTotal.isVisible = totalText1.isNotEmpty()
                    // "New Episode" badge — visible when tracking + new eps since last visit (if pref enabled)
                    b.itemCompactNewEpisodeBadge.isVisible =
                        PrefManager.getVal<Boolean>(PrefName.ShowNewEpisodeBadge) &&
                        NewEpisodeBadgeManager.shouldShowBadge(media)
                    if (media.relation != null) {
                        b.itemCompactRelation.text = "${media.relation}  "
                        b.itemCompactType.visibility = View.VISIBLE
                    }
                }
            }

            // ── Type 3: page small (trending/discover ViewPager) ─────────────
            3 -> {
                val b = (holder as MediaPageSmallViewHolder).binding
                val media = mediaList?.get(position)
                if (media != null) {
                    val bannerAnimations: Boolean = PrefManager.getVal(PrefName.BannerAnimations)
                    b.itemCompactImage.loadImage(media.cover)
                    if (bannerAnimations)
                        b.itemCompactBanner.setTransitionGenerator(
                            RandomTransitionGenerator(
                                (10000 + 15000 * ((PrefManager.getVal(PrefName.AnimationSpeed) as Float))).toLong(),
                                AccelerateDecelerateInterpolator()
                            )
                        )
                    blurImage(
                        if (bannerAnimations) b.itemCompactBanner else b.itemCompactBannerNoKen,
                        media.banner ?: media.cover
                    )
                    b.itemCompactOngoing.isVisible =
                        media.status == currActivity()!!.getString(R.string.status_releasing)
                    b.itemCompactTitle.text = media.userPreferredName
                    b.itemCompactLogoArt.visibility = View.GONE
                    b.itemCompactTitle.visibility = View.VISIBLE
                    val mediaId3 = media.id
                    b.itemCompactLogoArt.tag = mediaId3
                    activity.lifecycleScope.launch {
                        val logo = TmdbApi.getLogoUrl(mediaId3 ?: return@launch, media.anime != null)
                        if (b.itemCompactLogoArt.tag == mediaId3 && logo != null) {
                            b.itemCompactLogoArt.loadImage(logo)
                            b.itemCompactLogoArt.visibility = View.VISIBLE
                            b.itemCompactTitle.visibility = View.GONE
                        }
                    }
                    b.itemCompactScore.text =
                        ((if (media.userScore == 0) (media.meanScore ?: 0) else media.userScore) / 10.0).toString()
                    b.itemCompactScoreBG.background = ContextCompat.getDrawable(
                        b.root.context,
                        if (media.userScore != 0) R.drawable.item_user_score else R.drawable.item_score
                    )
                    media.genres.apply {
                        if (isNotEmpty()) {
                            var genres = ""
                            forEach { genres += "$it • " }
                            mediaList.random()
                        } else {
                            null
                        }
                        media.let {
                            val index = mediaList?.indexOf(it) ?: -1
                            clicked(index, null)
                        }
                    }
                }
            }
        }
    }

    inner class MediaViewHolder(val binding: ItemMediaCompactBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            if (matchParent) itemView.updateLayoutParams { width = -1 }
            itemView.setSafeOnClickListener {
                clicked(
                    bindingAdapterPosition,
                    binding.itemCompactImage,
                    resizeBitmap(getBitmapFromImageView(binding.itemCompactImage), 100)
                )
            }
            itemView.setOnLongClickListener { longClicked(bindingAdapterPosition) }
        }
    }

    inner class MediaLargeViewHolder(val binding: ItemMediaLargeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setSafeOnClickListener {
                clicked(
                    bindingAdapterPosition,
                    binding.itemCompactImage,
                    resizeBitmap(getBitmapFromImageView(binding.itemCompactImage), 100)
                )
            }
            itemView.setOnLongClickListener { longClicked(bindingAdapterPosition) }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    inner class MediaPageViewHolder(val binding: ItemMediaPageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.itemCompactImage.setSafeOnClickListener {
                clicked(
                    bindingAdapterPosition,
                    binding.itemCompactImage,
                    resizeBitmap(getBitmapFromImageView(binding.itemCompactImage), 100)
                )
            }
            itemView.setOnTouchListener { _, _ -> true }
            binding.itemCompactImage.setOnLongClickListener { longClicked(bindingAdapterPosition) }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    inner class MediaPageSmallViewHolder(val binding: ItemMediaPageSmallBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.itemCompactImage.setSafeOnClickListener {
                clicked(
                    bindingAdapterPosition,
                    binding.itemCompactImage,
                    resizeBitmap(getBitmapFromImageView(binding.itemCompactImage), 100)
                )
            }
            binding.itemCompactTitleContainer.setSafeOnClickListener {
                clicked(
                    bindingAdapterPosition,
                    binding.itemCompactImage,
                    resizeBitmap(getBitmapFromImageView(binding.itemCompactImage), 100)
                )
            }
            itemView.setOnTouchListener { _, _ -> true }
            binding.itemCompactImage.setOnLongClickListener { longClicked(bindingAdapterPosition) }
        }
    }

    fun clicked(position: Int, itemCompactImage: ImageView?, bitmap: Bitmap? = null) {
        if ((mediaList?.size ?: 0) > position && position != -1) {
            val media = mediaList?.get(position)
            if (bitmap != null) MediaSingleton.bitmap = bitmap
            // Dismiss the "New Episode" badge as soon as the user taps into the show.
            media?.id?.let { NewEpisodeBadgeManager.markAsSeen(it, activity.applicationContext) }
            ContextCompat.startActivity(
                activity,
                Intent(activity, MediaDetailsActivity::class.java).putExtra("media", media as Serializable),
                if (itemCompactImage != null) {
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
                        itemCompactImage,
                        ViewCompat.getTransitionName(itemCompactImage)!!
                    ).toBundle()
                } else {
                    null
                }
            )
        }
    }

    fun longClicked(position: Int): Boolean {
        if (isOtherUser) return false
        if ((mediaList?.size ?: 0) > position && position != -1) {
            val media = mediaList?.get(position) ?: return false
            if (activity.supportFragmentManager.findFragmentByTag("list") == null) {
                MediaListDialogSmallFragment.newInstance(media)
                    .show(activity.supportFragmentManager, "list")
                return true
            }
        }
        return false
    }

    fun getBitmapFromImageView(imageView: ImageView): Bitmap? {
        val drawable = imageView.drawable ?: return null
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun resizeBitmap(source: Bitmap?, maxDimension: Int): Bitmap? {
        if (source == null) return null
        val width = source.width
        val height = source.height
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxDimension
            newHeight = (height * (maxDimension.toFloat() / width)).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (width * (maxDimension.toFloat() / height)).toInt()
        }
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
    }
}
