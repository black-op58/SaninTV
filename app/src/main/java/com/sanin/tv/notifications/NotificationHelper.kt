package com.sanin.tv.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sanin.tv.R

object NotificationHelper {

    const val CHANNEL_NEW_EPISODES = "new_episodes"
    const val CHANNEL_NAME = "New Episodes"
    const val CHANNEL_DESC = "Notifies when new episodes are available for shows in your watchlist"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_NEW_EPISODES,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showNewEpisodeNotification(
        context: Context,
        mediaId: Int,
        showTitle: String,
        episodeNumber: Int,
        coverUrl: String? = null
    ) {
        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("mediaId", mediaId)
            }

        val pendingIntent = PendingIntent.getActivity(
            context, mediaId,
            intent ?: Intent(),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_NEW_EPISODES)
            .setSmallIcon(R.drawable.ic_round_new_releases_24)
            .setContentTitle(showTitle)
            .setContentText("Episode $episodeNumber is now available")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Episode $episodeNumber of $showTitle is now available. Tap to watch.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(mediaId, notification)
        }
    }

    fun cancelNotification(context: Context, mediaId: Int) {
        NotificationManagerCompat.from(context).cancel(mediaId)
    }
}
