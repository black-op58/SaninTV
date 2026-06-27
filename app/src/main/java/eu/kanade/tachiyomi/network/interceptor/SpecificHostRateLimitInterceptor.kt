package eu.kanade.tachiyomi.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class SpecificHostRateLimitInterceptor(
    private val host: String,
    private val requestsPerPeriod: Int = 5,
    private val periodMillis: Long = 1000L
) : Interceptor {
    // TODO: Implementation was not present in the source ZIP
    override fun intercept(chain: Interceptor.Chain): Response {
    return chain.proceed(chain.request())
    }
}
