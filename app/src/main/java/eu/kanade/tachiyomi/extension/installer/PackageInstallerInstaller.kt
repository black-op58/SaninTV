package eu.kanade.tachiyomi.extension.installer
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.IntentSanitizer
import com.sanin.tv.R
import com.sanin.tv.snackString
import com.sanin.tv.toast
import com.sanin.tv.util.Logger
import eu.kanade.tachiyomi.extension.InstallStep
import eu.kanade.tachiyomi.util.lang.use
import eu.kanade.tachiyomi.util.system.getParcelableExtraCompat
import eu.kanade.tachiyomi.util.system.getUriSize
class PackageInstallerInstaller(
private val service: Service) : Installer(service) {
    private val packageInstaller = service.packageManager.packageInstaller    
private val packageActionReceiver = 
object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
when (intent.getIntExtra(                PackageInstaller.EXTRA_STATUS,                PackageInstaller.STATUS_FAILURE            )) {                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
    val userAction =                        intent.getParcelableExtraCompat<Intent>(Intent.EXTRA_INTENT)?.run {                            IntentSanitizer.Builder()                                .allowAction(this.action!!)                                .allowExtra(PackageInstaller.EXTRA_SESSION_ID) { id -> id == activeSession?.second }                                .allowAnyComponent()                                .allowPackage {                                    // There is no way to check the actual installer name so allow all.                                    true                                }                                .build()                                .sanitizeByFiltering(this)                        }
if (userAction == null) {                        Logger.log("Fatal error for $intent")                        continueQueue(InstallStep.Error)
return                    }                    userAction.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)                    service.startActivity(userAction)                }                PackageInstaller.STATUS_FAILURE_ABORTED -> {                    continueQueue(InstallStep.Idle)                }                PackageInstaller.STATUS_SUCCESS -> continueQueue(InstallStep.Installed)                PackageInstaller.STATUS_FAILURE_CONFLICT -> {                    Logger.log("Failed to install extension due to conflict")                    toast(context.getString(R.string.failed_ext_install_conflict))                    continueQueue(InstallStep.Error)
}
else -> {                    Logger.log("Fatal error for $intent")                    Logger.log("Status: ${intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)}")                    continueQueue(InstallStep.Error)                }            }        }    }

private var activeSession: Pair<Entry, Int>? = null    // Always ready    
override var ready = true    
override fun processEntry(entry: Entry) {        super.processEntry(entry)        }    }

override fun cancelEntry(entry: Entry): Boolean {        activeSession?.let { (activeEntry, sessionId) ->
if (activeEntry == entry) {                packageInstaller.abandonSession(sessionId)
return false            }        }
return true    }

override fun onDestroy() {        service.unregisterReceiver(packageActionReceiver)        super.onDestroy()    }    init {        ContextCompat.registerReceiver(            service,            packageActionReceiver,            IntentFilter(INSTALL_ACTION),            ContextCompat.RECEIVER_EXPORTED,        )    }}

private const val INSTALL_ACTION = "PackageInstallerInstaller.INSTALL_ACTION"
}
