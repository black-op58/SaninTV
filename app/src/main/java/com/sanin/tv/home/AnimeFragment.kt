package com.sanin.tv.home
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.sanin.tv.R
import com.sanin.tv.Refresh
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.connections.anilist.AnilistAnimeViewModel
import com.sanin.tv.connections.anilist.getUserId
import com.sanin.tv.databinding.FragmentAnimeBinding
import com.sanin.tv.media.MediaAdaptor
import com.sanin.tv.navBarHeight
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.statusBarHeight
import com.sanin.tv.ui.activities.SearchActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
class AnimeFragment : Fragment() {
    private var _binding: FragmentAnimeBinding? = null    
private val binding get() = _binding!!    
private lateinit var animePageAdapter: AnimePageAdapter
val model: AnilistAnimeViewModel by activityViewModels()    
override fun onCreateView(        inflater: LayoutInflater,        container: ViewGroup?,        savedInstanceState: Bundle?    ): View {        _binding = FragmentAnimeBinding.inflate(inflater, container, false)
return binding.root    }

override fun onDestroyView() {        super.onDestroyView()        _binding = null    }

@SuppressLint("NotifyDataSetChanged")    
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {        super.onViewCreated(view, savedInstanceState)        
val scope = viewLifecycleOwner.lifecycleScope
var height = statusBarHeight
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    val displayCutout = activity?.window?.decorView?.rootWindowInsets?.displayCutout
if (displayCutout != null && displayCutout.boundingRects.size > 0) {                height = max(                    statusBarHeight,                    min(                        displayCutout.boundingRects[0].width(),                        displayCutout.boundingRects[0].height()                    )                )            }        }        animePageAdapter = AnimePageAdapter(            requireActivity(),            requireActivity().supportFragmentManager,            viewLifecycleOwner.lifecycle        )        binding.animeViewPager.adapter = animePageAdapter        binding.animeViewPager.offscreenPageLimit = 1        binding.animeViewPager.apply {            isFocusable = true            getChildAt(0)?.setOnKeyListener { _, keyCode, event ->
if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
val count = animePageAdapter.itemCount
when (keyCode) {                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
    val next = (currentItem + 1).coerceAtMost(count - 1)                        setCurrentItem(next, true)
true                    }                    KeyEvent.KEYCODE_DPAD_LEFT -> {
    val prev = (currentItem - 1).coerceAtLeast(0)                        setCurrentItem(prev, true)
true
}
else -> false                }            }        }        // Scroll-to-top FAB visibility.        binding.animePageRecyclerView?.addOnScrollListener(            
object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
    override fun onScrolled(                    rv: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int                ) {                    super.onScrolled(rv, dx, dy)
if (dy > 0) {                        // Scrolling down — hide FAB after a short delay.                        rv.postDelayed({
if (_binding != null)                                binding.animePageScrollTop.visibility = View.GONE                        }, 300)                    }                }            }        )        animePageAdapter.ready.observe(viewLifecycleOwner) { i ->
if (i) {                model.getUpdated().observe(viewLifecycleOwner) {
if (it != null)                        animePageAdapter.updateRecent(MediaAdaptor(0, it, requireActivity()), it)                }                model.getMovies().observe(viewLifecycleOwner) {
if (it != null)                        animePageAdapter.updateMovies(MediaAdaptor(0, it, requireActivity()), it)                }                model.getTopRated().observe(viewLifecycleOwner) {
if (it != null)                        animePageAdapter.updateTopRated(MediaAdaptor(0, it, requireActivity()), it)                }                model.getMostFav().observe(viewLifecycleOwner) {
if (it != null)                        animePageAdapter.updateMostFav(MediaAdaptor(0, it, requireActivity()), it)                }
if (animePageAdapter.trendingViewPager != null) {                    animePageAdapter.updateHeight()                    model.getTrending().observe(viewLifecycleOwner) {
if (it != null) {                            animePageAdapter.updateTrending(                                MediaAdaptor(
if (PrefManager.getVal(PrefName.SmallView)) 3 else 2,                                    it,                                    requireActivity(),                                    viewPager = animePageAdapter.trendingViewPager                                )                            )                            animePageAdapter.updateAvatar()                        }                    }                }                binding.animePageScrollTop.translationY = -navBarHeight.toFloat()            }        }

fun load() = scope.launch(Dispatchers.Main) {            animePageAdapter.updateAvatar()        }        animePageAdapter.onSeasonClick = { i ->            scope.launch(Dispatchers.IO) { model.loadTrending(i) }        }        animePageAdapter.onSeasonLongClick = { i ->            
val (season, year) = Anilist.currentSeasons[i]            ContextCompat.startActivity(                requireContext(),                Intent(requireContext(), SearchActivity::class.java)                    .putExtra("type", "ANIME")                    .putExtra("season", season)                    .putExtra("seasonYear", year.toString())                    .putExtra("search", true),                null            )            true        }

var running = false
val live = Refresh.activity.getOrPut(this.hashCode()) { MutableLiveData(false) }        live.observe(viewLifecycleOwner) {
if (it && !running) {                running = true                scope.launch {                    withContext(Dispatchers.IO) {
    val rescueMode: Boolean = PrefManager.getVal(PrefName.RescueMode)
if (rescueMode) {                            withContext(Dispatchers.Main) { load() }
} else {                            Anilist.userid =                                PrefManager.getNullableVal<String>(PrefName.AnilistUserId, null)                                    ?.toIntOrNull()
if (Anilist.userid == null) {                                getUserId(requireContext()) { load() }
} else {                                scope.launch(Dispatchers.IO) {                                    getUserId(requireContext()) { load() }                                }                            }                        }                    }                    model.loaded = true
val loadTrending = async(Dispatchers.IO) { model.loadTrending(1) }

val loadAll      = async(Dispatchers.IO) { model.loadAll() }

val loadPopular  = async(Dispatchers.IO) {                        model.loadPopular(                            "ANIME",                            sort   = Anilist.sortBy[1],                            onList = PrefManager.getVal(PrefName.PopularAnimeList)                        )                    }                    loadTrending.await()
loadAll.await(
loadPopular.await()                    live.postValue(false)                    _binding?.animeRefresh?.isRefreshing = false                    running = false                }            }        }    }

override fun onResume() {
if (!model.loaded) Refresh.activity[this.hashCode()]!!.postValue(true
if (animePageAdapter.trendingViewPager != null) {            binding.root.requestApplyInsets()            binding.root.requestLayout()        }
if (this::animePageAdapter.isInitialized && _binding != null) {            animePageAdapter.updateNotificationCount()        }        super.onResume()    }
