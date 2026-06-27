package eu.kanade.tachiyomi.data.notification
object Notifications {

private fun createChannels(context: android.content.Context) {

val nm = context.getSystemService(android.app.NotificationManager::class.java)
nm.createNotificationChannels(


/**     * Notification channel used for Incognito Mode     */
    const val CHANNEL_INCOGNITO_MODE = "incognito_mode_channel"    const val ID_INCOGNITO_MODE = -701    
/**     * Notification channel used for comment notifications     */
  
private const val GROUP_COMMENTS = "group_comments"    const val CHANNEL_COMMENTS = "comments_channel"    const val CHANNEL_COMMENT_WARING = "comment_warning_channel"    const val ID_COMMENT_REPLY = -801    const val CHANNEL_APP_GLOBAL = "app_global"    
/**     * Notification channel and ids used for anilist updates.     */
    const val GROUP_ANILIST = "group_anilist"    const val CHANNEL_ANILIST = "anilist_channel"    const val ID_ANILIST = -901    
/**     * Notification channel and ids used subscription checks.     */
    const val GROUP_SUBSCRIPTION_CHECK = "group_subscription_check"    const val CHANNEL_SUBSCRIPTION_CHECK = "subscription_check_channel"    const val CHANNEL_SUBSCRIPTION_CHECK_PROGRESS = "subscription_check_progress_channel"    const val ID_SUBSCRIPTION_CHECK = -1001    const val ID_SUBSCRIPTION_CHECK_PROGRESS = -1002                    setName("Incognito Mode")
}                buildNotificationChannel(CHANNEL_COMMENTS, IMPORTANCE_HIGH) {
    setName("Comments")                    setGroup(GROUP_COMMENTS)
},                buildNotificationChannel(CHANNEL_COMMENT_WARING, IMPORTANCE_HIGH) {
    setName("Comment Warnings")                    setGroup(GROUP_COMMENTS)
},                buildNotificationChannel(CHANNEL_ANILIST, IMPORTANCE_DEFAULT) {
    setName("Anilist")                    setGroup(GROUP_ANILIST)
},                buildNotificationChannel(CHANNEL_SUBSCRIPTION_CHECK, IMPORTANCE_LOW) {
    setName("Subscription Checks")                    setGroup(GROUP_SUBSCRIPTION_CHECK)
},                buildNotificationChannel(CHANNEL_SUBSCRIPTION_CHECK_PROGRESS, IMPORTANCE_DEFAULT) {
    setName("Subscription Checks Progress")                    setGroup(GROUP_SUBSCRIPTION_CHECK)
},                buildNotificationChannel(CHANNEL_APP_GLOBAL, IMPORTANCE_HIGH) {
    setName("Global Updates")
},                buildNotificationChannel(CHANNEL_APP_UPDATE, IMPORTANCE_DEFAULT) {
    setGroup(GROUP_APK_UPDATES)                    setName("App Updates")
},                buildNotificationChannel(CHANNEL_EXTENSIONS_UPDATE, IMPORTANCE_DEFAULT) {
    setGroup(GROUP_APK_UPDATES)                    setName("Extension Updates")
},            ),        
}
