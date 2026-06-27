package com.sanin.tv.notifications.comment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sanin.tv.notifications.AlarmManagerScheduler
import com.sanin.tv.notifications.TaskScheduler
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.util.Logger
import kotlinx.coroutines.runBlocking
class CommentNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {        Logger.log("CommentNotificationReceiver: onReceive")        runBlocking(Dispatchers.IO) {            CommentNotificationTask().execute(context)        }

val commentInter
val =            CommentNotificationWorker.checkIntervals[PrefManager.getVal(PrefName.CommentNotificationInterval)]        AlarmManagerScheduler(context).scheduleRepeatingTask(            TaskScheduler.TaskType.COMMENT_NOTIFICATION,            commentInter
val        )    }}