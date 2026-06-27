package com.sanin.tv.connections.anilist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanin.tv.connections.tmdb.TmdbApi
import com.sanin.tv.profile.User
import com.sanin.tv.media.Media
import com.sanin.tv.tryWithSuspend
import kotlinx.coroutines.launch

/**
 * Activity-scoped ViewModel shared between HomeFragment and its siblings.
 * Holds all home-page data as LiveData and orchestrates data-loading calls
 * into the Anilist query layer.
 */
class AnilistHomeViewModel : ViewModel() {

    var loaded = false

    // ── Empty-state signal ────────────────────────────────────────────────────
    val empty = MutableLiveData<Boolean>(false)

    // ── User list sections ────────────────────────────────────────────────────
    private val animeContinue   = MutableLiveData<ArrayList<Media>>(null)
    private val animeFav        = MutableLiveData<ArrayList<Media>>(null)
    private val animePlanned    = MutableLiveData<ArrayList<Media>>(null)
    private val missingSequels  = MutableLiveData<ArrayList<Media>>(null)
    private val recommendation  = MutableLiveData<ArrayList<Media>>(null)
    private val hidden          = MutableLiveData<ArrayList<Media>>(null)

    fun getAnimeContinue():  LiveData<ArrayList<Media>> = animeContinue
    fun getAnimeFav():       LiveData<ArrayList<Media>> = animeFav
    fun getAnimePlanned():   LiveData<ArrayList<Media>> = animePlanned
    fun getMissingSequels(): LiveData<ArrayList<Media>> = missingSequels
    fun getRecommendation(): LiveData<ArrayList<Media>> = recommendation
    fun getHidden():         LiveData<ArrayList<Media>> = hidden

    // ── User status (friend activity) ─────────────────────────────────────────
    private val userStatus = MutableLiveData<ArrayList<User>>(null)
    fun getUserStatus(): LiveData<ArrayList<User>> = userStatus

    // ── Popular all-time (hero carousel) ──────────────────────────────────────
    val popularAllTime = MutableLiveData<List<Media>>(emptyList())

    // ─────────────────────────────────────────────────────────────────────────
    // Data loading
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Loads user-specific home page content (continue watching, favs, planned,
     * missing sequels, recommendations, hidden).
     * Delegates to [Anilist.query.initHomePage].
     */
    suspend fun initHomePage() {
        tryWithSuspend {
            val result = Anilist.query.initHomePage()
            animeContinue.postValue(result["current"]       ?: arrayListOf())
            animeFav.postValue(result["favourites"]         ?: arrayListOf())
            animePlanned.postValue(result["planning"]       ?: arrayListOf())
            missingSequels.postValue(result["missing"]      ?: arrayListOf())
            recommendation.postValue(result["recommended"]  ?: arrayListOf())
            hidden.postValue(result["hidden"]               ?: arrayListOf())

            // Pre-warm TMDB logo cache for the most visible sections so logos
            // are ready before any card scrolls into view.
            warmLogos(result["current"])
            warmLogos(result["favourites"])
            warmLogos(result["recommended"])
        }
    }

    /**
     * Refreshes banner/cover images for list entries that were loaded without them.
     */
    suspend fun setListImages() {
        tryWithSuspend {
            Anilist.query.setListImages()
        }
    }

    /**
     * Fetches the most popular anime of all time (for the hero carousel).
     * Stores up to 15 results in [popularAllTime].
     */
    suspend fun loadPopularAllTime() {
        tryWithSuspend {
            val list = Anilist.query.loadPopularAllTime()
            if (!list.isNullOrEmpty()) {
                popularAllTime.postValue(list)
                warmLogos(list)   // Carousel cards need logos immediately
            }
        }
    }

    /**
     * Fires one background coroutine per item to call [TmdbApi.getLogoUrl],
     * seeding the in-memory cache so cards display logos without waiting.
     * Safe to call multiple times — TmdbApi skips items already cached.
     */
    private fun warmLogos(list: List<Media>?) {
        list?.forEach { media ->
            val id = media.id ?: return@forEach
            viewModelScope.launch { TmdbApi.getLogoUrl(id) }
        }
    }

    /**
     * Loads the friend-activity user statuses shown as stories at the top of Home.
     */
    suspend fun initUserStatus() {
        tryWithSuspend {
            val users = Anilist.query.getUserStatus()
            userStatus.postValue(users ?: arrayListOf())
        }
    }
}
