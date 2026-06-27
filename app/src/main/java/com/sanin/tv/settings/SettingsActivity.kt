package com.sanin.tv.settings

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.sanin.tv.R
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.databinding.ActivitySettingsBinding
import com.sanin.tv.navBarHeight
import com.sanin.tv.profile.ProfileActivity
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.statusBarHeight
import kotlin.random.Random

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            settingsContainer.updatePadding(top = statusBarHeight, bottom = navBarHeight)

            val settingsList = listOf(
                // ── Profile (first item) ──────────────────────────────────
                Settings(
                    type    = 1,
                    name    = getString(R.string.profile),
                    desc    = "View and manage your profile",
                    icon    = R.drawable.ic_round_person_24,
                    onClick = {
                        startActivity(
                            Intent(this@SettingsActivity, ProfileActivity::class.java)
                                .putExtra("userId", Anilist.userid ?: -1)
                        )
                    }
                ),
                // ── Account ───────────────────────────────────────────────
                Settings(
                    type    = 1,
                    name    = getString(R.string.account),
                    desc    = getString(R.string.account_desc),
                    icon    = R.drawable.ic_round_manage_accounts_24,
                    onClick = {
                        startActivity(Intent(this@SettingsActivity, SettingsAccountActivity::class.java))
                    }
                ),
                // ── UI ────────────────────────────────────────────────────
                Settings(
                    type    = 1,
                    name    = getString(R.string.ui),
                    desc    = getString(R.string.ui_desc),
                    icon    = R.drawable.ic_round_auto_awesome_24,
                    onClick = {
                        startActivity(Intent(this@SettingsActivity, UserInterfaceSettingsActivity::class.java))
                    }
                ),
                // ── Common ────────────────────────────────────────────────
                Settings(
                    type    = 1,
                    name    = getString(R.string.common),
                    desc    = getString(R.string.common_desc),
                    icon    = R.drawable.ic_round_area_chart_24,
                    onClick = {
                        startActivity(Intent(this@SettingsActivity, SettingsCommonActivity::class.java))
                    }
                ),
                // ── Anime ─────────────────────────────────────────────────
                Settings(
                    type    = 1,
                    name    = getString(R.string.anime),
                    desc    = getString(R.string.anime_desc),
                    icon    = R.drawable.ic_round_movie_filter_24,
                    onClick = {
                        startActivity(Intent(this@SettingsActivity, SettingsAnimeActivity::class.java))
                    }
                ),
                // ── Advanced Video ────────────────────────────────────────
                Settings(
                    type       = 1,
                    name       = getString(R.string.advanced_video),
                    desc       = getString(R.string.advanced_video_desc),
                    icon       = R.drawable.ic_round_movie_filter_24,
                    isActivity = true,
                    onClick    = {
                        startActivity(Intent(this@SettingsActivity, SettingsVideoActivity::class.java))
                    }
                ),
                // ── Manga ─────────────────────────────────────────────────
                Settings(
                    type    = 1,
                    name    = getString(R.string.manga),
                    desc    = getString(R.string.manga_desc),
                    icon    = R.drawable.ic_round_import_contacts_24,
                    onClick = {
                        startActivity(Intent(this@SettingsActivity, SettingsMangaActivity::class.java))
                    }
                ),
                // ── Extensions ────────────────────────────────────────────
                Settings(
                    type    = 1,
                    name    = getString(R.string.extensions),
                    desc    = getString(R.string.ext_desc),
                    icon    = R.drawable.ic_round_extension_24,
                    onClick = {
                        startActivity(Intent(this@SettingsActivity, SettingsExtensionsActivity::class.java))
                    }
                ),
                // ── Notifications ─────────────────────────────────────────
                Settings(
                    type    = 1,
                    name    = getString(R.string.notifications),
                    desc    = getString(R.string.notifications_desc),
                    icon    = R.drawable.ic_round_notifications_none_24,
                    onClick = {
                        startActivity(Intent(this@SettingsActivity, SettingsNotificationActivity::class.java))
                    }
                ),
            )

            settingsRecyclerView.apply {
                adapter = SettingsAdapter(settingsList)
                layoutManager = LinearLayoutManager(this@SettingsActivity)
                isFocusable = true
                isFocusableInTouchMode = false
                setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN &&
                        keyCode == KeyEvent.KEYCODE_BACK) {
                        finish()
                        true
                    } else false
                }
            }

            settingsBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

            // Randomised logo animation on each visit
            val animations = listOf(
                R.drawable.settings_logo_animation,
                R.drawable.settings_logo_animation2,
                R.drawable.settings_logo_animation3
            )
            settingsLogo.setImageResource(animations[Random.nextInt(animations.size)])
            (settingsLogo.drawable as? Animatable)?.start()

            settingsVersion.apply {
                val versionName = context.packageManager
                    .getPackageInfo(context.packageName, 0).versionName
                text = buildString {
                    append(getString(R.string.version))
                    append(": $versionName (${getString(R.string.dantotsu)})")
                }
            }
        }
    }
}
