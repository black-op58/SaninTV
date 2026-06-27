package com.sanin.tv.feature_recommendations

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.connections.anilist.AnilistQueries
import com.sanin.tv.media.Media
import com.sanin.tv.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Feature 9: Recommendation Engine ViewModel
 * Provides LiveData for recommendation groups shown in RecommendationsFragment.
 */
class RecommendationsViewModel : ViewModel() {
    val recommendationGroups = MutableLiveData<List<RecommendationEngine.RecommendationGroup>>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()

    fun loadRecommendations(forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.postValue(true)
            error.postValue(null)
            try {
    val userId = Anilist.userid
                if (userId == null) {
                    error.postValue("Please log in to AniList to see recommendations.")
                    isLoading.postValue(false)
                    return@launch
                }

                val completedList = AnilistQueries.getWatchingMedia(userId)
                if (completedList.isNullOrEmpty()) {
                    error.postValue("No watch history found. Start watching some anime!")
                    isLoading.postValue(false)
                    return@launch
                }

                val groups = RecommendationEngine.buildRecommendations(completedList, forceRefresh)
                val topGenres = RecommendationEngine.extractGenres(completedList)

                // Re-rank recommendations within each group
                val rankedGroups = groups.map { group ->
                    group.copy(
                        recommendations = RecommendationEngine.rankRecommendations(
                            group.recommendations,
                            topGenres
                        )
                    )
                }

                recommendationGroups.postValue(rankedGroups)
                Logger.log("RecommendationsViewModel: ${rankedGroups.size} groups loaded")
            } catch (e: Exception) {
                Logger.log("RecommendationsViewModel: Error — ${e.message}")
                error.postValue("Failed to load recommendations: ${e.message}")
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    fun refresh() = loadRecommendations(forceRefresh = true)
}
