package com.sanin.tv.profile

import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.math.MathUtils
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.sanin.tv.R
import com.sanin.tv.blurImage
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.connections.anilist.api.Query
import com.sanin.tv.connections.mal.MAL
import com.sanin.tv.databinding.ActivityProfileBinding
import com.sanin.tv.loadImage
import com.sanin.tv.media.CharacterAdapter
import com.sanin.tv.media.MediaAdaptor
import com.sanin.tv.settings.AnilistSettingsActivity
import com.sanin.tv.settings.SettingsAccountActivity
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * Unified profile screen: banner + avatar header, AniList account card,
 * MAL login (no Discord), AniList Settings, Enable Comments toggle,
 * stats table, favourite anime, favourite characters, and a 3-dot menu
 * (View on AniList / Share Profile / Copy user ID).
 */
class ProfileActivity : AppCompatActivity(), AppBarLayout.OnOffsetChangedListener {

    lateinit var binding: ActivityProfileBinding

    private var mMaxScrollSize: Int = 0
    private var isCollapsed: Boolean = false
    private val percent: Float = 0.65f
    private val animDuration: Long = 300L

    private var userId: Int = -1
    private var loadedUser: Query.UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("userId", -1)

        @Suppress("DEPRECATION")
        val passedUser: Query.UserProfile? =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                intent.getSerializableExtra("user", Query.UserProfile::class.java)
            else
                intent.getSerializableExtra("user") as? Query.UserProfile

        if (userId == -1 && passedUser == null) {
            // Fall back to the logged-in user's own profile
            userId = Anilist.userid ?: run { finish(); return }
        }

        binding.profileAppBar.addOnOffsetChangedListener(this)

        // ── Close / back button ──────────────────────────────────────────
        binding.profileCloseButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // ── 3-dot menu: View on AniList | Share Profile | Copy user ID ──
        binding.profileMenuButton.setOnClickListener { anchor ->
            val popup = PopupMenu(this, anchor)
            popup.menu.add(0, 1, 0, "View on AniList")
            popup.menu.add(0, 2, 1, "Share Profile")
            popup.menu.add(0, 3, 2, "Copy user ID")
            popup.setOnMenuItemClickListener { item ->
                val name = loadedUser?.name ?: Anilist.username ?: "user"
                val uid  = loadedUser?.id   ?: userId
                val url  = "https://anilist.co/user/$name"
                when (item.itemId) {
                    1 -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    2 -> {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, url)
                        }
                        startActivity(Intent.createChooser(shareIntent, "Share Profile"))
                    }
                    3 -> {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("User ID", uid.toString()))
                        Toast.makeText(this, "Copied: $uid", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }
            popup.show()
        }

        // ── Populate account sections (no API call needed) ───────────────
        populateAnilistAccount()
        populateMalAccount()
        populateSettingsRows()

        // ── Load profile data from AniList API ───────────────────────────
        if (passedUser != null) {
            populateProfileHeader(passedUser)
            populateFavourites(passedUser)
            populateStats(passedUser)
        } else {
            binding.profileProgressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                val profile = withContext(Dispatchers.IO) {
                    Anilist.query.getUserProfile(userId)?.data?.user
                }
                withContext(Dispatchers.Main) {
                    binding.profileProgressBar.visibility = View.GONE
                    if (profile != null) {
                        loadedUser = profile
                        populateProfileHeader(profile)
                        populateFavourites(profile)
                        populateStats(profile)
                    }
                }
            }
        }
    }

    // ─── Profile header (banner, avatar, username) ─────────────────────────
    private fun populateProfileHeader(user: Query.UserProfile) {
        loadedUser = user
        binding.profileUserName.text = user.name ?: ""
        binding.profileUserAvatar.loadImage(user.avatar?.large)
        val bgView = binding.profileBannerImage
        if (bgView is com.flaviofaria.kenburnsview.KenBurnsView) {
            bgView.loadImage(user.bannerImage ?: user.avatar?.large)
        } else {
            blurImage(bgView, user.bannerImage ?: user.avatar?.large)
        }
    }

    // ─── Stats table ───────────────────────────────────────────────────────
    private fun populateStats(user: Query.UserProfile) {
        val animeStats = user.statistics?.anime
        val mangaStats = user.statistics?.manga
        binding.profileEpisodesWatched?.text = animeStats?.episodesWatched?.toString() ?: "0"
        val days = animeStats?.minutesWatched?.div(1440) ?: 0
        binding.profileDaysWatched?.text = days.toString()
        binding.profileAnimeMeanScore?.text = animeStats?.meanScore?.toString() ?: "0"
        binding.profileChaptersRead?.text = mangaStats?.chaptersRead?.toString() ?: "0"
        binding.profileVolumeRead?.text = mangaStats?.volumesRead?.toString() ?: "0"
        binding.profileMangaMeanScore?.text = mangaStats?.meanScore?.toString() ?: "0"
        binding.profileStatsSection?.isVisible = true
    }

    // ─── Favourite anime + characters ──────────────────────────────────────
    private fun populateFavourites(user: Query.UserProfile) {
        val favAnime = user.favourites?.anime?.nodes
        val favChars = user.favourites?.characters?.nodes

        if (!favAnime.isNullOrEmpty()) {
            binding.profileFavAnimeSection?.isVisible = true
            binding.profileFavAnimeRecycler?.apply {
                layoutManager = LinearLayoutManager(
                    this@ProfileActivity, LinearLayoutManager.HORIZONTAL, false
                )
                adapter = MediaAdaptor(MediaAdaptor.STYLE_SMALL, favAnime, this@ProfileActivity)
            }
        }

        if (!favChars.isNullOrEmpty()) {
            binding.profileFavCharactersSection?.isVisible = true
            binding.profileFavCharactersRecycler?.apply {
                layoutManager = LinearLayoutManager(
                    this@ProfileActivity, LinearLayoutManager.HORIZONTAL, false
                )
                adapter = CharacterAdapter(favChars, this@ProfileActivity)
            }
        }
    }

    // ─── AniList account card ──────────────────────────────────────────────
    private fun populateAnilistAccount() {
        if (Anilist.token != null) {
            val expiryDays = Anilist.getTokenExpiryDays()
            binding.profileAnilistBanner?.loadImage(Anilist.bg)
            binding.profileAnilistCardAvatar?.loadImage(Anilist.avatar)
            binding.profileAnilistUsername?.text = Anilist.username ?: ""
            binding.profileAnilistExpiry?.text =
                if (expiryDays != null) "Reconnect in $expiryDays days" else ""
            binding.profileAnilistLogout?.text = "Logout"
            binding.profileAnilistLogout?.setOnClickListener {
                Anilist.removeSavedToken()
                recreate()
            }
        } else {
            binding.profileAnilistCardAvatar?.isVisible = false
            binding.profileAnilistBanner?.isVisible = false
            binding.profileAnilistExpiry?.isVisible = false
            binding.profileAnilistUsername?.text = "Not connected to AniList"
            binding.profileAnilistLogout?.text = "Login to AniList"
            binding.profileAnilistLogout?.setOnClickListener {
                startActivity(Intent(this, SettingsAccountActivity::class.java))
            }
        }
    }

    // ─── MAL account section (no Discord) ─────────────────────────────────
    private fun populateMalAccount() {
        if (MAL.token != null) {
            binding.profileMalAvatar?.apply {
                isVisible = true
                loadImage(MAL.avatar)
            }
            binding.profileMalUsername?.apply {
                isVisible = true
                text = MAL.username ?: "MyAnimeList"
            }
            binding.profileMalLogin?.text = "Logout"
            binding.profileMalLogin?.setOnClickListener {
                MAL.token = null
                PrefManager.removeVal(PrefName.MALToken)
                recreate()
            }
        } else {
            binding.profileMalAvatar?.isVisible = false
            binding.profileMalUsername?.apply {
                isVisible = true
                text = "MyAnimeList"
            }
            binding.profileMalLogin?.text = "Login"
            binding.profileMalLogin?.setOnClickListener {
                startActivity(Intent(this, SettingsAccountActivity::class.java))
            }
        }
    }

    // ─── AniList Settings row + Enable Comments toggle ────────────────────
    private fun populateSettingsRows() {
        binding.profileAnilistSettingsRow?.setOnClickListener {
            startActivity(Intent(this, AnilistSettingsActivity::class.java))
        }

        val commentsEnabled = PrefManager.getVal(PrefName.CommentsEnabled, 1) == 1
        binding.profileCommentsSwitch?.isChecked = commentsEnabled
        binding.profileCommentsSwitch?.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.CommentsEnabled, if (isChecked) 1 else 0)
        }
        binding.profileCommentsRow?.setOnClickListener {
            binding.profileCommentsSwitch?.toggle()
        }
    }

    // ─── Collapsing app bar offset animation ──────────────────────────────
    override fun onOffsetChanged(appBarLayout: AppBarLayout, offset: Int) {
        if (mMaxScrollSize == 0) mMaxScrollSize = appBarLayout.totalScrollRange
        val percentage = abs(offset).toFloat() / mMaxScrollSize.toFloat()
        val avatarScale = 1f - MathUtils.clamp(percentage * 2f - 0.5f, 0f, 1f)
        binding.profileUserAvatarContainer.scaleX = avatarScale
        binding.profileUserAvatarContainer.scaleY = avatarScale
        binding.profileUserAvatarContainer.alpha  = avatarScale

        if (percentage >= percent && !isCollapsed) {
            isCollapsed = true
            ObjectAnimator.ofFloat(binding.profileUserName, "alpha", 1f, 0f)
                .setDuration(animDuration).start()
            (binding.profileBannerImage as? com.flaviofaria.kenburnsview.KenBurnsView)?.pause()
        } else if (percentage < percent && isCollapsed) {
            isCollapsed = false
            ObjectAnimator.ofFloat(binding.profileUserName, "alpha", 0f, 1f)
                .setDuration(animDuration).start()
            (binding.profileBannerImage as? com.flaviofaria.kenburnsview.KenBurnsView)?.resume()
        }
    }

    override fun onDestroy() {
        binding.profileAppBar.removeOnOffsetChangedListener(this)
        super.onDestroy()
    }
}
