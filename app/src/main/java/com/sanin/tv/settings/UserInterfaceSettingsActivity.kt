package com.sanin.tv.settings

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.sanin.tv.R
import com.sanin.tv.databinding.ActivitySettingsBinding
import com.sanin.tv.databinding.ItemHomeLayoutBinding
import com.sanin.tv.navBarHeight
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.statusBarHeight
import com.sanin.tv.util.customAlertDialog

class UserInterfaceSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            settingsContainer.updatePadding(top = statusBarHeight, bottom = navBarHeight)

            val settingsList = listOf(
                Settings(
                    type = 2,
                    name = getString(R.string.banner_animations),
                    desc = getString(R.string.banner_animations_desc),
                    icon = R.drawable.ic_round_auto_awesome_24,
                    pref = PrefName.BannerAnimations
                ),
                Settings(
                    type = 2,
                    name = getString(R.string.layout_animations),
                    desc = getString(R.string.layout_animations_desc),
                    icon = R.drawable.ic_round_animation_24,
                    pref = PrefName.LayoutAnimations
                ),
                Settings(
                    type = 2,
                    name = getString(R.string.emoji),
                    desc = getString(R.string.emoji_desc),
                    icon = R.drawable.ic_round_emoji_emotions_24,
                    pref = PrefName.Emoji
                ),
                Settings(
                    type = 2,
                    name = getString(R.string.blur_banners),
                    desc = getString(R.string.blur_banners_desc),
                    icon = R.drawable.blur_on,
                    pref = PrefName.BlurBanners
                ),
                Settings(
                    type = 2,
                    name = getString(R.string.small_view),
                    desc = getString(R.string.small_view_desc),
                    icon = R.drawable.ic_round_photo_size_select_actual_24,
                    pref = PrefName.SmallView
                ),
                Settings(
                    type = 2,
                    name = getString(R.string.immersive_mode),
                    desc = getString(R.string.immersive_mode_desc),
                    icon = R.drawable.ic_round_fullscreen_24,
                    pref = PrefName.ImmersiveMode
                ),
                Settings(
                    type = 1,
                    name = getString(R.string.animation_speed),
                    desc = getString(R.string.animation_speed_desc),
                    icon = R.drawable.ic_round_auto_awesome_24,
                    onClick = { showAnimationSpeedDialog() }
                ),
                Settings(
                    type = 1,
                    name = getString(R.string.home_layout),
                    desc = getString(R.string.home_layout_desc),
                    icon = R.drawable.ic_round_auto_awesome_24,
                    onClick = { showHomeLayoutDialog() }
                ),
                // ── Home Banner Mode ──────────────────────────────────────────
                Settings(
                    type = 1,
                    name = getString(R.string.home_banner_mode),
                    desc = getString(R.string.home_banner_mode_desc),
                    icon = R.drawable.ic_round_featured_play_list_24,
                    onClick = { showHomeBannerModeDialog() }
                ),
                // ── Card Image (Cover vs Banner) ──────────────────────────────
                Settings(
                    type = 1,
                    name = getString(R.string.card_image_type),
                    desc = listOf(
                        getString(R.string.card_image_cover),
                        getString(R.string.card_image_banner)
                    ).getOrElse(PrefManager.getVal<Int>(PrefName.CardImageType)) { getString(R.string.card_image_cover) },
                    icon = R.drawable.ic_round_photo_size_select_actual_24,
                    onClick = { b ->
                        val opts = arrayOf(
                            getString(R.string.card_image_cover),
                            getString(R.string.card_image_banner)
                        )
                        val cur = PrefManager.getVal<Int>(PrefName.CardImageType)
                        customAlertDialog().apply {
                            setTitle(R.string.card_image_type)
                            singleChoiceItems(opts, cur) { idx ->
                                PrefManager.setVal(PrefName.CardImageType, idx)
                                b.settingsDesc.text = opts.getOrElse(idx) { opts[0] }
                            }
                            setNegButton(R.string.cancel)
                            show()
                        }
                    }
                ),
                // ── Card Orientation ──────────────────────────────────────────
                Settings(
                    type = 1,
                    name = getString(R.string.card_orientation),
                    desc = listOf(
                        getString(R.string.card_landscape),
                        getString(R.string.card_portrait)
                    ).getOrElse(PrefManager.getVal<Int>(PrefName.CardOrientation)) { getString(R.string.card_portrait) },
                    icon = R.drawable.ic_round_fullscreen_24,
                    onClick = { b ->
                        val opts = arrayOf(
                            getString(R.string.card_landscape),
                            getString(R.string.card_portrait)
                        )
                        val cur = PrefManager.getVal<Int>(PrefName.CardOrientation)
                        customAlertDialog().apply {
                            setTitle(R.string.card_orientation)
                            singleChoiceItems(opts, cur) { idx ->
                                PrefManager.setVal(PrefName.CardOrientation, idx)
                                b.settingsDesc.text = opts.getOrElse(idx) { opts[1] }
                            }
                            setNegButton(R.string.cancel)
                            show()
                        }
                    }
                ),
                // ── Card Roundness ────────────────────────────────────────────
                Settings(
                    type = 1,
                    name = getString(R.string.card_roundness),
                    desc = run {
                        val dpVals = listOf(0, 8, 16, 24, 50)
                        val labels = listOf(
                            getString(R.string.card_round_none),
                            getString(R.string.card_round_small),
                            getString(R.string.card_round_medium),
                            getString(R.string.card_round_large),
                            getString(R.string.card_round_pill)
                        )
                        val cur = PrefManager.getVal<Int>(PrefName.CardRoundness)
                        labels.getOrElse(dpVals.indexOf(cur)) { getString(R.string.card_round_medium) }
                    },
                    icon = R.drawable.ic_round_art_track_24,
                    onClick = { b ->
                        val dpVals = listOf(0, 8, 16, 24, 50)
                        val opts = arrayOf(
                            getString(R.string.card_round_none),
                            getString(R.string.card_round_small),
                            getString(R.string.card_round_medium),
                            getString(R.string.card_round_large),
                            getString(R.string.card_round_pill)
                        )
                        val cur = PrefManager.getVal<Int>(PrefName.CardRoundness)
                        val selIdx = dpVals.indexOf(cur).coerceAtLeast(0)
                        customAlertDialog().apply {
                            setTitle(R.string.card_roundness)
                            singleChoiceItems(opts, selIdx) { idx ->
                                PrefManager.setVal(PrefName.CardRoundness, dpVals[idx])
                                b.settingsDesc.text = opts[idx]
                            }
                            setNegButton(R.string.cancel)
                            show()
                        }
                    }
                ),
                // ── Card Styles ───────────────────────────────────────────────
                Settings(
                    type = 1,
                    name = getString(R.string.card_style),
                    desc = listOf(
                        getString(R.string.card_style_rounded),
                        getString(R.string.card_style_minimal),
                        getString(R.string.card_style_classic),
                        getString(R.string.card_style_cover_only),
                        getString(R.string.card_style_liquid_glass),
                        getString(R.string.card_style_neon),
                        getString(R.string.card_style_compact)
                    ).getOrElse(PrefManager.getVal<Int>(PrefName.CardStyle)) { getString(R.string.card_style_rounded) },
                    icon = R.drawable.ic_round_art_track_24,
                    onClick = { b ->
                        val opts = arrayOf(
                            getString(R.string.card_style_rounded),
                            getString(R.string.card_style_minimal),
                            getString(R.string.card_style_classic),
                            getString(R.string.card_style_cover_only),
                            getString(R.string.card_style_liquid_glass),
                            getString(R.string.card_style_neon),
                            getString(R.string.card_style_compact)
                        )
                        val cur = PrefManager.getVal<Int>(PrefName.CardStyle)
                        customAlertDialog().apply {
                            setTitle(R.string.card_style)
                            singleChoiceItems(opts, cur) { idx ->
                                PrefManager.setVal(PrefName.CardStyle, idx)
                                b.settingsDesc.text = opts.getOrElse(idx) { opts[0] }
                            }
                            setNegButton(R.string.cancel)
                            show()
                        }
                    }
                ),
                Settings(
                    type = 1,
                    name = getString(R.string.spotlight_card_style),
                    desc = listOf(
                        getString(R.string.spotlight_style_default),
                        getString(R.string.spotlight_style_minimal),
                        getString(R.string.spotlight_style_large)
                    ).getOrElse(PrefManager.getVal<Int>(PrefName.SpotlightCardStyle)) { getString(R.string.spotlight_style_default) },
                    icon = R.drawable.ic_round_featured_play_list_24,
                    onClick = { b ->
                        val opts = arrayOf(
                            getString(R.string.spotlight_style_default),
                            getString(R.string.spotlight_style_minimal),
                            getString(R.string.spotlight_style_large)
                        )
                        val cur = PrefManager.getVal<Int>(PrefName.SpotlightCardStyle)
                        customAlertDialog().apply {
                            setTitle(R.string.spotlight_card_style)
                            singleChoiceItems(opts, cur) { idx ->
                                PrefManager.setVal(PrefName.SpotlightCardStyle, idx)
                                b.settingsDesc.text = opts.getOrElse(idx) { opts[0] }
                            }
                            setNegButton(R.string.cancel)
                            show()
                        }
                    }
                ),
                Settings(
                    type = 1,
                    name = getString(R.string.episode_view_mode),
                    desc = listOf(
                        getString(R.string.episode_view_list),
                        getString(R.string.episode_view_grid),
                        getString(R.string.episode_view_compact)
                    ).getOrElse(PrefManager.getVal<Int>(PrefName.EpisodeViewMode)) { getString(R.string.episode_view_list) },
                    icon = R.drawable.ic_round_auto_awesome_24,
                    onClick = { b ->
                        val opts = arrayOf(
                            getString(R.string.episode_view_list),
                            getString(R.string.episode_view_grid),
                            getString(R.string.episode_view_compact)
                        )
                        val cur = PrefManager.getVal<Int>(PrefName.EpisodeViewMode)
                        customAlertDialog().apply {
                            setTitle(R.string.episode_view_mode)
                            singleChoiceItems(opts, cur) { idx ->
                                PrefManager.setVal(PrefName.EpisodeViewMode, idx)
                                b.settingsDesc.text = opts.getOrElse(idx) { opts[0] }
                            }
                            setNegButton(R.string.cancel)
                            show()
                        }
                    }
                ),
                // ── Animations & Motion ───────────────────────────────────────
                Settings(
                    type = 2,
                    name = getString(R.string.trending_scroller),
                    desc = getString(R.string.trending_scroller_desc),
                    icon = R.drawable.ic_round_sync_24,
                    isChecked = PrefManager.getVal(PrefName.TrendingScroller),
                    switch = { isChecked, _ -> PrefManager.setVal(PrefName.TrendingScroller, isChecked) }
                ),
                Settings(
                    type = 1,
                    name = getString(R.string.blur_radius),
                    desc = "${PrefManager.getVal<Int>(PrefName.BlurRadius)} px",
                    icon = R.drawable.blur_on,
                    onClick = { b ->
                        val opts = (1..50 step 5).map { "${it} px" }.toMutableList()
                            .also { if (!it.contains("${PrefManager.getVal<Int>(PrefName.BlurRadius)} px")) it.add(0, "${PrefManager.getVal<Int>(PrefName.BlurRadius)} px") }
                            .toTypedArray()
                        val steps = (listOf(PrefManager.getVal<Int>(PrefName.BlurRadius)) + (1..50 step 5).toList()).distinct().sorted()
                        val cur = PrefManager.getVal<Int>(PrefName.BlurRadius)
                        val blurOpts = steps.map { "${it} px" }.toTypedArray()
                        customAlertDialog().apply {
                            setTitle(R.string.blur_radius)
                            singleChoiceItems(blurOpts, steps.indexOf(cur).coerceAtLeast(0)) { idx ->
                                PrefManager.setVal(PrefName.BlurRadius, steps[idx])
                                b.settingsDesc.text = "${steps[idx]} px"
                            }
                            setNegButton(R.string.cancel)
                            show()
                        }
                    }
                ),
                Settings(
                    type = 1,
                    name = getString(R.string.sampling),
                    desc = "${PrefManager.getVal<Int>(PrefName.Sampling)}×",
                    icon = R.drawable.ic_round_photo_size_select_actual_24,
                    onClick = { b ->
                        val opts = (1..8).map { "${it}×" }.toTypedArray()
                        val cur = PrefManager.getVal<Int>(PrefName.Sampling)
                        customAlertDialog().apply {
                            setTitle(R.string.sampling)
                            singleChoiceItems(opts, (cur - 1).coerceIn(0, 7)) { idx ->
                                PrefManager.setVal(PrefName.Sampling, idx + 1)
                                b.settingsDesc.text = "${idx + 1}×"
                            }
                            setNegButton(R.string.cancel)
                            show()
                        }
                    }
                ),
                // ── Notification & Status Bar ─────────────────────────────────
                Settings(
                    type = 2,
                    name = getString(R.string.hide_notification_dot),
                    desc = getString(R.string.hide_notification_dot_desc),
                    icon = R.drawable.ic_round_notifications_none_24,
                    isChecked = !PrefManager.getVal<Boolean>(PrefName.ShowNotificationRedDot),
                    switch = { isChecked, _ -> PrefManager.setVal(PrefName.ShowNotificationRedDot, !isChecked) }
                ),
                // ── Card Indicators ───────────────────────────────────────────
                Settings(
                    type = 2,
                    name = getString(R.string.show_new_episode_badge),
                    desc = getString(R.string.show_new_episode_badge_desc),
                    icon = R.drawable.ic_round_notifications_none_24,
                    pref = PrefName.ShowNewEpisodeBadge
                ),
                Settings(
                    type = 2,
                    name = getString(R.string.show_releasing_indicator),
                    desc = getString(R.string.show_releasing_indicator_desc),
                    icon = R.drawable.ic_round_auto_awesome_24,
                    pref = PrefName.ShowReleasingIndicator
                ),
                // ── Focus Effects ─────────────────────────────────────────────
                Settings(
                    type = 1,
                    name = getString(R.string.focus_effects),
                    desc = listOf(
                        getString(R.string.focus_glow),
                        getString(R.string.focus_breathing),
                        getString(R.string.focus_pulse),
                        getString(R.string.focus_shaking),
                        getString(R.string.focus_none)
                    ).getOrElse(PrefManager.getVal<Int>(PrefName.FocusEffect)) { getString(R.string.focus_glow) },
                    icon = R.drawable.ic_round_auto_awesome_24,
                    onClick = { b ->
                        val opts = arrayOf(
                            getString(R.string.focus_glow),
                            getString(R.string.focus_breathing),
                            getString(R.string.focus_pulse),
                            getString(R.string.focus_shaking),
                            getString(R.string.focus_none)
                        )
                        val cur = PrefManager.getVal<Int>(PrefName.FocusEffect)
                        customAlertDialog().apply {
                            setTitle(R.string.focus_effects)
                            singleChoiceItems(opts, cur) { idx ->
                                PrefManager.setVal(PrefName.FocusEffect, idx)
                                b.settingsDesc.text = opts.getOrElse(idx) { opts[0] }
                            }
                            setNegButton(R.string.cancel)
                            show()
                        }
                    }
                ),
            )

            settingsRecyclerView.adapter = SettingsAdapter(settingsList, this@UserInterfaceSettingsActivity)
            settingsRecyclerView.layoutManager = LinearLayoutManager(this@UserInterfaceSettingsActivity)
            settingsRecyclerView.isFocusable = true
            settingsRecyclerView.isFocusableInTouchMode = false
            settingsRecyclerView.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                    finish()
                    true
                } else false
            }
            settingsBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
    }

    // ── Animation speed dialog ────────────────────────────────────────────────

    private fun showAnimationSpeedDialog() {
        val speedView = layoutInflater.inflate(R.layout.dialog_animation_speed, null)
        val slider = speedView.findViewById<Slider>(R.id.uiSettingsAnimationSpeed)
        slider.value = PrefManager.getVal(PrefName.AnimationSpeed)
        slider.isFocusable = true
        slider.isFocusableInTouchMode = false
        slider.setOnKeyListener { _, keyCode, event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    val step = slider.stepSize.takeIf { it > 0f } ?: 0.1f
                    slider.value = (slider.value + step).coerceAtMost(slider.valueTo)
                    true
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    val step = slider.stepSize.takeIf { it > 0f } ?: 0.1f
                    slider.value = (slider.value - step).coerceAtLeast(slider.valueFrom)
                    true
                }
                else -> false
            }
        }
        customAlertDialog().apply {
            setTitle(R.string.animation_speed)
            setCustomView(speedView)
            setPosButton(R.string.ok) { PrefManager.setVal(PrefName.AnimationSpeed, slider.value) }
            setNegButton(R.string.cancel)
            show()
        }
    }

    // ── Home layout dialog ────────────────────────────────────────────────────

    private fun showHomeLayoutDialog() {
        val rv = RecyclerView(this)
        rv.layoutManager = LinearLayoutManager(this)
        rv.isFocusable = true
        rv.isFocusableInTouchMode = false
        val homeLayoutShow: List<Boolean> = PrefManager.getVal(PrefName.HomeLayout)
        val homeLayoutOrder: List<Int> = PrefManager.getVal(PrefName.HomeLayoutOrder)
        rv.adapter = HomeLayoutAdapter(
            context   = this,
            showList  = homeLayoutShow.toMutableList(),
            orderList = homeLayoutOrder.toMutableList()
        )
        customAlertDialog().apply {
            setTitle(R.string.home_layout)
            setCustomView(rv)
            setPosButton(R.string.ok) {
                val adapter = rv.adapter as HomeLayoutAdapter
                PrefManager.setVal(PrefName.HomeLayout, adapter.showList)
                PrefManager.setVal(PrefName.HomeLayoutOrder, adapter.orderList)
            }
            setNegButton(R.string.cancel)
            show()
        }
    }

    // ── Home Banner Mode dialog ───────────────────────────────────────────────

    /**
     * Shows a four-option alert dialog for choosing the Home Banner Mode:
     *   0 = Carousel   — 15 most popular anime all-time, auto-cycling
     *   1 = Profile    — User's AniList profile banner
     *   2 = Navigating — Crossfades to focused card's banner (default)
     *   3 = Off        — No banner
     */
    private fun showHomeBannerModeDialog() {
        val current: Int = PrefManager.getVal(PrefName.HomeBannerMode)

        val modeNames = arrayOf(
            "${getString(R.string.banner_mode_carousel)}\n${getString(R.string.banner_mode_carousel_desc)}",
            "${getString(R.string.banner_mode_profile)}\n${getString(R.string.banner_mode_profile_desc)}",
            "${getString(R.string.banner_mode_navigating)}\n${getString(R.string.banner_mode_navigating_desc)}",
            "${getString(R.string.banner_mode_off)}\n${getString(R.string.banner_mode_off_desc)}"
        )

        var selected = current
        customAlertDialog().apply {
            setTitle(R.string.home_banner_mode)
            singleChoiceItems(
                items             = modeNames,
                selectedItemIndex = current,
                dismissOnSelect   = false
            ) { idx -> selected = idx }
            setPosButton(R.string.ok) { PrefManager.setVal(PrefName.HomeBannerMode, selected) }
            setNegButton(R.string.cancel)
            show()
        }
    }

    // ── HomeLayoutAdapter ─────────────────────────────────────────────────────

    inner class HomeLayoutAdapter(
        private val context: android.content.Context,
        val showList:  MutableList<Boolean>,
        val orderList: MutableList<Int>
    ) : RecyclerView.Adapter<HomeLayoutAdapter.HomeLayoutViewHolder>() {

        private val sectionNames = listOf(
            context.getString(R.string.continue_watching),
            context.getString(R.string.fav_anime),
            context.getString(R.string.planned_anime),
            context.getString(R.string.missing_sequels),
            context.getString(R.string.recommended),
            context.getString(R.string.status),
            context.getString(R.string.hidden),
            context.getString(R.string.featured)
        )

        inner class HomeLayoutViewHolder(val b: ItemHomeLayoutBinding) :
            RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeLayoutViewHolder {
            val b = ItemHomeLayoutBinding.inflate(layoutInflater, parent, false)
            return HomeLayoutViewHolder(b)
        }

        override fun getItemCount() = sectionNames.size

        override fun onBindViewHolder(holder: HomeLayoutViewHolder, position: Int) {
            val b = holder.b
            val idx = if (orderList.size == sectionNames.size) orderList[position] else position
            b.homeLayoutSectionName.text = sectionNames.getOrElse(idx) { "Section $idx" }
            b.homeLayoutSectionSwitch.apply {
                isFocusable = true
                isFocusableInTouchMode = false
                isChecked = showList.getOrElse(idx) { true }
                setOnCheckedChangeListener { _, isChecked ->
                    if (idx < showList.size) showList[idx] = isChecked
                }
            }
            b.root.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN &&
                    (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)
                ) {
                    b.homeLayoutSectionSwitch.toggle()
                    true
                } else false
            }
        }
    }
}
