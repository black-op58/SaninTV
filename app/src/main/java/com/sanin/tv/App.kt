package com.sanin.tv

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.sanin.tv.feature_newepisode.NewEpisodeBadgeManager
import com.sanin.tv.notifications.EpisodeNotificationWorker
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName

class App : Application() {
    private val mFTActivityLifecycleCallbacks = FTActivityLifecycleCallbacks()

    override fun onCreate() {
        super.onCreate()
        instance = this
        context = applicationContext
        registerActivityLifecycleCallbacks(mFTActivityLifecycleCallbacks)
        scheduleBackgroundTasks()
    }

    private fun scheduleBackgroundTasks() {
        PrefManager.init(this)
        NewEpisodeBadgeManager.init(this)
        if (PrefManager.getVal<Boolean>(PrefName.NewEpisodeNotifications)) {
            EpisodeNotificationWorker.schedule(this)
        }
    }

    inner class FTActivityLifecycleCallbacks : ActivityLifecycleCallbacks {
        var currentActivity: Activity? = null
        var lastActivity: String? = null
        override fun onActivityCreated(p0: Activity, p1: Bundle?) {
            lastActivity = p0.javaClass.simpleName
        }
        override fun onActivityStarted(p0: Activity) {
            currentActivity = p0
        }
        override fun onActivityResumed(p0: Activity) {
            currentActivity = p0
        }
        override fun onActivityPaused(p0: Activity) {}
        override fun onActivityStopped(p0: Activity) {
            // Persist current episode counts so the next session can detect new episodes.
            NewEpisodeBadgeManager.onAppBackground(p0.applicationContext)
        }
        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
        override fun onActivityDestroyed(p0: Activity) {}
    }

    companion object {
        var instance: App? = null

        /**
         * Reference to the application context.
         * USE WITH EXTREME CAUTION!
         */
        var context: Context? = null

        fun currentContext(): Context? {
    return instance?.mFTActivityLifecycleCallbacks?.currentActivity ?: context
        }

        fun currentActivity(): Activity? {
    return instance?.mFTActivityLifecycleCallbacks?.currentActivity
        }
    }
}
