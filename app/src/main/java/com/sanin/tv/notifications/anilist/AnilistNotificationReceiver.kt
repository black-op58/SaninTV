package com.sanin.tv.notifications.anilist
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sanin.tv.notifications.AlarmManagerScheduler
import com.sanin.tv.notifications.TaskScheduler
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.util.Logger
import kotlinx.coroutines.runBlocking
class AnilistNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {        Logger.log("AnilistNotificationReceiver: onReceive")        runBlocking(Dispatchers.IO) {            AnilistNotificationTask().execute(context)        }

val anilistInter
val =            AnilistNotificationWorker.checkIntervals[PrefManager.getVal(PrefName.AnilistNotificationInterval)]        AlarmManagerScheduler(context).scheduleRepeatingTask(            TaskScheduler.TaskType.ANILIST_NOTIFICATION,            anilistInter
val        )    }}