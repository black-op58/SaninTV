package com.sanin.tv.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.sanin.tv.R
import com.sanin.tv.connections.tmdb.TmdbApi
import com.sanin.tv.providers.ProviderSourcesActivity
import com.sanin.tv.databinding.ActivitySettingsAnimeBinding
import com.sanin.tv.navBarHeight
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.settings.saving.SourceMemoryManager
import com.sanin.tv.snackString
import com.sanin.tv.statusBarHeight
import com.sanin.tv.util.customAlertDialog

class SettingsAnimeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsAnimeBinding

    private var cacheStorageDesc: TextView? = null

    private val folderPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            TmdbApi.migrateStorageSaf(uri)
            PrefManager.setVal(PrefName.CacheStorageUri, uri.toString())
            cacheStorageDesc?.text = uri.lastPathSegment
                ?: getString(R.string.cache_storage_custom)
            snackString(getString(R.string.cache_storage_changed_folder))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsAnimeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            settingsAnimeLayout.updatePadding(top = statusBarHeight, bottom = navBarHeight)
            animeSettingsBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

            settingsEpList.setOnClickListener {
                setEpViewAlpha(settingsEpList, settingsEpGrid, settingsEpCompact)
                snackString(getString(R.string.ep_list_view))
            }
            settingsEpGrid.setOnClickListener {
                setEpViewAlpha(settingsEpGrid, settingsEpList, settingsEpCompact)
                snackString(getString(R.string.ep_grid_view))
            }
            settingsEpCompact.setOnClickListener {
                setEpViewAlpha(settingsEpCompact, settingsEpList, settingsEpGrid)
                snackString(getString(R.string.ep_compact_view))
            }

            val settingsList = arrayListOf(

                // ─── Logo Art section header ──────────────────────────────
                Settings(
                    type  = 1,
                    name  = getString(R.string.tmdb_section),
                    desc  = getString(R.string.tmdb_section_desc),
                    icon  = R.drawable.ic_round_auto_awesome_24,
                    onClick = null
                ),

                // Clear logo cache
                Settings(
                    type  = 1,
                    name  = getString(R.string.clear_tmdb_cache),
                    desc  = getString(R.string.clear_tmdb_cache_desc),
                    icon  = R.drawable.ic_round_delete_24,
                    attach = { b ->
                        val bytes = TmdbApi.cacheFileSizeBytes()
                        b.settingsDesc.text = if (bytes > 0)
                            getString(R.string.clear_tmdb_cache_size, bytes / 1024)
                        else
                            getString(R.string.clear_tmdb_cache_desc)
                    },
                    onClick = { b ->
                        customAlertDialog().apply {
                            setTitle(R.string.clear_tmdb_cache)
                            setMessage(getString(R.string.clear_tmdb_cache_confirm))
                            setPosButton(R.string.yes) {
                                TmdbApi.clearCache()
                                b.settingsDesc.text = getString(R.string.clear_tmdb_cache_desc)
                                snackString(getString(R.string.clear_tmdb_cache_done))
                            }
                            setNegButton(R.string.no)
                            show()
                        }
                    }
                ),

                // Logo cache storage folder (SAF)
                Settings(
                    type  = 1,
                    name  = getString(R.string.cache_storage),
                    desc  = getString(R.string.cache_storage_desc),
                    icon  = R.drawable.ic_round_dns_24,
                    isActivity = true,
                    attach = { b ->
                        val uriStr = PrefManager.getVal<String>(PrefName.CacheStorageUri)
                        b.settingsDesc.text = if (uriStr.isBlank())
                            getString(R.string.cache_storage_desc)
                        else
                            Uri.parse(uriStr).lastPathSegment
                                ?: getString(R.string.cache_storage_custom)
                        cacheStorageDesc = b.settingsDesc
                    },
                    onClick = { _ ->
                        val uriStr = PrefManager.getVal<String>(PrefName.CacheStorageUri)
                        val options = if (uriStr.isBlank()) {
                            arrayOf(getString(R.string.cache_storage_choose_folder))
                        } else {
                            arrayOf(
                                getString(R.string.cache_storage_choose_folder),
                                getString(R.string.cache_storage_reset_internal)
                            )
                        }
                        customAlertDialog().apply {
                            setTitle(R.string.cache_storage)
                            singleChoiceItems(items = options) { idx ->
                                when (idx) {
                                    0 -> folderPickerLauncher.launch(null)
                                    1 -> {
                                        TmdbApi.resetStorageToInternal()
                                        PrefManager.setVal(PrefName.CacheStorageUri, "")
                                        cacheStorageDesc?.text = getString(R.string.cache_storage_desc)
                                        snackString(getString(R.string.cache_storage_reset_done))
                                    }
                                }
                            }
                            setNegButton(R.string.cancel)
                            show()
                        }
                    }
                ),

                // ─── Stream Providers ─────────────────────────────────────
                Settings(
                    type       = 1,
                    name       = getString(R.string.provider_sources_entry),
                    desc       = getString(R.string.provider_sources_entry_desc),
                    icon       = R.drawable.ic_round_dns_24,
                    isActivity = true,
                    onClick    = { _ ->
                        startActivity(Intent(this@SettingsAnimeActivity, ProviderSourcesActivity::class.java))
                    }
                ),

                // ─── Continue Watching section header ─────────────────────
                Settings(
                    type  = 1,
                    name  = getString(R.string.continue_watching),
                    desc  = getString(R.string.continue_watching_desc),
                    icon  = R.drawable.ic_round_play_circle_24,
                    onClick = null
                ),

                // Source memory expiry
                Settings(
                    type  = 1,
                    name  = getString(R.string.source_memory_expiry),
                    desc  = getString(R.string.source_memory_expiry_desc),
                    icon  = R.drawable.ic_round_calendar_today_24,
                    isActivity = true,
                    attach = { b ->
                        val hours = PrefManager.getVal<Int>(PrefName.SourceMemoryExpiryHours)
                        b.settingsDesc.text = SourceMemoryManager.EXPIRY_OPTIONS
                            .firstOrNull { it.hours == hours }?.label
                            ?: "1 day"
                    },
                    onClick = { b ->
                        val options = SourceMemoryManager.EXPIRY_OPTIONS
                        val labels  = options.map { it.label }.toTypedArray()
                        val current = PrefManager.getVal<Int>(PrefName.SourceMemoryExpiryHours)
                        val idx     = options.indexOfFirst { it.hours == current }.coerceAtLeast(0)

                        customAlertDialog().apply {
                            setTitle(R.string.source_memory_expiry)
                            setSingleChoiceItems(labels, idx) { _, which ->
                                PrefManager.setVal(PrefName.SourceMemoryExpiryHours, options[which].hours)
                                b.settingsDesc.text = options[which].label
                            }
                            setNegButton(R.string.cancel)
                            show()
                        }
                    }
                ),

                // Screenshot toggle
                Settings(
                    type      = 2,
                    name      = getString(R.string.continue_watching_screenshot),
                    desc      = getString(R.string.continue_watching_screenshot_desc),
                    icon      = R.drawable.ic_round_art_track_24,
                    isChecked = PrefManager.getVal(PrefName.ContinueWatchingShowScreenshot),
                    switch    = { isChecked, _ ->
                        PrefManager.setVal(PrefName.ContinueWatchingShowScreenshot, isChecked)
                    }
                ),

                // Clear source memory
                Settings(
                    type  = 1,
                    name  = getString(R.string.clear_source_memory),
                    desc  = getString(R.string.clear_source_memory_desc),
                    icon  = R.drawable.ic_round_delete_24,
                    onClick = { _ ->
                        customAlertDialog().apply {
                            setTitle(R.string.clear_source_memory)
                            setMessage(getString(R.string.clear_source_memory_confirm))
                            setPosButton(R.string.yes) {
                                SourceMemoryManager.clearAll()
                                snackString(getString(R.string.clear_source_memory_done))
                            }
                            setNegButton(R.string.no)
                            show()
                        }
                    }
                ),

                // ─── Content ──────────────────────────────────────────────────
                Settings(
                    type  = 2,
                    name  = getString(R.string.prefer_dub),
                    desc  = getString(R.string.prefer_dub_desc),
                    icon  = R.drawable.ic_round_audiotrack_24,
                    isChecked = PrefManager.getVal(PrefName.PreferDub),
                    switch = { isChecked, _ -> PrefManager.setVal(PrefName.PreferDub, isChecked) }
                ),
                Settings(
                    type  = 2,
                    name  = getString(R.string.smart_source_persistence),
                    desc  = getString(R.string.smart_source_persistence_desc),
                    icon  = R.drawable.ic_round_dns_24,
                    isChecked = PrefManager.getVal(PrefName.SmartSourcePersistence),
                    switch = { isChecked, _ -> PrefManager.setVal(PrefName.SmartSourcePersistence, isChecked) }
                ),

                // ─── Sync / AniList ───────────────────────────────────────────
                Settings(
                    type  = 2,
                    name  = getString(R.string.auto_sync_anilist),
                    desc  = getString(R.string.auto_sync_anilist_desc),
                    icon  = R.drawable.ic_round_sync_24,
                    isChecked = PrefManager.getVal(PrefName.AutoSyncAniList),
                    switch = { isChecked, _ -> PrefManager.setVal(PrefName.AutoSyncAniList, isChecked) }
                ),
                Settings(
                    type  = 2,
                    name  = getString(R.string.update_progress_automatically),
                    desc  = getString(R.string.update_progress_automatically_desc),
                    icon  = R.drawable.ic_round_sync_24,
                    isChecked = PrefManager.getVal(PrefName.UpdateProgressAutomatically),
                    switch = { isChecked, _ -> PrefManager.setVal(PrefName.UpdateProgressAutomatically, isChecked) }
                ),
                Settings(
                    type  = 2,
                    name  = getString(R.string.update_progress_chapters),
                    desc  = getString(R.string.update_progress_chapters_desc),
                    icon  = R.drawable.ic_round_menu_book_24,
                    isChecked = PrefManager.getVal(PrefName.UpdateProgressForChapters),
                    switch = { isChecked, _ -> PrefManager.setVal(PrefName.UpdateProgressForChapters, isChecked) }
                ),
                Settings(
                    type  = 2,
                    name  = getString(R.string.update_progress_hentai),
                    desc  = getString(R.string.update_progress_hentai_desc),
                    icon  = R.drawable.ic_round_nsfw_24,
                    isChecked = PrefManager.getVal(PrefName.UpdateProgressForHentai),
                    switch = { isChecked, _ -> PrefManager.setVal(PrefName.UpdateProgressForHentai, isChecked) }
                ),

                // ─── Home Layout Visibility ───────────────────────────────────
                Settings(
                    type  = 2,
                    name  = getString(R.string.show_continue_watching),
                    desc  = getString(R.string.show_continue_watching_desc),
                    icon  = R.drawable.ic_round_play_circle_24,
                    isChecked = PrefManager.getVal(PrefName.ShowContinueWatching),
                    switch = { isChecked, _ -> PrefManager.setVal(PrefName.ShowContinueWatching, isChecked) }
                ),
                Settings(
                    type  = 2,
                    name  = getString(R.string.show_planned),
                    desc  = getString(R.string.show_planned_desc),
                    icon  = R.drawable.ic_round_star_24,
                    isChecked = PrefManager.getVal(PrefName.ShowPlanned),
                    switch = { isChecked, _ -> PrefManager.setVal(PrefName.ShowPlanned, isChecked) }
                ),
                Settings(
                    type  = 2,
                    name  = getString(R.string.show_recommendations),
                    desc  = getString(R.string.show_recommendations_desc),
                    icon  = R.drawable.ic_round_auto_awesome_24,
                    isChecked = PrefManager.getVal(PrefName.ShowRecommendations),
                    switch = { isChecked, _ -> PrefManager.setVal(PrefName.ShowRecommendations, isChecked) }
                ),
                Settings(
                    type  = 2,
                    name  = getString(R.string.show_trending),
                    desc  = getString(R.string.show_trending_desc),
                    icon  = R.drawable.ic_round_sync_24,
                    isChecked = PrefManager.getVal(PrefName.ShowTrending),
                    switch = { isChecked, _ -> PrefManager.setVal(PrefName.ShowTrending, isChecked) }
                ),
                Settings(
                    type  = 2,
                    name  = getString(R.string.show_popular),
                    desc  = getString(R.string.show_popular_desc),
                    icon  = R.drawable.ic_round_favorite_24,
                    isChecked = PrefManager.getVal(PrefName.ShowPopular),
                    switch = { isChecked, _ -> PrefManager.setVal(PrefName.ShowPopular, isChecked) }
                ),
                Settings(
                    type  = 2,
                    name  = getString(R.string.show_recent),
                    desc  = getString(R.string.show_recent_desc),
                    icon  = R.drawable.ic_round_calendar_today_24,
                    isChecked = PrefManager.getVal(PrefName.ShowRecent),
                    switch = { isChecked, _ -> PrefManager.setVal(PrefName.ShowRecent, isChecked) }
                )
            )

            settingsRecyclerView.apply {
                adapter = SettingsAdapter(settingsList)
                layoutManager = LinearLayoutManager(this@SettingsAnimeActivity)
                isFocusable = true
                isFocusableInTouchMode = false
                setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN &&
                        keyCode == KeyEvent.KEYCODE_BACK) {
                        finish(); true
                    } else false
                }
            }
        }
    }

    private fun setEpViewAlpha(active: android.view.View, vararg inactive: android.view.View) {
        active.alpha = 1f
        inactive.forEach { it.alpha = 0.33f }
    }
}
