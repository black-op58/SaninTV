package com.sanin.tv.feature_watch

import android.view.View
import android.view.animation.AnimationUtils
import androidx.lifecycle.LifecycleCoroutineScope
import com.sanin.tv.R
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Feature 6: Skip Intro / Skip Outro — overlay button controller
 *
 * Usage inside your video player Fragment/Activity:
 *
 *   // 1. Create once after inflating the view
 *   val skipManager = SkipIntroManager(
 *       skipButton = binding.skipIntroButton,   // a Button/TextView in your layout
 *       onSkip = { seconds -> player.seekTo((seconds * 1000).toLong()) }
 *   )
 *
 *   // 2. Load timestamps when episode starts (you need the MAL ID)
 *   skipManager.load(lifecycleScope, malId = media.malId ?: return, episodeNumber = ep)
 *
 *   // 3. Call every ~500ms from your player progress callback
 *   skipManager.onPlayerProgress(currentPositionSeconds)
 *
 * Layout requirement — add to your player overlay XML:
 *
 *   <Button
 *       android:id="@+id/skipIntroButton"
 *       android:layout_width="wrap_content"
 *       android:layout_height="wrap_content"
 *       android:text="@string/skip_intro"
 *       android:visibility="gone"
 *       ... />
 */
class SkipIntroManager(
    private val skipButton: View,
    private val onSkip: (Double) -> Unit
) {
    private var result: AniSkipApi.AniSkipResult? = null
    private var fetchJob: Job? = null
    private var currentInterval: AniSkipApi.SkipInterval? = null

    fun load(scope: LifecycleCoroutineScope, malId: Int, episodeNumber: Int) {
        fetchJob?.cancel()
        result = null
        hideButton()

        fetchJob = scope.launch {
            result = AniSkipApi.getSkipTimes(malId, episodeNumber)
        }
    }

    fun onPlayerProgress(currentSeconds: Double) {
        if (!PrefManager.getVal<Boolean>(PrefName.ShowSkipIntroButton)) return
        val skipResult = result ?: return

        val activeInterval = skipResult.intervals.firstOrNull { it.containsTime(currentSeconds) }

        if (activeInterval != null && activeInterval != currentInterval) {
            currentInterval = activeInterval
            showButton(activeInterval)
        } else if (activeInterval == null && currentInterval != null) {
            currentInterval = null
            hideButton()
        }
    }

    private fun showButton(interval: AniSkipApi.SkipInterval) {
        val label = when (interval.type) {
            AniSkipApi.SkipType.OPENING,
            AniSkipApi.SkipType.MIXED_OPENING -> skipButton.context.getString(R.string.skip_intro)
            AniSkipApi.SkipType.ENDING,
            AniSkipApi.SkipType.MIXED_ENDING  -> skipButton.context.getString(R.string.skip_ending)
        }
        if (skipButton is android.widget.TextView) skipButton.text = label

        skipButton.visibility = View.VISIBLE
        skipButton.startAnimation(
            AnimationUtils.loadAnimation(skipButton.context, android.R.anim.fade_in)
        )
        skipButton.setOnClickListener {
            onSkip(interval.endTime)
            hideButton()
        }
    }

    private fun hideButton() {
        if (skipButton.visibility == View.GONE) return
        skipButton.startAnimation(
            AnimationUtils.loadAnimation(skipButton.context, android.R.anim.fade_out)
        )
        skipButton.visibility = View.GONE
        skipButton.setOnClickListener(null)
    }

    fun reset() {
        fetchJob?.cancel()
        result = null
        currentInterval = null
        hideButton()
    }
}
