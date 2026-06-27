package com.sanin.tv.notifications.subscription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sanin.tv.notifications.AlarmManagerScheduler
import com.sanin.tv.notifications.TaskScheduler
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.util.Logger
import kotlinx.coroutines.runBlocking
class SubscriptionNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {        Logger.log("SubscriptionNotificationReceiver: onReceive")        runBlocking(Dispatchers.IO) {            SubscriptionNotificationTask().execute(context)        }

val subscriptionInter
val =            SubscriptionNotificationWorker.checkIntervals[PrefManager.getVal(PrefName.SubscriptionNotificationInterval)]        AlarmManagerScheduler(context).scheduleRepeatingTask(            TaskScheduler.TaskType.SUBSCRIPTION_NOTIFICATION,            subscriptionInter
val        )    }}