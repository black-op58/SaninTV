package com.sanin.tv.connections
import com.sanin.tv.R
import com.sanin.tv.Refresh
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.connections.mal.MAL
import com.sanin.tv.currContext
import com.sanin.tv.feature_streak.StreakManager
import com.sanin.tv.feature_streak.StreakToastHelper
import com.sanin.tv.media.Media
import com.sanin.tv.media.emptyMedia
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
fun updateProgress(media: Media, number: String) {
    val incognito: Boolean = PrefManager.getVal(PrefName.Incognito)    
val rescueMode: Boolean = PrefManager.getVal(PrefName.RescueMode)
if (!incognito) {
if (rescueMode) {            // In rescue mode: cache the update for later AL sync and mirror to MAL
val a = number.toFloatOrNull()?.toInt()
if ((a ?: 0) > (media.userProgress ?: -1)) {
    val status = if (media.userStatus == "REPEATING") media.userStatus ?: "CURRENT" else "CURRENT"                
val pending = PendingProgressUpdate(                    mediaId = media.id,                    idMAL = media.idMAL,                    isAnime = media.anime != null,                    progress = a ?: 0,                    status = status,                )                
val existing: List<PendingProgressUpdate> =                    PrefManager.getVal(PrefName.PendingProgressUpdates, listOf())                
val updated = existing.filterNot { it.mediaId == media.id } + pending                PrefManager.setVal(PrefName.PendingProgressUpdates, updated)                CoroutineScope(Dispatchers.IO).launch {                    MAL.query.editList(                        media.idMAL,                        media.anime != null,                        a, null, status                    )                    toast(currContext()?.getString(R.string.setting_progress, a))                }            }            media.userProgress = number.toFloatOrNull()?.toInt()            Refresh.all()
} else if (Anilist.userid != null) {            CoroutineScope(Dispatchers.IO).launch {
    val a = number.toFloatOrNull()?.toInt()
if ((a ?: 0) > (media.userProgress ?: -1)) {                    Anilist.mutation.editList(                        media.id,                        a,                        status = if (media.userStatus == "REPEATING") media.userStatus else "CURRENT"                    )                    MAL.query.editList(                        media.idMAL,                        media.anime != null,                        a, null,
if (media.userStatus == "REPEATING") media.userStatus ?: "CURRENT" else "CURRENT"                    )                    toast(currContext()?.getString(R.string.setting_progress, a))
                    // ── Streak tracking ──────────────────────────────────────
                    currContext()?.let { ctx ->
                        val newStreak = StreakManager.recordWatchToday(ctx)
                        StreakToastHelper.showIfMilestone(ctx, newStreak)
                    }
                }                media.userProgress = a                Refresh.all()            }
} else {            toast(currContext()?.getString(R.string.login_anilist_account))        }
} else {        toast("Sneaky sneaky :3")    }}/** Sync all pending progress updates (cached during rescue mode) to AniList. */
fun syncPendingProgressUpdates() {
    if (PrefManager.getVal<Boolean>(PrefName.RescueMode)) return
    if (Anilist.userid == null) return
    CoroutineScope(Dispatchers.IO).launch {
        val pending: List<PendingProgressUpdate> =
            PrefManager.getVal(PrefName.PendingProgressUpdates, listOf())
        for (update in pending) {
            try {
                Anilist.mutation.editList(update.mediaId, update.progress, status = update.status)
            } catch (_: Exception) {}
        }
        PrefManager.setVal(PrefName.PendingProgressUpdates, listOf<PendingProgressUpdate>())
        val deletions: List<PendingDeletion> = PrefManager.getVal(PrefName.PendingDeletions, listOf())
        val remaining = deletions.toMutableList()
        for (deletion in deletions) {
            try {
                val anilistId = deletion.mediaId
                val fakeMedia = emptyMedia().copy(id = anilistId, idMAL = deletion.idMAL)
                val listId = Anilist.query.userMediaDetails(fakeMedia).userListId
                if (listId != null) { Anilist.mutation.deleteList(listId) }
                val removeList = PrefManager.getCustomVal("removeList", setOf<Int>())
                PrefManager.setCustomVal("removeList", removeList.minus(anilistId))
                val progressUpdates: List<PendingProgressUpdate> =
                    PrefManager.getVal(PrefName.PendingProgressUpdates, listOf())
                val filteredUpdates = progressUpdates.filterNot { u ->
                    u.mediaId == deletion.mediaId ||
                        (deletion.idMAL != null && u.idMAL == deletion.idMAL && u.mediaId == u.idMAL)
                }
                if (filteredUpdates.size != progressUpdates.size) {
                    PrefManager.setVal(PrefName.PendingProgressUpdates, filteredUpdates)
                }
                if (!Anilist.anilistDisabledSignal) remaining.remove(deletion)
            } catch (_: Exception) {}
        }
        PrefManager.setVal(PrefName.PendingDeletions, remaining)
        if (remaining.isEmpty()) {
            toast(currContext()?.getString(R.string.sync_complete))
        } else {
            toast(currContext()?.getString(R.string.sync_partial, remaining.size))
        }
        Refresh.all()
    }
}
