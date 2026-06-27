package com.sanin.tv.home

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LayoutAnimationController
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sanin.tv.MainActivity
import com.sanin.tv.R
import com.sanin.tv.Refresh
import com.sanin.tv.blurImage
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.connections.anilist.AnilistHomeViewModel
import com.sanin.tv.connections.anilist.getUserId
import com.sanin.tv.connections.mal.MAL
import com.sanin.tv.connections.tmdb.TmdbApi
import com.sanin.tv.currContext
import com.sanin.tv.databinding.FragmentHomeBinding
import com.sanin.tv.home.status.UserStatusAdapter
import com.sanin.tv.loadImage
import com.sanin.tv.media.Media
import com.sanin.tv.media.MediaAdaptor
import com.sanin.tv.media.MediaListViewActivity
import com.sanin.tv.media.user.ListActivity
import com.sanin.tv.navBarHeight
import com.sanin.tv.openLinkInBrowser
import com.sanin.tv.profile.ProfileActivity
import com.sanin.tv.setSafeOnClickListener
import com.sanin.tv.setSlideIn
import com.sanin.tv.setSlideUp
import com.sanin.tv.settings.SettingsDialogFragment
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefManager.asLiveBool
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.snackString
import com.sanin.tv.statusBarHeight
import com.sanin.tv.tryWithSuspend
import com.sanin.tv.util.DpadHelper.enableDpadNavigation
import com.sanin.tv.util.Logger
import com.sanin.tv.util.customAlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    val model: AnilistHomeViewModel by activityViewModels()

    private var heroCarouselAdapter: HeroCarouselAdapter? = null

    // ── Navigating banner state ───────────────────────────────────────────────
    // Which of the two ImageView slots is currently "foreground"
    private var navBannerSlotA = true
    private var navBannerCurrentMediaId: Int = -1

    // Track live data per RecyclerView so focus → media lookup works without
    // touching the individual adapters.
    private val rvDataMap = mutableMapOf<RecyclerView, List<Media>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        heroCarouselAdapter?.stopAutoAdvance()
        heroCarouselAdapter = null
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val scope = lifecycleScope
        Logger.log("HomeFragment")

        fun load() {
            Logger.log("Loading HomeFragment")
            if (activity != null && _binding != null) lifecycleScope.launch(Dispatchers.Main) {
                val rescueMode: Boolean = PrefManager.getVal(PrefName.RescueMode)
                if (rescueMode && MAL.token != null) {
                    binding.homeUserName.text = MAL.username ?: Anilist.username
                    binding.homeUserAvatar.loadImage(MAL.avatar ?: Anilist.avatar)
                } else {
                    binding.homeUserName.text = Anilist.username
                    binding.homeUserAvatar.loadImage(Anilist.avatar)
                }
                if (!rescueMode) {
                    binding.homeUserEpisodesWatched.text = Anilist.episodesWatched.toString()
                    binding.homeUserChaptersRead.text = Anilist.chapterRead.toString()
                    binding.homeNotificationCount.isVisible =
                        Anilist.unreadNotificationCount > 0
                        && PrefManager.getVal<Boolean>(PrefName.ShowNotificationRedDot) == true
                    binding.homeNotificationCount.text = Anilist.unreadNotificationCount.toString()
                } else {
                    binding.homeUserEpisodesWatched.text = MAL.episodesWatched?.toString() ?: "—"
                    binding.homeUserChaptersRead.text = MAL.chaptersRead?.toString() ?: "—"
                    binding.homeNotificationCount.isVisible = false
                }
                val bannerAnimations: Boolean = PrefManager.getVal(PrefName.BannerAnimations)
                val bannerMode: Int = PrefManager.getVal(PrefName.HomeBannerMode)
                val bannerUrl = if (rescueMode) (Anilist.bg ?: MAL.avatar) else Anilist.bg

                // Show/hide banner areas based on selected mode
                applyBannerMode(bannerMode, bannerAnimations, bannerUrl)
            }
        }

        // ── Search / avatar ───────────────────────────────────────────────────
        binding.searchImageContainer.setSafeOnClickListener {
            SearchBottomSheet.newInstance().show(
                (it.context as androidx.appcompat.app.AppCompatActivity).supportFragmentManager,
                "search"
            )
        }
        binding.homeUserAvatarContainer.setOnLongClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            if (!PrefManager.getVal<Boolean>(PrefName.RescueMode)) {
                ContextCompat.startActivity(
                    requireContext(), Intent(requireContext(), ProfileActivity::class.java)
                        .putExtra("userId", Anilist.userid), null
                )
            } else {
                val malUsername = MAL.username
                if (!malUsername.isNullOrBlank()) {
                    try {
                        CustomTabsIntent.Builder().build().launchUrl(
                            requireContext(), Uri.parse("https://myanimelist.net/profile/$malUsername")
                        )
                    } catch (e: Exception) {
                        openLinkInBrowser("https://myanimelist.net/profile/$malUsername")
                    }
                } else {
                    snackString(getString(R.string.rescue_mode_active))
                }
            }
            false
        }

        // ── Layout margins / insets ───────────────────────────────────────────
        binding.homeContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = navBarHeight
        }
        binding.homeUserBg.updateLayoutParams { height += statusBarHeight }
        binding.homeUserBgNoKen.updateLayoutParams { height += statusBarHeight }
        binding.homeTopContainer.updatePadding(top = statusBarHeight)

        var reached = false
        val duration = ((PrefManager.getVal(PrefName.AnimationSpeed) as Float) * 200).toLong()

        var height = statusBarHeight
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val displayCutout = activity?.window?.decorView?.rootWindowInsets?.displayCutout
            if (displayCutout != null && displayCutout.boundingRects.size > 0) {
                height = max(
                    statusBarHeight,
                    min(displayCutout.boundingRects[0].width(), displayCutout.boundingRects[0].height())
                )
            }
        }
        binding.homeRefresh.setSlingshotDistance(height + 128)
        binding.homeRefresh.setProgressViewEndTarget(false, height + 128)
        binding.homeRefresh.setOnRefreshListener {
            Refresh.activity[1]!!.postValue(true)
        }

        // ── Hero "Continue Watching" card ─────────────────────────────────────
        val heroAdapter = ContinueWatchingHeroAdapter(
            context = requireContext(),
            scope   = viewLifecycleOwner.lifecycleScope,
            onResume = { media ->
                startActivity(
                    Intent(requireActivity(), com.sanin.tv.media.MediaDetailsActivity::class.java)
                        .putExtra("media", media)
                        .putExtra("autoResume", true)
                )
            }
        )
        binding.homeHeroContinueRecycler.apply {
            adapter = heroAdapter
            layoutManager = LinearLayoutManager(requireContext())
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        model.getAnimeContinue().observe(viewLifecycleOwner) { list ->
            val hero = list?.firstOrNull { it.userStatus == "CURRENT" }
            heroAdapter.submitMedia(hero)
            binding.homeHeroContinueContainer.isVisible = hero != null

            // Seed navigating banner with last-watched on first load
            if (hero != null && navBannerCurrentMediaId == -1) {
                updateNavigatingBanner(hero)
            }
        }

        // ── Enhanced "Continue Watching" row ──────────────────────────────────
        initRecyclerView(
            model.getAnimeContinue(),
            binding.homeContinueWatchingContainer,
            binding.homeWatchingRecyclerView,
            binding.homeWatchingProgressBar,
            binding.homeWatchingEmpty,
            binding.homeContinueWatch,
            binding.homeContinueWatchMore,
            getString(R.string.continue_watching)
        )
        val anilistContinueAdapter = AnilistContinueAdapter(requireActivity())
        binding.homeWatchingRecyclerView.adapter = anilistContinueAdapter
        binding.homeWatchingRecyclerView.enableDpadNavigation()
        model.getAnimeContinue().observe(viewLifecycleOwner) { list ->
            anilistContinueAdapter.submitList(list ?: emptyList())
            list?.let { rvDataMap[binding.homeWatchingRecyclerView] = it }
        }
        binding.homeWatchingBrowseButton.setOnClickListener { (activity as? MainActivity)?.navigateTo("anime") }

        initRecyclerView(
            model.getAnimeFav(),
            binding.homeFavAnimeContainer,
            binding.homeFavAnimeRecyclerView,
            binding.homeFavAnimeProgressBar,
            binding.homeFavAnimeEmpty,
            binding.homeFavAnime,
            binding.homeFavAnimeMore,
            getString(R.string.fav_anime)
        )
        model.getAnimeFav().observe(viewLifecycleOwner) { list ->
            list?.let { rvDataMap[binding.homeFavAnimeRecyclerView] = it }
        }

        initRecyclerView(
            model.getAnimePlanned(),
            binding.homePlannedAnimeContainer,
            binding.homePlannedAnimeRecyclerView,
            binding.homePlannedAnimeProgressBar,
            binding.homePlannedAnimeEmpty,
            binding.homePlannedAnime,
            binding.homePlannedAnimeMore,
            getString(R.string.planned_anime)
        )
        model.getAnimePlanned().observe(viewLifecycleOwner) { list ->
            list?.let { rvDataMap[binding.homePlannedAnimeRecyclerView] = it }
        }
        binding.homePlannedAnimeBrowseButton.setOnClickListener { (activity as? MainActivity)?.navigateTo("anime") }

        initRecyclerView(
            model.getMissingSequels(),
            binding.homeMissingSequelsContainer,
            binding.homeMissingSequelsRecyclerView,
            binding.homeMissingSequelsProgressBar,
            binding.homeMissingSequelsEmpty,
            binding.homeMissingSequels,
            binding.homeMissingSequelsMore,
            getString(R.string.missing_sequels)
        )
        model.getMissingSequels().observe(viewLifecycleOwner) { list ->
            list?.let { rvDataMap[binding.homeMissingSequelsRecyclerView] = it }
        }

        initRecyclerView(
            model.getRecommendation(),
            binding.homeRecommendedContainer,
            binding.homeRecommendedRecyclerView,
            binding.homeRecommendedProgressBar,
            binding.homeRecommendedEmpty,
            binding.homeRecommended,
            binding.homeRecommendedMore,
            getString(R.string.recommended)
        )
        model.getRecommendation().observe(viewLifecycleOwner) { list ->
            list?.let { rvDataMap[binding.homeRecommendedRecyclerView] = it }
        }

        // ── User status ───────────────────────────────────────────────────────
        binding.homeUserStatusContainer.visibility = View.VISIBLE
        binding.homeUserStatusProgressBar.visibility = View.VISIBLE
        binding.homeUserStatusRecyclerView.visibility = View.GONE
        model.getUserStatus().observe(viewLifecycleOwner) {
            binding.homeUserStatusRecyclerView.visibility = View.GONE
            if (it != null) {
                if (it.isNotEmpty()) {
                    PrefManager.getLiveVal(PrefName.RefreshStatus, false).apply {
                        asLiveBool()
                        observe(viewLifecycleOwner) { _ ->
                            binding.homeUserStatusRecyclerView.adapter = UserStatusAdapter(it)
                        }
                    }
                    binding.homeUserStatusRecyclerView.layoutManager = LinearLayoutManager(
                        requireContext(), LinearLayoutManager.HORIZONTAL, false
                    )
                    binding.homeUserStatusRecyclerView.enableDpadNavigation()
                    binding.homeUserStatusRecyclerView.visibility = View.VISIBLE
                    binding.homeUserStatusRecyclerView.layoutAnimation =
                        LayoutAnimationController(setSlideIn(), 0.25f)
                } else {
                    binding.homeUserStatusContainer.visibility = View.GONE
                }
                binding.homeUserStatusProgressBar.visibility = View.GONE
            }
        }

        // ── Hidden items ──────────────────────────────────────────────────────
        binding.homeHiddenItemsContainer.visibility = View.GONE
        model.getHidden().observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.isNotEmpty()) {
                    binding.homeHiddenItemsRecyclerView.adapter =
                        MediaAdaptor(0, it, requireActivity())
                    binding.homeHiddenItemsRecyclerView.layoutManager = LinearLayoutManager(
                        requireContext(), LinearLayoutManager.HORIZONTAL, false
                    )
                    binding.homeHiddenItemsRecyclerView.enableDpadNavigation()
                    binding.homeContinueWatch.setOnLongClickListener {
                        binding.homeHiddenItemsContainer.visibility = View.VISIBLE
                        binding.homeHiddenItemsRecyclerView.layoutAnimation =
                            LayoutAnimationController(setSlideIn(), 0.25f)
                        true
                    }
                    binding.homeHiddenItemsMore.setSafeOnClickListener { _ ->
                        MediaListViewActivity.passedMedia = it
                        ContextCompat.startActivity(
                            requireActivity(),
                            Intent(requireActivity(), MediaListViewActivity::class.java)
                                .putExtra("title", getString(R.string.hidden)), null
                        )
                    }
                    binding.homeHiddenItemsTitle.setOnLongClickListener {
                        binding.homeHiddenItemsContainer.visibility = View.GONE
                        true
                    }
                } else {
                    binding.homeContinueWatch.setOnLongClickListener {
                        snackString(getString(R.string.no_hidden_items)); true
                    }
                }
            } else {
                binding.homeContinueWatch.setOnLongClickListener {
                    snackString(getString(R.string.no_hidden_items)); true
                }
            }
        }

        // ── Hero Carousel ─────────────────────────────────────────────────────
        model.popularAllTime.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) initHeroCarousel(list)
            else rvDataMap // keep existing map
        }

        // ── Navigating Banner: global D-pad focus watcher ─────────────────────
        // Wired directly here — no manual plumbing required in adapters.
        view.viewTreeObserver.addOnGlobalFocusChangeListener { _, newFocus ->
            if (_binding == null || newFocus == null) return@addOnGlobalFocusChangeListener
            val bannerMode: Int = PrefManager.getVal(PrefName.HomeBannerMode)
            if (bannerMode != 2) return@addOnGlobalFocusChangeListener  // NAVIGATING = 2

            // Walk up the view tree to find the ancestor RecyclerView and the
            // direct child of it (the card item) that received focus.
            var currentView: View = newFocus
            var parentRv: RecyclerView? = null
            var itemView: View = newFocus

            while (currentView.parent != null) {
                val parent = currentView.parent
                if (parent is RecyclerView) {
                    parentRv = parent
                    itemView = currentView
                    break
                }
                if (parent is View) currentView = parent else break
            }

            if (parentRv != null) {
                val pos = parentRv.getChildAdapterPosition(itemView)
                val media = rvDataMap[parentRv]?.getOrNull(pos)
                if (media != null && media.id != navBannerCurrentMediaId) {
                    updateNavigatingBanner(media)
                }
            }
        }

        // ── Refresh / load data ───────────────────────────────────────────────
        var running = false
        val live = Refresh.activity.getOrPut(1) { MutableLiveData(false) }
        live.observe(viewLifecycleOwner) {
            if (it && !running) {
                running = true
                scope.launch {
                    withContext(Dispatchers.IO) {
                        if (Anilist.token != null) {
                            if (MAL.token != null && MAL.episodesWatched == null) {
                                tryWithSuspend { MAL.query.getUserData() }
                            }
                            withContext(Dispatchers.Main) { load() }
                        } else {
                            Anilist.userid =
                                PrefManager.getNullableVal<String>(PrefName.AnilistUserId, null)
                                    ?.toIntOrNull()
                            if (Anilist.userid == null) {
                                withContext(Dispatchers.Main) {
                                    getUserId(requireContext()) { load() }
                                }
                            } else {
                                getUserId(requireContext()) { load() }
                            }
                        }
                        model.loaded = true
                    }

                    if (Anilist.anilistDisabledSignal && !PrefManager.getVal<Boolean>(PrefName.RescueMode)) {
                        withContext(Dispatchers.Main) {
                            if (isAdded && _binding != null) {
                                requireContext().customAlertDialog().apply {
                                    setTitle(R.string.rescue_mode_prompt_title)
                                    setMessage(R.string.rescue_mode_prompt_message)
                                    setPosButton(R.string.rescue_mode_enable) {
                                        PrefManager.setVal(PrefName.RescueMode, true)
                                        Anilist.anilistDisabledSignal = false
                                        val intent = Intent(requireContext(), MainActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                        startActivity(intent)
                                        activity?.overridePendingTransition(0, 0)
                                        activity?.finish()
                                        activity?.overridePendingTransition(0, 0)
                                    }
                                    setNegButton(R.string.no)
                                    show()
                                }
                            }
                        }
                    }

                    var empty = true
                    val homeLayoutShow: List<Boolean> = PrefManager.getVal(PrefName.HomeLayout)
                    var homeLayoutOrder: List<Int> = PrefManager.getVal(PrefName.HomeLayoutOrder)
                    if (homeLayoutOrder.isEmpty()) homeLayoutOrder = (0..7).toList()

                    val containers = listOf(
                        binding.homeContinueWatchingContainer,
                        binding.homeFavAnimeContainer,
                        binding.homePlannedAnimeContainer,
                        binding.homeMissingSequelsContainer,
                        binding.homeRecommendedContainer,
                        binding.homeUserStatusContainer,
                        binding.homeHiddenItemsContainer,
                        binding.homeHeroCarouselContainer
                    )

                    withContext(Dispatchers.Main) {
                        containers.indices.forEach { i ->
                            if (homeLayoutShow.getOrElse(i) { true }) {
                                empty = false
                            } else {
                                containers[i].visibility = View.GONE
                            }
                        }
                        var insertIndex =
                            binding.homeContainer.indexOfChild(binding.homeHiddenItemsContainer) + 1
                        homeLayoutOrder.forEach { i ->
                            val container = containers.getOrNull(i)
                            if (container != null) {
                                binding.homeContainer.removeView(container)
                                binding.homeContainer.addView(container, insertIndex)
                                insertIndex++
                            }
                        }
                    }

                    val rescueMode: Boolean = PrefManager.getVal(PrefName.RescueMode)
                    val initHomePage  = async(Dispatchers.IO) { model.initHomePage() }
                    val setListImages = async(Dispatchers.IO) { model.setListImages() }
                    val loadPopular   = async(Dispatchers.IO) { model.loadPopularAllTime() }
                    if (!rescueMode) {
                        val initUserStatus = async(Dispatchers.IO) { model.initUserStatus() }
                        awaitAll(initHomePage, initUserStatus, setListImages, loadPopular)
                    } else {
                        awaitAll(initHomePage, setListImages, loadPopular)
                    }

                    withContext(Dispatchers.Main) {
                        model.empty.postValue(empty)
                        binding.homeHiddenItemsContainer.visibility = View.GONE
                    }

                    live.postValue(false)
                    _binding?.homeRefresh?.isRefreshing = false
                    running = false
                }
            }
        }
    }

    // ── Banner mode switcher ──────────────────────────────────────────────────

    /**
     * Shows/hides the correct banner section based on [mode]:
     * - 0 CAROUSEL   → hero carousel visible, profile + navigating hidden
     * - 1 PROFILE    → profile banner (homeUserBg / homeUserBgNoKen) visible
     * - 2 NAVIGATING → navigating banner container visible, seeded with last-watched
     * - 3 OFF        → all banner containers hidden
     */
    private fun applyBannerMode(mode: Int, bannerAnimations: Boolean, bannerUrl: String?) {
        val b = _binding ?: return

        // Reset everything first
        b.homeUserBg.visibility         = View.GONE
        b.homeUserBgNoKen.visibility     = View.GONE
        b.homeHeroCarouselContainer.visibility   = View.GONE
        b.homeNavigatingBannerContainer.visibility = View.GONE

        when (mode) {
            0 -> { // CAROUSEL — visibility toggled by initHeroCarousel / popular data arriving
                b.homeHeroCarouselContainer.visibility = View.VISIBLE
            }
            1 -> { // PROFILE
                if (bannerAnimations) {
                    b.homeUserBg.visibility = View.VISIBLE
                    blurImage(b.homeUserBg, bannerUrl)
                } else {
                    b.homeUserBgNoKen.visibility = View.VISIBLE
                    blurImage(b.homeUserBgNoKen, bannerUrl)
                }
            }
            2 -> { // NAVIGATING
                b.homeNavigatingBannerContainer.visibility = View.VISIBLE
            }
            3 -> { // OFF — all hidden, already done above
            }
        }
    }

    // ── Navigating banner ─────────────────────────────────────────────────────

    /**
     * Crossfades the navigating banner to [media]'s banner image and updates
     * the metadata overlay (title/logo, status, rating, genres, synopsis, button).
     *
     * The two ImageView slots (navBannerBgA / navBannerBgB) alternate so we
     * never see a blank flash during the transition.
     */
    private fun updateNavigatingBanner(media: Media) {
        val b = _binding ?: return
        navBannerCurrentMediaId = media.id

        val bannerUrl = media.banner ?: media.cover ?: return

        // Determine which slot is currently in front and which is the back buffer
        val front = if (navBannerSlotA) b.navBannerBgA else b.navBannerBgB
        val back  = if (navBannerSlotA) b.navBannerBgB else b.navBannerBgA

        // Load new image into the back slot, then crossfade
        back.loadImage(bannerUrl)
        back.alpha = 0f
        back.animate()
            .alpha(1f)
            .setDuration(400)
            .withEndAction {
                front.alpha = 0f
                navBannerSlotA = !navBannerSlotA
            }
            .start()

        // ── Text / metadata overlay ───────────────────────────────────────────
        b.navBannerTitle.text = media.userPreferredName
        b.navBannerLogo.visibility  = View.GONE
        b.navBannerTitle.visibility = View.VISIBLE

        b.navBannerStatus.text  = media.status?.replace("_", " ") ?: ""
        b.navBannerStatus.isVisible = media.status != null
        b.navBannerRating.text  = media.meanScore?.let { "★ ${it / 10.0}" } ?: ""
        b.navBannerRating.isVisible = media.meanScore != null
        b.navBannerGenres.text  = media.genres.take(2).joinToString(" • ")
        b.navBannerGenres.isVisible = media.genres.isNotEmpty()
        b.navBannerSynopsis.text = media.description
            ?.replace(Regex("<.*?>"), "")
            ?.take(200) ?: ""

        // Watch / Continue button label
        val isWatching = media.userStatus == "CURRENT"
        b.navBannerWatchBtn.text = if (isWatching)
            getString(R.string.continue_watching_short)
        else
            getString(R.string.watch_now)
        b.navBannerWatchBtn.setOnClickListener { openMediaDetail(media) }

        // ── TMDB logo art (async, replaces text title when available) ─────────
        lifecycleScope.launch(Dispatchers.IO) {
            val logoUrl = TmdbApi.getLogoUrl(media.id, media.anime != null)
            withContext(Dispatchers.Main) {
                if (_binding == null || navBannerCurrentMediaId != media.id) return@withContext
                if (logoUrl != null) {
                    binding.navBannerLogo.loadImage(logoUrl)
                    binding.navBannerLogo.visibility  = View.VISIBLE
                    binding.navBannerTitle.visibility = View.GONE
                } else {
                    binding.navBannerLogo.visibility  = View.GONE
                    binding.navBannerTitle.visibility = View.VISIBLE
                }
            }
        }
    }

    // ── Hero carousel ─────────────────────────────────────────────────────────

    private fun initHeroCarousel(popularAllTime: List<Media>) {
        if (popularAllTime.isEmpty()) return
        heroCarouselAdapter?.stopAutoAdvance()
        heroCarouselAdapter = HeroCarouselAdapter(
            activity       = requireActivity(),
            mediaList      = popularAllTime.take(15),
            onWatchClicked = { media -> openMediaDetail(media) }
        )
        binding.homeHeroViewPager.apply {
            adapter            = heroCarouselAdapter
            offscreenPageLimit = 2
            isFocusable        = true
            registerOnPageChangeCallback(heroCarouselAdapter!!.pageCallback)
            // D-pad navigation: left/right swipe carousel, center opens detail
            getChildAt(0)?.setOnKeyListener { _, keyCode, event ->
                if (event.action != android.view.KeyEvent.ACTION_DOWN) return@setOnKeyListener false
                val pager = this
                when (keyCode) {
                    android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        val next = (pager.currentItem + 1).coerceAtMost(
                            (heroCarouselAdapter?.itemCount ?: 1) - 1
                        )
                        pager.setCurrentItem(next, true)
                        true
                    }
                    android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
                        val prev = (pager.currentItem - 1).coerceAtLeast(0)
                        pager.setCurrentItem(prev, true)
                        true
                    }
                    android.view.KeyEvent.KEYCODE_DPAD_CENTER,
                    android.view.KeyEvent.KEYCODE_ENTER -> {
                        heroCarouselAdapter?.mediaList?.getOrNull(pager.currentItem)?.let {
                            openMediaDetail(it)
                        }
                        true
                    }
                    else -> false
                }
            }
        }
        heroCarouselAdapter!!.startAutoAdvance(binding.homeHeroViewPager)
        binding.homeHeroDotIndicator?.setViewPager2(binding.homeHeroViewPager)
    }

    private fun openMediaDetail(media: Media) {
        ContextCompat.startActivity(
            requireContext(),
            Intent(requireContext(), com.sanin.tv.media.MediaDetailsActivity::class.java)
                .putExtra("media", media),
            null
        )
    }

    // ── initRecyclerView helper ───────────────────────────────────────────────

    private fun initRecyclerView(
        mode: LiveData<ArrayList<Media>>,
        container: View,
        recyclerView: RecyclerView,
        progress: View,
        empty: View,
        title: View,
        more: View,
        string: String
    ) {
        container.visibility = View.VISIBLE
        progress.visibility  = View.VISIBLE
        recyclerView.visibility = View.GONE

        mode.observe(viewLifecycleOwner) {
            recyclerView.visibility = View.GONE
            if (it != null) {
                if (it.isNotEmpty()) {
                    recyclerView.adapter = MediaAdaptor(0, it, requireActivity())
                    recyclerView.layoutManager = LinearLayoutManager(
                        requireContext(), LinearLayoutManager.HORIZONTAL, false
                    )
                    recyclerView.enableDpadNavigation()
                    more.setOnClickListener { v ->
                        MediaListViewActivity.passedMedia = it
                        ContextCompat.startActivity(
                            v.context,
                            Intent(v.context, MediaListViewActivity::class.java)
                                .putExtra("title", string),
                            null
                        )
                    }
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.layoutAnimation =
                        LayoutAnimationController(setSlideIn(), 0.25f)
                } else {
                    empty.visibility = View.VISIBLE
                }
                more.visibility = View.VISIBLE
                title.visibility = View.VISIBLE
                more.startAnimation(setSlideUp())
                title.startAnimation(setSlideUp())
                progress.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        if (!model.loaded) Refresh.activity[1]!!.postValue(true)
        if (_binding != null) {
            val rescueMode: Boolean = PrefManager.getVal(PrefName.RescueMode)
            binding.homeNotificationCount.isVisible = !rescueMode
                && Anilist.unreadNotificationCount > 0
                && PrefManager.getVal<Boolean>(PrefName.ShowNotificationRedDot) == true
            binding.homeNotificationCount.text = Anilist.unreadNotificationCount.toString()
        }
        super.onResume()
    }
}
