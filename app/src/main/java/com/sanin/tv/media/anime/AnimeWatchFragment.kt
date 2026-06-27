package com.sanin.tv.media.anime
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.math.MathUtils
import com.sanin.tv.R
import com.sanin.tv.databinding.FragmentMediaSourceBinding
import com.sanin.tv.media.Media
import com.sanin.tv.media.MediaDetailsActivity
import com.sanin.tv.media.MediaDetailsViewModel
import com.sanin.tv.media.MediaNameAdapter
import com.sanin.tv.media.MediaType
import com.sanin.tv.navBarHeight
import com.sanin.tv.notifications.subscription.SubscriptionHelper
import com.sanin.tv.notifications.subscription.SubscriptionHelper.Companion.saveSubscription
import com.sanin.tv.others.LanguageMapper
import com.sanin.tv.parsers.AnimeParser
import com.sanin.tv.parsers.AnimeSources
import com.sanin.tv.parsers.HAnimeSources
import com.sanin.tv.setBaseline
import com.sanin.tv.setNavigationTheme
import com.sanin.tv.toPx
import com.sanin.tv.settings.extensionprefs.AnimeSourcePreferencesFragment
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.snackString
import com.sanin.tv.toast
import com.sanin.tv.util.Logger
import com.sanin.tv.util.StoragePermissions.Companion.accessAlertDialog
import com.sanin.tv.util.StoragePermissions.Companion.hasDirAccess
import com.sanin.tv.util.customAlertDialog
import com.anggrayudi.storage.file.extension
import com.google.android.material.appbar.AppBarLayout
import eu.kanade.tachiyomi.animesource.ConfigurableAnimeSource
import eu.kanade.tachiyomi.extension.anime.model.AnimeExtension
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import tachiyomi.core.util.lang.launchIO
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt
class AnimeWatchFragment : Fragment() {    // ──────────────────────────────────────────────────────────────────────────    // Fields    // ──────────────────────────────────────────────────────────────────────────    
private var _binding: FragmentMediaSourceBinding? = null    
private val binding get() = _binding!!    
private val model: MediaDetailsViewModel by activityViewModels()    
private lateinit var media: Media    
private lateinit var gridLayoutManager: GridLayoutManager    
private lateinit var headerAdapter: MediaNameAdapter    /** Cached episodes map keyed by sourceIndex, populated by the ViewModel. */    
private var loadedEpisodes: Map<Int, Map<Int, com.sanin.tv.media.anime.Episode>>? = null
var start: Int = 0
var end: Int? = null
var style: Int = 0
var reverse: Boolean = false
var continueEp: Boolean = false
var progress: Int = View.GONE
var subscribed = false    // ──────────────────────────────────────────────────────────────────────────    // Lifecycle    // ──────────────────────────────────────────────────────────────────────────    
override fun onCreateView(        inflater: LayoutInflater,        container: ViewGroup?,        savedInstanceState: Bundle?,    ): View {        _binding = FragmentMediaSourceBinding.inflate(inflater, container, false)
return binding.root    }

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {        super.onViewCreated(view, savedInstanceState)        // ── RecyclerView ──────────────────────────────────────────────────────        gridLayoutManager = GridLayoutManager(requireContext(), 1)        binding.mediaSourceRecycler.layoutManager = gridLayoutManager        binding.mediaSourceRecycler.apply {            isFocusable = true            isFocusableInTouchMode = false            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS            // D-pad key handler on the RecyclerView itself (catches unhandled keys).            setOnKeyListener { _, keyCode, event ->
if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
val lm = gridLayoutManager
val itemCount = adapter?.itemCount ?: 0
when (keyCode) {                    KeyEvent.KEYCODE_DPAD_UP -> {
    val first = lm.findFirstCompletelyVisibleItemPosition()
if (first == 0) {                            // At top — let the parent handle focus (go to header / source bar)                            return@setOnKeyListener false                        }                        false                    }                    KeyEvent.KEYCODE_DPAD_DOWN -> {
    val last = lm.findLastCompletelyVisibleItemPosition()
if (last >= itemCount - 1) {                            // At bottom — consume so focus doesn't escape                            return@setOnKeyListener true                        }                        false
}
else -> false                }            }        }        // Scroll-to-top FAB visibility        binding.mediaSourceRecycler.addOnScrollListener(
object : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {                super.onScrolled(recyclerView, dx, dy)                
val position = gridLayoutManager.findFirstVisibleItemPosition()
if (position > 2) {                    binding.ScrollTop.translationY = -(navBarHeight + 12.toPx).toFloat()                    binding.ScrollTop.visibility = View.VISIBLE
} else {                    binding.ScrollTop.visibility = View.GONE                }            }        })        model.scrolledToTop.observe(viewLifecycleOwner) {
if (it) binding.mediaSourceRecycler.scrollToPosition(0)        }        continueEp = model.continueMedia ?: false        model.getMedia().observe(viewLifecycleOwner) {
if (it != null) {
if (this::media.isInitialized) {
if (it.anime != null && it.anime?.episodes == null) {                        it.anime?.episodes = media.anime?.episodes                    }                }                media = it                media.selected = model.loadSelected(media)                
val _hasSavedSel = com.sanin.tv.settings.saving.PrefManager.getNullableCustomVal("Selected-${media.id}", null, com.sanin.tv.media.Selected::class.java) != null
if (!_hasSavedSel) {
    val _lastExt = com.sanin.tv.home.WatchProgressManager.getLastExtensionIndex()                    
val _srcSize = model.watchSources?.size ?: 0
if (_lastExt > 0 && _lastExt < _srcSize) {                        media.selected!!.sourceIndex = _lastExt                        model.saveSelected(media.id, media.selected!!)                    }                }
if (media.format == "LOCAL") {
    val localSourceIndex = AnimeSources.list.indexOfFirst { parser -> parser.name == "Local" }                        .takeIf { parserIndex -> parserIndex >= 0 } ?: 0                    media.selected!!.sourceIndex = localSourceIndex                }                subscribed =                    SubscriptionHelper.getSubscriptions().containsKey(media.id)                style = media.selected!!.recyclerStyle                reverse = media.selected!!.recyclerReversed                progress = View.GONE                binding.mediaInfoProgressBar.visibility = progress
if (loadedEpisodes != null) {
    val episodes = loadedEpisodes[media.selected!!.sourceIndex]
if (episodes != null) {
    val metadataPriority = PrefManager.getVal<Int>(PrefName.EpisodeMetadataSource)                        episodes.forEach { (i, episode) ->                            // 1. Jikan (Lowest for metadata, only source for filler flag)
if (media.anime?.fillerEpisodes != null) {
if (media.anime!!.fillerEpisodes!!.containsKey(i)) {
    val fillerEp = media.anime!!.fillerEpisodes!![i]                                    episode.filler = fillerEp?.filler ?: false                                    episode.date = fillerEp?.date ?: episode.date                                }                            }

val applyKitsu = {
if (media.anime?.kitsuEpisodes != null) {
if (media.anime!!.kitsuEpisodes!!.containsKey(i)) {
    val kitsuEp = media.anime!!.kitsuEpisodes!![i]                                        episode.desc = kitsuEp?.desc ?: episode.desc                                        episode.thumb = kitsuEp?.thumb ?: episode.thumb                                    }                                }                            }

val applyAniZip = {
if (media.anime?.anifyEpisodes != null) {
if (media.anime!!.anifyEpisodes!!.containsKey(i)) {
    val anifyEp = media.anime!!.anifyEpisodes!![i]                                        episode.desc = anifyEp?.desc ?: episode.desc                                        episode.thumb = anifyEp?.thumb ?: episode.thumb                                        episode.rating = anifyEp?.extra?.get("rating") ?: episode.rating
val airDate = anifyEp?.extra?.get("airDate")
if (!airDate.isNullOrBlank()) {                                            episode.date = airDate.substringBefore("T")                                        }                                    }                                }                            }
if (metadataPriority == 0) {                                applyAniZip()                                applyKitsu()
} else {                                applyKitsu()                                applyAniZip()                            }                            // Title fallback order: AniZip English -> Kitsu -> Jikan/MAL -> "Episode X"                            
val anifyTitle = cleanTitle(media.anime?.anifyEpisodes?.get(i)?.title)                            
val kitsuTitle = cleanTitle(media.anime?.kitsuEpisodes?.get(i)?.title)                            
val jikanTitle = cleanTitle(media.anime?.fillerEpisodes?.get(i)?.title)                            episode.title = anifyTitle ?: kitsuTitle ?: jikanTitle ?: buildFallbackEpisodeTitle(i, episode)                        }                        media.anime?.episodes = episodes                        // CHIP GROUP
val total = episodes.size
val divisions = total.toDouble() / 10                        start = 0                        end = null
val limit = when {                            (divisions < 25) -> 25                            (divisions < 50) -> 50
else -> 100                        }                        headerAdapter.clearChips()
if (total > limit) {
    val arr = media.anime!!.episodes!!.keys.toTypedArray()                            
val stored = ceil((total).toDouble() / limit).toInt()                            
val position = MathUtils.clamp(media.selected!!.chip, 0, stored - 1)                            
val last = if (position + 1 == stored) total else (limit * (position + 1))                            start = limit * (position)                            end = last - 1                            headerAdapter.updateChips(                                limit,                                arr,                                (1..stored).toList().toTypedArray(),                                position                            )                        }                    } // end if (episodes != null)                } // end if (loadedEpisodes != null)            } // end if (it != null)        } // end observe    } // end onViewCreated    
override fun onDestroyView() {        _binding = null        super.onDestroyView()    }    // ──────────────────────────────────────────────────────────────────────────    // Public API called by the host Activity / HeaderAdapter    // ──────────────────────────────────────────────────────────────────────────    
fun onSourceChange(i: Int): AnimeParser {        media.anime?.episodes = null        reload()        
val selected = model.loadSelected(media)        model.watchSources?.get(selected.sourceIndex)?.showUserTextListener = null        selected.sourceIndex = i        selected.server = null        model.saveSelected(media.id, selected)        media.selected = selected        com.sanin.tv.home.WatchProgressManager.saveLastExtensionIndex(i)
return model.watchSources?.get(i)!!    }

fun onLangChange(i: Int) {
    val selected = model.loadSelected(media)        selected.langIndex = i        model.saveSelected(media.id, selected)        media.selected = selected    }

fun onDubClicked(checked: Boolean) {
    val selected = model.loadSelected(media)        model.watchSources?.get(selected.sourceIndex)?.selectDub = checked        selected.preferDub = checked        model.saveSelected(media.id, selected)        media.selected = selected        lifecycleScope.launch(Dispatchers.IO) {            model.forceLoadEpisode(                media,                selected.sourceIndex            )        }    }

fun loadEpisodes(i: Int, invalidate: Boolean) {        lifecycleScope.launch(Dispatchers.IO) { model.loadEpisodes(media, i, invalidate) }    }

fun loadKitsuEpisodesAsync() {        lifecycleScope.launch(Dispatchers.IO) { model.loadKitsuEpisodes(media) }    }

fun onIconPressed(viewType: Int, rev: Boolean) {        style = viewType        reverse = rev        media.selected!!.recyclerStyle = style        media.selected!!.recyclerReversed = reverse        model.saveSelected(media.id, media.selected!!)        reload()    }

fun onChipClicked(i: Int, s: Int, e: Int) {        media.selected!!.chip = i        start = s        end = e        model.saveSelected(media.id, media.selected!!)        reload()    }

fun onNotificationPressed(subscribed: Boolean, source: String) {        this.subscribed = subscribed        saveSubscription(media, subscribed)        snackString(
if (subscribed) getString(R.string.subscribed_notification, source)
else getString(R.string.unsubscribed_notification)        )    }

fun openSettings(pkg: AnimeExtension.Installed) {
    val changeUIVisibility: (Boolean) -> Unit = { show ->            
val activity = activity
if (activity is MediaDetailsActivity && isAdded) {                activity.findViewById<AppBarLayout>(R.id.mediaAppBar).isVisible = show                activity.findViewById<ViewPager2>(R.id.mediaViewPager).isVisible = show                activity.findViewById<CardView>(R.id.mediaCover).isVisible = show                activity.findViewById<CardView>(R.id.mediaClose).isVisible = show                activity.navBar.isVisible = show                activity.findViewById<FrameLayout>(R.id.fragmentExtensionsContainer).isGone = show            }        }

var itemSelected = false
val allSettings = pkg.sources.filterIsInstance<ConfigurableAnimeSource>(
if (allSettings.isNotEmpty()) {
    var selectedSetting = allSettings[0]
if (allSettings.size > 1) {
    val names =                    allSettings.map { LanguageMapper.getLanguageName(it.lang) }.toTypedArray()                requireContext()                    .customAlertDialog()                    .apply {                        setTitle("Select a Source")                        singleChoiceItems(names) { which ->                            selectedSetting = allSettings[which]                            itemSelected = true                            requireActivity().runOnUiThread {
    val fragment =                                    AnimeSourcePreferencesFragment().getInstance(selectedSetting.id) {                                        changeUIVisibility(true)                                        loadEpisodes(media.selected!!.sourceIndex, true)                                    }                                parentFragmentManager.beginTransaction()                                    .setCustomAnimations(R.anim.slide_up, R.anim.slide_down)                                    .replace(R.id.fragmentExtensionsContainer, fragment)                                    .addToBackStack(null)                                    .commit()                            }                        }                        onDismiss {
if (!itemSelected) {                                changeUIVisibility(true)                            }                        }                        show()                    }
} else {                // If there's only one setting, proceed with the fragment transaction                requireActivity().runOnUiThread {
    val fragment =                        AnimeSourcePreferencesFragment().getInstance(selectedSetting.id) {                            changeUIVisibility(true)                            loadEpisodes(media.selected!!.sourceIndex, true)                        }                    changeUIVisibility(false)                    parentFragmentManager.beginTransaction()                        .setCustomAnimations(R.anim.slide_up, R.anim.slide_down)                        .replace(R.id.fragmentExtensionsContainer, fragment)                        .addToBackStack(null)                        .commit()                }            }        }    }    // ──────────────────────────────────────────────────────────────────────────    // Private helpers    // ──────────────────────────────────────────────────────────────────────────    
private fun reload() {        // Triggers the RecyclerView adapter to re-bind with the current episode range.        model.scrolledToTop.postValue(true)    }    /** Returns null for blank/generic titles like "null" or empty strings. */    
private fun cleanTitle(raw: String?): String? {
if (raw.isNullOrBlank() || raw.equals("null", ignoreCase = true)) return null
return raw    }

private fun buildFallbackEpisodeTitle(index: Int, episode: com.sanin.tv.media.anime.Episode): String {
return episode.number?.let { "Episode $it" } ?: "Episode ${index + 1}"    }}
}}}}
