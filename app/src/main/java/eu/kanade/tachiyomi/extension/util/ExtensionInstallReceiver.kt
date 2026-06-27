package eu.kanade.tachiyomi.extension.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ExtensionInstallReceiver : BroadcastReceiver() {
    // TODO: Implementation was not present in the source ZIP
    override fun onReceive(context: Context, intent: Intent) {}

    companion object {
    fun isReplacing(intent: Intent): Boolean {
    return intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
        }
        fun getPackageNameFromIntent(intent: Intent?): String? {
    return intent?.data?.encodedSchemeSpecificPart
        }
    }
}
