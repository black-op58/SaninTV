package eu.kanade.tachiyomi.network
import android.content.Context
import android.os.Build
import com.sanin.tv.Mapper
import com.sanin.tv.settings.saving.PrefManager
import com.sanin.tv.settings.saving.PrefName
import com.sanin.tv.util.Logger
import com.lagradost.nicehttp.Requests
import eu.kanade.tachiyomi.network.interceptor.CloudflareInterceptor
import eu.kanade.tachiyomi.network.interceptor.IgnoreGzipInterceptor
import eu.kanade.tachiyomi.network.interceptor.UncaughtExceptionInterceptor
import eu.kanade.tachiyomi.network.interceptor.UserAgentInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.brotli.BrotliInterceptor
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Authenticator
import java.net.PasswordAuthentication
import java.util.prefs.Preferences
import okhttp3.Credentials
import okhttp3.Response
import okhttp3.Route
class NetworkHelper(    context: Context) {   init {     setupSocks5Proxy()   }

private fun setupSocks5Proxy() {
    val proxyEnabled = PrefManager.getVal<Boolean>(PrefName.EnableSocks5Proxy)
if (proxyEnabled) {
    val proxyHost = PrefManager.getVal<String>(PrefName.Socks5ProxyHost)            
val proxyPort = PrefManager.getVal<String>(PrefName.Socks5ProxyPort)            System.setProperty("socksProxyHost", proxyHost)            System.setProperty("socksProxyPort", proxyPort)
if (PrefManager.getVal<Boolean>(PrefName.ProxyAuthEnabled)) {
    val proxyUsername = PrefManager.getVal<String>(PrefName.Socks5ProxyUsername)                
val proxyPassword = PrefManager.getVal<String>(PrefName.Socks5ProxyPassword)                Authenticator.setDefault(
object : Authenticator() {
    override fun getPasswordAuthentication(): PasswordAuthentication {
return PasswordAuthentication(proxyUsername, proxyPassword.toCharArray())                    }                 }              )            }
} else {            System.clearProperty("socksProxyHost")            System.clearProperty("socksProxyPort")            Authenticator.setDefault(null)        }    }

val cookieJar = AndroidCookieJar()    
val client: OkHttpClient = run {
    val builder = OkHttpClient.Builder()            .cookieJar(cookieJar)            .connectTimeout(30, TimeUnit.SECONDS)            .readTimeout(30, TimeUnit.SECONDS)            .callTimeout(2, TimeUnit.MINUTES)            .cache(                Cache(                    directory = File(context.externalCacheDir ?: context.cacheDir, "network_cache"),                    maxSize = 5L * 1024 * 1024, // 5 MiB                ),            )            .addInterceptor(UncaughtExceptionInterceptor())            .addInterceptor(UserAgentInterceptor(::defaultUserAgentProvider))            .addNetworkInterceptor(IgnoreGzipInterceptor())     * @deprecated Since extension-lib 1.5     */    
@Deprecated("The regular client handles Cloudflare by default")    
@Suppress("UNUSED")    
val cloudflareClient: OkHttpClient = client
val requestClient = Requests(        client,        mapOf(            "User-Agent" to                    defaultUserAgentProvider()                        .format(Build.VERSION.RELEASE, Build.MODEL)        ),        defaultCacheTime = 6,        defaultCacheTimeUnit = TimeUnit.HOURS,        responseParser = Mapper    )    
companion object {
    fun defaultUserAgentProvider() = PrefManager.getVal<String>(PrefName.DefaultUserAgent)    }}
}))
