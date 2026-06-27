package com.sanin.tv.notifications
import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.sanin.tv.notifications.TaskScheduler.TaskType
import com.sanin.tv.notifications.anilist.AnilistNotificationWorker
import com.sanin.tv.notifications.comment.CommentNotificationWorker
import com.sanin.tv.notifications.subscription.SubscriptionNotificationWorker
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.util.Logger
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
    val scheduler = AlarmManagerScheduler(context)            PrefManager.init(context)            Logger.init(context)            Logger.log("Starting SaninTV Subscription Service on Boot")
if (PrefManager.getVal(PrefName.UseAlarmManager)) {
    val commentInter
val =                    CommentNotificationWorker.checkIntervals[PrefManager.getVal(PrefName.CommentNotificationInterval)]                
val anilistInter
val =                    AnilistNotificationWorker.checkIntervals[PrefManager.getVal(PrefName.AnilistNotificationInterval)]                
val subscriptionInter
val =                    SubscriptionNotificationWorker.checkIntervals[PrefManager.getVal(PrefName.SubscriptionNotificationInterval)]                scheduler.scheduleRepeatingTask(                    TaskType.COMMENT_NOTIFICATION,                    commentInter
val                )                scheduler.scheduleRepeatingTask(                    TaskType.ANILIST_NOTIFICATION,                    anilistInter
val                )                scheduler.scheduleRepeatingTask(                    TaskType.SUBSCRIPTION_NOTIFICATION,                    subscriptionInter
val                )            }        }    }}

class AlarmPermissionStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
if (intent?.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) {            PrefManager.init(context)            
val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
val canScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {                alarmManager.canScheduleExactAlarms()
} else {                true            }
if (canScheduleExactAlarms) {                TaskScheduler.create(context, false).cancelAllTasks()                TaskScheduler.create(context, true).scheduleAllTasks(context)
} else {                TaskScheduler.create(context, true).cancelAllTasks()                TaskScheduler.create(context, false).scheduleAllTasks(context)            }            PrefManager.setVal(PrefName.UseAlarmManager, canScheduleExactAlarms)        }    }}